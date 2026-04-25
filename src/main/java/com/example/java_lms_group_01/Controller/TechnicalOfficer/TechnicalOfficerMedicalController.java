package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.model.request.MedicalRequest;
import com.example.java_lms_group_01.session.LoggedInTechnicalOfficer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TechnicalOfficerMedicalController {

    @FXML private TextField txtStudentRegNo, txtCourseCode, txtAttendanceId, txtSearch;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpSubmissionDate;
    @FXML private ComboBox<String> cmbSessionType;

    @FXML private TableView<Medical> tblMedical;
    @FXML private TableColumn<Medical, String> colMedicalId, colStudentRegNo, colCourseCode,
            colDate, colDescription, colSessionType, colAttendanceId, colApprovalStatus, colTechOfficerReg;

    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();

    @FXML
    public void initialize() {
        // Setup Dropdown
        cmbSessionType.setItems(FXCollections.observableArrayList("theory", "practical"));

        // Setup Table Columns (Standard Beginner Style)
        colMedicalId.setCellValueFactory(data -> { return data.getValue().medicalIdProperty(); });
        colStudentRegNo.setCellValueFactory(data -> { return data.getValue().studentRegNoProperty(); });
        colCourseCode.setCellValueFactory(data -> { return data.getValue().courseCodeProperty(); });
        colDate.setCellValueFactory(data -> { return data.getValue().dateProperty(); });
        colDescription.setCellValueFactory(data -> { return data.getValue().descriptionProperty(); });
        colSessionType.setCellValueFactory(data -> { return data.getValue().sessionTypeProperty(); });
        colAttendanceId.setCellValueFactory(data -> { return data.getValue().attendanceIdProperty(); });
        colApprovalStatus.setCellValueFactory(data -> { return data.getValue().approvalStatusProperty(); });
        colTechOfficerReg.setCellValueFactory(data -> { return data.getValue().techOfficerRegProperty(); });

        // Selection Listener: When you click a row, fill the form
        tblMedical.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row != null) {
                txtStudentRegNo.setText(row.getStudentRegNo());
                txtCourseCode.setText(row.getCourseCode());
                txtDescription.setText(row.getDescription());
                txtAttendanceId.setText(row.getAttendanceId());
                cmbSessionType.setValue(row.getSessionType());

                // Convert String date to LocalDate
                if (row.getDate() != null && !row.getDate().isBlank()) {
                    dpSubmissionDate.setValue(LocalDate.parse(row.getDate()));
                } else {
                    dpSubmissionDate.setValue(null);
                }
            }
        });

        // Initial load
        loadMedicalData("");
    }

    @FXML
    private void addRecord(ActionEvent event) {
        if (isFormValid()) {
            try {
                MedicalRequest request = createRequest();
                technicalOfficerRepository.addMedical(request);

                loadMedicalData(txtSearch.getText());
                clearFormFields();
                showInfo("Medical record added!");
            } catch (Exception e) {
                showError("Failed to add record.", e);
            }
        }
    }

    @FXML
    private void updateRecord(ActionEvent event) {
        Medical selected = tblMedical.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Please select a medical record from the table.");
            return;
        }

        if (isFormValid()) {
            try {
                int id = Integer.parseInt(selected.getMedicalId());
                MedicalRequest request = createRequest();

                technicalOfficerRepository.updateMedical(id, request);
                loadMedicalData(txtSearch.getText());
                showInfo("Record updated!");
            } catch (Exception e) {
                showError("Failed to update record.", e);
            }
        }
    }

    @FXML
    private void deleteRecord(ActionEvent event) {
        Medical selected = tblMedical.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Select a record to delete.");
            return;
        }

        try {
            int medId = Integer.parseInt(selected.getMedicalId());
            int attId = Integer.parseInt(selected.getAttendanceId());

            technicalOfficerRepository.deleteMedical(medId, attId);
            loadMedicalData(txtSearch.getText());
            clearFormFields();
            showInfo("Record deleted.");
        } catch (Exception e) {
            showError("Failed to delete record.", e);
        }
    }

    @FXML
    private void clearForm(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    private void searchRecords(ActionEvent event) {
        loadMedicalData(txtSearch.getText());
    }

    @FXML
    private void refreshRecords(ActionEvent event) {
        txtSearch.clear();
        loadMedicalData("");
    }

    private void loadMedicalData(String keyword) {
        try {
            List<Medical> list = technicalOfficerRepository.findMedical(keyword);
            tblMedical.getItems().setAll(list);
        } catch (SQLException e) {
            showError("Could not load medical data.", e);
        }
    }

    private boolean isFormValid() {
        // Check if basic text fields are empty
        if (txtStudentRegNo.getText().isBlank() ||
                txtCourseCode.getText().isBlank() ||
                txtAttendanceId.getText().isBlank() ||
                txtDescription.getText().isBlank() ||
                dpSubmissionDate.getValue() == null ||
                cmbSessionType.getValue() == null) {

            showWarning("Please fill in all required fields.");
            return false;
        }

        // Check if Attendance ID is a number
        try {
            Integer.parseInt(txtAttendanceId.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            showWarning("Attendance ID must be a valid number.");
            return false;
        }
    }

    private MedicalRequest createRequest() {
        // Get the logged-in officer's ID
        String officerReg = LoggedInTechnicalOfficer.getRegistrationNo();
        if (officerReg == null) officerReg = "";

        // Package the data for the database
        return new MedicalRequest(
                txtStudentRegNo.getText().trim(),
                txtCourseCode.getText().trim(),
                Integer.parseInt(txtAttendanceId.getText().trim()),
                dpSubmissionDate.getValue(),
                cmbSessionType.getValue(),
                txtDescription.getText().trim(),
                officerReg
        );
    }

    private void clearFormFields() {
        txtStudentRegNo.clear();
        txtCourseCode.clear();
        txtAttendanceId.clear();
        txtDescription.clear();
        dpSubmissionDate.setValue(null);
        cmbSessionType.setValue(null);
        tblMedical.getSelectionModel().clearSelection();
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