package com.example.java_lms_group_01.Controller.Lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class LecturerMaterialsController {

    @FXML
    private TextField txtCourse;

    @FXML
    private TextField txtMaterial;

    @FXML
    private TableView materialsTable;

    @FXML
    public void uploadMaterial() {

        String course = txtCourse.getText();
        String material = txtMaterial.getText();

        System.out.println("Material Uploaded for course: " + course);
        System.out.println("Material Title: " + material);
    }

}