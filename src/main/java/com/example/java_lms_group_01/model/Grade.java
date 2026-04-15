package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple grade model used in tables.
 */
public class Grade {
    private String courseCode;
    private String courseName;
    private String finalMarks;
    private String total;
    private String grade;

    public Grade() {
    }

    public Grade(String courseCode, String courseName, String finalMarks, String total, String grade) {
        setCourseCode(courseCode);
        setCourseName(courseName);
        setFinalMarks(finalMarks);
        setTotal(total);
        setGrade(grade);
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

    public String getFinalMarks() {
        return finalMarks;
    }

    public void setFinalMarks(String finalMarks) {
        this.finalMarks = text(finalMarks);
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = text(total);
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = text(grade);
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty courseNameProperty() {
        return property(courseName);
    }

    public SimpleStringProperty finalMarksProperty() {
        return property(finalMarks);
    }

    public SimpleStringProperty totalProperty() {
        return property(total);
    }

    public SimpleStringProperty gradeProperty() {
        return property(grade);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
