package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Timetable;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the timetable for the logged-in lecturer.
 */
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
        colDepartment.setCellValueFactory(d -> d.getValue().departmentProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colDay.setCellValueFactory(d -> d.getValue().dayProperty());
        colStartTime.setCellValueFactory(d -> d.getValue().startTimeProperty());
        colEndTime.setCellValueFactory(d -> d.getValue().endTimeProperty());
        colSession.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        loadTimetable();
    }

    private void loadTimetable() {
        String lecturerReg = LecturerContext.getRegistrationNo();
        if (lecturerReg == null || lecturerReg.isBlank()) {
            return;
        }

        try {
            List<LecturerRepository.TimetableRecord> recordList =
                    lecturerRepository.findTimetableByLecturer(lecturerReg, null);
            List<Timetable> rows = new ArrayList<>();
            for (LecturerRepository.TimetableRecord record : recordList) {
                rows.add(new Timetable(
                        record.getTimetableId(),
                        record.getDepartment(),
                        record.getLecId(),
                        record.getCourseCode(),
                        record.getAdminId(),
                        record.getDay(),
                        parseTime(record.getStartTime()),
                        parseTime(record.getEndTime()),
                        record.getSessionType()
                ));
            }
            tblTimetable.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load lecturer timetable.", e);
        }
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    private java.time.LocalTime parseTime(String value) {
        return value == null || value.isBlank() ? null : java.time.LocalTime.parse(value);
    }

}
