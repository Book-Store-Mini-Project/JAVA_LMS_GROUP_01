package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * JavaFX table row that shows SGPA and CGPA for one undergraduate.
 */
public class UndergraduateSummary {
    private final SimpleStringProperty studentReg;
    private final SimpleStringProperty studentName;
    private final SimpleStringProperty sgpa;
    private final SimpleStringProperty cgpa;

    public UndergraduateSummary(String studentReg, String studentName, String sgpa, String cgpa) {
        this.studentReg = new SimpleStringProperty(studentReg);
        this.studentName = new SimpleStringProperty(studentName);
        this.sgpa = new SimpleStringProperty(sgpa);
        this.cgpa = new SimpleStringProperty(cgpa);
    }

    public SimpleStringProperty studentRegProperty() { return studentReg; }
    public SimpleStringProperty studentNameProperty() { return studentName; }
    public SimpleStringProperty sgpaProperty() { return sgpa; }
    public SimpleStringProperty cgpaProperty() { return cgpa; }

    public String getStudentReg() { return studentReg.get(); }
    public String getStudentName() { return studentName.get(); }
    public String getSgpa() { return sgpa.get(); }
    public String getCgpa() { return cgpa.get(); }
}
