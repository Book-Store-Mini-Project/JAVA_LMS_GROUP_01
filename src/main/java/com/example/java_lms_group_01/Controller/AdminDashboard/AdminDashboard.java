package com.example.java_lms_group_01.Controller.AdminDashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

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
    void btnOnActionLogout(ActionEvent event) {

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

}
