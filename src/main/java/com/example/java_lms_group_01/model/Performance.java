package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Performance {
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty studentName;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty caMarks;
    private final SimpleStringProperty endMarks;
    private final SimpleStringProperty totalMarks;
    private final SimpleStringProperty grade;
    private final SimpleStringProperty gpa;
    private final SimpleStringProperty sgpa;

    public Performance(String studentReg, String studentName, String courseCode, String caMarks,
                       String endMarks, String totalMarks, String grade, String gpa, String sgpa) {
        this.studentReg = new SimpleStringProperty(studentReg);
        this.studentName = new SimpleStringProperty(studentName);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.caMarks = new SimpleStringProperty(caMarks);
        this.endMarks = new SimpleStringProperty(endMarks);
        this.totalMarks = new SimpleStringProperty(totalMarks);
        this.grade = new SimpleStringProperty(grade);
        this.gpa = new SimpleStringProperty(gpa);
        this.sgpa = new SimpleStringProperty(sgpa);
    }

    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty studentNameProperty() { return studentName; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty caMarksProperty() { return caMarks; }
    public SimpleStringProperty endMarksProperty() { return endMarks; }
    public SimpleStringProperty totalMarksProperty() { return totalMarks; }
    public SimpleStringProperty gradeProperty() { return grade; }
    public SimpleStringProperty gpaProperty() { return gpa; }
    public SimpleStringProperty sgpaProperty() { return sgpa; }
}
