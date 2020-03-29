import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;


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

    private static final String EMAIL_REGEX = "" +
            "^([a-zA-Z0-9_\\-.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))" +
            "([a-zA-Z]{2,4}|[0-9]{1,3})(]?)$";

    public static final String CSV_PATH = "./src/main/java/members/members.csv";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login_screen.fxml"));
            Scene main = new Scene(root, 600, 600);
            main.getStylesheets().addAll(getClass().getResource("/login_screen.css").toExternalForm());

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
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(CSV_PATH)));
            String line;
            while ((line = reader.readLine()) != null){
                System.out.println(line);
                System.out.println(username.getText());
                String[] users = line.split(", ");
                System.out.println(users[0]);
                System.out.println(users[1]);
                System.out.println(users[2]);
                if(username.getText().equals(users[0]) && pwd.getText().equals(users[1]) && class_code.getText().equals(users[2])){
                    StageHelper.loadMainEditor("The Language Learn App", 600, 500,
                            "/main_screen.fxml", "/main_screen.css");
                    StageHelper.closeCurrentWindow(username);
                    hasUser = true;
                }
            }
            reader.close();

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
        System.out.println("banana");
        try {
            File members = new File(CSV_PATH);
            System.out.println(members);
            if(!members.exists()){
                if(!members.mkdir()) System.out.println("net");
            }

            if(pwdSign.getText().equals(repwdSign.getText()) && email.getText().matches(EMAIL_REGEX)) {

                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(members)), true);
                writer.println(String.format("%s, %s, %s, %s",
                        usernameSign.getText(), pwdSign.getText(), classSign.getText(), email.getText()));
                writer.close();
                errorText.setText("SUCCESS! Now, login in the next tab.");
            } else {
                errorText.setText("The passwords do not match or your email is not in the right format");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToPrevTab(){
        tabPane.getSelectionModel().select(signUpTab);
    }

    @FXML
    private void selectAvatar(){
        System.out.println("hiw");
        openAvatarScreen();
    }

    @FXML
    private void changeAvatar(){
        openAvatarScreen();
    }

    private void openAvatarScreen(){
        System.out.println("hi");
        Stage avatarDialog = new Stage();
        avatarDialog.initModality(Modality.WINDOW_MODAL);
        avatarDialog.setResizable(false);
        avatarDialog.setTitle("Choose Your Avatar!");

        AvatarScreen as = new AvatarScreen();
        Scene dialogScene = new Scene(as.createAvatarDialog(), 1000, 400);
        avatarDialog.setScene(dialogScene);
        avatarDialog.show();

    }


}

