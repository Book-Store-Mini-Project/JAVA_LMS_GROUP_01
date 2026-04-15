package com.example.java_lms_group_01.util;

/**
 * Converts marks into published grades and grade points.
 */
public final class GradeScaleUtil {

    // Assumption: the prescribed minimum for both CA and ESA is 40% of that component's max weight.
    private static final double COMPONENT_MINIMUM_RATIO = 0.40;
    private static final double EPSILON = 1.0e-9;

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
        if (marks >= 35) return 1.3;
        return 0.0;
    }

    public static double minimumRequiredMark(double maximum) {
        if (maximum <= EPSILON) {
            return 0.0;
        }
        return maximum * COMPONENT_MINIMUM_RATIO;
    }

    public static boolean meetsComponentRequirement(double marks, double maximum) {
        return maximum <= EPSILON || meetsComponentMinimum(marks, maximum);
    }

    public static boolean meetsCaRequirement(MarkBreakdown breakdown) {
        return meetsComponentRequirement(breakdown.getCaMarks(), breakdown.getCaMaximum());
    }

    public static boolean meetsEndRequirement(MarkBreakdown breakdown) {
        return meetsComponentRequirement(breakdown.getEndMarks(), breakdown.getEndMaximum());
    }

    public static GradeResult evaluatePublishedGrade(MarkBreakdown breakdown,
                                                     boolean attendanceEligible,
                                                     boolean examPresent,
                                                     boolean approvedExamMedical) {
        if (!attendanceEligible) {
            return new GradeResult("E", 0.0);
        }

        if (approvedExamMedical) {
            return new GradeResult("MC", null);
        }

        boolean hasCaComponent = breakdown.getCaMaximum() > EPSILON;
        boolean hasEndComponent = breakdown.getEndMaximum() > EPSILON;
        boolean caPassed = meetsCaRequirement(breakdown);
        boolean endPassed = meetsEndRequirement(breakdown);

        if (hasEndComponent && !examPresent) {
            return caPassed ? new GradeResult("EE", null) : new GradeResult("E", 0.0);
        }

        if (!caPassed && !endPassed) {
            return new GradeResult("E", 0.0);
        }
        if (!caPassed) {
            return new GradeResult("EC", null);
        }
        if (!endPassed) {
            return new GradeResult("EE", null);
        }

        return new GradeResult(toLetterGrade(breakdown.getTotalMarks()), toGradePoint(breakdown.getTotalMarks()));
    }

    public static boolean isEnglishCourse(String courseCode) {
        return courseCode != null && courseCode.trim().toUpperCase().startsWith("ENG");
    }

    private static boolean meetsComponentMinimum(double marks, double maximum) {
        return marks + EPSILON >= maximum * COMPONENT_MINIMUM_RATIO;
    }
}
