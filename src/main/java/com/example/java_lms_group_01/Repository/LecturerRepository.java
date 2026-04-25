package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.Attendance;
import com.example.java_lms_group_01.model.Eligibility;
import com.example.java_lms_group_01.model.Mark;
import com.example.java_lms_group_01.model.Material;
import com.example.java_lms_group_01.model.Performance;
import com.example.java_lms_group_01.model.Student;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.model.request.MarkRequest;
import com.example.java_lms_group_01.model.request.MaterialRequest;
import com.example.java_lms_group_01.model.summary.AcademicSummary;
import com.example.java_lms_group_01.model.summary.UndergraduateSummary;
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LecturerRepository {

    public List<Attendance> findAttendanceMedicalByLecturer(String lecturerReg, String keyword) throws SQLException {

        List<Attendance> list = new ArrayList<>();

        String safeKeyword = keyword == null ? "" : keyword.trim();
        String pattern = "%" + safeKeyword + "%";

        String sql =
                "SELECT a.attendance_id, a.StudentReg, a.courseCode, a.SubmissionDate, " +
                        "a.session_type, a.attendance_status, a.tech_officer_reg, " +
                        "m.medical_id, m.Description, m.approval_status " +
                        "FROM attendance a " +
                        "INNER JOIN course c ON c.courseCode = a.courseCode " +
                        "LEFT JOIN medical m ON m.attendance_id = a.attendance_id " +
                        "WHERE c.lecturerRegistrationNo = ? " +
                        "AND (? = '' OR a.StudentReg LIKE ? OR a.courseCode LIKE ?) " +
                        "ORDER BY a.attendance_id DESC";

        Connection con = DBConnection.getInstance().getConnection();

        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setString(1, lecturerReg);
        stmt.setString(2, safeKeyword);
        stmt.setString(3, pattern);
        stmt.setString(4, pattern);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            Attendance a = new Attendance(
                    String.valueOf(rs.getInt("attendance_id")),
                    rs.getString("StudentReg") == null ? "" : rs.getString("StudentReg"),
                    rs.getString("courseCode") == null ? "" : rs.getString("courseCode"),
                    rs.getDate("SubmissionDate") == null ? null : rs.getDate("SubmissionDate").toString(),
                    rs.getString("session_type") == null ? "" : rs.getString("session_type"),
                    rs.getString("attendance_status") == null ? "" : rs.getString("attendance_status"),
                    rs.getString("tech_officer_reg") == null ? "" : rs.getString("tech_officer_reg"),
                    rs.getObject("medical_id") == null ? "" : String.valueOf(rs.getInt("medical_id")),
                    rs.getString("Description") == null ? "" : rs.getString("Description"),
                    rs.getString("approval_status") == null ? "" : rs.getString("approval_status")
            );

            list.add(a);
        }

        return list;
    }
    //update medical decision
    public void updateMedicalDecision(String lecturerReg, int medicalId, int attendanceId, String approvalStatus, String attendanceStatus) throws SQLException {

        Connection con = DBConnection.getInstance().getConnection();

        String medicalSql =
                "UPDATE medical " +
                        "SET approval_status = ?, " +
                        "approved_by_lecturer = ?, " +
                        "approved_at = CURRENT_DATE " +
                        "WHERE medical_id = ?";

        PreparedStatement medicalStmt = con.prepareStatement(medicalSql);
        medicalStmt.setString(1, approvalStatus);
        medicalStmt.setString(2, lecturerReg);
        medicalStmt.setInt(3, medicalId);

        int updated = medicalStmt.executeUpdate();

        if (updated == 0) {
            throw new SQLException("Medical record not found");
        }

        String attendanceSql =
                "UPDATE attendance SET attendance_status = ? WHERE attendance_id = ?";

        PreparedStatement attendanceStmt = con.prepareStatement(attendanceSql);
        attendanceStmt.setString(1, attendanceStatus);
        attendanceStmt.setInt(2, attendanceId);

        attendanceStmt.executeUpdate();
    }

    public List<Eligibility> findEligibilityByLecturer(String lecturerReg,String studentKeyword, String courseCode, String batch) throws SQLException {

        String sql =
                "SELECT e.studentReg, u.firstName, u.lastName, e.courseCode, " +
                        "SUM(CASE WHEN a.attendance_id IS NOT NULL AND " +
                        "(a.attendance_status = 'present' OR " +
                        "(a.attendance_status = 'medical' AND m.approval_status = 'approved')) " +
                        "THEN 1 ELSE 0 END) AS eligible_sessions, " +
                        "COUNT(a.attendance_id) AS total_sessions, " +
                        "MAX(mk.quiz_1) AS quiz_1, MAX(mk.quiz_2) AS quiz_2, MAX(mk.quiz_3) AS quiz_3, " +
                        "MAX(mk.assessment) AS assessment, MAX(mk.Project) AS Project, MAX(mk.mid_term) AS mid_term " +
                        "FROM enrollment e " +
                        "INNER JOIN course c ON c.courseCode = e.courseCode " +
                        "INNER JOIN student s ON s.registrationNo = e.studentReg " +
                        "INNER JOIN users u ON u.user_id = s.registrationNo " +
                        "LEFT JOIN attendance a ON a.StudentReg = e.studentReg AND a.courseCode = e.courseCode " +
                        "LEFT JOIN medical m ON m.attendance_id = a.attendance_id " +
                        "LEFT JOIN marks mk ON mk.StudentReg = e.studentReg AND mk.courseCode = e.courseCode " +
                        "WHERE c.lecturerRegistrationNo = ? " +
                        "AND (? = '' OR s.batch = ?) " +
                        "AND (? = '' OR e.studentReg LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?) " +
                        "AND (? = '' OR e.courseCode = ?) " +
                        "GROUP BY e.studentReg, u.firstName, u.lastName, e.courseCode " +
                        "ORDER BY e.studentReg, e.courseCode";

        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stm = con.prepareStatement(sql);

        String keyword = studentKeyword == null ? "" : studentKeyword;
        String pattern = "%" + keyword + "%";

        stm.setString(1, lecturerReg);

        stm.setString(2, batch);
        stm.setString(3, batch);

        stm.setString(4, keyword);
        stm.setString(5, pattern);
        stm.setString(6, pattern);
        stm.setString(7, pattern);

        stm.setString(8, courseCode);
        stm.setString(9, courseCode);

        ResultSet rs = stm.executeQuery();

        List<Eligibility> list = new ArrayList<>();

        while (rs.next()) {

            int eligibleSessions = rs.getInt("eligible_sessions");
            int totalSessions = rs.getInt("total_sessions");

            double attendance = totalSessions == 0 ? 0 : (eligibleSessions * 100.0 / totalSessions);

            MarkBreakdown breakdown = AssessmentStructureUtil.calculateMarkBreakdown(
                    con,
                    rs.getString("courseCode"),
                    nullableDecimal(rs.getObject("quiz_1")),
                    nullableDecimal(rs.getObject("quiz_2")),
                    nullableDecimal(rs.getObject("quiz_3")),
                    nullableDecimal(rs.getObject("assessment")),
                    nullableDecimal(rs.getObject("Project")),
                    nullableDecimal(rs.getObject("mid_term")),
                    null,
                    null
            );

            double caMarks = breakdown.getCaMarks();
            double caThreshold = GradeScaleUtil.minimumRequiredMark(breakdown.getCaMaximum());

            boolean eligible = attendance >= 80 && caMarks >= caThreshold;

            list.add(new Eligibility(
                    rs.getString("studentReg"),
                    rs.getString("firstName") + " " + rs.getString("lastName"),
                    rs.getString("courseCode"),
                    String.valueOf(eligibleSessions),
                    String.valueOf(totalSessions),
                    String.format("%.2f%%", attendance),
                    String.format("%.2f", caMarks),
                    String.format("%.2f", caThreshold),
                    eligible ? "Eligible" : "Not Eligible"
            ));
        }

        return list;
    }
    //update FindPerformanceByLecture method
    public List<Performance> findPerformanceByLecturer( String lecturerReg, String studentKeyword, String courseCode, String batch) throws SQLException {

        String sql =
                "SELECT m.StudentReg, u.firstName, u.lastName, m.courseCode, c.name, " +
                        "m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical, " +
                        "(SELECT ea.status FROM exam_attendance ea " +
                        "WHERE ea.studentReg = m.StudentReg AND ea.courseCode = m.courseCode LIMIT 1) AS exam_status, " +
                        "EXISTS (SELECT 1 FROM medical md " +
                        "WHERE md.StudentReg = m.StudentReg AND md.courseCode = m.courseCode " +
                        "AND md.approval_status = 'approved' AND LOWER(COALESCE(md.session_type,''))='exam') AS approved_exam_medical " +
                        "FROM marks m " +
                        "INNER JOIN course c ON c.courseCode = m.courseCode " +
                        "INNER JOIN student s ON s.registrationNo = m.StudentReg " +
                        "INNER JOIN users u ON u.user_id = s.registrationNo " +
                        "WHERE c.lecturerRegistrationNo = ? " +
                        "AND (? = '' OR s.batch = ?) " +
                        "AND (? = '' OR m.StudentReg LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?) " +
                        "AND (? = '' OR m.courseCode = ?) " +
                        "ORDER BY m.StudentReg, m.courseCode";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = studentKeyword == null ? "" : studentKeyword;
            String pattern = "%" + keyword + "%";

            statement.setString(1, lecturerReg);

            statement.setString(2, batch);
            statement.setString(3, batch);

            statement.setString(4, keyword);
            statement.setString(5, pattern);
            statement.setString(6, pattern);
            statement.setString(7, pattern);

            statement.setString(8, courseCode);
            statement.setString(9, courseCode);

            ResultSet rs = statement.executeQuery();

            List<Performance> list = new ArrayList<>();

            while (rs.next()) {

                String studentReg = rs.getString("StudentReg");
                String course = rs.getString("courseCode");

                MarkBreakdown breakdown = AssessmentStructureUtil.calculateMarkBreakdown(
                        connection,
                        course,
                        nullableDecimal(rs.getObject("quiz_1")),
                        nullableDecimal(rs.getObject("quiz_2")),
                        nullableDecimal(rs.getObject("quiz_3")),
                        nullableDecimal(rs.getObject("assessment")),
                        nullableDecimal(rs.getObject("Project")),
                        nullableDecimal(rs.getObject("mid_term")),
                        nullableDecimal(rs.getObject("final_theory")),
                        nullableDecimal(rs.getObject("final_practical"))
                );

                boolean attendanceOk = isAttendanceEligible(connection, studentReg, course);

                GradeResult gradeResult = GradeScaleUtil.evaluatePublishedGrade(
                        breakdown,
                        attendanceOk,
                        rs.getString("exam_status"),
                        rs.getInt("approved_exam_medical") == 1
                );

                String sgpa = String.format("%.2f", gradeResult.getGradePoint() == null ? 0 : gradeResult.getGradePoint());

                list.add(new Performance(
                        studentReg,
                        rs.getString("firstName") + " " + rs.getString("lastName"),
                        course,
                        rs.getString("name"),
                        String.format("%.2f", breakdown.getCaMarks()),
                        String.format("%.2f", breakdown.getEndMarks()),
                        String.format("%.2f", breakdown.getTotalMarks()),
                        gradeResult.getPublishedGrade(),
                        sgpa,
                        sgpa
                ));
            }

            return list;
        }
    }

    public List<UndergraduateSummary> findUndergraduateSummariesByLecturer( String lecturerReg, String studentKeyword, String courseCode, String batch) throws SQLException {

        String sql =
                "SELECT DISTINCT s.registrationNo, u.firstName, u.lastName " +
                        "FROM student s " +
                        "INNER JOIN users u ON u.user_id = s.registrationNo " +
                        "INNER JOIN enrollment e ON e.studentReg = s.registrationNo " +
                        "INNER JOIN course c ON c.courseCode = e.courseCode " +
                        "WHERE c.lecturerRegistrationNo = ? " +
                        "AND (? = '' OR s.batch = ?) " +
                        "AND (? = '' OR s.registrationNo LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?) " +
                        "AND (? = '' OR e.courseCode = ?) " +
                        "ORDER BY s.registrationNo";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            String keyword = studentKeyword == null ? "" : studentKeyword;
            String pattern = "%" + keyword + "%";

            statement.setString(1, lecturerReg);

            statement.setString(2, batch);
            statement.setString(3, batch);

            statement.setString(4, keyword);
            statement.setString(5, pattern);
            statement.setString(6, pattern);
            statement.setString(7, pattern);

            statement.setString(8, courseCode);
            statement.setString(9, courseCode);

            ResultSet rs = statement.executeQuery();

            List<UndergraduateSummary> list = new ArrayList<>();

            while (rs.next()) {

                String regNo = rs.getString("registrationNo");

                AcademicSummary summary = calculateAcademicSummary(connection, regNo);

                list.add(new UndergraduateSummary(
                        regNo,
                        rs.getString("firstName") + " " + rs.getString("lastName"),
                        summary.isWithheld() ? "WH" : String.format("%.2f", summary.getSgpa()),
                        summary.isWithheld() ? "WH" : String.format("%.2f", summary.getCgpa())
                ));
            }

            return list;
        }
    }

    public void addMarks(String lecturerReg, MarkRequest request) throws SQLException {

        Connection con = DBConnection.getInstance().getConnection();

        PreparedStatement stm = con.prepareStatement(
                "INSERT INTO marks (LectureReg, StudentReg, courseCode, quiz_1, quiz_2, quiz_3, " +
                        "assessment, Project, mid_term, final_theory, final_practical) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );

        stm.setString(1, lecturerReg);
        stm.setString(2, request.getStudentReg());
        stm.setString(3, request.getCourseCode());

        stm.setObject(4, request.getQuiz1());
        stm.setObject(5, request.getQuiz2());
        stm.setObject(6, request.getQuiz3());
        stm.setObject(7, request.getAssessment());
        stm.setObject(8, request.getProject());
        stm.setObject(9, request.getMidTerm());
        stm.setObject(10, request.getFinalTheory());
        stm.setObject(11, request.getFinalPractical());

        stm.executeUpdate();
    }

    public void updateMarks(String lecturerReg, int markId, MarkRequest request) throws SQLException {

        Connection con = DBConnection.getInstance().getConnection();

        PreparedStatement stm = con.prepareStatement(
                "UPDATE marks SET StudentReg=?, courseCode=?, quiz_1=?, quiz_2=?, quiz_3=?, " +
                        "assessment=?, Project=?, mid_term=?, final_theory=?, final_practical=? " +
                        "WHERE mark_id=? AND LectureReg=?"
        );

        stm.setString(1, request.getStudentReg());
        stm.setString(2, request.getCourseCode());

        stm.setObject(3, request.getQuiz1());
        stm.setObject(4, request.getQuiz2());
        stm.setObject(5, request.getQuiz3());
        stm.setObject(6, request.getAssessment());
        stm.setObject(7, request.getProject());
        stm.setObject(8, request.getMidTerm());
        stm.setObject(9, request.getFinalTheory());
        stm.setObject(10, request.getFinalPractical());

        stm.setInt(11, markId);
        stm.setString(12, lecturerReg);

        stm.executeUpdate();
    }

    public void deleteMarks(String lecturerReg, int markId) throws SQLException {

        Connection con = DBConnection.getInstance().getConnection();

        PreparedStatement stm = con.prepareStatement(
                "DELETE FROM marks WHERE mark_id = ? AND LectureReg = ?"
        );

        stm.setInt(1, markId);
        stm.setString(2, lecturerReg);

        stm.executeUpdate();
    }

    public List<Mark> findMarksByLecturer(String lecturerReg, String keyword) throws SQLException {

        String sql =
                "SELECT mark_id, StudentReg, courseCode, quiz_1, quiz_2, quiz_3, assessment, Project, mid_term, final_theory, final_practical " +
                        "FROM marks " +
                        "WHERE LectureReg = ? " +
                        "AND (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?) " +
                        "ORDER BY mark_id DESC";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            String key = keyword == null ? "" : keyword;
            String pattern = "%" + key + "%";

            ps.setString(1, lecturerReg);
            ps.setString(2, key);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            ResultSet rs = ps.executeQuery();

            List<Mark> list = new ArrayList<>();

            while (rs.next()) {

                list.add(new Mark(
                        rs.getString("mark_id"),
                        rs.getString("StudentReg"),
                        rs.getString("courseCode"),
                        rs.getString("quiz_1"),
                        rs.getString("quiz_2"),
                        rs.getString("quiz_3"),
                        rs.getString("assessment"),
                        rs.getString("Project"),
                        rs.getString("mid_term"),
                        rs.getString("final_theory"),
                        rs.getString("final_practical")
                ));
            }

            return list;
        }
    }

    public List<String> findBatchesByLecturer(String lecturerReg) throws SQLException {

        String sql =
                "SELECT DISTINCT s.batch " +
                        "FROM student s " +
                        "INNER JOIN enrollment e ON e.studentReg = s.registrationNo " +
                        "INNER JOIN course c ON c.courseCode = e.courseCode " +
                        "WHERE c.lecturerRegistrationNo = ? " +
                        "AND s.batch IS NOT NULL " +
                        "AND TRIM(s.batch) <> '' " +
                        "ORDER BY s.batch";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, lecturerReg);

            ResultSet rs = ps.executeQuery();

            List<String> list = new ArrayList<>();

            while (rs.next()) {
                list.add(rs.getString("batch"));
            }

            return list;
        }
    }

    public int addMaterial(String lecturerReg, MaterialRequest request) throws SQLException {

        String sql =
                "INSERT INTO lecture_materials (courseCode, name, path, material_type) " +
                        "SELECT ?, ?, ?, ? " +
                        "FROM course " +
                        "WHERE courseCode = ? AND lecturerRegistrationNo = ?";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.getCourseCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getPath());
            ps.setString(4, request.getMaterialType());
            ps.setString(5, request.getCourseCode());
            ps.setString(6, lecturerReg);

            return ps.executeUpdate();
        }
    }

    public int updateMaterial(String lecturerReg, int materialId, MaterialRequest request) throws SQLException {

        String sql =
                "UPDATE lecture_materials lm " +
                        "JOIN course c ON c.courseCode = lm.courseCode " +
                        "SET lm.courseCode = ?, lm.name = ?, lm.path = ?, lm.material_type = ? " +
                        "WHERE lm.material_id = ? " +
                        "AND c.lecturerRegistrationNo = ?";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.getCourseCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getPath());
            ps.setString(4, request.getMaterialType());
            ps.setInt(5, materialId);
            ps.setString(6, lecturerReg);

            return ps.executeUpdate();
        }
    }

    public int deleteMaterial(String lecturerReg, int materialId) throws SQLException {

        String sql =
                "DELETE FROM lecture_materials " +
                        "WHERE material_id = ? " +
                        "AND courseCode IN (SELECT courseCode FROM course WHERE lecturerRegistrationNo = ?)";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, materialId);
            ps.setString(2, lecturerReg);

            return ps.executeUpdate();
        }
    }

    public List<Material> findMaterialsByLecturer(String lecturerReg, String keyword) throws SQLException {

        String sql =
                "SELECT material_id, courseCode, name, path, material_type " +
                        "FROM lecture_materials " +
                        "WHERE courseCode IN ( " +
                        "   SELECT courseCode FROM course WHERE lecturerRegistrationNo = ? " +
                        ") " +
                        "AND (? = '' OR courseCode LIKE ? OR name LIKE ?) " +
                        "ORDER BY material_id DESC";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            String key = keyword == null ? "" : keyword;
            String pattern = "%" + key + "%";

            ps.setString(1, lecturerReg);
            ps.setString(2, key);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            ResultSet rs = ps.executeQuery();

            List<Material> list = new ArrayList<>();

            while (rs.next()) {

                list.add(new Material(
                        rs.getString("material_id"),
                        rs.getString("courseCode"),
                        rs.getString("name"),
                        rs.getString("path"),
                        rs.getString("material_type")
                ));
            }

            return list;
        }
    }

    public List<Student> findStudentsByLecturer(String lecturerReg, String keyword) throws SQLException {

        String sql =
                "SELECT DISTINCT s.registrationNo, u.firstName, u.lastName, u.email, u.phoneNumber, s.department, s.status " +
                        "FROM student s " +
                        "INNER JOIN users u ON u.user_id = s.registrationNo " +
                        "INNER JOIN enrollment e ON e.studentReg = s.registrationNo " +
                        "INNER JOIN course c ON c.courseCode = e.courseCode " +
                        "WHERE c.lecturerRegistrationNo = ? " +
                        "AND (? = '' OR s.registrationNo LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ? OR s.department LIKE ?) " +
                        "ORDER BY s.registrationNo";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            String key = keyword == null ? "" : keyword;
            String pattern = "%" + key + "%";

            ps.setString(1, lecturerReg);
            ps.setString(2, key);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);

            ResultSet rs = ps.executeQuery();

            List<Student> list = new ArrayList<>();

            while (rs.next()) {

                String regNo = rs.getString("registrationNo");

                AcademicSummary summary = calculateAcademicSummary(connection, regNo);

                list.add(new Student(
                        regNo,
                        rs.getString("firstName") + " " + rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        rs.getString("department"),
                        rs.getString("status"),
                        String.format("%.2f", summary.getCgpa())
                ));
            }

            return list;
        }
    }

    public List<Timetable> findTimetableByLecturer(String lecturerReg, String keyword) throws SQLException {

        String sql =
                "SELECT time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type " +
                        "FROM timetable " +
                        "WHERE lec_id = ? " +
                        "AND (? = '' OR courseCode LIKE ? OR day LIKE ? OR time_table_id LIKE ?) " +
                        "ORDER BY day, start_time";

        Connection connection = DBConnection.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            String key = keyword == null ? "" : keyword;
            String pattern = "%" + key + "%";

            ps.setString(1, lecturerReg);
            ps.setString(2, key);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);

            ResultSet rs = ps.executeQuery();

            List<Timetable> list = new ArrayList<>();

            while (rs.next()) {

                list.add(new Timetable(
                        rs.getString("time_table_id"),
                        rs.getString("department"),
                        rs.getString("lec_id"),
                        rs.getString("courseCode"),
                        rs.getString("admin_id"),
                        rs.getString("day"),
                        rs.getTime("start_time") == null ? null : rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time") == null ? null : rs.getTime("end_time").toLocalTime(),
                        rs.getString("session_type")
                ));
            }

            return list;
        }
    }

    private AcademicSummary calculateAcademicSummary(Connection connection, String studentReg) throws SQLException {

        String sql =
                "SELECT m.courseCode, c.credit, " +
                        "m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical, " +
                        "(SELECT ea.status FROM exam_attendance ea " +
                        "WHERE ea.studentReg = m.StudentReg AND ea.courseCode = m.courseCode LIMIT 1) AS exam_status, " +
                        "EXISTS (SELECT 1 FROM medical md " +
                        "WHERE md.StudentReg = m.StudentReg AND md.courseCode = m.courseCode " +
                        "AND md.approval_status = 'approved' AND LOWER(COALESCE(md.session_type,''))='exam') AS approved_exam_medical " +
                        "FROM marks m " +
                        "INNER JOIN course c ON c.courseCode = m.courseCode " +
                        "WHERE m.StudentReg = ?";

        double gpaPoints = 0.0;
        int gpaCredits = 0;

        double sgpaPoints = 0.0;
        int sgpaCredits = 0;

        boolean withheld = false;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, studentReg);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String courseCode = rs.getString("courseCode");
                int credit = rs.getInt("credit");

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

                GradeResult result = GradeScaleUtil.evaluatePublishedGrade(
                        breakdown,
                        isAttendanceEligible(connection, studentReg, courseCode),
                        rs.getString("exam_status"),
                        rs.getInt("approved_exam_medical") == 1
                );

                if ("MC".equalsIgnoreCase(result.getPublishedGrade())) {
                    withheld = true;
                }

                if (result.getGradePoint() != null) {

                    double gp = result.getGradePoint();

                    sgpaPoints += gp * credit;
                    sgpaCredits += credit;

                    if (!GradeScaleUtil.isEnglishCourse(courseCode)) {
                        gpaPoints += gp * credit;
                        gpaCredits += credit;
                    }
                }
            }
        }

        double cgpa = gpaCredits == 0 ? 0.0 : gpaPoints / gpaCredits;
        double sgpa = sgpaCredits == 0 ? 0.0 : sgpaPoints / sgpaCredits;

        return new AcademicSummary(cgpa, sgpa, withheld);
    }

    private boolean isAttendanceEligible(Connection connection, String studentReg, String courseCode) throws SQLException {
        String sql = "SELECT COALESCE(SUM(CASE "
                + "WHEN a.attendance_status = 'present' THEN 1 "
                + "WHEN a.attendance_status = 'medical' AND EXISTS ( "
                + "SELECT 1 "
                + "FROM medical m "
                + "WHERE m.attendance_id = a.attendance_id "
                + "AND m.approval_status = 'approved'"
                + ") THEN 1 "
                + "ELSE 0 "
                + "END), 0) AS eligible_sessions, "
                + "COUNT(*) AS total_sessions "
                + "FROM attendance a "
                + "WHERE a.StudentReg = ? "
                + "AND a.courseCode = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentReg);
            statement.setString(2, courseCode);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return AttendanceEligibilityUtil.calculatePercentage(
                        rs.getInt("eligible_sessions"),
                        rs.getInt("total_sessions")
                ) >= AttendanceEligibilityUtil.MIN_ELIGIBILITY_PERCENTAGE;
            }
        }
    }

    private static Double nullableDecimal(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }
}
