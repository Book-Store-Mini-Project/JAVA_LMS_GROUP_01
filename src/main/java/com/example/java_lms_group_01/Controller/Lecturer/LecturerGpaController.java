package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Performance;
import com.example.java_lms_group_01.model.summary.UndergraduateSummary;
import com.example.java_lms_group_01.session.LoggedInLecture;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerGpaController {

    @FXML
    private ComboBox<String> cmbBatch;
    @FXML
    private TextField txtStudentSearch;
    @FXML
    private TableView<Performance> tblPerformance;
    @FXML
    private TableColumn<Performance, String> colStudentReg;
    @FXML
    private TableColumn<Performance, String> colStudentName;
    @FXML
    private TableColumn<Performance, String> colCourseCode;
    @FXML
    private TableColumn<Performance, String> colCourseName;
    @FXML
    private TableColumn<Performance, String> colCaMarks;
    @FXML
    private TableColumn<Performance, String> colResultFinalMarks;
    @FXML
    private TableColumn<Performance, String> colTotalMarks;
    @FXML
    private TableColumn<Performance, String> colGrade;
    @FXML
    private TableColumn<Performance, String> colSgpa;
    @FXML
    private TableColumn<Performance, String> colCgpa;
    @FXML
    private TableView<UndergraduateSummary> tblUndergraduateSummary;
    @FXML
    private TableColumn<UndergraduateSummary, String> colSummaryStudentReg;
    @FXML
    private TableColumn<UndergraduateSummary, String> colSummaryStudentName;
    @FXML
    private TableColumn<UndergraduateSummary, String> colSummarySgpa;
    @FXML
    private TableColumn<UndergraduateSummary, String> colSummaryCgpa;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        setupPerformanceColumns();
        setupSummaryColumns();
        loadBatchOptions();
        loadReports("", "");
    }

    // Set up the marks table.
    private void setupPerformanceColumns() {
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colCourseName.setCellValueFactory(d -> d.getValue().courseNameProperty());
        colCaMarks.setCellValueFactory(d -> d.getValue().caMarksProperty());
        colResultFinalMarks.setCellValueFactory(d -> d.getValue().finalMarksProperty());
        colTotalMarks.setCellValueFactory(d -> d.getValue().totalMarksProperty());
        colGrade.setCellValueFactory(d -> d.getValue().gradeProperty());
        colSgpa.setCellValueFactory(d -> d.getValue().sgpaProperty());
        colCgpa.setCellValueFactory(d -> d.getValue().cgpaProperty());
    }

    // Set up the summary table.
    private void setupSummaryColumns() {
        colSummaryStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colSummaryStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colSummarySgpa.setCellValueFactory(d -> d.getValue().sgpaProperty());
        colSummaryCgpa.setCellValueFactory(d -> d.getValue().cgpaProperty());
    }

    @FXML
    private void submitFilters() {
        loadReports(text(txtStudentSearch), selectedBatch());
    }

    // Load the batch list for the logged-in lecturer.
    private void loadBatchOptions() {
        try {
            List<String> batches = lecturerRepository.findBatchesByLecturer(currentLecturer());
            List<String> options = new ArrayList<>();
            options.add("All Batches");
            if (batches != null) {
                options.addAll(batches);
            }
            cmbBatch.getItems().setAll(options);
            cmbBatch.setValue("All Batches");
        } catch (SQLException e) {
            showError("Failed to load batch list.", e);
        }
    }

    private String selectedBatch() {
        String batch = cmbBatch.getValue();
        if (batch == null || "All Batches".equals(batch)) {
            return "";
        }
        return batch.trim();
    }

    // Load both reports using the same search and batch filter.
    private void loadReports(String studentReg, String batch) {
        try {
            String lecturerId = currentLecturer();
            tblPerformance.getItems().setAll(
                    lecturerRepository.findPerformanceByLecturer(lecturerId, studentReg, "", batch)
            );
            tblUndergraduateSummary.getItems().setAll(
                    lecturerRepository.findUndergraduateSummariesByLecturer(lecturerId, studentReg, "", batch)
            );
        } catch (SQLException e) {
            showError("Failed to load marks/grades/GPA.", e);
        }
    }

    private String currentLecturer() {
        String reg = LoggedInLecture.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
