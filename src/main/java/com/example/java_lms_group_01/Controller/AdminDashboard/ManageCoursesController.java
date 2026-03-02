package com.example.java_lms_group_01.Controller.AdminDashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ManageCoursesController {

    @FXML
    private ComboBox<?> cmbDeptFilter;

    @FXML
    private TableColumn<?, ?> colCourseId;

    @FXML
    private TableColumn<?, ?> colCourseName;

    @FXML
    private TableColumn<?, ?> colCredits;

    @FXML
    private TableColumn<?, ?> colDeptId;

    @FXML
    private TableView<?> tblCourses;

    @FXML
    private TextField txtSearchCourse;

    @FXML
    void btnOnActionAddNewCourse(ActionEvent event) {

    }

    @FXML
    void btnOnActionDeleteCourse(ActionEvent event) {

    }

    @FXML
    void btnOnActionUpdateCourse(ActionEvent event) {

    }

}
