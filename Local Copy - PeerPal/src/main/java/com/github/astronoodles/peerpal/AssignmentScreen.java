package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.base.Assignment;
import com.github.astronoodles.peerpal.base.LanguageTextEditor;
import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.CreateAssignmentDialog;
import com.github.astronoodles.peerpal.extras.CloudStorageConfig;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    public AssignmentScreen(String name) {
        this.name = name;
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

        //data.addAll(obtainAssignments(name));
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
                        try {
                            FXMLLoader dialogLoader = new FXMLLoader(getClass().getResource("/main_screen.fxml"));

                            Parent root = dialogLoader.load();

                            LanguageTextEditor languageEditor = dialogLoader.getController();
                            languageEditor.connectToUser(name);

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
                } else if (event.getButton() == MouseButton.SECONDARY && (!row.isEmpty())) {
                    createContextMenu(row.getItem()).
                            show(row, event.getScreenX(), event.getScreenY());

                }
            });
            return row;
        });

        table.setItems(data);
        grid.add(table, 0, 1, 3, 1);
        GridPane.setHgrow(table, Priority.ALWAYS);
        GridPane.setVgrow(table, Priority.SOMETIMES);

        return grid;
    }


    public static GridPane setupPane(String name) {
        GridPane grid = new GridPane();

        grid.setVgap(10.0);
        grid.setPadding(new Insets(30, 20, 30, 25));

        //Top Heading Label

        Label welcome = new Label(String.format("Welcome %s. \nPlease find or create or wait for " +
                "your assignment to begin!", name));
        welcome.setFont(new Font("Cambria", 15));
        welcome.setWrapText(true);
        GridPane.setHgrow(welcome, Priority.SOMETIMES);

        ImageView refreshIcon = new ImageView(
                new Image(AssignmentScreen.class.getResourceAsStream("/refresh.png"), 50, 50, true, true));
        Button refreshButton = new Button("", refreshIcon);
        refreshButton.setPrefWidth(30);
        refreshButton.setTooltip(new Tooltip("Click here to refresh current assignments."));
        refreshButton.setPrefHeight(30);
//        GridPane.setHgrow(refreshButton, Priority.SOMETIMES);
//        GridPane.setVgrow(refreshButton, Priority.SOMETIMES);

        Label refreshText = new Label("Refresh in progress...");
        refreshText.setStyle("-fx-text-fill: #42a5f5; -fx-font-size: 13;");
        refreshText.setVisible(false);

        Label heading = new Label("NOW VIEWING: Home Page");
//        GridPane.setHgrow(heading, Priority.SOMETIMES);

        Background originalBG = refreshButton.getBackground();
        // Border originalBorder = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
        //        new CornerRadii(1), new BorderWidths(1)));

        Background pressedBG = new Background(new BackgroundFill(Color.web("#90caf9"),
                new CornerRadii(1), new Insets(3)));
        Border pressedBorder = new Border(new BorderStroke(Color.web("#1565c0"),
                BorderStrokeStyle.SOLID, new CornerRadii(1), new BorderWidths(2)));

        PauseTransition bgTransition = new PauseTransition(Duration.seconds(6));
        bgTransition.setOnFinished(event -> {
            refreshButton.setBackground(originalBG);
            refreshButton.setDefaultButton(true);
            refreshText.setVisible(false);
        });
        refreshButton.setOnAction((event) -> {
            refreshButton.setBackground(pressedBG);
            refreshButton.setBorder(pressedBorder);
            refreshText.setVisible(true);

            CloudStorageConfig config = new CloudStorageConfig();
            if (config.isCloudStorageFull()) config.downloadCloudStorage();

            bgTransition.playFromStart();
        });

        grid.add(welcome, 0, 0, 2, 1);
        grid.add(refreshButton, 3, 0, 1, 1);
        grid.add(heading, 0, 2, 2, 1);
        grid.add(refreshText, 0, 3, 2, 1);

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
            descAlert.initOwner(table.getScene().getWindow());
            descAlert.show();
        });

        MenuItem uploadItem = new MenuItem("Upload Assignment");
        uploadItem.setOnAction(e -> {
            //Adding file
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            CreateAssignmentDialog.mapExtensionToName().get(curAssignment.getFileExtension()),
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

                    Period latePeriod = AssignmentTeacherScreen.EXPIRY_PERIOD;
                    LocalDate lateDate = curAssignment.getEndDate().plus(latePeriod);

                    if (LocalDate.now().isAfter(lateDate)) {
                        curAssignment.setStatus(StudentAssignment.AssignmentStatus.LATE);
                    } else {
                        curAssignment.setStatus(StudentAssignment.AssignmentStatus.UPLOADED);
                    }
                    //backUpAssignments(data, name);
                    System.out.println(curAssignment);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        MenuItem feedbackItem = new MenuItem("See Teacher Feedback");
        feedbackItem.setOnAction(event -> {
            Alert feedbackAlert = new Alert(Alert.AlertType.INFORMATION, curAssignment.getAssignmentFeedback(), ButtonType.OK);
            feedbackAlert.initOwner(table.getScene().getWindow());
            feedbackAlert.setTitle("Teacher Feedback Notice");
            feedbackAlert.setHeaderText("See Your Teacher's Feedback");
            feedbackAlert.show();
        });

        if(curAssignment.getStatus() != StudentAssignment.AssignmentStatus.GRADED) {
            return new ContextMenu(descItem, uploadItem);
        } else return new ContextMenu(descItem, feedbackItem);
    }

}
