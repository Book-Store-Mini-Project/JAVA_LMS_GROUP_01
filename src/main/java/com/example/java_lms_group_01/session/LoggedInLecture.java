package com.example.java_lms_group_01.session;

// Stores the current logged-in lecturer registration number.
public class LoggedInLecture {

    private static String registrationNo;

    private LoggedInLecture() {
    }

    public static void setRegistrationNo(String regNo) {
        registrationNo = regNo;
    }

    public static String getRegistrationNo() {
        return registrationNo;
    }

    public static void clear() {
        registrationNo = null;
    }
}
