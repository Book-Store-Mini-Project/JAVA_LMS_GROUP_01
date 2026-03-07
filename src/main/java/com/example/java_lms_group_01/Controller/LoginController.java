package com.example.java_lms_group_01.Controller;

import com.example.java_lms_group_01.model.users.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField loginEmail;

    @FXML
    private PasswordField loginPass;

    @FXML
    void btnOnActionLogin(ActionEvent event) {
        String registrationNo = loginEmail.getText() == null ? "" : loginEmail.getText().trim();
        String password = loginPass.getText() == null ? "" : loginPass.getText().trim();

        if (registrationNo.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your registration number.");
            return;
        }

        if (password.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter your password.");
            return;
        }

        try {
            UserRole role = findRoleByRegistrationNo(registrationNo, password);

            if (role == null) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid registration number or password.");
                return;
            }

            loadLandingPage(role, registrationNo);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private UserRole findRoleByRegistrationNo(String registrationNo, String password) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        if (isPasswordValid(connection, "SELECT password FROM admin WHERE registrationNo = ?", registrationNo, password)) {
            return UserRole.ADMIN;
        }
        if (isPasswordValid(connection, "SELECT password FROM lecturer WHERE registrationNo = ?", registrationNo, password)) {
            return UserRole.LECTURER;
        }
        if (isPasswordValid(connection, "SELECT password FROM student WHERE registrationNo = ?", registrationNo, password)) {
            return UserRole.STUDENT;
        }
        if (isPasswordValid(connection, "SELECT password FROM tech_officer WHERE registrationNo = ?", registrationNo, password)) {
            return UserRole.TECHNICAL_OFFICER;
        }
        return null;
    }

    private boolean isPasswordValid(Connection connection, String sql, String registrationNo, String rawPassword) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                String storedPassword = resultSet.getString("password");
                return PasswordUtil.matches(rawPassword, storedPassword);
            }
        }
    }

    private void loadLandingPage(UserRole role, String registrationNo) throws Exception {
        String fxmlPath;
        String title;

        switch (role) {
            case ADMIN -> {
                fxmlPath = "/view/Admin/admin_dashboard.fxml";
                title = "Admin Dashboard";
            }
            case LECTURER -> {
                fxmlPath = "/view/Landing/lecturer_landing.fxml";
                title = "Lecturer Landing";
            }
            case STUDENT -> {
                fxmlPath = "/view/Landing/student_landing.fxml";
                title = "Student Landing";
            }
            case TECHNICAL_OFFICER -> {
                fxmlPath = "/view/Landing/technical_officer_landing.fxml";
                title = "Technical Officer Landing";
            }
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        if (role != UserRole.ADMIN) {
            com.example.java_lms_group_01.Controller.LandingPages.RoleLandingController controller = loader.getController();
            controller.setLandingData(role.value(), registrationNo);
        }

        Stage currentStage = (Stage) loginEmail.getScene().getWindow();
        currentStage.setTitle(title);
        currentStage.setScene(new Scene(root));
        currentStage.centerOnScreen();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
