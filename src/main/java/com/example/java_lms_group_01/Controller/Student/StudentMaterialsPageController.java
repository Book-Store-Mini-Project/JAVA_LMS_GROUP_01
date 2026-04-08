package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.StudentContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class StudentMaterialsPageController {

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
        colMaterialId.setCellValueFactory(d -> d.getValue().materialIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colMaterialName.setCellValueFactory(d -> d.getValue().nameProperty());
        colPath.setCellValueFactory(d -> d.getValue().pathProperty());
        colType.setCellValueFactory(d -> d.getValue().typeProperty());
        tblMaterials.setRowFactory(table -> {
            TableRow<MaterialRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    downloadMaterial(row.getItem());
                }
            });
            return row;
        });
        loadMaterials(null);
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
    private void downloadSelectedMaterial() {
        MaterialRow selected = tblMaterials.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select a material first.");
            return;
        }
        downloadMaterial(selected);
    }

    private void loadMaterials(String keyword) {
        String studentReg = StudentContext.getRegistrationNo();
        if (studentReg == null || studentReg.isBlank()) {
            return;
        }

        String safeKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT DISTINCT lm.material_id, lm.courseCode, lm.name, lm.path, lm.material_type
                FROM lecture_materials lm
                INNER JOIN enrollment e ON e.courseCode = lm.courseCode
                WHERE e.studentReg = ?
                  AND (? = '' OR lm.courseCode LIKE ? OR lm.name LIKE ?)
                ORDER BY lm.courseCode, lm.material_id DESC
                """;

        List<MaterialRow> rows = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance().getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                String pattern = "%" + safeKeyword + "%";
                statement.setString(1, studentReg);
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
            showError("Failed to load course materials.", e);
        }
    }

    private void downloadMaterial(MaterialRow material) {
        String rawPath = material.getPath();
        if (rawPath.isBlank()) {
            showWarning("This material does not have a valid file path or URL.");
            return;
        }

        try {
            if (isWebUrl(rawPath)) {
                Path downloadedFile = downloadFromUrl(material);
                showInfo("Downloaded to:\n" + downloadedFile);
                openFile(downloadedFile);
                return;
            }

            Path localFile = Path.of(rawPath);
            if (!Files.exists(localFile)) {
                showWarning("File not found:\n" + rawPath);
                return;
            }
            openFile(localFile);
        } catch (Exception e) {
            showError("Failed to open or download the selected material.", e);
        }
    }

    private Path downloadFromUrl(MaterialRow material) throws Exception {
        Path downloadsDir = Path.of(System.getProperty("user.home"), "Downloads");
        Files.createDirectories(downloadsDir);

        String fileName = buildFileName(material);
        Path targetFile = uniquePath(downloadsDir.resolve(fileName));

        try (InputStream inputStream = new URL(material.getPath()).openStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return targetFile;
    }

    private Path uniquePath(Path file) {
        if (!Files.exists(file)) {
            return file;
        }

        String fileName = file.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex >= 0 ? fileName.substring(dotIndex) : "";

        int counter = 1;
        Path parent = file.getParent();
        Path candidate = file;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(baseName + "_" + counter + extension);
            counter++;
        }
        return candidate;
    }

    private String buildFileName(MaterialRow material) {
        String sanitizedName = material.getName().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (sanitizedName.isBlank()) {
            sanitizedName = "material_" + material.getMaterialId();
        }

        String extension = extensionFromPath(material.getPath());
        if (extension.isBlank()) {
            extension = "." + material.getType().toLowerCase(Locale.ROOT);
        }
        return sanitizedName + extension;
    }

    private String extensionFromPath(String value) {
        try {
            String pathPart = isWebUrl(value) ? URI.create(value).getPath() : value;
            if (pathPart == null) {
                return "";
            }
            int dotIndex = pathPart.lastIndexOf('.');
            if (dotIndex < 0 || dotIndex == pathPart.length() - 1) {
                return "";
            }
            String extension = pathPart.substring(dotIndex);
            return extension.length() <= 10 ? extension : "";
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isWebUrl(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private void openFile(Path file) throws Exception {
        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException("Desktop operations are not supported on this system.");
        }
        Desktop.getDesktop().open(file.toFile());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Material Access");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Material Downloaded");
        alert.setHeaderText(null);
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
        public String getName() { return name.get(); }
        public String getPath() { return path.get(); }
        public String getType() { return type.get(); }
    }
}
