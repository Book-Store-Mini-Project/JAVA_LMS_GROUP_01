package com.example.java_lms_group_01.Controller.AdminDashboard;

import com.example.java_lms_group_01.model.users.Admin;
import com.example.java_lms_group_01.model.UserManagementRow;
import com.example.java_lms_group_01.model.users.UserRole;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageUsersController implements Initializable {

    @FXML
    private TabPane tabUsers;
    @FXML
    private Tab tabAdmins;
    @FXML
    private Tab tabLecturers;
    @FXML
    private Tab tabStudents;
    @FXML
    private Tab tabTechnicalOfficers;

    @FXML
    private TableColumn<UserManagementRow, String> adminAccessLevel;
    @FXML
    private TableColumn<UserManagementRow, String> adminDeptId;
    @FXML
    private TableColumn<UserManagementRow, String> adminEmail;
    @FXML
    private TableColumn<UserManagementRow, String> adminFirstName;
    @FXML
    private TableColumn<UserManagementRow, String> adminGender;
    @FXML
    private TableColumn<UserManagementRow, String> adminId;
    @FXML
    private TableColumn<UserManagementRow, String> adminLastName;
    @FXML
    private TableColumn<UserManagementRow, String> adminPhone;
    @FXML
    private TableView<UserManagementRow> tblAdmins;

    @FXML
    private TableColumn<UserManagementRow, String> lecDeptId;
    @FXML
    private TableColumn<UserManagementRow, String> lecEmail;
    @FXML
    private TableColumn<UserManagementRow, String> lecFirstName;
    @FXML
    private TableColumn<UserManagementRow, String> lecGender;
    @FXML
    private TableColumn<UserManagementRow, String> lecId;
    @FXML
    private TableColumn<UserManagementRow, String> lecLastName;
    @FXML
    private TableColumn<UserManagementRow, String> lecPhone;
    @FXML
    private TableColumn<UserManagementRow, String> lecPosition;
    @FXML
    private TableColumn<UserManagementRow, String> lecRegNo;
    @FXML
    private TableView<UserManagementRow> tblLecturers;

    @FXML
    private TableColumn<UserManagementRow, String> stuBatchId;
    @FXML
    private TableColumn<UserManagementRow, String> stuDeptId;
    @FXML
    private TableColumn<UserManagementRow, String> stuEmail;
    @FXML
    private TableColumn<UserManagementRow, String> stuFirstName;
    @FXML
    private TableColumn<UserManagementRow, String> stuGender;
    @FXML
    private TableColumn<UserManagementRow, String> stuId;
    @FXML
    private TableColumn<UserManagementRow, String> stuLastName;
    @FXML
    private TableColumn<UserManagementRow, String> stuPhone;
    @FXML
    private TableColumn<UserManagementRow, String> stuRegNo;
    @FXML
    private TableColumn<UserManagementRow, String> stuStatus;
    @FXML
    private TableView<UserManagementRow> tblStudents;

    @FXML
    private TableColumn<UserManagementRow, String> toDeptId;
    @FXML
    private TableColumn<UserManagementRow, String> toEmail;
    @FXML
    private TableColumn<UserManagementRow, String> toFirstName;
    @FXML
    private TableColumn<UserManagementRow, String> toGender;
    @FXML
    private TableColumn<UserManagementRow, String> toId;
    @FXML
    private TableColumn<UserManagementRow, String> toLab;
    @FXML
    private TableColumn<UserManagementRow, String> toLastName;
    @FXML
    private TableColumn<UserManagementRow, String> toPhone;
    @FXML
    private TableColumn<UserManagementRow, String> toPosition;
    @FXML
    private TableColumn<UserManagementRow, String> toShift;
    @FXML
    private TableView<UserManagementRow> tblTechnicalOfficers;

    private final Admin admin = new Admin();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureAdminTable();
        configureLecturerTable();
        configureStudentTable();
        configureTechnicalOfficerTable();
        loadAllTables();
    }

    @FXML
    void btnOnActionRefresh(ActionEvent event) {
        loadAllTables();
    }

    @FXML
    void btnOnActionAdd(ActionEvent event) {
        UserRole role = getActiveRole();
        if (role == null) {
            showInfo("Please select a valid role tab.");
            return;
        }

        try {
            UserManagementRow row = showRoleDialog(role, null);
            if (row == null) {
                return;
            }

            boolean created = admin.addUser(role, row);

            if (created) {
                loadAllTables();
                showInfo(role.value() + " created successfully.");
            }
        } catch (IllegalArgumentException e) {
            showInfo(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to add " + role.value() + ".", e);
        }
    }

    @FXML
    void btnOnActionEdit(ActionEvent event) {
        UserRole role = getActiveRole();
        if (role == null) {
            showInfo("Please select a valid role tab.");
            return;
        }

        UserManagementRow selected = getSelectedRowByRole(role);
        if (selected == null) {
            showInfo("Please select a row in the active tab.");
            return;
        }

        try {
            UserManagementRow row = showRoleDialog(role, selected);
            if (row == null) {
                return;
            }

            boolean updated = admin.updateUser(role, row);

            if (updated) {
                loadAllTables();
                showInfo(role.value() + " updated successfully.");
            }
        } catch (IllegalArgumentException e) {
            showInfo(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to update " + role.value() + ".", e);
        }
    }

    @FXML
    void btnOnActionDelete(ActionEvent event) {
        UserRole role = getActiveRole();
        if (role == null) {
            showInfo("Please select a valid role tab.");
            return;
        }

        UserManagementRow selected = getSelectedRowByRole(role);
        if (selected == null) {
            showInfo("Please select a row in the active tab.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete " + role.value());
        confirmation.setContentText("Delete registration number " + selected.getRegistrationNo() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean deleted = admin.deleteUser(role, selected.getUserId());

            if (deleted) {
                loadAllTables();
                showInfo(role.value() + " deleted successfully.");
            }
        } catch (SQLException e) {
            showError("Failed to delete " + role.value() + ".", e);
        }
    }

    private UserRole getActiveRole() {
        Tab selectedTab = tabUsers.getSelectionModel().getSelectedItem();
        if (selectedTab == tabAdmins) {
            return UserRole.ADMIN;
        }
        if (selectedTab == tabLecturers) {
            return UserRole.LECTURER;
        }
        if (selectedTab == tabStudents) {
            return UserRole.STUDENT;
        }
        if (selectedTab == tabTechnicalOfficers) {
            return UserRole.TECHNICAL_OFFICER;
        }
        return null;
    }

    private UserManagementRow getSelectedRowByRole(UserRole role) {
        return switch (role) {
            case ADMIN -> tblAdmins.getSelectionModel().getSelectedItem();
            case LECTURER -> tblLecturers.getSelectionModel().getSelectedItem();
            case STUDENT -> tblStudents.getSelectionModel().getSelectedItem();
            case TECHNICAL_OFFICER -> tblTechnicalOfficers.getSelectionModel().getSelectedItem();
            default -> null;
        };
    }

    private UserManagementRow showRoleDialog(UserRole role, UserManagementRow existing) {
        return switch (role) {
            case ADMIN -> showAdminDialog(existing);
            case LECTURER -> showLecturerDialog(existing);
            case STUDENT -> showStudentDialog(existing);
            case TECHNICAL_OFFICER -> showTechnicalOfficerDialog(existing);
            default -> null;
        };
    }

    private UserManagementRow showAdminDialog(UserManagementRow existing) {
        boolean edit = existing != null;
        Dialog<UserManagementRow> dialog = baseDialog(edit ? "Edit Admin" : "Add Admin", edit);

        TextField[] common = commonFields(existing);
        DatePicker dob = dateOfBirthPicker(existing);
        ComboBox<String> gender = genderBox(existing);

        TextField txtReg = new TextField(edit ? value(existing.getRegistrationNo()) : "");
        txtReg.setDisable(edit);
        TextField txtPassword = new TextField("");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int r = addCommonGrid(grid, common, dob, gender);
        grid.add(new Label("Registration No:"), 0, r);
        grid.add(txtReg, 1, r++);
        grid.add(new Label(edit ? "New Password (optional):" : "Password:"), 0, r);
        grid.add(txtPassword, 1, r);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }
            String password = value(txtPassword);
            if (!edit && password.isBlank()) {
                throw new IllegalArgumentException("Password is required.");
            }
            return new UserManagementRow(
                    edit ? existing.getUserId() : required(txtReg, "Registration No"),
                    required(common[0], "First name"),
                    required(common[1], "Last name"),
                    required(common[2], "Email"),
                    value(common[3]),
                    value(common[4]),
                    dob.getValue(),
                    gender.getValue(),
                    UserRole.ADMIN.value(),
                    required(txtReg, "Registration No"),
                    password,
                    null,
                    null,
                    null,
                    null
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private UserManagementRow showLecturerDialog(UserManagementRow existing) {
        boolean edit = existing != null;
        Dialog<UserManagementRow> dialog = baseDialog(edit ? "Edit Lecturer" : "Add Lecturer", edit);

        TextField[] common = commonFields(existing);
        DatePicker dob = dateOfBirthPicker(existing);
        ComboBox<String> gender = genderBox(existing);

        TextField txtReg = new TextField(edit ? value(existing.getRegistrationNo()) : "");
        txtReg.setDisable(edit);
        TextField txtPassword = new TextField("");
        TextField txtDepartment = new TextField(edit ? value(existing.getDepartment()) : "");
        TextField txtPosition = new TextField(edit ? value(existing.getPosition()) : "");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int r = addCommonGrid(grid, common, dob, gender);
        grid.add(new Label("Registration No:"), 0, r);
        grid.add(txtReg, 1, r++);
        grid.add(new Label(edit ? "New Password (optional):" : "Password:"), 0, r);
        grid.add(txtPassword, 1, r++);
        grid.add(new Label("Department:"), 0, r);
        grid.add(txtDepartment, 1, r++);
        grid.add(new Label("Position:"), 0, r);
        grid.add(txtPosition, 1, r);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }
            String password = value(txtPassword);
            if (!edit && password.isBlank()) {
                throw new IllegalArgumentException("Password is required.");
            }
            return new UserManagementRow(
                    edit ? existing.getUserId() : required(txtReg, "Registration No"),
                    required(common[0], "First name"),
                    required(common[1], "Last name"),
                    required(common[2], "Email"),
                    value(common[3]),
                    value(common[4]),
                    dob.getValue(),
                    gender.getValue(),
                    UserRole.LECTURER.value(),
                    required(txtReg, "Registration No"),
                    password,
                    required(txtDepartment, "Department"),
                    null,
                    null,
                    required(txtPosition, "Position")
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private UserManagementRow showStudentDialog(UserManagementRow existing) {
        boolean edit = existing != null;
        Dialog<UserManagementRow> dialog = baseDialog(edit ? "Edit Student" : "Add Student", edit);

        TextField[] common = commonFields(existing);
        DatePicker dob = dateOfBirthPicker(existing);
        ComboBox<String> gender = genderBox(existing);

        TextField txtReg = new TextField(edit ? value(existing.getRegistrationNo()) : "");
        txtReg.setDisable(edit);
        TextField txtPassword = new TextField("");
        TextField txtDepartment = new TextField(edit ? value(existing.getDepartment()) : "");
        TextField txtGpa = new TextField(edit && existing.getGpa() != null ? String.valueOf(existing.getGpa()) : "");
        ComboBox<String> cmbStatus = new ComboBox<>();
        cmbStatus.getItems().addAll("proper", "repeat");
        cmbStatus.setValue(edit ? value(existing.getStatus()) : "proper");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int r = addCommonGrid(grid, common, dob, gender);
        grid.add(new Label("Registration No:"), 0, r);
        grid.add(txtReg, 1, r++);
        grid.add(new Label(edit ? "New Password (optional):" : "Password:"), 0, r);
        grid.add(txtPassword, 1, r++);
        grid.add(new Label("Department:"), 0, r);
        grid.add(txtDepartment, 1, r++);
        grid.add(new Label("GPA (optional):"), 0, r);
        grid.add(txtGpa, 1, r++);
        grid.add(new Label("Status:"), 0, r);
        grid.add(cmbStatus, 1, r);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }
            String password = value(txtPassword);
            if (!edit && password.isBlank()) {
                throw new IllegalArgumentException("Password is required.");
            }
            return new UserManagementRow(
                    edit ? existing.getUserId() : required(txtReg, "Registration No"),
                    required(common[0], "First name"),
                    required(common[1], "Last name"),
                    required(common[2], "Email"),
                    value(common[3]),
                    value(common[4]),
                    dob.getValue(),
                    gender.getValue(),
                    UserRole.STUDENT.value(),
                    required(txtReg, "Registration No"),
                    password,
                    required(txtDepartment, "Department"),
                    parseOptionalDouble(txtGpa),
                    requiredCombo(cmbStatus, "Status"),
                    null
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private UserManagementRow showTechnicalOfficerDialog(UserManagementRow existing) {
        boolean edit = existing != null;
        Dialog<UserManagementRow> dialog = baseDialog(edit ? "Edit Technical Officer" : "Add Technical Officer", edit);

        TextField[] common = commonFields(existing);
        DatePicker dob = dateOfBirthPicker(existing);
        ComboBox<String> gender = genderBox(existing);

        TextField txtReg = new TextField(edit ? value(existing.getRegistrationNo()) : "");
        txtReg.setDisable(edit);
        TextField txtPassword = new TextField("");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        int r = addCommonGrid(grid, common, dob, gender);
        grid.add(new Label("Registration No:"), 0, r);
        grid.add(txtReg, 1, r++);
        grid.add(new Label(edit ? "New Password (optional):" : "Password:"), 0, r);
        grid.add(txtPassword, 1, r);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }
            String password = value(txtPassword);
            if (!edit && password.isBlank()) {
                throw new IllegalArgumentException("Password is required.");
            }
            return new UserManagementRow(
                    edit ? existing.getUserId() : required(txtReg, "Registration No"),
                    required(common[0], "First name"),
                    required(common[1], "Last name"),
                    required(common[2], "Email"),
                    value(common[3]),
                    value(common[4]),
                    dob.getValue(),
                    gender.getValue(),
                    UserRole.TECHNICAL_OFFICER.value(),
                    required(txtReg, "Registration No"),
                    password,
                    null,
                    null,
                    null,
                    null
            );
        });

        return dialog.showAndWait().orElse(null);
    }

    private Dialog<UserManagementRow> baseDialog(String title, boolean edit) {
        Dialog<UserManagementRow> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(edit ? "Update selected record." : "Enter details.");
        ButtonType save = new ButtonType(edit ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        return dialog;
    }

    private TextField[] commonFields(UserManagementRow existing) {
        TextField txtFirstName = new TextField(existing == null ? "" : value(existing.getFirstName()));
        TextField txtLastName = new TextField(existing == null ? "" : value(existing.getLastName()));
        TextField txtEmail = new TextField(existing == null ? "" : value(existing.getEmail()));
        TextField txtAddress = new TextField(existing == null ? "" : value(existing.getAddress()));
        TextField txtPhone = new TextField(existing == null ? "" : value(existing.getPhoneNumber()));
        return new TextField[]{txtFirstName, txtLastName, txtEmail, txtAddress, txtPhone};
    }

    private DatePicker dateOfBirthPicker(UserManagementRow existing) {
        return new DatePicker(existing == null ? null : existing.getDateOfBirth());
    }

    private ComboBox<String> genderBox(UserManagementRow existing) {
        ComboBox<String> cmbGender = new ComboBox<>();
        cmbGender.getItems().addAll("Male", "Female", "Other");
        cmbGender.setValue(existing == null ? null : existing.getGender());
        return cmbGender;
    }

    private int addCommonGrid(GridPane grid, TextField[] common, DatePicker dob, ComboBox<String> gender) {
        int r = 0;
        grid.add(new Label("First Name:"), 0, r);
        grid.add(common[0], 1, r++);
        grid.add(new Label("Last Name:"), 0, r);
        grid.add(common[1], 1, r++);
        grid.add(new Label("Email:"), 0, r);
        grid.add(common[2], 1, r++);
        grid.add(new Label("Address:"), 0, r);
        grid.add(common[3], 1, r++);
        grid.add(new Label("Phone:"), 0, r);
        grid.add(common[4], 1, r++);
        grid.add(new Label("Date of Birth:"), 0, r);
        grid.add(dob, 1, r++);
        grid.add(new Label("Gender:"), 0, r);
        grid.add(gender, 1, r++);
        return r;
    }

    private void loadAllTables() {
        try {
            tblAdmins.getItems().setAll(admin.getAdmins());
            tblLecturers.getItems().setAll(admin.getLecturers());
            tblStudents.getItems().setAll(admin.getStudents());
            tblTechnicalOfficers.getItems().setAll(admin.getTechnicalOfficers());
        } catch (SQLException e) {
            showError("Failed to load user tables.", e);
        }
    }

    private void configureAdminTable() {
        adminId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getUserId())));
        adminFirstName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getFirstName())));
        adminLastName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getLastName())));
        adminEmail.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getEmail())));
        adminPhone.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getPhoneNumber())));
        adminGender.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getGender())));
        adminDeptId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getAddress())));
        adminAccessLevel.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getRegistrationNo())));
    }

    private void configureLecturerTable() {
        lecId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getUserId())));
        lecFirstName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getFirstName())));
        lecLastName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getLastName())));
        lecEmail.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getEmail())));
        lecPhone.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getPhoneNumber())));
        lecGender.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getGender())));
        lecRegNo.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getRegistrationNo())));
        lecDeptId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getDepartment())));
        lecPosition.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getPosition())));
    }

    private void configureStudentTable() {
        stuId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getUserId())));
        stuFirstName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getFirstName())));
        stuLastName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getLastName())));
        stuEmail.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getEmail())));
        stuPhone.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getPhoneNumber())));
        stuGender.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getGender())));
        stuRegNo.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getRegistrationNo())));
        stuDeptId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getDepartment())));
        stuBatchId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getGpa())));
        stuStatus.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getStatus())));
    }

    private void configureTechnicalOfficerTable() {
        toId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getUserId())));
        toFirstName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getFirstName())));
        toLastName.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getLastName())));
        toEmail.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getEmail())));
        toPhone.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getPhoneNumber())));
        toGender.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getGender())));
        toDeptId.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getAddress())));
        toPosition.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getRegistrationNo())));
        toLab.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getDateOfBirth())));
        toShift.setCellValueFactory(d -> new SimpleStringProperty(value(d.getValue().getRole())));
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private String value(Double value) {
        return value == null ? "" : String.format("%.2f", value);
    }

    private String value(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private String value(TextField textField) {
        return textField.getText() == null ? "" : textField.getText().trim();
    }

    private String required(TextField textField, String fieldName) {
        String text = value(textField);
        if (text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return text;
    }

    private String requiredCombo(ComboBox<String> comboBox, String fieldName) {
        String value = comboBox.getValue();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    private Double parseOptionalDouble(TextField textField) {
        String text = value(textField);
        if (text.isBlank()) {
            return null;
        }
        try {
            double gpa = Double.parseDouble(text);
            if (gpa < 0 || gpa > 4.0) {
                throw new IllegalArgumentException("GPA must be between 0.00 and 4.00.");
            }
            return gpa;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("GPA must be a valid number.");
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Database Error");
        alert.setContentText(message + "\n" + e.getMessage());
        alert.showAndWait();
    }
}
