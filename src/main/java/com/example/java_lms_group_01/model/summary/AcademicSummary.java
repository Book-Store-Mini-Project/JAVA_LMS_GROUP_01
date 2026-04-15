package com.example.java_lms_group_01.model.summary;

/**
 * Simple GPA summary.
 */
public class AcademicSummary {
    private double cgpa;
    private double sgpa;

    public AcademicSummary() {
    }

    public AcademicSummary(double cgpa, double sgpa) {
        this.cgpa = cgpa;
        this.sgpa = sgpa;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }
}
