package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class TechnicalOfficerProfileController {

    @FXML
    private TextField txtRegistrationNo;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtPicturePath;

    private final UserProfileRepository userProfileRepository = new UserProfileRepository();

    @FXML
    public void initialize() {
        txtRegistrationNo.setEditable(false);
        loadProfile();
    }

    @FXML
    private void saveProfile() {
        String registrationNo = TechnicalOfficerContext.getRegistrationNo();
        if (registrationNo == null || registrationNo.isBlank()) {
            show(Alert.AlertType.WARNING, "Session Error", "Technical officer session not found. Please login again.");
            return;
        }

        try {
            userProfileRepository.updateTechnicalOfficerProfile(
                    registrationNo,
                    value(txtFirstName),
                    value(txtLastName),
                    value(txtEmail),
                    value(txtPhone),
                    value(txtAddress),
                    value(txtPicturePath)
            );
            show(Alert.AlertType.INFORMATION, "Profile Updated", "Profile details updated successfully.");
        } catch (SQLException e) {
            show(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadProfile() {
        String registrationNo = TechnicalOfficerContext.getRegistrationNo();
        if (registrationNo == null || registrationNo.isBlank()) {
            return;
        }

        try {
            var profile = userProfileRepository.findTechnicalOfficerProfile(registrationNo);
            if (profile == null) {
                return;
            }
            txtRegistrationNo.setText(safe(profile.getUserId()));
            txtFirstName.setText(safe(profile.getFirstName()));
            txtLastName.setText(safe(profile.getLastName()));
            txtEmail.setText(safe(profile.getEmail()));
            txtPhone.setText(safe(profile.getPhoneNumber()));
            txtAddress.setText(safe(profile.getAddress()));
            txtPicturePath.setText(safe(profile.getProfileImagePath()));
        } catch (SQLException e) {
            show(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private String value(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
