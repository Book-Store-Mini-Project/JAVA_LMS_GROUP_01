package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Repository.AdminRepository;
import com.example.java_lms_group_01.model.Course;
import com.example.java_lms_group_01.model.UserRecord;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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

    private final AdminRepository adminRepository = new AdminRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        loadDepartmentFilter("All");
        loadCourses(null, null);

        txtSearchCourse.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbDeptFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    // Set the course table columns.
    private void setupColumns() {
        colHasPractical.setVisible(false);
        colCourseCode.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getCourseCode())));
        colName.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getName())));
        colCredits.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCredit()));
        colHasTheory.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getCourseType())));
        colLecturerId.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getLecturerRegistrationNo())));
        colDeptId.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getDepartment())));
        colSemester.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getSemester())));
    }

    @FXML
    void btnOnActionAddNewCourse(ActionEvent event) {
        Course course = openCourseForm(null);
        if (course == null) {
            return;
        }

        try {
            if (adminRepository.saveCourse(course)) {
                refreshCourses();
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
        Course selected = tblCourses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a course to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete Course");
        confirmation.setContentText("Delete course " + selected.getCourseCode() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            if (adminRepository.deleteCourseByCode(selected.getCourseCode())) {
                refreshCourses();
            } else {
                showInfo("No course was deleted.");
            }
        } catch (SQLException e) {
            showError("Failed to delete course.", e);
        }
    }

    @FXML
    void btnOnActionUpdateCourse(ActionEvent event) {
        Course selected = tblCourses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a course to edit.");
            return;
        }

        Course updated = openCourseForm(selected);
        if (updated == null) {
            return;
        }

        try {
            if (adminRepository.updateCourse(updated)) {
                refreshCourses();
                showInfo("Course updated successfully.");
            } else {
                showInfo("No course was updated.");
            }
        } catch (SQLException e) {
            showError("Failed to update course.", e);
        }
    }

    @FXML
    void btnOnActionEnrollStudent(ActionEvent event) {
        Course selected = tblCourses.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a course to enroll a student.");
            return;
        }

        try {
            List<UserRecord> students = adminRepository.findStudents();
            if (students.isEmpty()) {
                showInfo("No students are available for enrollment.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Enroll Student");
            dialog.setHeaderText("Enroll a student to " + selected.getCourseCode());

            ButtonType save = new ButtonType("Enroll", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

            ComboBox<UserRecord> cmbStudent = new ComboBox<>();
            cmbStudent.getItems().setAll(students);
            setupStudentComboBox(cmbStudent);

            ComboBox<String> cmbEnrollmentStatus = new ComboBox<>();
            cmbEnrollmentStatus.getItems().addAll("active", "completed", "dropped");
            cmbEnrollmentStatus.setValue("active");

            TextField txtCourseCode = new TextField(selected.getCourseCode());
            txtCourseCode.setEditable(false);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Course Code:"), 0, 0);
            grid.add(txtCourseCode, 1, 0);
            grid.add(new Label("Student:"), 0, 1);
            grid.add(cmbStudent, 1, 1);
            grid.add(new Label("Enrollment Status:"), 0, 2);
            grid.add(cmbEnrollmentStatus, 1, 2);

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(button -> button == save ? save : null);

            Button enrollButton = (Button) dialog.getDialogPane().lookupButton(save);
            enrollButton.addEventFilter(ActionEvent.ACTION, actionEvent -> {
                if (cmbStudent.getValue() == null) {
                    showInfo("Please select a student.");
                    actionEvent.consume();
                    return;
                }
                if (cmbEnrollmentStatus.getValue() == null || cmbEnrollmentStatus.getValue().isBlank()) {
                    showInfo("Please select an enrollment status.");
                    actionEvent.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != save) {
                return;
            }

            UserRecord student = cmbStudent.getValue();
            String status = cmbEnrollmentStatus.getValue();
            if (student == null || status == null || status.isBlank()) {
                return;
            }

            if (adminRepository.enrollStudentToCourse(
                    student.getRegistrationNo(),
                    selected.getCourseCode(),
                    status
            )) {
                showInfo("Student enrolled successfully.");
            } else {
                showInfo("No enrollment was saved.");
            }
        } catch (SQLException e) {
            showError("Failed to enroll student.", e);
        }
    }

    // Load courses based on the current filters.
    private void loadCourses(String department, String keyword) {
        try {
            tblCourses.getItems().setAll(adminRepository.findCoursesByFilters(department, keyword));
        } catch (SQLException e) {
            showError("Failed to load courses.", e);
        }
    }

    private void loadDepartmentFilter(String selectedValue) {
        try {
            cmbDeptFilter.getItems().clear();
            cmbDeptFilter.getItems().add("All");
            cmbDeptFilter.getItems().addAll(adminRepository.findAllCourseDepartments());
            cmbDeptFilter.setValue(cmbDeptFilter.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load department filters.", e);
        }
    }

    private void refreshCourses() {
        String selectedDepartment = cmbDeptFilter.getValue();
        loadDepartmentFilter(selectedDepartment == null ? "All" : selectedDepartment);
        applyFilters();
    }

    private void applyFilters() {
        String selectedDept = cmbDeptFilter.getValue();
        String department = "All".equals(selectedDept) ? null : selectedDept;
        loadCourses(department, text(txtSearchCourse));
    }

    // Open one form for adding and editing courses.
    private Course openCourseForm(Course existingCourse) {
        boolean edit = existingCourse != null;
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle(edit ? "Edit Course" : "Add New Course");
        dialog.setHeaderText(edit ? "Update selected course details." : "Enter new course details.");

        ButtonType save = new ButtonType(edit ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

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

        if (edit) {
            formController.setupForEdit(existingCourse);
        } else {
            formController.setupForCreate();
        }

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        final Course[] resultHolder = new Course[1];
        saveButton.addEventFilter(ActionEvent.ACTION, actionEvent -> {
            try {
                resultHolder[0] = formController.buildCourse();
            } catch (IllegalArgumentException e) {
                showInfo(e.getMessage());
                actionEvent.consume();
            }
        });

        dialog.setResultConverter(button -> button == save ? resultHolder[0] : null);
        Optional<Course> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void setupStudentComboBox(ComboBox<UserRecord> comboBox) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserRecord student) {
                return student == null ? "" : formatStudentLabel(student);
            }

            @Override
            public UserRecord fromString(String string) {
                return null;
            }
        });

        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(UserRecord item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatStudentLabel(item));
            }
        });
    }

    private String formatStudentLabel(UserRecord student) {
        return text(student.getRegistrationNo()) + " - "
                + (text(student.getFirstName()) + " " + text(student.getLastName())).trim();
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
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
