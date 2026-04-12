package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * JavaFX table row that shows calculated marks, grade, GPA, and SGPA.
 */
public class Performance {
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty studentName;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty courseName;
    private final SimpleStringProperty caMarks;
    private final SimpleStringProperty finalMarks;
    private final SimpleStringProperty totalMarks;
    private final SimpleStringProperty grade;
    private final SimpleStringProperty sgpa;
    private final SimpleStringProperty cgpa;

    public Performance(String studentReg, String studentName, String courseCode, String courseName,
                       String caMarks, String finalMarks, String totalMarks, String grade,
                       String sgpa, String cgpa) {
        this.studentReg = new SimpleStringProperty(studentReg);
        this.studentName = new SimpleStringProperty(studentName);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.courseName = new SimpleStringProperty(courseName);
        this.caMarks = new SimpleStringProperty(caMarks);
        this.finalMarks = new SimpleStringProperty(finalMarks);
        this.totalMarks = new SimpleStringProperty(totalMarks);
        this.grade = new SimpleStringProperty(grade);
        this.sgpa = new SimpleStringProperty(sgpa);
        this.cgpa = new SimpleStringProperty(cgpa);
    }

    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty studentNameProperty() { return studentName; }
    public SimpleStringProperty courseCodeProperty() { return courseCode; }
    public SimpleStringProperty courseNameProperty() { return courseName; }
    public SimpleStringProperty caMarksProperty() { return caMarks; }
    public SimpleStringProperty finalMarksProperty() { return finalMarks; }
    public SimpleStringProperty totalMarksProperty() { return totalMarks; }
    public SimpleStringProperty gradeProperty() { return grade; }
    public SimpleStringProperty sgpaProperty() { return sgpa; }
    public SimpleStringProperty cgpaProperty() { return cgpa; }

    public String getStudentReg() { return studentReg.get(); }
    public String getStudentName() { return studentName.get(); }
    public String getCourseCode() { return courseCode.get(); }
    public String getCourseName() { return courseName.get(); }
    public String getCaMarks() { return caMarks.get(); }
    public String getFinalMarks() { return finalMarks.get(); }
    public String getTotalMarks() { return totalMarks.get(); }
    public String getGrade() { return grade.get(); }
    public String getSgpa() { return sgpa.get(); }
    public String getCgpa() { return cgpa.get(); }
}
