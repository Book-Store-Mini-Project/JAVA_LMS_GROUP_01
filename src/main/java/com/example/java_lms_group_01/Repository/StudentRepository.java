package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.util.AssessmentStructureUtil;
import com.example.java_lms_group_01.util.AttendanceEligibilityUtil;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.GradeScaleUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for all student-facing pages.
 * Student controllers call this class to load attendance, grades, materials,
 * notices, and timetable data.
 */
public class StudentRepository {

    // Load all attendance rows for one student.
    public List<AttendanceRecord> findAttendanceByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT attendance_id, StudentReg, courseCode, SubmissionDate, session_type, attendance_status, tech_officer_reg
                FROM attendance
                WHERE StudentReg = ?
                ORDER BY SubmissionDate DESC, attendance_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapAttendanceRecord(rs));
                }
                return rows;
            }
        }
    }

    // Load attendance eligibility summary for each enrolled course.
    public List<AttendanceEligibilityRecord> findAttendanceEligibilityByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT e.courseCode,
                       SUM(CASE
                               WHEN a.attendance_id IS NOT NULL
                                    AND (a.attendance_status = 'present'
                                         OR (a.attendance_status = 'medical' AND m.approval_status = 'approved'))
                               THEN 1
                               ELSE 0
                           END) AS eligible_sessions,
                       COUNT(a.attendance_id) AS total_sessions
                FROM enrollment e
                LEFT JOIN attendance a ON a.StudentReg = e.studentReg AND a.courseCode = e.courseCode
                LEFT JOIN medical m ON m.attendance_id = a.attendance_id
                WHERE e.studentReg = ?
                GROUP BY e.courseCode
                ORDER BY e.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<AttendanceEligibilityRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapAttendanceEligibilityRecord(rs));
                }
                return rows;
            }
        }
    }

    // Load the student's enrolled courses.
    public List<CourseRecord> findCoursesByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT c.courseCode, c.name, c.lecturerRegistrationNo, c.department, c.semester, c.credit, c.course_type, e.status
                FROM enrollment e
                INNER JOIN course c ON c.courseCode = e.courseCode
                WHERE e.studentReg = ?
                ORDER BY c.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<CourseRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapCourseRecord(rs));
                }
                return rows;
            }
        }
    }

    // Build published grades and GPA values for the student.
    public GradeSummary findGradeSummary(String registrationNo) throws SQLException {
        String marksSql = """
                SELECT m.StudentReg, m.courseCode, c.name, c.credit, m.quiz_1, m.quiz_2, m.quiz_3, m.assessment, m.Project, m.mid_term, m.final_theory, m.final_practical,
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
                ORDER BY m.courseCode
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        List<GradeRecord> grades = new ArrayList<>();
        double gpaWeightedPoints = 0.0;
        int gpaCredits = 0;
        double sgpaWeightedPoints = 0.0;
        int sgpaCredits = 0;
        try (PreparedStatement statement = connection.prepareStatement(marksSql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    GradeCalculation gradeCalculation = calculateGrade(connection, rs);
                    grades.add(new GradeRecord(
                            gradeCalculation.courseCode,
                            gradeCalculation.courseName,
                            gradeCalculation.publishedGrade,
                            gradeCalculation.totalMarks
                    ));

                    if (gradeCalculation.gradePoint != null) {
                        sgpaWeightedPoints += gradeCalculation.gradePoint * gradeCalculation.credit;
                        sgpaCredits += gradeCalculation.credit;
                        if (!GradeScaleUtil.isEnglishCourse(gradeCalculation.courseCode)) {
                            gpaWeightedPoints += gradeCalculation.gradePoint * gradeCalculation.credit;
                            gpaCredits += gradeCalculation.credit;
                        }
                    }
                }
            }
        }

        double gpa = gpaCredits == 0 ? 0.0 : gpaWeightedPoints / gpaCredits;
        double sgpa = sgpaCredits == 0 ? 0.0 : sgpaWeightedPoints / sgpaCredits;
        return new GradeSummary(grades, gpa, sgpa);
    }

    public List<MaterialRecord> findMaterialsByStudent(String registrationNo, String keyword) throws SQLException {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT DISTINCT lm.material_id, lm.courseCode, lm.name, lm.path, lm.material_type
                FROM lecture_materials lm
                INNER JOIN enrollment e ON e.courseCode = lm.courseCode
                WHERE e.studentReg = ?
                  AND (? = '' OR lm.courseCode LIKE ? OR lm.name LIKE ?)
                ORDER BY lm.courseCode, lm.material_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + safeKeyword + "%";
            statement.setString(1, registrationNo);
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

    public List<MedicalRecord> findMedicalByStudent(String registrationNo) throws SQLException {
        String sql = """
                SELECT medical_id, StudentReg, courseCode, SubmissionDate, Description, session_type, attendance_id, tech_officer_reg, approval_status
                FROM medical
                WHERE StudentReg = ?
                ORDER BY SubmissionDate DESC, medical_id DESC
                """;
        Connection connection = DBConnection.getInstance().getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registrationNo);
            try (ResultSet rs = statement.executeQuery()) {
                List<MedicalRecord> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapMedicalRecord(rs));
                }
                return rows;
            }
        }
    }

    public List<TimetableRecord> findTimetableByStudent(String registrationNo) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        String department = findStudentDepartment(connection, registrationNo);
        if (department.isBlank()) {
            return new ArrayList<>();
        }
        List<TimetableRecord> rows = new ArrayList<>();
        if (!loadTimetableRows(connection, department, "timetable", rows)) {
            loadTimetableRows(connection, department, "timeTable", rows);
        }
        return rows;
    }

    private AttendanceRecord mapAttendanceRecord(ResultSet rs) throws SQLException {
        return new AttendanceRecord(
                String.valueOf(rs.getInt("attendance_id")),
                safe(rs.getString("StudentReg")),
                safe(rs.getString("courseCode")),
                dateToString(rs.getDate("SubmissionDate")),
                safe(rs.getString("session_type")),
                safe(rs.getString("attendance_status")),
                safe(rs.getString("tech_officer_reg"))
        );
    }

    private AttendanceEligibilityRecord mapAttendanceEligibilityRecord(ResultSet rs) throws SQLException {
        return new AttendanceEligibilityRecord(
                safe(rs.getString("courseCode")),
                rs.getInt("eligible_sessions"),
                rs.getInt("total_sessions")
        );
    }

    private CourseRecord mapCourseRecord(ResultSet rs) throws SQLException {
        return new CourseRecord(
                safe(rs.getString("courseCode")),
                safe(rs.getString("name")),
                safe(rs.getString("lecturerRegistrationNo")),
                safe(rs.getString("department")),
                safe(rs.getString("semester")),
                String.valueOf(rs.getInt("credit")),
                safe(rs.getString("course_type")),
                safe(rs.getString("status"))
        );
    }

    private GradeCalculation calculateGrade(Connection connection, ResultSet rs) throws SQLException {
        String courseCode = safe(rs.getString("courseCode"));
        boolean attendanceEligible = isAttendanceEligible(connection, rs.getString("StudentReg"), courseCode);
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
        boolean examPresent = rs.getInt("exam_present") == 1;
        boolean approvedExamMedical = rs.getInt("approved_exam_medical") == 1;
        int credit = rs.getInt("credit");
        GradeScaleUtil.GradeResult gradeResult =
                GradeScaleUtil.evaluatePublishedGrade(breakdown, attendanceEligible, examPresent, approvedExamMedical);

        GradeCalculation result = new GradeCalculation();
        result.courseCode = courseCode;
        result.courseName = safe(rs.getString("name"));
        result.totalMarks = breakdown.getTotalMarks();
        result.credit = credit;
        result.gradePoint = gradeResult.getGradePoint();
        result.publishedGrade = gradeResult.getPublishedGrade();
        return result;
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

    private MedicalRecord mapMedicalRecord(ResultSet rs) throws SQLException {
        return new MedicalRecord(
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
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, regNo);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? safe(rs.getString("department")) : "";
            }
        }
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

    private boolean loadTimetableRows(Connection connection, String department, String tableName, List<TimetableRecord> rows) throws SQLException {
        String sql = """
                SELECT t.time_table_id, t.department, t.lec_id, t.courseCode, t.admin_id, t.day, t.start_time, t.end_time, t.session_type
                FROM %s t
                WHERE t.department = ?
                ORDER BY t.day, t.start_time
                """.formatted(tableName);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, department);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapTimetableRecord(rs));
                }
                return true;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("doesn't exist")) {
                return false;
            }
            throw e;
        }
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

    private static class GradeCalculation {
        private String courseCode;
        private String courseName;
        private String publishedGrade;
        private double totalMarks;
        private Double gradePoint;
        private int credit;
    }

    public static class AttendanceRecord {
        private final String attendanceId;
        private final String studentReg;
        private final String courseCode;
        private final String submissionDate;
        private final String sessionType;
        private final String attendanceStatus;
        private final String techOfficerReg;

        public AttendanceRecord(String attendanceId, String studentReg, String courseCode, String submissionDate,
                                String sessionType, String attendanceStatus, String techOfficerReg) {
            this.attendanceId = attendanceId;
            this.studentReg = studentReg;
            this.courseCode = courseCode;
            this.submissionDate = submissionDate;
            this.sessionType = sessionType;
            this.attendanceStatus = attendanceStatus;
            this.techOfficerReg = techOfficerReg;
        }

        public String getAttendanceId() { return attendanceId; }
        public String getStudentReg() { return studentReg; }
        public String getCourseCode() { return courseCode; }
        public String getSubmissionDate() { return submissionDate; }
        public String getSessionType() { return sessionType; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public String getTechOfficerReg() { return techOfficerReg; }
    }

    public static class AttendanceEligibilityRecord {
        private final String courseCode;
        private final int eligibleSessions;
        private final int totalSessions;

        public AttendanceEligibilityRecord(String courseCode, int eligibleSessions, int totalSessions) {
            this.courseCode = courseCode;
            this.eligibleSessions = eligibleSessions;
            this.totalSessions = totalSessions;
        }

        public String getCourseCode() { return courseCode; }
        public int getEligibleSessions() { return eligibleSessions; }
        public int getTotalSessions() { return totalSessions; }
    }

    public static class CourseRecord {
        private final String courseCode;
        private final String name;
        private final String lecturer;
        private final String department;
        private final String semester;
        private final String credit;
        private final String type;
        private final String enrollmentStatus;

        public CourseRecord(String courseCode, String name, String lecturer, String department,
                            String semester, String credit, String type, String enrollmentStatus) {
            this.courseCode = courseCode;
            this.name = name;
            this.lecturer = lecturer;
            this.department = department;
            this.semester = semester;
            this.credit = credit;
            this.type = type;
            this.enrollmentStatus = enrollmentStatus;
        }

        public String getCourseCode() { return courseCode; }
        public String getName() { return name; }
        public String getLecturer() { return lecturer; }
        public String getDepartment() { return department; }
        public String getSemester() { return semester; }
        public String getCredit() { return credit; }
        public String getType() { return type; }
        public String getEnrollmentStatus() { return enrollmentStatus; }
    }

    public static class GradeRecord {
        private final String courseCode;
        private final String courseName;
        private final String grade;
        private final double totalMarks;

        public GradeRecord(String courseCode, String courseName, String grade, double totalMarks) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.grade = grade;
            this.totalMarks = totalMarks;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getGrade() { return grade; }
        public double getTotalMarks() { return totalMarks; }
    }

    public static class GradeSummary {
        private final List<GradeRecord> grades;
        private final double gpa;
        private final double sgpa;

        public GradeSummary(List<GradeRecord> grades, double gpa, double sgpa) {
            this.grades = grades;
            this.gpa = gpa;
            this.sgpa = sgpa;
        }

        public List<GradeRecord> getGrades() { return grades; }
        public double getGpa() { return gpa; }
        public double getSgpa() { return sgpa; }
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

    public static class MedicalRecord {
        private final String medicalId;
        private final String studentReg;
        private final String courseCode;
        private final String submissionDate;
        private final String description;
        private final String sessionType;
        private final String attendanceId;
        private final String approvalStatus;
        private final String techOfficerReg;

        public MedicalRecord(String medicalId, String studentReg, String courseCode, String submissionDate,
                             String description, String sessionType, String attendanceId, String approvalStatus,
                             String techOfficerReg) {
            this.medicalId = medicalId;
            this.studentReg = studentReg;
            this.courseCode = courseCode;
            this.submissionDate = submissionDate;
            this.description = description;
            this.sessionType = sessionType;
            this.attendanceId = attendanceId;
            this.approvalStatus = approvalStatus;
            this.techOfficerReg = techOfficerReg;
        }

        public String getMedicalId() { return medicalId; }
        public String getStudentReg() { return studentReg; }
        public String getCourseCode() { return courseCode; }
        public String getSubmissionDate() { return submissionDate; }
        public String getDescription() { return description; }
        public String getSessionType() { return sessionType; }
        public String getAttendanceId() { return attendanceId; }
        public String getApprovalStatus() { return approvalStatus; }
        public String getTechOfficerReg() { return techOfficerReg; }
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
