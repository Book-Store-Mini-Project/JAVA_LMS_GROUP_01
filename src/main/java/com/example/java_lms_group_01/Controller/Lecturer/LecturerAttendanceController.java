package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.AttendanceMedical;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LecturerAttendanceController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<AttendanceMedical> tblAttendanceMedical;
    @FXML
    private TableColumn<AttendanceMedical, String> colAttendanceId;
    @FXML
    private TableColumn<AttendanceMedical, String> colStudentReg;
    @FXML
    private TableColumn<AttendanceMedical, String> colCourseCode;
    @FXML
    private TableColumn<AttendanceMedical, String> colDate;
    @FXML
    private TableColumn<AttendanceMedical, String> colSessionType;
    @FXML
    private TableColumn<AttendanceMedical, String> colAttendanceStatus;
    @FXML
    private TableColumn<AttendanceMedical, String> colMedicalId;
    @FXML
    private TableColumn<AttendanceMedical, String> colMedicalDescription;
    @FXML
    private TableColumn<AttendanceMedical, String> colMedicalApproval;
    @FXML
    private TableColumn<AttendanceMedical, String> colTechOfficerReg;
    @FXML
    private Button btnApproveMedical;
    @FXML
    private Button btnRejectMedical;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
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
        tblAttendanceMedical.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> updateActionState(row));
        loadRecords(null);
    }

    @FXML
    private void searchRecords() {
        loadRecords(txtSearch.getText());
    }

    @FXML
    private void refreshRecords() {
        txtSearch.clear();
        loadRecords(null);
    }

    @FXML
    private void approveMedical() {
        updateMedicalDecision("approved", "medical", "Medical approved.");
    }

    @FXML
    private void rejectMedical() {
        updateMedicalDecision("rejected", "absent", "Medical rejected. Attendance marked as absent.");
    }

    private void loadRecords(String keyword) {
        try {
            var rows = lecturerRepository.findAttendanceMedicalByLecturer(currentLecturer(), keyword).stream()
                    .map(r -> new AttendanceMedical(r.attendanceId(), r.studentReg(), r.courseCode(), r.date(),
                            r.sessionType(), r.attendanceStatus(), r.medicalId(), r.medicalDescription(),
                            r.medicalApprovalStatus(), r.techOfficerReg()))
                    .toList();
            tblAttendanceMedical.getItems().setAll(rows);
            updateActionState(tblAttendanceMedical.getSelectionModel().getSelectedItem());
        } catch (SQLException e) {
            showError("Failed to load attendance/medical records.", e);
        }
    }

    private void updateMedicalDecision(String approvalStatus, String attendanceStatus, String successMessage) {
        AttendanceMedical selected = tblAttendanceMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a medical record first.");
            return;
        }
        if (selected.getMedicalId().isBlank()) {
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
            loadRecords(txtSearch.getText());
            showInfo(successMessage);
        } catch (Exception e) {
            showError("Failed to update medical approval.", e);
        }
    }

    private void updateActionState(AttendanceMedical row) {
        boolean enabled = row != null && !row.getMedicalId().isBlank();
        if (btnApproveMedical != null) {
            btnApproveMedical.setDisable(!enabled);
        }
        if (btnRejectMedical != null) {
            btnRejectMedical.setDisable(!enabled);
        }
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

    private void showWarn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Medical Decision");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
