package com.example.java_lms_group_01.model.summary;

// Holds calculated CA marks, exam marks, and totals.
public class MarkBreakdown {
    private final double caMarks;
    private final double endMarks;
    private final double totalMarks;
    private final double caMaximum;
    private final double endMaximum;

    public MarkBreakdown(double caMarks, double endMarks, double totalMarks, double caMaximum, double endMaximum) {
        this.caMarks = caMarks;
        this.endMarks = endMarks;
        this.totalMarks = totalMarks;
        this.caMaximum = caMaximum;
        this.endMaximum = endMaximum;
    }

    public double getCaMarks() {
        return caMarks;
    }

    public double getEndMarks() {
        return endMarks;
    }

    public double getTotalMarks() {
        return totalMarks;
    }

    public double getCaMaximum() {
        return caMaximum;
    }

    public double getEndMaximum() {
        return endMaximum;
    }
}
