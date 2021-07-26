package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.ErrorType;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LessonScreen {

    private static String[] lessonParts;
    private final String username;

    public LessonScreen(String username, String htmlLesson) {
        this.username = username;
        lessonParts = readLesson(htmlLesson).split("<br><br>");
    }

    public TabPane loadScene(ErrorType type) {
        TabPane tabPane = new TabPane();
        Map<String, String> userAvatarMap = StageHelper.getUserAvatarMapping();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // handle obtaining the model ID out of the complete relative avatar path in the user avatar mapping
        int modelID = 1;
        String relativePath = userAvatarMap.get(username);
        if (!relativePath.isEmpty()) {
            Matcher digitMatcher = Pattern.compile("\\d+").matcher(relativePath);
            digitMatcher.find();
            modelID = Integer.parseInt(digitMatcher.group());
        }

        for (int i = 0; i < lessonParts.length; i++) {
            WebView htmlText = new WebView();
            htmlText.setPrefHeight(500);
            htmlText.setPrefWidth(300);
            System.out.println(lessonParts[i]);
            htmlText.getEngine().loadContent(String.format(lessonParts[i],
                    modelID));
            htmlText.getEngine().setUserStyleSheetLocation(getClass().getResource("/lessons.css").toString());

            String tabTitle = String.format("Part %d", i + 1).equals("Part 1") && lessonParts.length == 1 ? "Lesson"
                    : String.format("Part %d", i + 1);
            Tab lessonPart = new Tab(tabTitle, htmlText);
            tabPane.getTabs().add(lessonPart);
        }

        Tab exerciseTab = new Tab("Exercises", addExerciseTab(type));
        tabPane.getTabs().add(exerciseTab);

        return tabPane;
    }


    private ScrollPane addDropdown(Map<String, ExerciseTriad> options) {
        VBox exercises = new VBox(2);

        Label title = new Label("Exercises");
        title.setFont(Font.font("Charter", 30));
        title.setPrefWidth(200);
        title.setMaxHeight(50);

        Label desc = new Label("Now that you are familiar with this topic, " +
                "practice your skills on the following problems by filling in the blank.");
        desc.setFont(Font.font("Arial", FontWeight.MEDIUM, FontPosture.ITALIC, 12));
        desc.setWrapText(true);
        desc.setPadding(new Insets(10, 0, 10, 0));

        int i = 1;
        for (Map.Entry<String, ExerciseTriad> entry : options.entrySet()) {
            Label exerciseLabel = new Label(String.format("%d. %s", i, entry.getKey()));
            exerciseLabel.setFont(Font.font("Verdana", 13));
            ComboBox<String> dropdown = new ComboBox<>(
                    FXCollections.observableArrayList(entry.getValue().solutions));
            HBox exercise = new HBox(9, exerciseLabel, dropdown);

            Label solutionLabel = new Label(entry.getValue().wrongSolutionMsg);
            solutionLabel.setMaxHeight(50);
            solutionLabel.setPadding(new Insets(10, 0, 15, 0));
            solutionLabel.setPrefWidth(200);
            solutionLabel.setVisible(false);

            dropdown.setOnAction((e) -> {
                int solutionIndex = entry.getValue().solutionIndex;
                if (dropdown.getValue().equals(entry.getValue().solutions[solutionIndex])) {
                    solutionLabel.setText(entry.getValue().correctSolutionMsg);
                    solutionLabel.setTextFill(Color.GREEN);
                    solutionLabel.setFont(Font.font("Charter", 13));
                } else {
                    solutionLabel.setText(entry.getValue().wrongSolutionMsg);
                    solutionLabel.setTextFill(Color.RED);
                    solutionLabel.setFont(Font.font("Charter", 12));
                }
                solutionLabel.setVisible(true);
            });

            VBox exerciseSolution = new VBox(4, exercise, solutionLabel);
            exercises.getChildren().add(exerciseSolution);
            i++;
        }
        exercises.getChildren().add(0, title);
        exercises.getChildren().add(1, desc);
        exercises.setPadding(new Insets(10, 10, 20, 20));

        return new ScrollPane(exercises);
    }


    private String readLesson(String lessonString) {
        Path lessonPath = Paths.get(lessonString);
        try (BufferedReader reader = Files.newBufferedReader(lessonPath)) {
            StringBuilder sb = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                sb.append(line);
            }
            //System.out.println("does this method even run?");
            return sb.toString();
        } catch (NoSuchFileException e) {
            return "<p> The developers have not created this lesson yet. Please contact them for further info.<p>";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lessonString;
    }

    private ScrollPane addExerciseTab(ErrorType type) {
        // TODO Find a better way to do this?
        switch (type) {
            case GEN:
                return addDropdown(createGenderMap());
            case ART:
                return new ScrollPane(new VBox());
        }

        return new ScrollPane(new VBox()); // This should never happen
    }

    // CREATE MAPS FOR EACH LESSON
    private Map<String, ExerciseTriad> createGenderMap() {
        String correctMessage1 = "This solution is correct!";
        String wrongMessage1 = "This solution is incorrect!";
        Map<String, ExerciseTriad> genderMap = new HashMap<>(10);

        genderMap.put("Ana es _____", new ExerciseTriad(
                new String[]{"bueno", "buena"}, 0, correctMessage1, wrongMessage1));
        genderMap.put("Carlos y yo somos _____", new ExerciseTriad(
                new String[]{"inteligente", "inteligentes"}, 1, correctMessage1, wrongMessage1));
        genderMap.put("Ella es ____", new ExerciseTriad(
                new String[]{"cómico", "cómica"}, 1, correctMessage1, wrongMessage1));
        genderMap.put("Pedro y Carmen son _____", new ExerciseTriad(
                new String[]{"perezoso", "perezosos", "perezosa",
                        "perezosas"}, 1, correctMessage1, wrongMessage1));
        genderMap.put("Mi maestra es _____", new ExerciseTriad(new String[]{"bueno", "buena"},
                1, correctMessage1, wrongMessage1));
        genderMap.put("Juan es _____", new ExerciseTriad(new String[]{"alto", "alta", "altos", "altas"},
                0, correctMessage1, wrongMessage1));
        genderMap.put("El perro es _____", new ExerciseTriad(
                new String[]{"malo", "mala", "malas", "malos"}, 0, correctMessage1, wrongMessage1));
        genderMap.put("La chica ____ está en la clase.", new ExerciseTriad(
                new String[]{"Alto", "Alta", "Altos", "Altas"}, 1, correctMessage1, wrongMessage1));
        genderMap.put("Hoy es un día _____.", new ExerciseTriad(
                new String[]{"bonito", "bonita", "bonitos", "bonitas"}, 1, correctMessage1, wrongMessage1));
        genderMap.put("Translate \"One Pleasant Girl\" ",
                new ExerciseTriad(new String[]{"Una muchacha agradable", "Una muchacha agradabla"}, 1, correctMessage1, wrongMessage1));

        return genderMap;
    }

    private static class ExerciseTriad {
        private final String[] solutions;
        private final int solutionIndex;
        private final String correctSolutionMsg;
        private final String wrongSolutionMsg;

        private ExerciseTriad(String[] solutions, int solutionIndex, String correctMsg, String wrongMsg) {
            this.solutions = solutions;
            this.solutionIndex = solutionIndex;
            this.correctSolutionMsg = correctMsg;
            this.wrongSolutionMsg = wrongMsg;
        }

    }
}
