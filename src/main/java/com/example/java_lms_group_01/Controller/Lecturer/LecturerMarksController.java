package com.example.java_lms_group_01.Controller.Lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class LecturerMarksController {

    @FXML
    private TextField txtStudentID;

    @FXML
    private TextField txtCourseID;

    @FXML
    private TextField txtMarks;

    @FXML
    private TableView marksTable;

    @FXML
    public void uploadMarks() {

        String studentID = txtStudentID.getText();
        String courseID = txtCourseID.getText();
        String marks = txtMarks.getText();

        System.out.println("Marks Uploaded");
        System.out.println(studentID + " " + courseID + " " + marks);
    }

}