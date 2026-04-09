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

public class StudentAttendancePageController {

    @FXML
    private Label lblEligibilityRule;
    @FXML
    private TableView<AttendanceEligibilitySummary> tblEligibilitySummary;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colSummaryCourseCode;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colSummaryEligibleSessions;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colSummaryTotalSessions;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colSummaryAttendancePct;
    @FXML
    private TableColumn<AttendanceEligibilitySummary, String> colSummaryEligibility;
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
        lblEligibilityRule.setText("Eligibility rule: minimum 80% attendance in each course");
        colSummaryCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSummaryEligibleSessions.setCellValueFactory(d -> d.getValue().eligibleSessionsProperty());
        colSummaryTotalSessions.setCellValueFactory(d -> d.getValue().totalSessionsProperty());
        colSummaryAttendancePct.setCellValueFactory(d -> d.getValue().attendancePctProperty());
        colSummaryEligibility.setCellValueFactory(d -> d.getValue().eligibilityProperty());
        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceStatus.setCellValueFactory(d -> d.getValue().attendanceStatusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());
        loadAttendanceData();
    }

    private void loadAttendanceData() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var summaryRows = studentRepository.findAttendanceEligibilityByStudent(regNo).stream()
                    .map(r -> new AttendanceEligibilitySummary(
                            r.courseCode(),
                            String.valueOf(r.eligibleSessions()),
                            String.valueOf(r.totalSessions()),
                            AttendanceEligibilityUtil.formatPercentage(r.eligibleSessions(), r.totalSessions()),
                            AttendanceEligibilityUtil.toEligibilityStatus(r.eligibleSessions(), r.totalSessions())
                    ))
                    .toList();
            tblEligibilitySummary.getItems().setAll(summaryRows);

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
