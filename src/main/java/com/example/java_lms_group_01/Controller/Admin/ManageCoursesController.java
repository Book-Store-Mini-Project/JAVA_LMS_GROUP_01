package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Service.AdminService;
import com.example.java_lms_group_01.model.Course;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Admin screen for viewing, filtering, adding, editing, and deleting courses.
 */
public class ManageCoursesController implements Initializable {

    @FXML
    private ComboBox<String> cmbDeptFilter;

    @FXML
    private TableColumn<Course, String> colCourseCode;

    @FXML
    private TableColumn<Course, Number> colCredits;

    @FXML
    private TableColumn<Course, String> colDeptId;

    @FXML
    private TableColumn<Course, String> colHasPractical;

    @FXML
    private TableColumn<Course, String> colHasTheory;

    @FXML
    private TableColumn<Course, String> colLecturerId;

    @FXML
    private TableColumn<Course, String> colName;

    @FXML
    private TableColumn<Course, String> colSemester;

    @FXML
    private TableView<Course> tblCourses;

    @FXML
    private TextField txtSearchCourse;

    private final AdminService adminService = new AdminService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureColumns();
        loadDepartmentFilter("All");
        loadCourses(null, null);

        txtSearchCourse.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbDeptFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void configureColumns() {
        colHasPractical.setVisible(false);
        colCourseCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCredits.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCredit()));
        colHasTheory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseType()));
        colLecturerId.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getLecturerRegistrationNo())));
        colDeptId.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getDepartment())));
        colSemester.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getSemester())));
    }

    private void loadDepartmentFilter(String selectedValue) {
        try {
            cmbDeptFilter.getItems().clear();
            cmbDeptFilter.getItems().add("All");
            cmbDeptFilter.getItems().addAll(adminService.getCourseDepartments());
            cmbDeptFilter.setValue(cmbDeptFilter.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load department filters.", e);
        }
    }

    private void applyFilters() {
        String selectedDept = cmbDeptFilter.getValue();
        String department = "All".equals(selectedDept) ? null : selectedDept;
        loadCourses(department, txtSearchCourse.getText());
    }

    // Load courses using the current filter values.
    private void loadCourses(String department, String keyword) {
        try {
            List<Course> courses = adminService.getCourses(department, keyword);
            tblCourses.getItems().setAll(courses);
        } catch (SQLException e) {
            showError("Failed to load courses.", e);
        }
    }

    @FXML
    void btnOnActionAddNewCourse(ActionEvent event) {
        Course course = showCourseForm(null);
        if (course == null) {
            return;
        }

        try {
            boolean saved = adminService.addCourse(course);
            if (saved) {
                String selectedDept = cmbDeptFilter.getValue();
                loadDepartmentFilter(selectedDept);
                applyFilters();
                showInfo("Course added successfully.");
            } else {
                showInfo("No course was added.");
            }
        } catch (SQLException e) {
            showError("Failed to add course.", e);
        }
    }

    @FXML
    void btnOnActionDeleteCourse(ActionEvent event) {
        Course selectedCourse = tblCourses.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showInfo("Please select a course to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete Course");
        confirmation.setContentText("Delete course " + selectedCourse.getCourseCode() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean deleted = adminService.deleteCourse(selectedCourse.getCourseCode());
            if (deleted) {
                String selectedDept = cmbDeptFilter.getValue();
                loadDepartmentFilter(selectedDept);
                applyFilters();
                showInfo("Course deleted successfully.");
            } else {
                showInfo("No course was deleted.");
            }
        } catch (SQLException e) {
            showError("Failed to delete course.", e);
        }
    }

    @FXML
    void btnOnActionUpdateCourse(ActionEvent event) {
        Course selectedCourse = tblCourses.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showInfo("Please select a course to edit.");
            return;
        }

        Course updatedCourse = showCourseForm(selectedCourse);
        if (updatedCourse == null) {
            return;
        }

        try {
            boolean updated = adminService.updateCourse(updatedCourse);
            if (updated) {
                String selectedDept = cmbDeptFilter.getValue();
                loadDepartmentFilter(selectedDept);
                applyFilters();
                showInfo("Course updated successfully.");
            } else {
                showInfo("No course was updated.");
            }
        } catch (SQLException e) {
            showError("Failed to update course.", e);
        }
    }

    // Open the reusable course form dialog and return the saved course object.
    private Course showCourseForm(Course existingCourse) {
        boolean editMode = existingCourse != null;
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Edit Course" : "Add New Course");
        dialog.setHeaderText(editMode ? "Update selected course details." : "Enter new course details.");

        ButtonType saveButtonType = new ButtonType(editMode ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        CourseFormController formController;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Admin/course_form.fxml"));
            Region formRoot = loader.load();
            formController = loader.getController();
            dialog.getDialogPane().setContent(formRoot);
        } catch (IOException e) {
            showError("Failed to open course form.", e);
            return null;
        }

        if (editMode) {
            formController.setupForEdit(existingCourse);
        } else {
            formController.setupForCreate();
        }

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        final Course[] resultHolder = new Course[1];
        saveButton.addEventFilter(ActionEvent.ACTION, actionEvent -> {
            try {
                resultHolder[0] = formController.buildCourse();
            } catch (IllegalArgumentException e) {
                showInfo(e.getMessage());
                actionEvent.consume();
            }
        });

        dialog.setResultConverter(button -> button == saveButtonType ? resultHolder[0] : null);
        Optional<Course> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private String value(String text) {
        return text == null ? "" : text;
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
