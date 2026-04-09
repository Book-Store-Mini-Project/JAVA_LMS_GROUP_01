package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.util.AssessmentStructureUtil;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.GradeScaleUtil;

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

    public List<AttendanceEligibilityRecord> findAttendanceEligibilityByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT e.courseCode,
                       SUM(CASE
                               WHEN a.attendance_id IS NOT NULL
                                    AND (a.attendance_status = 'present'
                                         OR (a.attendance_status = 'medical' AND m.approval_status = 'approved'))
                               THEN 1
                               ELSE 0
                           END) AS eligible_sessions,
                       COUNT(a.attendance_id) AS total_sessions
                FROM enrollment e
                LEFT JOIN attendance a ON a.StudentReg = e.studentReg AND a.courseCode = e.courseCode
                LEFT JOIN medical m ON m.attendance_id = a.attendance_id
                WHERE e.studentReg = ?
                GROUP BY e.courseCode
                ORDER BY e.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceEligibilityRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new AttendanceEligibilityRecord(
                            safe(rs.getString("courseCode")),
                            rs.getInt("eligible_sessions"),
                            rs.getInt("total_sessions")
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
                SELECT m.courseCode, c.name, c.credit, m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical,
                       EXISTS (
                           SELECT 1
                           FROM exam_attendance ea
                           WHERE ea.studentReg = m.StudentReg
                             AND ea.courseCode = m.courseCode
                             AND ea.status = 'present'
                       ) AS exam_present,
                       EXISTS (
                           SELECT 1
                           FROM medical md
                           WHERE md.StudentReg = m.StudentReg
                             AND md.courseCode = m.courseCode
                             AND md.approval_status = 'approved'
                             AND LOWER(COALESCE(md.session_type, '')) = 'exam'
                       ) AS approved_exam_medical
                FROM marks m
                INNER JOIN course c ON c.courseCode = m.courseCode
                WHERE m.StudentReg = ?
                ORDER BY m.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        List<GradeRecord> grades = new ArrayList<>();
        double gpaWeightedPoints = 0.0;
        int gpaCredits = 0;
        double sgpaWeightedPoints = 0.0;
        int sgpaCredits = 0;
        try (PreparedStatement statement = connection.prepareStatement(marksSql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String courseCode = safe(rs.getString("courseCode"));
                    double totalMarks = AssessmentStructureUtil.calculateMarkBreakdown(
                            connection,
                            courseCode,
                            nullableDecimal(rs.getObject("quiz_1")),
                            nullableDecimal(rs.getObject("quiz_2")),
                            nullableDecimal(rs.getObject("quiz_3")),
                            nullableDecimal(rs.getObject("assessment")),
                            nullableDecimal(rs.getObject("Project")),
                            nullableDecimal(rs.getObject("mid_term")),
                            nullableDecimal(rs.getObject("final_theory")),
                            nullableDecimal(rs.getObject("final_practical"))
                    ).totalMarks();
                    boolean examPresent = rs.getInt("exam_present") == 1;
                    boolean approvedExamMedical = rs.getInt("approved_exam_medical") == 1;
                    int credit = rs.getInt("credit");
                    Double gradePoint = GradeScaleUtil.toGradePoint(totalMarks, examPresent, approvedExamMedical);
                    String publishedGrade = GradeScaleUtil.toPublishedGrade(totalMarks, examPresent, approvedExamMedical);

                    grades.add(new GradeRecord(
                            courseCode,
                            safe(rs.getString("name")),
                            publishedGrade,
                            totalMarks
                    ));

                    if (gradePoint != null) {
                        sgpaWeightedPoints += gradePoint * credit;
                        sgpaCredits += credit;
                        if (!GradeScaleUtil.isEnglishCourse(courseCode)) {
                            gpaWeightedPoints += gradePoint * credit;
                            gpaCredits += credit;
                        }
                    }
                }
            }
        }

        double gpa = gpaCredits == 0 ? 0.0 : gpaWeightedPoints / gpaCredits;
        double sgpa = sgpaCredits == 0 ? 0.0 : sgpaWeightedPoints / sgpaCredits;
        return new GradeSummary(grades, gpa, sgpa);
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

    private static Double nullableDecimal(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    public record AttendanceRecord(String attendanceId, String studentReg, String courseCode, String submissionDate,
                                   String sessionType, String attendanceStatus, String techOfficerReg) {}
    public record AttendanceEligibilityRecord(String courseCode, int eligibleSessions, int totalSessions) {}
    public record CourseRecord(String courseCode, String name, String lecturer, String department,
                               String semester, String credit, String type, String enrollmentStatus) {}
    public record GradeRecord(String courseCode, String courseName, String grade, double totalMarks) {}
    public record GradeSummary(List<GradeRecord> grades, double gpa, double sgpa) {}
    public record MaterialRecord(String materialId, String courseCode, String name, String path, String type) {}
    public record MedicalRecord(String medicalId, String studentReg, String courseCode, String submissionDate,
                                String description, String sessionType, String attendanceId, String approvalStatus,
                                String techOfficerReg) {}
    public record TimetableRecord(String timetableId, String department, String lecId, String courseCode,
                                  String adminId, String day, String startTime, String endTime,
                                  String sessionType) {}
}
