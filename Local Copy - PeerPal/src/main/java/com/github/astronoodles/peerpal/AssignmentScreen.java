package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.AssignmentDialog;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        System.out.println("Old Assignments: " + data);

        //data.addAll(StageHelper.obtainMissingAssignments(obtainAssignments(name), data));
        data.addAll(obtainAssignments(name));
        System.out.println("All assignments: " + data);
        table.setItems(data);
        //System.out.println(data.get(0).getFullName());

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

        //Images
    /*Image assignmentImage = new Image("https://i.pinimg.com/originals/56/b4/9f/56b49f8fe357deecf54ad7805209d79e.png",
            170,140, true,true);
    grid.add(new ImageView(assignmentImage), 0, 4,2,1);
    Image assignmentImage2 = new Image("https://www.clker.com/cliparts/Y/l/P/U/K/D/talking-bubble-no-shadow-md.png",
            200,100, false,true);
    grid.add(new ImageView(assignmentImage2), 2, 3,3,2);*/
        return grid;
    }


    public static GridPane setupPane(String name) {
        GridPane grid = new GridPane();

        grid.setVgap(10.0);
        grid.setPadding(new Insets(30, 20, 30, 25));

        //Top Heading Label

        Label welcome = new Label(String.format("Welcome %s", name));
        welcome.setFont(new Font("Cambria", 15));

        Label heading = new Label("NOW VIEWING: Home Page");
        grid.add(welcome, 0, 0, 2, 1);
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

                    LocalDate lateDate = curAssignment.getEndDate().plus(2, ChronoUnit.WEEKS);

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

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(assignmentsLoc,
                    StandardOpenOption.WRITE))) {

                for (StudentAssignment assign : assignments) {
                    System.out.println("Current Backed Up Assignment: " + assign);
                    assign.setAssignmentPath(assignmentsLoc.toString());
                    oos.writeObject(new StudentAssignment.SerializableStudentAssignment(assign));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ENSURE that the general assignments file are read first AND then read the student assignments!
    protected static List<StudentAssignment> obtainAssignments(String studentName) {
        List<StudentAssignment> assignments = new ArrayList<>(10);

        try {
            // Read both the student assignments and the general assignments path to see current assignments
            // and new teacher assignments

            // Only read a path if it exists (the studentAssignments will not exist in the beginning)
            Path[] allAssignmentPaths = new Path[]{Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                    "storage", studentName, "studentAssignments.dat"),
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", "assignments.dat")};
            List<String> retrievedAssignNames = new ArrayList<>(10);

            for (Path assignmentsLoc : allAssignmentPaths) {
                if (Files.exists(assignmentsLoc)) {
                    System.out.println("Path Reading: " + assignmentsLoc.toString());
                    ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(assignmentsLoc,
                            StandardOpenOption.READ));
                    while (true) {
                        try {
                            if (assignmentsLoc.getFileName().toString().equals("studentAssignments.dat")) {
                                StudentAssignment.SerializableStudentAssignment assign =
                                        (StudentAssignment.SerializableStudentAssignment)
                                                ois.readObject();
                                StudentAssignment assign1 = new StudentAssignment(assign);
                                retrievedAssignNames.add(assign1.getFullName());
                                assignments.add(assign1);

                            } else if (assignmentsLoc.getFileName().toString().equals("assignments.dat")) {
                                Assignment.SerializableAssignment assign =
                                        (Assignment.SerializableAssignment)
                                                ois.readObject();
                                StudentAssignment blankAssign = new StudentAssignment(assign);

                                if(!retrievedAssignNames.contains(blankAssign.getFullName())) {
                                    assignments.add(blankAssign);
                                }
                            }
                        } catch (ClassNotFoundException ce) {
                            ce.printStackTrace();
                            ois.close();
                        } catch (EOFException eof) {
                            System.out.println("Finished reading one path");
                            break;
                        } catch(StreamCorruptedException sce) {
                            System.err.println("Double check your files");
                            break;

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return assignments;
    }

}
