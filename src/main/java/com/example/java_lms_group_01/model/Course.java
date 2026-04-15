package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple course model used by forms, tables, and repositories.
 */
public class Course {
    private String courseCode;
    private String name;
    private String lecturerRegistrationNo;
    private String department;
    private String semester;
    private int credit;
    private CourseType courseType;
    private String enrollmentStatus;

    public Course() {
    }

    public Course(String courseCode, String name, String lecturerRegistrationNo, String department,
                  String semester, int credit, CourseType courseType) {
        this.courseCode = courseCode;
        this.name = name;
        this.lecturerRegistrationNo = lecturerRegistrationNo;
        this.department = department;
        this.semester = semester;
        this.credit = credit;
        this.courseType = courseType;
    }

    public Course(String courseCode, String name, String lecturerRegistrationNo, String department,
                  String semester, int credit, String courseType) {
        this(courseCode, name, lecturerRegistrationNo, department, semester, credit, CourseType.fromValue(courseType));
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLecturerRegistrationNo() {
        return lecturerRegistrationNo;
    }

    public void setLecturerRegistrationNo(String lecturerRegistrationNo) {
        this.lecturerRegistrationNo = lecturerRegistrationNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getCourseType() {
        return courseType == null ? "" : courseType.getDatabaseValue();
    }

    public CourseType getCourseTypeEnum() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = CourseType.fromValue(courseType);
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus == null ? "" : enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty nameProperty() {
        return property(name);
    }

    public SimpleStringProperty lecturerProperty() {
        return property(lecturerRegistrationNo);
    }

    public SimpleStringProperty departmentProperty() {
        return property(department);
    }

    public SimpleStringProperty semesterProperty() {
        return property(semester);
    }

    public SimpleStringProperty creditProperty() {
        return new SimpleStringProperty(String.valueOf(credit));
    }

    public SimpleStringProperty typeProperty() {
        return property(getCourseType());
    }

    public SimpleStringProperty enrollmentStatusProperty() {
        return property(getEnrollmentStatus());
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(value == null ? "" : value);
    }
}
