package com.example.java_lms_group_01.model.request;

import java.time.LocalDate;

/**
 * Simple medical request.
 */
public class MedicalRequest {
    private String studentRegNo;
    private String courseCode;
    private int attendanceId;
    private LocalDate submissionDate;
    private String sessionType;
    private String description;
    private String techOfficerReg;

    public MedicalRequest() {
    }

    public MedicalRequest(String studentRegNo, String courseCode, int attendanceId,
                          LocalDate submissionDate, String sessionType,
                          String description, String techOfficerReg) {
        this.studentRegNo = studentRegNo;
        this.courseCode = courseCode;
        this.attendanceId = attendanceId;
        this.submissionDate = submissionDate;
        this.sessionType = sessionType;
        this.description = description;
        this.techOfficerReg = techOfficerReg;
    }

    public String getStudentRegNo() {
        return studentRegNo;
    }

    public void setStudentRegNo(String studentRegNo) {
        this.studentRegNo = studentRegNo;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTechOfficerReg() {
        return techOfficerReg;
    }

    public void setTechOfficerReg(String techOfficerReg) {
        this.techOfficerReg = techOfficerReg;
    }
}
