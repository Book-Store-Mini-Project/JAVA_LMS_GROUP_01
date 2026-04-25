package com.example.java_lms_group_01.model.summary;

// Holds the final published grade and grade point.
public class GradeResult {
    private final String publishedGrade;
    private final Double gradePoint;

    public GradeResult(String publishedGrade, Double gradePoint) {
        this.publishedGrade = publishedGrade;
        this.gradePoint = gradePoint;
    }

    public String getPublishedGrade() {
        return publishedGrade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }
}
