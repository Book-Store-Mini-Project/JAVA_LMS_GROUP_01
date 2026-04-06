package com.example.java_lms_group_01.Controller.Lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LecturerProfileController {

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtDepartment;

    @FXML
    public void updateProfile() {

        String name = txtName.getText();
        String email = txtEmail.getText();
        String phone = txtPhone.getText();
        String department = txtDepartment.getText();

        System.out.println("Profile Updated");
        System.out.println(name + " " + email + " " + phone + " " + department);
    }

}