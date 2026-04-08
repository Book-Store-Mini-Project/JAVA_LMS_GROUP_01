package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Attendance {
    private final SimpleStringProperty attendanceId;
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty submissionDate;
    private final SimpleStringProperty sessionType;
    private final SimpleStringProperty attendanceStatus;
    private final SimpleStringProperty techOfficerReg;

    public Attendance(String attendanceId, String studentReg, String courseCode, String submissionDate,
                      String sessionType, String attendanceStatus, String techOfficerReg) {
        this.attendanceId = new SimpleStringProperty(attendanceId);
        this.studentReg = new SimpleStringProperty(studentReg);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.submissionDate = new SimpleStringProperty(submissionDate);
        this.sessionType = new SimpleStringProperty(sessionType);
        this.attendanceStatus = new SimpleStringProperty(attendanceStatus);
        this.techOfficerReg = new SimpleStringProperty(techOfficerReg);
    }

    public SimpleStringProperty attendanceIdProperty() { return attendanceId; }
    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty studentRegNoProperty() { return studentReg; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty submissionDateProperty() { return submissionDate; }
    public SimpleStringProperty dateProperty() { return submissionDate; }
    public SimpleStringProperty sessionTypeProperty() { return sessionType; }
    public SimpleStringProperty attendanceStatusProperty() { return attendanceStatus; }
    public SimpleStringProperty statusProperty() { return attendanceStatus; }
    public SimpleStringProperty techOfficerRegProperty() { return techOfficerReg; }

    public String getAttendanceId() { return attendanceId.get(); }
    public String getStudentReg() { return studentReg.get(); }
    public String getStudentRegNo() { return studentReg.get(); }
    public String getCourseCode() { return courseCode.get(); }
    public String getSubmissionDate() { return submissionDate.get(); }
    public String getDate() { return submissionDate.get(); }
    public String getSessionType() { return sessionType.get(); }
    public String getAttendanceStatus() { return attendanceStatus.get(); }
    public String getStatus() { return attendanceStatus.get(); }

    public void setStudentRegNo(String value) { studentReg.set(value); }
    public void setCourseCode(String value) { courseCode.set(value); }
    public void setDate(String value) { submissionDate.set(value); }
    public void setSessionType(String value) { sessionType.set(value); }
    public void setStatus(String value) { attendanceStatus.set(value); }
}
