package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LandingPageController {

    @FXML
    public void goToMusicianSignup(ActionEvent event) {
        navigateTo("/application/views/MusicianSignup.fxml", event);
    }

    @FXML
    public void goToCafeSignup(ActionEvent event) {
        navigateTo("/application/views/CafeSignup.fxml", event);
    }

    @FXML
    public void goToMusicianLogin(ActionEvent event) {
        navigateTo("/application/views/MusicianLogin.fxml", event);
    }

    @FXML
    public void goToCafeLogin(ActionEvent event) {
        navigateTo("/application/views/CafeLogin.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene newScene = new Scene(loader.load());
            Stage newStage = new Stage();
            newStage.setScene(newScene);
            newStage.setTitle("StageSync");
            newStage.show();

            // Close current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
