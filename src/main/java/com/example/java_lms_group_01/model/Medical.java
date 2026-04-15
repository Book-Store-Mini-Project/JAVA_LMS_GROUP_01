package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple medical model used in tables.
 */
public class Medical {
    private String medicalId;
    private String studentReg;
    private String courseCode;
    private String submissionDate;
    private String description;
    private String sessionType;
    private String attendanceId;
    private String approvalStatus;
    private String techOfficerReg;

    public Medical() {
    }

    public Medical(String medicalId, String studentReg, String courseCode, String submissionDate,
                   String description, String sessionType, String attendanceId,
                   String approvalStatus, String techOfficerReg) {
        setMedicalId(medicalId);
        setStudentReg(studentReg);
        setCourseCode(courseCode);
        setSubmissionDate(submissionDate);
        setDescription(description);
        setSessionType(sessionType);
        setAttendanceId(attendanceId);
        setApprovalStatus(approvalStatus);
        setTechOfficerReg(techOfficerReg);
    }

    public String getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(String medicalId) {
        this.medicalId = text(medicalId);
    }

    public String getStudentReg() {
        return studentReg;
    }

    public void setStudentReg(String studentReg) {
        this.studentReg = text(studentReg);
    }

    public String getStudentRegNo() {
        return studentReg;
    }

    public void setStudentRegNo(String studentRegNo) {
        this.studentReg = text(studentRegNo);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = text(courseCode);
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = text(submissionDate);
    }

    public String getDate() {
        return submissionDate;
    }

    public void setDate(String date) {
        this.submissionDate = text(date);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = text(description);
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = text(sessionType);
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = text(attendanceId);
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = text(approvalStatus);
    }

    public String getTechOfficerReg() {
        return techOfficerReg;
    }

    public void setTechOfficerReg(String techOfficerReg) {
        this.techOfficerReg = text(techOfficerReg);
    }

    public SimpleStringProperty medicalIdProperty() {
        return property(medicalId);
    }

    public SimpleStringProperty studentRegProperty() {
        return property(studentReg);
    }

    public SimpleStringProperty studentRegNoProperty() {
        return property(studentReg);
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty submissionDateProperty() {
        return property(submissionDate);
    }

    public SimpleStringProperty dateProperty() {
        return property(submissionDate);
    }

    public SimpleStringProperty descriptionProperty() {
        return property(description);
    }

    public SimpleStringProperty sessionTypeProperty() {
        return property(sessionType);
    }

    public SimpleStringProperty attendanceIdProperty() {
        return property(attendanceId);
    }

    public SimpleStringProperty approvalStatusProperty() {
        return property(approvalStatus);
    }

    public SimpleStringProperty techOfficerRegProperty() {
        return property(techOfficerReg);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
