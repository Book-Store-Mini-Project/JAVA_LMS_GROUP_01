package com.example.java_lms_group_01.model.request;

/**
 * Simple marks request.
 */
public class MarkRequest {
    private String studentReg;
    private String courseCode;
    private Double quiz1;
    private Double quiz2;
    private Double quiz3;
    private Double assessment;
    private Double project;
    private Double midTerm;
    private Double finalTheory;
    private Double finalPractical;

    public MarkRequest() {
    }

    public MarkRequest(String studentReg, String courseCode, Double quiz1, Double quiz2, Double quiz3,
                       Double assessment, Double project, Double midTerm, Double finalTheory,
                       Double finalPractical) {
        this.studentReg = studentReg;
        this.courseCode = courseCode;
        this.quiz1 = quiz1;
        this.quiz2 = quiz2;
        this.quiz3 = quiz3;
        this.assessment = assessment;
        this.project = project;
        this.midTerm = midTerm;
        this.finalTheory = finalTheory;
        this.finalPractical = finalPractical;
    }

    public String getStudentReg() {
        return studentReg;
    }

    public void setStudentReg(String studentReg) {
        this.studentReg = studentReg;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Double getQuiz1() {
        return quiz1;
    }

    public void setQuiz1(Double quiz1) {
        this.quiz1 = quiz1;
    }

    public Double getQuiz2() {
        return quiz2;
    }

    public void setQuiz2(Double quiz2) {
        this.quiz2 = quiz2;
    }

    public Double getQuiz3() {
        return quiz3;
    }

    public void setQuiz3(Double quiz3) {
        this.quiz3 = quiz3;
    }

    public Double getAssessment() {
        return assessment;
    }

    public void setAssessment(Double assessment) {
        this.assessment = assessment;
    }

    public Double getProject() {
        return project;
    }

    public void setProject(Double project) {
        this.project = project;
    }

    public Double getMidTerm() {
        return midTerm;
    }

    public void setMidTerm(Double midTerm) {
        this.midTerm = midTerm;
    }

    public Double getFinalTheory() {
        return finalTheory;
    }

    public void setFinalTheory(Double finalTheory) {
        this.finalTheory = finalTheory;
    }

    public Double getFinalPractical() {
        return finalPractical;
    }

    public void setFinalPractical(Double finalPractical) {
        this.finalPractical = finalPractical;
    }
}
