package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.UserImageRepository;
import com.example.java_lms_group_01.session.LoggedInStudent;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class StudentDashboardController {

    @FXML
    private Label lblRegistrationNo;
    @FXML
    private ImageView imgProfile;
    @FXML
    private AnchorPane contentArea;

    private final UserImageRepository userImageRepository = new UserImageRepository();

    public void setStudentData(String registrationNo) {
        LoggedInStudent.setRegistrationNo(registrationNo);
        lblRegistrationNo.setText("Registration No: " + registrationNo);
        loadProfileImage(registrationNo);
        loadContent("/view/Student/student_profile.fxml");
    }

    @FXML
    private void navProfile(ActionEvent event) {
        loadContent("/view/Student/student_profile.fxml");
    }

    @FXML
    private void navAttendance(ActionEvent event) {
        loadContent("/view/Student/student_attendance.fxml");
    }

    @FXML
    private void navMedical(ActionEvent event) {
        loadContent("/view/Student/student_medical.fxml");
    }

    @FXML
    private void navCourses(ActionEvent event) {
        loadContent("/view/Student/student_courses.fxml");
    }

    @FXML
    private void navMaterials(ActionEvent event) {
        loadContent("/view/Student/student_materials.fxml");
    }

    @FXML
    private void navGrades(ActionEvent event) {
        loadContent("/view/Student/student_grades.fxml");
    }

    @FXML
    private void navTimetable(ActionEvent event) {
        loadContent("/view/Student/student_timetable.fxml");
    }

    @FXML
    private void navNotices(ActionEvent event) {
        loadContent("/view/Student/student_notices.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        LoggedInStudent.clear();
        openLoginPage();
    }

    // Load one student page into the dashboard content area.
    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            showError("Cannot open: " + fxmlPath, e);
        }
    }

    private void openLoginPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login_page.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("LMS Login");
            stage.show();
        } catch (IOException e) {
            showError("Cannot open login page.", e);
        }
    }

    private void loadProfileImage(String registrationNo) {
        try {
            ProfileImageUtil.loadImage(imgProfile, userImageRepository.findImagePathByUserId(registrationNo));
        } catch (SQLException e) {
            ProfileImageUtil.loadImage(imgProfile, null);
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
