package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.AssignmentDialog;
import com.github.astronoodles.peerpal.dialogs.StudentAssignmentGrid;
import com.sun.media.jfxmedia.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AssignmentTeacherScreen {

    private final String name;
    private final String modelPath; // TODO add to assignment screen?
    public List<StudentAssignment> updatedStudentAssignments = new ArrayList<>(10);


    public AssignmentTeacherScreen(String name, String modelPath) {
        super();
        this.name = name;
        this.modelPath = modelPath;
    }

    private final List<Assignment> backedList = new ArrayList<>(Assignment.getAssignmentCount());
    private final ObservableList<Assignment> data = FXCollections.observableList(backedList);
    //create table and list of values
    private final TableView<Assignment> table = new TableView<>();


    public GridPane loadStage() {
        GridPane grid = AssignmentScreen.setupPane(name);

        table.setEditable(true);

        TableColumn<Assignment, String> schoolCol = addSimilarColumns();

        TableColumn<Assignment, LocalDate> startDate = new TableColumn<>("Start Date");
        startDate.setPrefWidth(100);
        startDate.setCellValueFactory(
                new PropertyValueFactory<>("startDate"));

        TableColumn<Assignment, LocalDate> endDate = new TableColumn<>("End Date");
        endDate.setPrefWidth(100);
        endDate.setCellValueFactory(
                new PropertyValueFactory<>("endDate"));


        //organizing columns

        table.getColumns().add(schoolCol);
        schoolCol.getColumns().add(startDate);
        schoolCol.getColumns().add(endDate);

        final Button createAssign = new Button("Create Assignment");
        createAssign.setPrefWidth(200);
        createAssign.setPrefHeight(50);

        createAssign.setOnAction(e -> {
            try {
                FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/assignment_page.fxml"));

                Parent root = dialogLoader.load();

                AssignmentDialog assignDialog = dialogLoader.getController();
                assignDialog.connectAssignmentTable(name);

                Scene scene = new Scene(root, 500, 400);
                scene.getStylesheets().add(getClass().getResource("/assignment_styles.css").toExternalForm());

                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Create An Assignment");
                stage.showAndWait();

                //System.out.println("Old Data: " + data);

                if (assignDialog.getCurAssignment() != null &&
                        !data.contains(assignDialog.getCurAssignment()))
                    data.add(assignDialog.getCurAssignment());


                //System.out.println("New Data: " + data);

                table.setItems(data);
                // backUpAssignments(data);

            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Be more careful with the FXML location");
            }
        });

        grid.add(createAssign, 0, 4);

        //double-clicking function
        Label nameLabel = new Label();
        Label closed = new Label();

        closed.setText("not closed");


        table.setRowFactory(tv -> {
            TableRow<Assignment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    if (!(nameLabel.getText().indexOf("Assignment") == 0)) {
                        updateAssignmentsByGrid(row);
                    }
                } else if (event.getButton() == MouseButton.SECONDARY && (!row.isEmpty())) {
                    System.out.println("TESTING 123");
                    createTeacherContextMenu(row.getItem()).
                            show(tv, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
        data.addAll(AssignmentScreen.obtainAssignments(name));
        clearUpAssignments();

        System.out.println("Queried Data: " + data);
        table.setItems(data);

        createAssign.setDisable(!data.isEmpty());
        grid.add(table, 0, 1, 3, 1);
        return grid;
    }

    private void updateAssignmentsByGrid(TableRow<Assignment> row) {

        // ----------- GRADE LOOKUP SECTION -----------

        // Get all of the completed data from the students and ensure that we can look at it
        // and access the grades on them
        CloudAssignmentParser parser = new CloudAssignmentParser(
                String.format("%s.%s", row.getItem().getFullName(), row.getItem().getFileExtension())
        );

        Map<String, List<StudentAssignment>> allAssign = parser.getStudentAssignments();
        List<StudentAssignment> studentData = isolateAssignments(
                allAssign, row.getItem());
        //System.out.println("this is it " + studentData);

        Stage assignGridDialog = new Stage();
        StudentAssignmentGrid sag = new StudentAssignmentGrid(studentData);

        Scene sc = new Scene(sag.createStudentGrid(new LinkedList<>(allAssign.keySet()),
                1, StudentAssignmentGrid.STUDENT_ROWS), 500, 500);

        assignGridDialog.setScene(sc);
        assignGridDialog.setTitle("Student Assignments");
        assignGridDialog.showAndWait();

        // Add all assignments that have updated grades plus
        // assignments that still need to be stored in the student data file

        System.out.println("BEFORE: " + sag.getUpdatedStudentAssignments());

        for (StudentAssignment updatedAssign : sag.getUpdatedStudentAssignments()) {
            if (!updatedStudentAssignments.contains(updatedAssign)) {
                updatedStudentAssignments.add(updatedAssign);
                break;
            }

            for (int i = 0; i < updatedStudentAssignments.size(); i++) {
                if (updatedAssign.getFullName().equals(updatedStudentAssignments.get(i).getFullName())) {
                    updatedAssign.setAssignmentPath(updatedStudentAssignments.get(i).getAssignmentPath());
                    updatedStudentAssignments.set(i, updatedAssign);
                }
            }
        }

        System.out.println("Full Assignments To Update: " + updatedStudentAssignments);
    }

    private ContextMenu createTeacherContextMenu(Assignment assign) {
        MenuItem deleteItem = new MenuItem("Delete Assignment");
        deleteItem.setOnAction((event -> {
            data.remove(assign);
        }));

        MenuItem descItem = new MenuItem("See Description");
        descItem.setOnAction((event) -> {
            Alert descAlert = new Alert(Alert.AlertType.INFORMATION,
                    assign.getDescription(), ButtonType.OK);
            descAlert.setHeaderText("Here is the description for your assignment!");
            descAlert.setTitle(String.format("%s - Description", assign.getFullName()));
            descAlert.show();
        });
        return new ContextMenu(deleteItem, descItem);
    }

    private List<StudentAssignment> isolateAssignments(
            Map<String, List<StudentAssignment>> studentAssignments, Assignment item) {
        List<StudentAssignment> assignments = new LinkedList<>();
        for (List<StudentAssignment> assign : studentAssignments.values()) {
            //System.out.println(assign);
            String pathCheck = null;
            for (StudentAssignment studentAssignment : assign) {
                if (studentAssignment.getFullName().trim().equals(item.getFullName().trim())) {
                    // TODO Ensure that all assignments have an assignment path and
                    // TODO are written to the studentAssignments file.
                    assignments.add(studentAssignment);
                }

                if (studentAssignment.getAssignmentPath() != null) {
                    pathCheck = studentAssignment.getAssignmentPath();
                } else {
                    studentAssignment.setAssignmentPath(pathCheck);
                }
            }
        }
        return assignments;
    }

    /**
     * Backs up all assignments that the teacher has created to the assignments.dat
     * It saves these assignments in a LIST format so all reads from this folder must be done
     * with the consideration that the assignments are saved in a list.
     */
    protected void backUpAssignments() {
        try {
            Path assignmentsLoc =
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", "assignments.dat");

            if (!Files.exists(assignmentsLoc)) Files.createFile(assignmentsLoc);
            List<Assignment.SerializableAssignment> serializableAssignments =
                    data.parallelStream().map(Assignment.SerializableAssignment::new).collect(Collectors.toList());

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(assignmentsLoc,
                    StandardOpenOption.WRITE))) {
                oos.writeObject(serializableAssignments);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears all assignments 3 days after the maximum end date of the assignments.
     * At this point, the teacher can add more assignments due to the data list being empty
     */
    private void clearUpAssignments() {
        Period gradingPeriod = Period.ofDays(3);
        // find the maximum end date of the assignments + add 3 days for grading
        LocalDate date = LocalDate.MIN;
        for (Assignment assignment : data) {
            if (assignment.getEndDate().isAfter(date)) {
                date = assignment.getEndDate();
            }
        }
        date = date.plus(gradingPeriod);

        // delete the assignments for both studentAssignments.dat (all students) and assignments.dat
        // This info will still remain online but will be deleted on local machine
        if (LocalDate.now().isEqual(date) || LocalDate.now().isAfter(date)) {
            try (Stream<Path> dirWalk = Files.walk(Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                    "storage"))) {
                Path assignmentsPath = Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                        "storage", "assignments.dat");
                Files.delete(assignmentsPath);

                dirWalk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .peek(System.out::println)
                        .forEach(File::delete);

                data.clear();
            } catch (IOException e) {
                e.printStackTrace(); // i hate these errors
            }
        }
    }

    /**
     * Updates the individual student assignments from the teacher interaction with the grid of student assignments
     * You must ensure that each of the student assignments has a path they are saved with and it is not null!
     */
    public void updateGridStudentAssignments() {

        // NOTE - assert that all student assignments are saved to the same path
        if (!updatedStudentAssignments.isEmpty()) {
            String assignmentPath = updatedStudentAssignments.get(0).getAssignmentPath();

            List<StudentAssignment.SerializableStudentAssignment> serializableStudentAssignments =
                    updatedStudentAssignments.parallelStream().map(StudentAssignment.SerializableStudentAssignment::new).
                            collect(Collectors.toList());

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(assignmentPath),
                    StandardOpenOption.WRITE))) {
                oos.writeObject(serializableStudentAssignments);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtains all assignments that the teacher has created from the assignments.dat file in the storage folder.
     * These assignments are bare-bones (they do not have a grade on them or any student effort attached)
     * All assignments are read from a list format saved in the assignments.dat file.
     *
     * @return A list containing all bare-bones assignments read from the assignments.dat file
     */
//    @SuppressWarnings("unchecked")
//    protected static List<Assignment> obtainAssignments() {
//        List<Assignment> assignments = new ArrayList<>(10);
//
//        try {
//            // the general path of the assignments.dat file.
//            Path assignmentsLoc =
//                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
//                            "storage", "assignments.dat");
//
//            // skip the running time of the file reading process if the file itself doesn't exist yet
//            if (!Files.exists(assignmentsLoc)) {
//                return assignments;
//            }
//
//            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(assignmentsLoc,
//                    StandardOpenOption.READ))) {
//
//                // all assignments are read are going to be part of a specific list
//                List<Assignment.SerializableAssignment> serializableAssignments =
//                        (List<Assignment.SerializableAssignment>) ois.readObject();
//
//                assignments.addAll(serializableAssignments.stream().map(
//                        (Assignment::new)).collect(Collectors.toList()));
//            }
//
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        return assignments;
//    }
    private TableColumn<Assignment, String> addSimilarColumns() {
        TableColumn<Assignment, String> schoolCol = new TableColumn<>("School: Bronx Science");

        //making table columns and setting a value to the column

        TableColumn<Assignment, String> assignNameCol = new TableColumn<>("Assignment Name");
        assignNameCol.setPrefWidth(150);
        assignNameCol.setCellValueFactory(
                new PropertyValueFactory<>("fullName"));

        TableColumn<Assignment, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setPrefWidth(150);
        instructorCol.setCellValueFactory(
                new PropertyValueFactory<>("instructorName"));


        schoolCol.getColumns().add(assignNameCol);
        schoolCol.getColumns().add(instructorCol);

        return schoolCol;
    }

}
