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

/**
 * Main dashboard for technical officers.
 * It shows summary information and loads working pages into the content area.
 */
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
        setLabelText(lblRegistrationNo, "Registration No: -");
        setLabelText(lblOfficerName, "Name: -");
        setLabelText(lblOfficerEmail, "Email: -");
        setLabelText(lblAttendanceCount, "0");
        setLabelText(lblMedicalCount, "0");
        setLabelText(lblUnreadNoticeCount, "0");
        setLabelText(lblUserId, "User ID: -");
        setLabelText(lblDepartment, "Department: -");
        setLabelText(lblPhone, "Phone: -");
        setLabelText(lblAddress, "Address: -");
        dashboardHomeNodes.addAll(contentArea.getChildren());
    }

    public void setTechnicalOfficerData(String registrationNo) {
        TechnicalOfficerContext.setRegistrationNo(registrationNo);
        setLabelText(lblRegistrationNo, "Registration No: " + registrationNo);
        setLabelText(lblUserId, "User ID: " + registrationNo);
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

    // Load one technical officer sub page into the dashboard content area.
    private void loadContent(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText(null);
            alert.setContentText("Cannot open: " + fxmlPath + "\n" + rootMessage(e));
            alert.showAndWait();
        }
    }

    private void loadOfficerDetails(String registrationNo) {
        try {
            com.example.java_lms_group_01.model.UserManagementRow profile =
                    userProfileRepository.findTechnicalOfficerProfile(registrationNo);
            if (profile == null) {
                return;
            }
            String fullName = (raw(profile.getFirstName()) + " " + raw(profile.getLastName())).trim();
            setLabelText(lblOfficerName, "Name: " + (fullName.isBlank() ? "-" : fullName));
            setLabelText(lblOfficerEmail, "Email: " + safe(profile.getEmail()));
            setLabelText(lblPhone, "Phone: " + safe(profile.getPhoneNumber()));
            setLabelText(lblAddress, "Address: " + safe(profile.getAddress()));
            if (imgProfile != null) {
                ProfileImageUtil.loadImage(imgProfile, profile.getProfileImagePath());
            }
        } catch (SQLException e) {
            showError("Failed to load technical officer details.", e);
        }
    }

    private void loadDashboardCounts() {
        try {
            setLabelText(lblAttendanceCount, String.valueOf(technicalOfficerRepository.countAttendance()));
            setLabelText(lblMedicalCount, String.valueOf(technicalOfficerRepository.countMedical()));
            setLabelText(lblUnreadNoticeCount, String.valueOf(technicalOfficerRepository.countNotices()));
        } catch (SQLException e) {
            setLabelText(lblAttendanceCount, "0");
            setLabelText(lblMedicalCount, "0");
            setLabelText(lblUnreadNoticeCount, "0");
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

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    private String rootMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            return current.getClass().getSimpleName();
        }
        return message;
    }
}
