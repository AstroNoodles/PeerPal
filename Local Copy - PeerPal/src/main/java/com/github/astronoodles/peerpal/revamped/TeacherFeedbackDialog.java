package com.github.astronoodles.peerpal.revamped;

import com.github.astronoodles.peerpal.base.StudentAssignment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;

public class TeacherFeedbackDialog {

    @FXML
    public TextArea feedbackText;

    @FXML
    public VBox commentPane;

    private LinkedList<StudentAssignment.Feedback> lst = new LinkedList<>();

    public TeacherFeedbackDialog(String studentResponse) {
        feedbackText.setText(studentResponse);
    }

    @FXML
    public void createComment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment_tool.fxml"));
            CommentToolDialog dialog = loader.getController();

            String studentResponseText = feedbackText.getText();
            String currSelection = feedbackText.getSelectedText();

            dialog.initialize(studentResponseText.indexOf(currSelection),
                    studentResponseText.indexOf(currSelection) + currSelection.length());

            Parent commentTool = loader.load();

            Scene commentScene = new Scene(commentTool, 500, 400, Color.web("#e3f2fd"));
            commentScene.getStylesheets().add(getClass().getResource("/feedback_styles.css").toExternalForm());
            Stage commentStage = new Stage();
            commentStage.setScene(commentScene);
            commentStage.setTitle("Create A Comment");
            commentStage.setResizable(false);

            commentStage.showAndWait();

            if(!commentStage.isShowing()) {
                commentPane.getChildren().add(commentTool);

                StudentAssignment.Feedback feedback = dialog.getFeedback();
                lst.add(feedback);

                commentTool.setOnMouseEntered(e -> {
                    feedbackText.selectRange(feedback.getStartPos(), feedback.getEndPos());
                });

            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<StudentAssignment.Feedback> getAllTeacherFeedback() {
        return lst;
    }
}
