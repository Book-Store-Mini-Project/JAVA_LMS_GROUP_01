package com.example.java_lms_group_01.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple singleton used to keep one database connection for the application.
 * Every repository calls this class before running SQL.
 */
public class DBConnection {
    private static DBConnection instance;
    private Connection connection;

    private DBConnection() throws SQLException {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lms02", "root", "2003");
            ensureStudentBatchColumn();
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database!");
            throw e;
        }
    }

    public static DBConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void ensureStudentBatchColumn() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "student", "batch");
        if (columns.next()) {
            columns.close();
            return;
        }
        columns.close();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE student ADD COLUMN batch VARCHAR(50)");
        }
    }
}
