package com.example.java_lms_group_01.session;

// Stores the current logged-in technical officer registration number.
public class LoggedInTechnicalOfficer {

    private static String registrationNo;

    private LoggedInTechnicalOfficer() {
    }

    public static String getRegistrationNo() {
        return registrationNo;
    }

    public static void setRegistrationNo(String registrationNo) {
        LoggedInTechnicalOfficer.registrationNo = registrationNo;
    }

    public static void clear() {
        registrationNo = null;
    }
}
