package com.example.java_lms_group_01.model.users;

/**
 * User roles available in the system.
 */
public enum UserRole {
    ADMIN("Admin"),
    LECTURER("Lecturer"),
    STUDENT("Student"),
    TECHNICAL_OFFICER("TechnicalOfficer");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unsupported role: null");
        }
        if (ADMIN.value.equalsIgnoreCase(value)) {
            return ADMIN;
        }
        if (LECTURER.value.equalsIgnoreCase(value)) {
            return LECTURER;
        }
        if (STUDENT.value.equalsIgnoreCase(value)) {
            return STUDENT;
        }
        if (TECHNICAL_OFFICER.value.equalsIgnoreCase(value)) {
            return TECHNICAL_OFFICER;
        }
        throw new IllegalArgumentException("Unsupported role: " + value);
    }
}
