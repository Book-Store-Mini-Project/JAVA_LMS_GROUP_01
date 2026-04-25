package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Repository.AdminRepository;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.session.LoggedInAdmin;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageTimetablesController implements Initializable {

    @FXML
    private ComboBox<String> cmbFilterDepartment;
    @FXML
    private ComboBox<String> cmbFilterSemester;
    @FXML
    private TableColumn<Timetable, String> colAcademicYear;
    @FXML
    private TableColumn<Timetable, String> colDepartmentId;
    @FXML
    private TableColumn<Timetable, String> colLecId;
    @FXML
    private TableColumn<Timetable, String> colCourseCode;
    @FXML
    private TableColumn<Timetable, String> colAdminId;
    @FXML
    private TableColumn<Timetable, String> colSemester;
    @FXML
    private TableColumn<Timetable, String> colStartTime;
    @FXML
    private TableColumn<Timetable, String> colEndTime;
    @FXML
    private TableColumn<Timetable, String> colTimetableId;
    @FXML
    private TableView<Timetable> tblTimetable;
    @FXML
    private TextField txtSearchAcademicYear;

    private final AdminRepository adminRepository = new AdminRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        loadDepartmentFilter("All");
        loadDayFilter("All");
        loadTimetables(null, null, null);

        cmbFilterDepartment.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbFilterSemester.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        txtSearchAcademicYear.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    // Set the timetable table columns.
    private void setupColumns() {
        colTimetableId.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getTimeTableId())));
        colDepartmentId.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getDepartment())));
        colLecId.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getLecId())));
        colCourseCode.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getCourseCode())));
        colAdminId.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getAdminId())));
        colSemester.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getDay())));
        colStartTime.setCellValueFactory(d -> new SimpleStringProperty(timeText(d.getValue().getStartTime())));
        colEndTime.setCellValueFactory(d -> new SimpleStringProperty(timeText(d.getValue().getEndTime())));
        colAcademicYear.setCellValueFactory(d -> new SimpleStringProperty(text(d.getValue().getSessionType())));
    }

    @FXML
    private void btnOnActionAddNewSchedule(ActionEvent event) {
        Timetable timetable = openTimetableDialog(null);
        if (timetable == null) {
            return;
        }

        try {
            if (adminRepository.saveTimetable(timetable)) {
                refreshFiltersAndTable();
                showInfo("Timetable added successfully.");
            } else {
                showInfo("No timetable was added.");
            }
        } catch (SQLException e) {
            showError("Failed to add timetable.", e);
        }
    }

    @FXML
    private void btnOnActionDeleteSchedule(ActionEvent event) {
        Timetable selected = tblTimetable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a timetable to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete Timetable");
        confirmation.setContentText("Delete timetable ID " + selected.getTimeTableId() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            if (adminRepository.deleteTimetableById(selected.getTimeTableId())) {
                refreshFiltersAndTable();
            } else {
                showInfo("No timetable was deleted.");
            }
        } catch (SQLException e) {
            showError("Failed to delete timetable.", e);
        }
    }

    @FXML
    private void btnOnActionUpdateSchedule(ActionEvent event) {
        Timetable selected = tblTimetable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a timetable to edit.");
            return;
        }

        Timetable updated = openTimetableDialog(selected);
        if (updated == null) {
            return;
        }

        try {
            if (adminRepository.updateTimetable(updated)) {
                refreshFiltersAndTable();
                showInfo("Timetable updated successfully.");
            } else {
                showInfo("No timetable was updated.");
            }
        } catch (SQLException e) {
            showError("Failed to update timetable.", e);
        }
    }

    // Load timetable rows using the current filter values.
    private void loadTimetables(String department, String day, String keyword) {
        try {
            List<Timetable> timetables = adminRepository.findTimetablesByFilters(department, day, keyword);
            tblTimetable.getItems().setAll(timetables);
        } catch (SQLException e) {
            showError("Failed to load timetables.", e);
        }
    }

    private void loadDepartmentFilter(String selectedValue) {
        try {
            cmbFilterDepartment.getItems().clear();
            cmbFilterDepartment.getItems().add("All");
            cmbFilterDepartment.getItems().addAll(adminRepository.findAllTimetableDepartments());
            cmbFilterDepartment.setValue(cmbFilterDepartment.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load department filters.", e);
        }
    }

    private void loadDayFilter(String selectedValue) {
        try {
            cmbFilterSemester.getItems().clear();
            cmbFilterSemester.getItems().add("All");
            cmbFilterSemester.getItems().addAll(adminRepository.findAllTimetableDays());
            cmbFilterSemester.setValue(cmbFilterSemester.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load day filters.", e);
        }
    }

    private void refreshFiltersAndTable() {
        String selectedDepartment = cmbFilterDepartment.getValue();
        String selectedDay = cmbFilterSemester.getValue();
        loadDepartmentFilter(selectedDepartment == null ? "All" : selectedDepartment);
        loadDayFilter(selectedDay == null ? "All" : selectedDay);
        applyFilters();
    }

    private void applyFilters() {
        String department = "All".equals(cmbFilterDepartment.getValue()) ? null : cmbFilterDepartment.getValue();
        String day = "All".equals(cmbFilterSemester.getValue()) ? null : cmbFilterSemester.getValue();
        loadTimetables(department, day, text(txtSearchAcademicYear));
    }

    // Open one dialog for adding and editing timetables.
    private Timetable openTimetableDialog(Timetable existing) {
        boolean edit = existing != null;
        String adminRegNo = text(LoggedInAdmin.getRegistrationNo());

        Dialog<Timetable> dialog = new Dialog<>();
        dialog.setTitle(edit ? "Edit Timetable" : "Add Timetable");
        dialog.setHeaderText(edit ? "Update selected timetable details." : "Enter new timetable details.");

        ButtonType save = new ButtonType(edit ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField txtId = new TextField(edit ? text(existing.getTimeTableId()) : "");
        TextField txtDepartment = new TextField(edit ? text(existing.getDepartment()) : "");
        TextField txtLecId = new TextField(edit ? text(existing.getLecId()) : "");
        TextField txtCourseCode = new TextField(edit ? text(existing.getCourseCode()) : "");
        TextField txtAdminId = new TextField(edit ? text(existing.getAdminId()) : adminRegNo);
        TextField txtDay = new TextField(edit ? text(existing.getDay()) : "");
        TextField txtStartTime = new TextField(edit ? timeText(existing.getStartTime()) : "");
        TextField txtEndTime = new TextField(edit ? timeText(existing.getEndTime()) : "");
        ComboBox<String> cmbSessionType = new ComboBox<>();
        cmbSessionType.getItems().addAll("theory", "practical","both");
        cmbSessionType.setValue(edit ? text(existing.getSessionType()) : null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Timetable ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Department:"), 0, 1);
        grid.add(txtDepartment, 1, 1);
        grid.add(new Label("Lecturer Reg No (optional):"), 0, 2);
        grid.add(txtLecId, 1, 2);
        grid.add(new Label("Course Code (optional):"), 0, 3);
        grid.add(txtCourseCode, 1, 3);
        grid.add(new Label("Admin Reg No (optional):"), 0, 4);
        grid.add(txtAdminId, 1, 4);
        grid.add(new Label("Day:"), 0, 5);
        grid.add(txtDay, 1, 5);
        grid.add(new Label("Start Time (HH:mm):"), 0, 6);
        grid.add(txtStartTime, 1, 6);
        grid.add(new Label("End Time (HH:mm):"), 0, 7);
        grid.add(txtEndTime, 1, 7);
        grid.add(new Label("Session Type:"), 0, 8);
        grid.add(cmbSessionType, 1, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != save) {
                return null;
            }

            String timetableId = text(txtId);
            String department = text(txtDepartment);
            String day = text(txtDay);
            String sessionType = text(cmbSessionType.getValue());
            String lecId = text(txtLecId);
            String courseCode = text(txtCourseCode);
            String adminId = text(txtAdminId);

            if (adminId.isBlank()) {
                adminId = adminRegNo;
            }

            if (!validateBasicFields(timetableId, department, day, sessionType)) {
                return null;
            }
            if (!validateOptionalCodes(lecId, courseCode, adminId)) {
                return null;
            }

            LocalTime startTime;
            LocalTime endTime;
            try {
                startTime = parseOptionalTime(text(txtStartTime), "Start Time");
                endTime = parseOptionalTime(text(txtEndTime), "End Time");
            } catch (IllegalArgumentException e) {
                showInfo(e.getMessage());
                return null;
            }

            if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
                showInfo("End Time must be later than Start Time.");
                return null;
            }

            return new Timetable(
                    timetableId,
                    department,
                    emptyToNull(lecId),
                    emptyToNull(courseCode),
                    emptyToNull(adminId),
                    day,
                    startTime,
                    endTime,
                    sessionType
            );
        });

        Optional<Timetable> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean validateBasicFields(String timetableId, String department, String day, String sessionType) {
        if (timetableId.isBlank()) {
            showInfo("Timetable ID is required.");
            return false;
        }
        if (timetableId.length() > 5) {
            showInfo("Timetable ID must be at most 5 characters.");
            return false;
        }
        if (department.isBlank()) {
            showInfo("Department is required.");
            return false;
        }
        if (department.length() > 3) {
            showInfo("Department must be at most 3 characters.");
            return false;
        }
        if (day.isBlank()) {
            showInfo("Day is required.");
            return false;
        }
        if (day.length() > 10) {
            showInfo("Day must be at most 10 characters.");
            return false;
        }
        if (sessionType.isBlank()) {
            showInfo("Session type is required.");
            return false;
        }
        if (!"theory".equals(sessionType) && !"practical".equals(sessionType)) {
            showInfo("Session type must be theory or practical.");
            return false;
        }
        return true;
    }

    private boolean validateOptionalCodes(String lecId, String courseCode, String adminId) {
        if (!lecId.isBlank() && lecId.length() > 10) {
            showInfo("Lecturer Reg No must be at most 10 characters.");
            return false;
        }
        if (!courseCode.isBlank() && courseCode.length() > 10) {
            showInfo("Course Code must be at most 10 characters.");
            return false;
        }
        if (!adminId.isBlank() && adminId.length() > 10) {
            showInfo("Admin Reg No must be at most 10 characters.");
            return false;
        }
        return true;
    }

    private LocalTime parseOptionalTime(String value, String field) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " must be in HH:mm:ss format.");
        }
    }

    private String emptyToNull(String value) {
        return value.isBlank() ? null : value;
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String timeText(LocalTime time) {
        return time == null ? "" : time.toString();
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
