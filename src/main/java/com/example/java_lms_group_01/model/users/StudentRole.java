package com.example.java_lms_group_01.model.users;

/**
 * Contract for student-specific profile data.
 */
public interface StudentRole extends UserContract {
    String getRegistrationNo();

    void setRegistrationNo(String registrationNo);

    String getPassword();

    void setPassword(String password);

    float getGPA();

    void setGPA(float gpa);

    String getBatch();

    void setBatch(String batch);

    String getStatus();

    void setStatus(String status);
}
