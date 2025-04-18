package application.controllers;

import application.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.IOException;

public class CafeDashboardController {

    @FXML private Label nameLabel, emailLabel, locationLabel;
    @FXML private Label phoneLabel, websiteLabel, cityLabel, postalCodeLabel;
    @FXML private Label capacityLabel, musicTypesLabel, hoursLabel, descriptionLabel;
    @FXML private TextField titleField, dateField, payField, requirementsField;
    @FXML private TextArea descField;
    @FXML private Label gigStatusLabel;
    @FXML private ListView<HBox> applicationsList;

    private int cafeId; // Replace with logged-in cafe ID

    @FXML
    public void initialize() {
        try {
            loadProfile();
            loadApplications(); // Load all applications instead of using dropdown
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error initializing profile: " + e.getMessage());
        }
    }

    private void loadProfile() throws Exception {
        try (Connection conn = getDatabaseConnection()) {
            String sql = "SELECT cafe_id, name, email, phone_number, location FROM cafes WHERE cafe_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Basic Info
                nameLabel.setText(rs.getString("name"));
                emailLabel.setText(rs.getString("email"));
                phoneLabel.setText(rs.getString("phone_number"));
                locationLabel.setText(rs.getString("location"));

                // Set default values for non-existent fields
                websiteLabel.setText("Not Available");
                cityLabel.setText("Not Available");
                postalCodeLabel.setText("Not Available");
                capacityLabel.setText("Not Available");
                musicTypesLabel.setText("Not Available");
                hoursLabel.setText("Not Available");
                descriptionLabel.setText("Not Available");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error loading profile: " + e.getMessage());
            throw e;
        }
    }

    @FXML
    public void postGig() {
        String title = titleField.getText();
        String desc = descField.getText();
        String date = dateField.getText();
        String pay = payField.getText();
        String requirements = requirementsField.getText();

        if (title.isEmpty() || date.isEmpty()) {
            gigStatusLabel.setText("Title and Date are required.");
            return;
        }

        if (!isValidDateFormat(date)) {
            gigStatusLabel.setText("Invalid date format. Use YYYY-MM-DD HH:MM.");
            return;
        }

        if (pay.isEmpty()) pay = "Negotiable";
        if (requirements.isEmpty()) requirements = "None";

        try (Connection conn = getDatabaseConnection()) {
            if (isGigOverlap(conn, date)) {
                gigStatusLabel.setText("The gig overlaps with an existing one.");
                return;
            }

            String sql = "INSERT INTO gigs (cafe_id, title, description, date, time, location, pay, requirements) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            stmt.setString(2, title);
            stmt.setString(3, desc);
            stmt.setString(4, date.split(" ")[0]);
            stmt.setString(5, date.split(" ")[1]);
            stmt.setString(6, "Default Location");
            stmt.setString(7, pay);
            stmt.setString(8, requirements);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                gigStatusLabel.setText("Gig posted successfully.");
            } else {
                gigStatusLabel.setText("Failed to post gig.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error: " + e.getMessage());
        }
    }

    private boolean isGigOverlap(Connection conn, String date) throws SQLException {
        String[] dateTime = date.split(" ");
        String datePart = dateTime[0];
        String timePart = dateTime[1];

        String sql = "SELECT * FROM gigs WHERE cafe_id = ? AND date = ? AND time = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, cafeId);
        stmt.setString(2, datePart);
        stmt.setString(3, timePart);

        ResultSet rs = stmt.executeQuery();
        return rs.next();
    }

    private void loadApplications() throws Exception {
        applicationsList.getItems().clear();

        try (Connection conn = getDatabaseConnection()) {
            String sql = "SELECT m.first_name, m.last_name, m.genre, m.instruments, a.status, a.application_id, g.cafe_id " +
                    "FROM applications a " +
                    "JOIN musicians m ON a.musician_id = m.musician_id " +
                    "JOIN gigs g ON a.gig_id = g.gig_id " +
                    "WHERE g.cafe_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<HBox> apps = FXCollections.observableArrayList();
            while (rs.next()) {
                int applicationId = rs.getInt("application_id");
                String status = rs.getString("status");

                Label info = new Label("Name: " + rs.getString("first_name") + " " + rs.getString("last_name") +
                        ", Genre: " + rs.getString("genre") +
                        ", Instruments: " + rs.getString("instruments") +
                        ", Status: " + status);

                Button acceptBtn = new Button("Accept");
                Button rejectBtn = new Button("Reject");

                if (!status.equalsIgnoreCase("Pending")) {
                    acceptBtn.setDisable(true);
                    rejectBtn.setDisable(true);
                }

                acceptBtn.setOnAction(e -> updateApplicationStatus(applicationId, "Accepted"));
                rejectBtn.setOnAction(e -> updateApplicationStatus(applicationId, "Rejected"));

                HBox hbox = new HBox(10, info, acceptBtn, rejectBtn);
                hbox.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
                apps.add(hbox);
            }

            if (apps.isEmpty()) {
                HBox empty = new HBox(new Label("No applications yet."));
                applicationsList.getItems().add(empty);
            } else {
                applicationsList.setItems(apps);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error loading applications: " + e.getMessage());
        }
    }

    private void updateApplicationStatus(int applicationId, String newStatus) {
        try (Connection conn = getDatabaseConnection()) {
            // First verify if this application is for a gig owned by this café
            String verifySQL = "SELECT g.cafe_id FROM applications a " +
                    "JOIN gigs g ON a.gig_id = g.gig_id " +
                    "WHERE a.application_id = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifySQL);
            verifyStmt.setInt(1, applicationId);
            ResultSet verifyRs = verifyStmt.executeQuery();

            if (!verifyRs.next() || verifyRs.getInt("cafe_id") != cafeId) {
                gigStatusLabel.setText("You don't have permission to update this application.");
                return;
            }

            String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, applicationId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                gigStatusLabel.setText("Application " + newStatus.toLowerCase() + ".");
                loadApplications(); // Refresh the list
            } else {
                gigStatusLabel.setText("Failed to update status.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error updating status: " + e.getMessage());
        }
    }

    private boolean isValidDateFormat(String date) {
        try {
            LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private Connection getDatabaseConnection() throws Exception {
        Connection conn = DBUtil.getConnection();
        if (conn == null) {
            gigStatusLabel.setText("Database connection failed.");
            throw new SQLException("Connection failed");
        }
        return conn;
    }

    public void setCafeId(int cafeId) {
        this.cafeId = cafeId;
    }

    @FXML
    private void editProfile() {
        try {
            // Load the edit profile dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/EditCafeDialog.fxml"));
            Parent editDialog = loader.load();

            // Get the controller and pass current cafe data
            EditCafeDialogController controller = loader.getController();
            controller.setCafeData(cafeId);

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Cafe Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(nameLabel.getScene().getWindow());

            Scene scene = new Scene(editDialog);
            dialogStage.setScene(scene);

            // Show the dialog and wait for it to close
            dialogStage.showAndWait();

            // Refresh the profile data after editing
            loadProfile();
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error opening edit dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/CafeLogin.fxml"));
            Parent loginPage = loader.load();

            // Get the current stage
            Stage stage = (Stage) nameLabel.getScene().getWindow();

            // Set the login page scene
            Scene scene = new Scene(loginPage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error logging out");
        }
    }
}
