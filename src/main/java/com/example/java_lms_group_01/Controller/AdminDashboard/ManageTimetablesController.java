package com.example.java_lms_group_01.Controller.AdminDashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ManageTimetablesController {

    @FXML
    private ComboBox<?> cmbFilterBatch;

    @FXML
    private ComboBox<?> cmbFilterDay;

    @FXML
    private TableColumn<?, ?> colBatch;

    @FXML
    private TableColumn<?, ?> colCourse;

    @FXML
    private TableColumn<?, ?> colDay;

    @FXML
    private TableColumn<?, ?> colLocation;

    @FXML
    private TableColumn<?, ?> colTime;

    @FXML
    private TableView<?> tblTimetable;

    @FXML
    private TextField txtSearchSchedule;

    @FXML
    void btnOnActionAddNewSchedule(ActionEvent event) {

    }

    @FXML
    void btnOnActionDeleteSchedule(ActionEvent event) {

    }

    @FXML
    void btnOnActionUpdateSchedule(ActionEvent event) {

    }

}
