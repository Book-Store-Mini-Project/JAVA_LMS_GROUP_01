package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows timetable entries for the logged-in student's department.
 */
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
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
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
            List<StudentRepository.TimetableRecord> recordList = studentRepository.findTimetableByStudent(regNo);
            List<Timetable> rows = new ArrayList<>();
            for (StudentRepository.TimetableRecord record : recordList) {
                rows.add(new Timetable(
                        record.getTimetableId(),
                        record.getDepartment(),
                        record.getLecId(),
                        record.getCourseCode(),
                        record.getAdminId(),
                        record.getDay(),
                        parseTime(record.getStartTime()),
                        parseTime(record.getEndTime()),
                        record.getSessionType()
                ));
            }
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
