package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows medical submissions that belong to the logged-in student.
 */
public class StudentMedicalPageController {

    @FXML
    private TableView<Medical> tblMedical;
    @FXML
    private TableColumn<Medical, String> colCourseCode;
    @FXML
    private TableColumn<Medical, String> colSubmissionDate;
    @FXML
    private TableColumn<Medical, String> colDescription;
    @FXML
    private TableColumn<Medical, String> colSessionType;
    @FXML
    private TableColumn<Medical, String> colApprovalStatus;

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colDescription.setCellValueFactory(d -> d.getValue().descriptionProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colApprovalStatus.setCellValueFactory(d -> d.getValue().approvalStatusProperty());
        loadMedical();
    }

    private void loadMedical() {
        String regNo = StudentContext.getRegistrationNo();
        if (regNo == null || regNo.isBlank()) {
            return;
        }

        try {
            List<StudentRepository.MedicalRecord> recordList = studentRepository.findMedicalByStudent(regNo);
            List<Medical> rows = new ArrayList<>();
            for (StudentRepository.MedicalRecord record : recordList) {
                rows.add(new Medical(
                        record.getMedicalId(),
                        record.getStudentReg(),
                        record.getCourseCode(),
                        record.getSubmissionDate(),
                        record.getDescription(),
                        record.getSessionType(),
                        record.getAttendanceId(),
                        record.getApprovalStatus(),
                        record.getTechOfficerReg()
                ));
            }
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
