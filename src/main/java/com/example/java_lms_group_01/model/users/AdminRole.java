package com.example.java_lms_group_01.model.users;

/**
 * Contract for admin profile data.
 */
public interface AdminRole extends UserContract {
    String getRegistrationNo();

    void setRegistrationNo(String registrationNo);

    String getPassword();

    void setPassword(String password);
}
