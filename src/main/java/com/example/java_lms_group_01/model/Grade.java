package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * JavaFX table row that stores one course grade for the student screen.
 */
public class Grade {
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty courseName;
    private final SimpleStringProperty finalMarks;
    private final SimpleStringProperty total;
    private final SimpleStringProperty grade;

    public Grade(String courseCode, String courseName, String finalMarks, String total, String grade) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.courseName = new SimpleStringProperty(courseName);
        this.finalMarks = new SimpleStringProperty(finalMarks);
        this.total = new SimpleStringProperty(total);
        this.grade = new SimpleStringProperty(grade);
    }

    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty courseNameProperty() { return courseName; }
    public SimpleStringProperty finalMarksProperty() { return finalMarks; }
    public SimpleStringProperty totalProperty() { return total; }
    public SimpleStringProperty gradeProperty() { return grade; }

    public String getCourseCode() { return courseCode.get(); }
    public String getCourseName() { return courseName.get(); }
    public String getFinalMarks() { return finalMarks.get(); }
    public String getTotal() { return total.get(); }
    public String getGrade() { return grade.get(); }
}
