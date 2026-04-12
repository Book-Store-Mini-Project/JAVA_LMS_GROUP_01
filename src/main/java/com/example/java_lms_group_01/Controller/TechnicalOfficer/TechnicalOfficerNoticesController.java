package com.example.java_lms_group_01.Controller.TechnicalOfficer;

import com.example.java_lms_group_01.Repository.NoticeRepository;
import com.example.java_lms_group_01.model.Notice;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

/**
 * Shows notices to the technical officer.
 */
public class TechnicalOfficerNoticesController {

    @FXML
    private TableView<Notice> tblNotices;
    @FXML
    private TableColumn<Notice, String> colNoticeId;
    @FXML
    private TableColumn<Notice, String> colTitle;
    @FXML
    private TableColumn<Notice, String> colContent;
    @FXML
    private TableColumn<Notice, String> colPublishDate;
    @FXML
    private TableColumn<Notice, String> colCreatedBy;

    private final NoticeRepository noticeRepository = new NoticeRepository();

    @FXML
    public void initialize() {
        colNoticeId.setCellValueFactory(d -> d.getValue().noticeIdProperty());
        colTitle.setCellValueFactory(d -> d.getValue().titleProperty());
        colContent.setCellValueFactory(d -> d.getValue().contentProperty());
        colPublishDate.setCellValueFactory(d -> d.getValue().publishDateProperty());
        colCreatedBy.setCellValueFactory(d -> d.getValue().createdByProperty());
        loadNotices();
    }

    private void loadNotices() {
        try {
            List<Notice> notices = noticeRepository.findAll();
            tblNotices.getItems().setAll(notices);
        } catch (SQLException e) {
            showError("Failed to load notices.", e);
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
