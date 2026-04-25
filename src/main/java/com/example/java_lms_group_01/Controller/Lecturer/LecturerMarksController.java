package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Mark;
import com.example.java_lms_group_01.model.request.MarkRequest;
import com.example.java_lms_group_01.session.LoggedInLecture;
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
    private TextField txtAssessment;
    @FXML
    private TextField txtProject;
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
    private TableColumn<Mark, String> colAssessment;
    @FXML
    private TableColumn<Mark, String> colProject;
    @FXML
    private TableColumn<Mark, String> colMid;
    @FXML
    private TableColumn<Mark, String> colFinalTheory;
    @FXML
    private TableColumn<Mark, String> colFinalPractical;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSelectionListener();
        loadMarks("");
    }

    // Set table columns
    private void setupTableColumns() {
        colMarkId.setCellValueFactory(d -> d.getValue().markIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colQuiz1.setCellValueFactory(d -> d.getValue().quiz1Property());
        colQuiz2.setCellValueFactory(d -> d.getValue().quiz2Property());
        colQuiz3.setCellValueFactory(d -> d.getValue().quiz3Property());
        colAssessment.setCellValueFactory(d -> d.getValue().assessmentProperty());
        colProject.setCellValueFactory(d -> d.getValue().projectProperty());
        colMid.setCellValueFactory(d -> d.getValue().midTermProperty());
        colFinalTheory.setCellValueFactory(d -> d.getValue().finalTheoryProperty());
        colFinalPractical.setCellValueFactory(d -> d.getValue().finalPracticalProperty());
    }

    // When a row is clicked, copy the data into the form fields.
    private void setupSelectionListener() {
        tblMarks.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            fillForm(newValue);
        });
    }

    private void fillForm(Mark row) {
        txtStudentReg.setText(row.getStudentReg());
        txtCourseCode.setText(row.getCourseCode());
        txtQuiz1.setText(row.getQuiz1());
        txtQuiz2.setText(row.getQuiz2());
        txtQuiz3.setText(row.getQuiz3());
        txtAssessment.setText(row.getAssessment());
        txtProject.setText(row.getProject());
        txtMidTerm.setText(row.getMidTerm());
        txtFinalTheory.setText(row.getFinalTheory());
        txtFinalPractical.setText(row.getFinalPractical());
    }

    @FXML
    private void addMarks() {
        if (!isFormValid()) {
            return;
        }

        try {
            lecturerRepository.addMarks(currentLecturer(), buildRequest());
            loadMarks(text(txtSearch));
            clearForm();
        } catch (Exception e) {
            showError("Failed to add marks.", e);
        }
    }

    @FXML
    private void updateMarks() {
        Mark selected = selectedMark();
        if (selected == null) {
            showWarn("Select a marks record to update.");
            return;
        }
        if (!isFormValid()) {
            return;
        }

        try {
            lecturerRepository.updateMarks(
                    currentLecturer(),
                    Integer.parseInt(selected.getMarkId()),
                    buildRequest()
            );
            loadMarks(text(txtSearch));
        } catch (Exception e) {
            showError("Failed to update marks.", e);
        }
    }

    @FXML
    private void deleteMarks() {
        Mark selected = selectedMark();
        if (selected == null) {
            showWarn("Select a marks record to delete.");
            return;
        }

        try {
            lecturerRepository.deleteMarks(currentLecturer(), Integer.parseInt(selected.getMarkId()));
            loadMarks(text(txtSearch));
            clearForm();
        } catch (Exception e) {
            showError("Failed to delete marks.", e);
        }
    }

    @FXML
    private void searchMarks() {
        loadMarks(text(txtSearch));
    }

    @FXML
    private void clearForm() {
        txtStudentReg.clear();
        txtCourseCode.clear();
        txtQuiz1.clear();
        txtQuiz2.clear();
        txtQuiz3.clear();
        txtAssessment.clear();
        txtProject.clear();
        txtMidTerm.clear();
        txtFinalTheory.clear();
        txtFinalPractical.clear();
        tblMarks.getSelectionModel().clearSelection();
    }

    // Read marks from the database and show them in the table.
    private void loadMarks(String keyword) {
        try {
            tblMarks.getItems().setAll(
                    lecturerRepository.findMarksByLecturer(currentLecturer(), keyword)
            );
        } catch (SQLException e) {
            showError("Failed to load marks.", e);
        }
    }

    // Check the form before saving.
    private boolean isFormValid() {
        if (text(txtStudentReg).isBlank() || text(txtCourseCode).isBlank()) {
            showWarn("Student registration and course code are required.");
            return false;
        }

        try {
            checkNumber(txtQuiz1);
            checkNumber(txtQuiz2);
            checkNumber(txtQuiz3);
            checkNumber(txtAssessment);
            checkNumber(txtProject);
            checkNumber(txtMidTerm);
            checkNumber(txtFinalTheory);
            checkNumber(txtFinalPractical);
        } catch (IllegalArgumentException e) {
            showWarn(e.getMessage());
            return false;
        }

        return true;
    }

    // Empty cells are allowed, but if a value is typed it must be a number from 0 to 100.
    private void checkNumber(TextField field) {
        String value = text(field);
        if (value.isBlank()) {
            return;
        }

        try {
            double number = Double.parseDouble(value);
            if (number < 0 || number > 100) {
                throw new IllegalArgumentException("Marks must be between 0 and 100.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Marks must be numeric.");
        }
    }

    private Mark selectedMark() {
        return tblMarks.getSelectionModel().getSelectedItem();
    }

    private String currentLecturer() {
        String reg = LoggedInLecture.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    // Convert the form into one request object.
    private MarkRequest buildRequest() {
        return new MarkRequest(
                text(txtStudentReg),
                text(txtCourseCode),
                parseNumber(txtQuiz1),
                parseNumber(txtQuiz2),
                parseNumber(txtQuiz3),
                parseNumber(txtAssessment),
                parseNumber(txtProject),
                parseNumber(txtMidTerm),
                parseNumber(txtFinalTheory),
                parseNumber(txtFinalPractical)
        );
    }

    // Blank fields become null so the repository can store them as empty values.
    private Double parseNumber(TextField field) {
        String value = text(field);
        if (value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value);
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
