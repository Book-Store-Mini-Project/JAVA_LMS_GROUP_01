package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.util.AssessmentStructureUtil;
import com.example.java_lms_group_01.util.AttendanceEligibilityUtil;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.GradeScaleUtil;

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
    public List<AttendanceMedicalRecord> findAttendanceMedicalByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT a.attendance_id, a.StudentReg, a.courseCode, a.SubmissionDate, a.session_type, a.attendance_status, a.tech_officer_reg,
                       m.medical_id, m.Description, m.approval_status
                FROM attendance a
                INNER JOIN course c ON c.courseCode = a.courseCode
                LEFT JOIN medical m ON m.attendance_id = a.attendance_id
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR a.StudentReg LIKE ? OR a.courseCode LIKE ?)
                ORDER BY a.attendance_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceMedicalRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapAttendanceMedicalRecord(rs));
                }
                return rows;
            }
        }
    }

    public void updateMedicalDecision(String lecturerReg, int medicalId, int attendanceId, String approvalStatus, String attendanceStatus) throws SQLException {
        String medicalSql = """
                UPDATE medical
                SET approval_status = ?, approved_by_lecturer = ?, approved_at = CURRENT_DATE
                WHERE medical_id = ?
                  AND attendance_id IN (
                      SELECT a.attendance_id
                      FROM attendance a
                      INNER JOIN course c ON c.courseCode = a.courseCode
                      WHERE c.lecturerRegistrationNo = ?
                  )
                """;
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
        } catch (Exception e) {
            connection.rollback();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public List<EligibilityRecord> findEligibilityByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT e.studentReg AS StudentReg, u.firstName, u.lastName, e.courseCode,
                       SUM(CASE
                               WHEN a.attendance_id IS NOT NULL
                                    AND (a.attendance_status = 'present'
                                         OR (a.attendance_status = 'medical' AND m.approval_status = 'approved'))
                               THEN 1
                               ELSE 0
                           END) AS eligible_sessions,
                       COUNT(a.attendance_id) AS total_sessions
                FROM enrollment e
                INNER JOIN course c ON c.courseCode = e.courseCode
                INNER JOIN student s ON s.registrationNo = e.studentReg
                INNER JOIN users u ON u.user_id = s.registrationNo
                LEFT JOIN attendance a ON a.StudentReg = e.studentReg AND a.courseCode = e.courseCode
                LEFT JOIN medical m ON m.attendance_id = a.attendance_id
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR e.studentReg LIKE ? OR e.courseCode LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?)
                GROUP BY e.studentReg, u.firstName, u.lastName, e.courseCode
                ORDER BY e.studentReg, e.courseCode
                """;
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
                List<EligibilityRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapEligibilityRecord(rs));
                }
                return rows;
            }
        }
    }

    // Load marks and calculated performance for students taught by this lecturer.
    public List<PerformanceRecord> findPerformanceByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT m.StudentReg, u.firstName, u.lastName, m.courseCode, s.GPA,
                       m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical,
                       EXISTS (
                           SELECT 1
                           FROM exam_attendance ea
                           WHERE ea.studentReg = m.StudentReg
                             AND ea.courseCode = m.courseCode
                             AND ea.status = 'present'
                       ) AS exam_present,
                       EXISTS (
                           SELECT 1
                           FROM medical md
                           WHERE md.StudentReg = m.StudentReg
                             AND md.courseCode = m.courseCode
                             AND md.approval_status = 'approved'
                             AND LOWER(COALESCE(md.session_type, '')) = 'exam'
                       ) AS approved_exam_medical
                FROM marks m
                INNER JOIN course c ON c.courseCode = m.courseCode
                INNER JOIN student s ON s.registrationNo = m.StudentReg
                INNER JOIN users u ON u.user_id = s.registrationNo
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR m.StudentReg LIKE ? OR m.courseCode LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ?)
                ORDER BY m.StudentReg, m.courseCode
                """;
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
                List<PerformanceRecord> rows = new ArrayList<>();
                Map<String, AcademicSummary> academicSummaryByStudent = new HashMap<>();
                while (rs.next()) {
                    String studentReg = safe(rs.getString("StudentReg"));
                    String courseCode = safe(rs.getString("courseCode"));
                    AcademicSummary summary = academicSummaryByStudent.get(studentReg);
                    if (summary == null) {
                        summary = calculateAcademicSummary(connection, studentReg);
                        academicSummaryByStudent.put(studentReg, summary);
                    }
                    rows.add(mapPerformanceRecord(connection, rs, courseCode, summary));
                }
                return rows;
            }
        }
    }

    private AcademicSummary calculateAcademicSummary(Connection connection, String studentReg) throws SQLException {
        String sql = """
                SELECT m.courseCode, c.credit, m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical,
                       EXISTS (
                           SELECT 1
                           FROM exam_attendance ea
                           WHERE ea.studentReg = m.StudentReg
                             AND ea.courseCode = m.courseCode
                             AND ea.status = 'present'
                       ) AS exam_present,
                       EXISTS (
                           SELECT 1
                           FROM medical md
                           WHERE md.StudentReg = m.StudentReg
                             AND md.courseCode = m.courseCode
                             AND md.approval_status = 'approved'
                             AND LOWER(COALESCE(md.session_type, '')) = 'exam'
                       ) AS approved_exam_medical
                FROM marks m
                INNER JOIN course c ON c.courseCode = m.courseCode
                WHERE m.StudentReg = ?
                """;
        double gpaWeightedPoints = 0.0;
        int gpaCredits = 0;
        double sgpaWeightedPoints = 0.0;
        int sgpaCredits = 0;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentReg);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String courseCode = safe(rs.getString("courseCode"));
                    AssessmentStructureUtil.MarkBreakdown breakdown = AssessmentStructureUtil.calculateMarkBreakdown(
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
                    GradeScaleUtil.GradeResult gradeResult = GradeScaleUtil.evaluatePublishedGrade(
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

    public void addMarks(String lecturerReg, MarksMutation mutation) throws SQLException {
        String sql = """
                INSERT INTO marks (
                  LectureReg, StudentReg, courseCode, quiz_1, quiz_2, quiz_3,
                  assessment, Project, mid_term, final_theory, final_practical
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lecturerReg);
            bindMarksMutation(statement, mutation);
            statement.executeUpdate();
        }
    }

    public void updateMarks(String lecturerReg, int markId, MarksMutation mutation) throws SQLException {
        String sql = """
                UPDATE marks SET
                  StudentReg = ?, courseCode = ?, quiz_1 = ?, quiz_2 = ?, quiz_3 = ?,
                  assessment = ?, Project = ?, mid_term = ?, final_theory = ?, final_practical = ?
                WHERE mark_id = ? AND LectureReg = ?
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindMarksMutation(statement, mutation);
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

    public List<MarksRecord> findMarksByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT mark_id, StudentReg, courseCode, quiz_1, quiz_2, quiz_3, assessment, Project, mid_term, final_theory, final_practical
                FROM marks
                WHERE LectureReg = ? AND (? = '' OR StudentReg LIKE ? OR courseCode LIKE ?)
                ORDER BY mark_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<MarksRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapMarksRecord(rs));
                }
                return rows;
            }
        }
    }

    public int addMaterial(String lecturerReg, MaterialMutation mutation) throws SQLException {
        String sql = """
                INSERT INTO lecture_materials (courseCode, name, path, material_type)
                SELECT ?, ?, ?, ?
                WHERE EXISTS (
                    SELECT 1
                    FROM course
                    WHERE courseCode = ? AND lecturerRegistrationNo = ?
                )
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, mutation.getCourseCode());
            statement.setString(2, mutation.getName());
            statement.setString(3, mutation.getPath());
            statement.setString(4, mutation.getMaterialType());
            statement.setString(5, mutation.getCourseCode());
            statement.setString(6, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public int updateMaterial(String lecturerReg, int materialId, MaterialMutation mutation) throws SQLException {
        String sql = """
                UPDATE lecture_materials
                SET courseCode = ?, name = ?, path = ?, material_type = ?
                WHERE material_id = ?
                  AND courseCode IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                  AND ? IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, mutation.getCourseCode());
            statement.setString(2, mutation.getName());
            statement.setString(3, mutation.getPath());
            statement.setString(4, mutation.getMaterialType());
            statement.setInt(5, materialId);
            statement.setString(6, lecturerReg);
            statement.setString(7, mutation.getCourseCode());
            statement.setString(8, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public int deleteMaterial(String lecturerReg, int materialId) throws SQLException {
        String sql = """
                DELETE FROM lecture_materials
                WHERE material_id = ?
                  AND courseCode IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, materialId);
            statement.setString(2, lecturerReg);
            return statement.executeUpdate();
        }
    }

    public List<MaterialRecord> findMaterialsByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT material_id, courseCode, name, path, material_type
                FROM lecture_materials
                WHERE courseCode IN (
                    SELECT courseCode
                    FROM course
                    WHERE lecturerRegistrationNo = ?
                )
                AND (? = '' OR courseCode LIKE ? OR name LIKE ?)
                ORDER BY material_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<MaterialRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapMaterialRecord(rs));
                }
                return rows;
            }
        }
    }

    public List<StudentRecord> findStudentsByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT DISTINCT s.registrationNo, u.firstName, u.lastName, u.email, u.phoneNumber, s.department, s.status
                FROM student s
                INNER JOIN users u ON u.user_id = s.registrationNo
                INNER JOIN enrollment e ON e.studentReg = s.registrationNo
                INNER JOIN course c ON c.courseCode = e.courseCode
                WHERE c.lecturerRegistrationNo = ?
                  AND (? = '' OR s.registrationNo LIKE ? OR u.firstName LIKE ? OR u.lastName LIKE ? OR s.department LIKE ?)
                ORDER BY s.registrationNo
                """;
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
                List<StudentListRow> baseRows = new ArrayList<>();
                while (rs.next()) {
                    baseRows.add(mapStudentListRow(rs));
                }

                List<StudentRecord> rows = new ArrayList<>();
                for (StudentListRow baseRow : baseRows) {
                    AcademicSummary summary = calculateAcademicSummary(connection, baseRow.getRegNo());
                    rows.add(new StudentRecord(
                            baseRow.getRegNo(),
                            baseRow.getName(),
                            baseRow.getEmail(),
                            baseRow.getPhone(),
                            baseRow.getDepartment(),
                            baseRow.getStatus(),
                            String.format("%.2f", summary.getGpa())
                    ));
                }
                return rows;
            }
        }
    }

    public List<TimetableRecord> findTimetableByLecturer(String lecturerReg, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT time_table_id, department, lec_id, courseCode, admin_id, day, start_time, end_time, session_type
                FROM timetable
                WHERE lec_id = ?
                  AND (? = '' OR courseCode LIKE ? OR day LIKE ? OR time_table_id LIKE ?)
                ORDER BY day, start_time
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, lecturerReg);
            statement.setString(2, safeKeyword);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            try (ResultSet rs = statement.executeQuery()) {
                List<TimetableRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapTimetableRecord(rs));
                }
                return rows;
            }
        }
    }

    private AttendanceMedicalRecord mapAttendanceMedicalRecord(ResultSet rs) throws SQLException {
        return new AttendanceMedicalRecord(
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                dateToString(rs.getDate("SubmissionDate")),
                safe(rs.getString("session_type")),
                safe(rs.getString("attendance_status")),
                rs.getObject("medical_id") == null ? "" : String.valueOf(rs.getInt("medical_id")),
                safe(rs.getString("Description")),
                safe(rs.getString("approval_status")),
                safe(rs.getString("tech_officer_reg"))
        );
    }

    private EligibilityRecord mapEligibilityRecord(ResultSet rs) throws SQLException {
        return new EligibilityRecord(
                safe(rs.getString("StudentReg")),
                fullName(rs),
                safe(rs.getString("courseCode")),
                rs.getInt("eligible_sessions"),
                rs.getInt("total_sessions")
        );
    }

    private PerformanceRecord mapPerformanceRecord(Connection connection, ResultSet rs, String courseCode,
                                                   AcademicSummary summary) throws SQLException {
        AssessmentStructureUtil.MarkBreakdown breakdown = calculateMarkBreakdown(connection, courseCode, rs);
        GradeScaleUtil.GradeResult gradeResult = GradeScaleUtil.evaluatePublishedGrade(
                breakdown,
                isAttendanceEligible(connection, rs.getString("StudentReg"), courseCode),
                rs.getInt("exam_present") == 1,
                rs.getInt("approved_exam_medical") == 1
        );
        return new PerformanceRecord(
                safe(rs.getString("StudentReg")),
                fullName(rs),
                courseCode,
                breakdown.getCaMarks(),
                breakdown.getEndMarks(),
                breakdown.getTotalMarks(),
                gradeResult.getPublishedGrade(),
                summary.getGpa(),
                summary.getSgpa()
        );
    }

    private AssessmentStructureUtil.MarkBreakdown calculateMarkBreakdown(Connection connection, String courseCode,
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
        String sql = """
                SELECT COALESCE(SUM(CASE
                           WHEN a.attendance_status = 'present' THEN 1
                           WHEN a.attendance_status = 'medical' AND EXISTS (
                               SELECT 1
                               FROM medical m
                               WHERE m.attendance_id = a.attendance_id
                                 AND m.approval_status = 'approved'
                           ) THEN 1
                           ELSE 0
                       END), 0) AS eligible_sessions,
                       COUNT(*) AS total_sessions
                FROM attendance a
                WHERE a.StudentReg = ?
                  AND a.courseCode = ?
                """;
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

    private MarksRecord mapMarksRecord(ResultSet rs) throws SQLException {
        return new MarksRecord(
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

    private MaterialRecord mapMaterialRecord(ResultSet rs) throws SQLException {
        return new MaterialRecord(
                String.valueOf(rs.getInt("material_id")),
                safe(rs.getString("courseCode")),
                safe(rs.getString("name")),
                safe(rs.getString("path")),
                safe(rs.getString("material_type"))
        );
    }

    private StudentListRow mapStudentListRow(ResultSet rs) throws SQLException {
        return new StudentListRow(
                safe(rs.getString("registrationNo")),
                fullName(rs),
                safe(rs.getString("email")),
                safe(rs.getString("phoneNumber")),
                safe(rs.getString("department")),
                safe(rs.getString("status"))
        );
    }

    private TimetableRecord mapTimetableRecord(ResultSet rs) throws SQLException {
        return new TimetableRecord(
                safe(rs.getString("time_table_id")),
                safe(rs.getString("department")),
                safe(rs.getString("lec_id")),
                safe(rs.getString("courseCode")),
                safe(rs.getString("admin_id")),
                safe(rs.getString("day")),
                timeToString(rs.getTime("start_time")),
                timeToString(rs.getTime("end_time")),
                safe(rs.getString("session_type"))
        );
    }

    private String fullName(ResultSet rs) throws SQLException {
        return (safe(rs.getString("firstName")) + " " + safe(rs.getString("lastName"))).trim();
    }

    private void bindMarksMutation(PreparedStatement statement, MarksMutation mutation) throws SQLException {
        statement.setString(1, mutation.getStudentReg());
        statement.setString(2, mutation.getCourseCode());
        setNullableDecimal(statement, 3, mutation.getQuiz1());
        setNullableDecimal(statement, 4, mutation.getQuiz2());
        setNullableDecimal(statement, 5, mutation.getQuiz3());
        setNullableDecimal(statement, 6, mutation.getAssessment());
        setNullableDecimal(statement, 7, mutation.getProject());
        setNullableDecimal(statement, 8, mutation.getMidTerm());
        setNullableDecimal(statement, 9, mutation.getFinalTheory());
        setNullableDecimal(statement, 10, mutation.getFinalPractical());
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

    private static String timeToString(java.sql.Time time) {
        return time == null ? "" : time.toString();
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

    public static class AttendanceMedicalRecord {
        private final String attendanceId;
        private final String studentReg;
        private final String courseCode;
        private final String date;
        private final String sessionType;
        private final String attendanceStatus;
        private final String medicalId;
        private final String medicalDescription;
        private final String medicalApprovalStatus;
        private final String techOfficerReg;

        public AttendanceMedicalRecord(String attendanceId, String studentReg, String courseCode, String date,
                                       String sessionType, String attendanceStatus, String medicalId,
                                       String medicalDescription, String medicalApprovalStatus, String techOfficerReg) {
            this.attendanceId = attendanceId;
            this.studentReg = studentReg;
            this.courseCode = courseCode;
            this.date = date;
            this.sessionType = sessionType;
            this.attendanceStatus = attendanceStatus;
            this.medicalId = medicalId;
            this.medicalDescription = medicalDescription;
            this.medicalApprovalStatus = medicalApprovalStatus;
            this.techOfficerReg = techOfficerReg;
        }

        public String getAttendanceId() { return attendanceId; }
        public String getStudentReg() { return studentReg; }
        public String getCourseCode() { return courseCode; }
        public String getDate() { return date; }
        public String getSessionType() { return sessionType; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public String getMedicalId() { return medicalId; }
        public String getMedicalDescription() { return medicalDescription; }
        public String getMedicalApprovalStatus() { return medicalApprovalStatus; }
        public String getTechOfficerReg() { return techOfficerReg; }
    }

    public static class EligibilityRecord {
        private final String studentReg;
        private final String studentName;
        private final String courseCode;
        private final int eligibleSessions;
        private final int totalSessions;

        public EligibilityRecord(String studentReg, String studentName, String courseCode, int eligibleSessions, int totalSessions) {
            this.studentReg = studentReg;
            this.studentName = studentName;
            this.courseCode = courseCode;
            this.eligibleSessions = eligibleSessions;
            this.totalSessions = totalSessions;
        }

        public String getStudentReg() { return studentReg; }
        public String getStudentName() { return studentName; }
        public String getCourseCode() { return courseCode; }
        public int getEligibleSessions() { return eligibleSessions; }
        public int getTotalSessions() { return totalSessions; }
    }

    public static class PerformanceRecord {
        private final String studentReg;
        private final String studentName;
        private final String courseCode;
        private final double caMarks;
        private final double endMarks;
        private final double totalMarks;
        private final String publishedGrade;
        private final Double gpa;
        private final Double sgpa;

        public PerformanceRecord(String studentReg, String studentName, String courseCode, double caMarks, double endMarks,
                                 double totalMarks, String publishedGrade, Double gpa, Double sgpa) {
            this.studentReg = studentReg;
            this.studentName = studentName;
            this.courseCode = courseCode;
            this.caMarks = caMarks;
            this.endMarks = endMarks;
            this.totalMarks = totalMarks;
            this.publishedGrade = publishedGrade;
            this.gpa = gpa;
            this.sgpa = sgpa;
        }

        public String getStudentReg() { return studentReg; }
        public String getStudentName() { return studentName; }
        public String getCourseCode() { return courseCode; }
        public double getCaMarks() { return caMarks; }
        public double getEndMarks() { return endMarks; }
        public double getTotalMarks() { return totalMarks; }
        public String getPublishedGrade() { return publishedGrade; }
        public Double getGpa() { return gpa; }
        public Double getSgpa() { return sgpa; }
    }

    private static class AcademicSummary {
        private final double gpa;
        private final double sgpa;

        public AcademicSummary(double gpa, double sgpa) {
            this.gpa = gpa;
            this.sgpa = sgpa;
        }

        public double getGpa() { return gpa; }
        public double getSgpa() { return sgpa; }
    }

    public static class MarksMutation {
        private final String studentReg;
        private final String courseCode;
        private final Double quiz1;
        private final Double quiz2;
        private final Double quiz3;
        private final Double assessment;
        private final Double project;
        private final Double midTerm;
        private final Double finalTheory;
        private final Double finalPractical;

        public MarksMutation(String studentReg, String courseCode, Double quiz1, Double quiz2, Double quiz3,
                             Double assessment, Double project, Double midTerm, Double finalTheory,
                             Double finalPractical) {
            this.studentReg = studentReg;
            this.courseCode = courseCode;
            this.quiz1 = quiz1;
            this.quiz2 = quiz2;
            this.quiz3 = quiz3;
            this.assessment = assessment;
            this.project = project;
            this.midTerm = midTerm;
            this.finalTheory = finalTheory;
            this.finalPractical = finalPractical;
        }

        public String getStudentReg() { return studentReg; }
        public String getCourseCode() { return courseCode; }
        public Double getQuiz1() { return quiz1; }
        public Double getQuiz2() { return quiz2; }
        public Double getQuiz3() { return quiz3; }
        public Double getAssessment() { return assessment; }
        public Double getProject() { return project; }
        public Double getMidTerm() { return midTerm; }
        public Double getFinalTheory() { return finalTheory; }
        public Double getFinalPractical() { return finalPractical; }
    }

    public static class MarksRecord {
        private final String markId;
        private final String studentReg;
        private final String courseCode;
        private final String quiz1;
        private final String quiz2;
        private final String quiz3;
        private final String assessment;
        private final String project;
        private final String midTerm;
        private final String finalTheory;
        private final String finalPractical;

        public MarksRecord(String markId, String studentReg, String courseCode, String quiz1, String quiz2,
                           String quiz3, String assessment, String project, String midTerm,
                           String finalTheory, String finalPractical) {
            this.markId = markId;
            this.studentReg = studentReg;
            this.courseCode = courseCode;
            this.quiz1 = quiz1;
            this.quiz2 = quiz2;
            this.quiz3 = quiz3;
            this.assessment = assessment;
            this.project = project;
            this.midTerm = midTerm;
            this.finalTheory = finalTheory;
            this.finalPractical = finalPractical;
        }

        public String getMarkId() { return markId; }
        public String getStudentReg() { return studentReg; }
        public String getCourseCode() { return courseCode; }
        public String getQuiz1() { return quiz1; }
        public String getQuiz2() { return quiz2; }
        public String getQuiz3() { return quiz3; }
        public String getAssessment() { return assessment; }
        public String getProject() { return project; }
        public String getMidTerm() { return midTerm; }
        public String getFinalTheory() { return finalTheory; }
        public String getFinalPractical() { return finalPractical; }
    }

    public static class MaterialMutation {
        private final String courseCode;
        private final String name;
        private final String path;
        private final String materialType;

        public MaterialMutation(String courseCode, String name, String path, String materialType) {
            this.courseCode = courseCode;
            this.name = name;
            this.path = path;
            this.materialType = materialType;
        }

        public String getCourseCode() { return courseCode; }
        public String getName() { return name; }
        public String getPath() { return path; }
        public String getMaterialType() { return materialType; }
    }

    public static class MaterialRecord {
        private final String materialId;
        private final String courseCode;
        private final String name;
        private final String path;
        private final String type;

        public MaterialRecord(String materialId, String courseCode, String name, String path, String type) {
            this.materialId = materialId;
            this.courseCode = courseCode;
            this.name = name;
            this.path = path;
            this.type = type;
        }

        public String getMaterialId() { return materialId; }
        public String getCourseCode() { return courseCode; }
        public String getName() { return name; }
        public String getPath() { return path; }
        public String getType() { return type; }
    }

    public static class StudentRecord {
        private final String regNo;
        private final String name;
        private final String email;
        private final String phone;
        private final String department;
        private final String status;
        private final String gpa;

        public StudentRecord(String regNo, String name, String email, String phone, String department, String status, String gpa) {
            this.regNo = regNo;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.department = department;
            this.status = status;
            this.gpa = gpa;
        }

        public String getRegNo() { return regNo; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getDepartment() { return department; }
        public String getStatus() { return status; }
        public String getGpa() { return gpa; }
    }

    private static class StudentListRow {
        private final String regNo;
        private final String name;
        private final String email;
        private final String phone;
        private final String department;
        private final String status;

        public StudentListRow(String regNo, String name, String email, String phone, String department, String status) {
            this.regNo = regNo;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.department = department;
            this.status = status;
        }

        public String getRegNo() { return regNo; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getDepartment() { return department; }
        public String getStatus() { return status; }
    }

    public static class TimetableRecord {
        private final String timetableId;
        private final String department;
        private final String lecId;
        private final String courseCode;
        private final String adminId;
        private final String day;
        private final String startTime;
        private final String endTime;
        private final String sessionType;

        public TimetableRecord(String timetableId, String department, String lecId, String courseCode,
                               String adminId, String day, String startTime, String endTime,
                               String sessionType) {
            this.timetableId = timetableId;
            this.department = department;
            this.lecId = lecId;
            this.courseCode = courseCode;
            this.adminId = adminId;
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.sessionType = sessionType;
        }

        public String getTimetableId() { return timetableId; }
        public String getDepartment() { return department; }
        public String getLecId() { return lecId; }
        public String getCourseCode() { return courseCode; }
        public String getAdminId() { return adminId; }
        public String getDay() { return day; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getSessionType() { return sessionType; }
    }
}
