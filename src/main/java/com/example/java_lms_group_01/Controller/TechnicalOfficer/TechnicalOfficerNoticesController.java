package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.NoticeRepository;
import com.example.java_lms_group_01.model.Notice;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

public class TechnicalOfficerNoticesController {

    @FXML private TableView<Notice> tblNotices;

    @FXML private TableColumn<Notice, String> colNoticeId;
    @FXML private TableColumn<Notice, String> colTitle;
    @FXML private TableColumn<Notice, String> colContent;
    @FXML private TableColumn<Notice, String> colPublishDate;
    @FXML private TableColumn<Notice, String> colCreatedBy;

    private final NoticeRepository noticeRepository = new NoticeRepository();

    @FXML
    public void initialize() {
        // Bind table columns to the Notice model properties
        configureTableColumns();

        // Load the notices from the database
        loadAllNotices();
    }

    private void configureTableColumns() {
        // Beginner-friendly way to link columns to data
        colNoticeId.setCellValueFactory(data -> {
            return data.getValue().noticeIdProperty();
        });

        colTitle.setCellValueFactory(data -> {
            return data.getValue().titleProperty();
        });

        colContent.setCellValueFactory(data -> {
            return data.getValue().contentProperty();
        });

        colPublishDate.setCellValueFactory(data -> {
            return data.getValue().publishDateProperty();
        });

        colCreatedBy.setCellValueFactory(data -> {
            return data.getValue().createdByProperty();
        });
    }

    private void loadAllNotices() {
        try {
            // Fetch the list from the repository
            List<Notice> noticeList = noticeRepository.findAll();

            // Set the list into the TableView
            tblNotices.getItems().setAll(noticeList);

        } catch (SQLException e) {
            showErrorMessage("Database Error", "Failed to load notices.");
        }
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}