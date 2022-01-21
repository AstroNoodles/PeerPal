package com.github.astronoodles.peerpal.revamped;

import com.github.astronoodles.peerpal.base.StudentAssignment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.LinkedList;

public class StudentFeedbackDialog {

    @FXML
    public VBox mainBox, commentPane;


    public void initialize(String studentName, StudentAssignment assign, String assignText) throws IOException {
        Node baseNode = TeacherFeedbackDialog.initResponse(studentName, assign, assignText);
        LinkedList<StudentAssignment.Feedback> feedbackList = assign.getAssignmentFeedback();

        for(StudentAssignment.Feedback feedback : feedbackList) {
            Parent feedbackBox = FXMLLoader.load(getClass().getResource("/student_feedback.fxml"));
            Label feedbackText = (Label) feedbackBox.lookup("#studentFeedbackBox");
            feedbackText.setText(feedback.getFeedbackText());

            feedbackBox.setOnMouseEntered(e -> {
                if(baseNode instanceof TextArea) {
                    TextArea txt = (TextArea) baseNode;
                    txt.selectRange(feedback.getStartPos(), feedback.getEndPos());
                } else if(baseNode instanceof WebView) {
                    WebView wv = (WebView) baseNode;
                    System.out.println(wv);
                    // TODO select the text in the webview (hard to do)
                }
            });
            commentPane.getChildren().add(feedbackBox);
        }
        mainBox.getChildren().add(baseNode);

        Button saveResponses = new Button("Save Edits");
        saveResponses.getStyleClass().add("assignmentDialogButtons");
        saveResponses.setPrefWidth(100);
        saveResponses.setPrefHeight(100);

    }


}
