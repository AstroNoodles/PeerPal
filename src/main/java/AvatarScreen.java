import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AvatarScreen {

    private static final String MODEL_CSS =
            "-fx-border-radius: 2; -fx-padding: 5";
    //private static final Path currDir =
    //        Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
    private static final Path currDir =
            Paths.get(System.getProperty("user.dir"));


    // TEST
    public static void main(String[] args){
        AvatarScreen as = new AvatarScreen();
        // Path currDir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "avatars");
        System.out.println(currDir);
        try {
            System.out.println(as.getNumFiles(currDir));
            System.out.println(as.getSubFilePaths());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VBox createAvatarDialog(){
        HBox mainBox = new HBox(30);

        Image modelImage = new Image(getClass().getResource("/model.png").toExternalForm(), 200, 200,
                true, true, true);
        System.out.println(getClass().getResource("/model.png").toExternalForm());
        ImageView model = new ImageView(modelImage);
        model.setStyle(MODEL_CSS);

        StackPane borderImage = new StackPane(model);
        borderImage.setStyle(MODEL_CSS);

        VBox titleBox = new VBox(3);
        Label title = new Label("Choose your Avatar!");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: black; -fx-padding: 10; " +
                "-fx-font-family: 'DejaVu Sans', Arial, Helvetica, sans-serif");
        title.setPrefWidth(500);
        title.setPrefHeight(300);

        titleBox.getChildren().addAll(title, mainBox);

        mainBox.getChildren().addAll(borderImage);

        try {
            GridPane imageGrid = new GridPane();
            imageGrid.setHgap(5);
            imageGrid.setVgap(5);
            imageGrid.setPadding(new Insets(10, 10, 10, 0));

            List<String> subFilePaths = getSubFilePaths();
            // subFilePaths.remove("/avatars/model.png");
            subFilePaths.remove("/model.png");
            System.out.println(subFilePaths);
            int numFiles = (int) getNumFiles(currDir);

            double gridArrangement = Math.sqrt(numFiles);
            int gridWidth;

            if(Math.ceil(gridArrangement) > gridArrangement){
                gridWidth = (int) gridArrangement + 1;
            } else {
                gridWidth = (int) gridArrangement;
            }


            int row = 0;
            int column = 0;
            for (String subFilePath : subFilePaths) {
                if (column > gridWidth) {
                    column = 0;
                    row++;
                }
                System.out.println(column);
                System.out.println(subFilePath);
                if (subFilePath.endsWith(".png")) {
                    Image im = new Image(getClass().getResource(subFilePath).toExternalForm(),
                            150, 150, true, true, true);
                    Button imButton = new Button("", new ImageView(im));
                    imButton.setPrefWidth(170);
                    imButton.setPrefHeight(170);
                    imageGrid.add(imButton, column, row);
                    column++;
                }
            }
            mainBox.getChildren().add(imageGrid);

        } catch(IOException e){
            e.printStackTrace();
        }

        return titleBox;
    }


    private long getNumFiles(Path folderPath) throws IOException {
        if(Files.isDirectory(folderPath, LinkOption.NOFOLLOW_LINKS)){
            return Files.list(folderPath).count();
        } else {
            return 1;
        }
    }

    private List<String> getSubFilePaths() throws IOException {
        System.out.println(currDir);
        Path resPath = Paths.get(currDir.toString(), "src", "main");

        // TODO
        // Find a better way around doing this. This seems spotty... Ask StackOverflow???
        String[] imgs = new String[]{"/D1.png", "/D2.png", "/D3.png", "/D4.png", "/D5.png", "/D6.png"};

        return Arrays.asList(imgs);

    }


}
