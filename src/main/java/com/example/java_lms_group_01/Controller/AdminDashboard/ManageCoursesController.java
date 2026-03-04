package com.example.java_lms_group_01.Controller.AdminDashboard;

import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.Repository.CourseRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ManageCoursesController implements Initializable {

    @FXML
    private ComboBox<String> cmbDeptFilter;

    @FXML
    private TableColumn<Course, String> colCourseCode;

    @FXML
    private TableColumn<Course, Number> colCredits;

    @FXML
    private TableColumn<Course, Number> colDeptId;

    @FXML
    private TableColumn<Course, String> colHasPractical;

    @FXML
    private TableColumn<Course, String> colHasTheory;

    @FXML
    private TableColumn<Course, Number> colLecturerId;

    @FXML
    private TableColumn<Course, String> colName;

    @FXML
    private TableColumn<Course, String> colSemester;

    @FXML
    private TableView<Course> tblCourses;

    @FXML
    private TextField txtSearchCourse;

    private final CourseRepository courseRepository = new CourseRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureColumns();
        loadDepartmentFilter();
        loadCourses(null, null);

        txtSearchCourse.textProperty().addListener((obs, oldValue, newValue) ->
                applyFilters()
        );

        cmbDeptFilter.valueProperty().addListener((obs, oldValue, newValue) ->
                applyFilters()
        );
    }

    private void configureColumns() {
        colCourseCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCourseCode()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCredits.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCredits()));
        colHasTheory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isHasTheory() ? "Yes" : "No"));
        colHasPractical.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isHasPractical() ? "Yes" : "No"));
        colLecturerId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getLecturerId()));
        colDeptId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDeptId()));
        colSemester.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemester()));
    }

    private void loadDepartmentFilter() {
        try {
            cmbDeptFilter.getItems().clear();
            cmbDeptFilter.getItems().add("All");
            for (Integer deptId : courseRepository.findAllDepartmentIds()) {
                cmbDeptFilter.getItems().add(String.valueOf(deptId));
            }
            cmbDeptFilter.setValue("All");
        } catch (SQLException e) {
            showError("Failed to load department filters.", e);
        }
    }

    private void applyFilters() {
        String selectedDept = cmbDeptFilter.getValue();
        Integer deptId = null;
        if (selectedDept != null && !"All".equals(selectedDept)) {
            deptId = Integer.parseInt(selectedDept);
        }
        loadCourses(deptId, txtSearchCourse.getText());
    }

    private void loadCourses(Integer deptId, String keyword) {
        try {
            List<Course> courses = courseRepository.findByFilters(deptId, keyword);
            tblCourses.getItems().setAll(courses);
        } catch (SQLException e) {
            showError("Failed to load courses.", e);
        }
    }

    @FXML
    void btnOnActionAddNewCourse(ActionEvent event) {
        showInfo("Add New Course is not wired to a form yet.");
    }

    @FXML
    void btnOnActionDeleteCourse(ActionEvent event) {
        Course selectedCourse = tblCourses.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            showInfo("Please select a course to delete.");
            return;
        }

        try {
            boolean deleted = courseRepository.deleteByCourseCode(selectedCourse.getCourseCode());
            if (deleted) {
                loadCourses(null, null);
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
        showInfo("Update Course is not wired to an edit form yet.");
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
