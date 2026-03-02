package com.example.java_lms_group_01.Controller.RegisterForms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class StudentRegisterFormController {

    @FXML
    private Button btnRegisterStudent;

    @FXML
    private ComboBox<?> cmbGender;

    @FXML
    private ComboBox<?> cmbStuStatus;

    @FXML
    private DatePicker datePickerDob;

    @FXML
    private TextField txtAddress;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtStuBatchId;

    @FXML
    private TextField txtStuDeptId;

    @FXML
    private TextField txtStuRegNo;

    @FXML
    void handleRegisterStudent(ActionEvent event) {

    }

}
