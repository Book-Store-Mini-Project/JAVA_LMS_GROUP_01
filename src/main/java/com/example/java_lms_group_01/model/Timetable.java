package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;

/**
 * Simple timetable model used by admin, lecturer, student, and technical officer screens.
 */
public class Timetable {
    private String timetableId;
    private String department;
    private String lecturerId;
    private String courseCode;
    private String adminId;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionType;

    public Timetable() {
    }

    public Timetable(String timetableId, String department, String lecturerId, String courseCode, String adminId,
                     String day, LocalTime startTime, LocalTime endTime, String sessionType) {
        this.timetableId = timetableId;
        this.department = department;
        this.lecturerId = lecturerId;
        this.courseCode = courseCode;
        this.adminId = adminId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sessionType = sessionType;
    }

    public String getTimeTableId() {
        return timetableId;
    }

    public void setTimeTableId(String timeTableId) {
        this.timetableId = timeTableId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLecId() {
        return lecturerId;
    }

    public void setLecId(String lecId) {
        this.lecturerId = lecId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public SimpleStringProperty timetableIdProperty() {
        return property(timetableId);
    }

    public SimpleStringProperty departmentProperty() {
        return property(department);
    }

    public SimpleStringProperty lecturerIdProperty() {
        return property(lecturerId);
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty adminIdProperty() {
        return property(adminId);
    }

    public SimpleStringProperty dayProperty() {
        return property(day);
    }

    public SimpleStringProperty startTimeProperty() {
        return property(startTime == null ? "" : startTime.toString());
    }

    public SimpleStringProperty endTimeProperty() {
        return property(endTime == null ? "" : endTime.toString());
    }

    public SimpleStringProperty sessionTypeProperty() {
        return property(sessionType);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(value == null ? "" : value);
    }
}
