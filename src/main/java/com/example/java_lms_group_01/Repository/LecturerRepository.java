package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LecturerRepository {

    public List<AttendanceMedicalRecord> findAttendanceMedicalByLecturer(String lecturerReg, String keyword) throws SQLException {
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
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceMedicalRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new AttendanceMedicalRecord(
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
                return rows;
            }
        }
    }

    public void updateMedicalDecision(String lecturerReg, int medicalId, int attendanceId, String approvalStatus, String attendanceStatus) throws SQLException {
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
        Connection connection = DBConnection.getInstance().getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement medicalStatement = connection.prepareStatement(medicalSql);
             PreparedStatement attendanceStatement = connection.prepareStatement(attendanceSql)) {
            medicalStatement.setString(1, approvalStatus);
            medicalStatement.setString(2, lecturerReg);
            medicalStatement.setInt(3, medicalId);
            medicalStatement.setString(4, lecturerReg);
            int medicalUpdated = medicalStatement.executeUpdate();
            if (medicalUpdated == 0) {
                throw new SQLException("You can approve only medical records for your own courses.");
            }

            attendanceStatement.setString(1, attendanceStatus);
            attendanceStatement.setInt(2, attendanceId);
            attendanceStatement.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException sqlException) {
                throw sqlException;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public List<EligibilityRecord> findEligibilityByLecturer(String lecturerReg, String keyword) throws SQLException {
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
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            statement.setString(6, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<EligibilityRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new EligibilityRecord(
                            safe(rs.getString("StudentReg")),
                            (safe(rs.getString("firstName")) + " " + safe(rs.getString("lastName"))).trim(),
                            safe(rs.getString("courseCode")),
                            rs.getInt("eligible_sessions"),
                            rs.getInt("total_sessions")
                    ));
                }
                return rows;
            }
        }
    }

    public List<PerformanceRecord> findPerformanceByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT m.StudentReg, u.firstName, u.lastName, m.courseCode, s.GPA,
                       m.quiz_1, m.quiz_2, m.quiz_3, m.assessment_1, m.assessment_2, m.mid_term, m.final_theory, m.final_practical
                FROM marks m
                INNER JOIN course c ON c.courseCode = m.courseCode
                INNER JOIN student s ON s.registrationNo = m.StudentReg
                INNER JOIN users u ON u.user_id = s.registrationNo
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR m.StudentReg LIKE ? OR m.courseCode LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?)
                ORDER BY m.StudentReg, m.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            statement.setString(6, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<PerformanceRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new PerformanceRecord(
                            safe(rs.getString("StudentReg")),
                            (safe(rs.getString("firstName")) + " " + safe(rs.getString("lastName"))).trim(),
                            safe(rs.getString("courseCode")),
                            averageOfMarks(rs),
                            rs.getObject("GPA") == null ? null : ((Number) rs.getObject("GPA")).doubleValue()
                    ));
                }
                return rows;
            }
        }
    }

    public void addMarks(String lecturerReg, MarksMutation mutation) throws SQLException {
        String sql = """
                INSERT INTO marks (
                  LectureReg, StudentReg, courseCode, quiz_1, quiz_2, quiz_3,
                  assessment_1, assessment_2, mid_term, final_theory, final_practical
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lecturerReg);
            bindMarksMutation(statement, mutation);
            statement.executeUpdate();
        }
    }

    public void updateMarks(String lecturerReg, int markId, MarksMutation mutation) throws SQLException {
        String sql = """
                UPDATE marks SET
                  StudentReg = ?, courseCode = ?, quiz_1 = ?, quiz_2 = ?, quiz_3 = ?,
                  assessment_1 = ?, assessment_2 = ?, mid_term = ?, final_theory = ?, final_practical = ?
                WHERE mark_id = ? AND LectureReg = ?
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindMarksMutation(statement, mutation);
            statement.setInt(11, markId);
            statement.setString(12, lecturerReg);
            statement.executeUpdate();
        }
    }

    public void deleteMarks(String lecturerReg, int markId) throws SQLException {
        String sql = "DELETE FROM marks WHERE mark_id = ? AND LectureReg = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, markId);
            statement.setString(2, lecturerReg);
            statement.executeUpdate();
        }
    }

    public List<MarksRecord> findMarksByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT mark_id, StudentReg, courseCode, quiz_1, quiz_2, quiz_3, assessment_1, assessment_2, mid_term, final_theory, final_practical
                FROM marks
                WHERE LectureReg = ? AND (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?)
                ORDER BY mark_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<MarksRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new MarksRecord(
                            String.valueOf(rs.getInt("mark_id")),
                            safe(rs.getString("StudentReg")),
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
                return rows;
            }
        }
    }

    public int addMaterial(String lecturerReg, MaterialMutation mutation) throws SQLException {
        String sql = """
                INSERT INTO lecture_materials (courseCode, name, path, material_type)
                SELECT ?, ?, ?, ?
                WHERE EXISTS (
                    SELECT 1
                    FROM course
                    WHERE courseCode = ? AND lecturerRegistrationNo = ?
                )
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, mutation.courseCode());
            statement.setString(2, mutation.name());
            statement.setString(3, mutation.path());
            statement.setString(4, mutation.materialType());
            statement.setString(5, mutation.courseCode());
            statement.setString(6, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public int updateMaterial(String lecturerReg, int materialId, MaterialMutation mutation) throws SQLException {
        String sql = """
                UPDATE lecture_materials
                SET courseCode = ?, name = ?, path = ?, material_type = ?
                WHERE material_id = ?
                  AND courseCode IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                  AND ? IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, mutation.courseCode());
            statement.setString(2, mutation.name());
            statement.setString(3, mutation.path());
            statement.setString(4, mutation.materialType());
            statement.setInt(5, materialId);
            statement.setString(6, lecturerReg);
            statement.setString(7, mutation.courseCode());
            statement.setString(8, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public int deleteMaterial(String lecturerReg, int materialId) throws SQLException {
        String sql = """
                DELETE FROM lecture_materials
                WHERE material_id = ?
                  AND courseCode IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            statement.setString(2, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public List<MaterialRecord> findMaterialsByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT material_id, courseCode, name, path, material_type
                FROM lecture_materials
                WHERE courseCode IN (
                    SELECT courseCode
                    FROM course
                    WHERE lecturerRegistrationNo = ?
                )
                AND (? = '' OR courseCode LIKE ? OR name LIKE ?)
                ORDER BY material_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
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

    public List<StudentRecord> findStudentsByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT DISTINCT s.registrationNo, u.firstName, u.lastName, u.email, u.phoneNumber, s.department, s.status, s.GPA
                FROM student s
                INNER JOIN users u ON u.user_id = s.registrationNo
                INNER JOIN enrollment e ON e.studentReg = s.registrationNo
                INNER JOIN course c ON c.courseCode = e.courseCode
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR s.registrationNo LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ? OR s.department LIKE ?)
                ORDER BY s.registrationNo
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            statement.setString(6, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<StudentRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new StudentRecord(
                            safe(rs.getString("registrationNo")),
                            (safe(rs.getString("firstName")) + " " + safe(rs.getString("lastName"))).trim(),
                            safe(rs.getString("email")),
                            safe(rs.getString("phoneNumber")),
                            safe(rs.getString("department")),
                            safe(rs.getString("status")),
                            rs.getObject("GPA") == null ? "" : String.format("%.2f", ((Number) rs.getObject("GPA")).doubleValue())
                    ));
                }
                return rows;
            }
        }
    }

    public List<TimetableRecord> findTimetableByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type
                FROM timetable
                WHERE lec_id = ?
                  AND (? = '' OR courseCode LIKE ? OR day LIKE ? OR time_table_id LIKE ?)
                ORDER BY day, start_time
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<TimetableRecord> rows = new ArrayList<>();
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
                return rows;
            }
        }
    }

    private void bindMarksMutation(PreparedStatement statement, MarksMutation mutation) throws SQLException {
        statement.setString(1, mutation.studentReg());
        statement.setString(2, mutation.courseCode());
        setNullableDecimal(statement, 3, mutation.quiz1());
        setNullableDecimal(statement, 4, mutation.quiz2());
        setNullableDecimal(statement, 5, mutation.quiz3());
        setNullableDecimal(statement, 6, mutation.assessment1());
        setNullableDecimal(statement, 7, mutation.assessment2());
        setNullableDecimal(statement, 8, mutation.midTerm());
        setNullableDecimal(statement, 9, mutation.finalTheory());
        setNullableDecimal(statement, 10, mutation.finalPractical());
    }

    private static double averageOfMarks(ResultSet rs) throws SQLException {
        String[] columns = {"quiz_1", "quiz_2", "quiz_3", "assessment_1", "assessment_2", "mid_term", "final_theory", "final_practical"};
        double sum = 0.0;
        int count = 0;
        for (String column : columns) {
            Object value = rs.getObject(column);
            if (value != null) {
                sum += ((Number) value).doubleValue();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    private static void setNullableDecimal(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
            return;
        }
        statement.setDouble(index, value);
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

    public record AttendanceMedicalRecord(String attendanceId, String studentReg, String courseCode, String date,
                                          String sessionType, String attendanceStatus, String medicalId,
                                          String medicalDescription, String medicalApprovalStatus, String techOfficerReg) {}
    public record EligibilityRecord(String studentReg, String studentName, String courseCode, int eligibleSessions, int totalSessions) {}
    public record PerformanceRecord(String studentReg, String studentName, String courseCode, double totalMarks, Double gpa) {}
    public record MarksMutation(String studentReg, String courseCode, Double quiz1, Double quiz2, Double quiz3,
                                Double assessment1, Double assessment2, Double midTerm, Double finalTheory,
                                Double finalPractical) {}
    public record MarksRecord(String markId, String studentReg, String courseCode, String quiz1, String quiz2,
                              String quiz3, String assessment1, String assessment2, String midTerm,
                              String finalTheory, String finalPractical) {}
    public record MaterialMutation(String courseCode, String name, String path, String materialType) {}
    public record MaterialRecord(String materialId, String courseCode, String name, String path, String type) {}
    public record StudentRecord(String regNo, String name, String email, String phone, String department, String status, String gpa) {}
    public record TimetableRecord(String timetableId, String department, String lecId, String courseCode,
                                  String adminId, String day, String startTime, String endTime,
                                  String sessionType) {}
}
