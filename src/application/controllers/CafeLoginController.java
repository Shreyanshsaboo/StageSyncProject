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
import java.sql.ResultSet;

public class CafeLoginController {

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
            String sql = "SELECT * FROM cafes WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int cafeId = rs.getInt("cafe_id");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CafeDashboard.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Café Owner Dashboard");

                // Pass the cafeId to the CafeDashboardController
                CafeDashboardController controller = loader.getController();
                controller.setCafeId(cafeId);

                System.out.println("Cafe ID set to: " + cafeId);

                stage.show();

                Stage current = (Stage) emailField.getScene().getWindow();
                current.close();
            } else {
                statusLabel.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleSignUpLink() {
        try {
            // Load the cafe signup page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CafeSignup.fxml"));
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
    private void handleBack() {
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
