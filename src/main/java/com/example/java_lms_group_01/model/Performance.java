package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple performance model used in tables.
 */
public class Performance {
    private String studentReg;
    private String studentName;
    private String courseCode;
    private String courseName;
    private String caMarks;
    private String finalMarks;
    private String totalMarks;
    private String grade;
    private String sgpa;
    private String cgpa;

    public Performance() {
    }

    public Performance(String studentReg, String studentName, String courseCode, String courseName,
                       String caMarks, String finalMarks, String totalMarks, String grade,
                       String sgpa, String cgpa) {
        setStudentReg(studentReg);
        setStudentName(studentName);
        setCourseCode(courseCode);
        setCourseName(courseName);
        setCaMarks(caMarks);
        setFinalMarks(finalMarks);
        setTotalMarks(totalMarks);
        setGrade(grade);
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = text(courseCode);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = text(courseName);
    }

    public String getCaMarks() {
        return caMarks;
    }

    public void setCaMarks(String caMarks) {
        this.caMarks = text(caMarks);
    }

    public String getFinalMarks() {
        return finalMarks;
    }

    public void setFinalMarks(String finalMarks) {
        this.finalMarks = text(finalMarks);
    }

    public String getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = text(totalMarks);
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = text(grade);
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

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty courseNameProperty() {
        return property(courseName);
    }

    public SimpleStringProperty caMarksProperty() {
        return property(caMarks);
    }

    public SimpleStringProperty finalMarksProperty() {
        return property(finalMarks);
    }

    public SimpleStringProperty totalMarksProperty() {
        return property(totalMarks);
    }

    public SimpleStringProperty gradeProperty() {
        return property(grade);
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
