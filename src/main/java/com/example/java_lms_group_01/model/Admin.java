package com.example.java_lms_group_01.model;

import com.example.java_lms_group_01.model.User;

import java.time.LocalDate;

public class Admin extends User {
    private int deptId;
    private LocalDate dateOfAppointment;
    private String accessLevel;

    public Admin(int userId, String firstName, String lastName, String email,
                 String address, String phoneNumber, LocalDate dateOfBirth, String gender,
                 int deptId, LocalDate dateOfAppointment, String accessLevel) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
        this.deptId = deptId;
        this.dateOfAppointment = dateOfAppointment;
        this.accessLevel = accessLevel;
    }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }
    public LocalDate getDateOfAppointment() { return dateOfAppointment; }
    public void setDateOfAppointment(LocalDate dateOfAppointment) { this.dateOfAppointment = dateOfAppointment; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }


}