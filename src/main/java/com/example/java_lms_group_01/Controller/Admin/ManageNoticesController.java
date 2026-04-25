package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Repository.AdminRepository;
import com.example.java_lms_group_01.model.Notice;
import com.example.java_lms_group_01.session.LoggedInAdmin;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageNoticesController implements Initializable {

    @FXML
    private TableView<Notice> tblNotices;
    @FXML
    private TableColumn<Notice, Number> colNoticeId;
    @FXML
    private TableColumn<Notice, String> colTitle;
    @FXML
    private TableColumn<Notice, String> colDate;
    @FXML
    private TableColumn<Notice, String> colAuthor;
    @FXML
    private TextField txtSearchNotice;

    private final AdminRepository adminRepository = new AdminRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        loadNotices("");

        txtSearchNotice.textProperty().addListener((obs, oldValue, newValue) -> loadNotices(newValue));
    }

    // Set up the notice table columns.
    private void setupColumns() {
        colNoticeId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNoticeId()));
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(text(data.getValue().getTitle())));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(dateText(data.getValue().getPublishDate())));
        colAuthor.setCellValueFactory(data -> new SimpleStringProperty(text(data.getValue().getCreatedBy())));
    }

    @FXML
    void btnOnActionAddNewNotice(ActionEvent event) {
        Notice notice = openNoticeDialog(null);
        if (notice == null) {
            return;
        }

        try {
            if (adminRepository.saveNotice(notice)) {
                loadNotices(text(txtSearchNotice));
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
        Notice selected = tblNotices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a notice to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete Notice");
        confirmation.setContentText("Delete notice: " + selected.getTitle() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            if (adminRepository.deleteNoticeById(selected.getNoticeId())) {
                loadNotices(text(txtSearchNotice));
            } else {
                showInfo("No notice was deleted.");
            }
        } catch (SQLException e) {
            showError("Failed to delete notice.", e);
        }
    }

    @FXML
    void btnOnActionViewNotice(ActionEvent event) {
        Notice selected = tblNotices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Please select a notice to view or edit.");
            return;
        }

        Notice updated = openNoticeDialog(selected);
        if (updated == null) {
            return;
        }

        try {
            if (adminRepository.updateNotice(updated)) {
                loadNotices(text(txtSearchNotice));
                showInfo("Notice updated successfully.");
            } else {
                showInfo("No notice was updated.");
            }
        } catch (SQLException e) {
            showError("Failed to update notice.", e);
        }
    }

    // Open one dialog for both adding and editing notices.
    private Notice openNoticeDialog(Notice existingNotice) {
        boolean edit = existingNotice != null;
        String adminRegNo = text(LoggedInAdmin.getRegistrationNo());

        Dialog<Notice> dialog = new Dialog<>();
        dialog.setTitle(edit ? "Edit Notice" : "Create Notice");
        dialog.setHeaderText(edit ? "Update the notice details." : "Enter a new notice.");

        ButtonType save = new ButtonType(edit ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField txtTitle = new TextField(edit ? text(existingNotice.getTitle()) : "");
        TextArea txtContent = new TextArea(edit ? text(existingNotice.getContent()) : "");
        DatePicker datePicker = new DatePicker(edit ? existingNotice.getPublishDate() : LocalDate.now());
        TextField txtCreatedBy = new TextField(edit ? text(existingNotice.getCreatedBy()) : adminRegNo);

        txtContent.setPrefRowCount(5);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Title:"), 0, 0);
        grid.add(txtTitle, 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(txtContent, 1, 1);
        grid.add(new Label("Publish Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Created By:"), 0, 3);
        grid.add(txtCreatedBy, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != save) {
                return null;
            }

            String title = text(txtTitle);
            String content = text(txtContent);
            LocalDate publishDate = datePicker.getValue();
            String createdBy = text(txtCreatedBy);

            if (title.isBlank()) {
                showInfo("Title is required.");
                return null;
            }
            if (publishDate == null) {
                showInfo("Publish date is required.");
                return null;
            }
            if (createdBy.isBlank()) {
                createdBy = adminRegNo;
            }
            if (createdBy.isBlank()) {
                showInfo("Created By is required.");
                return null;
            }

            return new Notice(
                    edit ? existingNotice.getNoticeId() : 0,
                    title,
                    content,
                    publishDate,
                    createdBy
            );
        });

        Optional<Notice> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void loadNotices(String keyword) {
        try {
            List<Notice> notices;
            if (keyword == null || keyword.trim().isEmpty()) {
                notices = adminRepository.findAllNotices();
            } else {
                notices = adminRepository.findNoticesByKeyword(keyword.trim());
            }
            tblNotices.getItems().setAll(notices);
        } catch (SQLException e) {
            showError("Failed to load notices.", e);
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String text(TextArea area) {
        return area.getText() == null ? "" : area.getText().trim();
    }

    private String dateText(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message + "\n" + exception.getMessage());
        alert.showAndWait();
    }
}
