package com.github.astronoodles.peerpal.base;

import com.github.astronoodles.peerpal.LessonScreen;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LanguageTextEditor extends Application {


    @FXML
    public TextArea area;

    @FXML
    public Button button, lang_dialog;

    @FXML
    public Label notice;

    @FXML
    public ComboBox<String> languages;

    @FXML
    public VBox errorBox;

    private String username;
    private static int hotkeyID = 0;
    private final Map<Character, Integer> hotkeyMap = new HashMap<>(10);

    public static void main(String[] args){
        launch(args);
    }

    @Override
    // DEBUG ONLY. DELETE WHEN READY
    public void start(Stage primaryStage) {
        StageHelper.loadSceneFXML("The Language Learn App", 600, 500,
                "/main_screen.fxml", "/main_screen.css");
    }

    public void connectToUser(String username) {
        this.username = username;
    }

    private JLanguageTool textAreaCheck(){

        if(area.getText().equals("")) {
            return null;
        }

        if (languages.getValue() == null) {
            languages.setValue("Spanish");
        }

        System.out.println(languages.getValue());

        return new JLanguageTool(langToClass().get(languages.getValue()));
    }

    @FXML
    protected void checkLanguage(){
        try {
            JLanguageTool checker = textAreaCheck();
            if(checker != null) {
                List<RuleMatch> errors = checker.check(area.getText());

                if (errors.size() == 0) {
                    notice.setVisible(false);
                    lang_dialog.setVisible(false);
                    errorBox.getChildren().clear();
                    successDialog();
                } else {
                    notice.setVisible(true);
                    lang_dialog.setVisible(true);
                }
            }
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @FXML
    protected void submitAssignment(){
        System.out.println("Submitting:" + area.getText());
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Your File Save Location");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("TXT files (.txt)", "*.txt"));
        File loc = chooser.showSaveDialog(lang_dialog.getScene().getWindow());

        if(loc != null){
            Path p = loc.toPath();
            try(BufferedWriter writer = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE)){
                writer.write(area.getText());
            } catch(IOException ex){
                ex.printStackTrace();
            }
        }

    }

    @FXML
    public void fixErrors() {
        System.out.println("Showing dialog");
        enterMistakes();
    }

    // Menu Items

    @FXML
    public void shortcutAccentDialog(KeyEvent ke) {
       if(ke.isShiftDown() && ke.getCode().equals(KeyCode.A)){
           openAccentDialog();
       }
    }

    @FXML
    public void openAccentDialog() {
        GridPane gridButtons = new GridPane();
        gridButtons.setVgap(2);
        gridButtons.setHgap(3);
        int rowMax = 7;

        String languageSelected = languages.getValue();

        if(languageSelected == null) {
            Alert nullLangAlert = new Alert(Alert.AlertType.WARNING, "No language has been selected in the dropdown. " +
                    "\nPlease select a language so that the correct accents can be loaded for your language.", ButtonType.OK);
            nullLangAlert.setHeaderText("No Language Selected");
            nullLangAlert.setTitle("Cannot Load Accent Dialog");
            nullLangAlert.showAndWait();
            return;
        }

        switch(languages.getValue()) {
            case "Spanish":
                char[] spanishChars = new char[] {'Á', 'É', 'Í', 'Ó', 'Ú', 'Ü', 'Ñ', '¿', '¡'};
                int gridArea = 0;
                for (char spanishChar : spanishChars) {
                    Button charButton = createCharacterMapButton(spanishChar, area);

                    gridButtons.add(charButton, gridArea % rowMax, Math.floorDiv(gridArea, rowMax));

                    char lowerChar = Character.toLowerCase(spanishChar);

                    if (lowerChar != spanishChar) {
                        Button lowerCharButton = createCharacterMapButton(lowerChar, area);
                        gridButtons.add(lowerCharButton, (gridArea + 1) % rowMax, Math.floorDiv(gridArea + 1, rowMax));
                        gridArea += 2;
                    } else gridArea++;
                }
                break;
            case "Russian":
            case "Korean":
            case "Urdu":
            default:
                Button nothingYet = new Button("Not implemented yet. Hold on maestro.");
                nothingYet.setPrefWidth(100);
                nothingYet.setPrefHeight(100);
                nothingYet.getStyleClass().add("avatarPick");
                gridButtons.add(nothingYet, 0, 0, 2, 1);
                break;
        }

        Stage accentStage = new Stage();
        accentStage.setScene(new Scene(gridButtons, 200, 100));
        accentStage.setTitle("Accent Dialog");
        accentStage.show();
    }

    @FXML
    public void onEnterHotkey(KeyEvent ke) {
        String keyCodeName = ke.getCode().getName();
        for(Map.Entry<Character, Integer> entrySet : hotkeyMap.entrySet()) {
            if(ke.isShiftDown() && Character.isDigit(keyCodeName.charAt(0))) {
                if(entrySet.getValue() == Integer.parseInt(keyCodeName)) {
                    area.setText(area.getText() + entrySet.getKey());
                }
            } else break;
        }
    }

    private Button createCharacterMapButton(char charText, TextArea area) {
        Button charMapButton = new Button(String.valueOf(charText));
        charMapButton.setPrefHeight(40);
        charMapButton.setPrefWidth(40);
        charMapButton.getStyleClass().add("avatarPick");

        charMapButton.setOnAction(event -> area.setText(area.getText() + charMapButton.getText()));

        charMapButton.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton() == MouseButton.SECONDARY) {
                MenuItem hotkeyItem = new MenuItem("Create Hotkey");
                char buttonChar = charMapButton.getText().charAt(0);
                if(!hotkeyMap.containsKey(buttonChar)) {
                    hotkeyItem.setOnAction(event -> {
                        Alert hotkeyAlert = new Alert(Alert.AlertType.INFORMATION, "Do you want to create a hotkey for this accent?" +
                                "\nBy creating a hotkey, you can easily type an accent by clicking Shift + 1-9.\n" +
                                "Your hotkeys will reset when you restart PeerPal", ButtonType.YES, ButtonType.NO);
                        hotkeyAlert.setTitle("Hotkey Alert");
                        hotkeyAlert.setHeaderText("What is a hotkey?");

                        Optional<ButtonType> hotkeyOptional = hotkeyAlert.showAndWait();

                        if (hotkeyOptional.isPresent() && hotkeyOptional.get() == ButtonType.YES) {
                            hotkeyMap.put(buttonChar, hotkeyID);

                            Alert hotKeyInsert = new Alert(Alert.AlertType.INFORMATION,
                                    String.format("Your new hotkey for %c is set to Shift + %d!", buttonChar, hotkeyID), ButtonType.OK);
                            hotKeyInsert.setHeaderText("Hotkey Created!");
                            hotKeyInsert.setTitle("Hotkey Update");
                            hotKeyInsert.showAndWait();

                            hotkeyID++;
                        }
                    });
                } else if(hotkeyMap.size() == 10) {
                    Alert hotKeyFull = new Alert(Alert.AlertType.WARNING, "Only 10 Hotkeys Are Allowed At A Time.",ButtonType.OK);
                    hotKeyFull.setHeaderText("Too Many Hotkeys!");
                    hotKeyFull.setTitle("Hotkey List Filled!");
                    hotKeyFull.showAndWait();
                } else {
                    hotkeyItem.setOnAction(event -> {
                        Alert alreadyHaveHotkey = new Alert(Alert.AlertType.WARNING, String.format("You already have a hotkey for this accent." +
                                "\nIt is set to the key press Shift + %d.", hotkeyMap.get(buttonChar)), ButtonType.OK);
                        alreadyHaveHotkey.setHeaderText("You Already Have This Hotkey On!");
                        alreadyHaveHotkey.setTitle("Activated Hotkey!");
                        alreadyHaveHotkey.showAndWait();
                    });
                }
                ContextMenu hotkeyMenu = new ContextMenu(hotkeyItem);
                hotkeyMenu.show(charMapButton, mouseEvent.getX(), mouseEvent.getY());
            }
        });

        return charMapButton;
    }

    private void successDialog(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Great Job!");
        alert.setContentText("No mistakes were found. Keep going!");
        alert.setTitle("Notice");
        alert.show();
    }

    private void enterMistakes(){
        try {
            JLanguageTool checker = textAreaCheck();
            if(checker != null) {
                List<RuleMatch> errors = checker.check(area.getText());
                System.out.println(errors.size() + " " + area.getText());
                for (RuleMatch error : errors) {

                    ReplacementTriad triad = new ReplacementTriad(error.getFromPos(), error.getToPos(),
                            error.getSuggestedReplacements());
                    triad.setMessage(error.getMessage());
                    System.out.println(triad);

                    if(errorBox.getChildren().size() > 0) errorBox.getChildren().clear();
                    addErrorCards(triad);

                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void addErrorCards(ReplacementTriad triad){
        VBox card = createErrorCard(ErrorType.explanationToError(triad.message),
                triad.message, triad.replacements, triad);
        errorBox.getChildren().add(card);
    }

    private Border errorCardBorder(){
        BorderStroke cardStroke = new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(2),
                new BorderWidths(1));
        return new Border(cardStroke);
    }


    public VBox createErrorCard(ErrorType type, String explanation, List<String> errors, ReplacementTriad triad){
        VBox errorCard = new VBox();
        errorCard.setPrefHeight(350);
        errorCard.setPrefWidth(600);
        errorCard.setMaxWidth(Double.MAX_VALUE);
        errorCard.setSpacing(3);
        errorCard.setBorder(errorCardBorder());

        Insets padding = new Insets(5, 0, 0, 10);
        errorCard.setPadding(padding);

        Label title = new Label(type.getCodeName());
        title.setPrefHeight(20);
        title.setPrefWidth(100);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        title.getStyleClass().add("login_plain");

        Label text = new Label(explanation);
        text.setMaxWidth(Double.MAX_VALUE);
        text.setPrefWidth(200);
        text.setPrefHeight(100);
        text.getStyleClass().add("login_plain");
        text.setWrapText(true);

        ButtonBar errorButtons = new ButtonBar();
        errorButtons.setPrefHeight(200);
        errorButtons.setMaxWidth(Double.MAX_VALUE);
        errorButtons.setPrefWidth(100);
        errorButtons.getStyleClass().add("login_plain");
        errorButtons.setStyle("-fx-background-color: aliceblue");

        if(errors.size() > 0) {
            for (String error : errors) {
                Button errorButton = new Button(error);
                errorButton.setPrefWidth(50);
                errorButton.setMaxWidth(Double.MAX_VALUE);
                errorButton.setWrapText(true);
                errorButton.setAlignment(Pos.CENTER);
                errorButton.setPrefHeight(30);
                errorButton.getStyleClass().add("login_plain");
                errorButton.setOnAction((event) -> {
                    area.replaceText(triad.startPos, triad.endPos, error);
                    lang_dialog.setVisible(false);
                    notice.setVisible(false);
                    errorBox.getChildren().remove(errorButtons.getParent());
                });
                errorButtons.getButtons().add(errorButton);
            }
        } else {
            Button quitButton = new Button("Recheck?");
            quitButton.setPrefWidth(200);
            quitButton.setMaxWidth(Double.MAX_VALUE);
            quitButton.setWrapText(true);
            quitButton.setPrefHeight(20);
            quitButton.setAlignment(Pos.CENTER);
            quitButton.getStyleClass().add("login_plain");
            quitButton.setOnAction((event -> {
                lang_dialog.setVisible(false);
                notice.setVisible(false);
                errorBox.getChildren().remove(errorButtons.getParent());
            }));
            errorButtons.getButtons().add(quitButton);
        }
        Region space = new Region();
        space.setPrefWidth(30);
        space.setPrefWidth(50);

        Button learnMore = new Button("Learn More");
        learnMore.setPrefHeight(20);
        learnMore.setPrefWidth(100);
        learnMore.getStyleClass().add("login_plain");
        learnMore.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(learnMore, Priority.ALWAYS);
        learnMore.setOnAction((e) -> {
            Stage s = new Stage();
            LessonScreen ls = new LessonScreen(username, String.format("./src/main/java/com/github/astronoodles/" +
                    "peerpal/lessons/%s.html", type.getCodeName()));
            Scene sc = new Scene(ls.loadScene(type), 750, 750, true);
            s.setScene(sc);
            s.setTitle(String.format("%s Lesson", type.getErrorName()));
            s.show();
        });

        errorCard.getChildren().addAll(title, text, errorButtons, space, learnMore);

        return errorCard;
    }


    private HashMap<String, Language> langToClass(){
        HashMap<String, Language> langToClass = new HashMap<>();
        langToClass.put("English", new AmericanEnglish());
        langToClass.put("Spanish", new Spanish());
        langToClass.put("British English", new BritishEnglish());
        return langToClass;
    }


    private static class ReplacementTriad {
        private final int startPos;
        private final int endPos;
        private final List<String> replacements;
        private String message;

        private ReplacementTriad(int startPos, int endPos, List<String> replacements) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.replacements = replacements;
        }


        public void setMessage(String message) {
            this.message = message;
        }


        @Override
        public String toString() {
            return String.format("Error: %s from pos %d to %d", message, startPos, endPos);
        }
    }

}
