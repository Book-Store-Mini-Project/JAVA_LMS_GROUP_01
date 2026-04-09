package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Grade;
import com.example.java_lms_group_01.util.GradeScaleUtil;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class StudentGradePageController {

    @FXML
    private TableView<Grade> tblGrades;
    @FXML
    private TableColumn<Grade, String> colCourseCode;
    @FXML
    private TableColumn<Grade, String> colCourseName;
    @FXML
    private TableColumn<Grade, String> colGrade;
    @FXML
    private Label lblGpa;
    @FXML
    private Label lblSgpa;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colCourseName.setCellValueFactory(d -> d.getValue().courseNameProperty());
        colGrade.setCellValueFactory(d -> d.getValue().gradeProperty());
        loadGradesAndGpa();
    }

    private void loadGradesAndGpa() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var summary = studentRepository.findGradeSummary(regNo);
            var rows = summary.grades().stream()
                    .map(r -> new Grade(
                            r.courseCode(),
                            r.courseName(),
                            "",
                            r.grade()
                    ))
                    .toList();
            tblGrades.getItems().setAll(rows);
            lblGpa.setText("GPA (without English) : " + String.format("%.2f", summary.gpa()));
            lblSgpa.setText("SGPA (with English) : " + String.format("%.2f", summary.sgpa()));
        } catch (SQLException e) {
            showError("Failed to load grades and GPA.", e);
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
