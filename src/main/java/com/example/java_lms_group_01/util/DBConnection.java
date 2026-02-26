package com.example.java_lms_group_01.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {


    private static DBConnection instance;

    private Connection connection;


    private DBConnection() throws SQLException {
        try {
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/lms", "root", "2003"
            );
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
}