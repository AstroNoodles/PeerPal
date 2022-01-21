package com.github.astronoodles.peerpal.dialogs;

import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.extras.StageHelper;
import com.github.astronoodles.peerpal.revamped.TeacherFeedbackDialog;
import javafx.animation.FillTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StudentAssignmentGrid {
    private final List<StudentAssignment> assignments;
    public static final int STUDENT_ROWS = 5;
    private List<String> assignText;

    public StudentAssignmentGrid(List<StudentAssignment> assignments) {
        this.assignments = assignments;
    }

    public Pane createStudentGrid(List<String> studentNames, int imageID, int studentRows) {

        // TODO I want to remove all of the students if they did not submit their assignments on this page
        List<StudentAssignment> refreshedAssignments = refreshStudents(studentNames);
        Map<String, Integer> studentAvatarMap = StageHelper.getUserAvatarMapping();

        if (imageID < 1 || imageID > 6) imageID = 0;
        if (refreshedAssignments.size() != studentNames.size()) return null;

        GridPane pane = new GridPane();
        pane.setVgap(3);
        pane.setHgap(3);
        pane.setPadding(new Insets(5));

        final Font gridFont = Font.font("DejaVu Sans", FontWeight.BOLD, 13);
        if (refreshedAssignments.size() == 0) {
            Label noStudents = new Label("No students have turned in their assignments yet.\nPlease wait...");
            noStudents.setPrefWidth(500);
            noStudents.setAlignment(Pos.CENTER);
            noStudents.setPrefHeight(200);
            noStudents.setFont(gridFont);
            VBox.setVgrow(noStudents, Priority.ALWAYS);

            VBox emptyContainer = new VBox(2, noStudents);
            emptyContainer.setAlignment(Pos.CENTER);
            emptyContainer.setPrefWidth(500);
            emptyContainer.setPrefHeight(400);
            return emptyContainer;
        }

        if (studentRows > refreshedAssignments.size()) studentRows = refreshedAssignments.size();

        // the length of the assignments array is a proxy for the number of students in the class
        for (int i = 0; i < studentRows; i++) {
            for (int j = 0; j < (refreshedAssignments.size() / studentRows); j++) {
                final int curRow = i;
                final int curCol = j;

                Label nameLabel = new Label(studentNames.get(i));
                nameLabel.setPrefWidth(200);
                nameLabel.setFont(gridFont);
                nameLabel.setAlignment(Pos.CENTER);
                Image avatarImage = new Image(
                        getClass().getResource(String.format("/avatars/D%d.png",
                                studentAvatarMap.getOrDefault(studentNames.get(curRow), 2))).toExternalForm(),
                        100, 100, true, true);
                ImageView avatarView = new ImageView(avatarImage);

                Button viewAssignment = new Button("View Assignment");
                viewAssignment.setPrefWidth(150);
                viewAssignment.setOnAction(e -> {
                    try {
                        assignText =
                                downloadAssignment(studentNames.get(curRow), refreshedAssignments.get(curRow * curCol));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                GridPane.setHgrow(viewAssignment, Priority.SOMETIMES);
                GridPane.setVgrow(viewAssignment, Priority.SOMETIMES);

                Button updateGrade = new Button("Update Grade");
                updateGrade.setPrefWidth(150);
                GridPane.setVgrow(updateGrade, Priority.SOMETIMES);
                GridPane.setHgrow(updateGrade, Priority.SOMETIMES);

                // double check the math is right on this... (see below)
                updateGrade.setOnAction(e -> {
                    updateAssignmentGrade(updateGrade.getScene().getWindow(),
                            refreshedAssignments.get(curRow * curCol));
                });

                Button giveFeedback = new Button("Give Feedback");
                giveFeedback.setPrefWidth(150);
                giveFeedback.setTextFill(Color.web("#0277bd"));
                GridPane.setVgrow(giveFeedback, Priority.SOMETIMES);
                GridPane.setHgrow(giveFeedback, Priority.SOMETIMES);

                giveFeedback.setOnAction(e -> {
                    createFeedbackDialog(updateGrade.getScene().getWindow(), studentNames.get(curRow),
                            refreshedAssignments.get(curRow * curCol));
                });

                VBox container = new VBox(2, nameLabel, avatarView, viewAssignment, updateGrade, giveFeedback);
                container.setAlignment(Pos.CENTER);
                BorderStroke containerStroke = new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
                        new CornerRadii(1), new BorderWidths(1));
                container.setBorder(new Border(containerStroke));

                ScrollPane containerScroller = new ScrollPane(container);
                containerScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                containerScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

                pane.add(containerScroller, i, j);
            }
        }
        return pane;
    }

    /**
     * Refreshes the student grid to only contain students who have submitted their assignment for a given assignment
     */
    private List<StudentAssignment> refreshStudents(List<String> studentNames) {
        List<StudentAssignment> completedAssignments = new ArrayList<>(studentNames.size() / 2);

        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).getStatus() != StudentAssignment.AssignmentStatus.MISSING) {
                completedAssignments.add(assignments.get(i));
            } else {
                studentNames.remove(i);
            }
        }
        return completedAssignments;
    }

    public static List<String> downloadAssignment(String studentName, Assignment curAssignment) throws IOException {
        Path assignmentsLoc =
                Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", studentName);

        // assertion that this file exists and it is the only file in the student assignment directory
        Path srcPath = null;
        System.out.println(assignmentsLoc.toFile());

        try (DirectoryStream<Path> dstream = Files.newDirectoryStream(assignmentsLoc,
                String.format("*.%s", curAssignment.getFileExtension()))) {
            for (Path entry : dstream) {
                if (entry.getFileName().toString().contains(curAssignment.getAssignmentName())) {
                    srcPath = entry;
                    break;
                }
            }
        }

        File destFile = new File(String.format("%s/Downloads/%s",
                System.getProperty("user.home"), srcPath.getFileName().toString()));

        try (FileChannel srcChannel = new FileInputStream(srcPath.toFile()).getChannel();
             FileChannel destChannel = new FileOutputStream(destFile).getChannel()) {
            if (!destFile.exists()) destFile.createNewFile();

            destChannel.transferFrom(srcChannel, 0, srcChannel.size());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().edit(destFile);
            }

        }

        if(curAssignment.getFileExtension().equals("txt") || curAssignment.getFileExtension().equals("html")) {
            return Files.readAllLines(destFile.toPath());
        } else {
            return Collections.singletonList("");
        }

    }

    private void updateAssignmentGrade(Window srcWindow, StudentAssignment curAssignment) {
        TextInputDialog gradeInput = new TextInputDialog();
        gradeInput.setTitle("Input Assignment Grade");
        gradeInput.setHeaderText("Add Student's Grade");
        gradeInput.setContentText("Now that you've read the student's assignment, " +
                "please insert their grade in the input box below.");
        gradeInput.initOwner(srcWindow);

        final Button okButton = (Button) gradeInput.getDialogPane().lookupButton(ButtonType.OK);
        final TextField textField = gradeInput.getEditor();

        // disabled property --> disable = true - enable = false
        final BooleanBinding integerBinding = Bindings.createBooleanBinding(() -> {
            try {
                return Float.parseFloat(textField.getText()) > 120; // 120 for extra credit
            } catch (NumberFormatException ex) {
                return true;
            }
        }, textField.textProperty());
        okButton.disableProperty().bind(integerBinding);
        showGradeTransition(okButton);


        okButton.setOnAction(event -> {
            curAssignment.setGrade(Float.parseFloat(textField.getText()));
            curAssignment.setStatus(StudentAssignment.AssignmentStatus.GRADED);
        });

        gradeInput.showAndWait();
    }

    public static String squashLineListToString(List<String> lst) {
        StringBuilder build = new StringBuilder();
        lst.forEach(elem -> {
            build.append(elem);
            build.append("\n");
        });
        return build.toString();
    }

    private void createFeedbackDialog(Window srcWindow, String studentName, StudentAssignment curAssign) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("teacher_feedback_dialog.fxml"));
            Parent feedbackDialog = loader.load();
            TeacherFeedbackDialog controller = loader.getController();

            if(assignText == null) {
                assignText = downloadAssignment(studentName, curAssign);
            }

            controller.initialize(studentName, curAssign, squashLineListToString(assignText));

            Scene feedbackDialogScene = new Scene(feedbackDialog, 600, 400, Color.web("#90caf9"));
            feedbackDialogScene.getStylesheets().add(getClass().getResource("/feedback_styles.css").toExternalForm());
            Stage fstage = new Stage();
            fstage.setScene(feedbackDialogScene);
            fstage.initOwner(srcWindow);

            fstage.showAndWait();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private SequentialTransition showGradeTransition(Button gradeButton) {
        FillTransition fillForward = new FillTransition(Duration.seconds(2), Color.web("#eeeeee"),
                Color.web("#a5d6a7"));
        FillTransition fillBackward = new FillTransition(Duration.seconds(2), Color.web("#a5d6a7"),
                Color.web("#eeeeee"));

        return new SequentialTransition(gradeButton, fillForward, fillBackward);
    }

    public List<StudentAssignment> getUpdatedStudentAssignments() {
        return assignments;
    }
}
