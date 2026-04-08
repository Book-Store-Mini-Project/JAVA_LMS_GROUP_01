package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
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

public class TechnicalOfficerDashboardController {

    @FXML
    private Label lblRegistrationNo;

    @FXML
    private Label lblOfficerName;

    @FXML
    private Label lblOfficerEmail;

    @FXML
    private Label lblAttendanceCount;

    @FXML
    private Label lblMedicalCount;

    @FXML
    private Label lblUnreadNoticeCount;

    @FXML
    private Label lblUserId;

    @FXML
    private Label lblDepartment;

    @FXML
    private Label lblPhone;

    @FXML
    private Label lblAddress;
    @FXML
    private ImageView imgProfile;

    @FXML
    private AnchorPane contentArea;

    private final List<javafx.scene.Node> dashboardHomeNodes = new ArrayList<>();
    private final UserProfileRepository userProfileRepository = new UserProfileRepository();
    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();

    @FXML
    public void initialize() {
        lblRegistrationNo.setText("Registration No: -");
        lblOfficerName.setText("Name: -");
        lblOfficerEmail.setText("Email: -");
        lblAttendanceCount.setText("0");
        lblMedicalCount.setText("0");
        lblUnreadNoticeCount.setText("0");
        lblUserId.setText("User ID: -");
        lblDepartment.setText("Department: -");
        lblPhone.setText("Phone: -");
        lblAddress.setText("Address: -");
        dashboardHomeNodes.addAll(contentArea.getChildren());
    }

    public void setTechnicalOfficerData(String registrationNo) {
        TechnicalOfficerContext.setRegistrationNo(registrationNo);
        lblRegistrationNo.setText("Registration No: " + registrationNo);
        lblUserId.setText("User ID: " + registrationNo);
        loadOfficerDetails(registrationNo);
        loadDashboardCounts();
    }

    @FXML
    private void navDashboard(ActionEvent event) {
        contentArea.getChildren().setAll(dashboardHomeNodes);
        loadDashboardCounts();
    }

    @FXML
    private void navAttendance(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_attendance.fxml");
    }

    @FXML
    private void navProfile(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_profile.fxml");
    }

    @FXML
    private void navMedical(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_medical.fxml");
    }

    @FXML
    private void navNotices(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_notices.fxml");
    }

    @FXML
    private void navTimetables(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_timetable.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        TechnicalOfficerContext.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login_page.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("LMS Login");
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot open login page.");
            alert.showAndWait();
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot open: " + fxmlPath);
            alert.showAndWait();
        }
    }

    private void loadOfficerDetails(String registrationNo) {
        try {
            var profile = userProfileRepository.findTechnicalOfficerProfile(registrationNo);
            if (profile == null) {
                return;
            }
            String fullName = (raw(profile.getFirstName()) + " " + raw(profile.getLastName())).trim();
            lblOfficerName.setText("Name: " + (fullName.isBlank() ? "-" : fullName));
            lblOfficerEmail.setText("Email: " + safe(profile.getEmail()));
            lblPhone.setText("Phone: " + safe(profile.getPhoneNumber()));
            lblAddress.setText("Address: " + safe(profile.getAddress()));
            ProfileImageUtil.loadImage(imgProfile, profile.getProfileImagePath());
        } catch (SQLException e) {
            showError("Failed to load technical officer details.", e);
        }
    }

    private void loadDashboardCounts() {
        try {
            lblAttendanceCount.setText(String.valueOf(technicalOfficerRepository.countAttendance()));
            lblMedicalCount.setText(String.valueOf(technicalOfficerRepository.countMedical()));
            lblUnreadNoticeCount.setText(String.valueOf(technicalOfficerRepository.countNotices()));
        } catch (SQLException e) {
            lblAttendanceCount.setText("0");
            lblMedicalCount.setText("0");
            lblUnreadNoticeCount.setText("0");
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String raw(String value) {
        return value == null ? "" : value.trim();
    }
}
