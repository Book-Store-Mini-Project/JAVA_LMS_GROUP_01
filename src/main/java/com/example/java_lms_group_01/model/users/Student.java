package com.example.java_lms_group_01.model.users;

import java.time.LocalDate;

/**
 * Student object used when the program needs student-specific fields.
 */
public class Student extends User implements StudentRole {
    private String registrationNo;
    private String password;
    private String batch;
    private float gpa;
    private String status;

    public Student() {
    }

    public Student(String userId, String firstName, String lastName, String email, String address,
                   String phoneNumber, LocalDate dateOfBirth, String gender) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getGPA() {
        return gpa;
    }

    public void setGPA(float gpa) {
        this.gpa = gpa;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }
}
