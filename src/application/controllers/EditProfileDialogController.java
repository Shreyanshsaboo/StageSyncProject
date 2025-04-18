package application.controllers;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.*;

public class EditProfileDialogController {
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField genreField;
    @FXML private TextField instrumentsField;
    @FXML private TextField experienceField;

    private int musicianId;

    public void setMusicianData(int musicianId) {
        this.musicianId = musicianId;
        loadMusicianData();
    }

    private void loadMusicianData() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT email, phone, genre, instruments, experience FROM musicians WHERE musician_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, musicianId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                genreField.setText(rs.getString("genre"));
                instrumentsField.setText(rs.getString("instruments"));
                experienceField.setText(rs.getString("experience"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE musicians SET email = ?, phone = ?, genre = ?, instruments = ?, experience = ? WHERE musician_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, emailField.getText());
            stmt.setString(2, phoneField.getText());
            stmt.setString(3, genreField.getText());
            stmt.setString(4, instrumentsField.getText());
            stmt.setString(5, experienceField.getText());
            stmt.setInt(6, musicianId);

            stmt.executeUpdate();
            closeDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}