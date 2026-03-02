package com.example.java_lms_group_01.Controller.RegisterForms;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TechnicalOfficerRegisterFormController {

    @FXML
    private Button btnRegisterTO;

    @FXML
    private ComboBox<?> cmbGender;

    @FXML
    private ComboBox<?> cmbTOShift;

    @FXML
    private DatePicker datePickerDob;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFirstName;

    @FXML
    private TextField txtLastName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtTODeptId;

    @FXML
    private TextField txtTOLab;

    @FXML
    private TextField txtTOPosition;

    @FXML
    private TextArea txtTOQualifications;

    @FXML
    void handleRegisterTO(ActionEvent event) {

    }

}
