package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Mark {
    private final SimpleStringProperty markId;
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty quiz1;
    private final SimpleStringProperty quiz2;
    private final SimpleStringProperty quiz3;
    private final SimpleStringProperty assessment;
    private final SimpleStringProperty project;
    private final SimpleStringProperty midTerm;
    private final SimpleStringProperty finalTheory;
    private final SimpleStringProperty finalPractical;

    public Mark(String markId, String studentReg, String courseCode, String quiz1, String quiz2,
                String quiz3, String assessment, String project, String midTerm,
                String finalTheory, String finalPractical) {
        this.markId = new SimpleStringProperty(markId);
        this.studentReg = new SimpleStringProperty(studentReg);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.quiz1 = new SimpleStringProperty(quiz1);
        this.quiz2 = new SimpleStringProperty(quiz2);
        this.quiz3 = new SimpleStringProperty(quiz3);
        this.assessment = new SimpleStringProperty(assessment);
        this.project = new SimpleStringProperty(project);
        this.midTerm = new SimpleStringProperty(midTerm);
        this.finalTheory = new SimpleStringProperty(finalTheory);
        this.finalPractical = new SimpleStringProperty(finalPractical);
    }

    public SimpleStringProperty markIdProperty() { return markId; }
    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty quiz1Property() { return quiz1; }
    public SimpleStringProperty quiz2Property() { return quiz2; }
    public SimpleStringProperty quiz3Property() { return quiz3; }
    public SimpleStringProperty assessmentProperty() { return assessment; }
    public SimpleStringProperty projectProperty() { return project; }
    public SimpleStringProperty midTermProperty() { return midTerm; }
    public SimpleStringProperty finalTheoryProperty() { return finalTheory; }
    public SimpleStringProperty finalPracticalProperty() { return finalPractical; }

    public String getMarkId() { return markId.get(); }
    public String getStudentReg() { return studentReg.get(); }
    public String getCourseCode() { return courseCode.get(); }
    public String getQuiz1() { return quiz1.get(); }
    public String getQuiz2() { return quiz2.get(); }
    public String getQuiz3() { return quiz3.get(); }
    public String getAssessment() { return assessment.get(); }
    public String getProject() { return project.get(); }
    public String getMidTerm() { return midTerm.get(); }
    public String getFinalTheory() { return finalTheory.get(); }
    public String getFinalPractical() { return finalPractical.get(); }
}
