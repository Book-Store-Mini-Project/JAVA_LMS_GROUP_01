package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Grade {
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty courseName;
    private final SimpleStringProperty total;
    private final SimpleStringProperty grade;

    public Grade(String courseCode, String courseName, String total, String grade) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.courseName = new SimpleStringProperty(courseName);
        this.total = new SimpleStringProperty(total);
        this.grade = new SimpleStringProperty(grade);
    }

    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty courseNameProperty() { return courseName; }
    public SimpleStringProperty totalProperty() { return total; }
    public SimpleStringProperty gradeProperty() { return grade; }
}
