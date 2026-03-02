package com.example.java_lms_group_01.Controller.RegisterForms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AdminRegisterFormController {

    @FXML
    private Button btnRegisterAdmin;

    @FXML
    private ComboBox<?> cmbAccessLevel;

    @FXML
    private ComboBox<?> cmbGender;

    @FXML
    private DatePicker datePickerAppointment;

    @FXML
    private DatePicker datePickerDob;

    @FXML
    private TextField txtAddress;

    @FXML
    private TextField txtDeptId;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtPhone;

    @FXML
    void handleRegisterAdmin(ActionEvent event) {

    }

}
