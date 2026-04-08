package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
import com.example.java_lms_group_01.util.UserImageRepository;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        String sql = """
                SELECT u.firstName, u.lastName, u.email, u.phoneNumber, u.address
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
                    String firstName = raw(rs.getString("firstName"));
                    String lastName = raw(rs.getString("lastName"));
                    String fullName = (firstName + " " + lastName).trim();
                    lblOfficerName.setText("Name: " + (fullName.isBlank() ? "-" : fullName));
                    lblOfficerEmail.setText("Email: " + safe(rs.getString("email")));
                    lblPhone.setText("Phone: " + safe(rs.getString("phoneNumber")));
                    lblAddress.setText("Address: " + safe(rs.getString("address")));
                    ProfileImageUtil.loadImage(imgProfile, UserImageRepository.findImagePathByUserId(connection, registrationNo));
                }
            }
        } catch (SQLException e) {
            showError("Failed to load technical officer details.", e);
        }
    }

    private void loadDashboardCounts() {
        lblAttendanceCount.setText(String.valueOf(fetchCount("SELECT COUNT(*) FROM attendance")));
        lblMedicalCount.setText(String.valueOf(fetchCount("SELECT COUNT(*) FROM medical")));
        lblUnreadNoticeCount.setText(String.valueOf(fetchCount("SELECT COUNT(*) FROM notice")));
    }

    private int fetchCount(String sql) {
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ignored) {
            return 0;
        }
        return 0;
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
