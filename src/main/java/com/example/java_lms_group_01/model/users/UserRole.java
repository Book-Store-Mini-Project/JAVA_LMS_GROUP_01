package com.example.java_lms_group_01.model.users;

public enum UserRole {
    ADMIN("Admin"),
    LECTURER("Lecturer"),
    STUDENT("Student"),
    TECHNICAL_OFFICER("TechnicalOfficer");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static UserRole fromValue(String value) {
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unsupported role: " + value);
    }
}
