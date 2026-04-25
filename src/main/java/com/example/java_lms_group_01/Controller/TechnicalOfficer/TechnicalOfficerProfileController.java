package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.model.UserRecord;
import com.example.java_lms_group_01.session.LoggedInTechnicalOfficer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class TechnicalOfficerProfileController {

    @FXML private TextField txtRegistrationNo;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private TextField txtPicturePath;

    private final UserProfileRepository userProfileRepository = new UserProfileRepository();

    @FXML
    public void initialize() {
        // Lock the Registration Number field so it cannot be changed
        txtRegistrationNo.setEditable(false);

        // Load the officer's current data into the text fields
        loadProfileData();
    }

    @FXML
    private void saveProfile() {
        // Get the ID of the person currently logged in
        String regNo = LoggedInTechnicalOfficer.getRegistrationNo();

        if (regNo == null || regNo.isEmpty()) {
            showSimpleAlert(Alert.AlertType.WARNING, "Session Error", "Please login again.");
            return;
        }

        try {
            // Send the current text from the fields to the database repository
            userProfileRepository.updateTechnicalOfficerProfile(
                    regNo,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    txtEmail.getText().trim(),
                    txtPhone.getText().trim(),
                    txtAddress.getText().trim(),
                    txtPicturePath.getText().trim()
            );

            showSimpleAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");

        } catch (SQLException e) {
            showSimpleAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadProfileData() {
        String regNo = LoggedInTechnicalOfficer.getRegistrationNo();

        if (regNo == null || regNo.isEmpty()) {
            return;
        }

        try {
            // Fetch the profile record from the database
            UserRecord profile = userProfileRepository.findTechnicalOfficerProfile(regNo);

            if (profile != null) {
                // Fill the text fields with data from the database record
                txtRegistrationNo.setText(profile.getUserId());

                // Use a simple check to avoid putting "null" text in the boxes
                txtFirstName.setText(profile.getFirstName() != null ? profile.getFirstName() : "");
                txtLastName.setText(profile.getLastName() != null ? profile.getLastName() : "");
                txtEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
                txtPhone.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");
                txtAddress.setText(profile.getAddress() != null ? profile.getAddress() : "");
                txtPicturePath.setText(profile.getProfileImagePath() != null ? profile.getProfileImagePath() : "");
            }

        } catch (SQLException e) {
            showSimpleAlert(Alert.AlertType.ERROR, "Database Error", "Could not load profile.");
        }
    }


    private void showSimpleAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}