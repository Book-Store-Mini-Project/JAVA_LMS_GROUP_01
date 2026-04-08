package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class StudentTimetablePageController {

    @FXML
    private TableView<Timetable> tblTimetable;
    @FXML
    private TableColumn<Timetable, String> colTimetableId;
    @FXML
    private TableColumn<Timetable, String> colDepartment;
    @FXML
    private TableColumn<Timetable, String> colLecId;
    @FXML
    private TableColumn<Timetable, String> colCourseCode;
    @FXML
    private TableColumn<Timetable, String> colAdminId;
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
        colTimetableId.setCellValueFactory(d -> d.getValue().timetableIdProperty());
        colDepartment.setCellValueFactory(d -> d.getValue().departmentProperty());
        colLecId.setCellValueFactory(d -> d.getValue().lecIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colAdminId.setCellValueFactory(d -> d.getValue().adminIdProperty());
        colDay.setCellValueFactory(d -> d.getValue().dayProperty());
        colStartTime.setCellValueFactory(d -> d.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(d -> d.getValue().endTimeProperty());
        colSession.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        loadTimetable();
    }

    private void loadTimetable() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var rows = studentRepository.findTimetableByStudent(regNo).stream()
                    .map(r -> new Timetable(r.timetableId(), r.department(), r.lecId(), r.courseCode(), r.adminId(),
                            r.day(), parseTime(r.startTime()), parseTime(r.endTime()), r.sessionType()))
                    .toList();
            tblTimetable.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load timetable details.", e);
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    private java.time.LocalTime parseTime(String value) {
        return value == null || value.isBlank() ? null : java.time.LocalTime.parse(value);
    }
}
