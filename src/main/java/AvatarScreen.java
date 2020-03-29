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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AvatarScreen {

    private static final String MODEL_CSS =
            "-fx-border-radius: 2; -fx-padding: 5";


    // TEST
    public static void main(String[] args){
        AvatarScreen as = new AvatarScreen();
        Path currDir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "avatars");
        System.out.println(currDir);
        try {
            Files.list(currDir).forEach(System.out::println);
            System.out.println(Files.isDirectory(currDir));
            System.out.println(as.getNumFiles(currDir));
            System.out.println(as.getSubFilePaths(currDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VBox createAvatarDialog(){
        HBox mainBox = new HBox(30);
        Path currDir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");

        Image modelImage = new Image(getClass().getResource("/avatars/model.png").toExternalForm(), 200, 200,
                true, true, true);
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

            List<String> subFilePaths = getSubFilePaths(currDir);
            subFilePaths.remove("/avatars/model.png");
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
                Image im = new Image(subFilePath, 150, 150, true, true, true);
                Button imButton = new Button("", new ImageView(im));
                imButton.setPrefWidth(170);
                imButton.setPrefHeight(170);
                imageGrid.add(imButton, column, row);
                column++;
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

    private List<String> getSubFilePaths(Path folderPath) throws IOException {
        List<String> subFilePaths = new ArrayList<>();
        if(Files.isDirectory(folderPath, LinkOption.NOFOLLOW_LINKS)){
            List<String> collector =
                    Files.list(folderPath).
                            map(p -> String.format("/avatars/%s", p.toAbsolutePath().toString())).collect(Collectors.toList());
            subFilePaths.addAll(collector);
        } else {
            subFilePaths.add(String.format("/avatars/%s", folderPath.getFileName()));
        }
        return subFilePaths;
    }


}
