package com.github.astronoodles.peerpal.revamped;

import com.github.astronoodles.peerpal.AssignmentTeacherScreen;
import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.AssignmentIO;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.CreateAssignmentDialog;
import com.github.astronoodles.peerpal.dialogs.MainAssignmentScreen;
import com.github.astronoodles.peerpal.extras.CloudStorageConfig;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RevampedAssignmentScreen {

    @FXML
    public Label introText, refreshLabel;

    @FXML
    public VBox assignmentContainer;

    @FXML
    public Button refreshButton;

    public String studentName;
    public boolean user;
    public List<Assignment> generalAssignments;
    public List<StudentAssignment> data = new LinkedList<>();

    public RevampedAssignmentScreen(String studentName, boolean user) {
        this.studentName = studentName;
        this.user = user;
        introText.setText(String.format(introText.getText(), studentName));

        try {
            assignmentContainer.getChildren().addAll(populateAssignments(studentName));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private List<Node> populateAssignments(String studentName) throws IOException {
        List<Node> assignmentCards = new LinkedList<>();
        data = AssignmentIO.obtainAssignments(studentName);
        generalAssignments.addAll(AssignmentIO.obtainAssignments(studentName));
        if(data.isEmpty()) {
            Label emptyDataLabel = new Label("No assignments are currently active. Contact your professor " +
                    "to add some assignments and then you'll find them here!");
            emptyDataLabel.setStyle("-fx-font-size: 17; -fx-text-fill: #8b008b;" +
                    "-fx-font-family: Charter, Arial, Times, sans-serif; -fx-font-weight: bolder");
            emptyDataLabel.setPrefWidth(200);
            emptyDataLabel.setPrefHeight(100);
            emptyDataLabel.setWrapText(true);
            assignmentCards.add(emptyDataLabel);
        } else {
            Color changeColor = Color.web("#ede7f6");
            for(StudentAssignment assignment : data) {
                Parent card = FXMLLoader.load(getClass().getResource("/assignment_card.fxml"));
                HBox cardBox = (HBox) card.lookup("#assignmentCard");
                Label assignmentOpening = (Label) card.lookup("#textAssignmentCard");
                Label assignmentTitle = (Label) card.lookup("#cardAssignmentTitle");
                Label startDateLabel = (Label) card.lookup("#startDateLabel");
                Label endDateLabel = (Label) card.lookup("#endDateLabel");

                Color originalBGColor = (Color) cardBox.getBackground().getFills().get(0).getFill();

                FillTransition fillTransition = new FillTransition(Duration.seconds(3), cardBox.getShape(), originalBGColor, changeColor);
                FillTransition oppositeTransition = new FillTransition(Duration.seconds(3), cardBox.getShape(), changeColor, originalBGColor);

                assignmentOpening.setText(String.format(assignmentOpening.getText(), studentName));
                assignmentTitle.setText(String.format(assignmentTitle.getText(), assignment.getFullName()));
                startDateLabel.setText(String.format(startDateLabel.getText(), assignment.getStartDate()));
                endDateLabel.setText(String.format(endDateLabel.getText(), assignment.getEndDate()));

                card.setOnMousePressed(e -> {
                    fillTransition.playFromStart();
                });

                // just some fancy transitions on the BG color when the button is pressed ^^
                // then I just get rid of the text to be formatted with the actual text of the assignment

                card.setOnMouseReleased(e -> {
                    oppositeTransition.playFromStart();
                    openAssignmentDialog(assignment);
                });

                assignmentCards.add(card);
            }
        }
        return assignmentCards;
    }

    @FXML
    public void onAnnounce(ActionEvent ae) {
        String menuStyle = "-fx-font-family: Charter, Arial, Times, sans-serif; -fx-text-fill: #607d8b";
        MenuItem ctxCreateAssign = new MenuItem("Create Assignment!");
        ctxCreateAssign.setStyle(menuStyle);
        ctxCreateAssign.setOnAction(ev -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/create_assignment_page.fxml"));
                Parent assignScreen = loader.load();

                CreateAssignmentDialog assignDialog = loader.getController();
                // note that in this case studentName will be the teacher's name because the only one
                // accessing this menu item will be the teacher
                assignDialog.connectAssignmentTable(studentName);

                Scene assignDialogScene = new Scene(assignScreen, 300, 400, true);
                assignDialogScene.getStylesheets().add(getClass().getResource("/assignment_styles.css").toExternalForm());
                Stage assignDialogStage = new Stage();
                assignDialogStage.setScene(assignDialogScene);
                assignDialogStage.setTitle("Create An Assignment!");
                assignDialogStage.initOwner(introText.getScene().getWindow());

                assignDialogStage.showAndWait();



                if(assignDialog.getCurAssignment() != null &&
                        !StudentAssignment.assignmentContains(data, assignDialog.getCurAssignment())) {
                    generalAssignments.add(assignDialog.getCurAssignment());
                }

            } catch(IOException ex) {
                ex.printStackTrace();
            }
        });

        MenuItem ctxCreateInquiry = new MenuItem("Make Announcement!");
        ctxCreateInquiry.setStyle(menuStyle);
        ctxCreateInquiry.setOnAction(ev -> {
            TextInputDialog textDialog = new TextInputDialog();

        });
    }

    @FXML
    public void refreshAssignments(){
        Background originalBG = refreshButton.getBackground();
        Background pressedBG = new Background(new BackgroundFill(Color.web("#90caf9"),
                new CornerRadii(1), new Insets(3)));
        Border pressedBorder = new Border(new BorderStroke(Color.web("#1565c0"),
                BorderStrokeStyle.SOLID, new CornerRadii(1), new BorderWidths(2)));

        PauseTransition bgTransition = new PauseTransition(Duration.seconds(6));
        bgTransition.setOnFinished(event -> {
            refreshButton.setBackground(originalBG);
            refreshButton.setDefaultButton(true);
            refreshLabel.setVisible(false);
        });

        refreshButton.setBackground(pressedBG);
        refreshButton.setBorder(pressedBorder);
        refreshLabel.setVisible(true);

        CloudStorageConfig config = new CloudStorageConfig();
        if (config.isCloudStorageFull()) config.downloadCloudStorage();

        bgTransition.playFromStart();
    }

    private void openAssignmentDialog(StudentAssignment assignment) {
        try {
            // more lookups but this time of the assignment dialog before it gets loaded in response
            // to wanting to open the assignment.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assignment_dialog.fxml"));
            Parent assignDialog = loader.load();
            
            MainAssignmentScreen assignmentScreen = loader.getController();
            assignmentScreen.formatAssignmentText(assignment);

            Scene dialogScene = new Scene(assignDialog, 300, 300);
            dialogScene.getStylesheets().add(getClass().getResource("/assignment_styles.css").toExternalForm());
            Stage dialogStage = new Stage();
            dialogStage.setScene(dialogScene);
            dialogStage.setTitle(String.format("'%s' Assignment", assignment.getFullName()));
            dialogStage.setResizable(true);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(introText.getScene().getWindow());
            dialogStage.show();

        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }


}
