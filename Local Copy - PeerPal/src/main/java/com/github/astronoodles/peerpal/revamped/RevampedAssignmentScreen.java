package com.github.astronoodles.peerpal.revamped;

import com.github.astronoodles.peerpal.AssignmentTeacherScreen;
import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.extras.CloudStorageConfig;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    public List<StudentAssignment> data;

    public RevampedAssignmentScreen(String studentName) {
        this.studentName = studentName;
        introText.setText(String.format(introText.getText(), studentName));

        try {
            populateAssignments(studentName);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void populateAssignments(String studentName) throws IOException {
        data = obtainAssignments(studentName);
        if(data.isEmpty()) {
            Label emptyDataLabel = new Label("No assignments are currently active. Contact your professor " +
                    "to add some assignments and then you'll find them here!");
            emptyDataLabel.setStyle("-fx-font-size: 17; -fx-text-fill: #8b008b;" +
                    "-fx-font-family: Charter, Arial, Times, sans-serif; -fx-font-weight: bolder");
            emptyDataLabel.setPrefWidth(200);
            emptyDataLabel.setPrefHeight(100);
            emptyDataLabel.setWrapText(true);
            assignmentContainer.getChildren().add(emptyDataLabel);
        } else {
            for(StudentAssignment assignment : data) {
                Parent card = FXMLLoader.load(getClass().getResource("/assignment_card.fxml"));
                Label assignmentOpening = (Label) card.lookup("#textAssignmentCard");
                Label assignmentTitle = (Label) card.lookup("#cardAssignmentTitle");
                Label startDateLabel = (Label) card.lookup("#startDateLabel");
                Label endDateLabel = (Label) card.lookup("#endDateLabel");

                assignmentOpening.setText(String.format(assignmentOpening.getText(), studentName));
                assignmentTitle.setText(String.format(assignmentTitle.getText(), assignment.getFullName()));
                startDateLabel.setText(String.format(startDateLabel.getText(), assignment.getStartDate()));
                endDateLabel.setText(String.format(endDateLabel.getText(), assignment.getEndDate()));

                assignmentContainer.getChildren().add(card);
            }
        }
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

    /**
     * Backs up all student assignments into their appropriate storage folder given by the student's name.
     * The student assignments will be saved in a LIST format so ensure that you read the assignments by
     * reading a list!
     *
     * @param assignments The list of student assignments to be saved for a particular student
     * @param studentName The student's name (the name of the student's folder)
     */
    private void backUpAssignments(List<StudentAssignment> assignments, String studentName) {
        try {
            Path assignmentsLoc =
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", "assignments.dat");

            if (assignments.stream().anyMatch((assign) ->
                    assign.getStatus() == StudentAssignment.AssignmentStatus.UPLOADED ||
                            assign.getStatus() == StudentAssignment.AssignmentStatus.LATE)) {
                assignmentsLoc = Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", studentName, "studentAssignments.dat");
            }

            if (!Files.exists(assignmentsLoc)) Files.createFile(assignmentsLoc);

            final Path finalAssignmentsLoc = assignmentsLoc;
            assignments.forEach(assign -> assign.setAssignmentPath(finalAssignmentsLoc.toString()));

            List<StudentAssignment.SerializableStudentAssignment> serializableStudentAssignments =
                    assignments.parallelStream()
                            .map(StudentAssignment.SerializableStudentAssignment::new)
                            .collect(Collectors.toList());

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(assignmentsLoc,
                    StandardOpenOption.WRITE))) {
                oos.writeObject(serializableStudentAssignments);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method retrieves all of the student assignments from the respective student folder as indicated by
     * the studentName property specified in the method.
     * Make sure to read the assignments from a LIST rather than individually as they are inputted as a LIST
     * in the above method.
     *
     * @param studentName The student name and name of the folder to retrieve the student names from.
     * @return A list of all of the student assignments that the student specified
     */
    @SuppressWarnings("unchecked")
    protected static List<StudentAssignment> obtainAssignments(String studentName) {
        // ENSURE that the general assignments file are read first AND then read the student assignments!
        List<StudentAssignment> assignments = new LinkedList<>();

        // Read both the student assignments and the general assignments path to see current assignments
        // and new teacher assignments

        // Only read a path if it exists (the studentAssignments will not exist in the beginning)
        Path[] allAssignmentPaths = new Path[]{Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                "storage", studentName, "studentAssignments.dat"),
                Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", "assignments.dat")};
        List<String> retrievedAssignNames = new LinkedList<>();

        for (Path assignmentPath : allAssignmentPaths) {
            if (Files.exists(assignmentPath)) {
                try (ObjectInputStream ois =
                             new ObjectInputStream(Files.newInputStream(assignmentPath, StandardOpenOption.READ))) {
                    if (assignmentPath.getFileName().toString().equals("studentAssignments.dat")) {
                        // I know what I am doing with these casts
                        List<StudentAssignment.SerializableStudentAssignment> serializableStudentAssignments =
                                (List<StudentAssignment.SerializableStudentAssignment>) ois.readObject();
                        assignments.addAll(serializableStudentAssignments.parallelStream()
                                .map(StudentAssignment::new).filter(assign -> {
                                    LocalDate expireDate = assign.getEndDate().plus(AssignmentTeacherScreen.EXPIRY_PERIOD);
                                    return expireDate.isEqual(LocalDate.now()) || expireDate.isAfter(LocalDate.now());
                                }).collect(Collectors.toList()));
                        assignments.forEach(assign -> retrievedAssignNames.add(assign.getFullName()));
                    } else { // assignments.dat
                        List<Assignment.SerializableAssignment> serializableAssignments =
                                (List<Assignment.SerializableAssignment>) ois.readObject();
                        assignments.addAll(serializableAssignments.parallelStream().map(StudentAssignment::new)
                                .filter(assign2 -> {
                                    LocalDate assignExpireDate = assign2.getEndDate().plus(AssignmentTeacherScreen.EXPIRY_PERIOD);
                                    return (assignExpireDate.isEqual(LocalDate.now()) || assignExpireDate.isAfter(LocalDate.now()))
                                            && !retrievedAssignNames.contains(assign2.getFullName());
                                })
                                .collect(Collectors.toList()));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return assignments;
    }

}
