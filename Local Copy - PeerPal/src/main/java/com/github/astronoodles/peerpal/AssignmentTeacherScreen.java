package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.AssignmentDialog;
import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.dialogs.StudentAssignmentGrid;
import com.github.astronoodles.peerpal.extras.StageHelper;
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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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


    public GridPane loadStage(){
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

        Button createAssign = new Button("Create Assignment");
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

//                for(Assignment storedAssign : obtainAssignments()) {
//                    if(data.stream().noneMatch(assign ->
//                            assign.getFullName().equals(storedAssign.getFullName()))) {
//                        data.add(storedAssign);
//                    }
//                }
                data.addAll(StageHelper.obtainMissingAssignments(obtainAssignments(), data));

                System.out.println("Old Data: " + data);
                //data.addAll(assignDialog.getTeacherAssignments());
                if(assignDialog.getCurAssignment() != null &&
                        !data.contains(assignDialog.getCurAssignment()))
                    data.add(assignDialog.getCurAssignment());


                System.out.println("New Data: " + data);

                table.setItems(data);
                // backUpAssignments(data);

            } catch(IOException ex) {
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
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    if (!(nameLabel.getText().indexOf("Assignment")==0)) {
                        CloudAssignmentParser parser = new CloudAssignmentParser(
                                String.format("%s.%s", row.getItem().getFullName(), row.getItem().getFileExtension())
                        );
                        Map<String, List<StudentAssignment>> allAssign = parser.getStudentAssignments();
                        List<StudentAssignment> studentData = isolateAssignments(
                                allAssign, row.getItem());
                        System.out.println("this is it " + studentData);
                        //System.out.println(studentData.get(0).getAssignmentPath());

                        Stage assignGridDialog = new Stage();
                        StudentAssignmentGrid sag = new StudentAssignmentGrid(
                                studentData.parallelStream().filter(StudentAssignment::isDirty).
                                        collect(Collectors.toList()));


                        Scene sc = new Scene(sag.createStudentGrid(new LinkedList<>(allAssign.keySet()),
                                1, StudentAssignmentGrid.STUDENT_ROWS), 500, 500);

                        assignGridDialog.setScene(sc);
                        assignGridDialog.setTitle("Student Assignments");
                        assignGridDialog.showAndWait();

                        // Add all assignments that have updated grades plus
                        // assignments that still need to be stored in the student data file


                        for(StudentAssignment updatedAssign : sag.getUpdatedStudentAssignments()) {
                            if(updatedStudentAssignments.isEmpty()) {
                                updatedStudentAssignments.add(updatedAssign);
                                break;
                            }

                            for(int i = 0; i < updatedStudentAssignments.size(); i++) {
                                if(updatedAssign.getFullName().equals(updatedStudentAssignments.get(i).getFullName())){
                                    updatedAssign.setAssignmentPath(updatedStudentAssignments.get(i).getAssignmentPath());
                                    updatedStudentAssignments.set(i, updatedAssign);
                                }
                            }
                        };

                        List<StudentAssignment> extras = studentData.parallelStream().filter((assign) -> !assign.isDirty()).collect(Collectors.toList());
                        System.out.println("Starting Assignments: " + updatedStudentAssignments);

                        System.out.println(extras);


                        for(StudentAssignment extraAssign : extras) {
                           if(updatedStudentAssignments.stream().noneMatch((assign) -> assign.getFullName().equals(extraAssign.getFullName()))) {
                               updatedStudentAssignments.add(extraAssign);
                           }
                        }

                        System.out.println("Full Assignments To Update: " + updatedStudentAssignments);

                    }
                } else if(event.getButton() == MouseButton.SECONDARY && (!row.isEmpty())) {
                    System.out.println("TESTING 123");
                    createTeacherContextMenu(row.getItem()).
                            show(tv, event.getScreenX(), event.getScreenY());
                }
            });
            return row ;
        });
        data.addAll(AssignmentScreen.obtainAssignments(name));
        System.out.println(data);

        table.setItems(data);
        grid.add(table,0,1,3,1);
        return grid;
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
        System.out.println(studentAssignments.values().size());
        for(List<StudentAssignment> assign : studentAssignments.values()) {
            System.out.println(assign);
            String pathCheck = null;
            for (StudentAssignment studentAssignment : assign) {
                if (studentAssignment.getFullName().trim().equals(item.getFullName().trim())) {
                    // TODO Ensure that all assignments have an assignment path and
                    // TODO are written to the studentAssignments file.
                    studentAssignment.markDirty();
                }

                if(studentAssignment.getAssignmentPath() != null) {
                    pathCheck = studentAssignment.getAssignmentPath();
                } else {
                    studentAssignment.setAssignmentPath(pathCheck);
                }


                assignments.add(studentAssignment);
            }
        }
        return assignments;
    }

    protected void backUpAssignments() {
        try {
            Path assignmentsLoc =
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                    "storage", "assignments.dat");

            if(!Files.exists(assignmentsLoc)) Files.createFile(assignmentsLoc);

            try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(assignmentsLoc,
                    StandardOpenOption.WRITE))) {

                for (Assignment assign : data) {
                    oos.writeObject(new Assignment.SerializableAssignment(assign));
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void updateGridStudentAssignments() {

        // NOTE - assert that all student assignments are saved to the same path
        String assignmentPath = updatedStudentAssignments.get(0).getAssignmentPath();

        try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(assignmentPath),
                StandardOpenOption.WRITE))) {
            for(StudentAssignment assign : updatedStudentAssignments) {
                oos.writeObject(new StudentAssignment.SerializableStudentAssignment(assign));
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    protected static List<Assignment> obtainAssignments() {
        List<Assignment> assignments = new ArrayList<>(10);

        try {
            // System.out.println(System.getProperty("user.dir"));
            Path assignmentsLoc =
                    Paths.get("./src/main/java/com/github/astronoodles/peerpal",
                            "storage", "assignments.dat");

            if(!Files.exists(assignmentsLoc)) {
                return assignments;
            }

            ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(assignmentsLoc,
                    StandardOpenOption.READ));
            while (true) {
                try {
                    Assignment.SerializableAssignment assign = (Assignment.SerializableAssignment)
                            ois.readObject();
                    assignments.add(new Assignment(assign));
                } catch(ClassNotFoundException ce) {
                    ce.printStackTrace();
                    ois.close();
                } catch(EOFException eof) {
                    ois.close();
                    return assignments;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return assignments;
    }

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
