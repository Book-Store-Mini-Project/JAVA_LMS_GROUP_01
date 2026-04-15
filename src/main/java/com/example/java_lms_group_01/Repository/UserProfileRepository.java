package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.UserManagementRow;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handle user profile (student, lecturer, technical officer)
 */
public class UserProfileRepository {

    private final UserImageRepository imageRepo = new UserImageRepository();

    // =========================
    // LOAD PROFILES
    // =========================

    public UserManagementRow findStudentProfile(String regNo) throws SQLException {

        String sql = "SELECT * FROM users u INNER JOIN student s ON s.registrationNo = u.user_id WHERE s.registrationNo=?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, regNo);

        ResultSet rs = statement.executeQuery();

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
                null, null,
                "Student",
                rs.getString("user_id"),
                null,
                rs.getString("department"),
                rs.getString("batch"),
                rs.getObject("GPA") == null ? null : ((Number) rs.getObject("GPA")).doubleValue(),
                rs.getString("status"),
                null,
                imageRepo.findImagePathByUserId(connection, regNo)
        );
    }

    public UserManagementRow findLecturerProfile(String regNo) throws SQLException {

        String sql = "SELECT * FROM users u INNER JOIN lecturer l ON l.registrationNo=u.user_id WHERE l.registrationNo=?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, regNo);

        ResultSet rs = statement.executeQuery();

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
                null, null,
                "Lecturer",
                rs.getString("user_id"),
                null,
                rs.getString("department"),
                null,
                null,
                null,
                rs.getString("position"),
                imageRepo.findImagePathByUserId(connection, regNo)
        );
    }

    public UserManagementRow findTechnicalOfficerProfile(String regNo) throws SQLException {

        String sql = "SELECT * FROM users u INNER JOIN tech_officer t ON t.registrationNo=u.user_id WHERE t.registrationNo=?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, regNo);

        ResultSet rs = statement.executeQuery();

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
                null, null,
                "TechnicalOfficer",
                rs.getString("user_id"),
                null,
                null, null, null, null, null,
                imageRepo.findImagePathByUserId(connection, regNo)
        );
    }

    // =========================
    // UPDATE PROFILES
    // =========================

    public void updateStudentProfile(String regNo, String email, String phone, String address,
                                     String image, String currentPw, String newPw) throws SQLException {

        String sql = "UPDATE users SET email=?, phoneNumber=?, address=? WHERE user_id=?";

        Connection connection = DBConnection.getInstance().getConnection();
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, clean(email));
            statement.setString(2, clean(phone));
            statement.setString(3, clean(address));
            statement.setString(4, regNo);

            statement.executeUpdate();

            // update password if entered
            if (hasText(newPw)) {
                updatePassword(connection, regNo, currentPw, newPw);
            }

            imageRepo.upsertImagePath(connection, regNo, image);

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    public void updateLecturerProfile(String regNo, String first, String last, String email,
                                      String address, String phone, String dep, String pos, String image) throws SQLException {

        Connection connection = DBConnection.getInstance().getConnection();
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {

            PreparedStatement user = connection.prepareStatement(
                    "UPDATE users SET firstName=?, lastName=?, email=?, address=?, phoneNumber=? WHERE user_id=?"
            );

            user.setString(1, clean(first));
            user.setString(2, clean(last));
            user.setString(3, clean(email));
            user.setString(4, clean(address));
            user.setString(5, clean(phone));
            user.setString(6, regNo);
            user.executeUpdate();

            PreparedStatement lecturer = connection.prepareStatement(
                    "UPDATE lecturer SET department=?, position=? WHERE registrationNo=?"
            );

            lecturer.setString(1, clean(dep));
            lecturer.setString(2, clean(pos));
            lecturer.setString(3, regNo);
            lecturer.executeUpdate();

            imageRepo.upsertImagePath(connection, regNo, image);

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    public void updateTechnicalOfficerProfile(String regNo, String first, String last,
                                              String email, String phone, String address, String image) throws SQLException {

        String sql = "UPDATE users SET firstName=?, lastName=?, email=?, phoneNumber=?, address=? WHERE user_id=?";

        Connection connection = DBConnection.getInstance().getConnection();
        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, clean(first));
            statement.setString(2, clean(last));
            statement.setString(3, clean(email));
            statement.setString(4, clean(phone));
            statement.setString(5, clean(address));
            statement.setString(6, regNo);

            statement.executeUpdate();

            imageRepo.upsertImagePath(connection, regNo, image);

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    // =========================
    // HELPER METHODS
    // =========================

    private void updatePassword(Connection con, String regNo, String currentPw, String newPw) throws SQLException {

        String stored = getPassword(con, regNo);

        if (stored == null || !PasswordUtil.matches(currentPw, stored)) {
            throw new IllegalArgumentException("Wrong current password");
        }

        PreparedStatement stm = con.prepareStatement(
                "UPDATE student SET password=? WHERE registrationNo=?"
        );

        stm.setString(1, PasswordUtil.hashPassword(newPw));
        stm.setString(2, regNo);

        stm.executeUpdate();
    }

    private String getPassword(Connection con, String regNo) throws SQLException {

        PreparedStatement stm = con.prepareStatement("SELECT password FROM student WHERE registrationNo=?");
        stm.setString(1, regNo);

        ResultSet rs = stm.executeQuery();

        if (rs.next()) {
            return rs.getString("password");
        }

        return null;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private boolean hasText(String v) {
        return v != null && !v.trim().isEmpty();
    }
}
