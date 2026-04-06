package com.example.java_lms_group_01.Controller.Lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LecturerDashboardController {

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnMaterials;

    @FXML
    private Button btnMarks;

    @FXML
    private Button btnStudents;

    @FXML
    private Button btnEligibility;

    @FXML
    private Button btnGPA;

    @FXML
    private Button btnAttendance;

    @FXML
    private Button btnNotices;

    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {

        btnProfile.setOnAction(e -> openProfile());
        btnMaterials.setOnAction(e -> openMaterials());
        btnMarks.setOnAction(e -> openMarks());
        btnStudents.setOnAction(e -> openStudents());
        btnAttendance.setOnAction(e -> openAttendance());
        btnNotices.setOnAction(e -> openNotices());
        btnLogout.setOnAction(e -> logout());

    }

    private void openProfile() {
        System.out.println("Open Lecturer Profile Page");
    }

    private void openMaterials() {
        System.out.println("Open Course Materials Page");
    }

    private void openMarks() {
        System.out.println("Open Upload Marks Page");
    }

    private void openStudents() {
        System.out.println("Open Student Details Page");
    }

    private void openAttendance() {
        System.out.println("Open Attendance Page");
    }

    private void openNotices() {
        System.out.println("Open Notices Page");
    }

    private void logout() {
        System.out.println("Logout");
    }

}