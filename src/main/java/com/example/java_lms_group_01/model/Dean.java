package com.example.java_lms_group_01.model;

import java.time.LocalDate;

public class Dean extends User{
    private int deptId;

    public Dean(int userId, String firstName, String lastName, String email, String address, String phoneNumber, LocalDate dateOfBirth, String gender) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
    }

    public LocalDate getDateOfAppointment() {
        return dateOfAppointment;
    }

    public void setDateOfAppointment(LocalDate dateOfAppointment) {
        this.dateOfAppointment = dateOfAppointment;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    private LocalDate dateOfAppointment;
    private String qualifications;
}
