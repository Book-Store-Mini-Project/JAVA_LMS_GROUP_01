package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.session.LoggedInLecture;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class LecturerAttendanceController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Attendance> tblAttendanceMedical;
    @FXML
    private TableColumn<Attendance, String> colAttendanceId;
    @FXML
    private TableColumn<Attendance, String> colStudentReg;
    @FXML
    private TableColumn<Attendance, String> colCourseCode;
    @FXML
    private TableColumn<Attendance, String> colDate;
    @FXML
    private TableColumn<Attendance, String> colSessionType;
    @FXML
    private TableColumn<Attendance, String> colAttendanceStatus;
    @FXML
    private TableColumn<Attendance, String> colMedicalId;
    @FXML
    private TableColumn<Attendance, String> colMedicalDescription;
    @FXML
    private TableColumn<Attendance, String> colMedicalApproval;
    @FXML
    private TableColumn<Attendance, String> colTechOfficerReg;
    @FXML
    private Button btnApproveMedical;
    @FXML
    private Button btnRejectMedical;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        setupColumns();
        setupSelectionListener();
        loadRecords("");
    }

    // Set the table columns one by one.
    private void setupColumns() {
        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colDate.setCellValueFactory(d -> d.getValue().dateProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceStatus.setCellValueFactory(d -> d.getValue().attendanceStatusProperty());
        colMedicalId.setCellValueFactory(d -> d.getValue().medicalIdProperty());
        colMedicalDescription.setCellValueFactory(d -> d.getValue().medicalDescriptionProperty());
        colMedicalApproval.setCellValueFactory(d -> d.getValue().medicalApprovalStatusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());
    }

    // Enable or disable buttons when the selected row changes.
    private void setupSelectionListener() {
        tblAttendanceMedical.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateActionState(newValue);
        });
    }

    @FXML
    private void searchRecords() {
        loadRecords(text(txtSearch));
    }

    @FXML
    private void refreshRecords() {
        txtSearch.clear();
        loadRecords("");
    }

    @FXML
    private void approveMedical() {
        updateMedicalDecision("approved", "present", "Medical approved. Attendance marked as present.");
    }

    @FXML
    private void rejectMedical() {
        updateMedicalDecision("rejected", "absent", "Medical rejected. Attendance marked as absent.");
    }

    // Load attendance and medical data for the current lecturer.
    private void loadRecords(String keyword) {
        try {
            String lecturer = currentLecturer();
            List<Attendance> list = lecturerRepository.findAttendanceMedicalByLecturer(lecturer, keyword);
            tblAttendanceMedical.getItems().setAll(list);
            updateActionState(tblAttendanceMedical.getSelectionModel().getSelectedItem());
        } catch (SQLException e) {
            showError("Failed to load attendance/medical records.", e);
        }
    }

    // Update the database after the lecturer approves or rejects a medical request.
    private void updateMedicalDecision(String approvalStatus, String attendanceStatus, String successMessage) {
        Attendance selected = tblAttendanceMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a medical record first.");
            return;
        }

        if (!hasMedicalRecord(selected)) {
            showWarn("Selected attendance record has no medical submission.");
            return;
        }

        try {
            lecturerRepository.updateMedicalDecision(
                    currentLecturer(),
                    Integer.parseInt(selected.getMedicalId()),
                    Integer.parseInt(selected.getAttendanceId()),
                    approvalStatus,
                    attendanceStatus
            );
            loadRecords(text(txtSearch));
            showInfo(successMessage);
        } catch (Exception e) {
            showError("Failed to update medical approval.", e);
        }
    }

    // Only enable the buttons when the row has a medical record.
    private void updateActionState(Attendance row) {
        boolean enabled = hasMedicalRecord(row);
        btnApproveMedical.setDisable(!enabled);
        btnRejectMedical.setDisable(!enabled);
    }

    private boolean hasMedicalRecord(Attendance row) {
        return row != null
                && row.getMedicalId() != null
                && !row.getMedicalId().isBlank();
    }

    private String currentLecturer() {
        String reg = LoggedInLecture.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    private void showWarn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
