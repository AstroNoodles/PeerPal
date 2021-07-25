package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.AssignmentDialog;
import com.github.astronoodles.peerpal.extras.CloudStorageConfig;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentScreen {

    private final String name;
    private final String modelPath; // TODO Add to assignment screen?

    public AssignmentScreen(String name, String modelPath) {
        this.name = name;
        this.modelPath = modelPath;
    }

    private final ObservableList<StudentAssignment> data = FXCollections.observableArrayList();
    //create table and list of values
    private final TableView<StudentAssignment> table = new TableView<>();


    public GridPane loadStage() {
        GridPane grid = AssignmentScreen.setupPane(name);

        table.setEditable(false);

        TableColumn<StudentAssignment, String> schoolCol = addSimilarColumns();

        TableColumn<StudentAssignment, StudentAssignment.AssignmentStatus> statusCol =
                new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        TableColumn<StudentAssignment, Float> gradeCol = new TableColumn<>("Grade");
        gradeCol.setPrefWidth(100);
        gradeCol.setCellValueFactory(
                new PropertyValueFactory<>("grade"));

        //organizing columns

        table.getColumns().add(schoolCol);
        schoolCol.getColumns().add(statusCol);
        schoolCol.getColumns().add(gradeCol);

        //connecting the column to value in list
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );
        gradeCol.setCellValueFactory(
                new PropertyValueFactory<>("grade")
        );

        //data.addAll(StageHelper.obtainMissingAssignments(obtainAssignments(name), data));
        data.addAll(obtainAssignments(name));
        System.out.println("All assignments: " + data);
        table.setItems(data);

        //double-clicking function
        Label idLabel = new Label();
        Label closed = new Label();

        closed.setText("not closed");
        table.setRowFactory(tv -> {
            TableRow<StudentAssignment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    if (!(idLabel.getText().indexOf("Assignment") == 0)) {
                        StageHelper.loadSceneFXML("The Language Learn App", 600, 500,
                                "/main_screen.fxml", "/main_screen.css");
                    }
                } else if (event.getButton() == MouseButton.SECONDARY && (!row.isEmpty())) {
                    createContextMenu(row.getItem()).
                            show(row, event.getScreenX(), event.getScreenY());

                }
            });
            return row;
        });

        table.setItems(data);
        grid.add(table, 0, 1, 3, 1);

        return grid;
    }


    public static GridPane setupPane(String name) {
        GridPane grid = new GridPane();

        grid.setVgap(10.0);
        grid.setPadding(new Insets(30, 20, 30, 25));

        //Top Heading Label

        Label welcome = new Label(String.format("Welcome %s", name));
        welcome.setFont(new Font("Cambria", 15));

        ImageView refreshIcon = new ImageView(new Image(AssignmentScreen.class.getResourceAsStream("/refresh.png")));
        Button refreshButton = new Button("", refreshIcon);
        refreshButton.setPrefWidth(50);
        refreshButton.setPrefHeight(50);

        refreshButton.setOnAction((event) -> {
            CloudStorageConfig config = new CloudStorageConfig();
            if (!config.isCloudStorageEmpty()) config.downloadCloudStorage();
        });

        Label heading = new Label("NOW VIEWING: Home Page");
        grid.add(welcome, 0, 0, 2, 1);
        grid.add(refreshButton, 2, 0, 1, 1);
        grid.add(heading, 0, 2, 2, 1);

        return grid;
    }

    private TableColumn<StudentAssignment, String> addSimilarColumns() {
        TableColumn<StudentAssignment, String> schoolCol = new TableColumn<>("School: Bronx Science");

        //making table columns and setting a value to the column

        TableColumn<StudentAssignment, String> assignNameCol = new TableColumn<>("Assignment Name");
        assignNameCol.setPrefWidth(150);
        assignNameCol.setCellValueFactory(
                new PropertyValueFactory<>("fullName"));

        TableColumn<StudentAssignment, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setPrefWidth(150);
        instructorCol.setCellValueFactory(
                new PropertyValueFactory<>("instructorName"));


        schoolCol.getColumns().add(assignNameCol);
        schoolCol.getColumns().add(instructorCol);

        return schoolCol;
    }


    private ContextMenu createContextMenu(StudentAssignment curAssignment) {
        MenuItem descItem = new MenuItem("See Description");
        descItem.setOnAction(e -> {
            Alert descAlert = new Alert(Alert.AlertType.INFORMATION,
                    String.format("The description for your assignment is:\n\n%s",
                            curAssignment.getDescription()), ButtonType.OK);
            descAlert.setHeaderText("Here is the description for your assignment!");
            descAlert.setTitle("Assignment Description");
            descAlert.show();
        });

        MenuItem uploadItem = new MenuItem("Upload Assignment");
        uploadItem.setOnAction(e -> {
            //Adding file
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            AssignmentDialog.mapExtensionToName().get(curAssignment.getFileExtension()),
                            String.format("*.%s", curAssignment.getFileExtension())));
            File selectedFile = fileChooser.showOpenDialog(new Stage());

            if (selectedFile != null) {
                try {
                    Path userLoc = Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", name, String.format("%s.%s",
                                    curAssignment.getFullName().trim(), curAssignment.getFileExtension()));

                    if (!Files.exists(userLoc)) {
                        Files.createDirectories(userLoc.getParent());
                        Files.createFile(userLoc);
                    }

                    Files.copy(selectedFile.toPath(), userLoc, StandardCopyOption.REPLACE_EXISTING);

                    Period latePeriod = Period.ofWeeks(2); // adjustable
                    LocalDate lateDate = curAssignment.getEndDate().plus(latePeriod);

                    if (LocalDate.now().isAfter(lateDate)) {
                        curAssignment.setStatus(StudentAssignment.AssignmentStatus.LATE);
                    } else {
                        curAssignment.setStatus(StudentAssignment.AssignmentStatus.UPLOADED);
                    }
                    backUpAssignments(data, name);
                    System.out.println(curAssignment);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return new ContextMenu(descItem, uploadItem);
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
        List<StudentAssignment> assignments = new ArrayList<>(10);

        // Read both the student assignments and the general assignments path to see current assignments
        // and new teacher assignments

        // Only read a path if it exists (the studentAssignments will not exist in the beginning)
        Path[] allAssignmentPaths = new Path[]{Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                "storage", studentName, "studentAssignments.dat"),
                Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", "assignments.dat")};
        List<String> retrievedAssignNames = new ArrayList<>(10);

        for (Path assignmentPath : allAssignmentPaths) {
            if (Files.exists(assignmentPath)) {
                try (ObjectInputStream ois =
                             new ObjectInputStream(Files.newInputStream(assignmentPath, StandardOpenOption.READ))) {
                    if (assignmentPath.getFileName().toString().equals("studentAssignments.dat")) {
                        // I know what I am doing with these casts
                        List<StudentAssignment.SerializableStudentAssignment> serializableStudentAssignments =
                                (List<StudentAssignment.SerializableStudentAssignment>) ois.readObject();
                        assignments.addAll(serializableStudentAssignments.parallelStream()
                                .map(StudentAssignment::new).collect(Collectors.toList()));
                        assignments.forEach(assign -> retrievedAssignNames.add(assign.getFullName()));
                    } else { // assignments.dat
                        List<Assignment.SerializableAssignment> serializableAssignments =
                                (List<Assignment.SerializableAssignment>) ois.readObject();
                        assignments.addAll(serializableAssignments.parallelStream().map(StudentAssignment::new)
                                .filter(assign2 -> !retrievedAssignNames.contains(assign2.getFullName()))
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
