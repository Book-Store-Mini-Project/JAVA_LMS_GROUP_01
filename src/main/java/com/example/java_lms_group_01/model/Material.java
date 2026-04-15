package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Simple material model used in tables.
 */
public class Material {
    private String materialId;
    private String courseCode;
    private String name;
    private String path;
    private String type;

    public Material() {
    }

    public Material(String materialId, String courseCode, String name, String path, String type) {
        setMaterialId(materialId);
        setCourseCode(courseCode);
        setName(name);
        setPath(path);
        setType(type);
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = text(materialId);
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = text(courseCode);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = text(name);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = text(path);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = text(type);
    }

    public SimpleStringProperty materialIdProperty() {
        return property(materialId);
    }

    public SimpleStringProperty courseCodeProperty() {
        return property(courseCode);
    }

    public SimpleStringProperty nameProperty() {
        return property(name);
    }

    public SimpleStringProperty pathProperty() {
        return property(path);
    }

    public SimpleStringProperty typeProperty() {
        return property(type);
    }

    private static SimpleStringProperty property(String value) {
        return new SimpleStringProperty(text(value));
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
