package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class StudentCoursePageController {

    @FXML
    private TableView<Course> tblCourses;
    @FXML
    private TableColumn<Course, String> colCourseCode;
    @FXML
    private TableColumn<Course, String> colName;
    @FXML
    private TableColumn<Course, String> colLecturer;
    @FXML
    private TableColumn<Course, String> colDepartment;
    @FXML
    private TableColumn<Course, String> colSemester;
    @FXML
    private TableColumn<Course, String> colCredit;
    @FXML
    private TableColumn<Course, String> colType;
    @FXML
    private TableColumn<Course, String> colEnrollmentStatus;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colName.setCellValueFactory(d -> d.getValue().nameProperty());
        colLecturer.setCellValueFactory(d -> d.getValue().lecturerProperty());
        colDepartment.setCellValueFactory(d -> d.getValue().departmentProperty());
        colSemester.setCellValueFactory(d -> d.getValue().semesterProperty());
        colCredit.setCellValueFactory(d -> d.getValue().creditProperty());
        colType.setCellValueFactory(d -> d.getValue().typeProperty());
        colEnrollmentStatus.setCellValueFactory(d -> d.getValue().enrollmentStatusProperty());
        loadCourses();
    }

    private void loadCourses() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var rows = studentRepository.findCoursesByStudent(regNo).stream()
                    .map(r -> {
                        Course course = new Course(r.courseCode(), r.name(), r.lecturer(), r.department(), r.semester(),
                                Integer.parseInt(r.credit()), r.type());
                        course.setEnrollmentStatus(r.enrollmentStatus());
                        return course;
                    })
                    .toList();
            tblCourses.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load course details.", e);
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
