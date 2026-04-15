package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.Repository.LecturerRepository;
import com.example.java_lms_group_01.model.Material;
import com.example.java_lms_group_01.model.request.MaterialRequest;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
/**
 * Lets a lecturer manage course materials for the courses assigned to that lecturer.
 */
public class LecturerMaterialsController {

    @FXML
    private TextField txtCourseCode;
    @FXML
    private TextField txtMaterialName;
    @FXML
    private TextField txtPath;
    @FXML
    private ComboBox<String> cmbMaterialType;
    @FXML
    private TableView<Material> tblMaterials;
    @FXML
    private TableColumn<Material, String> colMaterialId;
    @FXML
    private TableColumn<Material, String> colCourseCode;
    @FXML
    private TableColumn<Material, String> colMaterialName;
    @FXML
    private TableColumn<Material, String> colPath;
    @FXML
    private TableColumn<Material, String> colType;

    private final LecturerRepository lecturerRepository = new LecturerRepository();

    @FXML
    public void initialize() {
        cmbMaterialType.setItems(FXCollections.observableArrayList("pdf", "video", "ppt", "other"));
        colMaterialId.setCellValueFactory(d -> d.getValue().materialIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colMaterialName.setCellValueFactory(d -> d.getValue().nameProperty());
        colPath.setCellValueFactory(d -> d.getValue().pathProperty());
        colType.setCellValueFactory(d -> d.getValue().typeProperty());
        loadMaterials();

        tblMaterials.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row == null) {
                return;
            }
            txtCourseCode.setText(row.getCourseCode());
            txtMaterialName.setText(row.getName());
            txtPath.setText(row.getPath());
            cmbMaterialType.setValue(row.getType());
        });
    }

    @FXML
    private void addMaterial() {
        if (!validForm()) {
            return;
        }
        try {
            int affectedRows = lecturerRepository.addMaterial(currentLecturer(), buildMutation());
            if (affectedRows == 0) {
                showWarn("You can add materials only for courses assigned to you.");
                return;
            }
            loadMaterials();
            clearForm();
        } catch (Exception e) {
            showError("Failed to add material.", e);
        }
    }

    @FXML
    private void updateMaterial() {
        Material selected = tblMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a material to update.");
            return;
        }
        if (!validForm()) {
            return;
        }
        try {
            int affectedRows = lecturerRepository.updateMaterial(currentLecturer(), Integer.parseInt(selected.getMaterialId()), buildMutation());
            if (affectedRows == 0) {
                showWarn("You can update only materials for your own courses.");
                return;
            }
            loadMaterials();
        } catch (Exception e) {
            showError("Failed to update material.", e);
        }
    }

    @FXML
    private void deleteMaterial() {
        Material selected = tblMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a material to delete.");
            return;
        }
        try {
            int affectedRows = lecturerRepository.deleteMaterial(currentLecturer(), Integer.parseInt(selected.getMaterialId()));
            if (affectedRows == 0) {
                showWarn("You can delete only materials for your own courses.");
                return;
            }
            loadMaterials();
            clearForm();
        } catch (Exception e) {
            showError("Failed to delete material.", e);
        }
    }

    @FXML
    private void clearForm() {
        txtCourseCode.clear();
        txtMaterialName.clear();
        txtPath.clear();
        cmbMaterialType.setValue(null);
        tblMaterials.getSelectionModel().clearSelection();
    }

    private void loadMaterials() {
        try {
            tblMaterials.getItems().setAll(
                    lecturerRepository.findMaterialsByLecturer(currentLecturer(), null)
            );
        } catch (SQLException e) {
            showError("Failed to load materials.", e);
        }
    }

    private boolean validForm() {
        if (value(txtCourseCode).isBlank() || value(txtMaterialName).isBlank() || value(txtPath).isBlank() || cmbMaterialType.getValue() == null) {
            showWarn("Fill all required fields.");
            return false;
        }
        return true;
    }

    private String currentLecturer() {
        String reg = LecturerContext.getRegistrationNo();
        return reg == null ? "" : reg.trim();
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private void showWarn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private MaterialRequest buildMutation() {
        return new MaterialRequest(
                value(txtCourseCode),
                value(txtMaterialName),
                value(txtPath),
                cmbMaterialType.getValue()
        );
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

}
