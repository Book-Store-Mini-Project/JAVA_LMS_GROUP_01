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

public class LecturerTimetableController {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<TimetableRow> tblTimetable;
    @FXML
    private TableColumn<TimetableRow, String> colTimetableId;
    @FXML
    private TableColumn<TimetableRow, String> colDepartment;
    @FXML
    private TableColumn<TimetableRow, String> colLecId;
    @FXML
    private TableColumn<TimetableRow, String> colCourseCode;
    @FXML
    private TableColumn<TimetableRow, String> colAdminId;
    @FXML
    private TableColumn<TimetableRow, String> colDay;
    @FXML
    private TableColumn<TimetableRow, String> colStartTime;
    @FXML
    private TableColumn<TimetableRow, String> colEndTime;
    @FXML
    private TableColumn<TimetableRow, String> colSession;

    @FXML
    public void initialize() {
        colTimetableId.setCellValueFactory(d -> d.getValue().timetableIdProperty());
        colDepartment.setCellValueFactory(d -> d.getValue().departmentProperty());
        colLecId.setCellValueFactory(d -> d.getValue().lecIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colAdminId.setCellValueFactory(d -> d.getValue().adminIdProperty());
        colDay.setCellValueFactory(d -> d.getValue().dayProperty());
        colStartTime.setCellValueFactory(d -> d.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(d -> d.getValue().endTimeProperty());
        colSession.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        loadTimetable(null);
    }

    @FXML
    private void searchTimetable() {
        loadTimetable(txtSearch.getText());
    }

    @FXML
    private void refreshTimetable() {
        txtSearch.clear();
        loadTimetable(null);
    }

    private void loadTimetable(String keyword) {
        String lecturerReg = LecturerContext.getRegistrationNo();
        if (lecturerReg == null || lecturerReg.isBlank()) {
            return;
        }

        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type
                FROM timetable
                WHERE lec_id = ?
                  AND (? = '' OR courseCode LIKE ? OR day LIKE ? OR time_table_id LIKE ?)
                ORDER BY day, start_time
                """;

        List<TimetableRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + safeKeyword + "%";
                statement.setString(1, lecturerReg);
                statement.setString(2, safeKeyword);
                statement.setString(3, pattern);
                statement.setString(4, pattern);
                statement.setString(5, pattern);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new TimetableRow(
                                safe(rs.getString("time_table_id")),
                                safe(rs.getString("department")),
                                safe(rs.getString("lec_id")),
                                safe(rs.getString("courseCode")),
                                safe(rs.getString("admin_id")),
                                safe(rs.getString("day")),
                                rs.getTime("start_time") == null ? "" : rs.getTime("start_time").toString(),
                                rs.getTime("end_time") == null ? "" : rs.getTime("end_time").toString(),
                                safe(rs.getString("session_type"))
                        ));
                    }
                }
            }
            tblTimetable.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load lecturer timetable.", e);
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

    public static class TimetableRow {
        private final SimpleStringProperty timetableId;
        private final SimpleStringProperty department;
        private final SimpleStringProperty lecId;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty adminId;
        private final SimpleStringProperty day;
        private final SimpleStringProperty startTime;
        private final SimpleStringProperty endTime;
        private final SimpleStringProperty sessionType;

        public TimetableRow(String timetableId, String department, String lecId, String courseCode, String adminId, String day, String startTime, String endTime, String sessionType) {
            this.timetableId = new SimpleStringProperty(timetableId);
            this.department = new SimpleStringProperty(department);
            this.lecId = new SimpleStringProperty(lecId);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.adminId = new SimpleStringProperty(adminId);
            this.day = new SimpleStringProperty(day);
            this.startTime = new SimpleStringProperty(startTime);
            this.endTime = new SimpleStringProperty(endTime);
            this.sessionType = new SimpleStringProperty(sessionType);
        }

        public SimpleStringProperty timetableIdProperty() { return timetableId; }
        public SimpleStringProperty departmentProperty() { return department; }
        public SimpleStringProperty lecIdProperty() { return lecId; }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }
        public SimpleStringProperty adminIdProperty() { return adminId; }
        public SimpleStringProperty dayProperty() { return day; }
        public SimpleStringProperty startTimeProperty() { return startTime; }
        public SimpleStringProperty endTimeProperty() { return endTime; }
        public SimpleStringProperty sessionTypeProperty() { return sessionType; }
    }
}
