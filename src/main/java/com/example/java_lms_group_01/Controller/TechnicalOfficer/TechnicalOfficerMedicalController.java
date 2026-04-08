package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.sql.SQLException;

public class TechnicalOfficerMedicalController {

    @FXML
    private TextField txtStudentRegNo;
    @FXML
    private TextField txtCourseCode;
    @FXML
    private TextField txtAttendanceId;
    @FXML
    private DatePicker dpSubmissionDate;
    @FXML
    private ComboBox<String> cmbSessionType;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Medical> tblMedical;
    @FXML
    private TableColumn<Medical, String> colMedicalId;
    @FXML
    private TableColumn<Medical, String> colStudentRegNo;
    @FXML
    private TableColumn<Medical, String> colCourseCode;
    @FXML
    private TableColumn<Medical, String> colDate;
    @FXML
    private TableColumn<Medical, String> colDescription;
    @FXML
    private TableColumn<Medical, String> colSessionType;
    @FXML
    private TableColumn<Medical, String> colAttendanceId;
    @FXML
    private TableColumn<Medical, String> colApprovalStatus;
    @FXML
    private TableColumn<Medical, String> colTechOfficerReg;

    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();

    @FXML
    public void initialize() {
        cmbSessionType.setItems(FXCollections.observableArrayList("theory", "practical"));

        colMedicalId.setCellValueFactory(d -> d.getValue().medicalIdProperty());
        colStudentRegNo.setCellValueFactory(d -> d.getValue().studentRegNoProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colDate.setCellValueFactory(d -> d.getValue().dateProperty());
        colDescription.setCellValueFactory(d -> d.getValue().descriptionProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colApprovalStatus.setCellValueFactory(d -> d.getValue().approvalStatusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());

        loadMedical(null);

        tblMedical.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row == null) {
                return;
            }
            txtStudentRegNo.setText(row.getStudentRegNo());
            txtCourseCode.setText(row.getCourseCode());
            dpSubmissionDate.setValue(row.getDate().isBlank() ? null : LocalDate.parse(row.getDate()));
            txtDescription.setText(row.getDescription());
            cmbSessionType.setValue(row.getSessionType());
            txtAttendanceId.setText(row.getAttendanceId());
        });
    }

    @FXML
    private void addRecord(ActionEvent event) {
        if (!validForm()) {
            return;
        }
        try {
            technicalOfficerRepository.addMedical(buildMedicalMutation());
            loadMedical(txtSearch.getText());
            clearForm(event);
        } catch (Exception e) {
            showError("Failed to add medical record.", e);
        }
    }

    @FXML
    private void updateRecord(ActionEvent event) {
        Medical selected = tblMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a record to update.");
            return;
        }
        if (!validForm()) {
            return;
        }
        try {
            technicalOfficerRepository.updateMedical(Integer.parseInt(selected.getMedicalId()), buildMedicalMutation());
            loadMedical(txtSearch.getText());
        } catch (Exception e) {
            showError("Failed to update medical record.", e);
        }
    }

    @FXML
    private void deleteRecord(ActionEvent event) {
        Medical selected = tblMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a record to delete.");
            return;
        }
        try {
            technicalOfficerRepository.deleteMedical(Integer.parseInt(selected.getMedicalId()), Integer.parseInt(selected.getAttendanceId()));
            loadMedical(txtSearch.getText());
            clearForm(event);
        } catch (Exception e) {
            showError("Failed to delete medical record.", e);
        }
    }

    @FXML
    private void clearForm(ActionEvent event) {
        txtStudentRegNo.clear();
        txtCourseCode.clear();
        txtAttendanceId.clear();
        dpSubmissionDate.setValue(null);
        txtDescription.clear();
        cmbSessionType.setValue(null);
        tblMedical.getSelectionModel().clearSelection();
    }

    @FXML
    private void searchRecords(ActionEvent event) {
        loadMedical(txtSearch.getText());
    }

    @FXML
    private void refreshRecords(ActionEvent event) {
        txtSearch.clear();
        loadMedical(null);
    }

    private boolean validForm() {
        if (value(txtStudentRegNo).isBlank() || value(txtCourseCode).isBlank() || dpSubmissionDate.getValue() == null
                || value(txtDescription).isBlank() || cmbSessionType.getValue() == null || value(txtAttendanceId).isBlank()) {
            showWarn("Fill all required fields.");
            return false;
        }
        try {
            Integer.parseInt(value(txtAttendanceId));
        } catch (NumberFormatException e) {
            showWarn("Attendance ID must be numeric.");
            return false;
        }
        return true;
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String value(TextArea textArea) {
        return textArea.getText() == null ? "" : textArea.getText().trim();
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

    private void loadMedical(String keyword) {
        try {
            var rows = technicalOfficerRepository.findMedical(keyword).stream()
                    .map(r -> new Medical(r.medicalId(), r.studentRegNo(), r.courseCode(), r.date(), r.description(),
                            r.sessionType(), r.attendanceId(), r.approvalStatus(), r.techOfficerReg()))
                    .toList();
            tblMedical.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load medical records.", e);
        }
    }

    private String currentTechOfficerReg() {
        String reg = TechnicalOfficerContext.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private TechnicalOfficerRepository.MedicalMutation buildMedicalMutation() {
        return new TechnicalOfficerRepository.MedicalMutation(
                value(txtStudentRegNo),
                value(txtCourseCode),
                Integer.parseInt(value(txtAttendanceId)),
                dpSubmissionDate.getValue(),
                cmbSessionType.getValue(),
                value(txtDescription),
                currentTechOfficerReg()
        );
    }

}
