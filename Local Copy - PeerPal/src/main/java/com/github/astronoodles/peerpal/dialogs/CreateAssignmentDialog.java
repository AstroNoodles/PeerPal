package com.github.astronoodles.peerpal.dialogs;

import com.github.astronoodles.peerpal.base.Assignment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class CreateAssignmentDialog {

    @FXML
    private TextField assignName;

    @FXML
    private TextArea assignDesc;

    @FXML
    private DatePicker assignStart;

    @FXML
    private DatePicker assignEnd;

    @FXML
    private ComboBox<String> comboExtensions;

    private Assignment curAssignment;
    private String teacherName;

    public void connectAssignmentTable(String teacherName) {
        this.teacherName = teacherName;
    }

    public Assignment getCurAssignment() {
        return curAssignment;
    }


    @FXML
    private void createAssignment() {
        if (assignName.getText().trim().equals("") || assignStart.getEditor().getText().equals("")
                || assignEnd.getEditor().getText().equals("")) {
            Alert reqAlert = new Alert(Alert.AlertType.WARNING, "Please Answer All Required Questions", ButtonType.OK);
            reqAlert.setTitle("Required Questions Alert");
            reqAlert.setHeaderText("Required Questions");
            reqAlert.show();
            return;
        } else if (assignEnd.getValue().isBefore(assignStart.getValue())) {
            Alert dateAlert = new Alert(Alert.AlertType.WARNING,
                    "Please Ensure The End Date For The Assignment Is Before " +
                            "The Start Date", ButtonType.OK);
            dateAlert.setTitle("Time Difference Alert");
            dateAlert.setHeaderText("Time Difference Warning");
            dateAlert.show();
            return;
        } else if (comboExtensions.getValue().isEmpty()) {
            Alert extensionAlert = new Alert(Alert.AlertType.WARNING,
                    "Please input a required file type for your assignment", ButtonType.OK);
            extensionAlert.setTitle("File Extension Alert");
            extensionAlert.setHeaderText("Required File Type Warning");
            extensionAlert.show();
        } else {
            String extension = comboExtensions.getValue().split("-")[0].trim().toLowerCase();
            curAssignment = new Assignment(assignName.getText().trim(), teacherName,
                    assignDesc.getText(), extension, assignStart.getValue(),
                    assignEnd.getValue());
            //teacherAssignments.add(newAssign);
        }

        // close the window
        Stage currStage = (Stage) assignName.getScene().getWindow();
        currStage.close();
    }

    public static Map<String, String> mapExtensionToName() {
        Map<String, String> extensionName = new HashMap<>(6);
        extensionName.put("pdf", "PDF Files");
        extensionName.put("docx", "DOCX Files (Microsoft Word)");
        extensionName.put("txt", "TXT Files (Text Files)");
        extensionName.put("pptx", "PPTX Files (Microsoft PowerPoint");
        extensionName.put("rtf", "RTF Files (Rich Text Format)");
        extensionName.put("html", "HTML Files (Webpages)");
        return extensionName;
    }

}
