package com.example.java_lms_group_01.model;

import java.time.LocalDate;

public class TechnicalOfficer extends User{
    private int deptId;
    private String position;
    private String labAssigned;
    private String shift;
    private String qualifications;

    public TechnicalOfficer(int userId, String firstName, String lastName, String email, String address, String phoneNumber, LocalDate dateOfBirth, String gender) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getLabAssigned() {
        return labAssigned;
    }

    public void setLabAssigned(String labAssigned) {
        this.labAssigned = labAssigned;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }
}
