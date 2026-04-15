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

/**
 * This class handles notice table database operations.
 */
public class NoticeRepository {

    // Base select query
    private static final String BASE_SELECT =
            "SELECT notice_id, notice_title, notice_content, publishDate, createdBy FROM notice";

    /**
     * Get all notices from database
     */
    public List<Notice> findAll() throws SQLException {

        String sql = BASE_SELECT + " ORDER BY publishDate DESC, notice_id DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();

        List<Notice> noticeList = new ArrayList<>();

        while (rs.next()) {
            Notice notice = mapRow(rs);
            noticeList.add(notice);
        }

        return noticeList;
    }

    /**
     * Search notices using keyword
     */
    public List<Notice> findByKeyword(String keyword) throws SQLException {

        String sql = BASE_SELECT
                + " WHERE (? IS NULL OR ? = '' OR notice_title LIKE ? OR notice_content LIKE ?)"
                + " ORDER BY publishDate DESC, notice_id DESC";

        String safeKeyword = "";
        if (keyword != null) {
            safeKeyword = keyword.trim();
        }

        String pattern = "%" + safeKeyword + "%";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, safeKeyword);
        statement.setString(2, safeKeyword);
        statement.setString(3, pattern);
        statement.setString(4, pattern);

        ResultSet rs = statement.executeQuery();

        List<Notice> noticeList = new ArrayList<>();

        while (rs.next()) {
            Notice notice = mapRow(rs);
            noticeList.add(notice);
        }

        return noticeList;
    }

    /**
     * Save new notice
     */
    public boolean save(Notice notice) throws SQLException {

        String sql = "INSERT INTO notice (notice_title, notice_content, publishDate, createdBy) VALUES (?, ?, ?, ?)";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        setNoticeData(statement, notice);

        boolean inserted = statement.executeUpdate() > 0;

        if (!inserted) {
            return false;
        }

        ResultSet keys = statement.getGeneratedKeys();

        if (keys.next()) {
            notice.setNoticeId(keys.getInt(1));
        }

        return true;
    }

    /**
     * Update existing notice
     */
    public boolean update(Notice notice) throws SQLException {

        String sql = "UPDATE notice SET notice_title = ?, notice_content = ?, publishDate = ?, createdBy = ? WHERE notice_id = ?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        setNoticeData(statement, notice);
        statement.setInt(5, notice.getNoticeId());

        return statement.executeUpdate() > 0;
    }

    /**
     * Delete notice using ID
     */
    public boolean deleteById(int noticeId) throws SQLException {

        String sql = "DELETE FROM notice WHERE notice_id = ?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setInt(1, noticeId);

        return statement.executeUpdate() > 0;
    }

    /**
     * Convert one database row into Notice object
     */
    private Notice mapRow(ResultSet rs) throws SQLException {

        Date publishDate = rs.getDate("publishDate");

        if (publishDate == null) {
            return new Notice(
                    rs.getInt("notice_id"),
                    rs.getString("notice_title"),
                    rs.getString("notice_content"),
                    null,
                    rs.getString("createdBy")
            );
        }

        return new Notice(
                rs.getInt("notice_id"),
                rs.getString("notice_title"),
                rs.getString("notice_content"),
                publishDate.toLocalDate(),
                rs.getString("createdBy")
        );
    }

    private void setNoticeData(PreparedStatement statement, Notice notice) throws SQLException {
        statement.setString(1, notice.getTitle());
        statement.setString(2, notice.getContent());

        if (notice.getPublishDate() == null) {
            statement.setNull(3, java.sql.Types.DATE);
        } else {
            statement.setDate(3, Date.valueOf(notice.getPublishDate()));
        }

        statement.setString(4, notice.getCreatedBy());
    }
}
