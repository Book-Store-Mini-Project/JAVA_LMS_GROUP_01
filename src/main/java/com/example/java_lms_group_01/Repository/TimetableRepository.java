package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TimetableRepository {

    private static final String BASE_SELECT =
            "SELECT time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type FROM timeTable";

    public List<Timetable> findByFilters(String department, String day, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (department != null && !department.isBlank()) {
            sql.append(" AND department = ?");
            params.add(department);
        }

        if (day != null && !day.isBlank()) {
            sql.append(" AND day = ?");
            params.add(day);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (time_table_id LIKE ? OR courseCode LIKE ? OR lec_id LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY time_table_id DESC");

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<Timetable> timetables = new ArrayList<>();
                while (rs.next()) {
                    timetables.add(mapRow(rs));
                }
                return timetables;
            }
        }
    }

    public List<String> findAllDepartments() throws SQLException {
        String sql = "SELECT DISTINCT department FROM timeTable ORDER BY department";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<String> departments = new ArrayList<>();
            while (rs.next()) {
                String department = rs.getString("department");
                if (department != null && !department.isBlank()) {
                    departments.add(department);
                }
            }
            return departments;
        }
    }

    public List<String> findAllDays() throws SQLException {
        String sql = "SELECT DISTINCT day FROM timeTable ORDER BY day";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<String> days = new ArrayList<>();
            while (rs.next()) {
                String day = rs.getString("day");
                if (day != null && !day.isBlank()) {
                    days.add(day);
                }
            }
            return days;
        }
    }

    public boolean save(Timetable timetable) throws SQLException {
        String sql = "INSERT INTO timeTable (time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindTimetable(statement, timetable, false);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Timetable timetable) throws SQLException {
        String sql = "UPDATE timeTable SET department = ?, lec_id = ?, courseCode = ?, admin_id = ?, day = ?, start_time = ?, end_time = ?, session_type = ? WHERE time_table_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindTimetable(statement, timetable, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(String timetableId) throws SQLException {
        String sql = "DELETE FROM timeTable WHERE time_table_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, timetableId);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindTimetable(PreparedStatement statement, Timetable timetable, boolean forUpdate) throws SQLException {
        if (!forUpdate) {
            statement.setString(1, timetable.getTimeTableId());
            statement.setString(2, timetable.getDepartment());
            if (timetable.getLecId() == null || timetable.getLecId().isBlank()) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, timetable.getLecId());
            }
            if (timetable.getCourseCode() == null || timetable.getCourseCode().isBlank()) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, timetable.getCourseCode());
            }
            if (timetable.getAdminId() == null || timetable.getAdminId().isBlank()) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, timetable.getAdminId());
            }
            statement.setString(6, timetable.getDay());
            if (timetable.getStartTime() == null) {
                statement.setNull(7, Types.TIME);
            } else {
                statement.setTime(7, Time.valueOf(timetable.getStartTime()));
            }
            if (timetable.getEndTime() == null) {
                statement.setNull(8, Types.TIME);
            } else {
                statement.setTime(8, Time.valueOf(timetable.getEndTime()));
            }
            statement.setString(9, timetable.getSessionType());
            return;
        }

        statement.setString(1, timetable.getDepartment());
        if (timetable.getLecId() == null || timetable.getLecId().isBlank()) {
            statement.setNull(2, Types.VARCHAR);
        } else {
            statement.setString(2, timetable.getLecId());
        }
        if (timetable.getCourseCode() == null || timetable.getCourseCode().isBlank()) {
            statement.setNull(3, Types.VARCHAR);
        } else {
            statement.setString(3, timetable.getCourseCode());
        }
        if (timetable.getAdminId() == null || timetable.getAdminId().isBlank()) {
            statement.setNull(4, Types.VARCHAR);
        } else {
            statement.setString(4, timetable.getAdminId());
        }
        statement.setString(5, timetable.getDay());
        if (timetable.getStartTime() == null) {
            statement.setNull(6, Types.TIME);
        } else {
            statement.setTime(6, Time.valueOf(timetable.getStartTime()));
        }
        if (timetable.getEndTime() == null) {
            statement.setNull(7, Types.TIME);
        } else {
            statement.setTime(7, Time.valueOf(timetable.getEndTime()));
        }
        statement.setString(8, timetable.getSessionType());
        statement.setString(9, timetable.getTimeTableId());
    }

    private Timetable mapRow(ResultSet rs) throws SQLException {
        Time start = rs.getTime("start_time");
        Time end = rs.getTime("end_time");

        return new Timetable(
                rs.getString("time_table_id"),
                rs.getString("department"),
                rs.getString("lec_id"),
                rs.getString("courseCode"),
                rs.getString("admin_id"),
                rs.getString("day"),
                start == null ? null : start.toLocalTime(),
                end == null ? null : end.toLocalTime(),
                rs.getString("session_type")
        );
    }
}
