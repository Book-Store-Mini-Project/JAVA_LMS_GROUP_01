package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Mark;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LecturerMarksController {

    @FXML
    private TextField txtStudentReg;
    @FXML
    private TextField txtCourseCode;
    @FXML
    private TextField txtQuiz1;
    @FXML
    private TextField txtQuiz2;
    @FXML
    private TextField txtQuiz3;
    @FXML
    private TextField txtAssessment1;
    @FXML
    private TextField txtAssessment2;
    @FXML
    private TextField txtMidTerm;
    @FXML
    private TextField txtFinalTheory;
    @FXML
    private TextField txtFinalPractical;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Mark> tblMarks;
    @FXML
    private TableColumn<Mark, String> colMarkId;
    @FXML
    private TableColumn<Mark, String> colStudentReg;
    @FXML
    private TableColumn<Mark, String> colCourseCode;
    @FXML
    private TableColumn<Mark, String> colQuiz1;
    @FXML
    private TableColumn<Mark, String> colQuiz2;
    @FXML
    private TableColumn<Mark, String> colQuiz3;
    @FXML
    private TableColumn<Mark, String> colA1;
    @FXML
    private TableColumn<Mark, String> colA2;
    @FXML
    private TableColumn<Mark, String> colMid;
    @FXML
    private TableColumn<Mark, String> colFinalTheory;
    @FXML
    private TableColumn<Mark, String> colFinalPractical;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        colMarkId.setCellValueFactory(d -> d.getValue().markIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colQuiz1.setCellValueFactory(d -> d.getValue().quiz1Property());
        colQuiz2.setCellValueFactory(d -> d.getValue().quiz2Property());
        colQuiz3.setCellValueFactory(d -> d.getValue().quiz3Property());
        colA1.setCellValueFactory(d -> d.getValue().assessment1Property());
        colA2.setCellValueFactory(d -> d.getValue().assessment2Property());
        colMid.setCellValueFactory(d -> d.getValue().midTermProperty());
        colFinalTheory.setCellValueFactory(d -> d.getValue().finalTheoryProperty());
        colFinalPractical.setCellValueFactory(d -> d.getValue().finalPracticalProperty());
        loadMarks(null);

        tblMarks.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row == null) {
                return;
            }
            txtStudentReg.setText(row.getStudentReg());
            txtCourseCode.setText(row.getCourseCode());
            txtQuiz1.setText(row.getQuiz1());
            txtQuiz2.setText(row.getQuiz2());
            txtQuiz3.setText(row.getQuiz3());
            txtAssessment1.setText(row.getAssessment1());
            txtAssessment2.setText(row.getAssessment2());
            txtMidTerm.setText(row.getMidTerm());
            txtFinalTheory.setText(row.getFinalTheory());
            txtFinalPractical.setText(row.getFinalPractical());
        });
    }

    @FXML
    private void addMarks() {
        if (!validForm()) {
            return;
        }
        try {
            lecturerRepository.addMarks(currentLecturer(), buildMutation());
            loadMarks(txtSearch.getText());
            clearForm();
        } catch (Exception e) {
            showError("Failed to add marks.", e);
        }
    }

    @FXML
    private void updateMarks() {
        Mark selected = tblMarks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a marks record to update.");
            return;
        }
        if (!validForm()) {
            return;
        }
        try {
            lecturerRepository.updateMarks(currentLecturer(), Integer.parseInt(selected.getMarkId()), buildMutation());
            loadMarks(txtSearch.getText());
        } catch (Exception e) {
            showError("Failed to update marks.", e);
        }
    }

    @FXML
    private void deleteMarks() {
        Mark selected = tblMarks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a marks record to delete.");
            return;
        }
        try {
            lecturerRepository.deleteMarks(currentLecturer(), Integer.parseInt(selected.getMarkId()));
            loadMarks(txtSearch.getText());
            clearForm();
        } catch (Exception e) {
            showError("Failed to delete marks.", e);
        }
    }

    @FXML
    private void searchMarks() {
        loadMarks(txtSearch.getText());
    }

    @FXML
    private void refreshMarks() {
        txtSearch.clear();
        loadMarks(null);
    }

    @FXML
    private void clearForm() {
        txtStudentReg.clear();
        txtCourseCode.clear();
        txtQuiz1.clear();
        txtQuiz2.clear();
        txtQuiz3.clear();
        txtAssessment1.clear();
        txtAssessment2.clear();
        txtMidTerm.clear();
        txtFinalTheory.clear();
        txtFinalPractical.clear();
        tblMarks.getSelectionModel().clearSelection();
    }

    private void loadMarks(String keyword) {
        try {
            var rows = lecturerRepository.findMarksByLecturer(currentLecturer(), keyword).stream()
                    .map(r -> new Mark(r.markId(), r.studentReg(), r.courseCode(), r.quiz1(), r.quiz2(), r.quiz3(),
                            r.assessment1(), r.assessment2(), r.midTerm(), r.finalTheory(), r.finalPractical()))
                    .toList();
            tblMarks.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load marks.", e);
        }
    }

    private boolean validForm() {
        if (value(txtStudentReg).isBlank() || value(txtCourseCode).isBlank()) {
            showWarn("Student registration and course code are required.");
            return false;
        }
        try {
            validateDecimal(txtQuiz1);
            validateDecimal(txtQuiz2);
            validateDecimal(txtQuiz3);
            validateDecimal(txtAssessment1);
            validateDecimal(txtAssessment2);
            validateDecimal(txtMidTerm);
            validateDecimal(txtFinalTheory);
            validateDecimal(txtFinalPractical);
        } catch (IllegalArgumentException e) {
            showWarn(e.getMessage());
            return false;
        }
        return true;
    }

    private void validateDecimal(TextField textField) {
        String value = value(textField);
        if (value.isBlank()) {
            return;
        }
        try {
            double numeric = Double.parseDouble(value);
            if (numeric < 0 || numeric > 100) {
                throw new IllegalArgumentException("Exam marks must be between 0 and 100.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Marks must be numeric.");
        }
    }

    private String currentLecturer() {
        String reg = LecturerContext.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private LecturerRepository.MarksMutation buildMutation() {
        return new LecturerRepository.MarksMutation(
                value(txtStudentReg),
                value(txtCourseCode),
                parseDecimal(txtQuiz1),
                parseDecimal(txtQuiz2),
                parseDecimal(txtQuiz3),
                parseDecimal(txtAssessment1),
                parseDecimal(txtAssessment2),
                parseDecimal(txtMidTerm),
                parseDecimal(txtFinalTheory),
                parseDecimal(txtFinalPractical)
        );
    }

    private Double parseDecimal(TextField textField) {
        String value = value(textField);
        return value.isBlank() ? null : Double.parseDouble(value);
    }

    private void showWarn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

}
