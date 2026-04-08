package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TechnicalOfficerRepository {

    public void addAttendance(AttendanceMutation mutation) throws SQLException {
        String sql = "INSERT INTO attendance (StudentReg, courseCode, tech_officer_reg, SubmissionDate, session_type, attendance_status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAttendanceMutation(statement, mutation);
            statement.executeUpdate();
        }
    }

    public void updateAttendance(int attendanceId, AttendanceMutation mutation) throws SQLException {
        String sql = "UPDATE attendance SET StudentReg = ?, courseCode = ?, SubmissionDate = ?, session_type = ?, attendance_status = ?, tech_officer_reg = ? WHERE attendance_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAttendanceMutation(statement, mutation);
            statement.setInt(7, attendanceId);
            statement.executeUpdate();
        }
    }

    public void deleteAttendance(int attendanceId) throws SQLException {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, attendanceId);
            statement.executeUpdate();
        }
    }

    public List<AttendanceRecord> findAttendance(String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT attendance_id, StudentReg, courseCode, SubmissionDate, session_type, attendance_status, tech_officer_reg
                FROM attendance
                WHERE (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?)
                ORDER BY attendance_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, safeKeyword);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    Date submissionDate = rs.getDate("SubmissionDate");
                    rows.add(new AttendanceRecord(
                            String.valueOf(rs.getInt("attendance_id")),
                            safe(rs.getString("StudentReg")),
                            safe(rs.getString("courseCode")),
                            submissionDate == null ? "" : submissionDate.toString(),
                            safe(rs.getString("session_type")),
                            safe(rs.getString("attendance_status")),
                            safe(rs.getString("tech_officer_reg"))
                    ));
                }
                return rows;
            }
        }
    }

    public void addMedical(MedicalMutation mutation) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        ensureMedicalAttendance(connection, mutation);
        executeMedicalUpsert(connection, mutation, null);
    }

    public void updateMedical(int medicalId, MedicalMutation mutation) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        ensureMedicalAttendance(connection, mutation);
        executeMedicalUpsert(connection, mutation, medicalId);
    }

    public void deleteMedical(int medicalId, int attendanceId) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM medical WHERE medical_id = ?");
             PreparedStatement attendanceStatement = connection.prepareStatement("UPDATE attendance SET attendance_status = 'absent' WHERE attendance_id = ?")) {
            deleteStatement.setInt(1, medicalId);
            deleteStatement.executeUpdate();
            attendanceStatement.setInt(1, attendanceId);
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

    public List<MedicalRecord> findMedical(String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, session_type, attendance_id, tech_officer_reg, approval_status
                FROM medical
                WHERE (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?)
                ORDER BY medical_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, safeKeyword);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<MedicalRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    Date date = rs.getDate("SubmissionDate");
                    rows.add(new MedicalRecord(
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
                return rows;
            }
        }
    }

    public int countAttendance() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM attendance");
    }

    public int countMedical() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM medical");
    }

    public int countNotices() throws SQLException {
        return fetchCount("SELECT COUNT(*) FROM notice");
    }

    private void ensureMedicalAttendance(Connection connection, MedicalMutation mutation) throws SQLException {
        String sql = """
                SELECT attendance_id
                FROM attendance
                WHERE attendance_id = ?
                  AND StudentReg = ?
                  AND courseCode = ?
                  AND session_type = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, mutation.attendanceId());
            statement.setString(2, mutation.studentRegNo());
            statement.setString(3, mutation.courseCode());
            statement.setString(4, mutation.sessionType());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Attendance record does not match the given student, course, and session type.");
                }
            }
        }
    }

    private void executeMedicalUpsert(Connection connection, MedicalMutation mutation, Integer medicalId) throws SQLException {
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
            bindMedicalMutation(medicalStatement, mutation);
            if (medicalId != null) {
                medicalStatement.setInt(8, medicalId);
            }
            medicalStatement.executeUpdate();

            attendanceStatement.setString(1, mutation.techOfficerReg());
            attendanceStatement.setInt(2, mutation.attendanceId());
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

    private void bindAttendanceMutation(PreparedStatement statement, AttendanceMutation mutation) throws SQLException {
        statement.setString(1, mutation.studentRegNo());
        statement.setString(2, mutation.courseCode());
        statement.setString(3, mutation.techOfficerReg());
        statement.setDate(4, Date.valueOf(mutation.submissionDate()));
        statement.setString(5, mutation.sessionType());
        statement.setString(6, mutation.status());
    }

    private void bindMedicalMutation(PreparedStatement statement, MedicalMutation mutation) throws SQLException {
        statement.setString(1, mutation.studentRegNo());
        statement.setString(2, mutation.courseCode());
        statement.setString(3, mutation.techOfficerReg());
        statement.setDate(4, Date.valueOf(mutation.submissionDate()));
        statement.setString(5, mutation.description());
        statement.setString(6, mutation.sessionType());
        statement.setInt(7, mutation.attendanceId());
    }

    private int fetchCount(String sql) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public record AttendanceMutation(String studentRegNo, String courseCode, String techOfficerReg,
                                     java.time.LocalDate submissionDate, String sessionType, String status) {}
    public record AttendanceRecord(String attendanceId, String studentRegNo, String courseCode, String date,
                                   String sessionType, String status, String techOfficerReg) {}
    public record MedicalMutation(String studentRegNo, String courseCode, int attendanceId,
                                  java.time.LocalDate submissionDate, String sessionType,
                                  String description, String techOfficerReg) {}
    public record MedicalRecord(String medicalId, String studentRegNo, String courseCode, String date,
                                String description, String sessionType, String attendanceId,
                                String approvalStatus, String techOfficerReg) {}
}
