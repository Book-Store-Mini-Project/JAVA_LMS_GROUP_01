package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.TimetableRepository;
import com.example.java_lms_group_01.model.Timetable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TechnicalOfficerTimetableController {

    @FXML
    private ComboBox<String> cmbDepartment;
    @FXML
    private ComboBox<String> cmbDay;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Timetable> tblTimetables;
    @FXML
    private TableColumn<Timetable, String> colTimetableId;
    @FXML
    private TableColumn<Timetable, String> colDepartment;
    @FXML
    private TableColumn<Timetable, String> colLecturerId;
    @FXML
    private TableColumn<Timetable, String> colCourseCode;
    @FXML
    private TableColumn<Timetable, String> colAdminId;
    @FXML
    private TableColumn<Timetable, String> colDay;
    @FXML
    private TableColumn<Timetable, String> colStartTime;
    @FXML
    private TableColumn<Timetable, String> colEndTime;
    @FXML
    private TableColumn<Timetable, String> colSessionType;

    private final TimetableRepository timetableRepository = new TimetableRepository();

    @FXML
    public void initialize() {
        colTimetableId.setCellValueFactory(d -> d.getValue().timetableIdProperty());
        colDepartment.setCellValueFactory(d -> d.getValue().departmentProperty());
        colLecturerId.setCellValueFactory(d -> d.getValue().lecturerIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colAdminId.setCellValueFactory(d -> d.getValue().adminIdProperty());
        colDay.setCellValueFactory(d -> d.getValue().dayProperty());
        colStartTime.setCellValueFactory(d -> d.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(d -> d.getValue().endTimeProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());

        loadFilters();
        loadTimetables(null, null, null);
    }

    @FXML
    private void searchTimetables(ActionEvent event) {
        loadTimetables(cmbDepartment.getValue(), cmbDay.getValue(), txtSearch.getText());
    }

    @FXML
    private void refreshTimetables(ActionEvent event) {
        cmbDepartment.setValue(null);
        cmbDay.setValue(null);
        txtSearch.clear();
        loadTimetables(null, null, null);
    }

    private void loadFilters() {
        try {
            cmbDepartment.getItems().setAll(timetableRepository.findAllDepartments());
            cmbDay.getItems().setAll(timetableRepository.findAllDays());
        } catch (SQLException e) {
            showError("Failed to load timetable filters.", e);
        }
    }

    private void loadTimetables(String department, String day, String keyword) {
        try {
            List<Timetable> timetables = timetableRepository.findByFilters(department, day, keyword);
            tblTimetables.getItems().setAll(timetables);
        } catch (SQLException e) {
            showError("Failed to load timetables.", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

}
