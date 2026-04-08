package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.UserManagementRow;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileRepository {

    private final UserImageRepository userImageRepository = new UserImageRepository();

    public UserManagementRow findStudentProfile(String registrationNo) throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.phoneNumber, u.address,
                       s.department, s.GPA, s.status
                FROM users u
                INNER JOIN student s ON s.registrationNo = u.user_id
                WHERE s.registrationNo = ?
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserManagementRow(
                        rs.getString("user_id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("phoneNumber"),
                        null,
                        null,
                        "Student",
                        rs.getString("user_id"),
                        null,
                        rs.getString("department"),
                        rs.getObject("GPA") == null ? null : ((Number) rs.getObject("GPA")).doubleValue(),
                        rs.getString("status"),
                        null,
                        userImageRepository.findImagePathByUserId(connection, registrationNo)
                );
            }
        }
    }

    public UserManagementRow findLecturerProfile(String registrationNo) throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.address, u.phoneNumber, l.department, l.position
                FROM users u
                INNER JOIN lecturer l ON l.registrationNo = u.user_id
                WHERE l.registrationNo = ?
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserManagementRow(
                        rs.getString("user_id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("phoneNumber"),
                        null,
                        null,
                        "Lecturer",
                        rs.getString("user_id"),
                        null,
                        rs.getString("department"),
                        null,
                        null,
                        rs.getString("position"),
                        userImageRepository.findImagePathByUserId(connection, registrationNo)
                );
            }
        }
    }

    public UserManagementRow findTechnicalOfficerProfile(String registrationNo) throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.phoneNumber, u.address
                FROM users u
                INNER JOIN tech_officer t ON t.registrationNo = u.user_id
                WHERE t.registrationNo = ?
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserManagementRow(
                        rs.getString("user_id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("phoneNumber"),
                        null,
                        null,
                        "TechnicalOfficer",
                        rs.getString("user_id"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        userImageRepository.findImagePathByUserId(connection, registrationNo)
                );
            }
        }
    }

    public void updateStudentProfile(String registrationNo, String email, String phone, String address, String imagePath) throws SQLException {
        String sql = "UPDATE users SET email = ?, phoneNumber = ?, address = ? WHERE user_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emptyToNull(email));
            statement.setString(2, emptyToNull(phone));
            statement.setString(3, emptyToNull(address));
            statement.setString(4, registrationNo);
            statement.executeUpdate();
            userImageRepository.upsertImagePath(connection, registrationNo, imagePath);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public void updateLecturerProfile(String registrationNo, String firstName, String lastName, String email, String address,
                                      String phone, String department, String position, String imagePath) throws SQLException {
        String userSql = "UPDATE users SET firstName = ?, lastName = ?, email = ?, address = ?, phoneNumber = ? WHERE user_id = ?";
        String lecturerSql = "UPDATE lecturer SET department = ?, position = ? WHERE registrationNo = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement userStmt = connection.prepareStatement(userSql);
             PreparedStatement lecturerStmt = connection.prepareStatement(lecturerSql)) {
            userStmt.setString(1, emptyToNull(firstName));
            userStmt.setString(2, emptyToNull(lastName));
            userStmt.setString(3, emptyToNull(email));
            userStmt.setString(4, emptyToNull(address));
            userStmt.setString(5, emptyToNull(phone));
            userStmt.setString(6, registrationNo);
            userStmt.executeUpdate();

            lecturerStmt.setString(1, emptyToNull(department));
            lecturerStmt.setString(2, emptyToNull(position));
            lecturerStmt.setString(3, registrationNo);
            lecturerStmt.executeUpdate();

            userImageRepository.upsertImagePath(connection, registrationNo, imagePath);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    public void updateTechnicalOfficerProfile(String registrationNo, String firstName, String lastName, String email,
                                              String phone, String address, String imagePath) throws SQLException {
        String sql = "UPDATE users SET firstName = ?, lastName = ?, email = ?, phoneNumber = ?, address = ? WHERE user_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, emptyToNull(firstName));
            statement.setString(2, emptyToNull(lastName));
            statement.setString(3, emptyToNull(email));
            statement.setString(4, emptyToNull(phone));
            statement.setString(5, emptyToNull(address));
            statement.setString(6, registrationNo);
            statement.executeUpdate();
            userImageRepository.upsertImagePath(connection, registrationNo, imagePath);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e instanceof SQLException ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
