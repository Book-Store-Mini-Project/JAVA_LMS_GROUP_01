package com.example.java_lms_group_01.model;

public class Course {
    private String courseCode;
    private String name;
    private int credits;
    private boolean hasTheory;
    private boolean hasPractical;
    private int lecturerId;
    private int deptId;
    private String semester;

    public Course() {
    }

    public Course(String courseCode, String name, int credits, boolean hasTheory, boolean hasPractical,
                  int lecturerId, int deptId, String semester) {
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.hasTheory = hasTheory;
        this.hasPractical = hasPractical;
        this.lecturerId = lecturerId;
        this.deptId = deptId;
        this.semester = semester;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public boolean isHasTheory() {
        return hasTheory;
    }

    public void setHasTheory(boolean hasTheory) {
        this.hasTheory = hasTheory;
    }

    public boolean isHasPractical() {
        return hasPractical;
    }

    public void setHasPractical(boolean hasPractical) {
        this.hasPractical = hasPractical;
    }

    public int getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(int lecturerId) {
        this.lecturerId = lecturerId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
