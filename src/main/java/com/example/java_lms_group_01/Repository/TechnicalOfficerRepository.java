package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.model.request.AttendanceRequest;
import com.example.java_lms_group_01.model.request.MedicalRequest;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access used by technical officer pages.
 * This class handles attendance records, medical records, and dashboard counts.
 */
public class TechnicalOfficerRepository {

    // Add a new attendance record.
    public void addAttendance(AttendanceRequest request) throws SQLException {
        String sql = "INSERT INTO attendance (StudentReg, courseCode, tech_officer_reg, SubmissionDate, session_type, attendance_status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAttendanceRequest(statement, request);
            statement.executeUpdate();
        }
    }

    public void updateAttendance(int attendanceId, AttendanceRequest request) throws SQLException {
        String sql = "UPDATE attendance SET StudentReg = ?, courseCode = ?, SubmissionDate = ?, session_type = ?, attendance_status = ?, tech_officer_reg = ? WHERE attendance_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindAttendanceRequest(statement, request);
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

    public List<Attendance> findAttendance(String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT attendance_id, StudentReg, courseCode, SubmissionDate, session_type, attendance_status, tech_officer_reg "
                + "FROM attendance "
                + "WHERE (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?) "
                + "ORDER BY attendance_id DESC";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, safeKeyword);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Attendance> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapAttendanceRecord(rs));
                }
                return rows;
            }
        }
    }

    public void addMedical(MedicalRequest request) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        ensureMedicalAttendance(connection, request);
        executeMedicalUpsert(connection, request, null);
    }

    public void updateMedical(int medicalId, MedicalRequest request) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        ensureMedicalAttendance(connection, request);
        executeMedicalUpsert(connection, request, medicalId);
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
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public List<Medical> findMedical(String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, session_type, attendance_id, tech_officer_reg, approval_status "
                + "FROM medical "
                + "WHERE (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?) "
                + "ORDER BY medical_id DESC";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, safeKeyword);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Medical> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapMedicalRecord(rs));
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

    private void ensureMedicalAttendance(Connection connection, MedicalRequest request) throws SQLException {
        String sql = "SELECT attendance_id "
                + "FROM attendance "
                + "WHERE attendance_id = ? "
                + "AND StudentReg = ? "
                + "AND courseCode = ? "
                + "AND session_type = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, request.getAttendanceId());
            statement.setString(2, request.getStudentRegNo());
            statement.setString(3, request.getCourseCode());
            statement.setString(4, request.getSessionType());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Attendance record does not match the given student, course, and session type.");
                }
            }
        }
    }

    private void executeMedicalUpsert(Connection connection, MedicalRequest request, Integer medicalId) throws SQLException {
        String insertSql = "INSERT INTO medical (StudentReg, courseCode, tech_officer_reg, SubmissionDate, Description, session_type, attendance_id, approval_status, approved_by_lecturer, approved_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', NULL, NULL)";
        String updateSql = "UPDATE medical "
                + "SET StudentReg = ?, courseCode = ?, tech_officer_reg = ?, SubmissionDate = ?, Description = ?, session_type = ?, attendance_id = ?, "
                + "approval_status = 'pending', approved_by_lecturer = NULL, approved_at = NULL "
                + "WHERE medical_id = ?";
        String attendanceSql = "UPDATE attendance SET attendance_status = 'medical', tech_officer_reg = ? WHERE attendance_id = ?";
        String medicalSql = insertSql;
        if (medicalId != null) {
            medicalSql = updateSql;
        }
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement medicalStatement = connection.prepareStatement(medicalSql);
             PreparedStatement attendanceStatement = connection.prepareStatement(attendanceSql)) {
            bindMedicalRequest(medicalStatement, request);
            if (medicalId != null) {
                medicalStatement.setInt(8, medicalId);
            }
            medicalStatement.executeUpdate();

            attendanceStatement.setString(1, request.getTechOfficerReg());
            attendanceStatement.setInt(2, request.getAttendanceId());
            attendanceStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private void bindAttendanceRequest(PreparedStatement statement, AttendanceRequest request) throws SQLException {
        statement.setString(1, request.getStudentRegNo());
        statement.setString(2, request.getCourseCode());
        statement.setString(3, request.getTechOfficerReg());
        statement.setDate(4, Date.valueOf(request.getSubmissionDate()));
        statement.setString(5, request.getSessionType());
        statement.setString(6, request.getStatus());
    }

    private void bindMedicalRequest(PreparedStatement statement, MedicalRequest request) throws SQLException {
        statement.setString(1, request.getStudentRegNo());
        statement.setString(2, request.getCourseCode());
        statement.setString(3, request.getTechOfficerReg());
        statement.setDate(4, Date.valueOf(request.getSubmissionDate()));
        statement.setString(5, request.getDescription());
        statement.setString(6, request.getSessionType());
        statement.setInt(7, request.getAttendanceId());
    }

    private int fetchCount(String sql) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private Attendance mapAttendanceRecord(ResultSet rs) throws SQLException {
        Date submissionDate = rs.getDate("SubmissionDate");
        return new Attendance(
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                submissionDate == null ? "" : submissionDate.toString(),
                safe(rs.getString("session_type")),
                safe(rs.getString("attendance_status")),
                safe(rs.getString("tech_officer_reg"))
        );
    }

    private Medical mapMedicalRecord(ResultSet rs) throws SQLException {
        Date submissionDate = rs.getDate("SubmissionDate");
        return new Medical(
                String.valueOf(rs.getInt("medical_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                submissionDate == null ? "" : submissionDate.toString(),
                safe(rs.getString("Description")),
                safe(rs.getString("session_type")),
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("approval_status")),
                safe(rs.getString("tech_officer_reg"))
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
