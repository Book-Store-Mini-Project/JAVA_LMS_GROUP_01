package com.example.java_lms_group_01.util;

public final class GradeScaleUtil {

    private GradeScaleUtil() {
    }

    public static String toLetterGrade(double marks) {
        if (marks >= 80) return "A+";
        if (marks >= 75) return "A";
        if (marks >= 70) return "A-";
        if (marks >= 65) return "B+";
        if (marks >= 60) return "B";
        if (marks >= 55) return "B-";
        if (marks >= 50) return "C+";
        if (marks >= 45) return "C";
        if (marks >= 40) return "C-";
        if (marks >= 35) return "D";
        return "E";
    }

    public static String toPublishedGrade(double marks, boolean examPresent, boolean approvedExamMedical) {
        if (approvedExamMedical) {
            return "MC";
        }
        if (!examPresent) {
            return "E";
        }
        return toLetterGrade(marks);
    }

    public static double toGradePoint(double marks) {
        if (marks >= 80) return 4.0;
        if (marks >= 75) return 4.0;
        if (marks >= 70) return 3.7;
        if (marks >= 65) return 3.3;
        if (marks >= 60) return 3.0;
        if (marks >= 55) return 2.7;
        if (marks >= 50) return 2.3;
        if (marks >= 45) return 2.0;
        if (marks >= 40) return 1.7;
        if (marks >= 35) return 1.0;
        return 0.0;
    }

    public static Double toGradePoint(double marks, boolean examPresent, boolean approvedExamMedical) {
        if (approvedExamMedical) {
            return null;
        }
        if (!examPresent) {
            return 0.0;
        }
        return toGradePoint(marks);
    }

    public static boolean isEnglishCourse(String courseCode) {
        return courseCode != null && courseCode.trim().toUpperCase().startsWith("ENG");
    }
}
