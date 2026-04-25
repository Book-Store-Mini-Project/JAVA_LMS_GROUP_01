package com.example.java_lms_group_01.Controller.Admin;

import com.example.java_lms_group_01.Repository.AdminRepository;
import com.example.java_lms_group_01.model.UserRecord;
import com.example.java_lms_group_01.model.users.UserRole;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

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
    private TableColumn<UserRecord, String> adminAccessLevel;
    @FXML
    private TableColumn<UserRecord, String> adminDeptId;
    @FXML
    private TableColumn<UserRecord, String> adminEmail;
    @FXML
    private TableColumn<UserRecord, String> adminFirstName;
    @FXML
    private TableColumn<UserRecord, String> adminGender;
    @FXML
    private TableColumn<UserRecord, String> adminId;
    @FXML
    private TableColumn<UserRecord, String> adminLastName;
    @FXML
    private TableColumn<UserRecord, String> adminPhone;
    @FXML
    private TableView<UserRecord> tblAdmins;

    @FXML
    private TableColumn<UserRecord, String> lecDeptId;
    @FXML
    private TableColumn<UserRecord, String> lecEmail;
    @FXML
    private TableColumn<UserRecord, String> lecFirstName;
    @FXML
    private TableColumn<UserRecord, String> lecGender;
    @FXML
    private TableColumn<UserRecord, String> lecId;
    @FXML
    private TableColumn<UserRecord, String> lecLastName;
    @FXML
    private TableColumn<UserRecord, String> lecPhone;
    @FXML
    private TableColumn<UserRecord, String> lecPosition;
    @FXML
    private TableColumn<UserRecord, String> lecRegNo;
    @FXML
    private TableView<UserRecord> tblLecturers;

    @FXML
    private TableColumn<UserRecord, String> stuBatchId;
    @FXML
    private TableColumn<UserRecord, String> stuDeptId;
    @FXML
    private TableColumn<UserRecord, String> stuEmail;
    @FXML
    private TableColumn<UserRecord, String> stuFirstName;
    @FXML
    private TableColumn<UserRecord, String> stuGender;
    @FXML
    private TableColumn<UserRecord, String> stuId;
    @FXML
    private TableColumn<UserRecord, String> stuLastName;
    @FXML
    private TableColumn<UserRecord, String> stuPhone;
    @FXML
    private TableColumn<UserRecord, String> stuRegNo;
    @FXML
    private TableColumn<UserRecord, String> stuStatus;
    @FXML
    private TableView<UserRecord> tblStudents;

    @FXML
    private TableColumn<UserRecord, String> toDeptId;
    @FXML
    private TableColumn<UserRecord, String> toEmail;
    @FXML
    private TableColumn<UserRecord, String> toFirstName;
    @FXML
    private TableColumn<UserRecord, String> toGender;
    @FXML
    private TableColumn<UserRecord, String> toId;
    @FXML
    private TableColumn<UserRecord, String> toLab;
    @FXML
    private TableColumn<UserRecord, String> toLastName;
    @FXML
    private TableColumn<UserRecord, String> toPhone;
    @FXML
    private TableColumn<UserRecord, String> toPosition;
    @FXML
    private TableColumn<UserRecord, String> toShift;
    @FXML
    private TableView<UserRecord> tblTechnicalOfficers;

    private final AdminRepository adminRepository = new AdminRepository();
    private Map<UserRole, Tab> tabs;
    private Map<UserRole, TableView<UserRecord>> tables;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bindRoleViews();
        configureTables();
        loadAllTables();
    }

    @FXML
    void btnOnActionRefresh(ActionEvent event) {
        loadAllTables();
    }

    @FXML
    void btnOnActionAdd(ActionEvent event) {
        handleSave(false);
    }

    @FXML
    void btnOnActionEdit(ActionEvent event) {
        handleSave(true);
    }

    @FXML
    void btnOnActionDelete(ActionEvent event) {
        UserRole role = getActiveRole();
        if (role == null) {
            showInfo("Please select a valid role tab.");
            return;
        }

        UserRecord selected = getSelectedRowByRole(role);
        if (selected == null) {
            showInfo("Please select a row in the active tab.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setHeaderText("Delete " + role.getValue());
        confirmation.setContentText("Delete registration number " + selected.getRegistrationNo() + "?");
        Optional<ButtonType> answer = confirmation.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return;
        }

        try {
            if (deleteUser(role, selected.getUserId())) {
                loadAllTables();
                showInfo(role.getValue() + " deleted successfully.");
            }
        } catch (SQLException e) {
            showError("Failed to delete " + role.getValue() + ".", e);
        }
    }

    private void handleSave(boolean edit) {
        UserRole role = getActiveRole();
        if (role == null) {
            showInfo("Please select a valid role tab.");
            return;
        }

        UserRecord selected = null;
        if (edit) {
            selected = getSelectedRowByRole(role);
            if (selected == null) {
                showInfo("Please select a row in the active tab.");
                return;
            }
        }

        try {
            UserRecord row = showRoleDialog(role, selected);
            if (row == null) {
                return;
            }

            boolean changed = edit ? updateUser(role, row) : addUser(role, row);
            if (changed) {
                loadAllTables();
                showInfo(role.getValue() + (edit ? " updated successfully." : " created successfully."));
            }
        } catch (IllegalArgumentException e) {
            showInfo(e.getMessage());
        } catch (SQLException e) {
            showError("Failed to " + (edit ? "update " : "add ") + role.getValue() + ".", e);
        }
    }

    private void bindRoleViews() {
        tabs = new EnumMap<>(UserRole.class);
        tabs.put(UserRole.ADMIN, tabAdmins);
        tabs.put(UserRole.LECTURER, tabLecturers);
        tabs.put(UserRole.STUDENT, tabStudents);
        tabs.put(UserRole.TECHNICAL_OFFICER, tabTechnicalOfficers);

        tables = new EnumMap<>(UserRole.class);
        tables.put(UserRole.ADMIN, tblAdmins);
        tables.put(UserRole.LECTURER, tblLecturers);
        tables.put(UserRole.STUDENT, tblStudents);
        tables.put(UserRole.TECHNICAL_OFFICER, tblTechnicalOfficers);
    }

    private void configureTables() {
        configureAdminTable();
        configureLecturerTable();
        configureStudentTable();
        configureTechnicalOfficerTable();
    }

    private UserRole getActiveRole() {
        Tab selectedTab = tabUsers.getSelectionModel().getSelectedItem();
        for (Map.Entry<UserRole, Tab> entry : tabs.entrySet()) {
            if (entry.getValue() == selectedTab) {
                return entry.getKey();
            }
        }
        return null;
    }

    private UserRecord getSelectedRowByRole(UserRole role) {
        return tableFor(role).getSelectionModel().getSelectedItem();
    }

    private UserRecord showRoleDialog(UserRole role, UserRecord existing) {
        boolean edit = existing != null;
        Dialog<UserRecord> dialog = baseDialog(dialogTitle(role, edit), edit);
        RoleForm form = createRoleForm(existing);

        dialog.getDialogPane().setContent(buildFormGrid(role, form, edit));
        dialog.setResultConverter(button ->
                button.getButtonData() == ButtonBar.ButtonData.OK_DONE
                        ? buildUserRecord(role, existing, form, edit)
                        : null);

        return dialog.showAndWait().orElse(null);
    }

    private Dialog<UserRecord> baseDialog(String title, boolean edit) {
        Dialog<UserRecord> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(edit ? "Update selected record." : "Enter details.");
        ButtonType save = new ButtonType(edit ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        return dialog;
    }

    private String dialogTitle(UserRole role, boolean edit) {
        return (edit ? "Edit " : "Add ") + role.getValue();
    }

    private RoleForm createRoleForm(UserRecord existing) {
        UserFormFields common = createCommonFields(existing);
        return new RoleForm(
                common,
                dateOfBirthPicker(existing),
                genderBox(existing),
                textField(existing == null ? "" : value(existing.getRegistrationNo())),
                new PasswordField(),
                textField(existing == null ? "" : value(existing.getDepartment())),
                textField(existing == null ? "" : value(existing.getBatch())),
                textField(existing != null && existing.getGpa() != null ? value(existing.getGpa()) : ""),
                statusBox(existing),
                textField(existing == null ? "" : value(existing.getPosition()))
        );
    }

    private GridPane buildFormGrid(UserRole role, RoleForm form, boolean edit) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        int row = addCommonGrid(grid, form.commonFields(), form.dob(), form.gender());
        row = addRow(grid, "Registration No:", form.registrationNoField(), row);
        row = addRow(grid, edit ? "New Password (optional):" : "Password:", form.passwordField(), row);

        if (role == UserRole.LECTURER) {
            row = addRow(grid, "Department:", form.departmentField(), row);
            row = addRow(grid, "Position:", form.positionField(), row);
        } else if (role == UserRole.STUDENT) {
            row = addRow(grid, "Department:", form.departmentField(), row);
            row = addRow(grid, "Batch:", form.batchField(), row);
            row = addRow(grid, "GPA (optional):", form.gpaField(), row);
            row = addRow(grid, "Status:", form.statusField(), row);
        }

        return grid;
    }

    private UserRecord buildUserRecord(UserRole role, UserRecord existing, RoleForm form, boolean edit) {
        String registrationNo = required(form.registrationNoField(), "Registration No");
        String password = requirePasswordForCreate(edit, form.passwordField());
        String userId = edit ? existing.getUserId() : registrationNo;

        return switch (role) {
            case ADMIN -> new UserRecord(
                    userId,
                    required(form.commonFields().getFirstNameField(), "First name"),
                    required(form.commonFields().getLastNameField(), "Last name"),
                    required(form.commonFields().getEmailField(), "Email"),
                    value(form.commonFields().getAddressField()),
                    value(form.commonFields().getPhoneField()),
                    form.dob().getValue(),
                    form.gender().getValue(),
                    UserRole.ADMIN.getValue(),
                    registrationNo,
                    password,
                    null,
                    null,
                    null,
                    null,
                    null,
                    value(form.commonFields().getImagePathField())
            );
            case LECTURER -> new UserRecord(
                    userId,
                    required(form.commonFields().getFirstNameField(), "First name"),
                    required(form.commonFields().getLastNameField(), "Last name"),
                    required(form.commonFields().getEmailField(), "Email"),
                    value(form.commonFields().getAddressField()),
                    value(form.commonFields().getPhoneField()),
                    form.dob().getValue(),
                    form.gender().getValue(),
                    UserRole.LECTURER.getValue(),
                    registrationNo,
                    password,
                    required(form.departmentField(), "Department"),
                    null,
                    null,
                    null,
                    required(form.positionField(), "Position"),
                    value(form.commonFields().getImagePathField())
            );
            case STUDENT -> new UserRecord(
                    userId,
                    required(form.commonFields().getFirstNameField(), "First name"),
                    required(form.commonFields().getLastNameField(), "Last name"),
                    required(form.commonFields().getEmailField(), "Email"),
                    value(form.commonFields().getAddressField()),
                    value(form.commonFields().getPhoneField()),
                    form.dob().getValue(),
                    form.gender().getValue(),
                    UserRole.STUDENT.getValue(),
                    registrationNo,
                    password,
                    required(form.departmentField(), "Department"),
                    required(form.batchField(), "Batch"),
                    parseOptionalDouble(form.gpaField()),
                    requiredCombo(form.statusField(), "Status"),
                    null,
                    value(form.commonFields().getImagePathField())
            );
            case TECHNICAL_OFFICER -> new UserRecord(
                    userId,
                    required(form.commonFields().getFirstNameField(), "First name"),
                    required(form.commonFields().getLastNameField(), "Last name"),
                    required(form.commonFields().getEmailField(), "Email"),
                    value(form.commonFields().getAddressField()),
                    value(form.commonFields().getPhoneField()),
                    form.dob().getValue(),
                    form.gender().getValue(),
                    UserRole.TECHNICAL_OFFICER.getValue(),
                    registrationNo,
                    password,
                    null,
                    null,
                    null,
                    null,
                    null,
                    value(form.commonFields().getImagePathField())
            );
        };
    }

    private UserFormFields createCommonFields(UserRecord existing) {
        return new UserFormFields(
                textField(existing == null ? "" : value(existing.getFirstName())),
                textField(existing == null ? "" : value(existing.getLastName())),
                textField(existing == null ? "" : value(existing.getEmail())),
                textField(existing == null ? "" : value(existing.getAddress())),
                textField(existing == null ? "" : value(existing.getPhoneNumber())),
                textField(existing == null ? "" : value(existing.getProfileImagePath()))
        );
    }

    private DatePicker dateOfBirthPicker(UserRecord existing) {
        return new DatePicker(existing == null ? null : existing.getDateOfBirth());
    }

    private ComboBox<String> genderBox(UserRecord existing) {
        ComboBox<String> cmbGender = new ComboBox<>();
        cmbGender.getItems().addAll("Male", "Female", "Other");
        cmbGender.setValue(existing == null ? null : existing.getGender());
        return cmbGender;
    }

    private ComboBox<String> statusBox(UserRecord existing) {
        ComboBox<String> cmbStatus = new ComboBox<>();
        cmbStatus.getItems().addAll("proper", "repeat");
        String status = existing == null ? "proper" : value(existing.getStatus());
        cmbStatus.setValue(status.isBlank() ? "proper" : status);
        return cmbStatus;
    }

    private int addCommonGrid(GridPane grid, UserFormFields fields, DatePicker dob, ComboBox<String> gender) {
        int row = 0;
        row = addRow(grid, "First Name:", fields.getFirstNameField(), row);
        row = addRow(grid, "Last Name:", fields.getLastNameField(), row);
        row = addRow(grid, "Email:", fields.getEmailField(), row);
        row = addRow(grid, "Address:", fields.getAddressField(), row);
        row = addRow(grid, "Phone:", fields.getPhoneField(), row);

        HBox imageBox = new HBox(8.0);
        Button btnBrowseImage = new Button("Browse");
        btnBrowseImage.setOnAction(event -> chooseImageFile(fields.getImagePathField()));
        imageBox.getChildren().addAll(fields.getImagePathField(), btnBrowseImage);
        row = addRow(grid, "Profile Image:", imageBox, row);

        row = addRow(grid, "Date of Birth:", dob, row);
        return addRow(grid, "Gender:", gender, row);
    }

    private int addRow(GridPane grid, String label, Node field, int row) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }

    private void chooseImageFile(TextField targetField) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File file = chooser.showOpenDialog(targetField.getScene() == null ? null : targetField.getScene().getWindow());
        if (file != null) {
            targetField.setText(file.getAbsolutePath());
        }
    }

    private void loadAllTables() {
        try {
            for (UserRole role : UserRole.values()) {
                tableFor(role).getItems().setAll(findUsers(role));
            }
        } catch (SQLException e) {
            showError("Failed to load user tables.", e);
        }
    }

    private List<UserRecord> findUsers(UserRole role) throws SQLException {
        return switch (role) {
            case ADMIN -> adminRepository.findAdmins();
            case LECTURER -> adminRepository.findLecturers();
            case STUDENT -> adminRepository.findStudents();
            case TECHNICAL_OFFICER -> adminRepository.findTechnicalOfficers();
        };
    }

    private boolean addUser(UserRole role, UserRecord row) throws SQLException {
        return switch (role) {
            case ADMIN -> adminRepository.createAdmin(row);
            case LECTURER -> adminRepository.createLecturer(row);
            case STUDENT -> adminRepository.createStudent(row);
            case TECHNICAL_OFFICER -> adminRepository.createTechnicalOfficer(row);
        };
    }

    private boolean updateUser(UserRole role, UserRecord row) throws SQLException {
        return switch (role) {
            case ADMIN -> adminRepository.updateAdmin(row);
            case LECTURER -> adminRepository.updateLecturer(row);
            case STUDENT -> adminRepository.updateStudent(row);
            case TECHNICAL_OFFICER -> adminRepository.updateTechnicalOfficer(row);
        };
    }

    private boolean deleteUser(UserRole role, String userId) throws SQLException {
        return switch (role) {
            case ADMIN -> adminRepository.deleteAdmin(userId);
            case LECTURER -> adminRepository.deleteLecturer(userId);
            case STUDENT -> adminRepository.deleteStudent(userId);
            case TECHNICAL_OFFICER -> adminRepository.deleteTechnicalOfficer(userId);
        };
    }

    private TableView<UserRecord> tableFor(UserRole role) {
        return tables.get(role);
    }

    private void bind(TableColumn<UserRecord, String> column, Function<UserRecord, ?> extractor) {
        column.setCellValueFactory(d -> new SimpleStringProperty(display(extractor.apply(d.getValue()))));
    }

    private void configureAdminTable() {
        bind(adminId, UserRecord::getUserId);
        bind(adminFirstName, UserRecord::getFirstName);
        bind(adminLastName, UserRecord::getLastName);
        bind(adminEmail, UserRecord::getEmail);
        bind(adminPhone, UserRecord::getPhoneNumber);
        bind(adminGender, UserRecord::getGender);
        bind(adminDeptId, UserRecord::getAddress);
        bind(adminAccessLevel, UserRecord::getRegistrationNo);
    }

    private void configureLecturerTable() {
        bind(lecId, UserRecord::getUserId);
        bind(lecFirstName, UserRecord::getFirstName);
        bind(lecLastName, UserRecord::getLastName);
        bind(lecEmail, UserRecord::getEmail);
        bind(lecPhone, UserRecord::getPhoneNumber);
        bind(lecGender, UserRecord::getGender);
        bind(lecRegNo, UserRecord::getRegistrationNo);
        bind(lecDeptId, UserRecord::getDepartment);
        bind(lecPosition, UserRecord::getPosition);
    }

    private void configureStudentTable() {
        bind(stuId, UserRecord::getUserId);
        bind(stuFirstName, UserRecord::getFirstName);
        bind(stuLastName, UserRecord::getLastName);
        bind(stuEmail, UserRecord::getEmail);
        bind(stuPhone, UserRecord::getPhoneNumber);
        bind(stuGender, UserRecord::getGender);
        bind(stuRegNo, UserRecord::getRegistrationNo);
        bind(stuDeptId, UserRecord::getDepartment);
        bind(stuBatchId, UserRecord::getBatch);
        bind(stuStatus, UserRecord::getStatus);
    }

    private void configureTechnicalOfficerTable() {
        bind(toId, UserRecord::getUserId);
        bind(toFirstName, UserRecord::getFirstName);
        bind(toLastName, UserRecord::getLastName);
        bind(toEmail, UserRecord::getEmail);
        bind(toPhone, UserRecord::getPhoneNumber);
        bind(toGender, UserRecord::getGender);
        bind(toDeptId, UserRecord::getAddress);
        bind(toPosition, UserRecord::getRegistrationNo);
        bind(toLab, UserRecord::getDateOfBirth);
        bind(toShift, UserRecord::getRole);
    }

    private String display(Object value) {
        return value == null ? "" : value.toString();
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

    private TextField textField(String value) {
        return new TextField(value == null ? "" : value);
    }

    private String required(TextField textField, String fieldName) {
        String text = value(textField);
        if (text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return text;
    }

    private String requiredCombo(ComboBox<String> comboBox, String fieldName) {
        String selected = comboBox.getValue();
        if (selected == null || selected.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return selected;
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

    private String requirePasswordForCreate(boolean edit, TextField txtPassword) {
        String password = value(txtPassword);
        if (!edit && password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        return password;
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

    private record RoleForm(
            UserFormFields commonFields,
            DatePicker dob,
            ComboBox<String> gender,
            TextField registrationNoField,
            PasswordField passwordField,
            TextField departmentField,
            TextField batchField,
            TextField gpaField,
            ComboBox<String> statusField,
            TextField positionField
    ) {
    }
}
