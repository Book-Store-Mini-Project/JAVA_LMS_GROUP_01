package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.model.Eligibility;
import com.example.java_lms_group_01.model.Grade;
import com.example.java_lms_group_01.model.Material;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.model.summary.StudentGradeSummary;
import com.example.java_lms_group_01.util.AssessmentStructureUtil;
import com.example.java_lms_group_01.util.AttendanceEligibilityUtil;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.GradeResult;
import com.example.java_lms_group_01.util.GradeScaleUtil;
import com.example.java_lms_group_01.util.MarkBreakdown;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles student side database work.
 */
public class StudentRepository {

    /**
     * Get all attendance records for one student
     */
    public List<Attendance> findAttendanceByStudent(String registrationNo) throws SQLException {

        String sql = "SELECT attendance_id, StudentReg, courseCode, SubmissionDate, session_type, " +
                "attendance_status, tech_officer_reg " +
                "FROM attendance " +
                "WHERE StudentReg = ? " +
                "ORDER BY SubmissionDate DESC, attendance_id DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, registrationNo);

        ResultSet rs = statement.executeQuery();

        List<Attendance> list = new ArrayList<>();

        while (rs.next()) {
            Attendance record = mapAttendanceRecord(rs);
            list.add(record);
        }

        return list;
    }

    /**
     * Get attendance eligibility details for each enrolled course
     */
    public List<Eligibility> findAttendanceEligibilityByStudent(String registrationNo) throws SQLException {

        String sql = "SELECT e.courseCode, " +
                "SUM(CASE " +
                "WHEN a.attendance_id IS NOT NULL " +
                "AND (a.attendance_status = 'present' " +
                "OR (a.attendance_status = 'medical' AND m.approval_status = 'approved')) " +
                "THEN 1 ELSE 0 END) AS eligible_sessions, " +
                "COUNT(a.attendance_id) AS total_sessions, " +
                "MAX(mk.quiz_1) AS quiz_1, " +
                "MAX(mk.quiz_2) AS quiz_2, " +
                "MAX(mk.quiz_3) AS quiz_3, " +
                "MAX(mk.assessment) AS assessment, " +
                "MAX(mk.Project) AS Project, " +
                "MAX(mk.mid_term) AS mid_term, " +
                "MAX(mk.final_theory) AS final_theory, " +
                "MAX(mk.final_practical) AS final_practical " +
                "FROM enrollment e " +
                "LEFT JOIN attendance a ON a.StudentReg = e.studentReg AND a.courseCode = e.courseCode " +
                "LEFT JOIN medical m ON m.attendance_id = a.attendance_id " +
                "LEFT JOIN marks mk ON mk.StudentReg = e.studentReg AND mk.courseCode = e.courseCode " +
                "WHERE e.studentReg = ? " +
                "GROUP BY e.courseCode " +
                "ORDER BY e.courseCode";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, registrationNo);

        ResultSet rs = statement.executeQuery();

        List<Eligibility> list = new ArrayList<>();

        while (rs.next()) {
            String courseCode = safe(rs.getString("courseCode"));

            MarkBreakdown breakdown =
                    calculateMarkBreakdown(connection, courseCode, rs);

            double caThreshold = GradeScaleUtil.minimumRequiredMark(breakdown.getCaMaximum());
            int eligibleSessions = rs.getInt("eligible_sessions");
            int totalSessions = rs.getInt("total_sessions");
            boolean attendanceEligible = AttendanceEligibilityUtil.calculatePercentage(
                    eligibleSessions,
                    totalSessions
            ) >= AttendanceEligibilityUtil.MIN_ELIGIBILITY_PERCENTAGE;
            boolean caEligible = GradeScaleUtil.meetsCaRequirement(breakdown);

            Eligibility record = new Eligibility(
                    "",
                    "",
                    courseCode,
                    String.valueOf(eligibleSessions),
                    String.valueOf(totalSessions),
                    String.format("%.2f%%", AttendanceEligibilityUtil.calculatePercentage(eligibleSessions, totalSessions)),
                    String.format("%.2f", breakdown.getCaMarks()),
                    String.format("%.2f", caThreshold),
                    buildEligibilityStatus(attendanceEligible, caEligible)
            );

            list.add(record);
        }

        return list;
    }

    /**
     * Get all enrolled courses for the student
     */
    public List<Course> findCoursesByStudent(String registrationNo) throws SQLException {

        String sql = "SELECT c.courseCode, c.name, c.lecturerRegistrationNo, c.department, " +
                "c.semester, c.credit, c.course_type, e.status " +
                "FROM enrollment e " +
                "INNER JOIN course c ON c.courseCode = e.courseCode " +
                "WHERE e.studentReg = ? " +
                "ORDER BY c.courseCode";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, registrationNo);

        ResultSet rs = statement.executeQuery();

        List<Course> list = new ArrayList<>();

        while (rs.next()) {
            Course record = mapCourseRecord(rs);
            list.add(record);
        }

        return list;
    }

    /**
     * Build grade summary with GPA and SGPA
     */
    public StudentGradeSummary findGradeSummary(String registrationNo) throws SQLException {

        String sql = "SELECT m.StudentReg, m.courseCode, c.name, c.credit, " +
                "m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, " +
                "m.final_theory, m.final_practical, " +
                "EXISTS (SELECT 1 FROM exam_attendance ea WHERE ea.studentReg = m.StudentReg " +
                "AND ea.courseCode = m.courseCode AND ea.status = 'present') AS exam_present, " +
                "EXISTS (SELECT 1 FROM medical md WHERE md.StudentReg = m.StudentReg " +
                "AND md.courseCode = m.courseCode AND md.approval_status = 'approved' " +
                "AND LOWER(COALESCE(md.session_type, '')) = 'exam') AS approved_exam_medical " +
                "FROM marks m " +
                "INNER JOIN course c ON c.courseCode = m.courseCode " +
                "WHERE m.StudentReg = ? " +
                "ORDER BY m.courseCode";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, registrationNo);

        ResultSet rs = statement.executeQuery();

        List<Grade> grades = new ArrayList<>();

        double gpaWeightedPoints = 0.0;
        int gpaCredits = 0;
        double sgpaWeightedPoints = 0.0;
        int sgpaCredits = 0;

        while (rs.next()) {
            String courseCode = safe(rs.getString("courseCode"));
            boolean attendanceEligible = isAttendanceEligible(connection, rs.getString("StudentReg"), courseCode);
            MarkBreakdown breakdown = AssessmentStructureUtil.calculateMarkBreakdown(
                    connection,
                    courseCode,
                    nullableDecimal(rs.getObject("quiz_1")),
                    nullableDecimal(rs.getObject("quiz_2")),
                    nullableDecimal(rs.getObject("quiz_3")),
                    nullableDecimal(rs.getObject("assessment")),
                    nullableDecimal(rs.getObject("Project")),
                    nullableDecimal(rs.getObject("mid_term")),
                    nullableDecimal(rs.getObject("final_theory")),
                    nullableDecimal(rs.getObject("final_practical"))
            );
            GradeResult gradeResult = GradeScaleUtil.evaluatePublishedGrade(
                    breakdown,
                    attendanceEligible,
                    rs.getInt("exam_present") == 1,
                    rs.getInt("approved_exam_medical") == 1
            );
            int credit = rs.getInt("credit");

            grades.add(new Grade(
                    courseCode,
                    safe(rs.getString("name")),
                    String.format("%.2f", breakdown.getEndMarks()),
                    String.format("%.2f", breakdown.getTotalMarks()),
                    gradeResult.getPublishedGrade()
            ));

            if (gradeResult.getGradePoint() != null) {
                sgpaWeightedPoints += gradeResult.getGradePoint() * credit;
                sgpaCredits += credit;

                if (!GradeScaleUtil.isEnglishCourse(courseCode)) {
                    gpaWeightedPoints += gradeResult.getGradePoint() * credit;
                    gpaCredits += credit;
                }
            }
        }

        double cgpa = 0.0;
        if (gpaCredits != 0) {
            cgpa = gpaWeightedPoints / gpaCredits;
        }

        double sgpa = 0.0;
        if (sgpaCredits != 0) {
            sgpa = sgpaWeightedPoints / sgpaCredits;
        }

        return new StudentGradeSummary(grades, cgpa, sgpa);
    }

    /**
     * Get materials for student
     */
    public List<Material> findMaterialsByStudent(String registrationNo, String keyword) throws SQLException {

        String safeKeyword = "";
        if (keyword != null) {
            safeKeyword = keyword.trim();
        }

        String sql = "SELECT DISTINCT lm.material_id, lm.courseCode, lm.name, lm.path, lm.material_type " +
                "FROM lecture_materials lm " +
                "INNER JOIN enrollment e ON e.courseCode = lm.courseCode " +
                "WHERE e.studentReg = ? " +
                "AND (? = '' OR lm.courseCode LIKE ? OR lm.name LIKE ?) " +
                "ORDER BY lm.courseCode, lm.material_id DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        String pattern = "%" + safeKeyword + "%";
        statement.setString(1, registrationNo);
        statement.setString(2, safeKeyword);
        statement.setString(3, pattern);
        statement.setString(4, pattern);

        ResultSet rs = statement.executeQuery();

        List<Material> list = new ArrayList<>();

        while (rs.next()) {
            Material record = mapMaterialRecord(rs);
            list.add(record);
        }

        return list;
    }

    /**
     * Get medical records for student
     */
    public List<Medical> findMedicalByStudent(String registrationNo) throws SQLException {

        String sql = "SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, " +
                "session_type, attendance_id, tech_officer_reg, approval_status " +
                "FROM medical " +
                "WHERE StudentReg = ? " +
                "ORDER BY SubmissionDate DESC, medical_id DESC";

        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, registrationNo);

        ResultSet rs = statement.executeQuery();

        List<Medical> list = new ArrayList<>();

        while (rs.next()) {
            Medical record = mapMedicalRecord(rs);
            list.add(record);
        }

        return list;
    }

    /**
     * Get timetable for student department
     */
    public List<Timetable> findTimetableByStudent(String registrationNo) throws SQLException {

        Connection connection = DBConnection.getInstance().getConnection();

        String department = findStudentDepartment(connection, registrationNo);

        if (department.isBlank()) {
            return new ArrayList<>();
        }

        List<Timetable> list = new ArrayList<>();

        boolean found = loadTimetableRows(connection, department, "timetable", list);

        if (!found) {
            loadTimetableRows(connection, department, "timeTable", list);
        }

        return list;
    }

    private Attendance mapAttendanceRecord(ResultSet rs) throws SQLException {
        return new Attendance(
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                dateToString(rs.getDate("SubmissionDate")),
                safe(rs.getString("session_type")),
                safe(rs.getString("attendance_status")),
                safe(rs.getString("tech_officer_reg"))
        );
    }

    private Course mapCourseRecord(ResultSet rs) throws SQLException {
        Course course = new Course(
                safe(rs.getString("courseCode")),
                safe(rs.getString("name")),
                safe(rs.getString("lecturerRegistrationNo")),
                safe(rs.getString("department")),
                safe(rs.getString("semester")),
                rs.getInt("credit"),
                safe(rs.getString("course_type"))
        );
        course.setEnrollmentStatus(safe(rs.getString("status")));
        return course;
    }

    private Material mapMaterialRecord(ResultSet rs) throws SQLException {
        return new Material(
                String.valueOf(rs.getInt("material_id")),
                safe(rs.getString("courseCode")),
                safe(rs.getString("name")),
                safe(rs.getString("path")),
                safe(rs.getString("material_type"))
        );
    }

    private Medical mapMedicalRecord(ResultSet rs) throws SQLException {
        return new Medical(
                String.valueOf(rs.getInt("medical_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                dateToString(rs.getDate("SubmissionDate")),
                safe(rs.getString("Description")),
                safe(rs.getString("session_type")),
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("approval_status")),
                safe(rs.getString("tech_officer_reg"))
        );
    }

    private String findStudentDepartment(Connection connection, String regNo) throws SQLException {

        String sql = "SELECT department FROM student WHERE registrationNo = ?";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, regNo);

        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            return safe(rs.getString("department"));
        }

        return "";
    }

    private boolean isAttendanceEligible(Connection connection, String studentReg, String courseCode) throws SQLException {

        String sql = "SELECT COALESCE(SUM(CASE " +
                "WHEN a.attendance_status = 'present' THEN 1 " +
                "WHEN a.attendance_status = 'medical' AND EXISTS (" +
                "SELECT 1 FROM medical m WHERE m.attendance_id = a.attendance_id " +
                "AND m.approval_status = 'approved') THEN 1 " +
                "ELSE 0 END), 0) AS eligible_sessions, " +
                "COUNT(*) AS total_sessions " +
                "FROM attendance a " +
                "WHERE a.StudentReg = ? AND a.courseCode = ?";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, studentReg);
        statement.setString(2, courseCode);

        ResultSet rs = statement.executeQuery();

        if (!rs.next()) {
            return false;
        }

        double percentage = AttendanceEligibilityUtil.calculatePercentage(
                rs.getInt("eligible_sessions"),
                rs.getInt("total_sessions")
        );

        return percentage >= AttendanceEligibilityUtil.MIN_ELIGIBILITY_PERCENTAGE;
    }

    private MarkBreakdown calculateMarkBreakdown(Connection connection, String courseCode,
                                                 ResultSet rs) throws SQLException {
        return AssessmentStructureUtil.calculateMarkBreakdown(
                connection,
                courseCode,
                nullableDecimal(rs.getObject("quiz_1")),
                nullableDecimal(rs.getObject("quiz_2")),
                nullableDecimal(rs.getObject("quiz_3")),
                nullableDecimal(rs.getObject("assessment")),
                nullableDecimal(rs.getObject("Project")),
                nullableDecimal(rs.getObject("mid_term")),
                nullableDecimal(rs.getObject("final_theory")),
                nullableDecimal(rs.getObject("final_practical"))
        );
    }

    private boolean loadTimetableRows(Connection connection, String department,
                                      String tableName, List<Timetable> rows) throws SQLException {

        String sql = "SELECT t.time_table_id, t.department, t.lec_id, t.courseCode, t.admin_id, " +
                "t.day, t.start_time, t.end_time, t.session_type " +
                "FROM " + tableName + " t " +
                "WHERE t.department = ? " +
                "ORDER BY t.day, t.start_time";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, department);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                rows.add(mapTimetableRecord(rs));
            }

            return true;

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                return false;
            }

            throw e;
        }
    }

    private Timetable mapTimetableRecord(ResultSet rs) throws SQLException {
        return new Timetable(
                safe(rs.getString("time_table_id")),
                safe(rs.getString("department")),
                safe(rs.getString("lec_id")),
                safe(rs.getString("courseCode")),
                safe(rs.getString("admin_id")),
                safe(rs.getString("day")),
                rs.getTime("start_time") == null ? null : rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time") == null ? null : rs.getTime("end_time").toLocalTime(),
                safe(rs.getString("session_type"))
        );
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private static String dateToString(java.sql.Date date) {
        if (date == null) {
            return "";
        }
        return date.toString();
    }

    private static Double nullableDecimal(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).doubleValue();
    }

    private String buildEligibilityStatus(boolean attendanceEligible, boolean caEligible) {
        if (attendanceEligible && caEligible) {
            return "Eligible";
        }
        if (!attendanceEligible && !caEligible) {
            return "Attendance + CA Shortage";
        }
        if (!attendanceEligible) {
            return "Attendance Shortage";
        }
        return "CA Shortage";
    }
}
