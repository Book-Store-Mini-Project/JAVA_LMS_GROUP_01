package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple student table model.
 */
public class Student {
    private String regNo;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String status;
    private String gpa;

    public Student() {
    }

    public Student(String regNo, String name, String email, String phone, String department,
                   String status, String gpa) {
        setRegNo(regNo);
        setName(name);
        setEmail(email);
        setPhone(phone);
        setDepartment(department);
        setStatus(status);
        setGpa(gpa);
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = text(regNo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = text(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = text(email);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = text(phone);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = text(department);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = text(status);
    }

    public String getGpa() {
        return gpa;
    }

    public void setGpa(String gpa) {
        this.gpa = text(gpa);
    }

    public SimpleStringProperty regNoProperty() {
        return property(regNo);
    }

    public SimpleStringProperty nameProperty() {
        return property(name);
    }

    public SimpleStringProperty emailProperty() {
        return property(email);
    }

    public SimpleStringProperty phoneProperty() {
        return property(phone);
    }

    public SimpleStringProperty departmentProperty() {
        return property(department);
    }

    public SimpleStringProperty statusProperty() {
        return property(status);
    }

    public SimpleStringProperty gpaProperty() {
        return property(gpa);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
