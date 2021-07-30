package com.github.astronoodles.peerpal.base;

import com.github.astronoodles.peerpal.LessonScreen;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
