package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentMedicalPageController {

    @FXML
    private TableView<MedicalRow> tblMedical;
    @FXML
    private TableColumn<MedicalRow, String> colMedicalId;
    @FXML
    private TableColumn<MedicalRow, String> colStudentReg;
    @FXML
    private TableColumn<MedicalRow, String> colCourseCode;
    @FXML
    private TableColumn<MedicalRow, String> colSubmissionDate;
    @FXML
    private TableColumn<MedicalRow, String> colDescription;
    @FXML
    private TableColumn<MedicalRow, String> colSessionType;
    @FXML
    private TableColumn<MedicalRow, String> colAttendanceId;
    @FXML
    private TableColumn<MedicalRow, String> colApprovalStatus;
    @FXML
    private TableColumn<MedicalRow, String> colTechOfficerReg;

    @FXML
    public void initialize() {
        colMedicalId.setCellValueFactory(d -> d.getValue().medicalIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colDescription.setCellValueFactory(d -> d.getValue().descriptionProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colApprovalStatus.setCellValueFactory(d -> d.getValue().approvalStatusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());
        loadMedical();
    }

    private void loadMedical() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        String sql = """
                SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, session_type, attendance_id, tech_officer_reg, approval_status
                FROM medical
                WHERE StudentReg = ?
                ORDER BY SubmissionDate DESC, medical_id DESC
                """;

        List<MedicalRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, regNo);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new MedicalRow(
                                String.valueOf(rs.getInt("medical_id")),
                                safe(rs.getString("StudentReg")),
                                safe(rs.getString("courseCode")),
                                rs.getDate("SubmissionDate") == null ? "" : rs.getDate("SubmissionDate").toString(),
                                safe(rs.getString("Description")),
                                safe(rs.getString("session_type")),
                                String.valueOf(rs.getInt("attendance_id")),
                                safe(rs.getString("approval_status")),
                                safe(rs.getString("tech_officer_reg"))
                        ));
                    }
                }
            }
            tblMedical.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load medical details.", e);
        }
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

    public static class MedicalRow {
        private final SimpleStringProperty medicalId;
        private final SimpleStringProperty studentReg;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty submissionDate;
        private final SimpleStringProperty description;
        private final SimpleStringProperty sessionType;
        private final SimpleStringProperty attendanceId;
        private final SimpleStringProperty approvalStatus;
        private final SimpleStringProperty techOfficerReg;

        public MedicalRow(String medicalId, String studentReg, String courseCode, String submissionDate, String description, String sessionType, String attendanceId, String approvalStatus, String techOfficerReg) {
            this.medicalId = new SimpleStringProperty(medicalId);
            this.studentReg = new SimpleStringProperty(studentReg);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.submissionDate = new SimpleStringProperty(submissionDate);
            this.description = new SimpleStringProperty(description);
            this.sessionType = new SimpleStringProperty(sessionType);
            this.attendanceId = new SimpleStringProperty(attendanceId);
            this.approvalStatus = new SimpleStringProperty(approvalStatus);
            this.techOfficerReg = new SimpleStringProperty(techOfficerReg);
        }

        public SimpleStringProperty medicalIdProperty() { return medicalId; }
        public SimpleStringProperty studentRegProperty() { return studentReg; }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }
        public SimpleStringProperty submissionDateProperty() { return submissionDate; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty sessionTypeProperty() { return sessionType; }
        public SimpleStringProperty attendanceIdProperty() { return attendanceId; }
        public SimpleStringProperty approvalStatusProperty() { return approvalStatus; }
        public SimpleStringProperty techOfficerRegProperty() { return techOfficerReg; }
    }
}
