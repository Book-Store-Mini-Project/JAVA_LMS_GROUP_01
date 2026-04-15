package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple mark model used in tables.
 */
public class Mark {
    private String markId;
    private String studentReg;
    private String courseCode;
    private String quiz1;
    private String quiz2;
    private String quiz3;
    private String assessment;
    private String project;
    private String midTerm;
    private String finalTheory;
    private String finalPractical;

    public Mark() {
    }

    public Mark(String markId, String studentReg, String courseCode, String quiz1, String quiz2,
                String quiz3, String assessment, String project, String midTerm,
                String finalTheory, String finalPractical) {
        setMarkId(markId);
        setStudentReg(studentReg);
        setCourseCode(courseCode);
        setQuiz1(quiz1);
        setQuiz2(quiz2);
        setQuiz3(quiz3);
        setAssessment(assessment);
        setProject(project);
        setMidTerm(midTerm);
        setFinalTheory(finalTheory);
        setFinalPractical(finalPractical);
    }

    public String getMarkId() {
        return markId;
    }

    public void setMarkId(String markId) {
        this.markId = text(markId);
    }

    public String getStudentReg() {
        return studentReg;
    }

    public void setStudentReg(String studentReg) {
        this.studentReg = text(studentReg);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = text(courseCode);
    }

    public String getQuiz1() {
        return quiz1;
    }

    public void setQuiz1(String quiz1) {
        this.quiz1 = text(quiz1);
    }

    public String getQuiz2() {
        return quiz2;
    }

    public void setQuiz2(String quiz2) {
        this.quiz2 = text(quiz2);
    }

    public String getQuiz3() {
        return quiz3;
    }

    public void setQuiz3(String quiz3) {
        this.quiz3 = text(quiz3);
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = text(assessment);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = text(project);
    }

    public String getMidTerm() {
        return midTerm;
    }

    public void setMidTerm(String midTerm) {
        this.midTerm = text(midTerm);
    }

    public String getFinalTheory() {
        return finalTheory;
    }

    public void setFinalTheory(String finalTheory) {
        this.finalTheory = text(finalTheory);
    }

    public String getFinalPractical() {
        return finalPractical;
    }

    public void setFinalPractical(String finalPractical) {
        this.finalPractical = text(finalPractical);
    }

    public SimpleStringProperty markIdProperty() {
        return property(markId);
    }

    public SimpleStringProperty studentRegProperty() {
        return property(studentReg);
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty quiz1Property() {
        return property(quiz1);
    }

    public SimpleStringProperty quiz2Property() {
        return property(quiz2);
    }

    public SimpleStringProperty quiz3Property() {
        return property(quiz3);
    }

    public SimpleStringProperty assessmentProperty() {
        return property(assessment);
    }

    public SimpleStringProperty projectProperty() {
        return property(project);
    }

    public SimpleStringProperty midTermProperty() {
        return property(midTerm);
    }

    public SimpleStringProperty finalTheoryProperty() {
        return property(finalTheory);
    }

    public SimpleStringProperty finalPracticalProperty() {
        return property(finalPractical);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
