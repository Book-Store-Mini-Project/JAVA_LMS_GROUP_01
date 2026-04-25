package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Medical;
import com.example.java_lms_group_01.session.LoggedInStudent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

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
        setupColumns();
        loadMedical();
    }

    private void setupColumns() {
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colSubmissionDate.setCellValueFactory(d -> d.getValue().submissionDateProperty());
        colDescription.setCellValueFactory(d -> d.getValue().descriptionProperty());
        colSessionType.setCellValueFactory(d -> d.getValue().sessionTypeProperty());
        colApprovalStatus.setCellValueFactory(d -> d.getValue().approvalStatusProperty());
    }

    private void loadMedical() {
        String regNo = currentStudent();
        if (regNo.isBlank()) {
            return;
        }

        try {
            tblMedical.getItems().setAll(studentRepository.findMedicalByStudent(regNo));
        } catch (SQLException e) {
            showError("Failed to load medical details.", e);
        }
    }

    private String currentStudent() {
        String reg = LoggedInStudent.getRegistrationNo();
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
