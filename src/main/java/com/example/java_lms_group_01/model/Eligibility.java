package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Eligibility {
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty studentName;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty attendancePct;
    private final SimpleStringProperty eligibility;

    public Eligibility(String studentReg, String studentName, String courseCode, String attendancePct,
                       String eligibility) {
        this.studentReg = new SimpleStringProperty(studentReg);
        this.studentName = new SimpleStringProperty(studentName);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.attendancePct = new SimpleStringProperty(attendancePct);
        this.eligibility = new SimpleStringProperty(eligibility);
    }

    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty studentNameProperty() { return studentName; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty attendancePctProperty() { return attendancePct; }
    public SimpleStringProperty eligibilityProperty() { return eligibility; }
}
