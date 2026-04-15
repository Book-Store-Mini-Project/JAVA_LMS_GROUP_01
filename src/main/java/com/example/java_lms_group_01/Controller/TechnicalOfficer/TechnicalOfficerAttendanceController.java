package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TechnicalOfficerRepository;
import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.request.AttendanceRequest;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.sql.SQLException;

/**
 * Lets a technical officer add, update, search, and delete attendance records.
 */
public class TechnicalOfficerAttendanceController {

    @FXML
    private TextField txtStudentRegNo;
    @FXML
    private TextField txtCourseCode;
    @FXML
    private DatePicker dpAttendanceDate;
    @FXML
    private ComboBox<String> cmbSessionType;
    @FXML
    private ComboBox<String> cmbStatus;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Attendance> tblAttendance;
    @FXML
    private TableColumn<Attendance, String> colAttendanceId;
    @FXML
    private TableColumn<Attendance, String> colStudentRegNo;
    @FXML
    private TableColumn<Attendance, String> colCourseCode;
    @FXML
    private TableColumn<Attendance, String> colDate;
    @FXML
    private TableColumn<Attendance, String> colSessionType;
    @FXML
    private TableColumn<Attendance, String> colStatus;
    @FXML
    private TableColumn<Attendance, String> colTechOfficerReg;

    private final TechnicalOfficerRepository technicalOfficerRepository = new TechnicalOfficerRepository();

    @FXML
    public void initialize() {
        cmbSessionType.setItems(FXCollections.observableArrayList("theory", "practical"));
        cmbStatus.setItems(FXCollections.observableArrayList("present", "absent", "medical"));

        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colStudentRegNo.setCellValueFactory(d -> d.getValue().studentRegNoProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colDate.setCellValueFactory(d -> d.getValue().dateProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());

        loadAttendance(null);

        tblAttendance.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row == null) {
                return;
            }
            txtStudentRegNo.setText(row.getStudentRegNo());
            txtCourseCode.setText(row.getCourseCode());
            dpAttendanceDate.setValue(row.getDate().isBlank() ? null : LocalDate.parse(row.getDate()));
            cmbSessionType.setValue(row.getSessionType());
            cmbStatus.setValue(row.getStatus());
        });
    }

    @FXML
    private void addRecord(ActionEvent event) {
        if (!validForm()) {
            return;
        }
        try {
            technicalOfficerRepository.addAttendance(buildAttendanceRequest());
            loadAttendance(txtSearch.getText());
            clearForm(event);
        } catch (Exception e) {
            showError("Failed to add attendance record.", e);
        }
    }

    @FXML
    private void updateRecord(ActionEvent event) {
        Attendance selected = tblAttendance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a record to update.");
            return;
        }
        if (!validForm()) {
            return;
        }
        try {
            technicalOfficerRepository.updateAttendance(Integer.parseInt(selected.getAttendanceId()), buildAttendanceRequest());
            loadAttendance(txtSearch.getText());
        } catch (Exception e) {
            showError("Failed to update attendance record.", e);
        }
    }

    @FXML
    private void deleteRecord(ActionEvent event) {
        Attendance selected = tblAttendance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a record to delete.");
            return;
        }
        try {
            technicalOfficerRepository.deleteAttendance(Integer.parseInt(selected.getAttendanceId()));
            loadAttendance(txtSearch.getText());
            clearForm(event);
        } catch (Exception e) {
            showError("Failed to delete attendance record.", e);
        }
    }

    @FXML
    private void clearForm(ActionEvent event) {
        txtStudentRegNo.clear();
        txtCourseCode.clear();
        dpAttendanceDate.setValue(null);
        cmbSessionType.setValue(null);
        cmbStatus.setValue(null);
        tblAttendance.getSelectionModel().clearSelection();
    }

    @FXML
    private void searchRecords(ActionEvent event) {
        loadAttendance(txtSearch.getText());
    }

    @FXML
    private void refreshRecords(ActionEvent event) {
        txtSearch.clear();
        loadAttendance(null);
    }

    private boolean validForm() {
        if (value(txtStudentRegNo).isBlank() || value(txtCourseCode).isBlank() || dpAttendanceDate.getValue() == null
                || cmbSessionType.getValue() == null || cmbStatus.getValue() == null) {
            showWarn("Fill all required fields.");
            return false;
        }
        return true;
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
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

    private void loadAttendance(String keyword) {
        try {
            tblAttendance.getItems().setAll(technicalOfficerRepository.findAttendance(keyword));
        } catch (SQLException e) {
            showError("Failed to load attendance records.", e);
        }
    }

    private String currentTechOfficerReg() {
        String reg = TechnicalOfficerContext.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private AttendanceRequest buildAttendanceRequest() {
        return new AttendanceRequest(
                value(txtStudentRegNo),
                value(txtCourseCode),
                currentTechOfficerReg(),
                dpAttendanceDate.getValue(),
                cmbSessionType.getValue(),
                cmbStatus.getValue()
        );
    }

}
