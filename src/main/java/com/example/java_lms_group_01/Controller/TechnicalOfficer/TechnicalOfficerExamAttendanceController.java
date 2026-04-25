package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.model.ExamAttendance;
import com.example.java_lms_group_01.model.request.ExamAttendanceRequest;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TechnicalOfficerExamAttendanceController {

    @FXML private TextField txtStudentRegNo, txtCourseCode, txtSearch;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private DatePicker dpAttendanceDate;

    @FXML private TableView<ExamAttendance> tblExamAttendance;
    @FXML private TableColumn<ExamAttendance, String> colExamAttendanceId, colStudentRegNo,
            colCourseCode, colStatus, colAttendanceDate;

    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();

    @FXML
    public void initialize() {
        // Setup Status Dropdown
        cmbStatus.setItems(FXCollections.observableArrayList("present", "absent"));

        // Setup Table Columns (Standard Readable Style)
        colExamAttendanceId.setCellValueFactory(data -> { return data.getValue().examAttendanceIdProperty(); });
        colStudentRegNo.setCellValueFactory(data -> { return data.getValue().studentRegNoProperty(); });
        colCourseCode.setCellValueFactory(data -> { return data.getValue().courseCodeProperty(); });
        colStatus.setCellValueFactory(data -> { return data.getValue().statusProperty(); });
        colAttendanceDate.setCellValueFactory(data -> { return data.getValue().attendanceDateProperty(); });

        // Selection Listener: When a row is clicked, fill the text boxes
        tblExamAttendance.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row != null) {
                txtStudentRegNo.setText(row.getStudentRegNo());
                txtCourseCode.setText(row.getCourseCode());
                cmbStatus.setValue(row.getStatus());

                // Convert String date to LocalDate for the DatePicker
                if (row.getAttendanceDate() != null && !row.getAttendanceDate().isEmpty()) {
                    dpAttendanceDate.setValue(LocalDate.parse(row.getAttendanceDate()));
                } else {
                    dpAttendanceDate.setValue(null);
                }
            }
        });

        // 4. Initial load of data
        loadTableData("");
    }

    @FXML
    private void addRecord(ActionEvent event) {
        if (isFormValid()) {
            try {
                ExamAttendanceRequest request = createRequest();
                technicalOfficerRepository.addExamAttendance(request);

                loadTableData(txtSearch.getText());
                clearFormFields();
                showInfo("Exam attendance added!");
            } catch (Exception e) {
                showError("Failed to add record.", e);
            }
        }
    }

    @FXML
    private void updateRecord(ActionEvent event) {
        ExamAttendance selected = tblExamAttendance.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Please select a record from the table first.");
            return;
        }

        if (isFormValid()) {
            try {
                int id = Integer.parseInt(selected.getExamAttendanceId());
                ExamAttendanceRequest request = createRequest();

                technicalOfficerRepository.updateExamAttendance(id, request);
                loadTableData(txtSearch.getText());
                showInfo("Record updated successfully!");
            } catch (Exception e) {
                showError("Failed to update record.", e);
            }
        }
    }

    @FXML
    private void deleteRecord(ActionEvent event) {
        ExamAttendance selected = tblExamAttendance.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Select a record to delete.");
            return;
        }

        try {
            int id = Integer.parseInt(selected.getExamAttendanceId());
            technicalOfficerRepository.deleteExamAttendance(id);

            loadTableData(txtSearch.getText());
            clearFormFields();
            showInfo("Record deleted.");
        } catch (Exception e) {
            showError("Failed to delete record.", e);
        }
    }

    @FXML
    private void searchRecords(ActionEvent event) {
        loadTableData(txtSearch.getText());
    }

    @FXML
    private void refreshRecords(ActionEvent event) {
        txtSearch.clear();
        loadTableData("");
    }

    @FXML
    private void clearForm(ActionEvent event) {
        clearFormFields();
    }

    private void loadTableData(String keyword) {
        try {
            List<ExamAttendance> list = technicalOfficerRepository.findExamAttendance(keyword);
            tblExamAttendance.getItems().setAll(list);
        } catch (SQLException e) {
            showError("Could not load exam attendance data.", e);
        }
    }

    private boolean isFormValid() {
        // Simple checks to ensure no fields are empty
        if (txtStudentRegNo.getText().isBlank() ||
                txtCourseCode.getText().isBlank() ||
                cmbStatus.getValue() == null ||
                dpAttendanceDate.getValue() == null) {

            showWarning("Please fill in all fields.");
            return false;
        }
        return true;
    }

    private ExamAttendanceRequest createRequest() {
        // Package the data for the database
        return new ExamAttendanceRequest(
                txtStudentRegNo.getText().trim(),
                txtCourseCode.getText().trim(),
                cmbStatus.getValue(),
                dpAttendanceDate.getValue()
        );
    }

    private void clearFormFields() {
        txtStudentRegNo.clear();
        txtCourseCode.clear();
        cmbStatus.setValue(null);
        dpAttendanceDate.setValue(null);
        tblExamAttendance.getSelectionModel().clearSelection();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}