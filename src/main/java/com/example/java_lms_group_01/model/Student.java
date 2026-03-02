package com.example.java_lms_group_01.model;

import com.example.java_lms_group_01.model.User;

import java.time.LocalDate;

public class Student extends User {
    private int batchId;
    private String program;
    private double gpa;

    public Student(int userId, String firstName, String lastName, String email,
                   String address, String phoneNumber, LocalDate dateOfBirth, String gender,
                   int batchId, String program, double gpa) {
        super(userId, firstName, lastName, email, address, phoneNumber, dateOfBirth, gender);
        this.batchId = batchId;
        this.program = program;
        this.gpa = gpa;
    }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

}