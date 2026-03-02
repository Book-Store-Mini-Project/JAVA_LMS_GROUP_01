package com.example.java_lms_group_01.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LandingController {

    @FXML
    private TextField loginEmail;

    @FXML
    private PasswordField loginPass;

    @FXML
    void btnOnActionLogin(ActionEvent event) {
        // Login logic here
    }

    @FXML
    void btnOnActionAdmin(ActionEvent event) {
        loadForm("/view/RegisterForms/admin.fxml","admin Register");
    }

    @FXML
    void btnOnActionLecturer(ActionEvent event) {
        loadForm("/view/RegisterForms/lecturer.fxml","Lecturer Register");
    }

    @FXML
    void btnOnActionStudent(ActionEvent event) {
        loadForm("/view/RegisterForms/student.fxml","Student Register");
    }

    @FXML
    void btnOnActionTechnicalOfficer(ActionEvent event) {
        loadForm("/view/RegisterForms/technical_officer.fxml","Technical Officer Register");
    }

    private void loadForm(String path, String title) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(path)
            );

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.show();   // Opens new window
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}