package com.example.java_lms_group_01.util;

public final class AttendanceEligibilityUtil {

    public static final double MIN_ELIGIBILITY_PERCENTAGE = 80.0;

    private AttendanceEligibilityUtil() {
    }

    public static double calculatePercentage(int eligibleSessions, int totalSessions) {
        if (totalSessions <= 0) {
            return 0.0;
        }
        return eligibleSessions * 100.0 / totalSessions;
    }

    public static String formatPercentage(int eligibleSessions, int totalSessions) {
        return String.format("%.2f%%", calculatePercentage(eligibleSessions, totalSessions));
    }

    public static String toEligibilityStatus(int eligibleSessions, int totalSessions) {
        return calculatePercentage(eligibleSessions, totalSessions) >= MIN_ELIGIBILITY_PERCENTAGE
                ? "Eligible"
                : "Not Eligible";
    }
}
