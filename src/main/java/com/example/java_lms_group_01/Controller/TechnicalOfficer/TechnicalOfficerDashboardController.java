package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import com.example.java_lms_group_01.util.LoggedInTechnicalOfficer;
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


    //UI Components

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
    private ImageView imgProfile;

    @FXML
    private AnchorPane contentArea;

    //save dashboard home ui
    private final List<javafx.scene.Node> dashboardHomeNodes = new ArrayList<>();
   //connect db
    private final UserProfileRepository userProfileRepository = new UserProfileRepository();
    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();

    //when open run ui
    @FXML
    public void initialize() {
        setLabelText(lblRegistrationNo, "Registration No: -");
        setLabelText(lblOfficerName, "Name: -");
        setLabelText(lblOfficerEmail, "Email: -");
        setLabelText(lblAttendanceCount, "0");
        setLabelText(lblMedicalCount, "0");
        setLabelText(lblUnreadNoticeCount, "0");
        dashboardHomeNodes.addAll(contentArea.getChildren());
    }

    //logged user sv,update label ,load officer details and count dash board ...
    public void setTechnicalOfficerData(String registrationNo) {
        LoggedInTechnicalOfficer.setRegistrationNo(registrationNo);
        setLabelText(lblRegistrationNo, "Registration No: " + registrationNo);
        loadOfficerDetails(registrationNo);
        loadDashboardCounts();
    }

    //dashboard
    @FXML
    private void navDashboard(ActionEvent event) {
        contentArea.getChildren().setAll(dashboardHomeNodes);
        loadDashboardCounts();
    }
    //attendance page
    @FXML
    private void navAttendance(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_attendance.fxml");
    }

    //profile page
    @FXML
    private void navProfile(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_profile.fxml");
    }

    //medical page
    @FXML
    private void navMedical(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_medical.fxml");
    }

    //exam attendance page
    @FXML
    private void navExamAttendance(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_exam_attendance.fxml");
    }

    //notice
    @FXML
    private void navNotices(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_notices.fxml");
    }

    //time table
    @FXML
    private void navTimetables(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_timetable.fxml");
    }

    //logout user clear and load login page
    @FXML
    private void logout(ActionEvent event) {
        LoggedInTechnicalOfficer.clear();
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
            com.example.java_lms_group_01.model.UserRecord profile =
                    userProfileRepository.findTechnicalOfficerProfile(registrationNo);
            if (profile == null) {
                return;
            }
            String fullName = (raw(profile.getFirstName()) + " " + raw(profile.getLastName())).trim();
            setLabelText(lblOfficerName, "Name: " + (fullName.isBlank() ? "-" : fullName));
            setLabelText(lblOfficerEmail, "Email: " + safe(profile.getEmail()));
            if (imgProfile != null) {
                ProfileImageUtil.loadImage(imgProfile, profile.getProfileImagePath());
            }
        } catch (SQLException e) {
            showError("Failed to load technical officer details.", e);
        }
    }

    //attendance count ,medical count,notice count
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

    //helper method
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
