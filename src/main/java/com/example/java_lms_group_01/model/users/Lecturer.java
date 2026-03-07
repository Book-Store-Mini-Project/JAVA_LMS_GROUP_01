package com.example.java_lms_group_01.model.users;

import java.time.LocalDate;

public class Lecturer extends User {
    private String registrationNo ;
    private LocalDate department;
    private String position;
    private String password;

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public LocalDate getDepartment() {
        return department;
    }

    public void setDepartment(LocalDate department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Lecturer{" +
                "registrationNo='" + registrationNo + '\'' +
                ", department=" + department +
                ", position='" + position + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}