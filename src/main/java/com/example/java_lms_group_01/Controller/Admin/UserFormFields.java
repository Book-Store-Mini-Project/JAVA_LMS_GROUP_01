package com.example.java_lms_group_01.Controller.Admin;

import javafx.scene.control.TextField;

/**
 * Simple form fields holder used by the admin user dialog.
 */
class UserFormFields {
    private final TextField firstNameField;
    private final TextField lastNameField;
    private final TextField emailField;
    private final TextField addressField;
    private final TextField phoneField;
    private final TextField imagePathField;

    UserFormFields(TextField firstNameField, TextField lastNameField, TextField emailField,
                   TextField addressField, TextField phoneField, TextField imagePathField) {
        this.firstNameField = firstNameField;
        this.lastNameField = lastNameField;
        this.emailField = emailField;
        this.addressField = addressField;
        this.phoneField = phoneField;
        this.imagePathField = imagePathField;
    }

    TextField getFirstNameField() {
        return firstNameField;
    }

    TextField getLastNameField() {
        return lastNameField;
    }

    TextField getEmailField() {
        return emailField;
    }

    TextField getAddressField() {
        return addressField;
    }

    TextField getPhoneField() {
        return phoneField;
    }

    TextField getImagePathField() {
        return imagePathField;
    }
}
