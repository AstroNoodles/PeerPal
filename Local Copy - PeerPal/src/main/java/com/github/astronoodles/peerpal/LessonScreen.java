package com.github.astronoodles.peerpal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.astronoodles.peerpal.base.ErrorType;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LessonScreen {

    private static String[] lessonParts;
    private final String username;

    public LessonScreen(String username, String htmlLesson) {
        this.username = username;
        lessonParts = readLesson(htmlLesson).split("<br><br>");
    }

    public TabPane loadScene(ErrorType type) {
        TabPane tabPane = new TabPane();
        Map<String, Integer> studentAvatarMap = StageHelper.getUserAvatarMapping();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        ImageView avatarView = new ImageView(new Image(getClass().getResource(String.format("/avatars/D%d.png",
                studentAvatarMap.getOrDefault(username, 2))).toExternalForm(),
                200, 200, true, true));

        Label avatarSpeak = new Label("Hey I'm back! Remember to take notes on these lessons " +
                "and to do your best on the exercises. Think before you click an answer. " +
                "I know you will get 100% and learn from your mistakes!");
        avatarSpeak.setWrapText(true);
        avatarSpeak.setPrefWidth(400);
        avatarSpeak.setPrefHeight(200);
        avatarSpeak.setPadding(new Insets(2, 5, 5, 20));
        avatarSpeak.setStyle("-fx-font-size: 14; " +
                "-fx-font-family: \"Avenir Next\", Helvetica, Arial, sans-serif;");

        Timeline avatarTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(avatarSpeak.textFillProperty(), Color.web("#1565c0"))),
                new KeyFrame(Duration.ZERO, new KeyValue(avatarSpeak.opacityProperty(), 0.05)),
                new KeyFrame(Duration.seconds(3), new KeyValue(avatarSpeak.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(6), new KeyValue(avatarSpeak.textFillProperty(), Color.web("#6a1b9a")))
        );
        avatarTimeline.setAutoReverse(true);
        avatarTimeline.playFromStart();

        //FillTransition speakFill = new FillTransition(Duration.seconds(10), Color.web("#1565c0"), Color.web("#6a1b9a"));

        HBox avatarArea = new HBox(4, avatarView, avatarSpeak);

        for (int i = 0; i < lessonParts.length; i++) {
            WebView htmlText = new WebView();
            htmlText.setPrefHeight(500);
            htmlText.setPrefWidth(300);
            System.out.println(lessonParts[i]);
            htmlText.getEngine().loadContent(lessonParts[i]);
            htmlText.getEngine().setUserStyleSheetLocation(getClass().getResource("/lessons.css").toString());

            String tabTitle = String.format("Part %d", i + 1).equals("Part 1") && lessonParts.length == 1 ? "Lesson"
                    : String.format("Part %d", i + 1);
            Tab lessonPart = new Tab(tabTitle, new VBox(1, htmlText, avatarArea));

            tabPane.getTabs().add(lessonPart);
        }

        Tab exerciseTab = new Tab("Exercises", addExerciseTab(type));
        tabPane.getTabs().add(exerciseTab);

        return tabPane;
    }


    private ScrollPane addDropdown(List<LessonExercise> options) {
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
        for (LessonExercise exercise : options) {
            Label exerciseLabel = new Label(String.format("%d. %s", i, exercise.question));
            exerciseLabel.setFont(Font.font("Verdana", 13));
            ComboBox<String> dropdown = new ComboBox<>(
                    FXCollections.observableArrayList(exercise.possibleAnswers));
            HBox exerciseBox = new HBox(9, exerciseLabel, dropdown);

            Label solutionLabel = new Label(exercise.wrongResponse);
            solutionLabel.setMaxHeight(200);
            solutionLabel.setPadding(new Insets(10, 0, 15, 0));
            solutionLabel.setPrefWidth(exercises.getWidth());
            solutionLabel.setWrapText(true);
            solutionLabel.setVisible(false);

            dropdown.setOnAction((e) -> {
                if (dropdown.getValue().equals(exercise.correctAnswer)) {
                    solutionLabel.setText(exercise.correctResponse);
                    solutionLabel.setTextFill(Color.GREEN);
                    solutionLabel.setFont(Font.font("Charter", 13));
                } else {
                    solutionLabel.setText(exercise.wrongResponse);
                    solutionLabel.setTextFill(Color.RED);
                    solutionLabel.setFont(Font.font("Charter", 12));
                }
                solutionLabel.setVisible(true);
            });

            VBox exerciseSolution = new VBox(4, exerciseBox, solutionLabel);
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
        return addDropdown(readExercisesFromJSON(type));
    }

    // CREATE MAPS FOR EACH LESSON
    private List<LessonExercise> readExercisesFromJSON(ErrorType type) {
        ObjectMapper mapper = new ObjectMapper(); // since I need to use JACKSON for Azure, why not use it here?
        List<LessonExercise> exerciseList = new LinkedList<>();
        File exerciseFile = new File(String.format("./src/main/java/com/github/astronoodles/peerpal/" +
                "lessons/exercises/%s.json", type.getCodeName()));

        if (!exerciseFile.exists()) return exerciseList;

        try {
            JsonNode exerciseTree = mapper.readTree(exerciseFile);
            int exerciseCounter = 1;

            while (!exerciseTree.path(String.format("exercise%d", exerciseCounter)).isMissingNode()) {
                JsonNode exerciseNode = exerciseTree.path(String.format("exercise%d", exerciseCounter));
                System.out.printf("exercise%d%n", exerciseCounter);
                String question = exerciseNode.path("question").textValue();
                List<String> possibleAnswers = new ArrayList<>(6);
                exerciseNode.path("possibleAnswers").elements().
                        forEachRemaining(answer -> possibleAnswers.add(answer.textValue()));
                String correctAnswer = exerciseNode.path("correctAnswer").textValue();
                String correctResponse = exerciseNode.path("correctResponse").textValue();
                String wrongResponse = exerciseNode.path("wrongResponse").textValue();

                exerciseList.add(new LessonExercise(question, possibleAnswers, correctAnswer, correctResponse, wrongResponse));
                //System.out.println(question + " " + correctAnswer);
                exerciseCounter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return exerciseList;
    }

    private static class LessonExercise {
        private final String question;
        private final List<String> possibleAnswers;
        private final String correctAnswer;
        private final String correctResponse;
        private final String wrongResponse;


        private LessonExercise(String question, List<String> possibleAnswers, String correctAnswer, String correctResponse, String wrongResponse) {
            this.question = question;
            this.possibleAnswers = possibleAnswers;
            this.correctAnswer = correctAnswer;
            this.correctResponse = correctResponse;
            this.wrongResponse = wrongResponse;
        }
    }
}
