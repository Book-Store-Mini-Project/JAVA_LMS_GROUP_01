package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.session.LoggedInStudent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class StudentTimetablePageController {

    @FXML
    private TableView<Timetable> tblTimetable;
    @FXML
    private TableColumn<Timetable, String> colCourseCode;
    @FXML
    private TableColumn<Timetable, String> colDay;
    @FXML
    private TableColumn<Timetable, String> colStartTime;
    @FXML
    private TableColumn<Timetable, String> colEndTime;
    @FXML
    private TableColumn<Timetable, String> colSession;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadTimetable();
    }

    private void setupColumns() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colDay.setCellValueFactory(d -> d.getValue().dayProperty());
        colStartTime.setCellValueFactory(d -> d.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(d -> d.getValue().endTimeProperty());
        colSession.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
    }

    private void loadTimetable() {
        String regNo = currentStudent();
        if (regNo.isBlank()) {
            return;
        }

        try {
            tblTimetable.getItems().setAll(studentRepository.findTimetableByStudent(regNo));
        } catch (SQLException e) {
            showError("Failed to load timetable details.", e);
        }
    }

    private String currentStudent() {
        String reg = LoggedInStudent.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
