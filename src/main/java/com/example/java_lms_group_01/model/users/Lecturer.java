package com.example.java_lms_group_01.model.users;

/**
 * Lecturer object with department, position, and registration details.
 */
public class Lecturer extends User implements LecturerRole {
    private String registrationNo;
    private String department;
    private String position;
    private String password;

    public Lecturer() {
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
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
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
