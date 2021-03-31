package com.github.astronoodles.peerpal.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.util.Random;

public class ClassCodeDialog {

    @FXML
    private TextField teacherName;

    @FXML
    private TextField roomNum;

    @FXML
    private TextField languageSelect;

    @FXML
    private TextField classCode;

    @FXML
    private Button codeButton;

    private static final int CODE_LENGTH = 7;

    @FXML
    private void createCode(){
        if(!roomNum.getText().matches("^\\d+$")){ // matches a positive integer
            Alert properNum = new Alert(Alert.AlertType.WARNING,
                    "Please input a proper room number for your classroom", ButtonType.OK);
            properNum.setTitle("Room Number Error");
            properNum.setHeaderText("Check The Room Number");
            properNum.show();
        }

        if(!roomNum.getText().trim().equals("") || !teacherName.getText().trim().equals("") ||
                !languageSelect.getText().trim().equals("")) {
            String codeString = generateCode();
            classCode.setText(codeString);
            codeButton.setDisable(true);

            createDatabase(teacherName.getText(), languageSelect.getText(), codeString);
        }
    }

    private String generateCode(){
        final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
        final StringBuilder code = new StringBuilder();
        final Random r = new Random();

        for(int i = 0; i < CODE_LENGTH; i++) {
            int index = r.nextInt(CHARS.length());
            code.append(CHARS.charAt(index));
        }
        return code.toString();
    }

    private void createDatabase(String teacher, String language, String classCode) {
        // TODO once I have the database running
    }

}
