package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.UserManagementRow;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.PasswordUtil;
import com.example.java_lms_group_01.Repository.UserImageRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private final UserImageRepository userImageRepository = new UserImageRepository();

    public List<UserManagementRow> findAdmins() throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.address, u.phoneNumber, u.dateOfBirth, u.gender,
                       'Admin' AS role, a.registrationNo, NULL AS password, NULL AS department, NULL AS GPA, NULL AS status, NULL AS position, img.image_path AS profile_image_path
                FROM users u
                INNER JOIN admin a ON a.registrationNo = u.user_id
                LEFT JOIN user_profile_images img ON img.user_id = u.user_id
                ORDER BY u.user_id DESC
                """;
        return executeQuery(sql);
    }

    public List<UserManagementRow> findLecturers() throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.address, u.phoneNumber, u.dateOfBirth, u.gender,
                       'Lecturer' AS role, l.registrationNo, NULL AS password, l.department, NULL AS GPA, NULL AS status, l.position, img.image_path AS profile_image_path
                FROM users u
                INNER JOIN lecturer l ON l.registrationNo = u.user_id
                LEFT JOIN user_profile_images img ON img.user_id = u.user_id
                ORDER BY u.user_id DESC
                """;
        return executeQuery(sql);
    }

    public List<UserManagementRow> findStudents() throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.address, u.phoneNumber, u.dateOfBirth, u.gender,
                       'Student' AS role, s.registrationNo, NULL AS password, s.department, s.GPA, s.status, NULL AS position, img.image_path AS profile_image_path
                FROM users u
                INNER JOIN student s ON s.registrationNo = u.user_id
                LEFT JOIN user_profile_images img ON img.user_id = u.user_id
                ORDER BY u.user_id DESC
                """;
        return executeQuery(sql);
    }

    public List<UserManagementRow> findTechnicalOfficers() throws SQLException {
        String sql = """
                SELECT u.user_id, u.firstName, u.lastName, u.email, u.address, u.phoneNumber, u.dateOfBirth, u.gender,
                       'TechnicalOfficer' AS role, t.registrationNo, NULL AS password, NULL AS department, NULL AS GPA, NULL AS status, NULL AS position, img.image_path AS profile_image_path
                FROM users u
                INNER JOIN tech_officer t ON t.registrationNo = u.user_id
                LEFT JOIN user_profile_images img ON img.user_id = u.user_id
                ORDER BY u.user_id DESC
                """;
        return executeQuery(sql);
    }

    public boolean createAdmin(UserManagementRow row) throws SQLException {
        String roleSql = "INSERT INTO admin (registrationNo, password) VALUES (?, ?)";
        return createWithRole(row, roleSql, stmt -> {
            stmt.setString(1, requiredText(row.getRegistrationNo(), "Registration No"));
            stmt.setString(2, hashRequiredPassword(row.getPassword()));
        });
    }

    public boolean createLecturer(UserManagementRow row) throws SQLException {
        String roleSql = "INSERT INTO lecturer (registrationNo, password, department, position) VALUES (?, ?, ?, ?)";
        return createWithRole(row, roleSql, stmt -> {
            stmt.setString(1, requiredText(row.getRegistrationNo(), "Registration No"));
            stmt.setString(2, hashRequiredPassword(row.getPassword()));
            stmt.setString(3, requiredText(row.getDepartment(), "Department"));
            stmt.setString(4, requiredText(row.getPosition(), "Position"));
        });
    }

    public boolean createStudent(UserManagementRow row) throws SQLException {
        String roleSql = "INSERT INTO student (registrationNo, password, department, GPA, status) VALUES (?, ?, ?, ?, ?)";
        return createWithRole(row, roleSql, stmt -> {
            stmt.setString(1, requiredText(row.getRegistrationNo(), "Registration No"));
            stmt.setString(2, hashRequiredPassword(row.getPassword()));
            stmt.setString(3, requiredText(row.getDepartment(), "Department"));
            if (row.getGpa() == null) {
                stmt.setNull(4, Types.DECIMAL);
            } else {
                stmt.setDouble(4, row.getGpa());
            }
            stmt.setString(5, requiredText(row.getStatus(), "Status"));
        });
    }

    public boolean createTechnicalOfficer(UserManagementRow row) throws SQLException {
        String roleSql = "INSERT INTO tech_officer (registrationNo, password) VALUES (?, ?)";
        return createWithRole(row, roleSql, stmt -> {
            stmt.setString(1, requiredText(row.getRegistrationNo(), "Registration No"));
            stmt.setString(2, hashRequiredPassword(row.getPassword()));
        });
    }

    public boolean updateAdmin(UserManagementRow row) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            updateUser(connection, row);

            if (row.getPassword() != null && !row.getPassword().trim().isEmpty()) {
                try (PreparedStatement stmt =
                             connection.prepareStatement("UPDATE admin SET password = ? WHERE registrationNo = ?")) {
                    stmt.setString(1, PasswordUtil.hashPassword(row.getPassword().trim()));
                    stmt.setString(2, requiredText(row.getRegistrationNo(), "Registration No"));
                    stmt.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean updateLecturer(UserManagementRow row) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            updateUser(connection, row);

            String roleSql = row.getPassword() == null || row.getPassword().trim().isEmpty()
                    ? "UPDATE lecturer SET department = ?, position = ? WHERE registrationNo = ?"
                    : "UPDATE lecturer SET password = ?, department = ?, position = ? WHERE registrationNo = ?";

            try (PreparedStatement stmt = connection.prepareStatement(roleSql)) {
                if (row.getPassword() == null || row.getPassword().trim().isEmpty()) {
                    stmt.setString(1, requiredText(row.getDepartment(), "Department"));
                    stmt.setString(2, requiredText(row.getPosition(), "Position"));
                    stmt.setString(3, requiredText(row.getRegistrationNo(), "Registration No"));
                } else {
                    stmt.setString(1, PasswordUtil.hashPassword(row.getPassword().trim()));
                    stmt.setString(2, requiredText(row.getDepartment(), "Department"));
                    stmt.setString(3, requiredText(row.getPosition(), "Position"));
                    stmt.setString(4, requiredText(row.getRegistrationNo(), "Registration No"));
                }
                stmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean updateStudent(UserManagementRow row) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            updateUser(connection, row);

            String roleSql = row.getPassword() == null || row.getPassword().trim().isEmpty()
                    ? "UPDATE student SET department = ?, GPA = ?, status = ? WHERE registrationNo = ?"
                    : "UPDATE student SET password = ?, department = ?, GPA = ?, status = ? WHERE registrationNo = ?";

            try (PreparedStatement stmt = connection.prepareStatement(roleSql)) {
                if (row.getPassword() == null || row.getPassword().trim().isEmpty()) {
                    stmt.setString(1, requiredText(row.getDepartment(), "Department"));
                    if (row.getGpa() == null) {
                        stmt.setNull(2, Types.DECIMAL);
                    } else {
                        stmt.setDouble(2, row.getGpa());
                    }
                    stmt.setString(3, requiredText(row.getStatus(), "Status"));
                    stmt.setString(4, requiredText(row.getRegistrationNo(), "Registration No"));
                } else {
                    stmt.setString(1, PasswordUtil.hashPassword(row.getPassword().trim()));
                    stmt.setString(2, requiredText(row.getDepartment(), "Department"));
                    if (row.getGpa() == null) {
                        stmt.setNull(3, Types.DECIMAL);
                    } else {
                        stmt.setDouble(3, row.getGpa());
                    }
                    stmt.setString(4, requiredText(row.getStatus(), "Status"));
                    stmt.setString(5, requiredText(row.getRegistrationNo(), "Registration No"));
                }
                stmt.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean updateTechnicalOfficer(UserManagementRow row) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            updateUser(connection, row);

            if (row.getPassword() != null && !row.getPassword().trim().isEmpty()) {
                try (PreparedStatement stmt =
                             connection.prepareStatement("UPDATE tech_officer SET password = ? WHERE registrationNo = ?")) {
                    stmt.setString(1, PasswordUtil.hashPassword(row.getPassword().trim()));
                    stmt.setString(2, requiredText(row.getRegistrationNo(), "Registration No"));
                    stmt.executeUpdate();
                }
            }

            connection.commit();
            return true;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public boolean deleteAdmin(String userId) throws SQLException {
        return deleteWithRole(userId, "DELETE FROM admin WHERE registrationNo = ?");
    }

    public boolean deleteLecturer(String userId) throws SQLException {
        return deleteWithRole(userId, "DELETE FROM lecturer WHERE registrationNo = ?");
    }

    public boolean deleteStudent(String userId) throws SQLException {
        return deleteWithRole(userId, "DELETE FROM student WHERE registrationNo = ?");
    }

    public boolean deleteTechnicalOfficer(String userId) throws SQLException {
        return deleteWithRole(userId, "DELETE FROM tech_officer WHERE registrationNo = ?");
    }

    private boolean createWithRole(UserManagementRow row, String roleInsertSql, RoleStatementWriter writer) throws SQLException {
        String userSql = "INSERT INTO users (user_id, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement userStmt = connection.prepareStatement(userSql)) {
                fillUserStatement(userStmt, row, false);
                userStmt.executeUpdate();
            }

            try (PreparedStatement roleStmt = connection.prepareStatement(roleInsertSql)) {
                writer.write(roleStmt);
                roleStmt.executeUpdate();
            }

            userImageRepository.upsertImagePath(connection, requiredText(row.getRegistrationNo(), "Registration No"), row.getProfileImagePath());

            connection.commit();
            return true;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private boolean deleteWithRole(String userId, String roleDeleteSql) throws SQLException {
        String userSql = "DELETE FROM users WHERE user_id = ?";

        Connection connection = DBConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement roleStmt = connection.prepareStatement(roleDeleteSql)) {
                roleStmt.setString(1, userId);
                roleStmt.executeUpdate();
            }

            int affected;
            try (PreparedStatement userStmt = connection.prepareStatement(userSql)) {
                userStmt.setString(1, userId);
                affected = userStmt.executeUpdate();
            }

            connection.commit();
            return affected > 0;
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void updateUser(Connection connection, UserManagementRow row) throws SQLException {
        String userSql = "UPDATE users SET firstName = ?, lastName = ?, email = ?, address = ?, phoneNumber = ?, dateOfBirth = ?, gender = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(userSql)) {
            fillUserStatement(stmt, row, true);
            stmt.executeUpdate();
        }
        userImageRepository.upsertImagePath(connection, userId(row), row.getProfileImagePath());
    }

    private void fillUserStatement(PreparedStatement stmt, UserManagementRow row, boolean includeUserIdForWhere) throws SQLException {
        String userId = requiredText(row.getRegistrationNo(), "Registration No");
        if (!includeUserIdForWhere) {
            stmt.setString(1, userId);
            stmt.setString(2, requiredText(row.getFirstName(), "First name"));
            stmt.setString(3, requiredText(row.getLastName(), "Last name"));
            stmt.setString(4, requiredText(row.getEmail(), "Email"));
            stmt.setString(5, emptyToNull(row.getAddress()));
            stmt.setString(6, emptyToNull(row.getPhoneNumber()));
            if (row.getDateOfBirth() == null) {
                stmt.setNull(7, Types.DATE);
            } else {
                stmt.setDate(7, Date.valueOf(row.getDateOfBirth()));
            }
            stmt.setString(8, emptyToNull(row.getGender()));
            return;
        }

        stmt.setString(1, requiredText(row.getFirstName(), "First name"));
        stmt.setString(2, requiredText(row.getLastName(), "Last name"));
        stmt.setString(3, requiredText(row.getEmail(), "Email"));
        stmt.setString(4, emptyToNull(row.getAddress()));
        stmt.setString(5, emptyToNull(row.getPhoneNumber()));
        if (row.getDateOfBirth() == null) {
            stmt.setNull(6, Types.DATE);
        } else {
            stmt.setDate(6, Date.valueOf(row.getDateOfBirth()));
        }
        stmt.setString(7, emptyToNull(row.getGender()));
        stmt.setString(8, userId);
    }

    private List<UserManagementRow> executeQuery(String sql) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<UserManagementRow> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(mapRow(rs));
            }
            return rows;
        }
    }

    private UserManagementRow mapRow(ResultSet rs) throws SQLException {
        Date dob = rs.getDate("dateOfBirth");
        Object gpaValue = rs.getObject("GPA");

        return new UserManagementRow(
                rs.getString("user_id"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("phoneNumber"),
                dob == null ? null : dob.toLocalDate(),
                rs.getString("gender"),
                rs.getString("role"),
                rs.getString("registrationNo"),
                rs.getString("password"),
                rs.getString("department"),
                gpaValue == null ? null : ((Number) gpaValue).doubleValue(),
                rs.getString("status"),
                rs.getString("position"),
                rs.getString("profile_image_path")
        );
    }

    private String requiredText(String text, String field) {
        if (text == null || text.trim().isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return text.trim();
    }

    private String emptyToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String hashRequiredPassword(String rawPassword) {
        return PasswordUtil.hashPassword(requiredText(rawPassword, "Password"));
    }

    private String userId(UserManagementRow row) {
        return requiredText(row.getRegistrationNo(), "Registration No");
    }

    @FunctionalInterface
    private interface RoleStatementWriter {
        void write(PreparedStatement stmt) throws SQLException;
    }
}
