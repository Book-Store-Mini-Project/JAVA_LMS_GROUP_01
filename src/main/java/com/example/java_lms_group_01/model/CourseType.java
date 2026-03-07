package com.example.java_lms_group_01.model;

public enum CourseType {
    THEORY("theory"),
    PRACTICAL("practical"),
    BOTH("both");

    private final String dbValue;

    CourseType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static CourseType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Course type is required.");
        }

        String normalized = value.trim();
        for (CourseType type : values()) {
            if (type.dbValue.equalsIgnoreCase(normalized) || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported course type: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
