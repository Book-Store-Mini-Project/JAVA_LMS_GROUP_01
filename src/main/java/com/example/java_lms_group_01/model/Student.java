package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Student {
    private final SimpleStringProperty regNo;
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty phone;
    private final SimpleStringProperty department;
    private final SimpleStringProperty status;
    private final SimpleStringProperty gpa;

    public Student(String regNo, String name, String email, String phone, String department,
                   String status, String gpa) {
        this.regNo = new SimpleStringProperty(regNo);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.department = new SimpleStringProperty(department);
        this.status = new SimpleStringProperty(status);
        this.gpa = new SimpleStringProperty(gpa);
    }

    public SimpleStringProperty regNoProperty() { return regNo; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty phoneProperty() { return phone; }
    public SimpleStringProperty departmentProperty() { return department; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty gpaProperty() { return gpa; }
}
