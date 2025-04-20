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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

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
        System.out.println("\n=== Cafe Dashboard Initialization ===");
        System.out.println("Setting cafe ID to: " + cafeId);
        this.cafeId = cafeId;
        try {
            // First verify the cafe exists and get its name
            Connection conn = DBUtil.getConnection();
            String cafeSql = "SELECT name FROM cafes WHERE cafe_id = ?";
            PreparedStatement cafeStmt = conn.prepareStatement(cafeSql);
            cafeStmt.setInt(1, cafeId);
            ResultSet cafeRs = cafeStmt.executeQuery();

            if (cafeRs.next()) {
                System.out.println("Successfully connected as cafe: " + cafeRs.getString("name"));
            }

            // Check what gigs this cafe has
            String gigsSql = "SELECT gig_id, title FROM gigs WHERE cafe_id = ?";
            PreparedStatement gigsStmt = conn.prepareStatement(gigsSql);
            gigsStmt.setInt(1, cafeId);
            ResultSet gigsRs = gigsStmt.executeQuery();

            System.out.println("\nGigs owned by this cafe:");
            boolean hasGigs = false;
            while (gigsRs.next()) {
                hasGigs = true;
                System.out.println("- Gig ID: " + gigsRs.getInt("gig_id") + ", Title: " + gigsRs.getString("title"));
            }
            if (!hasGigs) {
                System.out.println("No gigs found for this cafe");
            }

            loadProfile();
            loadApplications();
            System.out.println("\nInitialization complete!");
            System.out.println("================================\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in setCafeId: " + e.getMessage());
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
        System.out.println("\n=== Loading Applications ===");
        System.out.println("Current Cafe ID: " + cafeId);

        try (Connection conn = getDatabaseConnection()) {
            // First, check what gigs this cafe owns
            String gigCheckSql = "SELECT gig_id, title FROM gigs WHERE cafe_id = ?";
            PreparedStatement gigCheckStmt = conn.prepareStatement(gigCheckSql);
            gigCheckStmt.setInt(1, cafeId);
            ResultSet gigRs = gigCheckStmt.executeQuery();

            System.out.println("\nGigs owned by cafe " + cafeId + ":");
            boolean hasGigs = false;
            while (gigRs.next()) {
                hasGigs = true;
                System.out.println("- Gig ID: " + gigRs.getInt("gig_id") + ", Title: " + gigRs.getString("title"));
            }
            if (!hasGigs) {
                System.out.println("No gigs found for this cafe!");
            }

            // Remove the status filter from SQL query to show all applications
            String sql = "SELECT m.first_name, m.last_name, m.genre, m.instruments, " +
                    "a.status, a.application_id, g.gig_id, g.title as gig_title " +
                    "FROM applications a " +
                    "JOIN musicians m ON a.musician_id = m.musician_id " +
                    "JOIN gigs g ON a.gig_id = g.gig_id " +
                    "WHERE g.cafe_id = ? " +
                    "ORDER BY a.application_date DESC";

            System.out.println("\nExecuting query to find all applications:");
            System.out.println(sql);

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<HBox> apps = FXCollections.observableArrayList();
            int applicationCount = 0;

            System.out.println("\nApplications found:");
            while (rs.next()) {
                applicationCount++;
                int applicationId = rs.getInt("application_id");
                String status = rs.getString("status");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String gigTitle = rs.getString("gig_title");

                System.out.println(String.format("- App ID: %d, Name: %s %s, Status: %s, Gig: %s",
                        applicationId, firstName, lastName, status, gigTitle));

                Label info = new Label(String.format("Gig: %s\nName: %s %s\nGenre: %s\nInstruments: %s",
                        gigTitle, firstName, lastName, rs.getString("genre"), rs.getString("instruments")));
                info.setWrapText(true);
                info.setStyle("-fx-font-size: 14px;");

                Label statusLabel = new Label("Status: " + status);
                statusLabel.setStyle("-fx-font-weight: bold;");

                // Style status label based on status
                switch (status.toLowerCase()) {
                    case "accepted":
                        statusLabel.setStyle(statusLabel.getStyle() + "; -fx-text-fill: #28a745;"); // Green
                        break;
                    case "rejected":
                        statusLabel.setStyle(statusLabel.getStyle() + "; -fx-text-fill: #dc3545;"); // Red
                        break;
                    default: // Pending
                        statusLabel.setStyle(statusLabel.getStyle() + "; -fx-text-fill: #ffc107;"); // Yellow
                        break;
                }

                Button acceptBtn = new Button("Accept");
                Button rejectBtn = new Button("Reject");

                // Style the buttons
                acceptBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                // Disable buttons if application is not pending
                if (!status.equalsIgnoreCase("Pending")) {
                    acceptBtn.setDisable(true);
                    rejectBtn.setDisable(true);
                }

                HBox buttonBox = new HBox(10, acceptBtn, rejectBtn);
                buttonBox.setStyle("-fx-alignment: center;");

                VBox infoBox = new VBox(5, info, statusLabel);
                HBox hbox = new HBox(20, infoBox, buttonBox);

                // Style the container based on status
                String baseStyle = "-fx-padding: 15; -fx-alignment: center-left; -fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-width: 2;";

                switch (status.toLowerCase()) {
                    case "accepted":
                        hbox.setStyle(baseStyle + "; -fx-border-color: #28a745;"); // Green border
                        break;
                    case "rejected":
                        hbox.setStyle(baseStyle + "; -fx-border-color: #dc3545;"); // Red border
                        break;
                    default: // Pending
                        hbox.setStyle(baseStyle + "; -fx-border-color: #ffc107;"); // Yellow border
                        break;
                }

                // Store applicationId in final variable to use in lambda
                final int currentAppId = applicationId;

                acceptBtn.setOnAction(event -> {
                    System.out.println("\n=== Accept Button Clicked ===");
                    System.out.println("Attempting to accept application ID: " + currentAppId);

                    try {
                        String updateSql = "UPDATE applications SET status = 'Accepted' WHERE application_id = ?";
                        System.out.println("Executing SQL: " + updateSql + " with ID: " + currentAppId);

                        Connection updateConn = getDatabaseConnection();
                        PreparedStatement updateStmt = updateConn.prepareStatement(updateSql);
                        updateStmt.setInt(1, currentAppId);
                        int result = updateStmt.executeUpdate();
                        System.out.println("Update result (rows affected): " + result);

                        if (result > 0) {
                            System.out.println("Database update successful!");
                            // Update UI
                            acceptBtn.setDisable(true);
                            rejectBtn.setDisable(true);
                            hbox.setStyle(baseStyle + "; -fx-border-color: #28a745;");
                            statusLabel.setText("Status: Accepted");
                            statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #ffc107;", "-fx-text-fill: #28a745;"));
                            gigStatusLabel.setText("Application accepted successfully!");
                            System.out.println("UI updated successfully");
                        } else {
                            System.out.println("Warning: No rows were updated in the database!");
                            gigStatusLabel.setText("Error: Could not update application status");
                        }
                        updateConn.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.err.println("Error details: " + ex.getMessage());
                        gigStatusLabel.setText("Error accepting application: " + ex.getMessage());
                    }
                    System.out.println("=== Accept Operation Complete ===\n");
                });

                rejectBtn.setOnAction(event -> {
                    System.out.println("\n=== Reject Button Clicked ===");
                    System.out.println("Attempting to reject application ID: " + currentAppId);

                    try {
                        String rejectSql = "UPDATE applications SET status = 'Rejected' WHERE application_id = ?";
                        System.out.println("Executing SQL: " + rejectSql + " with ID: " + currentAppId);

                        Connection rejectConn = getDatabaseConnection();
                        PreparedStatement rejectStmt = rejectConn.prepareStatement(rejectSql);
                        rejectStmt.setInt(1, currentAppId);
                        int result = rejectStmt.executeUpdate();
                        System.out.println("Update result (rows affected): " + result);

                        if (result > 0) {
                            System.out.println("Database update successful!");
                            // Update UI
                            acceptBtn.setDisable(true);
                            rejectBtn.setDisable(true);
                            hbox.setStyle(baseStyle + "; -fx-border-color: #dc3545;");
                            statusLabel.setText("Status: Rejected");
                            statusLabel.setStyle(statusLabel.getStyle().replace("-fx-text-fill: #ffc107;", "-fx-text-fill: #dc3545;"));
                            gigStatusLabel.setText("Application rejected successfully!");
                            System.out.println("UI updated successfully");
                        } else {
                            System.out.println("Warning: No rows were updated in the database!");
                            gigStatusLabel.setText("Error: Could not update application status");
                        }
                        rejectConn.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.err.println("Error details: " + ex.getMessage());
                        gigStatusLabel.setText("Error rejecting application: " + ex.getMessage());
                    }
                    System.out.println("=== Reject Operation Complete ===\n");
                });

                apps.add(hbox);
            }

            System.out.println("\nTotal applications found: " + applicationCount);

            if (apps.isEmpty()) {
                System.out.println("No applications to display");
                HBox empty = new HBox(new Label("No applications yet for your gigs."));
                empty.setStyle("-fx-padding: 10; -fx-alignment: center;");
                applicationsList.getItems().add(empty);
            } else {
                System.out.println("Setting " + apps.size() + " applications to the list");
                applicationsList.setItems(apps);
            }

            System.out.println("=== Applications Loading Complete ===\n");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading applications: " + e.getMessage());
            gigStatusLabel.setText("Error loading applications: " + e.getMessage());
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
            switchToLoginScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void reloadApplications() {
        try {
            loadApplications();
            gigStatusLabel.setText("Applications reloaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            gigStatusLabel.setText("Error reloading applications: " + e.getMessage());
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

