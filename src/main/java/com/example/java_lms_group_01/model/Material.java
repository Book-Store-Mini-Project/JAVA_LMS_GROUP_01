package com.example.java_lms_group_01.model;

import javafx.beans.property.SimpleStringProperty;

public class Material {
    private final SimpleStringProperty materialId;
    private final SimpleStringProperty courseCode;
    private final SimpleStringProperty name;
    private final SimpleStringProperty path;
    private final SimpleStringProperty type;

    public Material(String materialId, String courseCode, String name, String path, String type) {
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
