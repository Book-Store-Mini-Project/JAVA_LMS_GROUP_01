package com.example.java_lms_group_01.model.request;

/**
 * Simple material request.
 */
public class MaterialRequest {
    private String courseCode;
    private String name;
    private String path;
    private String materialType;

    public MaterialRequest() {
    }

    public MaterialRequest(String courseCode, String name, String path, String materialType) {
        this.courseCode = courseCode;
        this.name = name;
        this.path = path;
        this.materialType = materialType;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }
}
