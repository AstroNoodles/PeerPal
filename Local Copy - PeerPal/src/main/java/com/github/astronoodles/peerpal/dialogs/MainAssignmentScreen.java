package com.github.astronoodles.peerpal.dialogs;

import com.github.astronoodles.peerpal.base.AssignmentIO;
import com.github.astronoodles.peerpal.base.LanguageTextEditor;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.revamped.StudentFeedbackDialog;
import com.github.astronoodles.peerpal.revamped.TeacherFeedbackDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.Period;


public class MainAssignmentScreen {

    public static final Period EXPIRY_PERIOD = Period.ofDays(3);
    @FXML
    public Label assignmentTitle, assignedBy, statusText, assignedText, dueText, assignmentDescText, feedbackLabel;
    @FXML
    public ImageView fileIcon;
    @FXML
    public Button viewResponses;
    public String studentName;
    public StudentAssignment currAssignment;

    public void formatAssignmentText(String studentName, boolean isTeacher, StudentAssignment assignment) {
        this.studentName = studentName;
        this.currAssignment = assignment;

        assignmentTitle.setText(String.format(assignmentTitle.getText(), assignment.getAssignmentName()));
        assignedBy.setText(String.format(assignedBy.getText(), assignment.getInstructorName()));
        statusText.setText(String.format(statusText.getText(), assignment.getStatus().getStatusText()));
        statusText.setTextFill(assignment.getStatus().getStatusColor());
        assignedText.setText(String.format(assignedText.getText(), assignment.getStartDate()));
        dueText.setText(String.format(dueText.getText(), assignment.getEndDate()));
        assignmentDescText.setText(String.format(assignmentDescText.getText(), assignment.getDescription()));

        String icon_loc = String.format("/file_icons/%s_icon.png", assignment.getFileExtension());
        fileIcon.setImage(new Image(getClass().getResource(icon_loc).toExternalForm(), 150, 150,
                true, true, true));

        viewResponses.setVisible(isTeacher);
    }

    @FXML
    public void openLanguageEditor() {
        FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/main_screen.fxml"));

        try {
            Parent root = dialogLoader.load();

            LanguageTextEditor languageEditor = dialogLoader.getController();
            languageEditor.connectToUser(studentName);

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/main_screen.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("The Language Learn App");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void submitAssignment() {
        //Adding file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        CreateAssignmentDialog.mapExtensionToName().get(currAssignment.getFileExtension()),
                        String.format("*.%s", currAssignment.getFileExtension())));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            AssignmentIO.updateAssignment(selectedFile.toPath(), studentName, currAssignment);

            statusText.setText(currAssignment.getStatus().getStatusText());
            statusText.setTextFill(currAssignment.getStatus().getStatusColor());

            System.out.println(currAssignment);
        }
    }

    @FXML
    public void doFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("student_feedback_dialog.fxml"));
            Parent feedbackDialog = loader.load();
            StudentFeedbackDialog controller = loader.getController();

            controller.initialize(studentName, currAssignment, StudentAssignmentGrid.squashLineListToString(
                    StudentAssignmentGrid.downloadAssignment(studentName, currAssignment)));

            Scene feedbackDialogScene = new Scene(feedbackDialog, 600, 400, Color.web("#81c784"));
            feedbackDialogScene.getStylesheets().add(getClass().getResource("/feedback_styles.css").toExternalForm());
            Stage fstage = new Stage();
            fstage.setScene(feedbackDialogScene);
            fstage.initOwner(assignmentTitle.getScene().getWindow());

            fstage.showAndWait();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

}
