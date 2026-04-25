package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Repository.UserImageRepository;
import com.example.java_lms_group_01.session.LoggedInAdmin;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AdminDashboard {

    @FXML
    private AnchorPane contentArea;
    @FXML
    private Label lblAdminRegistrationNo;
    @FXML
    private ImageView imgProfile;

    private final UserImageRepository userImageRepository = new UserImageRepository();

    @FXML
    public void initialize() {
        lblAdminRegistrationNo.setText("Registration No: -");
    }

    public void setAdminData(String registrationNumber) {
        LoggedInAdmin.setRegistrationNo(registrationNumber);
        lblAdminRegistrationNo.setText("Registration No: " + registrationNumber);
        loadProfileImage(registrationNumber);
    }

    @FXML
    void btnOnActionLogout(ActionEvent event) {
        LoggedInAdmin.clear();
        openLoginPage(event);
    }

    @FXML
    void navCourses(ActionEvent event) {
        loadSubView("/view/Admin/manage_course.fxml");
    }

    @FXML
    void navNotices(ActionEvent event) {
        loadSubView("/view/Admin/manage_notices.fxml");
    }

    @FXML
    void navEnrollments(ActionEvent event) {
        loadSubView("/view/Admin/admin_enrollments.fxml");
    }

    @FXML
    void navTimetable(ActionEvent event) {
        loadSubView("/view/Admin/manage_timetables.fxml");
    }

    @FXML
    void navUsers(ActionEvent event) {
        loadSubView("/view/Admin/manage_users.fxml");
    }

    // Load one admin screen inside the dashboard area.
    private void loadSubView(String path) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(path));
            contentArea.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            showError("Failed to load: " + path, e);
        }
    }

    // Send the user back to the login page.
    private void openLoginPage(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login_page.fxml"));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Page");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Scene switch failed.", e);
        }
    }

    // Try to load the profile image. If it fails, show nothing.
    private void loadProfileImage(String regNo) {
        try {
            String path = userImageRepository.findImagePathByUserId(regNo);
            ProfileImageUtil.loadImage(imgProfile, path);
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
