package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    public List<AttendanceRecord> findAttendanceByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT attendance_id, StudentReg, courseCode, SubmissionDate, session_type, attendance_status, tech_officer_reg
                FROM attendance
                WHERE StudentReg = ?
                ORDER BY SubmissionDate DESC, attendance_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new AttendanceRecord(
                            String.valueOf(rs.getInt("attendance_id")),
                            safe(rs.getString("StudentReg")),
                            safe(rs.getString("courseCode")),
                            rs.getDate("SubmissionDate") == null ? "" : rs.getDate("SubmissionDate").toString(),
                            safe(rs.getString("session_type")),
                            safe(rs.getString("attendance_status")),
                            safe(rs.getString("tech_officer_reg"))
                    ));
                }
                return rows;
            }
        }
    }

    public List<CourseRecord> findCoursesByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT c.courseCode, c.name, c.lecturerRegistrationNo, c.department, c.semester, c.credit, c.course_type, e.status
                FROM enrollment e
                INNER JOIN course c ON c.courseCode = e.courseCode
                WHERE e.studentReg = ?
                ORDER BY c.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<CourseRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new CourseRecord(
                            safe(rs.getString("courseCode")),
                            safe(rs.getString("name")),
                            safe(rs.getString("lecturerRegistrationNo")),
                            safe(rs.getString("department")),
                            safe(rs.getString("semester")),
                            String.valueOf(rs.getInt("credit")),
                            safe(rs.getString("course_type")),
                            safe(rs.getString("status"))
                    ));
                }
                return rows;
            }
        }
    }

    public GradeSummary findGradeSummary(String registrationNo) throws SQLException {
        String marksSql = """
                SELECT courseCode, quiz_1, quiz_2, quiz_3, assessment_1, assessment_2, mid_term, final_theory, final_practical
                FROM marks
                WHERE StudentReg = ?
                ORDER BY courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        List<GradeRecord> grades = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(marksSql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    grades.add(new GradeRecord(
                            safe(rs.getString("courseCode")),
                            decimal(rs.getObject("quiz_1")),
                            decimal(rs.getObject("quiz_2")),
                            decimal(rs.getObject("quiz_3")),
                            decimal(rs.getObject("assessment_1")),
                            decimal(rs.getObject("assessment_2")),
                            decimal(rs.getObject("mid_term")),
                            decimal(rs.getObject("final_theory")),
                            decimal(rs.getObject("final_practical"))
                    ));
                }
            }
        }

        double gpa = 0.0;
        try (PreparedStatement statement = connection.prepareStatement("SELECT GPA FROM student WHERE registrationNo = ?")) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getObject("GPA") != null) {
                    gpa = ((Number) rs.getObject("GPA")).doubleValue();
                }
            }
        }
        return new GradeSummary(grades, gpa);
    }

    public List<MaterialRecord> findMaterialsByStudent(String registrationNo, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT DISTINCT lm.material_id, lm.courseCode, lm.name, lm.path, lm.material_type
                FROM lecture_materials lm
                INNER JOIN enrollment e ON e.courseCode = lm.courseCode
                WHERE e.studentReg = ?
                  AND (? = '' OR lm.courseCode LIKE ? OR lm.name LIKE ?)
                ORDER BY lm.courseCode, lm.material_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, registrationNo);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<MaterialRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new MaterialRecord(
                            String.valueOf(rs.getInt("material_id")),
                            safe(rs.getString("courseCode")),
                            safe(rs.getString("name")),
                            safe(rs.getString("path")),
                            safe(rs.getString("material_type"))
                    ));
                }
                return rows;
            }
        }
    }

    public List<MedicalRecord> findMedicalByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, session_type, attendance_id, tech_officer_reg, approval_status
                FROM medical
                WHERE StudentReg = ?
                ORDER BY SubmissionDate DESC, medical_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<MedicalRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new MedicalRecord(
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
                return rows;
            }
        }
    }

    public List<TimetableRecord> findTimetableByStudent(String registrationNo) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        String department = findStudentDepartment(connection, registrationNo);
        if (department.isBlank()) {
            return List.of();
        }
        List<TimetableRecord> rows = new ArrayList<>();
        if (!loadTimetableRows(connection, department, "timetable", rows)) {
            loadTimetableRows(connection, department, "timeTable", rows);
        }
        return rows;
    }

    private String findStudentDepartment(Connection connection, String regNo) throws SQLException {
        String sql = "SELECT department FROM student WHERE registrationNo = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, regNo);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? safe(rs.getString("department")) : "";
            }
        }
    }

    private boolean loadTimetableRows(Connection connection, String department, String tableName, List<TimetableRecord> rows) throws SQLException {
        String sql = """
                SELECT t.time_table_id, t.department, t.lec_id, t.courseCode, t.admin_id, t.day, t.start_time, t.end_time, t.session_type
                FROM %s t
                WHERE t.department = ?
                ORDER BY t.day, t.start_time
                """.formatted(tableName);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, department);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new TimetableRecord(
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
                return true;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                return false;
            }
            throw e;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String decimal(Object value) {
        if (value == null) {
            return "";
        }
        return String.format("%.2f", ((Number) value).doubleValue());
    }

    public record AttendanceRecord(String attendanceId, String studentReg, String courseCode, String submissionDate,
                                   String sessionType, String attendanceStatus, String techOfficerReg) {}
    public record CourseRecord(String courseCode, String name, String lecturer, String department,
                               String semester, String credit, String type, String enrollmentStatus) {}
    public record GradeRecord(String courseCode, String quiz1, String quiz2, String quiz3, String assessment1,
                              String assessment2, String midTerm, String finalTheory, String finalPractical) {}
    public record GradeSummary(List<GradeRecord> grades, double gpa) {}
    public record MaterialRecord(String materialId, String courseCode, String name, String path, String type) {}
    public record MedicalRecord(String medicalId, String studentReg, String courseCode, String submissionDate,
                                String description, String sessionType, String attendanceId, String approvalStatus,
                                String techOfficerReg) {}
    public record TimetableRecord(String timetableId, String department, String lecId, String courseCode,
                                  String adminId, String day, String startTime, String endTime,
                                  String sessionType) {}
}
