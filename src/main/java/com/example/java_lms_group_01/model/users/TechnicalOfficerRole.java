package com.example.java_lms_group_01.model.users;

/**
 * Contract for technical officer-specific profile data.
 */
public interface TechnicalOfficerRole extends UserContract {
    String getRegistrationNo();

    void setRegistrationNo(String registrationNo);

    String getPassword();

    void setPassword(String password);
}
