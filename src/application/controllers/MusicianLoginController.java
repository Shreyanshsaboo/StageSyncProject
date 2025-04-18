package application.controllers;

import application.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MusicianLoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Email and password are required.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM musicians WHERE email = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, password);  // Ideally, use hashed password comparison here

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        openMusicianDashboard();
                    } else {
                        statusLabel.setText("Invalid credentials.");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("An error occurred. Please try again.");
        }
    }

    private void openMusicianDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MusicianDashboard.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller and pass the musician data
            MusicianDashboardController controller = loader.getController();

            // Get musician_id from database
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "SELECT musician_id FROM musicians WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, emailField.getText());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int musicianId = rs.getInt("musician_id");
                    controller.setMusicianId(musicianId); // Set musician ID first
                    controller.setMusicianEmail(emailField.getText()); // Then set email which loads the profile
                }
            }

            Stage stage = new Stage();
            stage.setTitle("Musician Dashboard");
            stage.setScene(scene);
            stage.show();

            // Close login window
            Stage current = (Stage) emailField.getScene().getWindow();
            current.close();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading dashboard.");
        }
    }

    public void handleLogout(ActionEvent actionEvent) {
    }
}
