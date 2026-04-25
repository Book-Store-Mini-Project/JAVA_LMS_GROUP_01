package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.UserProfileRepository;
import com.example.java_lms_group_01.model.UserRecord;
import com.example.java_lms_group_01.model.users.Student;
import com.example.java_lms_group_01.session.LoggedInStudent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class StudentProfilePageController {

    @FXML private TextField txtRegistrationNo, txtName, txtEmail, txtPhone, txtAddress, txtPicturePath;

    @FXML private TextField txtDepartment, txtGpa, txtStatus;

    @FXML private PasswordField txtCurrentPassword, txtNewPassword, txtConfirmPassword;

    private final UserProfileRepository userProfileRepository = new UserProfileRepository();
    private Student currentStudent;

    @FXML
    public void initialize() {
        // Make certain fields non-editable (Normal way to lock fields)
        txtRegistrationNo.setEditable(false);
        txtName.setEditable(false);
        txtDepartment.setEditable(false);
        txtGpa.setEditable(false);
        txtStatus.setEditable(false);

        // Load the user's current data from the database
        loadProfileData();
    }

    @FXML
    private void saveProfile() {
        // Get the current student's ID
        String regNo = getLoggedStudentId();

        if (regNo.equals("") || currentStudent == null) {
            showSimpleAlert(Alert.AlertType.WARNING, "Error", "Student session not found.");
            return;
        }

        // Update our Student object with the new text from the fields
        currentStudent.setEmail(txtEmail.getText().trim());
        currentStudent.setPhoneNumber(txtPhone.getText().trim());
        currentStudent.setAddress(txtAddress.getText().trim());

        // Get the passwords
        String currentPass = txtCurrentPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        try {
            // Check if user is trying to change password
            if (!currentPass.isEmpty() || !newPass.isEmpty()) {
                // Perform basic password checks
                if (newPass.equals(confirmPass)) {
                    if (newPass.equals(currentPass)) {
                        showSimpleAlert(Alert.AlertType.WARNING, "Warning", "New password must be different!");
                        return;
                    }
                } else {
                    showSimpleAlert(Alert.AlertType.WARNING, "Warning", "Passwords do not match!");
                    return;
                }
            }

            // Send updated data to the Repository
            userProfileRepository.updateStudentProfile(
                    currentStudent.getRegistrationNo(),
                    currentStudent.getEmail(),
                    currentStudent.getPhoneNumber(),
                    currentStudent.getAddress(),
                    txtPicturePath.getText().trim(),
                    currentPass,
                    newPass
            );

            // Success! Clear passwords and show a message
            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();

            showSimpleAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully.");

        } catch (SQLException e) {
            showSimpleAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void loadProfileData() {
        String regNo = getLoggedStudentId();
        if (regNo.equals("")) {
            return;
        }

        try {
            // Fetch profile data using the ID
            UserRecord profile = userProfileRepository.findStudentProfile(regNo);

            if (profile != null) {
                // Map database record to our Student model
                currentStudent = new Student();
                currentStudent.setRegistrationNo(profile.getUserId());
                currentStudent.setFirstName(profile.getFirstName());
                currentStudent.setLastName(profile.getLastName());
                currentStudent.setEmail(profile.getEmail());
                currentStudent.setPhoneNumber(profile.getPhoneNumber());
                currentStudent.setAddress(profile.getAddress());

                // Set values to the UI TextFields
                txtRegistrationNo.setText(profile.getUserId());
                txtName.setText(profile.getFirstName() + " " + profile.getLastName());
                txtEmail.setText(profile.getEmail());
                txtPhone.setText(profile.getPhoneNumber());
                txtAddress.setText(profile.getAddress());
                txtDepartment.setText(profile.getDepartment());
                txtStatus.setText(profile.getStatus());
                txtPicturePath.setText(profile.getProfileImagePath());

                // Format GPA to 2 decimal places
                if (profile.getGpa() != null) {
                    txtGpa.setText(String.format("%.2f", profile.getGpa()));
                } else {
                    txtGpa.setText("0.00");
                }
            }
        } catch (SQLException e) {
            showSimpleAlert(Alert.AlertType.ERROR, "Database Error", "Could not load profile.");
        }
    }

    private String getLoggedStudentId() {
        String reg = LoggedInStudent.getRegistrationNo();
        if (reg == null) {
            return "";
        } else {
            return reg.trim();
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