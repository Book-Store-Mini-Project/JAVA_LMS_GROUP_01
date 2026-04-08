package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Grade;
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
    private TableColumn<Grade, String> colQuiz1;
    @FXML
    private TableColumn<Grade, String> colQuiz2;
    @FXML
    private TableColumn<Grade, String> colQuiz3;
    @FXML
    private TableColumn<Grade, String> colAssessment1;
    @FXML
    private TableColumn<Grade, String> colAssessment2;
    @FXML
    private TableColumn<Grade, String> colMidTerm;
    @FXML
    private TableColumn<Grade, String> colFinalTheory;
    @FXML
    private TableColumn<Grade, String> colFinalPractical;
    @FXML
    private TableColumn<Grade, String> colTotal;
    @FXML
    private TableColumn<Grade, String> colGrade;
    @FXML
    private Label lblGpa;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colQuiz1.setCellValueFactory(d -> d.getValue().quiz1Property());
        colQuiz2.setCellValueFactory(d -> d.getValue().quiz2Property());
        colQuiz3.setCellValueFactory(d -> d.getValue().quiz3Property());
        colAssessment1.setCellValueFactory(d -> d.getValue().assessment1Property());
        colAssessment2.setCellValueFactory(d -> d.getValue().assessment2Property());
        colMidTerm.setCellValueFactory(d -> d.getValue().midTermProperty());
        colFinalTheory.setCellValueFactory(d -> d.getValue().finalTheoryProperty());
        colFinalPractical.setCellValueFactory(d -> d.getValue().finalPracticalProperty());
        colTotal.setCellValueFactory(d -> d.getValue().totalProperty());
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
                    .map(r -> {
                        double total = calculateAverage(r);
                        return new Grade(r.courseCode(), r.quiz1(), r.quiz2(), r.quiz3(), r.assessment1(),
                                r.assessment2(), r.midTerm(), r.finalTheory(), r.finalPractical(),
                                String.format("%.2f", total), toGrade(total));
                    })
                    .toList();
            tblGrades.getItems().setAll(rows);
            lblGpa.setText("GPA : " + String.format("%.2f", summary.gpa()));
        } catch (SQLException e) {
            showError("Failed to load grades and GPA.", e);
        }
    }

    private double calculateAverage(StudentRepository.GradeRecord row) {
        String[] fields = {row.quiz1(), row.quiz2(), row.quiz3(), row.assessment1(), row.assessment2(), row.midTerm(), row.finalTheory(), row.finalPractical()};
        double sum = 0.0;
        int count = 0;
        for (String value : fields) {
            if (!value.isBlank()) {
                sum += Double.parseDouble(value);
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
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

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
