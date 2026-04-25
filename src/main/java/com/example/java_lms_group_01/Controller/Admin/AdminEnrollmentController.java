package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Repository.AdminRepository;
import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.model.EnrollmentRecord;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminEnrollmentController {

    @FXML
    private TextField txtSearchEnrollment;
    @FXML
    private ComboBox<String> cmbBatchFilter;
    @FXML
    private TableView<EnrollmentRecord> tblEnrollments;
    @FXML
    private TableColumn<EnrollmentRecord, String> colStudentReg;
    @FXML
    private TableColumn<EnrollmentRecord, String> colStudentName;
    @FXML
    private TableColumn<EnrollmentRecord, String> colBatch;
    @FXML
    private TableColumn<EnrollmentRecord, String> colCourseCode;
    @FXML
    private TableColumn<EnrollmentRecord, String> colCourseName;
    @FXML
    private TableColumn<EnrollmentRecord, String> colEnrollmentDate;
    @FXML
    private TableColumn<EnrollmentRecord, String> colStatus;

    private final AdminRepository adminRepository = new AdminRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadBatchFilter("All");
        loadEnrollments();

        txtSearchEnrollment.textProperty().addListener((obs, oldValue, newValue) -> loadEnrollments());
        cmbBatchFilter.valueProperty().addListener((obs, oldValue, newValue) -> loadEnrollments());
    }

    // Set the enrollment table columns.
    private void setupColumns() {
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colStudentName.setCellValueFactory(d -> d.getValue().studentNameProperty());
        colBatch.setCellValueFactory(d -> d.getValue().batchProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colCourseName.setCellValueFactory(d -> d.getValue().courseNameProperty());
        colEnrollmentDate.setCellValueFactory(d -> d.getValue().enrollmentDateProperty());
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());
    }

    @FXML
    private void btnOnActionAddEnrollment() {
        EnrollmentRecord selected = selectedRecord();
        if (selected == null) {
            return;
        }

        try {
            List<Course> courses = adminRepository.findAvailableCoursesForStudent(selected.getStudentReg());
            if (courses.isEmpty()) {
                showInfo("No available courses for the selected student.");
                return;
            }

            Optional<Course> course = openEnrollmentDialog(selected, courses);
            if (course.isEmpty()) {
                return;
            }

            if (adminRepository.createEnrollment(selected.getStudentReg(), course.get().getCourseCode())) {
                loadBatchFilter(text(cmbBatchFilter.getValue()));
                loadEnrollments();
                showInfo("Enrollment created as active.");
            } else {
                showInfo("No enrollment was created.");
            }
        } catch (IllegalArgumentException | SQLException e) {
            showError("Failed to create enrollment.", e);
        }
    }

    @FXML
    private void btnOnActionMakeCompleted() {
        updateSelectedEnrollmentStatus("completed");
    }

    @FXML
    private void btnOnActionMakeDropped() {
        updateSelectedEnrollmentStatus("dropped");
    }

    @FXML
    private void btnOnActionRefresh() {
        loadBatchFilter(text(cmbBatchFilter.getValue()));
        loadEnrollments();
    }

    // Load the batch filter list.
    private void loadBatchFilter(String selectedValue) {
        try {
            cmbBatchFilter.getItems().clear();
            cmbBatchFilter.getItems().add("All");
            cmbBatchFilter.getItems().addAll(adminRepository.findStudentBatches());
            cmbBatchFilter.setValue(cmbBatchFilter.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load batch filter.", e);
        }
    }

    // Load all enrollment rows using the current search and batch filter.
    private void loadEnrollments() {
        try {
            tblEnrollments.getItems().setAll(
                    adminRepository.findEnrollments(
                            text(txtSearchEnrollment),
                            cmbBatchFilter.getValue()
                    )
            );
        } catch (SQLException e) {
            showError("Failed to load students.", e);
        }
    }

    private void updateSelectedEnrollmentStatus(String status) {
        EnrollmentRecord selected = selectedRecord();
        if (selected == null) {
            return;
        }

        if (!selected.hasEnrollment()) {
            showInfo("This student does not have an enrollment for status update.");
            return;
        }

        try {
            if (adminRepository.updateEnrollmentStatus(selected.getEnrollmentId(), status)) {
                loadEnrollments();
                showInfo("Enrollment status updated to " + status + ".");
            } else {
                showInfo("No enrollment was updated.");
            }
        } catch (IllegalArgumentException | SQLException e) {
            showError("Failed to update enrollment status.", e);
        }
    }

    // Open a small dialog to pick one course for the selected student.
    private Optional<Course> openEnrollmentDialog(EnrollmentRecord selected, List<Course> courses) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Add Enrollment");
        dialog.setHeaderText("Create a new active enrollment for " + selected.getStudentReg());

        ButtonType save = new ButtonType("Add Enrollment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        ComboBox<String> cmbCourse = new ComboBox<>();
        List<String> labels = new ArrayList<>();
        for (Course course : courses) {
            labels.add(text(course.getCourseCode()) + " - " + text(course.getName()));
        }
        cmbCourse.getItems().setAll(labels);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Student Reg:"), 0, 0);
        grid.add(new Label(text(selected.getStudentReg())), 1, 0);
        grid.add(new Label("Student Name:"), 0, 1);
        grid.add(new Label(text(selected.getStudentName())), 1, 1);
        grid.add(new Label("Course:"), 0, 2);
        grid.add(cmbCourse, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != save) {
                return null;
            }
            int index = cmbCourse.getSelectionModel().getSelectedIndex();
            if (index < 0) {
                showInfo("Please select a course.");
                return null;
            }
            return courses.get(index);
        });

        Optional<Course> result = dialog.showAndWait();
        return result;
    }

    private EnrollmentRecord selectedRecord() {
        EnrollmentRecord selected = tblEnrollments.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a student row.");
            return null;
        }
        return selected;
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String text(ComboBox<String> comboBox) {
        return comboBox.getValue() == null ? "" : comboBox.getValue().trim();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Database Error");
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
