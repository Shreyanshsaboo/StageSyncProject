package application.controllers;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
}

