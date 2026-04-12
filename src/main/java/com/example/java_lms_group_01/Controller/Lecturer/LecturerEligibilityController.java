package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Eligibility;
import com.example.java_lms_group_01.util.AttendanceEligibilityUtil;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows attendance eligibility results for students in the lecturer's courses.
 */
public class LecturerEligibilityController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Eligibility> tblEligibility;
    @FXML
    private TableColumn<Eligibility, String> colStudentReg;
    @FXML
    private TableColumn<Eligibility, String> colStudentName;
    @FXML
    private TableColumn<Eligibility, String> colCourseCode;
    @FXML
    private TableColumn<Eligibility, String> colEligibleSessions;
    @FXML
    private TableColumn<Eligibility, String> colTotalSessions;
    @FXML
    private TableColumn<Eligibility, String> colAttendancePct;
    @FXML
    private TableColumn<Eligibility, String> colEligibility;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colEligibleSessions.setCellValueFactory(d -> d.getValue().eligibleSessionsProperty());
        colTotalSessions.setCellValueFactory(d -> d.getValue().totalSessionsProperty());
        colAttendancePct.setCellValueFactory(d -> d.getValue().attendancePctProperty());
        colEligibility.setCellValueFactory(d -> d.getValue().eligibilityProperty());
        loadEligibility(null);
    }

    @FXML
    private void searchEligibility() {
        loadEligibility(txtSearch.getText());
    }

    private void loadEligibility(String keyword) {
        try {
            List<LecturerRepository.EligibilityRecord> recordList =
                    lecturerRepository.findEligibilityByLecturer(currentLecturer(), keyword);
            List<Eligibility> rows = new ArrayList<>();
            for (LecturerRepository.EligibilityRecord record : recordList) {
                rows.add(new Eligibility(
                        record.getStudentReg(),
                        record.getStudentName(),
                        record.getCourseCode(),
                        String.valueOf(record.getEligibleSessions()),
                        String.valueOf(record.getTotalSessions()),
                        AttendanceEligibilityUtil.formatPercentage(record.getEligibleSessions(), record.getTotalSessions()),
                        AttendanceEligibilityUtil.toEligibilityStatus(record.getEligibleSessions(), record.getTotalSessions())
                ));
            }
            tblEligibility.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load undergraduate eligibility.", e);
        }
    }

    private String currentLecturer() {
        String reg = LecturerContext.getRegistrationNo();
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
