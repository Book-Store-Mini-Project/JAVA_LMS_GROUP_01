package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.AttendanceEligibilitySummary;
import com.example.java_lms_group_01.util.AttendanceEligibilityUtil;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a student's attendance details and attendance eligibility summary.
 */
public class StudentAttendancePageController {

    @FXML
    private TableView<Attendance> tblAttendance;
    @FXML
    private TableColumn<Attendance, String> colCourseCode;
    @FXML
    private TableColumn<Attendance, String> colSubmissionDate;
    @FXML
    private TableColumn<Attendance, String> colSessionType;
    @FXML
    private TableColumn<Attendance, String> colAttendanceStatus;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceStatus.setCellValueFactory(d -> d.getValue().attendanceStatusProperty());
        loadAttendanceData();
    }

    private void loadAttendanceData() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            List<StudentRepository.AttendanceEligibilityRecord> summaryRecordList =
                    studentRepository.findAttendanceEligibilityByStudent(regNo);
            List<AttendanceEligibilitySummary> summaryRows = new ArrayList<>();
            for (StudentRepository.AttendanceEligibilityRecord record : summaryRecordList) {
                summaryRows.add(new AttendanceEligibilitySummary(
                        record.getCourseCode(),
                        String.valueOf(record.getEligibleSessions()),
                        String.valueOf(record.getTotalSessions()),
                        AttendanceEligibilityUtil.formatPercentage(record.getEligibleSessions(), record.getTotalSessions()),
                        AttendanceEligibilityUtil.toEligibilityStatus(record.getEligibleSessions(), record.getTotalSessions())
                ));
            }
            
            List<StudentRepository.AttendanceRecord> attendanceRecordList =
                    studentRepository.findAttendanceByStudent(regNo);
            List<Attendance> rows = new ArrayList<>();
            for (StudentRepository.AttendanceRecord record : attendanceRecordList) {
                rows.add(new Attendance(
                        record.getAttendanceId(),
                        record.getStudentReg(),
                        record.getCourseCode(),
                        record.getSubmissionDate(),
                        record.getSessionType(),
                        record.getAttendanceStatus(),
                        record.getTechOfficerReg()
                ));
            }
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
