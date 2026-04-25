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
import com.example.java_lms_group_01.util.GradeScaleUtil;
import com.example.java_lms_group_01.model.summary.GradeResult;
import com.example.java_lms_group_01.model.summary.MarkBreakdown;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository {

    public List<Attendance> findAttendanceByStudent(String regNo) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        List<Attendance> list = new ArrayList<>();


        String sql = "SELECT * FROM attendance WHERE StudentReg = ? ORDER BY attendance_id DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regNo);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Attendance a = new Attendance(
                    String.valueOf(rs.getInt("attendance_id")),
                    rs.getString("StudentReg"),
                    rs.getString("courseCode"),
                    rs.getDate("SubmissionDate") == null ? "" : rs.getDate("SubmissionDate").toString(),
                    rs.getString("session_type"),
                    rs.getString("attendance_status"),
                    rs.getString("tech_officer_reg")
            );

            list.add(a);
        }

        return list;
    }

    public List<Eligibility> findAttendanceEligibilityByStudent(String regNo) throws SQLException {

        Connection connection = DBConnection.getInstance().getConnection();
        List<Eligibility> list = new ArrayList<>();

        String sql = "SELECT e.courseCode FROM enrollment e WHERE e.studentReg = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regNo);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            String course = rs.getString("courseCode");

            // TOTAL SESSIONS
            String totalSql = "SELECT COUNT(*) FROM attendance WHERE StudentReg = ? AND courseCode = ?";
            PreparedStatement totalStmt = connection.prepareStatement(totalSql);
            totalStmt.setString(1, regNo);
            totalStmt.setString(2, course);

            ResultSet totalRs = totalStmt.executeQuery();
            int totalSessions = totalRs.next() ? totalRs.getInt(1) : 0;


            // ELIGIBLE SESSIONS
            String eligibleSql =
                    "SELECT COUNT(*) FROM attendance a " +
                            "LEFT JOIN medical m ON m.attendance_id = a.attendance_id " +
                            "WHERE a.StudentReg = ? AND a.courseCode = ? " +
                            "AND (a.attendance_status = 'present' " +
                            "OR (a.attendance_status = 'medical' AND m.approval_status = 'approved'))";

            PreparedStatement eligibleStmt = connection.prepareStatement(eligibleSql);
            eligibleStmt.setString(1, regNo);
            eligibleStmt.setString(2, course);

            ResultSet eligibleRs = eligibleStmt.executeQuery();
            int eligibleSessions = eligibleRs.next() ? eligibleRs.getInt(1) : 0;

            double attendancePercentage = AttendanceEligibilityUtil.calculatePercentage(
                    eligibleSessions,
                    totalSessions
            );


            // CA MARKS + THRESHOLD
            String markSql =
                    "SELECT quiz_1, quiz_2, quiz_3, assessment, Project, mid_term, final_theory, final_practical " +
                            "FROM marks WHERE StudentReg = ? AND courseCode = ?";

            PreparedStatement markStmt = connection.prepareStatement(markSql);
            markStmt.setString(1, regNo);
            markStmt.setString(2, course);

            ResultSet markRs = markStmt.executeQuery();

            double caMarks = 0;
            double caThreshold = 0;

            if (markRs.next()) {

                MarkBreakdown breakdown = AssessmentStructureUtil.calculateMarkBreakdown(
                        connection,
                        course,
                        markRs.getObject("quiz_1") == null ? null : ((Number) markRs.getObject("quiz_1")).doubleValue(),
                        markRs.getObject("quiz_2") == null ? null : ((Number) markRs.getObject("quiz_2")).doubleValue(),
                        markRs.getObject("quiz_3") == null ? null : ((Number) markRs.getObject("quiz_3")).doubleValue(),
                        markRs.getObject("assessment") == null ? null : ((Number) markRs.getObject("assessment")).doubleValue(),
                        markRs.getObject("Project") == null ? null : ((Number) markRs.getObject("Project")).doubleValue(),
                        markRs.getObject("mid_term") == null ? null : ((Number) markRs.getObject("mid_term")).doubleValue(),
                        markRs.getObject("final_theory") == null ? null : ((Number) markRs.getObject("final_theory")).doubleValue(),
                        markRs.getObject("final_practical") == null ? null : ((Number) markRs.getObject("final_practical")).doubleValue()
                );

                caMarks = breakdown.getCaMarks();
                caThreshold = GradeScaleUtil.minimumRequiredMark(breakdown.getCaMaximum());
            }


            //ELIGIBILITY LOGIC
            boolean attendanceEligible = attendancePercentage >= 80;
            boolean caEligible = caMarks >= caThreshold;

            String status;
            if (attendanceEligible && caEligible) {
                status = "Eligible";
            } else if (!attendanceEligible && !caEligible) {
                status = "Attendance + CA Shortage";
            } else if (!attendanceEligible) {
                status = "Attendance Shortage";
            } else {
                status = "CA Shortage";
            }

            // Eligibility
            Eligibility e = new Eligibility(
                    "",
                    "",
                    course,
                    String.valueOf(eligibleSessions),
                    String.valueOf(totalSessions),
                    String.format("%.2f%%", attendancePercentage),
                    String.format("%.2f", caMarks),
                    String.format("%.2f", caThreshold),
                    status
            );

            list.add(e);
        }

        return list;
    }

    public List<Course> findCoursesByStudent(String regNo) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        List<Course> list = new ArrayList<>();

        String sql = "SELECT c.*, e.status FROM course c " +
                "JOIN enrollment e ON c.courseCode = e.courseCode " +
                "WHERE e.studentReg = ?";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regNo);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            Course c = new Course(
                    rs.getString("courseCode"),
                    rs.getString("name"),
                    rs.getString("lecturerRegistrationNo"),
                    rs.getString("department"),
                    rs.getString("semester"),
                    rs.getInt("credit"),
                    rs.getString("course_type")
            );

            c.setEnrollmentStatus(rs.getString("status"));

            list.add(c);
        }

        return list;
    }

    public StudentGradeSummary findGradeSummary(String regNo) throws SQLException {

        List<Grade> grades = new ArrayList<>();

        double cgpaPoints = 0;
        int cgpaCredits = 0;

        double sgpaPoints = 0;
        int sgpaCredits = 0;

        boolean withheld = false;

        String sql =
                "SELECT m.StudentReg, m.courseCode, c.name, c.credit, " +
                        "m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, " +
                        "m.final_theory, m.final_practical, " +
                        "(SELECT status FROM exam_attendance WHERE studentReg = m.StudentReg AND courseCode = m.courseCode LIMIT 1) AS exam_status " +
                        "FROM marks m INNER JOIN course c ON c.courseCode = m.courseCode " +
                        "WHERE m.StudentReg = ?";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, regNo);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            String courseCode = rs.getString("courseCode");
            int credit = rs.getInt("credit");

            boolean attendanceOk = isAttendanceEligible(con, regNo, courseCode);

            MarkBreakdown marks = AssessmentStructureUtil.calculateMarkBreakdown(
                    con,
                    courseCode,
                    toDouble(rs.getObject("quiz_1")),
                    toDouble(rs.getObject("quiz_2")),
                    toDouble(rs.getObject("quiz_3")),
                    toDouble(rs.getObject("assessment")),
                    toDouble(rs.getObject("Project")),
                    toDouble(rs.getObject("mid_term")),
                    toDouble(rs.getObject("final_theory")),
                    toDouble(rs.getObject("final_practical"))
            );

            GradeResult result = GradeScaleUtil.evaluatePublishedGrade(
                    marks,
                    attendanceOk,
                    rs.getString("exam_status"),
                    false
            );

            if ("MC".equals(result.getPublishedGrade())) {
                withheld = true;
            }

            grades.add(new Grade(
                    courseCode,
                    rs.getString("name"),
                    String.valueOf(marks.getEndMarks()),
                    String.valueOf(marks.getTotalMarks()),
                    result.getPublishedGrade()
            ));

            if (result.getGradePoint() != null) {

                double gp = result.getGradePoint();

                sgpaPoints += gp * credit;
                sgpaCredits += credit;

                cgpaPoints += gp * credit;
                cgpaCredits += credit;
            }
        }

        double cgpa = (cgpaCredits == 0) ? 0 : cgpaPoints / cgpaCredits;
        double sgpa = (sgpaCredits == 0) ? 0 : sgpaPoints / sgpaCredits;

        return new StudentGradeSummary(grades, cgpa, sgpa, withheld);
    }

    public List<Material> findMaterialsByStudent(String regNo, String keyword) throws SQLException {

        Connection connection = DBConnection.getInstance().getConnection();
        List<Material> list = new ArrayList<>();

        // clean keyword
        String search = (keyword == null) ? "" : keyword.trim();
        String pattern = "%" + search + "%";

        String sql =
                "SELECT lm.material_id, lm.courseCode, lm.name, lm.path, lm.material_type " +
                        "FROM lecture_materials lm " +
                        "JOIN enrollment e ON e.courseCode = lm.courseCode " +
                        "WHERE e.studentReg = ? " +
                        "AND (? = '' OR lm.courseCode LIKE ? OR lm.name LIKE ?) " +
                        "ORDER BY lm.material_id DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setString(1, regNo);
        stmt.setString(2, search);
        stmt.setString(3, pattern);
        stmt.setString(4, pattern);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            Material m = new Material(
                    String.valueOf(rs.getInt("material_id")),
                    rs.getString("courseCode"),
                    rs.getString("name"),
                    rs.getString("path"),
                    rs.getString("material_type")
            );

            list.add(m);
        }

        return list;
    }

    public List<Medical> findMedicalByStudent(String regNo) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        List<Medical> list = new ArrayList<>();

        String sql = "SELECT * FROM medical WHERE StudentReg = ? ORDER BY medical_id DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, regNo);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            Medical m = new Medical(
                    String.valueOf(rs.getInt("medical_id")),
                    rs.getString("StudentReg"),
                    rs.getString("courseCode"),
                    rs.getDate("SubmissionDate") == null ? "" : rs.getDate("SubmissionDate").toString(),
                    rs.getString("Description"),
                    rs.getString("session_type"),
                    String.valueOf(rs.getInt("attendance_id")),
                    rs.getString("approval_status"),
                    rs.getString("tech_officer_reg")
            );

            list.add(m);
        }

        return list;
    }

    public List<Timetable> findTimetableByStudent(String regNo) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        List<Timetable> list = new ArrayList<>();

        // Get student department
        String department = findStudentDepartment(connection,regNo);

        if (department == null || department.isEmpty()) {
            return list;
        }

        // Get timetable for that department
        String sql = "SELECT * FROM timetable WHERE department = ? ORDER BY day, start_time";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, department);

        ResultSet rs = stmt.executeQuery();

        // Convert DB rows to objects
        while (rs.next()) {

            Timetable t = new Timetable(
                    rs.getString("time_table_id"),
                    rs.getString("department"),
                    rs.getString("lec_id"),
                    rs.getString("courseCode"),
                    rs.getString("admin_id"),
                    rs.getString("day"),
                    rs.getTime("start_time") == null ? null : rs.getTime("start_time").toLocalTime(),
                    rs.getTime("end_time") == null ? null : rs.getTime("end_time").toLocalTime(),
                    rs.getString("session_type")
            );

            list.add(t);
        }

        return list;
    }
    
    private String findStudentDepartment(Connection con, String regNo) throws SQLException {

        String sql = "SELECT department FROM student WHERE registrationNo = ?";

        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, regNo);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("department");
        }

        return "";
    }

    private boolean isAttendanceEligible(Connection con, String studentReg, String courseCode) throws SQLException {

        String sql =
                "SELECT " +
                        "SUM(CASE " +
                        "WHEN a.attendance_status = 'present' THEN 1 " +
                        "WHEN a.attendance_status = 'medical' AND EXISTS (" +
                        "SELECT 1 FROM medical m WHERE m.attendance_id = a.attendance_id " +
                        "AND m.approval_status = 'approved') THEN 1 " +
                        "ELSE 0 END) AS eligible_sessions, " +
                        "COUNT(*) AS total_sessions " +
                        "FROM attendance a " +
                        "WHERE a.StudentReg = ? AND a.courseCode = ?";

        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1, studentReg);
        stmt.setString(2, courseCode);

        ResultSet rs = stmt.executeQuery();

        if (!rs.next()) {
            return false;
        }

        int eligible = rs.getInt("eligible_sessions");
        int total = rs.getInt("total_sessions");

        double percentage = (total == 0)
                ? 0
                : (eligible * 100.0 / total);

        return percentage >= 80; // minimum required attendance
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        return ((Number) value).doubleValue();
    }
}
