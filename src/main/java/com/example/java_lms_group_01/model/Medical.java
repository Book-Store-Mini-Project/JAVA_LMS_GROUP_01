package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Medical {
    private final SimpleStringProperty medicalId;
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty submissionDate;
    private final SimpleStringProperty description;
    private final SimpleStringProperty sessionType;
    private final SimpleStringProperty attendanceId;
    private final SimpleStringProperty approvalStatus;
    private final SimpleStringProperty techOfficerReg;

    public Medical(String medicalId, String studentReg, String courseCode, String submissionDate,
                   String description, String sessionType, String attendanceId,
                   String approvalStatus, String techOfficerReg) {
        this.medicalId = new SimpleStringProperty(medicalId);
        this.studentReg = new SimpleStringProperty(studentReg);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.submissionDate = new SimpleStringProperty(submissionDate);
        this.description = new SimpleStringProperty(description);
        this.sessionType = new SimpleStringProperty(sessionType);
        this.attendanceId = new SimpleStringProperty(attendanceId);
        this.approvalStatus = new SimpleStringProperty(approvalStatus);
        this.techOfficerReg = new SimpleStringProperty(techOfficerReg);
    }

    public SimpleStringProperty medicalIdProperty() { return medicalId; }
    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty studentRegNoProperty() { return studentReg; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty submissionDateProperty() { return submissionDate; }
    public SimpleStringProperty dateProperty() { return submissionDate; }
    public SimpleStringProperty descriptionProperty() { return description; }
    public SimpleStringProperty sessionTypeProperty() { return sessionType; }
    public SimpleStringProperty attendanceIdProperty() { return attendanceId; }
    public SimpleStringProperty approvalStatusProperty() { return approvalStatus; }
    public SimpleStringProperty techOfficerRegProperty() { return techOfficerReg; }

    public String getMedicalId() { return medicalId.get(); }
    public String getStudentReg() { return studentReg.get(); }
    public String getStudentRegNo() { return studentReg.get(); }
    public String getCourseCode() { return courseCode.get(); }
    public String getSubmissionDate() { return submissionDate.get(); }
    public String getDate() { return submissionDate.get(); }
    public String getDescription() { return description.get(); }
    public String getSessionType() { return sessionType.get(); }
    public String getAttendanceId() { return attendanceId.get(); }

    public void setStudentRegNo(String value) { studentReg.set(value); }
    public void setCourseCode(String value) { courseCode.set(value); }
    public void setDate(String value) { submissionDate.set(value); }
    public void setDescription(String value) { description.set(value); }
    public void setSessionType(String value) { sessionType.set(value); }
    public void setAttendanceId(String value) { attendanceId.set(value); }
}
