package com.example.java_lms_group_01.Controller;

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private JFXTextField email;

    @FXML
    private JFXTextField password;

    @FXML
    void btnOnActionSubmit(ActionEvent event) {
        System.out.println("click");
    }

}
