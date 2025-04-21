package application.controllers;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CafeSignupController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField locationField;
    @FXML private Label statusLabel;

    @FXML
    public void handleSignUp() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String location = locationField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || location.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        // Name validation - cannot contain numbers
        if (name.matches(".*\\d.*")) {
            statusLabel.setText("Business name cannot contain numbers.");
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
            String sql = "INSERT INTO cafes (name, email, password, location) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, location);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                statusLabel.setStyle("-fx-text-fill: green;");
                statusLabel.setText("Sign-up successful! Please log in.");
            } else {
                statusLabel.setText("Sign-up failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleLoginLink() {
        try {
            // Load the cafe login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CafeLogin.fxml"));
            Parent loginPage = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();

            // Set the login page scene
            Scene scene = new Scene(loginPage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error navigating to login page");
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
}
