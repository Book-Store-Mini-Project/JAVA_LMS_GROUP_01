package com.example.java_lms_group_01.Controller.AdminDashboard;

import com.example.java_lms_group_01.model.users.Admin;
import com.example.java_lms_group_01.model.Timetable;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

    private final Admin admin = new Admin();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureColumns();
        loadDepartmentFilter("All");
        loadDayFilter("All");
        loadTimetables(null, null, null);

        cmbFilterDepartment.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        cmbFilterSemester.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        txtSearchAcademicYear.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void configureColumns() {
        colTimetableId.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getTimeTableId())));
        colDepartmentId.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getDepartment())));
        colLecId.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getLecId())));
        colCourseCode.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getCourseCode())));
        colAdminId.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getAdminId())));
        colSemester.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getDay())));
        colStartTime.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getStartTime())));
        colEndTime.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getEndTime())));
        colAcademicYear.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getSessionType())));
    }

    private void loadDepartmentFilter(String selectedValue) {
        try {
            cmbFilterDepartment.getItems().clear();
            cmbFilterDepartment.getItems().add("All");
            cmbFilterDepartment.getItems().addAll(admin.getTimetableDepartments());
            cmbFilterDepartment.setValue(cmbFilterDepartment.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load department filters.", e);
        }
    }

    private void loadDayFilter(String selectedValue) {
        try {
            cmbFilterSemester.getItems().clear();
            cmbFilterSemester.getItems().add("All");
            cmbFilterSemester.getItems().addAll(admin.getTimetableDays());
            cmbFilterSemester.setValue(cmbFilterSemester.getItems().contains(selectedValue) ? selectedValue : "All");
        } catch (SQLException e) {
            showError("Failed to load day filters.", e);
        }
    }

    private void applyFilters() {
        String department = "All".equals(cmbFilterDepartment.getValue()) ? null : cmbFilterDepartment.getValue();
        String day = "All".equals(cmbFilterSemester.getValue()) ? null : cmbFilterSemester.getValue();
        loadTimetables(department, day, txtSearchAcademicYear.getText());
    }

    private void loadTimetables(String department, String day, String keyword) {
        try {
            List<Timetable> timetables = admin.getTimetables(department, day, keyword);
            tblTimetable.getItems().setAll(timetables);
        } catch (SQLException e) {
            showError("Failed to load timetables.", e);
        }
    }

    @FXML
    void btnOnActionAddNewSchedule(ActionEvent event) {
        Timetable timetable = showTimetableDialog(null);
        if (timetable == null) {
            return;
        }

        try {
            boolean saved = admin.addTimetable(timetable);
            if (saved) {
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
    void btnOnActionDeleteSchedule(ActionEvent event) {
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
            boolean deleted = admin.deleteTimetable(selected.getTimeTableId());
            if (deleted) {
                refreshFiltersAndTable();
                showInfo("Timetable deleted successfully.");
            } else {
                showInfo("No timetable was deleted.");
            }
        } catch (SQLException e) {
            showError("Failed to delete timetable.", e);
        }
    }

    @FXML
    void btnOnActionUpdateSchedule(ActionEvent event) {
        Timetable selected = tblTimetable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a timetable to edit.");
            return;
        }

        Timetable updated = showTimetableDialog(selected);
        if (updated == null) {
            return;
        }

        try {
            boolean changed = admin.updateTimetable(updated);
            if (changed) {
                refreshFiltersAndTable();
                showInfo("Timetable updated successfully.");
            } else {
                showInfo("No timetable was updated.");
            }
        } catch (SQLException e) {
            showError("Failed to update timetable.", e);
        }
    }

    private Timetable showTimetableDialog(Timetable existing) {
        boolean editMode = existing != null;

        Dialog<Timetable> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "Edit Timetable" : "Add Timetable");
        dialog.setHeaderText(editMode ? "Update selected timetable details." : "Enter new timetable details.");

        ButtonType saveButtonType = new ButtonType(editMode ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField txtId = new TextField();
        TextField txtDepartment = new TextField();
        TextField txtLecId = new TextField();
        TextField txtCourseCode = new TextField();
        TextField txtAdminId = new TextField();
        TextField txtDay = new TextField();
        TextField txtStartTime = new TextField();
        TextField txtEndTime = new TextField();
        ComboBox<String> cmbSessionType = new ComboBox<>();
        cmbSessionType.getItems().addAll("theory", "practical");

        if (editMode) {
            txtId.setText(value(existing.getTimeTableId()));
            txtId.setDisable(true);
            txtDepartment.setText(value(existing.getDepartment()));
            txtLecId.setText(value(existing.getLecId()));
            txtCourseCode.setText(value(existing.getCourseCode()));
            txtAdminId.setText(value(existing.getAdminId()));
            txtDay.setText(value(existing.getDay()));
            txtStartTime.setText(value(existing.getStartTime()));
            txtEndTime.setText(value(existing.getEndTime()));
            cmbSessionType.setValue(value(existing.getSessionType()));
        }

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
            if (button != saveButtonType) {
                return null;
            }

            String timetableId = value(txtId);
            String department = value(txtDepartment);
            String day = value(txtDay);
            String sessionType = cmbSessionType.getValue();

            if (timetableId.isBlank()) {
                showInfo("Timetable ID is required.");
                return null;
            }
            if (timetableId.length() > 5) {
                showInfo("Timetable ID must be at most 5 characters.");
                return null;
            }
            if (department.isBlank()) {
                showInfo("Department is required.");
                return null;
            }
            if (department.length() > 3) {
                showInfo("Department must be at most 3 characters.");
                return null;
            }
            if (day.isBlank()) {
                showInfo("Day is required.");
                return null;
            }
            if (day.length() > 10) {
                showInfo("Day must be at most 10 characters.");
                return null;
            }
            if (sessionType == null || sessionType.isBlank()) {
                showInfo("Session type is required.");
                return null;
            }
            if (!"theory".equals(sessionType) && !"practical".equals(sessionType)) {
                showInfo("Session type must be theory or practical.");
                return null;
            }

            String lecId = value(txtLecId);
            String courseCode = value(txtCourseCode);
            String adminId = value(txtAdminId);
            if (!lecId.isBlank() && lecId.length() > 10) {
                showInfo("Lecturer Reg No must be at most 10 characters.");
                return null;
            }
            if (!courseCode.isBlank() && courseCode.length() > 10) {
                showInfo("Course Code must be at most 10 characters.");
                return null;
            }
            if (!adminId.isBlank() && adminId.length() > 10) {
                showInfo("Admin Reg No must be at most 10 characters.");
                return null;
            }

            LocalTime startTime;
            LocalTime endTime;
            try {
                startTime = parseOptionalTime(value(txtStartTime), "Start Time");
                endTime = parseOptionalTime(value(txtEndTime), "End Time");
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

    private LocalTime parseOptionalTime(String value, String field) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " must be in HH:mm format.");
        }
    }

    private String emptyToNull(String value) {
        return value.isBlank() ? null : value;
    }

    private void refreshFiltersAndTable() {
        String selectedDepartment = cmbFilterDepartment.getValue();
        String selectedDay = cmbFilterSemester.getValue();
        loadDepartmentFilter(selectedDepartment);
        loadDayFilter(selectedDay);
        applyFilters();
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String value(LocalTime time) {
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
