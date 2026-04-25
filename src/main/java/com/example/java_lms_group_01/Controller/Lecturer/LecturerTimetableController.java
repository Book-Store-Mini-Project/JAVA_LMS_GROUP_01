package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.session.LoggedInLecture;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class LecturerTimetableController {

    @FXML
    private TableView<Timetable> tblTimetable;
    @FXML
    private TableColumn<Timetable, String> colDepartment;
    @FXML
    private TableColumn<Timetable, String> colCourseCode;
    @FXML
    private TableColumn<Timetable, String> colDay;
    @FXML
    private TableColumn<Timetable, String> colStartTime;
    @FXML
    private TableColumn<Timetable, String> colEndTime;
    @FXML
    private TableColumn<Timetable, String> colSession;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        setupColumns();
        loadTimetable();
    }

    // Set the timetable table columns.
    private void setupColumns() {
        colDepartment.setCellValueFactory(d -> d.getValue().departmentProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colDay.setCellValueFactory(d -> d.getValue().dayProperty());
        colStartTime.setCellValueFactory(d -> d.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(d -> d.getValue().endTimeProperty());
        colSession.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
    }

    // Load the timetable for the logged-in lecturer.
    private void loadTimetable() {
        String lecturerReg = currentLecturer();
        if (lecturerReg.isBlank()) {
            return;
        }

        try {
            tblTimetable.getItems().setAll(
                    lecturerRepository.findTimetableByLecturer(lecturerReg, null)
            );
        } catch (SQLException e) {
            showError("Failed to load lecturer timetable.", e);
        }
    }

    private String currentLecturer() {
        String reg = LoggedInLecture.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
