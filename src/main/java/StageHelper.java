import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to transition between screens and hold key information.
 */
public class StageHelper {

    private StageHelper() {}

    private static class StageHelperSingleton {
        private static final StageHelper INSTANCE = new StageHelper();
    }

    /**
     * Singleton pattern for this class. There is only one instance to hold the key information needed.
     * @return A definitive instance of the class obtained from StageHelperSingleton
     * @see StageHelper.StageHelperSingleton
     */
    public static StageHelper getInstance(){
        return StageHelperSingleton.INSTANCE;
    }

    /**
     * This is a special map that holds the endings of verb tenses to whether the teacher has activated them.
     * The verb tenses are in regex because of certain imperfections with the perfect tenses (i.e he tenido)
     */
    public static Map<String[], Boolean> endingActive = new HashMap<>(6);

    /**
     * Transitions from one screen to the next.
     * @param title The title of the next screen.
     * @param width The width of the next screen.
     * @param height The height of the next screen.
     * @param pathToFxml The class resource path to the fxml design sheet.
     * @param pathToCss The class resource path to the css stylesheet.
     */
    public static void loadMainEditor(String title, int width, int height, String pathToFxml, String pathToCss){
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getInstance().getClass().getResource(pathToFxml));
            Scene main = new Scene(root, width, height);
            main.getStylesheets().addAll(getInstance().getClass().getResource(pathToCss).toExternalForm());

            primaryStage.setScene(main);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the current window. Most commonly used when a new screen is opened and only 1 screen needs to be active
     * at a time.
     * @param loadedNode The current screen.
     */
    public static void closeCurrentWindow(Node loadedNode) {
        Window win = loadedNode.getScene().getWindow();
        if (win instanceof Stage) {
            Stage currStage = (Stage) win;
            currStage.close();
        }
    }


}
