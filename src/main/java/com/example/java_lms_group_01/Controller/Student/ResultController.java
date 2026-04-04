package com.example.java_lms_group_01.Controller.Student;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;

public class ResultController {

    @FXML
    private TableView<?> resultTable;

    @FXML
    private TableColumn<?, ?> subjectColumn;

    @FXML
    private TableColumn<?, ?> marksColumn;

    @FXML
    private TableColumn<?, ?> gradeColumn;

    @FXML
    private Label sgpaLabel;

    @FXML
    private Label cgpaLabel;

    @FXML
    public void initialize() {
        System.out.println("Result Table Loaded");
    }
}
