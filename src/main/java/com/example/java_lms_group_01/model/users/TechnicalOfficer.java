package com.example.java_lms_group_01.model.users;

import java.time.LocalDate;

/**
 * Technical officer object with the extra fields needed for login and profile data.
 */
public class TechnicalOfficer extends User implements TechnicalOfficerRole {
    private String registrationNo;
    private String password;

    public TechnicalOfficer() {
    }

    public TechnicalOfficer(String userId, String firstName, String lastName, String email, String address,
                            String phoneNumber, LocalDate dateOfBirth, String gender,
                            String registrationNo, String password) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
        this.registrationNo = registrationNo;
        this.password = password;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "TechnicalOfficer{" +
                "registrationNo='" + registrationNo + '\'' +
                '}';
    }
}
