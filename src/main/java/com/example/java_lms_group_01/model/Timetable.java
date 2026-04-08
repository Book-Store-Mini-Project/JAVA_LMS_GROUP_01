package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;

public class Timetable {
    private String timeTableId;
    private String department;
    private String lecId;
    private String courseCode;
    private String adminId;
    private String day;
    private LocalTime startTime;
    private LocalTime endTime;
    private String sessionType;

    public Timetable() {
    }

    public Timetable(String timeTableId, String department, String lecId, String courseCode, String adminId,
                     String day, LocalTime startTime, LocalTime endTime, String sessionType) {
        this.timeTableId = timeTableId;
        this.department = department;
        this.lecId = lecId;
        this.courseCode = courseCode;
        this.adminId = adminId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sessionType = sessionType;
    }

    public String getTimeTableId() {
        return timeTableId;
    }

    public void setTimeTableId(String timeTableId) {
        this.timeTableId = timeTableId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLecId() {
        return lecId;
    }

    public void setLecId(String lecId) {
        this.lecId = lecId;
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
        return new SimpleStringProperty(value(timeTableId));
    }

    public SimpleStringProperty departmentProperty() {
        return new SimpleStringProperty(value(department));
    }

    public SimpleStringProperty lecIdProperty() {
        return new SimpleStringProperty(value(lecId));
    }

    public SimpleStringProperty lecturerIdProperty() {
        return lecIdProperty();
    }

    public SimpleStringProperty courseCodeProperty() {
        return new SimpleStringProperty(value(courseCode));
    }

    public SimpleStringProperty adminIdProperty() {
        return new SimpleStringProperty(value(adminId));
    }

    public SimpleStringProperty dayProperty() {
        return new SimpleStringProperty(value(day));
    }

    public SimpleStringProperty startTimeProperty() {
        return new SimpleStringProperty(startTime == null ? "" : startTime.toString());
    }

    public SimpleStringProperty endTimeProperty() {
        return new SimpleStringProperty(endTime == null ? "" : endTime.toString());
    }

    public SimpleStringProperty sessionTypeProperty() {
        return new SimpleStringProperty(value(sessionType));
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
