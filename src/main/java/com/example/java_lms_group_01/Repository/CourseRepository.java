package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    private static final String BASE_SELECT = "SELECT courseCode, name, lecturerRegistrationNo, department, semester, credit, course_type FROM course";

    public List<Course> findByFilters(String department, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (department != null && !department.isBlank()) {
            sql.append(" AND department = ?");
            params.add(department);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (courseCode LIKE ? OR name LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY courseCode");

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

    public List<String> findAllDepartments() throws SQLException {
        String sql = "SELECT DISTINCT department FROM course WHERE department IS NOT NULL AND department <> '' ORDER BY department";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            List<String> departments = new ArrayList<>();
            while (rs.next()) {
                departments.add(rs.getString("department"));
            }
            return departments;
        }
    }

    public boolean save(Course course) throws SQLException {
        String sql = "INSERT INTO course (courseCode, name, lecturerRegistrationNo, department, semester, credit, course_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCourse(statement, course, false);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE course SET name = ?, lecturerRegistrationNo = ?, department = ?, semester = ?, credit = ?, course_type = ? WHERE courseCode = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCourse(statement, course, true);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteByCourseCode(String courseCode) throws SQLException {
        String sql = "DELETE FROM course WHERE courseCode = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseCode);
            return statement.executeUpdate() > 0;
        }
    }

    private void bindCourse(PreparedStatement statement, Course course, boolean forUpdate) throws SQLException {
        if (!forUpdate) {
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getName());
            if (course.getLecturerRegistrationNo() == null || course.getLecturerRegistrationNo().isBlank()) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, course.getLecturerRegistrationNo());
            }
            if (course.getDepartment() == null || course.getDepartment().isBlank()) {
                statement.setNull(4, Types.VARCHAR);
            } else {
                statement.setString(4, course.getDepartment());
            }
            if (course.getSemester() == null || course.getSemester().isBlank()) {
                statement.setNull(5, Types.VARCHAR);
            } else {
                statement.setString(5, course.getSemester());
            }
            statement.setInt(6, course.getCredit());
            statement.setString(7, course.getCourseType());
            return;
        }

        statement.setString(1, course.getName());
        if (course.getLecturerRegistrationNo() == null || course.getLecturerRegistrationNo().isBlank()) {
            statement.setNull(2, Types.VARCHAR);
        } else {
            statement.setString(2, course.getLecturerRegistrationNo());
        }
        if (course.getDepartment() == null || course.getDepartment().isBlank()) {
            statement.setNull(3, Types.VARCHAR);
        } else {
            statement.setString(3, course.getDepartment());
        }
        if (course.getSemester() == null || course.getSemester().isBlank()) {
            statement.setNull(4, Types.VARCHAR);
        } else {
            statement.setString(4, course.getSemester());
        }
        statement.setInt(5, course.getCredit());
        statement.setString(6, course.getCourseType());
        statement.setString(7, course.getCourseCode());
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getString("courseCode"),
                rs.getString("name"),
                rs.getString("lecturerRegistrationNo"),
                rs.getString("department"),
                rs.getString("semester"),
                rs.getInt("credit"),
                rs.getString("course_type")
        );
    }
}
