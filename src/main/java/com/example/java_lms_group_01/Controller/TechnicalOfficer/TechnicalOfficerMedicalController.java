package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.TechnicalOfficerContext;
import javafx.beans.property.SimpleStringProperty;
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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private TableView<MedicalRow> tblMedical;
    @FXML
    private TableColumn<MedicalRow, String> colMedicalId;
    @FXML
    private TableColumn<MedicalRow, String> colStudentRegNo;
    @FXML
    private TableColumn<MedicalRow, String> colCourseCode;
    @FXML
    private TableColumn<MedicalRow, String> colDate;
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
            Connection connection = DBConnection.getInstance().getConnection();
            ensureMedicalAttendance(connection, Integer.parseInt(value(txtAttendanceId)));
            executeMedicalUpsert(connection, null);
            loadMedical(txtSearch.getText());
            clearForm(event);
        } catch (Exception e) {
            showError("Failed to add medical record.", e);
        }
    }

    @FXML
    private void updateRecord(ActionEvent event) {
        MedicalRow selected = tblMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a record to update.");
            return;
        }
        if (!validForm()) {
            return;
        }
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            ensureMedicalAttendance(connection, Integer.parseInt(value(txtAttendanceId)));
            executeMedicalUpsert(connection, Integer.parseInt(selected.getMedicalId()));
            loadMedical(txtSearch.getText());
        } catch (Exception e) {
            showError("Failed to update medical record.", e);
        }
    }

    @FXML
    private void deleteRecord(ActionEvent event) {
        MedicalRow selected = tblMedical.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a record to delete.");
            return;
        }
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM medical WHERE medical_id = ?");
                 PreparedStatement attendanceStatement = connection.prepareStatement("UPDATE attendance SET attendance_status = 'absent' WHERE attendance_id = ?")) {
                deleteStatement.setInt(1, Integer.parseInt(selected.getMedicalId()));
                deleteStatement.executeUpdate();

                attendanceStatement.setInt(1, Integer.parseInt(selected.getAttendanceId()));
                attendanceStatement.executeUpdate();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
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

    private void ensureMedicalAttendance(Connection connection, int attendanceId) throws SQLException {
        String sql = """
                SELECT attendance_id
                FROM attendance
                WHERE attendance_id = ?
                  AND StudentReg = ?
                  AND courseCode = ?
                  AND session_type = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, attendanceId);
            statement.setString(2, value(txtStudentRegNo));
            statement.setString(3, value(txtCourseCode));
            statement.setString(4, cmbSessionType.getValue());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Attendance record does not match the given student, course, and session type.");
                }
            }
        }
    }

    private void executeMedicalUpsert(Connection connection, Integer medicalId) throws SQLException {
        String insertSql = """
                INSERT INTO medical (StudentReg, courseCode, tech_officer_reg, SubmissionDate, Description, session_type, attendance_id, approval_status, approved_by_lecturer, approved_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', NULL, NULL)
                """;
        String updateSql = """
                UPDATE medical
                SET StudentReg = ?, courseCode = ?, tech_officer_reg = ?, SubmissionDate = ?, Description = ?, session_type = ?, attendance_id = ?,
                    approval_status = 'pending', approved_by_lecturer = NULL, approved_at = NULL
                WHERE medical_id = ?
                """;
        String attendanceSql = "UPDATE attendance SET attendance_status = 'medical', tech_officer_reg = ? WHERE attendance_id = ?";

        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement medicalStatement = connection.prepareStatement(medicalId == null ? insertSql : updateSql);
             PreparedStatement attendanceStatement = connection.prepareStatement(attendanceSql)) {
            medicalStatement.setString(1, value(txtStudentRegNo));
            medicalStatement.setString(2, value(txtCourseCode));
            medicalStatement.setString(3, currentTechOfficerReg());
            medicalStatement.setDate(4, Date.valueOf(dpSubmissionDate.getValue()));
            medicalStatement.setString(5, value(txtDescription));
            medicalStatement.setString(6, cmbSessionType.getValue());
            medicalStatement.setInt(7, Integer.parseInt(value(txtAttendanceId)));
            if (medicalId != null) {
                medicalStatement.setInt(8, medicalId);
            }
            medicalStatement.executeUpdate();

            attendanceStatement.setString(1, currentTechOfficerReg());
            attendanceStatement.setInt(2, Integer.parseInt(value(txtAttendanceId)));
            attendanceStatement.executeUpdate();

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
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
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, session_type, attendance_id, tech_officer_reg, approval_status
                FROM medical
                WHERE (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?)
                ORDER BY medical_id DESC
                """;

        List<MedicalRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + safeKeyword + "%";
                statement.setString(1, safeKeyword);
                statement.setString(2, pattern);
                statement.setString(3, pattern);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Date date = rs.getDate("SubmissionDate");
                        rows.add(new MedicalRow(
                                String.valueOf(rs.getInt("medical_id")),
                                safe(rs.getString("StudentReg")),
                                safe(rs.getString("courseCode")),
                                date == null ? "" : date.toString(),
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
            showError("Failed to load medical records.", e);
        }
    }

    private String currentTechOfficerReg() {
        String reg = TechnicalOfficerContext.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class MedicalRow {
        private final SimpleStringProperty medicalId;
        private final SimpleStringProperty studentRegNo;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty date;
        private final SimpleStringProperty description;
        private final SimpleStringProperty sessionType;
        private final SimpleStringProperty attendanceId;
        private final SimpleStringProperty approvalStatus;
        private final SimpleStringProperty techOfficerReg;

        public MedicalRow(String medicalId, String studentRegNo, String courseCode, String date, String description, String sessionType, String attendanceId, String approvalStatus, String techOfficerReg) {
            this.medicalId = new SimpleStringProperty(medicalId);
            this.studentRegNo = new SimpleStringProperty(studentRegNo);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.date = new SimpleStringProperty(date);
            this.description = new SimpleStringProperty(description);
            this.sessionType = new SimpleStringProperty(sessionType);
            this.attendanceId = new SimpleStringProperty(attendanceId);
            this.approvalStatus = new SimpleStringProperty(approvalStatus);
            this.techOfficerReg = new SimpleStringProperty(techOfficerReg);
        }

        public SimpleStringProperty medicalIdProperty() { return medicalId; }
        public SimpleStringProperty studentRegNoProperty() { return studentRegNo; }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty descriptionProperty() { return description; }
        public SimpleStringProperty sessionTypeProperty() { return sessionType; }
        public SimpleStringProperty attendanceIdProperty() { return attendanceId; }
        public SimpleStringProperty approvalStatusProperty() { return approvalStatus; }
        public SimpleStringProperty techOfficerRegProperty() { return techOfficerReg; }

        public String getStudentRegNo() { return studentRegNo.get(); }
        public String getCourseCode() { return courseCode.get(); }
        public String getDate() { return date.get(); }
        public String getDescription() { return description.get(); }
        public String getSessionType() { return sessionType.get(); }
        public String getAttendanceId() { return attendanceId.get(); }
        public String getMedicalId() { return medicalId.get(); }

        public void setStudentRegNo(String value) { studentRegNo.set(value); }
        public void setCourseCode(String value) { courseCode.set(value); }
        public void setDate(String value) { date.set(value); }
        public void setDescription(String value) { description.set(value); }
        public void setSessionType(String value) { sessionType.set(value); }
        public void setAttendanceId(String value) { attendanceId.set(value); }
    }
}
