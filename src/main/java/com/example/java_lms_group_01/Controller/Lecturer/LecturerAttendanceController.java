package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerAttendanceController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<AttendanceMedicalRow> tblAttendanceMedical;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colAttendanceId;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colStudentReg;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colCourseCode;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colDate;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colSessionType;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colAttendanceStatus;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colMedicalId;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colMedicalDescription;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colMedicalApproval;
    @FXML
    private TableColumn<AttendanceMedicalRow, String> colTechOfficerReg;
    @FXML
    private Button btnApproveMedical;
    @FXML
    private Button btnRejectMedical;

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
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT a.attendance_id, a.StudentReg, a.courseCode, a.SubmissionDate, a.session_type, a.attendance_status, a.tech_officer_reg,
                       m.medical_id, m.Description, m.approval_status
                FROM attendance a
                INNER JOIN course c ON c.courseCode = a.courseCode
                LEFT JOIN medical m ON m.attendance_id = a.attendance_id
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR a.StudentReg LIKE ? OR a.courseCode LIKE ?)
                ORDER BY a.attendance_id DESC
                """;

        List<AttendanceMedicalRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + safeKeyword + "%";
                statement.setString(1, currentLecturer());
                statement.setString(2, safeKeyword);
                statement.setString(3, pattern);
                statement.setString(4, pattern);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new AttendanceMedicalRow(
                                String.valueOf(rs.getInt("attendance_id")),
                                safe(rs.getString("StudentReg")),
                                safe(rs.getString("courseCode")),
                                rs.getDate("SubmissionDate") == null ? "" : rs.getDate("SubmissionDate").toString(),
                                safe(rs.getString("session_type")),
                                safe(rs.getString("attendance_status")),
                                rs.getObject("medical_id") == null ? "" : String.valueOf(rs.getInt("medical_id")),
                                safe(rs.getString("Description")),
                                safe(rs.getString("approval_status")),
                                safe(rs.getString("tech_officer_reg"))
                        ));
                    }
                }
            }
            tblAttendanceMedical.getItems().setAll(rows);
            updateActionState(tblAttendanceMedical.getSelectionModel().getSelectedItem());
        } catch (SQLException e) {
            showError("Failed to load attendance/medical records.", e);
        }
    }

    private void updateMedicalDecision(String approvalStatus, String attendanceStatus, String successMessage) {
        AttendanceMedicalRow selected = tblAttendanceMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a medical record first.");
            return;
        }
        if (selected.getMedicalId().isBlank()) {
            showWarn("Selected attendance record has no medical submission.");
            return;
        }

        String medicalSql = """
                UPDATE medical
                SET approval_status = ?, approved_by_lecturer = ?, approved_at = CURRENT_DATE
                WHERE medical_id = ?
                  AND attendance_id IN (
                      SELECT a.attendance_id
                      FROM attendance a
                      INNER JOIN course c ON c.courseCode = a.courseCode
                      WHERE c.lecturerRegistrationNo = ?
                  )
                """;
        String attendanceSql = "UPDATE attendance SET attendance_status = ? WHERE attendance_id = ?";

        try {
            Connection connection = DBConnection.getInstance().getConnection();
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement medicalStatement = connection.prepareStatement(medicalSql);
                 PreparedStatement attendanceStatement = connection.prepareStatement(attendanceSql)) {
                medicalStatement.setString(1, approvalStatus);
                medicalStatement.setString(2, currentLecturer());
                medicalStatement.setInt(3, Integer.parseInt(selected.getMedicalId()));
                medicalStatement.setString(4, currentLecturer());
                int medicalUpdated = medicalStatement.executeUpdate();
                if (medicalUpdated == 0) {
                    throw new SQLException("You can approve only medical records for your own courses.");
                }

                attendanceStatement.setString(1, attendanceStatus);
                attendanceStatement.setInt(2, Integer.parseInt(selected.getAttendanceId()));
                attendanceStatement.executeUpdate();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

            loadRecords(txtSearch.getText());
            showInfo(successMessage);
        } catch (Exception e) {
            showError("Failed to update medical approval.", e);
        }
    }

    private void updateActionState(AttendanceMedicalRow row) {
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

    private String safe(String value) {
        return value == null ? "" : value;
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

    public static class AttendanceMedicalRow {
        private final SimpleStringProperty attendanceId;
        private final SimpleStringProperty studentReg;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty date;
        private final SimpleStringProperty sessionType;
        private final SimpleStringProperty attendanceStatus;
        private final SimpleStringProperty medicalId;
        private final SimpleStringProperty medicalDescription;
        private final SimpleStringProperty medicalApprovalStatus;
        private final SimpleStringProperty techOfficerReg;

        public AttendanceMedicalRow(String attendanceId, String studentReg, String courseCode, String date, String sessionType, String attendanceStatus, String medicalId, String medicalDescription, String medicalApprovalStatus, String techOfficerReg) {
            this.attendanceId = new SimpleStringProperty(attendanceId);
            this.studentReg = new SimpleStringProperty(studentReg);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.date = new SimpleStringProperty(date);
            this.sessionType = new SimpleStringProperty(sessionType);
            this.attendanceStatus = new SimpleStringProperty(attendanceStatus);
            this.medicalId = new SimpleStringProperty(medicalId);
            this.medicalDescription = new SimpleStringProperty(medicalDescription);
            this.medicalApprovalStatus = new SimpleStringProperty(medicalApprovalStatus);
            this.techOfficerReg = new SimpleStringProperty(techOfficerReg);
        }

        public SimpleStringProperty attendanceIdProperty() { return attendanceId; }
        public SimpleStringProperty studentRegProperty() { return studentReg; }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty sessionTypeProperty() { return sessionType; }
        public SimpleStringProperty attendanceStatusProperty() { return attendanceStatus; }
        public SimpleStringProperty medicalIdProperty() { return medicalId; }
        public SimpleStringProperty medicalDescriptionProperty() { return medicalDescription; }
        public SimpleStringProperty medicalApprovalStatusProperty() { return medicalApprovalStatus; }
        public SimpleStringProperty techOfficerRegProperty() { return techOfficerReg; }

        public String getAttendanceId() { return attendanceId.get(); }
        public String getMedicalId() { return medicalId.get(); }
    }
}
