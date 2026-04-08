package com.example.java_lms_group_01.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class UserImageRepository {

    private UserImageRepository() {
    }

    public static String findImagePathByUserId(Connection connection, String userId) throws SQLException {
        String sql = "SELECT image_path FROM user_profile_images WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image_path");
                }
                return null;
            }
        }
    }

    public static void upsertImagePath(Connection connection, String userId, String imagePath) throws SQLException {
        if (imagePath == null || imagePath.trim().isBlank()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM user_profile_images WHERE user_id = ?")) {
                statement.setString(1, userId);
                statement.executeUpdate();
            }
            return;
        }

        String sql = """
                INSERT INTO user_profile_images (user_id, image_path)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE image_path = VALUES(image_path)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            statement.setString(2, imagePath.trim());
            statement.executeUpdate();
        }
    }
}
