package com.example.java_lms_group_01.model;

import com.example.java_lms_group_01.model.User;

import java.time.LocalDate;

public class Lecturer extends User {
    private int deptId;
    private LocalDate dateOfJoining;
    private String specialization;

    public Lecturer(int userId, String firstName, String lastName, String email,
                    String address, String phoneNumber, LocalDate dateOfBirth, String gender,
                    int deptId, LocalDate dateOfJoining, String specialization) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
        this.deptId = deptId;
        this.dateOfJoining = dateOfJoining;
        this.specialization = specialization;
    }

    public int getDeptId() { return deptId; }
    public void setDeptId(int deptId) { this.deptId = deptId; }
    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

}