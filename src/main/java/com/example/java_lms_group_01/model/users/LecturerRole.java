package com.example.java_lms_group_01.model.users;

/**
 * Contract for lecturer-specific profile data.
 */
public interface LecturerRole extends UserContract {
    String getRegistrationNo();

    void setRegistrationNo(String registrationNo);

    String getDepartment();

    void setDepartment(String department);

    String getPosition();

    void setPosition(String position);

    String getPassword();

    void setPassword(String password);
}
