package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple eligibility model used in tables.
 */
public class Eligibility {
    private String studentReg;
    private String studentName;
    private String courseCode;
    private String eligibleSessions;
    private String totalSessions;
    private String attendancePct;
    private String caMarks;
    private String caThreshold;
    private String eligibility;

    public Eligibility() {
    }

    public Eligibility(String studentReg, String studentName, String courseCode, String eligibleSessions,
                       String totalSessions, String attendancePct, String caMarks,
                       String caThreshold, String eligibility) {
        setStudentReg(studentReg);
        setStudentName(studentName);
        setCourseCode(courseCode);
        setEligibleSessions(eligibleSessions);
        setTotalSessions(totalSessions);
        setAttendancePct(attendancePct);
        setCaMarks(caMarks);
        setCaThreshold(caThreshold);
        setEligibility(eligibility);
    }

    public String getStudentReg() {
        return studentReg;
    }

    public void setStudentReg(String studentReg) {
        this.studentReg = text(studentReg);
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = text(studentName);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = text(courseCode);
    }

    public String getEligibleSessions() {
        return eligibleSessions;
    }

    public void setEligibleSessions(String eligibleSessions) {
        this.eligibleSessions = text(eligibleSessions);
    }

    public String getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(String totalSessions) {
        this.totalSessions = text(totalSessions);
    }

    public String getAttendancePct() {
        return attendancePct;
    }

    public void setAttendancePct(String attendancePct) {
        this.attendancePct = text(attendancePct);
    }

    public String getCaMarks() {
        return caMarks;
    }

    public void setCaMarks(String caMarks) {
        this.caMarks = text(caMarks);
    }

    public String getCaThreshold() {
        return caThreshold;
    }

    public void setCaThreshold(String caThreshold) {
        this.caThreshold = text(caThreshold);
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = text(eligibility);
    }

    public SimpleStringProperty studentRegProperty() {
        return property(studentReg);
    }

    public SimpleStringProperty studentNameProperty() {
        return property(studentName);
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty eligibleSessionsProperty() {
        return property(eligibleSessions);
    }

    public SimpleStringProperty totalSessionsProperty() {
        return property(totalSessions);
    }

    public SimpleStringProperty attendancePctProperty() {
        return property(attendancePct);
    }

    public SimpleStringProperty caMarksProperty() {
        return property(caMarks);
    }

    public SimpleStringProperty caThresholdProperty() {
        return property(caThreshold);
    }

    public SimpleStringProperty eligibilityProperty() {
        return property(eligibility);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
