package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.UserImageRepository;
import com.example.java_lms_group_01.util.LecturerContext;
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
import java.util.ArrayList;
import java.util.List;

public class LecturerDashboardController {

    @FXML
    private Label lblRegistrationNo;
    @FXML
    private Label lblRole;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private ImageView imgProfile;

    private final List<javafx.scene.Node> dashboardHomeNodes = new ArrayList<>();
    private final UserImageRepository userImageRepository = new UserImageRepository();

    @FXML
    public void initialize() {
        lblRegistrationNo.setText("Registration No: -");
        lblRole.setText("LECTURER");
        dashboardHomeNodes.addAll(contentArea.getChildren());
    }

    public void setLecturerData(String registrationNo) {
        LecturerContext.setRegistrationNo(registrationNo);
        lblRegistrationNo.setText("Registration No: " + registrationNo);
        loadProfileImage(registrationNo);
    }

    @FXML
    private void navDashboard(ActionEvent event) {
        contentArea.getChildren().setAll(dashboardHomeNodes);
    }

    @FXML
    private void navProfile(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_profile.fxml");
    }

    @FXML
    private void navMaterials(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_materials.fxml");
    }

    @FXML
    private void navTimetable(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_timetable.fxml");
    }

    @FXML
    private void navMarks(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_marks.fxml");
    }

    @FXML
    private void navStudents(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_students.fxml");
    }

    @FXML
    private void navEligibility(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_eligibility.fxml");
    }

    @FXML
    private void navGpa(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_gpa.fxml");
    }

    @FXML
    private void navAttendance(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_attendance.fxml");
    }

    @FXML
    private void navNotices(ActionEvent event) {
        loadContent("/view/Lecturer/Lecturer_notices.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        LecturerContext.clear();
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

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    private void loadProfileImage(String registrationNo) {
        try {
            ProfileImageUtil.loadImage(imgProfile, userImageRepository.findImagePathByUserId(registrationNo));
        } catch (SQLException ignored) {
            ProfileImageUtil.loadImage(imgProfile, null);
        }
    }
}
