package com.example.java_lms_group_01.Controller.AdminDashboard;

import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.model.CourseType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class CourseFormController {

    @FXML
    private TextField txtCourseCode;

    @FXML
    private TextField txtCredit;

    @FXML
    private TextField txtDepartment;

    @FXML
    private TextField txtLecturerRegNo;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSemester;

    @FXML
    private ComboBox<CourseType> cmbCourseType;

    public void setupForCreate() {
        txtCourseCode.setDisable(false);
        cmbCourseType.getItems().setAll(CourseType.values());
        cmbCourseType.setValue(CourseType.THEORY);
    }

    public void setupForEdit(Course course) {
        setupForCreate();
        txtCourseCode.setText(course.getCourseCode());
        txtCourseCode.setDisable(true);
        txtName.setText(course.getName());
        txtCredit.setText(String.valueOf(course.getCredit()));
        txtLecturerRegNo.setText(value(course.getLecturerRegistrationNo()));
        txtDepartment.setText(value(course.getDepartment()));
        txtSemester.setText(value(course.getSemester()));
        cmbCourseType.setValue(course.getCourseTypeEnum());
    }

    public Course buildCourse() {
        String courseCode = value(txtCourseCode);
        String name = value(txtName);
        String lecturerRegNo = value(txtLecturerRegNo);
        String department = value(txtDepartment);
        String semester = value(txtSemester);
        CourseType courseType = cmbCourseType.getValue();

        if (courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Course name is required.");
        }
        if (department.isBlank()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (semester.isBlank()) {
            throw new IllegalArgumentException("Semester is required.");
        }
        if (courseType == null) {
            throw new IllegalArgumentException("Course type is required.");
        }

        int credit;
        try {
            credit = Integer.parseInt(value(txtCredit));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Credit must be a valid number.");
        }

        if (credit <= 0) {
            throw new IllegalArgumentException("Credit must be greater than 0.");
        }

        return new Course(
                courseCode,
                name,
                lecturerRegNo.isBlank() ? null : lecturerRegNo,
                department,
                semester,
                credit,
                courseType
        );
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
