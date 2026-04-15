package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all database operations related to Course.
 */
public class CourseRepository {

    // Base query
    private static final String BASE_SELECT =
            "SELECT courseCode, name, lecturerRegistrationNo, department, semester, credit, course_type FROM course";

    /**
     * Get courses with optional filters (department + search keyword)
     */
    public List<Course> findByFilters(String department, String keyword) throws SQLException {

        // Build SQL query step by step
        String sql = BASE_SELECT + " WHERE 1=1";

        List<String> params = new ArrayList<>();

        // Filter by department
        if (department != null && !department.isEmpty()) {
            sql += " AND department = ?";
            params.add(department);
        }

        // Filter by keyword (course code or name)
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (courseCode LIKE ? OR name LIKE ?)";
            String pattern = "%" + keyword + "%";
            params.add(pattern);
            params.add(pattern);
        }

        sql += " ORDER BY courseCode";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        // Set parameters
        for (int i = 0; i < params.size(); i++) {
            statement.setString(i + 1, params.get(i));
        }

        ResultSet rs = statement.executeQuery();

        List<Course> courseList = new ArrayList<>();

        // Read data row by row
        while (rs.next()) {
            courseList.add(createCourseFromResult(rs));
        }

        return courseList;
    }

    /**
     * Get all departments
     */
    public List<String> findAllDepartments() throws SQLException {

        String sql = "SELECT DISTINCT department FROM course WHERE department IS NOT NULL AND department <> '' ORDER BY department";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();

        List<String> departments = new ArrayList<>();

        while (rs.next()) {
            departments.add(rs.getString("department"));
        }

        return departments;
    }

    /**
     * Save new course
     */
    public boolean save(Course course) throws SQLException {

        String sql = "INSERT INTO course VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        setCourseData(statement, course, false);

        return statement.executeUpdate() > 0;
    }

    /**
     * Update existing course
     */
    public boolean update(Course course) throws SQLException {

        String sql = "UPDATE course SET name=?, lecturerRegistrationNo=?, department=?, semester=?, credit=?, course_type=? WHERE courseCode=?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        setCourseData(statement, course, true);

        return statement.executeUpdate() > 0;
    }

    /**
     * Delete course by course code
     */
    public boolean deleteByCourseCode(String courseCode) throws SQLException {

        String sql = "DELETE FROM course WHERE courseCode=?";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, courseCode);

        return statement.executeUpdate() > 0;
    }

    /**
     * Set course data into PreparedStatement
     */
    private void setCourseData(PreparedStatement statement, Course course, boolean isUpdate) throws SQLException {

        if (!isUpdate) {
            // INSERT
            statement.setString(1, course.getCourseCode());
            statement.setString(2, course.getName());
            statement.setString(3, course.getLecturerRegistrationNo());
            statement.setString(4, course.getDepartment());
            statement.setString(5, course.getSemester());
            statement.setInt(6, course.getCredit());
            statement.setString(7, course.getCourseType());
        } else {
            // UPDATE
            statement.setString(1, course.getName());
            statement.setString(2, course.getLecturerRegistrationNo());
            statement.setString(3, course.getDepartment());
            statement.setString(4, course.getSemester());
            statement.setInt(5, course.getCredit());
            statement.setString(6, course.getCourseType());
            statement.setString(7, course.getCourseCode());
        }
    }

    /**
     * Convert ResultSet row into Course object
     */
    private Course createCourseFromResult(ResultSet rs) throws SQLException {

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
