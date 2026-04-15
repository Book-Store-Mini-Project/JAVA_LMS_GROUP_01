package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Eligibility;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
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
    private ComboBox<String> cmbBatch;
    @FXML
    private TextField txtStudentSearch;
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
    private TableColumn<Eligibility, String> colCaMarks;
    @FXML
    private TableColumn<Eligibility, String> colCaThreshold;
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
        colCaMarks.setCellValueFactory(d -> d.getValue().caMarksProperty());
        colCaThreshold.setCellValueFactory(d -> d.getValue().caThresholdProperty());
        colEligibility.setCellValueFactory(d -> d.getValue().eligibilityProperty());

        loadBatchOptions();
        loadEligibility("", "");
    }

    @FXML
    private void submitFilters() {
        loadEligibility(value(txtStudentSearch), selectedBatch());
    }

    private void loadBatchOptions() {
        try {
            List<String> batches = lecturerRepository.findBatchesByLecturer(currentLecturer());
            List<String> options = new ArrayList<>();
            options.add("All Batches");
            options.addAll(batches);
            cmbBatch.getItems().setAll(options);
            cmbBatch.setValue("All Batches");
        } catch (SQLException e) {
            showError("Failed to load batch list.", e);
        }
    }

    private String selectedBatch() {
        String batch = cmbBatch.getValue();
        return batch == null || "All Batches".equals(batch) ? "" : batch.trim();
    }

    private void loadEligibility(String studentReg, String batch) {
        try {
            tblEligibility.getItems().setAll(
                    lecturerRepository.findEligibilityByLecturer(currentLecturer(), studentReg, "", batch)
            );
        } catch (SQLException e) {
            showError("Failed to load undergraduate eligibility.", e);
        }
    }

    private String currentLecturer() {
        String reg = LecturerContext.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
