package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
import com.example.java_lms_group_01.util.UserImageRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TechnicalOfficerProfileController {

    @FXML
    private TextField txtRegistrationNo;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtPicturePath;

    @FXML
    public void initialize() {
        txtRegistrationNo.setEditable(false);
        loadProfile();
    }

    @FXML
    private void saveProfile() {
        String registrationNo = TechnicalOfficerContext.getRegistrationNo();
        if (registrationNo == null || registrationNo.isBlank()) {
            show(Alert.AlertType.WARNING, "Session Error", "Technical officer session not found. Please login again.");
            return;
        }

        String sql = "UPDATE users SET firstName = ?, lastName = ?, email = ?, phoneNumber = ?, address = ? WHERE user_id = ?";
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value(txtFirstName));
                statement.setString(2, value(txtLastName));
                statement.setString(3, value(txtEmail));
                statement.setString(4, value(txtPhone));
                statement.setString(5, value(txtAddress));
                statement.setString(6, registrationNo);
                statement.executeUpdate();
            }
            UserImageRepository.upsertImagePath(connection, registrationNo, value(txtPicturePath));
            show(Alert.AlertType.INFORMATION, "Profile Updated", "Profile details updated successfully.");
        } catch (SQLException e) {
            show(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadProfile() {
        String registrationNo = TechnicalOfficerContext.getRegistrationNo();
        if (registrationNo == null || registrationNo.isBlank()) {
            return;
        }

        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.phoneNumber, u.address
                FROM users u
                INNER JOIN tech_officer t ON t.registrationNo = u.user_id
                WHERE t.registrationNo = ?
                """;

        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, registrationNo);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    txtRegistrationNo.setText(safe(rs.getString("user_id")));
                    txtFirstName.setText(safe(rs.getString("firstName")));
                    txtLastName.setText(safe(rs.getString("lastName")));
                    txtEmail.setText(safe(rs.getString("email")));
                    txtPhone.setText(safe(rs.getString("phoneNumber")));
                    txtAddress.setText(safe(rs.getString("address")));
                    txtPicturePath.setText(safe(UserImageRepository.findImagePathByUserId(connection, registrationNo)));
                }
            }
        } catch (SQLException e) {
            show(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private String value(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
