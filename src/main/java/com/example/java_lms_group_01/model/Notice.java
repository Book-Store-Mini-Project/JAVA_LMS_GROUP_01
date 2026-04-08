package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private LocalDate publishDate;
    private String createdBy;

    public Notice() {
    }

    public Notice(int noticeId, String title, String content, LocalDate publishDate, String createdBy) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishDate = publishDate;
        this.createdBy = createdBy;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void publishNotice(){}
    public void editNotice(){}
    public void removeNotice(){}

    public SimpleStringProperty noticeIdProperty() {
        return new SimpleStringProperty(String.valueOf(noticeId));
    }

    public SimpleStringProperty titleProperty() {
        return new SimpleStringProperty(value(title));
    }

    public SimpleStringProperty contentProperty() {
        return new SimpleStringProperty(value(content));
    }

    public SimpleStringProperty publishDateProperty() {
        return new SimpleStringProperty(publishDate == null ? "" : publishDate.toString());
    }

    public SimpleStringProperty dateProperty() {
        return publishDateProperty();
    }

    public SimpleStringProperty createdByProperty() {
        return new SimpleStringProperty(value(createdBy));
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

}
