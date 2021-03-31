package com.github.astronoodles.peerpal;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class AvatarScreenFX {

    @FXML
    private ImageView selectedAvatar;

    private static int avatarID = 0;

    @FXML
    private Button submitButton;

    public static int getSelectedAvatarID(){
        return avatarID;
    }

    @FXML
    private void onAvatarSelected(ActionEvent ae){
        /* All buttons that have this event triggered will have an ImageView graphic in them.
            If this is not true, double check the FXML. */
        Button avatarButton = (Button) ae.getSource();
        ImageView newAvatar = (ImageView) avatarButton.getGraphic();

        SequentialTransition st = createFadeScaleTransition(selectedAvatar);
        selectedAvatar.setImage(newAvatar.getImage());

        // The trick is that the URL of the image is duplicated in the ID.
        String avatarCodeID = newAvatar.getId();
        String selectedAvatarID = String.valueOf(avatarCodeID.charAt(avatarCodeID.indexOf("D") + 1));
        avatarID = Integer.parseInt(selectedAvatarID);

        st.play();
    }

    @FXML
    private void onSubmitAvatar(){
        Stage currStage = (Stage) submitButton.getScene().getWindow();
        currStage.fireEvent(new WindowEvent(currStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private SequentialTransition createFadeScaleTransition(ImageView oldImage){
        SequentialTransition st = new SequentialTransition();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), oldImage);
        fadeIn.setFromValue(0.2f);
        fadeIn.setToValue(1f);
        fadeIn.setCycleCount(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), oldImage);
        scaleIn.setFromX(1);
        scaleIn.setFromY(1);
        scaleIn.setToX(0.5);
        scaleIn.setToY(0.5);
        scaleIn.setCycleCount(1);

        ScaleTransition scaleBack = new ScaleTransition(Duration.millis(200), oldImage);
        scaleIn.setFromX(0.5);
        scaleIn.setFromY(0.5);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        scaleIn.setCycleCount(1);

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(fadeIn, scaleBack);
        st.getChildren().addAll(scaleIn, pt);
        return st;
    }

}
