package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Performance;
import com.example.java_lms_group_01.model.summary.UndergraduateSummary;
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
 * Shows calculated performance, grades, GPA, and SGPA for students.
 */
public class LecturerGpaController {

    @FXML
    private ComboBox<String> cmbBatch;
    @FXML
    private TextField txtStudentSearch;
    @FXML
    private TableView<Performance> tblFinalMarks;
    @FXML
    private TableColumn<Performance, String> colFinalStudentReg;
    @FXML
    private TableColumn<Performance, String> colFinalStudentName;
    @FXML
    private TableColumn<Performance, String> colFinalCourseCode;
    @FXML
    private TableColumn<Performance, String> colFinalCourseName;
    @FXML
    private TableColumn<Performance, String> colFinalMarks;
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
        colFinalStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colFinalStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colFinalCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colFinalCourseName.setCellValueFactory(d -> d.getValue().courseNameProperty());
        colFinalMarks.setCellValueFactory(d -> d.getValue().finalMarksProperty());

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

        colSummaryStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colSummaryStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colSummarySgpa.setCellValueFactory(d -> d.getValue().sgpaProperty());
        colSummaryCgpa.setCellValueFactory(d -> d.getValue().cgpaProperty());

        loadBatchOptions();
        loadReports("", "");
    }

    @FXML
    private void submitFilters() {
        loadReports(value(txtStudentSearch), selectedBatch());
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

    private void loadReports(String studentReg, String batch) {
        try {
            List<Performance> performanceRows =
                    lecturerRepository.findPerformanceByLecturer(currentLecturer(), studentReg, "", batch);
            List<UndergraduateSummary> summaryRows =
                    lecturerRepository.findUndergraduateSummariesByLecturer(currentLecturer(), studentReg, "", batch);

            tblFinalMarks.getItems().setAll(performanceRows);
            tblPerformance.getItems().setAll(performanceRows);
            tblUndergraduateSummary.getItems().setAll(summaryRows);
        } catch (SQLException e) {
            showError("Failed to load marks/grades/GPA.", e);
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
