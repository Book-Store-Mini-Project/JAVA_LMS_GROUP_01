package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Notice;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NoticeRepository {

    private static final String BASE_SELECT =
            "SELECT notice_id, notice_title, notice_content, publishDate, createdBy FROM notice";

    public List<Notice> findAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY publishDate DESC, notice_id DESC";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<Notice> notices = new ArrayList<>();
            while (rs.next()) {
                notices.add(mapRow(rs));
            }
            return notices;
        }
    }

    public List<Notice> findByKeyword(String keyword) throws SQLException {
        String sql = BASE_SELECT +
                " WHERE (? IS NULL OR ? = '' OR notice_title LIKE ? OR notice_content LIKE ?)" +
                " ORDER BY publishDate DESC, notice_id DESC";

        String safeKeyword = keyword == null ? "" : keyword.trim();
        String pattern = "%" + safeKeyword + "%";

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safeKeyword);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                List<Notice> notices = new ArrayList<>();
                while (rs.next()) {
                    notices.add(mapRow(rs));
                }
                return notices;
            }
        }
    }

    public boolean save(Notice notice) throws SQLException {
        String sql = "INSERT INTO notice (notice_title, notice_content, publishDate, createdBy) VALUES (?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            if (notice.getPublishDate() == null) {
                statement.setNull(3, java.sql.Types.DATE);
            } else {
                statement.setDate(3, Date.valueOf(notice.getPublishDate()));
            }
            statement.setString(4, notice.getCreatedBy());

            boolean inserted = statement.executeUpdate() > 0;
            if (!inserted) {
                return false;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    notice.setNoticeId(keys.getInt(1));
                }
            }
            return true;
        }
    }

    public boolean update(Notice notice) throws SQLException {
        String sql = "UPDATE notice SET notice_title = ?, notice_content = ?, publishDate = ?, createdBy = ? WHERE notice_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getContent());
            if (notice.getPublishDate() == null) {
                statement.setNull(3, java.sql.Types.DATE);
            } else {
                statement.setDate(3, Date.valueOf(notice.getPublishDate()));
            }
            statement.setString(4, notice.getCreatedBy());
            statement.setInt(5, notice.getNoticeId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(int noticeId) throws SQLException {
        String sql = "DELETE FROM notice WHERE notice_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, noticeId);
            return statement.executeUpdate() > 0;
        }
    }

    private Notice mapRow(ResultSet rs) throws SQLException {
        Date publishDate = rs.getDate("publishDate");
        return new Notice(
                rs.getInt("notice_id"),
                rs.getString("notice_title"),
                rs.getString("notice_content"),
                publishDate == null ? null : publishDate.toLocalDate(),
                rs.getString("createdBy")
        );
    }
}
