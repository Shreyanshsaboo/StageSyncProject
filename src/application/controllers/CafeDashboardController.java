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
    @FXML private Label phoneLabel, cityLabel, postalCodeLabel;
    @FXML private Label capacityLabel, musicTypesLabel, hoursLabel, descriptionLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private TextField dateField;
    @FXML private TextField timeField;
    @FXML private TextField locationField;
    @FXML private TextField addressField;
    @FXML private TextField payField;
    @FXML private Label gigStatusLabel;
    @FXML private ListView<HBox> applicationsList;
    @FXML private ListView<HBox> gigsList;

    private int cafeId;
    private boolean gigsLoaded = false; // Track if gigs have been loaded

    @FXML
    public void initialize() throws Exception {
        loadProfile();
        // Don't load gigs here - will load on first tab visit
    }

    public void setCafeId(int cafeId) {
        this.cafeId = cafeId;
        try {
            loadProfile();
            loadApplications();
            // Don't load gigs here - will load on first tab visit
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error initializing profile: " + e.getMessage());
        }
    }

    private void loadProfile() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT name, email, phone_number, location, capacity FROM cafes WHERE cafe_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                emailLabel.setText(rs.getString("email"));
                phoneLabel.setText(rs.getString("phone_number"));
                locationLabel.setText(rs.getString("location"));
                cityLabel.setText(rs.getString("location")); // Using location as city since they're the same in your schema
                capacityLabel.setText(String.valueOf(rs.getInt("capacity")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error loading profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleTabChange() {
        if (!gigsLoaded) {
            loadGigs();
            gigsLoaded = true;
        }
    }

    @FXML
    public void postGig() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO gigs (cafe_id, title, description, date, time, location, address, pay) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, cafeId);
            stmt.setString(2, titleField.getText());
            stmt.setString(3, descField.getText());
            stmt.setString(4, dateField.getText());
            stmt.setString(5, timeField.getText());
            stmt.setString(6, locationField.getText());
            stmt.setString(7, addressField.getText());
            stmt.setString(8, payField.getText());

            stmt.executeUpdate();

            // Clear fields after successful post
            titleField.clear();
            descField.clear();
            dateField.clear();
            timeField.clear();
            locationField.clear();
            addressField.clear();
            payField.clear();

            gigStatusLabel.setText("Gig posted successfully!");

            // Reset gigsLoaded flag to force refresh on next tab visit
            gigsLoaded = false;
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error posting gig: " + e.getMessage());
        }
    }

    private void loadApplications() throws Exception {
        applicationsList.getItems().clear();

        try (Connection conn = getDatabaseConnection()) {
            // Modified query to only get accepted applications
            String sql = "SELECT m.first_name, m.last_name, m.genre, m.instruments, a.status, a.application_id, g.cafe_id " +
                    "FROM applications a " +
                    "JOIN musicians m ON a.musician_id = m.musician_id " +
                    "JOIN gigs g ON a.gig_id = g.gig_id " +
                    "WHERE g.cafe_id = ? AND (a.status = 'Accepted' OR a.status = 'Pending')";
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

                // Create the HBox first
                HBox hbox = new HBox(10, info, acceptBtn, rejectBtn);
                hbox.setStyle("-fx-padding: 5; -fx-alignment: center-left;");

                // Only enable buttons for pending applications
                if (!status.equalsIgnoreCase("Pending")) {
                    acceptBtn.setDisable(true);
                    rejectBtn.setDisable(true);
                }

                acceptBtn.setOnAction(e -> updateApplicationStatus(applicationId, "Accepted"));
                rejectBtn.setOnAction(e -> {
                    updateApplicationStatus(applicationId, "Rejected");
                    // Remove the application from the list when rejected
                    applicationsList.getItems().remove(hbox);
                });

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

    private void loadGigs() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM gigs WHERE cafe_id = ? ORDER BY date DESC, time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            // Clear existing items
            gigsList.getItems().clear();

            while (rs.next()) {
                String title = rs.getString("title");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String pay = rs.getString("pay");
                String location = rs.getString("location");
                String address = rs.getString("address");

                // Create a formatted string for each gig
                String gigInfo = String.format("%s - %s %s - $%s\nLocation: %s\nAddress: %s",
                        title, date, time, pay, location, address != null ? address : "No address provided");

                HBox gigBox = new HBox(10);
                Label gigLabel = new Label(gigInfo);
                gigLabel.setWrapText(true);
                gigLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

                gigBox.getChildren().add(gigLabel);
                gigBox.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");

                gigsList.getItems().add(gigBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error loading gigs: " + e.getMessage());
        }
    }

    private void switchToLoginScene() throws IOException {
        // ... existing code ...
    }
}
