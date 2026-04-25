package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Material;
import com.example.java_lms_group_01.session.LoggedInStudent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class StudentMaterialsPageController {

    // --- FXML UI COMPONENTS ---
    @FXML
    private TableView<Material> tblMaterials;

    @FXML
    private TableColumn<Material, String> colCourseCode;

    @FXML
    private TableColumn<Material, String> colMaterialName;

    @FXML
    private TableColumn<Material, String> colType;

    // The bridge to our database
    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        // Step 1: Tell the columns which data to show
        configureTable();

        // Step 2: Fill the table with data from the database
        loadMaterialsData();
    }

    private void configureTable() {
        // Standard way to link model fields to table columns
        colCourseCode.setCellValueFactory(data -> {
            return data.getValue().courseCodeProperty();
        });

        colMaterialName.setCellValueFactory(data -> {
            return data.getValue().nameProperty();
        });

        colType.setCellValueFactory(data -> {
            return data.getValue().typeProperty();
        });
    }

    private void loadMaterialsData() {
        // Get the logged-in student's ID
        String studentId = getLoggedStudentId();

        if (studentId.equals("")) {
            // If no student is logged in, don't do anything
            return;
        }

        try {
            // Get the list of materials from the repository
            List<Material> materialsList = studentRepository.findMaterialsByStudent(studentId, null);

            // Put the list into the table
            tblMaterials.getItems().setAll(materialsList);

        } catch (SQLException e) {
            showErrorMessage("Database Error", "Could not load the materials.");
        }
    }

    @FXML
    private void downloadSelectedMaterial() {
        // Get the item the user clicked on
        Material selected = tblMaterials.getSelectionModel().getSelectedItem();

        // Check if they actually selected something
        if (selected == null) {
            showWarningMessage("No Selection", "Please select a row from the table first.");
        } else {
            // Try to open the file
            openMaterialFile(selected);
        }
    }

    private void openMaterialFile(Material material) {
        String filePath = material.getPath();

        // Safety check: is the path empty?
        if (filePath == null || filePath.isEmpty()) {
            showWarningMessage("Missing File", "This material does not have a valid file path.");
            return;
        }

        try {
            // Create a File object from the path string
            File file = new File(filePath);

            // Check if the file actually exists on the hard drive
            if (file.exists()) {
                // Use Desktop to open the file with the default Windows/System app
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    showWarningMessage("Not Supported", "Your system cannot open files this way.");
                }
            } else {
                showWarningMessage("File Not Found", "The file does not exist at: " + filePath);
            }
        } catch (Exception e) {
            showErrorMessage("Error", "Could not open the file.");
        }
    }

    @FXML
    private void refreshTable() {
        // Simple refresh button logic
        loadMaterialsData();
    }

    private String getLoggedStudentId() {
        // current student registration number
        String reg = LoggedInStudent.getRegistrationNo();

        if (reg == null) {
            return "";
        } else {
            return reg.trim();
        }
    }

    private void showWarningMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}