package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Service.AdminService;
import com.example.java_lms_group_01.model.Notice;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Admin screen for creating, editing, searching, and deleting notices.
 */
public class ManageNoticesController implements Initializable {

    @FXML
    private TableColumn<Notice, String> colAuthor;

    @FXML
    private TableColumn<Notice, String> colDate;

    @FXML
    private TableColumn<Notice, Number> colNoticeId;

    @FXML
    private TableColumn<Notice, String> colTitle;

    @FXML
    private TableView<Notice> tblNotices;

    @FXML
    private TextField txtSearchNotice;

    private final AdminService adminService = new AdminService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureColumns();
        loadNotices(null);

        txtSearchNotice.textProperty().addListener((obs, oldValue, newValue) ->
                loadNotices(newValue)
        );
    }

    private void configureColumns() {
        colNoticeId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNoticeId()));
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPublishDate() == null ? "" : data.getValue().getPublishDate().toString()
        ));
        colAuthor.setCellValueFactory(data -> new SimpleStringProperty(value(data.getValue().getCreatedBy())));
    }

    private void loadNotices(String keyword) {
        try {
            List<Notice> notices = adminService.getNotices(keyword);
            tblNotices.getItems().setAll(notices);
        } catch (SQLException e) {
            showError("Failed to load notices.", e);
        }
    }

    @FXML
    void btnOnActionAddNewNotice(ActionEvent event) {
        Notice newNotice = showNoticeDialog(null);
        if (newNotice == null) {
            return;
        }

        try {
            boolean saved = adminService.createNotice(newNotice);
            if (saved) {
                loadNotices(txtSearchNotice.getText());
                showInfo("Notice added successfully.");
            } else {
                showInfo("No notice was added.");
            }
        } catch (SQLException e) {
            showError("Failed to add notice.", e);
        }
    }

    @FXML
    void btnOnActionDeleteNotice(ActionEvent event) {
        Notice selectedNotice = tblNotices.getSelectionModel().getSelectedItem();
        if (selectedNotice == null) {
            showInfo("Please select a notice to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete Notice");
        confirmation.setContentText("Delete selected notice: " + selectedNotice.getTitle() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean deleted = adminService.deleteNotice(selectedNotice.getNoticeId());
            if (deleted) {
                loadNotices(txtSearchNotice.getText());
                showInfo("Notice deleted successfully.");
            } else {
                showInfo("No notice was deleted.");
            }
        } catch (SQLException e) {
            showError("Failed to delete notice.", e);
        }
    }

    @FXML
    void btnOnActionViewNotice(ActionEvent event) {
        Notice selectedNotice = tblNotices.getSelectionModel().getSelectedItem();
        if (selectedNotice == null) {
            showInfo("Please select a notice to view or edit.");
            return;
        }

        Notice updatedNotice = showNoticeDialog(selectedNotice);
        if (updatedNotice == null) {
            return;
        }

        try {
            boolean updated = adminService.updateNotice(updatedNotice);
            if (updated) {
                loadNotices(txtSearchNotice.getText());
                showInfo("Notice updated successfully.");
            } else {
                showInfo("No notice was updated.");
            }
        } catch (SQLException e) {
            showError("Failed to update notice.", e);
        }
    }

    // Open a notice dialog and return the notice entered by the admin.
    private Notice showNoticeDialog(Notice existingNotice) {
        boolean editMode = existingNotice != null;

        Dialog<Notice> dialog = new Dialog<>();
        dialog.setTitle(editMode ? "View / Edit Notice" : "Create Notice");
        dialog.setHeaderText(editMode ? "Update selected notice details." : "Enter new notice details.");

        ButtonType saveButtonType = new ButtonType(editMode ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField txtTitle = new TextField();
        TextArea txtContent = new TextArea();
        txtContent.setPrefRowCount(5);
        DatePicker datePicker = new DatePicker();
        TextField txtCreatedBy = new TextField();

        if (editMode) {
            txtTitle.setText(existingNotice.getTitle());
            txtContent.setText(existingNotice.getContent());
            datePicker.setValue(existingNotice.getPublishDate());
            txtCreatedBy.setText(value(existingNotice.getCreatedBy()));
        } else {
            datePicker.setValue(LocalDate.now());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Title:"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(txtContent, 1, 1);
        grid.add(new Label("Publish Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Created By (Admin Reg No):"), 0, 3);
        grid.add(txtCreatedBy, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != saveButtonType) {
                return null;
            }

            String title = value(txtTitle);
            String content = value(txtContent);
            LocalDate publishDate = datePicker.getValue();

            if (title.isBlank()) {
                showInfo("Title is required.");
                return null;
            }
            if (publishDate == null) {
                showInfo("Publish date is required.");
                return null;
            }

            String createdBy = value(txtCreatedBy);
            if (createdBy.isBlank()) {
                showInfo("Created By is required.");
                return null;
            }

            Notice notice = new Notice(
                    editMode ? existingNotice.getNoticeId() : 0,
                    title,
                    content,
                    publishDate,
                    createdBy
            );
            return notice;
        });

        Optional<Notice> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String value(TextArea textArea) {
        return textArea.getText() == null ? "" : textArea.getText().trim();
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Database Error");
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
