package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple attendance model used in tables.
 */
public class Attendance {
    private String attendanceId;
    private String studentReg;
    private String courseCode;
    private String submissionDate;
    private String sessionType;
    private String attendanceStatus;
    private String techOfficerReg;
    private String medicalId;
    private String medicalDescription;
    private String medicalApprovalStatus;

    public Attendance() {
    }

    public Attendance(String attendanceId, String studentReg, String courseCode, String submissionDate,
                      String sessionType, String attendanceStatus, String techOfficerReg) {
        this(attendanceId, studentReg, courseCode, submissionDate, sessionType, attendanceStatus,
                techOfficerReg, "", "", "");
    }

    public Attendance(String attendanceId, String studentReg, String courseCode, String submissionDate,
                      String sessionType, String attendanceStatus, String techOfficerReg,
                      String medicalId, String medicalDescription, String medicalApprovalStatus) {
        setAttendanceId(attendanceId);
        setStudentReg(studentReg);
        setCourseCode(courseCode);
        setSubmissionDate(submissionDate);
        setSessionType(sessionType);
        setAttendanceStatus(attendanceStatus);
        setTechOfficerReg(techOfficerReg);
        setMedicalId(medicalId);
        setMedicalDescription(medicalDescription);
        setMedicalApprovalStatus(medicalApprovalStatus);
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = text(attendanceId);
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

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = text(sessionType);
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = text(attendanceStatus);
    }

    public String getStatus() {
        return attendanceStatus;
    }

    public void setStatus(String status) {
        this.attendanceStatus = text(status);
    }

    public String getTechOfficerReg() {
        return techOfficerReg;
    }

    public void setTechOfficerReg(String techOfficerReg) {
        this.techOfficerReg = text(techOfficerReg);
    }

    public String getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(String medicalId) {
        this.medicalId = text(medicalId);
    }

    public String getMedicalDescription() {
        return medicalDescription;
    }

    public void setMedicalDescription(String medicalDescription) {
        this.medicalDescription = text(medicalDescription);
    }

    public String getMedicalApprovalStatus() {
        return medicalApprovalStatus;
    }

    public void setMedicalApprovalStatus(String medicalApprovalStatus) {
        this.medicalApprovalStatus = text(medicalApprovalStatus);
    }

    public SimpleStringProperty attendanceIdProperty() {
        return property(attendanceId);
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

    public SimpleStringProperty sessionTypeProperty() {
        return property(sessionType);
    }

    public SimpleStringProperty attendanceStatusProperty() {
        return property(attendanceStatus);
    }

    public SimpleStringProperty statusProperty() {
        return property(attendanceStatus);
    }

    public SimpleStringProperty techOfficerRegProperty() {
        return property(techOfficerReg);
    }

    public SimpleStringProperty medicalIdProperty() {
        return property(medicalId);
    }

    public SimpleStringProperty medicalDescriptionProperty() {
        return property(medicalDescription);
    }

    public SimpleStringProperty medicalApprovalStatusProperty() {
        return property(medicalApprovalStatus);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
