package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles timetable database operations
 */
public class TimetableRepository {

    // =========================
    // GET TIMETABLE (WITH FILTERS)
    // =========================

    public List<Timetable> findByFilters(String department, String day, String keyword) throws SQLException {

        String sql = "SELECT * FROM timetable WHERE 1=1";

        List<String> params = new ArrayList<>();

        // Filter by department
        if (department != null && !department.isEmpty()) {
            sql += " AND department = ?";
            params.add(department);
        }

        // Filter by day
        if (day != null && !day.isEmpty()) {
            sql += " AND day = ?";
            params.add(day);
        }

        // Filter by keyword
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (time_table_id LIKE ? OR courseCode LIKE ? OR lec_id LIKE ?)";

            String pattern = "%" + keyword + "%";

            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql += " ORDER BY time_table_id DESC";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        // Set parameters
        for (int i = 0; i < params.size(); i++) {
            stm.setString(i + 1, params.get(i));
        }

        ResultSet rs = stm.executeQuery();

        List<Timetable> list = new ArrayList<>();

        while (rs.next()) {
            list.add(mapRow(rs));
        }

        return list;
    }

    // =========================
    // GET ALL DEPARTMENTS
    // =========================

    public List<String> findAllDepartments() throws SQLException {

        String sql = "SELECT DISTINCT department FROM timetable ORDER BY department";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        ResultSet rs = stm.executeQuery();

        List<String> list = new ArrayList<>();

        while (rs.next()) {
            String dep = rs.getString("department");

            if (dep != null && !dep.isEmpty()) {
                list.add(dep);
            }
        }

        return list;
    }

    // =========================
    // GET ALL DAYS
    // =========================

    public List<String> findAllDays() throws SQLException {

        String sql = "SELECT DISTINCT day FROM timetable ORDER BY day";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        ResultSet rs = stm.executeQuery();

        List<String> list = new ArrayList<>();

        while (rs.next()) {
            String day = rs.getString("day");

            if (day != null && !day.isEmpty()) {
                list.add(day);
            }
        }

        return list;
    }

    // =========================
    // SAVE TIMETABLE
    // =========================

    public boolean save(Timetable t) throws SQLException {

        String sql = "INSERT INTO timetable VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        setData(stm, t, false);

        return stm.executeUpdate() > 0;
    }

    // =========================
    // UPDATE TIMETABLE
    // =========================

    public boolean update(Timetable t) throws SQLException {

        String sql = "UPDATE timetable SET department=?, lec_id=?, courseCode=?, admin_id=?, day=?, start_time=?, end_time=?, session_type=? WHERE time_table_id=?";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        setData(stm, t, true);

        return stm.executeUpdate() > 0;
    }

    // =========================
    // DELETE TIMETABLE
    // =========================

    public boolean deleteById(String id) throws SQLException {

        String sql = "DELETE FROM timetable WHERE time_table_id=?";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        stm.setString(1, id);

        return stm.executeUpdate() > 0;
    }

    // =========================
    // HELPER METHODS
    // =========================

    private void setData(PreparedStatement stm, Timetable t, boolean update) throws SQLException {

        if (!update) {
            stm.setString(1, t.getTimeTableId());
            stm.setString(2, t.getDepartment());
            stm.setString(3, t.getLecId());
            stm.setString(4, t.getCourseCode());
            stm.setString(5, t.getAdminId());
            stm.setString(6, t.getDay());
            stm.setTime(7, t.getStartTime() == null ? null : Time.valueOf(t.getStartTime()));
            stm.setTime(8, t.getEndTime() == null ? null : Time.valueOf(t.getEndTime()));
            stm.setString(9, t.getSessionType());
        } else {
            stm.setString(1, t.getDepartment());
            stm.setString(2, t.getLecId());
            stm.setString(3, t.getCourseCode());
            stm.setString(4, t.getAdminId());
            stm.setString(5, t.getDay());
            stm.setTime(6, t.getStartTime() == null ? null : Time.valueOf(t.getStartTime()));
            stm.setTime(7, t.getEndTime() == null ? null : Time.valueOf(t.getEndTime()));
            stm.setString(8, t.getSessionType());
            stm.setString(9, t.getTimeTableId());
        }
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
