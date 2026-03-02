package com.example.java_lms_group_01.Controller.RegisterForms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class LectureRegisterFormController {

    @FXML
    private Button btnRegisterLecturer;

    @FXML
    private ComboBox<?> cmbGender;

    @FXML
    private DatePicker datePickerDob;

    @FXML
    private DatePicker datePickerJoining;

    @FXML
    private TextField txtAddress;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtLecDeptId;

    @FXML
    private TextField txtLecPosition;

    @FXML
    private TextField txtPhone;

    @FXML
    void handleRegisterLecturer(ActionEvent event) {

    }

}
