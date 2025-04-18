package application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChooseUserTypeController {

    @FXML
    public void goToMusicianSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MusicianSignup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Musician Sign Up");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToCafeSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CafeSignup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Cafe Owner Sign Up");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
