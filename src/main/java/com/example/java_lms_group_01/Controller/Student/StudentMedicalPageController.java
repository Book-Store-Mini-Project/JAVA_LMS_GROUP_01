package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class StudentMedicalPageController {

    @FXML
    private TableView<Medical> tblMedical;
    @FXML
    private TableColumn<Medical, String> colMedicalId;
    @FXML
    private TableColumn<Medical, String> colStudentReg;
    @FXML
    private TableColumn<Medical, String> colCourseCode;
    @FXML
    private TableColumn<Medical, String> colSubmissionDate;
    @FXML
    private TableColumn<Medical, String> colDescription;
    @FXML
    private TableColumn<Medical, String> colSessionType;
    @FXML
    private TableColumn<Medical, String> colAttendanceId;
    @FXML
    private TableColumn<Medical, String> colApprovalStatus;
    @FXML
    private TableColumn<Medical, String> colTechOfficerReg;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colMedicalId.setCellValueFactory(d -> d.getValue().medicalIdProperty());
        colStudentReg.setCellValueFactory(d -> d.getValue().studentRegProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colDescription.setCellValueFactory(d -> d.getValue().descriptionProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colAttendanceId.setCellValueFactory(d -> d.getValue().attendanceIdProperty());
        colApprovalStatus.setCellValueFactory(d -> d.getValue().approvalStatusProperty());
        colTechOfficerReg.setCellValueFactory(d -> d.getValue().techOfficerRegProperty());
        loadMedical();
    }

    private void loadMedical() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            var rows = studentRepository.findMedicalByStudent(regNo).stream()
                    .map(r -> new Medical(r.medicalId(), r.studentReg(), r.courseCode(), r.submissionDate(), r.description(), r.sessionType(), r.attendanceId(), r.approvalStatus(), r.techOfficerReg()))
                    .toList();
            tblMedical.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load medical details.", e);
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
