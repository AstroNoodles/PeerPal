package com.github.astronoodles.peerpal.extras;

import com.github.astronoodles.peerpal.LoginScreen;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to transition between screens and hold key information.
 */
public class StageHelper {

    public static final String TEAM_TEXT = "The purpose of the PeerPal app is to " +
            "initiate a large-scale computational project to contrive a virtual assistant in a learning platform " +
            "that is capable of helping students learn world languages such as Spanish and Chinese at their own " +
            "individualized pace and accommodate instructors by augmenting the grading process for assignments.\n" +
            "\n\nCreated By The PeerPal Team \n" +
            "Founded By Michael Batavia and Jonayet Lavin.";

    public static final String SAMPLE_CODE = "SPANISH3";
    private static final StageHelper helper = new StageHelper();

    private StageHelper() {
    }

    /**
     * Singleton pattern for this class. There is only one instance to hold the key information needed.
     *
     * @return A definitive instance of the class obtained from StageHelperSingleton
     * @see StageHelper#getInstance
     */
    public static StageHelper getInstance() {
        return helper;
    }

    /**
     * This is a special map that holds the endings of verb tenses to whether the teacher has activated them.
     * The verb tenses are in regex because of certain imperfections with the perfect tenses (i.e he tenido)
     */
    public static Map<String[], Boolean> endingActive = new HashMap<>(6);

    /**
     * Transitions from one screen to the next.
     *
     * @param title      The title of the next screen.
     * @param width      The width of the next screen.
     * @param height     The height of the next screen.
     * @param pathToFxml The class resource path to the fxml design sheet.
     * @param pathToCss  The class resource path to the css stylesheet.
     */
    public static Stage loadSceneFXML(String title, int width, int height, String pathToFxml, String pathToCss) {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getInstance().getClass().getResource(pathToFxml));
            Scene main = new Scene(root, width, height);
            main.getStylesheets().addAll(getInstance().getClass().getResource(pathToCss).toExternalForm());

            primaryStage.setScene(main);
            primaryStage.setTitle(title);
            primaryStage.show();

            return primaryStage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the current window. Most commonly used when a new screen is opened and only 1 screen needs to be active
     * at a time.
     * This may be useful at a later time.
     *
     * @param loadedNode The current screen.
     */
    public static void closeCurrentWindow(Node loadedNode) {
        Window win = loadedNode.getScene().getWindow();
        if (win instanceof Stage) {
            Stage currStage = (Stage) win;
            currStage.close();
        }
    }

    /**
     * Creates a Map<String, String> between the student or teacher's name and the link to their avatar.
     * This mapping may be used in the student assignment grid to show all of the students' avatars or may be used
     * to show the teacher's avatar while completing a lesson. <br>
     * This method reads the members.csv file and records the user's name and the link to their avatar in the
     * resources/avatars folder.
     *
     * @return A mapping between the user's name and their avatar from the available avatars in the program so far.
     */
    public static Map<String, Integer> getUserAvatarMapping() {
        // it is rare that this hashmap will be increased beyond capacity
        // (initial capacity is rough estimate of # of rows of hash map)
        Map<String, Integer> userAvatarMap = new HashMap<>(40, 0.9f);
        Path membersPath = Paths.get(LoginScreen.CSV_PATH);

        try {
            Files.lines(membersPath).forEach(line -> {
                String[] components = line.split(", ");
                String username = components[0];
                int imageID = Integer.parseInt(components[4]);
                userAvatarMap.put(username, imageID);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        return userAvatarMap;
    }


}
