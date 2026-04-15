package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.Eligibility;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

/**
 * Shows a student's attendance details and attendance eligibility summary.
 */
public class StudentAttendancePageController {

    @FXML
    private TableView<Eligibility> tblEligibilitySummary;
    @FXML
    private TableColumn<Eligibility, String> colSummaryCourseCode;
    @FXML
    private TableColumn<Eligibility, String> colEligibleSessions;
    @FXML
    private TableColumn<Eligibility, String> colTotalSessions;
    @FXML
    private TableColumn<Eligibility, String> colAttendancePct;
    @FXML
    private TableColumn<Eligibility, String> colCaMarks;
    @FXML
    private TableColumn<Eligibility, String> colCaThreshold;
    @FXML
    private TableColumn<Eligibility, String> colEligibility;
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
            tblEligibilitySummary.getItems().setAll(
                    studentRepository.findAttendanceEligibilityByStudent(regNo)
            );
            tblAttendance.getItems().setAll(
                    studentRepository.findAttendanceByStudent(regNo)
            );
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
