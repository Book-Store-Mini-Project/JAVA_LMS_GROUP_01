package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LecturerProfileController {

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
    private TextField txtDepartment;
    @FXML
    private TextField txtPosition;
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
        String regNo = LecturerContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            show(Alert.AlertType.WARNING, "Session Error", "Lecturer session not found. Please login again.");
            return;
        }

        try {
            userProfileRepository.updateLecturerProfile(
                    regNo,
                    value(txtFirstName),
                    value(txtLastName),
                    value(txtEmail),
                    value(txtAddress),
                    value(txtPhone),
                    value(txtDepartment),
                    value(txtPosition),
                    value(txtPicturePath)
            );
            show(Alert.AlertType.INFORMATION, "Profile Updated", "Lecturer profile updated successfully.");
        } catch (Exception e) {
            show(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadProfile() {
        String regNo = LecturerContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var profile = userProfileRepository.findLecturerProfile(regNo);
            if (profile == null) {
                return;
            }
            txtRegistrationNo.setText(safe(profile.getUserId()));
            txtFirstName.setText(safe(profile.getFirstName()));
            txtLastName.setText(safe(profile.getLastName()));
            txtEmail.setText(safe(profile.getEmail()));
            txtAddress.setText(safe(profile.getAddress()));
            txtPhone.setText(safe(profile.getPhoneNumber()));
            txtDepartment.setText(safe(profile.getDepartment()));
            txtPosition.setText(safe(profile.getPosition()));
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
