package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.AttendanceEligibilitySummary;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
    private TableView<AttendanceEligibilitySummary> tblEligibilitySummary;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colSummaryCourseCode;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colEligibleSessions;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colTotalSessions;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colAttendancePct;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colCaMarks;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colCaThreshold;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colEligibility;
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
        colSummaryCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colEligibleSessions.setCellValueFactory(d -> d.getValue().eligibleSessionsProperty());
        colTotalSessions.setCellValueFactory(d -> d.getValue().totalSessionsProperty());
        colAttendancePct.setCellValueFactory(d -> d.getValue().attendancePctProperty());
        colCaMarks.setCellValueFactory(d -> d.getValue().caMarksProperty());
        colCaThreshold.setCellValueFactory(d -> d.getValue().caThresholdProperty());
        colEligibility.setCellValueFactory(d -> d.getValue().eligibilityProperty());

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
                        String.format("%.2f%%", record.getTotalSessions() == 0 ? 0.0 :
                                record.getEligibleSessions() * 100.0 / record.getTotalSessions()),
                        String.format("%.2f", record.getCaMarks()),
                        String.format("%.2f", record.getCaThreshold()),
                        buildEligibilityStatus(record.isAttendanceEligible(), record.isCaEligible())
                ));
            }
            tblEligibilitySummary.getItems().setAll(summaryRows);
            
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

    private String buildEligibilityStatus(boolean attendanceEligible, boolean caEligible) {
        if (attendanceEligible && caEligible) {
            return "Eligible";
        }
        if (!attendanceEligible && !caEligible) {
            return "Attendance + CA Shortage";
        }
        if (!attendanceEligible) {
            return "Attendance Shortage";
        }
        return "CA Shortage";
    }
}
