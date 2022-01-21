package com.github.astronoodles.peerpal.revamped;

import com.github.astronoodles.peerpal.base.StudentAssignment;
import com.github.astronoodles.peerpal.dialogs.StudentAssignmentGrid;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;


import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import java.io.*;
import java.util.LinkedList;

public class TeacherFeedbackDialog {

    @FXML
    public VBox commentPane, mainBox;

    private Node baseNode;

    private final LinkedList<StudentAssignment.Feedback> lst = new LinkedList<>();

    protected static ScrollPane createScrollPane(String assignText, Insets margin) {
        TextArea textBox = new TextArea();
        textBox.setPrefWidth(200);
        textBox.setEditable(false);
        textBox.setText(assignText);
        textBox.setWrapText(true);
        textBox.setStyle("-fx-font-family: Arial, Times, sans-serif;\n" +
                "    -fx-font-size: 12;\n" +
                "    -fx-border-style: dashed;\n" +
                "    -fx-border-width: 2;\n" +
                "    -fx-border-radius: 1;");

        ScrollPane scrollPane = new ScrollPane(textBox);
        VBox.setVgrow(scrollPane, Priority.SOMETIMES);
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setPrefWidth(200);
        VBox.setMargin(scrollPane, margin);

        return scrollPane;
    }

    protected static WebView createWebView(String assignText, Insets margin) {
        WebView htmlViewer = new WebView();
        htmlViewer.setPrefHeight(200);
        htmlViewer.setPrefHeight(200);
        VBox.setVgrow(htmlViewer, Priority.SOMETIMES);
        VBox.setMargin(htmlViewer, margin);
        htmlViewer.getEngine().loadContent(assignText, "text/html");
        htmlViewer.setStyle("-fx-border-style: solid;\n" +
                "    -fx-border-width: 2;\n" +
                "    -fx-border-radius: 1;");
        return htmlViewer;
    }

    protected static WebView createRTFWebView(String assignText, Insets margin) {
        WebView rtfViewer = new WebView();
        rtfViewer.setPrefHeight(200);
        rtfViewer.setPrefWidth(200);
        VBox.setVgrow(rtfViewer, Priority.SOMETIMES);
        VBox.setMargin(rtfViewer, margin);
        rtfViewer.setStyle("-fx-border-style: solid;\n" +
                "    -fx-border-width: 2;\n" +
                "    -fx-border-radius: 1;");
        try {
            rtfViewer.getEngine().loadContent(rtfToHtml(new StringReader(assignText)), "text/html");
        } catch(IOException e) {
            e.printStackTrace();
        }
        return rtfViewer;
    }

    protected static Button createDefaultButton(String studentName, StudentAssignment assign, Insets margin) {
        Button viewAssign = new Button("View Assignment");
        viewAssign.setPrefHeight(50);
        viewAssign.setPrefWidth(100);
        VBox.setVgrow(viewAssign, Priority.SOMETIMES);
        VBox.setMargin(viewAssign, margin);
        viewAssign.setStyle("-fx-border-color: #ede7f6;\n" +
                "    -fx-border-radius: 1;\n" +
                "    -fx-border-insets: 1;\n" +
                "    -fx-font-family: 'Cambria', 'Archer', Times, Serif;");
        viewAssign.setOnAction(e -> {
            try {
                StudentAssignmentGrid.downloadAssignment(studentName, assign);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        });
        return viewAssign;
    }

    protected static Node initResponse(String studentName, StudentAssignment assign, String assignText) {
        Insets margin = new Insets(10);
        switch(assign.getFileExtension()) {
            case "txt":
                return createScrollPane(assignText, margin).getContent();
            case "html":
                return createWebView(assignText, margin);
            case "rtf":
                return createRTFWebView(assignText, margin);
            default:
                return createDefaultButton(studentName, assign, margin);
        }
    }

    public void initialize(String studentName, StudentAssignment assign, String assignText) {
       mainBox.getChildren().add(initResponse(studentName, assign, assignText));
    }

    @FXML
    public void createComment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment_tool.fxml"));
            CommentToolDialog dialog = loader.getController();

            if(baseNode instanceof TextArea) {
                TextArea area = (TextArea) baseNode;
                String text = area.getText();
                String currSelection = area.getSelectedText();

                dialog.initialize(text.indexOf(currSelection), text.indexOf(currSelection) + currSelection.length());
            } else if(baseNode instanceof WebView) {
                WebView wv = (WebView) baseNode;
                String text = (String) wv.getEngine().executeScript("window.getSelection().anchorNode.parentNode.textContent");
                String currSelection = (String) wv.getEngine().executeScript("window.getSelection().toString()");
                JSObject currRange = (JSObject) wv.getEngine().executeScript("window.getSelection().getRangeAt(0)");

                dialog.initialize(text.indexOf(currSelection), text.indexOf(currSelection) + currSelection.length(), currRange);
            } else {
                dialog.initialize(0, 0);
            }

            Parent commentTool = loader.load();

            Scene commentScene = new Scene(commentTool, 500, 400, Color.web("#e3f2fd"));
            commentScene.getStylesheets().add(getClass().getResource("/feedback_styles.css").toExternalForm());
            Stage commentStage = new Stage();
            commentStage.setScene(commentScene);
            commentStage.setTitle("Create A Comment");
            commentStage.setResizable(false);

            commentStage.showAndWait();

            if(!commentStage.isShowing()) {
                commentPane.getChildren().add(commentTool);

                StudentAssignment.Feedback feedback = dialog.getFeedback();
                lst.add(feedback);

                commentTool.setOnMouseEntered(e -> {
                    if(baseNode instanceof TextArea) {
                        TextArea area = (TextArea) baseNode;
                        area.selectRange(feedback.getStartPos(), feedback.getEndPos());
                    } else if(baseNode instanceof WebView) {
                        WebView wv = (WebView) baseNode;
                        JSObject range = dialog.getJSResult();
                        JSObject document = (JSObject) wv.getEngine().getDocument();

                        document.call("getSelection().addRange", range);
                    }
                });

            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedList<StudentAssignment.Feedback> getAllTeacherFeedback() {
        return lst;
    }

    public static String rtfToHtml(Reader rtf) throws IOException {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/rtf");
        EditorKit kitRtf = editorPane.getEditorKitForContentType("text/rtf");
        try {
            kitRtf.read(rtf, editorPane.getDocument(), 0);
            EditorKit kitHtml = editorPane.getEditorKitForContentType("text/html");
            Writer writer = new StringWriter();
            kitHtml.write(writer, editorPane.getDocument(), 0, editorPane.getDocument().getLength());
            return writer.toString();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }

}
