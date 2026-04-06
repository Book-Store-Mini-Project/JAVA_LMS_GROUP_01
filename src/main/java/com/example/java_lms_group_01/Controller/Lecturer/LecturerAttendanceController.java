package com.example.java_lms_group_01.Controller.Lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class LecturerAttendanceController {

    @FXML
    private TableView attendanceTable;

    @FXML
    public void initialize() {

        System.out.println("Attendance Records Loaded");

    }

}