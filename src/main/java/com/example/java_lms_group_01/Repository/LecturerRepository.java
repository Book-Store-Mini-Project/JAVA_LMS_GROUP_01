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
import com.example.java_lms_group_01.util.GradeResult;
import com.example.java_lms_group_01.util.GradeScaleUtil;
import com.example.java_lms_group_01.util.MarkBreakdown;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database access used by lecturer screens.
 * It contains the main lecturer operations such as marks, eligibility,
 * attendance-medical views, materials, student lists, and timetable data.
 */
public class LecturerRepository {

    // Load attendance rows together with any linked medical request for the lecturer's courses.
    public List<Attendance> findAttendanceMedicalByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT a.attendance_id, a.StudentReg, a.courseCode, a.SubmissionDate, a.session_type, a.attendance_status, a.tech_officer_reg, "
                + "m.medical_id, m.Description, m.approval_status "
                + "FROM attendance a "
                + "INNER JOIN course c ON c.courseCode = a.courseCode "
                + "LEFT JOIN medical m ON m.attendance_id = a.attendance_id "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND (? = '' OR a.StudentReg LIKE ? OR a.courseCode LIKE ?) "
                + "ORDER BY a.attendance_id DESC";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Attendance> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapAttendanceMedicalRecord(rs));
                }
                return rows;
            }
        }
    }

    public void updateMedicalDecision(String lecturerReg, int medicalId, int attendanceId, String approvalStatus, String attendanceStatus) throws SQLException {
        String medicalSql = "UPDATE medical "
                + "SET approval_status = ?, approved_by_lecturer = ?, approved_at = CURRENT_DATE "
                + "WHERE medical_id = ? "
                + "AND attendance_id IN ( "
                + "SELECT a.attendance_id "
                + "FROM attendance a "
                + "INNER JOIN course c ON c.courseCode = a.courseCode "
                + "WHERE c.lecturerRegistrationNo = ?"
                + ")";
        String attendanceSql = "UPDATE attendance SET attendance_status = ? WHERE attendance_id = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement medicalStatement = connection.prepareStatement(medicalSql);
             PreparedStatement attendanceStatement = connection.prepareStatement(attendanceSql)) {
            medicalStatement.setString(1, approvalStatus);
            medicalStatement.setString(2, lecturerReg);
            medicalStatement.setInt(3, medicalId);
            medicalStatement.setString(4, lecturerReg);
            int medicalUpdated = medicalStatement.executeUpdate();
            if (medicalUpdated == 0) {
                throw new SQLException("You can approve only medical records for your own courses.");
            }

            attendanceStatement.setString(1, attendanceStatus);
            attendanceStatement.setInt(2, attendanceId);
            attendanceStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public List<Eligibility> findEligibilityByLecturer(String lecturerReg, String studentKeyword,
                                                       String courseCode, String batch) throws SQLException {
        String safeStudentKeyword = studentKeyword == null ? "" : studentKeyword.trim();
        String safeCourseCode = courseCode == null ? "" : courseCode.trim();
        String safeBatch = batch == null ? "" : batch.trim();
        String sql = "SELECT e.studentReg AS StudentReg, u.firstName, u.lastName, e.courseCode, "
                + "SUM(CASE "
                + "WHEN a.attendance_id IS NOT NULL "
                + "AND (a.attendance_status = 'present' "
                + "OR (a.attendance_status = 'medical' AND m.approval_status = 'approved')) "
                + "THEN 1 "
                + "ELSE 0 "
                + "END) AS eligible_sessions, "
                + "COUNT(a.attendance_id) AS total_sessions, "
                + "MAX(mk.quiz_1) AS quiz_1, "
                + "MAX(mk.quiz_2) AS quiz_2, "
                + "MAX(mk.quiz_3) AS quiz_3, "
                + "MAX(mk.assessment) AS assessment, "
                + "MAX(mk.Project) AS Project, "
                + "MAX(mk.mid_term) AS mid_term, "
                + "MAX(mk.final_theory) AS final_theory, "
                + "MAX(mk.final_practical) AS final_practical "
                + "FROM enrollment e "
                + "INNER JOIN course c ON c.courseCode = e.courseCode "
                + "INNER JOIN student s ON s.registrationNo = e.studentReg "
                + "INNER JOIN users u ON u.user_id = s.registrationNo "
                + "LEFT JOIN attendance a ON a.StudentReg = e.studentReg AND a.courseCode = e.courseCode "
                + "LEFT JOIN medical m ON m.attendance_id = a.attendance_id "
                + "LEFT JOIN marks mk ON mk.StudentReg = e.studentReg AND mk.courseCode = e.courseCode "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND (? = '' OR COALESCE(s.batch, '') = ?) "
                + "AND (? = '' OR e.studentReg LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?) "
                + "AND (? = '' OR e.courseCode = ?) "
                + "GROUP BY e.studentReg, u.firstName, u.lastName, e.courseCode "
                + "ORDER BY e.studentReg, e.courseCode";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String studentPattern = "%" + safeStudentKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeBatch);
            statement.setString(3, safeBatch);
            statement.setString(4, safeStudentKeyword);
            statement.setString(5, studentPattern);
            statement.setString(6, studentPattern);
            statement.setString(7, studentPattern);
            statement.setString(8, safeCourseCode);
            statement.setString(9, safeCourseCode);
            try (ResultSet rs = statement.executeQuery()) {
                List<Eligibility> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapEligibilityRecord(connection, rs));
                }
                return rows;
            }
        }
    }

    // Load marks and calculated performance for students taught by this lecturer.
    public List<Performance> findPerformanceByLecturer(String lecturerReg, String studentKeyword,
                                                       String courseCode, String batch) throws SQLException {
        String safeStudentKeyword = studentKeyword == null ? "" : studentKeyword.trim();
        String safeCourseCode = courseCode == null ? "" : courseCode.trim();
        String safeBatch = batch == null ? "" : batch.trim();
        String sql = "SELECT m.StudentReg, u.firstName, u.lastName, m.courseCode, c.name, "
                + "m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical, "
                + "EXISTS ( "
                + "SELECT 1 "
                + "FROM exam_attendance ea "
                + "WHERE ea.studentReg = m.StudentReg "
                + "AND ea.courseCode = m.courseCode "
                + "AND ea.status = 'present'"
                + ") AS exam_present, "
                + "EXISTS ( "
                + "SELECT 1 "
                + "FROM medical md "
                + "WHERE md.StudentReg = m.StudentReg "
                + "AND md.courseCode = m.courseCode "
                + "AND md.approval_status = 'approved' "
                + "AND LOWER(COALESCE(md.session_type, '')) = 'exam'"
                + ") AS approved_exam_medical "
                + "FROM marks m "
                + "INNER JOIN course c ON c.courseCode = m.courseCode "
                + "INNER JOIN student s ON s.registrationNo = m.StudentReg "
                + "INNER JOIN users u ON u.user_id = s.registrationNo "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND (? = '' OR COALESCE(s.batch, '') = ?) "
                + "AND (? = '' OR m.StudentReg LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?) "
                + "AND (? = '' OR m.courseCode = ?) "
                + "ORDER BY m.StudentReg, m.courseCode";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String studentPattern = "%" + safeStudentKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeBatch);
            statement.setString(3, safeBatch);
            statement.setString(4, safeStudentKeyword);
            statement.setString(5, studentPattern);
            statement.setString(6, studentPattern);
            statement.setString(7, studentPattern);
            statement.setString(8, safeCourseCode);
            statement.setString(9, safeCourseCode);
            try (ResultSet rs = statement.executeQuery()) {
                List<Performance> rows = new ArrayList<>();
                Map<String, AcademicSummary> academicSummaryByStudent = new HashMap<>();
                while (rs.next()) {
                    String currentStudentReg = safe(rs.getString("StudentReg"));
                    String currentCourseCode = safe(rs.getString("courseCode"));
                    AcademicSummary summary = academicSummaryByStudent.get(currentStudentReg);
                    if (summary == null) {
                        summary = calculateAcademicSummary(connection, currentStudentReg);
                        academicSummaryByStudent.put(currentStudentReg, summary);
                    }
                    rows.add(mapPerformanceRecord(connection, rs, currentCourseCode, summary));
                }
                return rows;
            }
        }
    }

    public List<UndergraduateSummary> findUndergraduateSummariesByLecturer(String lecturerReg, String studentKeyword,
                                                                           String courseCode, String batch) throws SQLException {
        String safeStudentKeyword = studentKeyword == null ? "" : studentKeyword.trim();
        String safeCourseCode = courseCode == null ? "" : courseCode.trim();
        String safeBatch = batch == null ? "" : batch.trim();
        String sql = "SELECT DISTINCT s.registrationNo, u.firstName, u.lastName "
                + "FROM student s "
                + "INNER JOIN users u ON u.user_id = s.registrationNo "
                + "INNER JOIN enrollment e ON e.studentReg = s.registrationNo "
                + "INNER JOIN course c ON c.courseCode = e.courseCode "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND (? = '' OR COALESCE(s.batch, '') = ?) "
                + "AND (? = '' OR s.registrationNo LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?) "
                + "AND (? = '' OR e.courseCode = ?) "
                + "ORDER BY s.registrationNo";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String studentPattern = "%" + safeStudentKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeBatch);
            statement.setString(3, safeBatch);
            statement.setString(4, safeStudentKeyword);
            statement.setString(5, studentPattern);
            statement.setString(6, studentPattern);
            statement.setString(7, studentPattern);
            statement.setString(8, safeCourseCode);
            statement.setString(9, safeCourseCode);
            try (ResultSet rs = statement.executeQuery()) {
                List<UndergraduateSummary> rows = new ArrayList<>();
                while (rs.next()) {
                    AcademicSummary summary = calculateAcademicSummary(connection, safe(rs.getString("registrationNo")));
                    rows.add(new UndergraduateSummary(
                            safe(rs.getString("registrationNo")),
                            fullName(rs),
                            String.format("%.2f", summary.getSgpa()),
                            String.format("%.2f", summary.getCgpa())
                    ));
                }
                return rows;
            }
        }
    }

    private AcademicSummary calculateAcademicSummary(Connection connection, String studentReg) throws SQLException {
        String sql = "SELECT m.courseCode, c.credit, m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical, "
                + "EXISTS ( "
                + "SELECT 1 "
                + "FROM exam_attendance ea "
                + "WHERE ea.studentReg = m.StudentReg "
                + "AND ea.courseCode = m.courseCode "
                + "AND ea.status = 'present'"
                + ") AS exam_present, "
                + "EXISTS ( "
                + "SELECT 1 "
                + "FROM medical md "
                + "WHERE md.StudentReg = m.StudentReg "
                + "AND md.courseCode = m.courseCode "
                + "AND md.approval_status = 'approved' "
                + "AND LOWER(COALESCE(md.session_type, '')) = 'exam'"
                + ") AS approved_exam_medical "
                + "FROM marks m "
                + "INNER JOIN course c ON c.courseCode = m.courseCode "
                + "WHERE m.StudentReg = ?";
        double gpaWeightedPoints = 0.0;
        int gpaCredits = 0;
        double sgpaWeightedPoints = 0.0;
        int sgpaCredits = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentReg);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String courseCode = safe(rs.getString("courseCode"));
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
                    int credit = rs.getInt("credit");
                    GradeResult gradeResult = GradeScaleUtil.evaluatePublishedGrade(
                            breakdown,
                            isAttendanceEligible(connection, studentReg, courseCode),
                            rs.getInt("exam_present") == 1,
                            rs.getInt("approved_exam_medical") == 1
                    );
                    if (gradeResult.getGradePoint() != null) {
                        sgpaWeightedPoints += gradeResult.getGradePoint() * credit;
                        sgpaCredits += credit;
                        if (!GradeScaleUtil.isEnglishCourse(courseCode)) {
                            gpaWeightedPoints += gradeResult.getGradePoint() * credit;
                            gpaCredits += credit;
                        }
                    }
                }
            }
        }
        return new AcademicSummary(
                gpaCredits == 0 ? 0.0 : gpaWeightedPoints / gpaCredits,
                sgpaCredits == 0 ? 0.0 : sgpaWeightedPoints / sgpaCredits
        );
    }

    public void addMarks(String lecturerReg, MarkRequest request) throws SQLException {
        String sql = "INSERT INTO marks ("
                + "LectureReg, StudentReg, courseCode, quiz_1, quiz_2, quiz_3, "
                + "assessment, Project, mid_term, final_theory, final_practical"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lecturerReg);
            bindMarkRequest(statement, request);
            statement.executeUpdate();
        }
    }

    public void updateMarks(String lecturerReg, int markId, MarkRequest request) throws SQLException {
        String sql = "UPDATE marks SET "
                + "StudentReg = ?, courseCode = ?, quiz_1 = ?, quiz_2 = ?, quiz_3 = ?, "
                + "assessment = ?, Project = ?, mid_term = ?, final_theory = ?, final_practical = ? "
                + "WHERE mark_id = ? AND LectureReg = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindMarkRequest(statement, request);
            statement.setInt(11, markId);
            statement.setString(12, lecturerReg);
            statement.executeUpdate();
        }
    }

    public void deleteMarks(String lecturerReg, int markId) throws SQLException {
        String sql = "DELETE FROM marks WHERE mark_id = ? AND LectureReg = ?";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, markId);
            statement.setString(2, lecturerReg);
            statement.executeUpdate();
        }
    }

    public List<Mark> findMarksByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT mark_id, StudentReg, courseCode, quiz_1, quiz_2, quiz_3, assessment, Project, mid_term, final_theory, final_practical "
                + "FROM marks "
                + "WHERE LectureReg = ? AND (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?) "
                + "ORDER BY mark_id DESC";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Mark> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapMarksRecord(rs));
                }
                return rows;
            }
        }
    }

    public List<String> findBatchesByLecturer(String lecturerReg) throws SQLException {
        String sql = "SELECT DISTINCT s.batch "
                + "FROM student s "
                + "INNER JOIN enrollment e ON e.studentReg = s.registrationNo "
                + "INNER JOIN course c ON c.courseCode = e.courseCode "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND s.batch IS NOT NULL "
                + "AND TRIM(s.batch) <> '' "
                + "ORDER BY s.batch";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lecturerReg);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(safe(rs.getString("batch")));
                }
                return rows;
            }
        }
    }

    public List<String> findStudentRegistrationsByLecturer(String lecturerReg, String batch) throws SQLException {
        String safeBatch = batch == null ? "" : batch.trim();
        String sql = "SELECT DISTINCT s.registrationNo "
                + "FROM student s "
                + "INNER JOIN enrollment e ON e.studentReg = s.registrationNo "
                + "INNER JOIN course c ON c.courseCode = e.courseCode "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND (? = '' OR COALESCE(s.batch, '') = ?) "
                + "ORDER BY s.registrationNo";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lecturerReg);
            statement.setString(2, safeBatch);
            statement.setString(3, safeBatch);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(safe(rs.getString("registrationNo")));
                }
                return rows;
            }
        }
    }

    public int addMaterial(String lecturerReg, MaterialRequest request) throws SQLException {
        String sql = "INSERT INTO lecture_materials (courseCode, name, path, material_type) "
                + "SELECT ?, ?, ?, ? "
                + "WHERE EXISTS ( "
                + "SELECT 1 "
                + "FROM course "
                + "WHERE courseCode = ? AND lecturerRegistrationNo = ?"
                + ")";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getCourseCode());
            statement.setString(2, request.getName());
            statement.setString(3, request.getPath());
            statement.setString(4, request.getMaterialType());
            statement.setString(5, request.getCourseCode());
            statement.setString(6, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public int updateMaterial(String lecturerReg, int materialId, MaterialRequest request) throws SQLException {
        String sql = "UPDATE lecture_materials "
                + "SET courseCode = ?, name = ?, path = ?, material_type = ? "
                + "WHERE material_id = ? "
                + "AND courseCode IN ( "
                + "SELECT courseCode "
                + "FROM course "
                + "WHERE lecturerRegistrationNo = ?"
                + ") "
                + "AND ? IN ( "
                + "SELECT courseCode "
                + "FROM course "
                + "WHERE lecturerRegistrationNo = ?"
                + ")";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, request.getCourseCode());
            statement.setString(2, request.getName());
            statement.setString(3, request.getPath());
            statement.setString(4, request.getMaterialType());
            statement.setInt(5, materialId);
            statement.setString(6, lecturerReg);
            statement.setString(7, request.getCourseCode());
            statement.setString(8, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public int deleteMaterial(String lecturerReg, int materialId) throws SQLException {
        String sql = "DELETE FROM lecture_materials "
                + "WHERE material_id = ? "
                + "AND courseCode IN ( "
                + "SELECT courseCode "
                + "FROM course "
                + "WHERE lecturerRegistrationNo = ?"
                + ")";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            statement.setString(2, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public List<Material> findMaterialsByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT material_id, courseCode, name, path, material_type "
                + "FROM lecture_materials "
                + "WHERE courseCode IN ( "
                + "SELECT courseCode "
                + "FROM course "
                + "WHERE lecturerRegistrationNo = ?"
                + ") "
                + "AND (? = '' OR courseCode LIKE ? OR name LIKE ?) "
                + "ORDER BY material_id DESC";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Material> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapMaterialRecord(rs));
                }
                return rows;
            }
        }
    }

    public List<Student> findStudentsByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT DISTINCT s.registrationNo, u.firstName, u.lastName, u.email, u.phoneNumber, s.department, s.status "
                + "FROM student s "
                + "INNER JOIN users u ON u.user_id = s.registrationNo "
                + "INNER JOIN enrollment e ON e.studentReg = s.registrationNo "
                + "INNER JOIN course c ON c.courseCode = e.courseCode "
                + "WHERE c.lecturerRegistrationNo = ? "
                + "AND (? = '' OR s.registrationNo LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ? OR s.department LIKE ?) "
                + "ORDER BY s.registrationNo";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            statement.setString(6, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Student> baseRows = new ArrayList<>();
                while (rs.next()) {
                    baseRows.add(mapStudentListRow(rs));
                }

                List<Student> rows = new ArrayList<>();
                for (Student baseRow : baseRows) {
                    AcademicSummary summary = calculateAcademicSummary(connection, baseRow.getRegNo());
                    rows.add(new Student(
                            baseRow.getRegNo(),
                            baseRow.getName(),
                            baseRow.getEmail(),
                            baseRow.getPhone(),
                            baseRow.getDepartment(),
                            baseRow.getStatus(),
                            String.format("%.2f", summary.getCgpa())
                    ));
                }
                return rows;
            }
        }
    }

    public List<Timetable> findTimetableByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = "SELECT time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type "
                + "FROM timetable "
                + "WHERE lec_id = ? "
                + "AND (? = '' OR courseCode LIKE ? OR day LIKE ? OR time_table_id LIKE ?) "
                + "ORDER BY day, start_time";
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<Timetable> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapTimetableRecord(rs));
                }
                return rows;
            }
        }
    }

    private Attendance mapAttendanceMedicalRecord(ResultSet rs) throws SQLException {
        return new Attendance(
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                dateToString(rs.getDate("SubmissionDate")),
                safe(rs.getString("session_type")),
                safe(rs.getString("attendance_status")),
                safe(rs.getString("tech_officer_reg")),
                rs.getObject("medical_id") == null ? "" : String.valueOf(rs.getInt("medical_id")),
                safe(rs.getString("Description")),
                safe(rs.getString("approval_status"))
        );
    }

    private Eligibility mapEligibilityRecord(Connection connection, ResultSet rs) throws SQLException {
        String courseCode = safe(rs.getString("courseCode"));
        MarkBreakdown breakdown = calculateMarkBreakdown(connection, courseCode, rs);
        double attendancePercentage = AttendanceEligibilityUtil.calculatePercentage(
                rs.getInt("eligible_sessions"),
                rs.getInt("total_sessions")
        );
        boolean attendanceEligible = attendancePercentage >= AttendanceEligibilityUtil.MIN_ELIGIBILITY_PERCENTAGE;
        boolean caEligible = GradeScaleUtil.meetsCaRequirement(breakdown);
        return new Eligibility(
                safe(rs.getString("StudentReg")),
                fullName(rs),
                courseCode,
                String.valueOf(rs.getInt("eligible_sessions")),
                String.valueOf(rs.getInt("total_sessions")),
                String.format("%.2f%%", attendancePercentage),
                String.format("%.2f", breakdown.getCaMarks()),
                String.format("%.2f", GradeScaleUtil.minimumRequiredMark(breakdown.getCaMaximum())),
                buildEligibilityStatus(attendanceEligible, caEligible)
        );
    }

    private Performance mapPerformanceRecord(Connection connection, ResultSet rs, String courseCode,
                                             AcademicSummary summary) throws SQLException {
        MarkBreakdown breakdown = calculateMarkBreakdown(connection, courseCode, rs);
        GradeResult gradeResult = GradeScaleUtil.evaluatePublishedGrade(
                breakdown,
                isAttendanceEligible(connection, rs.getString("StudentReg"), courseCode),
                rs.getInt("exam_present") == 1,
                rs.getInt("approved_exam_medical") == 1
        );
        return new Performance(
                safe(rs.getString("StudentReg")),
                fullName(rs),
                courseCode,
                safe(rs.getString("name")),
                String.format("%.2f", breakdown.getCaMarks()),
                String.format("%.2f", breakdown.getEndMarks()),
                String.format("%.2f", breakdown.getTotalMarks()),
                gradeResult.getPublishedGrade(),
                String.format("%.2f", summary.getSgpa()),
                String.format("%.2f", summary.getCgpa())
        );
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

    private Mark mapMarksRecord(ResultSet rs) throws SQLException {
        return new Mark(
                String.valueOf(rs.getInt("mark_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                decimal(rs.getObject("quiz_1")),
                decimal(rs.getObject("quiz_2")),
                decimal(rs.getObject("quiz_3")),
                decimal(rs.getObject("assessment")),
                decimal(rs.getObject("Project")),
                decimal(rs.getObject("mid_term")),
                decimal(rs.getObject("final_theory")),
                decimal(rs.getObject("final_practical"))
        );
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

    private Student mapStudentListRow(ResultSet rs) throws SQLException {
        return new Student(
                safe(rs.getString("registrationNo")),
                fullName(rs),
                safe(rs.getString("email")),
                safe(rs.getString("phoneNumber")),
                safe(rs.getString("department")),
                safe(rs.getString("status")),
                ""
        );
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

    private String fullName(ResultSet rs) throws SQLException {
        return (safe(rs.getString("firstName")) + " " + safe(rs.getString("lastName"))).trim();
    }

    private void bindMarkRequest(PreparedStatement statement, MarkRequest request) throws SQLException {
        statement.setString(1, request.getStudentReg());
        statement.setString(2, request.getCourseCode());
        setNullableDecimal(statement, 3, request.getQuiz1());
        setNullableDecimal(statement, 4, request.getQuiz2());
        setNullableDecimal(statement, 5, request.getQuiz3());
        setNullableDecimal(statement, 6, request.getAssessment());
        setNullableDecimal(statement, 7, request.getProject());
        setNullableDecimal(statement, 8, request.getMidTerm());
        setNullableDecimal(statement, 9, request.getFinalTheory());
        setNullableDecimal(statement, 10, request.getFinalPractical());
    }

    private static void setNullableDecimal(PreparedStatement statement, int index, Double value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
            return;
        }
        statement.setDouble(index, value);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String dateToString(java.sql.Date date) {
        return date == null ? "" : date.toString();
    }

    private static String decimal(Object value) {
        if (value == null) {
            return "";
        }
        return String.format("%.2f", ((Number) value).doubleValue());
    }

    private static Double nullableDecimal(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
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
