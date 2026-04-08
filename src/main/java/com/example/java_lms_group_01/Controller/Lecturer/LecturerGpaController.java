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
    private TableColumn<Performance, String> colTotalMarks;
    @FXML
    private TableColumn<Performance, String> colGrade;
    @FXML
    private TableColumn<Performance, String> colGpa;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colTotalMarks.setCellValueFactory(d -> d.getValue().totalMarksProperty());
        colGrade.setCellValueFactory(d -> d.getValue().gradeProperty());
        colGpa.setCellValueFactory(d -> d.getValue().gpaProperty());
        loadPerformance(null);
    }

    @FXML
    private void searchPerformance() {
        loadPerformance(txtSearch.getText());
    }

    @FXML
    private void refreshPerformance() {
        txtSearch.clear();
        loadPerformance(null);
    }

    private void loadPerformance(String keyword) {
        try {
            var rows = lecturerRepository.findPerformanceByLecturer(currentLecturer(), keyword).stream()
                    .map(r -> new Performance(
                            r.studentReg(),
                            r.studentName(),
                            r.courseCode(),
                            String.format("%.2f", r.totalMarks()),
                            toGrade(r.totalMarks()),
                            r.gpa() == null ? "" : String.format("%.2f", r.gpa())
                    ))
                    .toList();
            tblPerformance.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load marks/grades/GPA.", e);
        }
    }

    private String toGrade(double marks) {
        if (marks >= 85) return "A+";
        if (marks >= 75) return "A";
        if (marks >= 70) return "A-";
        if (marks >= 65) return "B+";
        if (marks >= 60) return "B";
        if (marks >= 55) return "B-";
        if (marks >= 50) return "C+";
        if (marks >= 45) return "C";
        if (marks >= 40) return "C-";
        return "F";
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
