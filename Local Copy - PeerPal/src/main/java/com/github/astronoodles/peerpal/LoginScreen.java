package com.github.astronoodles.peerpal;

import com.github.astronoodles.peerpal.extras.StageHelper;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.Arrays;


public class LoginScreen extends Application {

    @FXML
    private TextField username, pwd, class_code;

    @FXML
    private TextField email, usernameSign, pwdSign, repwdSign, classSign;

    @FXML
    private Label errorText;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab signUpTab;
    
    @FXML
    private ImageView avatar,
            changedAvatar;

    private static final String EMAIL_REGEX = "" +
            "^([a-zA-Z0-9_\\-.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))" +
            "([a-zA-Z]{2,4}|[0-9]{1,3})(]?)$";

    public static final String CSV_PATH = "./src/main/java/com/github/astronoodles/peerpal/" +
            "extras/members.csv";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login_screen.fxml"));
            Scene main = new Scene(root, 600, 700);
            main.getStylesheets().addAll(getClass().getResource("/login_screen.css").
                    toExternalForm());

            primaryStage.setScene(main);
            primaryStage.setTitle("Language Learn Login");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void enter() {
        boolean hasUser = false;
        int classCodeEntries = 0;
        try(BufferedReader reader = new BufferedReader(new FileReader(CSV_PATH))){
            String line;
            while ((line = reader.readLine()) != null){
                String[] users = line.split(", ");
                // 4 Things In The CSV File
                // Full Name, Password, Class Code, Email, Avatar File ID
                //System.out.println(Arrays.toString(users));

                if(class_code.getText().equals(users[2])){
                    classCodeEntries += 1;
                }
                //System.out.println(classCodeEntries);
                if(username.getText().trim().equals(users[0]) &&
                        pwd.getText().equals(users[1]) && class_code.getText().equals(users[2])){

                    if(classCodeEntries > 1) {
                        AssignmentScreen screen = new AssignmentScreen(users[0], users[4]);
                        Scene scene = new Scene(screen.loadStage());

                        Stage stage = new Stage();
                        stage.setScene(scene);
                        stage.show();

                        // Comment out below for debug purposes
                        //StageHelper.closeCurrentWindow(username);
                        hasUser = true;
                        break;
                    } else {
                        AssignmentTeacherScreen screen = new AssignmentTeacherScreen(users[0], users[4]);
                        Scene s = new Scene(screen.loadStage());

                        Stage st = new Stage();
                        st.setOnCloseRequest((event) -> {
                            screen.backUpAssignments();
                            screen.updateGridStudentAssignments();
                        });
                        st.setScene(s);
                        st.show();

                        // Comment out below for debug purposes
                        //StageHelper.closeCurrentWindow(username);
                        hasUser = true;
                        break;
                    }

                }
            }

            if(!hasUser){
                Alert notice = new Alert(Alert.AlertType.INFORMATION,
                        "You have not signed up for this. Sign up now by clicking the bottom most button.",
                        new ButtonType("OK", ButtonBar.ButtonData.OK_DONE), new ButtonType("Cancel",
                        ButtonBar.ButtonData.CANCEL_CLOSE));
                notice.show();
            }


        } catch(IOException e){
            e.printStackTrace();
        }
    }


    @FXML
    private void signUp(){
        try {
            File members = new File(CSV_PATH);
            if(!members.createNewFile())
                System.out.println("The members.csv file is already created and has login info.");

            if(pwdSign.getText().equals(repwdSign.getText()) && email.getText().matches(EMAIL_REGEX)) {

                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(members, true)), true);
                writer.print(String.format("%s, %s, %s, %s, %d\r\n",
                        usernameSign.getText().trim(), pwdSign.getText(), classSign.getText(), email.getText(),
                        AvatarScreenFX.getSelectedAvatarID()));
                writer.close();
                errorText.setText("SUCCESS! Go to the next tab.");
                errorText.setTextFill(Color.GREEN);
            } else {
                errorText.setText("The passwords do not match.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // MENU ITEMS

    @FXML
    private void handleShortcuts(KeyEvent ke){
        if(ke.isShortcutDown() && ke.isShiftDown() && ke.getCode().equals(KeyCode.C)){
            createClassCode();
        }
    }

    @FXML
    private void createClassCode(){
        StageHelper.loadSceneFXML("Generate Class Code", 400, 300,
                "/create_class_code.fxml",
                "/login_screen.css");
    }

    @FXML
    private void showAboutDialog() {
        Alert alertDialog = new Alert(Alert.AlertType.INFORMATION, StageHelper.TEAM_TEXT,
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alertDialog.setTitle("About PeerPal");
        alertDialog.setHeaderText("What is PeerPal?");
        alertDialog.show();
    }

    // MENU ITEMS

    @FXML
    private void goToPrevTab(){
        tabPane.getSelectionModel().select(signUpTab);
    }

    @FXML
    private void selectAvatar(){
        EventHandler<WindowEvent> onCloseFirstTab = changeAvatarOnClose(avatar);
        openAvatarScreen(onCloseFirstTab);
    }

    private EventHandler<WindowEvent> changeAvatarOnClose(ImageView view) {
        return (windowEvent) -> {
            System.out.println(AvatarScreenFX.getSelectedAvatarID());
            String imageURL = getClass().getResource(
                    String.format("/avatars/D%d.png", AvatarScreenFX.getSelectedAvatarID())).toExternalForm();

            view.setImage(new Image(
                    imageURL,
                    175, 300, true, true));
        };
    }

    @FXML
    private void onLogin(){
        String imageURL = getClass().getResource(
                String.format("/avatars/D%d.png", AvatarScreenFX.getSelectedAvatarID())).toExternalForm();

        changedAvatar.setImage(new Image(imageURL,
                175, 300, true, true));
    }

    @FXML
    private void changeAvatar(){
        EventHandler<WindowEvent> onCloseSecondTab = changeAvatarOnClose(changedAvatar);
        openAvatarScreen(onCloseSecondTab);
    }

    private void openAvatarScreen(EventHandler<WindowEvent> onClose){
        Stage as = StageHelper.loadSceneFXML("Choose Your Avatar!", 800, 400,
                "/avatar_screen.fxml", "/avatar_screen.css");

        as.setOnCloseRequest(onClose);
    }


}

