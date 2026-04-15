package com.example.java_lms_group_01.model.summary;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple undergraduate summary used in tables.
 */
public class UndergraduateSummary {
    private String studentReg;
    private String studentName;
    private String sgpa;
    private String cgpa;

    public UndergraduateSummary() {
    }

    public UndergraduateSummary(String studentReg, String studentName, String sgpa, String cgpa) {
        setStudentReg(studentReg);
        setStudentName(studentName);
        setSgpa(sgpa);
        setCgpa(cgpa);
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

    public String getSgpa() {
        return sgpa;
    }

    public void setSgpa(String sgpa) {
        this.sgpa = text(sgpa);
    }

    public String getCgpa() {
        return cgpa;
    }

    public void setCgpa(String cgpa) {
        this.cgpa = text(cgpa);
    }

    public SimpleStringProperty studentRegProperty() {
        return property(studentReg);
    }

    public SimpleStringProperty studentNameProperty() {
        return property(studentName);
    }

    public SimpleStringProperty sgpaProperty() {
        return property(sgpa);
    }

    public SimpleStringProperty cgpaProperty() {
        return property(cgpa);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
