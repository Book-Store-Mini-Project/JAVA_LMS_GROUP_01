package com.example.java_lms_group_01.model;

/**
 * Simple enum used to keep course type values consistent.
 */
public enum CourseType {
    THEORY("theory"),
    PRACTICAL("practical"),
    BOTH("both");

    private final String dbValue;

    CourseType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDatabaseValue() {
        return dbValue;
    }

    public static CourseType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Course type is required.");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Course type is required.");
        }

        if (normalized.equalsIgnoreCase("theory")) {
            return THEORY;
        }
        if (normalized.equalsIgnoreCase("practical")) {
            return PRACTICAL;
        }
        if (normalized.equalsIgnoreCase("both")) {
            return BOTH;
        }

        throw new IllegalArgumentException("Unsupported course type: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
