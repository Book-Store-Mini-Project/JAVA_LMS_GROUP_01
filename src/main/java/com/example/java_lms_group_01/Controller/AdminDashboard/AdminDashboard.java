package com.example.java_lms_group_01.Controller.AdminDashboard;

import com.example.java_lms_group_01.Repository.UserImageRepository;
import com.example.java_lms_group_01.util.ProfileImageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDashboard {

    @FXML
    private Button btnCourses;

    @FXML
    private Button btnNotices;

    @FXML
    private Button btnTimetable;

    @FXML
    private Button btnUsers;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblAdminRegistrationNo;
    @FXML
    private ImageView imgProfile;
    private final UserImageRepository userImageRepository = new UserImageRepository();

    public void setAdminData(String registrationNo) {
        if (lblAdminRegistrationNo != null) {
            lblAdminRegistrationNo.setText("Registration No: " + registrationNo);
        }
        loadProfileImage(registrationNo);
    }

    @FXML
    void btnOnActionLogout(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_page.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setTitle("Login Page");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Navigation Error");
            error.setHeaderText(null);
            error.setContentText("Unable to load the login page.");
            error.showAndWait();
        }
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
    void navTimetable(ActionEvent event) {
        loadSubView("/view/Admin/manage_timetables.fxml");
    }

    @FXML
    void navUsers(ActionEvent event) {
        loadSubView("/view/Admin/manage_users.fxml");

    }

    private void loadSubView(String fxmlPath) {
        try {
            // 1. Get the URL starting from the root of the resources
            URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                System.err.println("CRITICAL: FXML not found at path: " + fxmlPath);
                return;
            }

            // 2. Load the FXML
            FXMLLoader loader = new FXMLLoader(resource);
            Parent node = loader.load();

            // 3. Clear and Inject into your AnchorPane (right side)
            contentArea.getChildren().setAll(node);

            // 4. Anchor it so it fits the whole area
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProfileImage(String registrationNo) {
        try {
            ProfileImageUtil.loadImage(imgProfile, userImageRepository.findImagePathByUserId(registrationNo));
        } catch (SQLException ignored) {
            ProfileImageUtil.loadImage(imgProfile, null);
        }
    }

}
