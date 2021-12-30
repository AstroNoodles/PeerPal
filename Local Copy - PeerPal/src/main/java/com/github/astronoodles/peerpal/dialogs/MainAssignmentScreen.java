package com.github.astronoodles.peerpal.dialogs;

import com.github.astronoodles.peerpal.base.StudentAssignment;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainAssignmentScreen {

    @FXML
    public Label assignmentTitle, assignedBy, statusText, assignedText, dueText, assignmentDescText;


    public void formatAssignmentText(StudentAssignment assignment) {
        assignmentTitle.setText(String.format(assignmentTitle.getText(), assignment.getFullName()));
        assignedBy.setText(String.format(assignedBy.getText(), assignment.getInstructorName()));
        statusText.setText(String.format(statusText.getText(), assignment.getStatus().toString()));
        statusText.setTextFill(assignment.getStatus().getStatusColor());
        assignedText.setText(String.format(assignedText.getText(), assignment.getStartDate()));
        dueText.setText(String.format(dueText.getText(), assignment.getEndDate()));
        assignmentDescText.setText(String.format(assignmentDescText.getText(), assignment.getDescription()));
    }

    @FXML
    public void openLanguageEditor() {

    }

    @FXML
    public void submitAssignment() {

    }

    @FXML
    public void viewTeacherFeedback() {

    }

}
