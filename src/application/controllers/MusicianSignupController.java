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
import java.util.regex.Pattern;

public class MusicianSignupController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField instrumentsField;
    @FXML
    private TextField experienceField;
    @FXML
    private Label statusLabel;

    @FXML
    public void handleSignUp() {
        String name = nameField.getText(); // Full name
        String email = emailField.getText();
        String password = passwordField.getText();
        String genre = genreField.getText();
        String instruments = instrumentsField.getText();
        String experience = experienceField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all required fields.");
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

        // Split the full name into first and last name
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO musicians (first_name, last_name, email, password, genre, instruments, experience, name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, genre);
            stmt.setString(6, instruments);
            stmt.setString(7, experience);
            stmt.setString(8, name); // Insert the full name in the 'name' column

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
            // Load the musician login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MusicianLogin.fxml"));
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

