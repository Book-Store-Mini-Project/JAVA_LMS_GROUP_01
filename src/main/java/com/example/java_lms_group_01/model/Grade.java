package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Grade {
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty quiz1;
    private final SimpleStringProperty quiz2;
    private final SimpleStringProperty quiz3;
    private final SimpleStringProperty assessment1;
    private final SimpleStringProperty assessment2;
    private final SimpleStringProperty midTerm;
    private final SimpleStringProperty finalTheory;
    private final SimpleStringProperty finalPractical;
    private final SimpleStringProperty total;
    private final SimpleStringProperty grade;

    public Grade(String courseCode, String quiz1, String quiz2, String quiz3, String assessment1,
                 String assessment2, String midTerm, String finalTheory, String finalPractical,
                 String total, String grade) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.quiz1 = new SimpleStringProperty(quiz1);
        this.quiz2 = new SimpleStringProperty(quiz2);
        this.quiz3 = new SimpleStringProperty(quiz3);
        this.assessment1 = new SimpleStringProperty(assessment1);
        this.assessment2 = new SimpleStringProperty(assessment2);
        this.midTerm = new SimpleStringProperty(midTerm);
        this.finalTheory = new SimpleStringProperty(finalTheory);
        this.finalPractical = new SimpleStringProperty(finalPractical);
        this.total = new SimpleStringProperty(total);
        this.grade = new SimpleStringProperty(grade);
    }

    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty quiz1Property() { return quiz1; }
    public SimpleStringProperty quiz2Property() { return quiz2; }
    public SimpleStringProperty quiz3Property() { return quiz3; }
    public SimpleStringProperty assessment1Property() { return assessment1; }
    public SimpleStringProperty assessment2Property() { return assessment2; }
    public SimpleStringProperty midTermProperty() { return midTerm; }
    public SimpleStringProperty finalTheoryProperty() { return finalTheory; }
    public SimpleStringProperty finalPracticalProperty() { return finalPractical; }
    public SimpleStringProperty totalProperty() { return total; }
    public SimpleStringProperty gradeProperty() { return grade; }
}
