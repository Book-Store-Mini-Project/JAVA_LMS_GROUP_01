package com.example.java_lms_group_01.Controller;

import com.example.java_lms_group_01.Controller.LandingPages.RoleLandingController;
import com.example.java_lms_group_01.Controller.AdminDashboard.AdminDashboard;
import com.example.java_lms_group_01.Controller.Lecturer.LecturerDashboardController;
import com.example.java_lms_group_01.Repository.AuthRepository;
import com.example.java_lms_group_01.Controller.Student.StudentDashboardController;
import com.example.java_lms_group_01.Controller.TechnicalOfficer.TechnicalOfficerDashboardController;
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

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField loginEmail;

    @FXML
    private PasswordField loginPass;

    private final AuthRepository authRepository = new AuthRepository();

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
            UserRole role = authRepository.findRoleByRegistrationNo(registrationNo, password);

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

    private void loadLandingPage(UserRole role, String registrationNo) throws Exception {
        String fxmlPath;
        String title;

        switch (role) {
            case ADMIN -> {
                fxmlPath = "/view/Admin/admin_dashboard.fxml";
                title = "Admin Dashboard";
            }
            case LECTURER -> {
                fxmlPath = "/view/Lecturer/Lecturer_dashboard.fxml";
                title = "Lecturer Dashboard";
            }
            case STUDENT -> {
                fxmlPath = "/view/Landing/student_landing.fxml";
                title = "Student Dashboard";
            }
            case TECHNICAL_OFFICER -> {
                fxmlPath = "/view/technicalofficer/technical_officer_dashboard.fxml";
                title = "Technical Officer Dashboard";
            }
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        if (role == UserRole.STUDENT) {
            StudentDashboardController controller = loader.getController();
            controller.setStudentData(registrationNo);
        } else if (role == UserRole.ADMIN) {
            AdminDashboard controller = loader.getController();
            controller.setAdminData(registrationNo);
        } else if (role == UserRole.LECTURER) {
            LecturerDashboardController controller = loader.getController();
            controller.setLecturerData(registrationNo);
        } else if (role == UserRole.TECHNICAL_OFFICER) {
            TechnicalOfficerDashboardController controller = loader.getController();
            controller.setTechnicalOfficerData(registrationNo);
        } else if (role != UserRole.ADMIN) {
            RoleLandingController controller = loader.getController();
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
