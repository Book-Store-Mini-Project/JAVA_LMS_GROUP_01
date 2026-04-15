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

        // Get database connection
        Connection connection = DBConnection.getInstance().getConnection();

        // Check Admin table
        if (checkPassword(connection,
                "SELECT password FROM admin WHERE registrationNo = ?",
                registrationNo,
                rawPassword)) {
            return UserRole.ADMIN;
        }

        // Check Lecturer table
        if (checkPassword(connection,
                "SELECT password FROM lecturer WHERE registrationNo = ?",
                registrationNo,
                rawPassword)) {
            return UserRole.LECTURER;
        }

        // Check Student table
        if (checkPassword(connection,
                "SELECT password FROM student WHERE registrationNo = ?",
                registrationNo,
                rawPassword)) {
            return UserRole.STUDENT;
        }

        // Check Technical Officer table
        if (checkPassword(connection,
                "SELECT password FROM tech_officer WHERE registrationNo = ?",
                registrationNo,
                rawPassword)) {
            return UserRole.TECHNICAL_OFFICER;
        }

        // If no match found
        return null;
    }

    private boolean checkPassword(Connection connection,
                                  String sql,
                                  String registrationNo,
                                  String rawPassword) throws SQLException {

        // Prepare SQL query
        PreparedStatement statement = connection.prepareStatement(sql);

        // Set registration number in query
        statement.setString(1, registrationNo);

        // Execute query
        ResultSet resultSet = statement.executeQuery();

        // If no user found
        if (!resultSet.next()) {
            return false;
        }

        // Get password from database
        String storedPassword = resultSet.getString("password");

        if (storedPassword == null) {
            return false;
        }

        // Compare entered password with stored password
        return PasswordUtil.matches(rawPassword, storedPassword);
    }
}
