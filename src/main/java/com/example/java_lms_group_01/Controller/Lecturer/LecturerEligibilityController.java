package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerEligibilityController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<EligibilityRow> tblEligibility;
    @FXML
    private TableColumn<EligibilityRow, String> colStudentReg;
    @FXML
    private TableColumn<EligibilityRow, String> colStudentName;
    @FXML
    private TableColumn<EligibilityRow, String> colCourseCode;
    @FXML
    private TableColumn<EligibilityRow, String> colAttendancePct;
    @FXML
    private TableColumn<EligibilityRow, String> colEligibility;

    @FXML
    public void initialize() {
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colAttendancePct.setCellValueFactory(d -> d.getValue().attendancePctProperty());
        colEligibility.setCellValueFactory(d -> d.getValue().eligibilityProperty());
        loadEligibility(null);
    }

    @FXML
    private void searchEligibility() {
        loadEligibility(txtSearch.getText());
    }

    @FXML
    private void refreshEligibility() {
        txtSearch.clear();
        loadEligibility(null);
    }

    private void loadEligibility(String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT a.StudentReg, u.firstName, u.lastName, a.courseCode,
                       SUM(CASE
                               WHEN a.attendance_status = 'present' THEN 1
                               WHEN a.attendance_status = 'medical' AND m.approval_status = 'approved' THEN 1
                               ELSE 0
                           END) AS eligible_sessions,
                       COUNT(*) AS total_sessions
                FROM attendance a
                INNER JOIN course c ON c.courseCode = a.courseCode
                INNER JOIN student s ON s.registrationNo = a.StudentReg
                INNER JOIN users u ON u.user_id = s.registrationNo
                LEFT JOIN medical m ON m.attendance_id = a.attendance_id
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR a.StudentReg LIKE ? OR a.courseCode LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?)
                GROUP BY a.StudentReg, u.firstName, u.lastName, a.courseCode
                ORDER BY a.StudentReg, a.courseCode
                """;

        List<EligibilityRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + safeKeyword + "%";
                statement.setString(1, currentLecturer());
                statement.setString(2, safeKeyword);
                statement.setString(3, pattern);
                statement.setString(4, pattern);
                statement.setString(5, pattern);
                statement.setString(6, pattern);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        int total = rs.getInt("total_sessions");
                        int effectivePresent = rs.getInt("eligible_sessions");
                        double pct = total == 0 ? 0.0 : (effectivePresent * 100.0) / total;
                        String status = pct >= 80.0 ? "Eligible" : "Not Eligible";
                        String fullName = safe(rs.getString("firstName")) + " " + safe(rs.getString("lastName"));

                        rows.add(new EligibilityRow(
                                safe(rs.getString("StudentReg")),
                                fullName.trim(),
                                safe(rs.getString("courseCode")),
                                String.format("%.2f%%", pct),
                                status
                        ));
                    }
                }
            }
            tblEligibility.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load undergraduate eligibility.", e);
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

    public static class EligibilityRow {
        private final SimpleStringProperty studentReg;
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty attendancePct;
        private final SimpleStringProperty eligibility;

        public EligibilityRow(String studentReg, String studentName, String courseCode, String attendancePct, String eligibility) {
            this.studentReg = new SimpleStringProperty(studentReg);
            this.studentName = new SimpleStringProperty(studentName);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.attendancePct = new SimpleStringProperty(attendancePct);
            this.eligibility = new SimpleStringProperty(eligibility);
        }

        public SimpleStringProperty studentRegProperty() { return studentReg; }
        public SimpleStringProperty studentNameProperty() { return studentName; }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }
        public SimpleStringProperty attendancePctProperty() { return attendancePct; }
        public SimpleStringProperty eligibilityProperty() { return eligibility; }
    }
}
