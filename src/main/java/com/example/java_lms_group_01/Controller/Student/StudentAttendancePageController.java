package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class StudentAttendancePageController {

    @FXML
    private TableView<Attendance> tblAttendance;
    @FXML
    private TableColumn<Attendance, String> colAttendanceId;
    @FXML
    private TableColumn<Attendance, String> colStudentReg;
    @FXML
    private TableColumn<Attendance, String> colCourseCode;
    @FXML
    private TableColumn<Attendance, String> colSubmissionDate;
    @FXML
    private TableColumn<Attendance, String> colSessionType;
    @FXML
    private TableColumn<Attendance, String> colAttendanceStatus;
    @FXML
    private TableColumn<Attendance, String> colTechOfficerReg;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceStatus.setCellValueFactory(d -> d.getValue().attendanceStatusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());
        loadAttendance();
    }

    private void loadAttendance() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var rows = studentRepository.findAttendanceByStudent(regNo).stream()
                    .map(r -> new Attendance(r.attendanceId(), r.studentReg(), r.courseCode(), r.submissionDate(), r.sessionType(), r.attendanceStatus(), r.techOfficerReg()))
                    .toList();
            tblAttendance.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load attendance details.", e);
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
