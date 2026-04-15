package com.example.java_lms_group_01.model.request;

import java.time.LocalDate;

/**
 * Simple attendance request.
 */
public class AttendanceRequest {
    private String studentRegNo;
    private String courseCode;
    private String techOfficerReg;
    private LocalDate submissionDate;
    private String sessionType;
    private String status;

    public AttendanceRequest() {
    }

    public AttendanceRequest(String studentRegNo, String courseCode, String techOfficerReg,
                             LocalDate submissionDate, String sessionType, String status) {
        this.studentRegNo = studentRegNo;
        this.courseCode = courseCode;
        this.techOfficerReg = techOfficerReg;
        this.submissionDate = submissionDate;
        this.sessionType = sessionType;
        this.status = status;
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

    public String getTechOfficerReg() {
        return techOfficerReg;
    }

    public void setTechOfficerReg(String techOfficerReg) {
        this.techOfficerReg = techOfficerReg;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
