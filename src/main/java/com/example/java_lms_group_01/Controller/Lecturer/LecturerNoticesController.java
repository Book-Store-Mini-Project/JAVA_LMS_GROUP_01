package com.example.java_lms_group_01.Controller.Lecturer;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class LecturerNoticesController {

    @FXML
    private ListView noticeList;

    @FXML
    public void initialize() {

        noticeList.getItems().add("Exam will start next week");
        noticeList.getItems().add("Assignment submission deadline Friday");
        noticeList.getItems().add("Holiday on Monday");

    }

}