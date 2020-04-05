import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    private static ImageView model;



    public VBox createAvatarDialog(Stage stage){
        HBox mainBox = new HBox(30);

        Image modelImage = new Image(getClass().getResource("/model.png").toExternalForm(), 200, 200,
                true, true, true);
        System.out.println(getClass().getResource("/model.png").toExternalForm());
        model = new ImageView(modelImage);
        model.setId("/model.png");

        VBox borderImage = new VBox(model);
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
            int numFiles = (int) getNumFiles();

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
                    imButton.setOnAction(e -> {
                        Image buttonImg = ((ImageView) imButton.getGraphic()).getImage();
                        model.setImage(buttonImg);
                        model.setId(subFilePath);

                    });
                    imageGrid.add(imButton, column, row);
                    column++;
                }
            }
            mainBox.getChildren().add(imageGrid);

            Button submit = new Button("Submit");
            submit.setStyle("-fx-background-color: #26C6DA; " +
                    "-fx-font-family: 'DejaVu Sans', Arial, Helvetica, sans-serif; -fx-font-size: 14; " +
                    "-fx-text-fill: black");
            submit.setPrefWidth(100);
            submit.setPrefHeight(50);
            submit.setOnAction(e -> {
                stage.close();
                System.out.println("XX - " + model.getId());
            });

            titleBox.getChildren().add(submit);

        } catch(IOException e){
            e.printStackTrace();
        }

        return titleBox;
    }


    private long getNumFiles() throws IOException {
        if(Files.isDirectory(currDir, LinkOption.NOFOLLOW_LINKS)){
            return Files.list(currDir).count();
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

    public static String getSelectedImage(){
        if(model == null){
            return "/model.png";
        }
        return model.getId();
    }

    public static void setSelectedImage(String newImage){
        model.setId(newImage);
    }


}
