package com.example.java_lms_group_01.util;

import com.example.java_lms_group_01.model.summary.GradeResult;
import com.example.java_lms_group_01.model.summary.MarkBreakdown;

// convert marks into Grades
public final class GradeScaleUtil {

    private static final double CA_MINIMUM_RATIO  = 0.40;
    private static final double END_MINIMUM_RATIO = 0.35;

    private GradeScaleUtil() {
    }

    // Convert numeric marks into Grades
    public static String toLetterGrade(double marks) {
        if (marks >= 80) return "A+";
        else if (marks >= 75) return "A";
        else if (marks >= 70) return "A-";
        else if (marks >= 65) return "B+";
        else if (marks >= 60) return "B";
        else if (marks >= 55) return "B-";
        else if (marks >= 50) return "C+";
        else if (marks >= 45) return "C";
        else if (marks >= 40) return "C-";
        else if (marks >= 35) return "D";
        else return "E";
    }

    // Convert numeric marks into GPA value
    public static double toGradePoint(double marks) {
        if (marks >= 75) return 4.0;
        else if (marks >= 70) return 3.7;
        else if (marks >= 65) return 3.3;
        else if (marks >= 60) return 3.0;
        else if (marks >= 55) return 2.7;
        else if (marks >= 50) return 2.3;
        else if (marks >= 45) return 2.0;
        else if (marks >= 40) return 1.7;
        else if (marks >= 35) return 1.3;
        else return 0.0;
    }

    // Find minimum CA pass mark (40%)
    public static double minimumRequiredMark(double maximum) {
        if (maximum <= 0) {
            return 0.0;
        }
        return maximum * CA_MINIMUM_RATIO;
    }

    // Check if CA is passed
    public static boolean meetsCaRequirement(MarkBreakdown breakdown) {

        double caMarks = breakdown.getCaMarks();

        double caMaximum = breakdown.getCaMaximum();

        // 3. Handle edge case (no CA component)
        if (caMaximum <= 0) {
            return true;
        }

        // 4. Calculate minimum required CA marks (40%)
        double requiredCaMarks = caMaximum * CA_MINIMUM_RATIO;

        // 5. Compare student marks with required minimum
        if (caMarks >= requiredCaMarks) {
            return true;
        } else {
            return false;
        }
    }

    // Check if END exam is passed
    public static boolean meetsEndRequirement(MarkBreakdown breakdown) {

        double endMarks = breakdown.getEndMarks();

        double endMaximum = breakdown.getEndMaximum();

        // 3. Handle edge case (no End exam component)
        if (endMaximum <= 0) {
            return true; // no exam, automatically pass
        }

        // 4. Calculate minimum required End marks (35%)
        double requiredEndMarks = endMaximum * END_MINIMUM_RATIO;

        // 5. Compare student marks with required minimum
        if (endMarks >= requiredEndMarks) {
            return true;
        } else {
            return false;
        }
    }

    // This decides FINAL result
    public static GradeResult evaluatePublishedGrade(
            MarkBreakdown breakdown,
            boolean attendanceEligible,
            String examAttendanceStatus,
            boolean approvedExamMedical) {

        // If student is not eligible for attendance → automatically fail
        if (!attendanceEligible) {
            return new GradeResult("E", 0.0);
        }

        //Check if course has an end exam component
        boolean hasEndComponent = breakdown.getEndMaximum() > 0;

        //Check CA and End pass conditions
        boolean caPassed = meetsCaRequirement(breakdown);
        boolean endPassed = meetsEndRequirement(breakdown);

        // get exam Attendance Status
        String safeExamStatus = examAttendanceStatus == null ? "" : examAttendanceStatus.trim().toLowerCase();

        // Convert status into boolean
        boolean examPresent = "present".equals(safeExamStatus);
        boolean examAbsent = "absent".equals(safeExamStatus);

        // If exam exists but student did not attend
        if (hasEndComponent && !examPresent) {

            // If student was absent AND has medical approval
            if (examAbsent && approvedExamMedical) {
                return new GradeResult("MC", null);
            }

            // Otherwise fail
            return new GradeResult("E", 0.0);
        }

        // If student failed BOTH CA and End exam
        if (!caPassed && !endPassed) {
            return new GradeResult("E", 0.0);
        }

        // If student failed CA only
        if (!caPassed) {
            return new GradeResult("EC", null);
        }

        // If student failed End exam only
        if (!endPassed) {
            return new GradeResult("EE", null);
        }

        // If all conditions passed
        double totalMarks = breakdown.getTotalMarks();

        // Convert total marks to [ Letter grade + GPA ]
        return new GradeResult(
                toLetterGrade(totalMarks),
                toGradePoint(totalMarks)
        );
    }

    // Check if courseCode is equal to English [SGPA CGPA]
    public static boolean isEnglishCourse(String courseCode) {
        return courseCode != null &&
                courseCode.trim().toUpperCase().startsWith("ENG");
    }
}
