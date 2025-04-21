package application.controllers;

import application.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
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

        // Email validation - must end with example.com or gmail.com
        if (!email.endsWith("example.com") && !email.endsWith("gmail.com")) {
            statusLabel.setText("Email must end with example.com or gmail.com");
            return;
        }

        // Password validation - must have minimum 5 letters
        if (password.length() < 5) {
            statusLabel.setText("Password must have at least 5 characters");
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

    @FXML
    public void handleSignUpLink() {
        try {
            // Load the musician signup page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MusicianSignup.fxml"));
            Parent signupPage = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the signup page scene
            Scene scene = new Scene(signupPage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error navigating to signup page");
        }
    }

    @FXML
    public void handleBack() {
        try {
            // Load the landing page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/LandingPage.fxml"));
            Parent landingPage = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the landing page scene
            Scene scene = new Scene(landingPage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error returning to landing page");
        }
    }

    public void handleLogout(ActionEvent actionEvent) {
    }
}
