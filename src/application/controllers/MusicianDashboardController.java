package application.controllers;

import application.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.sql.*;

public class MusicianDashboardController {

    @FXML private Label nameLabel, phoneLabel, emailLabel;
    @FXML private Label genreLabel, instrumentsLabel, experienceLabel;
    @FXML private TableView<Gig> gigTable;
    @FXML private TableColumn<Gig, String> titleCol, dateCol, payCol;
    @FXML private Label applyStatus;
    @FXML private ListView<String> applicationList;

    private int musicianId;
    private String musicianEmail;

    @FXML
    public void initialize() {
        try {
            loadProfile();
            setupGigTable();
            loadApplications();
        } catch (Exception e) {
            e.printStackTrace();
            applyStatus.setText("Error initializing: " + e.getMessage());
        }
    }

    private void loadProfile() throws Exception {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM musicians WHERE musician_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, musicianId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Basic Info
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                nameLabel.setText(fullName);
                phoneLabel.setText(rs.getString("phone"));
                emailLabel.setText(rs.getString("email"));

                // Musical Background
                genreLabel.setText(rs.getString("genre"));
                instrumentsLabel.setText(rs.getString("instruments"));
                experienceLabel.setText(rs.getString("experience") + " years");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            applyStatus.setText("Error loading profile: " + e.getMessage());
            throw e;
        }
    }

    @FXML
    private void editProfile() {
        try {
            // Load the edit profile dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/EditProfileDialog.fxml"));
            Parent editDialog = loader.load();

            // Get the controller and pass current musician data
            EditProfileDialogController controller = loader.getController();
            controller.setMusicianData(musicianId);

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Profile");
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
            applyStatus.setText("Error opening edit dialog: " + e.getMessage());
        }
    }

    private void setupGigTable() {
        gigTable.getItems().clear();
        gigTable.getColumns().clear();

        titleCol = new TableColumn<>("Title");
        dateCol = new TableColumn<>("Date & Time");
        payCol = new TableColumn<>("Pay");

        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        payCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPay()));

        gigTable.getColumns().addAll(titleCol, dateCol, payCol);
        loadGigs();
    }

    private void loadGigs() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT gig_id, title, CONCAT(date, ' ', time) AS date_time, pay, description, cafe_id FROM gigs";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ObservableList<Gig> gigs = FXCollections.observableArrayList();
            while (rs.next()) {
                Gig gig = new GigImpl(
                        rs.getInt("gig_id"),
                        rs.getString("title"),
                        rs.getString("date_time"),
                        rs.getString("pay"),
                        rs.getString("description"),
                        "", // Empty string for requirements since we're not using it
                        rs.getInt("cafe_id")
                );
                gigs.add(gig);
            }
            gigTable.setItems(gigs);
        } catch (Exception e) {
            e.printStackTrace();
            applyStatus.setText("Error loading gigs: " + e.getMessage());
        }
    }

    public void applyForGig() {
        ObservableList<Gig> selectedGigs = gigTable.getSelectionModel().getSelectedItems();
        if (selectedGigs.isEmpty()) {
            applyStatus.setText("Please select a gig to apply.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            for (Gig gig : selectedGigs) {
                int gigId = gig.getGigId();

                String checkSQL = "SELECT * FROM applications WHERE musician_id = ? AND gig_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                checkStmt.setInt(1, musicianId);
                checkStmt.setInt(2, gigId);
                ResultSet checkResult = checkStmt.executeQuery();

                if (checkResult.next()) {
                    applyStatus.setText("Already applied for this gig.");
                    return;
                }

                String applySQL = "INSERT INTO applications (gig_id, musician_id, status) VALUES (?, ?, 'Pending')";
                PreparedStatement applyStmt = conn.prepareStatement(applySQL);
                applyStmt.setInt(1, gigId);
                applyStmt.setInt(2, musicianId);
                applyStmt.executeUpdate();

                applyStatus.setText("Application submitted successfully.");
                loadApplications();
            }
        } catch (Exception e) {
            e.printStackTrace();
            applyStatus.setText("Error applying to gig: " + e.getMessage());
        }
    }

    private void loadApplications() {
        applicationList.getItems().clear();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT g.title, a.status FROM applications a " +
                    "JOIN gigs g ON a.gig_id = g.gig_id WHERE a.musician_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, musicianId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String app = "Gig: " + rs.getString("title") + " | Status: " + rs.getString("status");
                applicationList.getItems().add(app);
            }

            if (applicationList.getItems().isEmpty()) {
                applicationList.getItems().add("No applications yet.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            applyStatus.setText("Error loading applications: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Load the login page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/MusicianLogin.fxml"));
            Parent loginPage = loader.load();

            // Get the current stage
            Stage stage = (Stage) nameLabel.getScene().getWindow();

            // Set the login page scene
            Scene scene = new Scene(loginPage);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            applyStatus.setText("Error logging out");
        }
    }

    public void setMusicianId(int id) {
        this.musicianId = id;
        try {
            loadProfile();
            loadGigs();
            loadApplications();
        } catch (Exception e) {
            e.printStackTrace();
            applyStatus.setText("Error loading data: " + e.getMessage());
        }
    }

    public void setMusicianEmail(String email) {
        this.musicianEmail = email;
    }
}

