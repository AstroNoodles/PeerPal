import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AvatarScreen {

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

    public HBox createAvatarDialog(){
        HBox mainBox = new HBox(30);
        Path currDir = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "avatars");

        Image modelImage = new Image(getClass().getResource("/avatars/model.png").toString(), 200, 200,
                true, true, true);
        ImageView model = new ImageView(modelImage);
        mainBox.getChildren().add(model);

        try {
            long numFiles = getNumFiles(currDir);
            GridPane imageGrid = new GridPane();
            imageGrid.setHgap(3);
            imageGrid.setVgap(3);

            List<String> subFilePaths = getSubFilePaths(currDir);
            subFilePaths.remove("/avatars/model.png");

            double gridArrangement = Math.sqrt(numFiles);
            int gridWidth;
            int gridHeight;

            if(Math.ceil(gridArrangement) > gridArrangement){
                gridWidth = (int) gridArrangement;
                gridHeight = (int) gridArrangement + 1;
            } else {
                gridWidth = (int) gridArrangement;
                gridHeight = (int) gridArrangement;
            }


            for(int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    for (String subPath : subFilePaths) {
                        Image gridImage = new Image(subPath, 100, 100, true, true, true);
                        imageGrid.add(new ImageView(gridImage), x, y);
                    }
                }
            }
            mainBox.getChildren().add(imageGrid);

        } catch(IOException e){
            e.printStackTrace();
        }

        return mainBox;
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
                            map(p -> String.format("/avatars/%s", p.getFileName())).collect(Collectors.toList());
            subFilePaths.addAll(collector);
        } else {
            subFilePaths.add(folderPath.toString().replace("file:", ""));
        }
        return subFilePaths;
    }


}
