package com.example.java_lms_group_01.Controller.LandingPages;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class RoleLandingController {

    @FXML
    private Label lblRole;

    @FXML
    private Label lblRegistrationNo;

    public void setLandingData(String role, String registrationNo) {
        lblRole.setText(role + " Landing Page");
        lblRegistrationNo.setText("Registration No: " + registrationNo);
    }
}
