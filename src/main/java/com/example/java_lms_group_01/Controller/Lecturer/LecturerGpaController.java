package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Performance;
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
 * Shows calculated performance, grades, GPA, and SGPA for students.
 */
public class LecturerGpaController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Performance> tblPerformance;
    @FXML
    private TableColumn<Performance, String> colStudentReg;
    @FXML
    private TableColumn<Performance, String> colStudentName;
    @FXML
    private TableColumn<Performance, String> colCourseCode;
    @FXML
    private TableColumn<Performance, String> colCaMarks;
    @FXML
    private TableColumn<Performance, String> colEndMarks;
    @FXML
    private TableColumn<Performance, String> colTotalMarks;
    @FXML
    private TableColumn<Performance, String> colGrade;
    @FXML
    private TableColumn<Performance, String> colGpa;
    @FXML
    private TableColumn<Performance, String> colSgpa;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colCaMarks.setCellValueFactory(d -> d.getValue().caMarksProperty());
        colEndMarks.setCellValueFactory(d -> d.getValue().endMarksProperty());
        colTotalMarks.setCellValueFactory(d -> d.getValue().totalMarksProperty());
        colGrade.setCellValueFactory(d -> d.getValue().gradeProperty());
        colGpa.setCellValueFactory(d -> d.getValue().gpaProperty());
        colSgpa.setCellValueFactory(d -> d.getValue().sgpaProperty());
        loadPerformance(null);
    }

    @FXML
    private void searchPerformance() {
        loadPerformance(txtSearch.getText());
    }

    private void loadPerformance(String keyword) {
        try {
            List<LecturerRepository.PerformanceRecord> recordList =
                    lecturerRepository.findPerformanceByLecturer(currentLecturer(), keyword);
            List<Performance> rows = new ArrayList<>();
            for (LecturerRepository.PerformanceRecord record : recordList) {
                rows.add(new Performance(
                        record.getStudentReg(),
                        record.getStudentName(),
                        record.getCourseCode(),
                        String.format("%.2f", record.getCaMarks()),
                        String.format("%.2f", record.getEndMarks()),
                        String.format("%.2f", record.getTotalMarks()),
                        record.getPublishedGrade(),
                        record.getGpa() == null ? "" : String.format("%.2f", record.getGpa()),
                        record.getSgpa() == null ? "" : String.format("%.2f", record.getSgpa())
                ));
            }
            tblPerformance.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load marks/grades/GPA.", e);
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
