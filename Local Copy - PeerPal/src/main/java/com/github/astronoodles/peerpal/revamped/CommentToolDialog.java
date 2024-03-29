package com.github.astronoodles.peerpal.revamped;

import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import netscape.javascript.JSObject;

public class CommentToolDialog {

    @FXML
    public TextArea commentBox;

    private int startPos;
    private int endPos;
    private JSObject jsObject;

    public void initialize(int startPos, int endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public void initialize(int startPos, int endPos, JSObject jsObject) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.jsObject = jsObject;
    }

    @FXML
    public void submitComment() {
        StageHelper.closeCurrentWindow(commentBox);
    }

    public StudentAssignment.Feedback getFeedback() {
        return new StudentAssignment.Feedback(startPos, endPos, commentBox.getText());
    }

    public JSObject getJSResult() {
        return jsObject;
    }

}
