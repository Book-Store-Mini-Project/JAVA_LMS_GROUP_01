package com.example.java_lms_group_01.model.users;

import java.time.LocalDate;

/**
 * Common contract shared by every user type in the system.
 */
public interface UserContract {
    String getUserId();

    void setUserId(String userId);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getEmail();

    void setEmail(String email);

    String getAddress();

    void setAddress(String address);

    LocalDate getDateOfBirth();

    void setDateOfBirth(LocalDate dateOfBirth);

    String getPhoneNumber();

    void setPhoneNumber(String phoneNumber);

    String getGender();

    void setGender(String gender);
}
