package com.example.java_lms_group_01.Controller.Lecturer;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.LecturerContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private TextField txtSearch;
    @FXML
    private TableView<MaterialRow> tblMaterials;
    @FXML
    private TableColumn<MaterialRow, String> colMaterialId;
    @FXML
    private TableColumn<MaterialRow, String> colCourseCode;
    @FXML
    private TableColumn<MaterialRow, String> colMaterialName;
    @FXML
    private TableColumn<MaterialRow, String> colPath;
    @FXML
    private TableColumn<MaterialRow, String> colType;

    @FXML
    public void initialize() {
        cmbMaterialType.setItems(FXCollections.observableArrayList("pdf", "video", "ppt", "other"));
        colMaterialId.setCellValueFactory(d -> d.getValue().materialIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colMaterialName.setCellValueFactory(d -> d.getValue().nameProperty());
        colPath.setCellValueFactory(d -> d.getValue().pathProperty());
        colType.setCellValueFactory(d -> d.getValue().typeProperty());
        loadMaterials(null);

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
        String sql = """
                INSERT INTO lecture_materials (courseCode, name, path, material_type)
                SELECT ?, ?, ?, ?
                WHERE EXISTS (
                    SELECT 1
                    FROM course
                    WHERE courseCode = ? AND lecturerRegistrationNo = ?
                )
                """;

        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value(txtCourseCode));
                statement.setString(2, value(txtMaterialName));
                statement.setString(3, value(txtPath));
                statement.setString(4, cmbMaterialType.getValue());
                statement.setString(5, value(txtCourseCode));
                statement.setString(6, currentLecturer());
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showWarn("You can add materials only for courses assigned to you.");
                    return;
                }
            }
            loadMaterials(txtSearch.getText());
            clearForm();
        } catch (Exception e) {
            showError("Failed to add material.", e);
        }
    }

    @FXML
    private void updateMaterial() {
        MaterialRow selected = tblMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a material to update.");
            return;
        }
        if (!validForm()) {
            return;
        }
        String sql = """
                UPDATE lecture_materials
                SET courseCode = ?, name = ?, path = ?, material_type = ?
                WHERE material_id = ?
                  AND courseCode IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                  AND ? IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                """;
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, value(txtCourseCode));
                statement.setString(2, value(txtMaterialName));
                statement.setString(3, value(txtPath));
                statement.setString(4, cmbMaterialType.getValue());
                statement.setInt(5, Integer.parseInt(selected.getMaterialId()));
                statement.setString(6, currentLecturer());
                statement.setString(7, value(txtCourseCode));
                statement.setString(8, currentLecturer());
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showWarn("You can update only materials for your own courses.");
                    return;
                }
            }
            loadMaterials(txtSearch.getText());
        } catch (Exception e) {
            showError("Failed to update material.", e);
        }
    }

    @FXML
    private void deleteMaterial() {
        MaterialRow selected = tblMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Select a material to delete.");
            return;
        }
        String sql = """
                DELETE FROM lecture_materials
                WHERE material_id = ?
                  AND courseCode IN (
                      SELECT courseCode
                      FROM course
                      WHERE lecturerRegistrationNo = ?
                  )
                """;
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Integer.parseInt(selected.getMaterialId()));
                statement.setString(2, currentLecturer());
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    showWarn("You can delete only materials for your own courses.");
                    return;
                }
            }
            loadMaterials(txtSearch.getText());
            clearForm();
        } catch (Exception e) {
            showError("Failed to delete material.", e);
        }
    }

    @FXML
    private void searchMaterials() {
        loadMaterials(txtSearch.getText());
    }

    @FXML
    private void refreshMaterials() {
        txtSearch.clear();
        loadMaterials(null);
    }

    @FXML
    private void clearForm() {
        txtCourseCode.clear();
        txtMaterialName.clear();
        txtPath.clear();
        cmbMaterialType.setValue(null);
        tblMaterials.getSelectionModel().clearSelection();
    }

    private void loadMaterials(String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT material_id, courseCode, name, path, material_type
                FROM lecture_materials
                WHERE courseCode IN (
                    SELECT courseCode
                    FROM course
                    WHERE lecturerRegistrationNo = ?
                )
                AND (? = '' OR courseCode LIKE ? OR name LIKE ?)
                ORDER BY material_id DESC
                """;

        List<MaterialRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + safeKeyword + "%";
                statement.setString(1, currentLecturer());
                statement.setString(2, safeKeyword);
                statement.setString(3, pattern);
                statement.setString(4, pattern);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new MaterialRow(
                                String.valueOf(rs.getInt("material_id")),
                                safe(rs.getString("courseCode")),
                                safe(rs.getString("name")),
                                safe(rs.getString("path")),
                                safe(rs.getString("material_type"))
                        ));
                    }
                }
            }
            tblMaterials.getItems().setAll(rows);
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showWarn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }

    public static class MaterialRow {
        private final SimpleStringProperty materialId;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty name;
        private final SimpleStringProperty path;
        private final SimpleStringProperty type;

        public MaterialRow(String materialId, String courseCode, String name, String path, String type) {
            this.materialId = new SimpleStringProperty(materialId);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.name = new SimpleStringProperty(name);
            this.path = new SimpleStringProperty(path);
            this.type = new SimpleStringProperty(type);
        }

        public SimpleStringProperty materialIdProperty() { return materialId; }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty pathProperty() { return path; }
        public SimpleStringProperty typeProperty() { return type; }

        public String getMaterialId() { return materialId.get(); }
        public String getCourseCode() { return courseCode.get(); }
        public String getName() { return name.get(); }
        public String getPath() { return path.get(); }
        public String getType() { return type.get(); }
    }
}
