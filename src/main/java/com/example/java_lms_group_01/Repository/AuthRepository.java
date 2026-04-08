package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.users.UserRole;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthRepository {

    public UserRole findRoleByRegistrationNo(String registrationNo, String rawPassword) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        if (isPasswordValid(connection, "SELECT password FROM admin WHERE registrationNo = ?", registrationNo, rawPassword)) {
            return UserRole.ADMIN;
        }
        if (isPasswordValid(connection, "SELECT password FROM lecturer WHERE registrationNo = ?", registrationNo, rawPassword)) {
            return UserRole.LECTURER;
        }
        if (isPasswordValid(connection, "SELECT password FROM student WHERE registrationNo = ?", registrationNo, rawPassword)) {
            return UserRole.STUDENT;
        }
        if (isPasswordValid(connection, "SELECT password FROM tech_officer WHERE registrationNo = ?", registrationNo, rawPassword)) {
            return UserRole.TECHNICAL_OFFICER;
        }
        return null;
    }

    private boolean isPasswordValid(Connection connection, String sql, String registrationNo, String rawPassword) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                return PasswordUtil.matches(rawPassword, resultSet.getString("password"));
            }
        }
    }
}
