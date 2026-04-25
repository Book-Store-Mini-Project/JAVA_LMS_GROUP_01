package com.example.java_lms_group_01.session;

// Stores the current logged-in student registration number.
public class LoggedInStudent {

    private static String registrationNo;

    private LoggedInStudent() {
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
