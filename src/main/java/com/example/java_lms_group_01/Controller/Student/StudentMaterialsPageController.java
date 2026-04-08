package com.example.java_lms_group_01.Controller.Student;

import com.example.java_lms_group_01.Repository.StudentRepository;
import com.example.java_lms_group_01.model.Material;
import com.example.java_lms_group_01.util.StudentContext;
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
import java.sql.SQLException;
import java.util.Locale;

public class StudentMaterialsPageController {

    @FXML
    private TextField txtSearch;
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

    private final StudentRepository studentRepository = new StudentRepository();

    @FXML
    public void initialize() {
        colMaterialId.setCellValueFactory(d -> d.getValue().materialIdProperty());
        colCourseCode.setCellValueFactory(d -> d.getValue().courseCodeProperty());
        colMaterialName.setCellValueFactory(d -> d.getValue().nameProperty());
        colPath.setCellValueFactory(d -> d.getValue().pathProperty());
        colType.setCellValueFactory(d -> d.getValue().typeProperty());
        tblMaterials.setRowFactory(table -> {
            TableRow<Material> row = new TableRow<>();
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
        Material selected = tblMaterials.getSelectionModel().getSelectedItem();
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

        try {
            var rows = studentRepository.findMaterialsByStudent(studentReg, keyword).stream()
                    .map(r -> new Material(r.materialId(), r.courseCode(), r.name(), r.path(), r.type()))
                    .toList();
            tblMaterials.getItems().setAll(rows);
        } catch (SQLException e) {
            showError("Failed to load course materials.", e);
        }
    }

    private void downloadMaterial(Material material) {
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

    private Path downloadFromUrl(Material material) throws Exception {
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

    private String buildFileName(Material material) {
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

}
