package com.example.java_lms_group_01.model;

import java.time.LocalDate;

public class UserManagementRow {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;

    private String role;
    private String registrationNo;
    private String password;
    private String department;
    private Double gpa;
    private String status;
    private String position;

    public UserManagementRow(String userId, String firstName, String lastName, String email, String address,
                             String phoneNumber, LocalDate dateOfBirth, String gender, String role,
                             String registrationNo, String password, String department, Double gpa,
                             String status, String position) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.role = role;
        this.registrationNo = registrationNo;
        this.password = password;
        this.department = department;
        this.gpa = gpa;
        this.status = status;
        this.position = position;
    }

    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getRole() { return role; }
    public String getRegistrationNo() { return registrationNo; }
    public String getPassword() { return password; }
    public String getDepartment() { return department; }
    public Double getGpa() { return gpa; }
    public String getStatus() { return status; }
    public String getPosition() { return position; }
}
