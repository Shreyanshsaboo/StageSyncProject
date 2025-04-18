package application.controllers;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.*;

public class EditCafeDialogController {
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField locationField;
    @FXML private TextField venueTypeField;
    @FXML private TextField capacityField;
    @FXML private TextField nameField;

    private int cafeId;

    public void setCafeData(int cafeId) {
        this.cafeId = cafeId;
        loadCafeData();
    }

    private void loadCafeData() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT name, email, phone_number, location FROM cafes WHERE cafe_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone_number"));
                locationField.setText(rs.getString("location"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE cafes SET name = ?, email = ?, phone_number = ?, location = ? WHERE cafe_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, nameField.getText());
            stmt.setString(2, emailField.getText());
            stmt.setString(3, phoneField.getText());
            stmt.setString(4, locationField.getText());
            stmt.setInt(5, cafeId);

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