package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.LecturerContext;
import com.example.java_lms_group_01.util.UserImageRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LecturerProfileController {

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
    private TextField txtDepartment;
    @FXML
    private TextField txtPosition;
    @FXML
    private TextField txtPicturePath;

    @FXML
    public void initialize() {
        txtRegistrationNo.setEditable(false);
        loadProfile();
    }

    @FXML
    private void saveProfile() {
        String regNo = LecturerContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            show(Alert.AlertType.WARNING, "Session Error", "Lecturer session not found. Please login again.");
            return;
        }

        String userSql = "UPDATE users SET firstName = ?, lastName = ?, email = ?, address = ?, phoneNumber = ? WHERE user_id = ?";
        String lecturerSql = "UPDATE lecturer SET department = ?, position = ? WHERE registrationNo = ?";

        try {
            Connection connection = DBConnection.getInstance().getConnection();
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement userStmt = connection.prepareStatement(userSql)) {
                    userStmt.setString(1, value(txtFirstName));
                    userStmt.setString(2, value(txtLastName));
                    userStmt.setString(3, value(txtEmail));
                    userStmt.setString(4, value(txtAddress));
                    userStmt.setString(5, value(txtPhone));
                    userStmt.setString(6, regNo);
                    userStmt.executeUpdate();
                }

                UserImageRepository.upsertImagePath(connection, regNo, value(txtPicturePath));

                try (PreparedStatement roleStmt = connection.prepareStatement(lecturerSql)) {
                    roleStmt.setString(1, value(txtDepartment));
                    roleStmt.setString(2, value(txtPosition));
                    roleStmt.setString(3, regNo);
                    roleStmt.executeUpdate();
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }

            show(Alert.AlertType.INFORMATION, "Profile Updated", "Lecturer profile updated successfully.");
        } catch (Exception e) {
            show(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadProfile() {
        String regNo = LecturerContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.address, u.phoneNumber, l.department, l.position
                FROM users u
                INNER JOIN lecturer l ON l.registrationNo = u.user_id
                WHERE l.registrationNo = ?
                """;

        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, regNo);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    txtRegistrationNo.setText(safe(rs.getString("user_id")));
                    txtFirstName.setText(safe(rs.getString("firstName")));
                    txtLastName.setText(safe(rs.getString("lastName")));
                    txtEmail.setText(safe(rs.getString("email")));
                    txtAddress.setText(safe(rs.getString("address")));
                    txtPhone.setText(safe(rs.getString("phoneNumber")));
                    txtDepartment.setText(safe(rs.getString("department")));
                    txtPosition.setText(safe(rs.getString("position")));
                    txtPicturePath.setText(safe(UserImageRepository.findImagePathByUserId(connection, regNo)));
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
