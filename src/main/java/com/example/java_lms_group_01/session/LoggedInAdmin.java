package com.example.java_lms_group_01.session;

// Stores the current logged-in admin registration number.
public class LoggedInAdmin {

    private static String registrationNo;

    private LoggedInAdmin() {
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
