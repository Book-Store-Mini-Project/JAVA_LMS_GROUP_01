package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    private static final String BASE_SELECT = "SELECT course_code, name, credits, has_theory, has_practical, " +
            "lecturer_id, dept_id, semester FROM course";

    public List<Course> findAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY course_code";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<Course> courses = new ArrayList<>();
            while (rs.next()) {
                courses.add(mapRow(rs));
            }
            return courses;
        }
    }

    public List<Course> findByFilters(Integer deptId, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (deptId != null) {
            sql.append(" AND dept_id = ?");
            params.add(deptId);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (course_code LIKE ? OR name LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY course_code");

        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                List<Course> courses = new ArrayList<>();
                while (rs.next()) {
                    courses.add(mapRow(rs));
                }
                return courses;
            }
        }
    }

    public List<Integer> findAllDepartmentIds() throws SQLException {
        String sql = "SELECT DISTINCT dept_id FROM course ORDER BY dept_id";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<Integer> departmentIds = new ArrayList<>();
            while (rs.next()) {
                departmentIds.add(rs.getInt("dept_id"));
            }
            return departmentIds;
        }
    }

    public boolean save(Course course) throws SQLException {
        String sql = "INSERT INTO course (course_code, name, credits, has_theory, has_practical, lecturer_id, dept_id, semester) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getName());
            statement.setInt(3, course.getCredits());
            statement.setBoolean(4, course.isHasTheory());
            statement.setBoolean(5, course.isHasPractical());
            statement.setInt(6, course.getLecturerId());
            statement.setInt(7, course.getDeptId());
            statement.setString(8, course.getSemester());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE course SET name = ?, credits = ?, has_theory = ?, has_practical = ?, lecturer_id = ?, dept_id = ?, semester = ? " +
                "WHERE course_code = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getName());
            statement.setInt(2, course.getCredits());
            statement.setBoolean(3, course.isHasTheory());
            statement.setBoolean(4, course.isHasPractical());
            statement.setInt(5, course.getLecturerId());
            statement.setInt(6, course.getDeptId());
            statement.setString(7, course.getSemester());
            statement.setString(8, course.getCourseCode());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteByCourseCode(String courseCode) throws SQLException {
        String sql = "DELETE FROM course WHERE course_code = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseCode);
            return statement.executeUpdate() > 0;
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getString("course_code"),
                rs.getString("name"),
                rs.getInt("credits"),
                rs.getBoolean("has_theory"),
                rs.getBoolean("has_practical"),
                rs.getInt("lecturer_id"),
                rs.getInt("dept_id"),
                rs.getString("semester")
        );
    }
}
