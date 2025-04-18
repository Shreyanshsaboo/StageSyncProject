package application.controllers;

import application.DBUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.sql.*;

public class EditCafeDialogController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField locationField;
    @FXML private TextField cityField;
    @FXML private TextField capacityField;
    @FXML private TextArea descriptionField;

    private int cafeId;

    public void setCafeData(int cafeId) {
        this.cafeId = cafeId;
        loadCafeData();
    }

    private void loadCafeData() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT name, email, phone_number, location, capacity FROM cafes WHERE cafe_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cafeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone_number"));
                locationField.setText(rs.getString("location"));
                cityField.setText(rs.getString("location"));
                capacityField.setText(String.valueOf(rs.getInt("capacity")));
                // Description might be null, so handle it gracefully
                String description = rs.getString("description");
                descriptionField.setText(description != null ? description : "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update cafes table
                String cafeSql = "UPDATE cafes SET name = ?, email = ?, phone_number = ?, location = ?, " +
                        "capacity = ? WHERE cafe_id = ?";
                PreparedStatement cafeStmt = conn.prepareStatement(cafeSql);

                cafeStmt.setString(1, nameField.getText());
                cafeStmt.setString(2, emailField.getText());
                cafeStmt.setString(3, phoneField.getText());
                cafeStmt.setString(4, locationField.getText());
                cafeStmt.setInt(5, Integer.parseInt(capacityField.getText()));
                cafeStmt.setInt(6, cafeId);

                cafeStmt.executeUpdate();

                // Update address in gigs table if it exists
                String gigsSql = "UPDATE gigs SET address = ? WHERE cafe_id = ?";
                PreparedStatement gigsStmt = conn.prepareStatement(gigsSql);
                gigsStmt.setString(1, locationField.getText());
                gigsStmt.setInt(2, cafeId);
                gigsStmt.executeUpdate();

                conn.commit();
                closeDialog();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}