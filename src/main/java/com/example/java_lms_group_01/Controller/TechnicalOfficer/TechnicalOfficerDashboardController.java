package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.model.UserRecord;
import com.example.java_lms_group_01.session.LoggedInTechnicalOfficer;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

    @FXML private Label lblRegistrationNo, lblOfficerName, lblOfficerEmail;
    @FXML private Label lblAttendanceCount, lblMedicalCount, lblUnreadNoticeCount;
    @FXML private ImageView imgProfile;
    @FXML private AnchorPane contentArea;

    private final UserProfileRepository userProfileRepository = new UserProfileRepository();
    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();
    private final List<Node> dashboardHomeContent = new ArrayList<>();

    @FXML
    public void initialize() {
        // Set default values when the dashboard opens
        lblRegistrationNo.setText("Registration No: -");
        lblOfficerName.setText("Name: -");
        lblOfficerEmail.setText("Email: -");

        lblAttendanceCount.setText("0");
        lblMedicalCount.setText("0");
        lblUnreadNoticeCount.setText("0");

        // Save the first dashboard view so we can bring it back later
        dashboardHomeContent.clear();
        dashboardHomeContent.addAll(contentArea.getChildren());
    }

    // This method is called right after the Technical Officer logs in
    public void setTechnicalOfficerData(String registrationNo) {
        // Save the ID globally
        LoggedInTechnicalOfficer.setRegistrationNo(registrationNo);

        // Display ID on the dashboard
        lblRegistrationNo.setText("Registration No: " + registrationNo);

        // Get name/email/image from database
        loadOfficerProfile(registrationNo);

        // Update the small counter boxes
        updateDashboardStats();
    }

    // NAVIGATION
    @FXML
    private void navDashboard(ActionEvent event) {
        // Put the original dashboard content back
        contentArea.getChildren().setAll(dashboardHomeContent);
        updateDashboardStats();
    }

    @FXML
    private void navAttendance(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_attendance.fxml");
    }

    @FXML
    private void navMedical(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_medical.fxml");
    }

    @FXML
    private void navExamAttendance(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_exam_attendance.fxml");
    }

    @FXML
    private void navProfile(ActionEvent event) {
        loadContent("/view/technicalofficer/technical_officer_profile.fxml");
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
        // Clear login session
        LoggedInTechnicalOfficer.clear();

        try {
            // Switch back to Login Screen
            Parent loginPage = FXMLLoader.load(getClass().getResource("/view/login_page.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("LMS Login");
            stage.show();
        } catch (IOException e) {
            showError("Could not log out", e);
        }
    }

    // Swaps the middle part of the screen with a new FXML page
    private void loadContent(String fxmlPath) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Clear current view and add new one
            contentArea.getChildren().setAll(page);

            // Anchor to all sides so it fits the screen
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

        } catch (IOException e) {
            showError("Cannot open page: " + fxmlPath, e);
        }
    }

    private void loadOfficerProfile(String registrationNo) {
        try {
            UserRecord profile = userProfileRepository.findTechnicalOfficerProfile(registrationNo);

            if (profile != null) {
                String fullName = profile.getFirstName() + " " + profile.getLastName();
                lblOfficerName.setText("Name: " + fullName);
                lblOfficerEmail.setText("Email: " + profile.getEmail());

                // Load the profile picture
                ProfileImageUtil.loadImage(imgProfile, profile.getProfileImagePath());
            }
        } catch (SQLException e) {
            showError("Failed to load profile details", e);
        }
    }

    private void updateDashboardStats() {
        try {
            // Ask repository for counts to show on the main cards
            lblAttendanceCount.setText(String.valueOf(technicalOfficerRepository.countAttendance()));
            lblMedicalCount.setText(String.valueOf(technicalOfficerRepository.countMedical()));
            lblUnreadNoticeCount.setText(String.valueOf(technicalOfficerRepository.countNotices()));
        } catch (SQLException e) {
            // If database fails, just keep them at 0
            lblAttendanceCount.setText("0");
        }
    }

    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
