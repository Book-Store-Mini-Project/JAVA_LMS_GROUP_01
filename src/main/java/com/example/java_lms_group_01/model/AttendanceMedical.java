package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class AttendanceMedical {
    private final SimpleStringProperty attendanceId;
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty date;
    private final SimpleStringProperty sessionType;
    private final SimpleStringProperty attendanceStatus;
    private final SimpleStringProperty medicalId;
    private final SimpleStringProperty medicalDescription;
    private final SimpleStringProperty medicalApprovalStatus;
    private final SimpleStringProperty techOfficerReg;

    public AttendanceMedical(String attendanceId, String studentReg, String courseCode, String date,
                             String sessionType, String attendanceStatus, String medicalId,
                             String medicalDescription, String medicalApprovalStatus,
                             String techOfficerReg) {
        this.attendanceId = new SimpleStringProperty(attendanceId);
        this.studentReg = new SimpleStringProperty(studentReg);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.date = new SimpleStringProperty(date);
        this.sessionType = new SimpleStringProperty(sessionType);
        this.attendanceStatus = new SimpleStringProperty(attendanceStatus);
        this.medicalId = new SimpleStringProperty(medicalId);
        this.medicalDescription = new SimpleStringProperty(medicalDescription);
        this.medicalApprovalStatus = new SimpleStringProperty(medicalApprovalStatus);
        this.techOfficerReg = new SimpleStringProperty(techOfficerReg);
    }

    public SimpleStringProperty attendanceIdProperty() { return attendanceId; }
    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty dateProperty() { return date; }
    public SimpleStringProperty sessionTypeProperty() { return sessionType; }
    public SimpleStringProperty attendanceStatusProperty() { return attendanceStatus; }
    public SimpleStringProperty medicalIdProperty() { return medicalId; }
    public SimpleStringProperty medicalDescriptionProperty() { return medicalDescription; }
    public SimpleStringProperty medicalApprovalStatusProperty() { return medicalApprovalStatus; }
    public SimpleStringProperty techOfficerRegProperty() { return techOfficerReg; }

    public String getAttendanceId() { return attendanceId.get(); }
    public String getMedicalId() { return medicalId.get(); }
}
