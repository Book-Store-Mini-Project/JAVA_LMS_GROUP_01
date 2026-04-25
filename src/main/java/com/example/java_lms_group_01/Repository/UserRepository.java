package com.example.java_lms_group_01.Repository;

import com.example.java_lms_group_01.model.UserRecord;
import com.example.java_lms_group_01.util.DBConnection;
import com.example.java_lms_group_01.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserRepository {

    private final UserImageRepository userImageRepository = new UserImageRepository();

    // Find All Admins
    public List<UserRecord> findAdmins() throws SQLException {
        List<UserRecord> list = new ArrayList<>();

        Connection con = DBConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users u JOIN admin a ON u.user_id = a.registrationNo";

        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            UserRecord u = new UserRecord(
                    rs.getString("user_id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("phoneNumber"),
                    rs.getDate("dateOfBirth") == null ? null : rs.getDate("dateOfBirth").toLocalDate(),
                    rs.getString("gender"),
                    "Admin",
                    rs.getString("registrationNo"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            list.add(u);
        }

        return list;
    }

    // Find All Lecturers
    public List<UserRecord> findLecturers() throws SQLException {
        List<UserRecord> list = new ArrayList<>();

        Connection con = DBConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users u JOIN lecturer l ON u.user_id = l.registrationNo";

        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            UserRecord u = new UserRecord(
                    rs.getString("user_id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("phoneNumber"),
                    rs.getDate("dateOfBirth") == null ? null : rs.getDate("dateOfBirth").toLocalDate(),
                    rs.getString("gender"),
                    "Lecturer",
                    rs.getString("registrationNo"),
                    null,
                    rs.getString("department"),
                    null,
                    null,
                    null,
                    rs.getString("position"),
                    null
            );
            list.add(u);
        }

        return list;
    }

    // Find All Students
    public List<UserRecord> findStudents() throws SQLException {
        List<UserRecord> list = new ArrayList<>();

        Connection con = DBConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users u JOIN student s ON u.user_id = s.registrationNo";

        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            UserRecord u = new UserRecord(
                    rs.getString("user_id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("phoneNumber"),
                    rs.getDate("dateOfBirth") == null ? null : rs.getDate("dateOfBirth").toLocalDate(),
                    rs.getString("gender"),
                    "Student",
                    rs.getString("registrationNo"),
                    null,
                    rs.getString("department"),
                    rs.getString("batch"),
                    rs.getDouble("GPA"),
                    rs.getString("status"),
                    null,
                    null
            );
            list.add(u);
        }

        return list;
    }

    // Find All Technical Officers
    public List<UserRecord> findTechnicalOfficers() throws SQLException {
        List<UserRecord> list = new ArrayList<>();

        Connection con = DBConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users u JOIN tech_officer t ON u.user_id = t.registrationNo";

        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            UserRecord u = new UserRecord(
                    rs.getString("user_id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getString("phoneNumber"),
                    rs.getDate("dateOfBirth") == null ? null : rs.getDate("dateOfBirth").toLocalDate(),
                    rs.getString("gender"),
                    "TechnicalOfficer",
                    rs.getString("registrationNo"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            list.add(u);
        }

        return list;
    }

    // Create Admin
    public boolean createAdmin(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            // Insert into users
            PreparedStatement u = con.prepareStatement(
                    "INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            u.setString(1, row.getRegistrationNo());
            u.setString(2, row.getFirstName());
            u.setString(3, row.getLastName());
            u.setString(4, row.getEmail());
            u.setString(5, row.getAddress());
            u.setString(6, row.getPhoneNumber());
            u.setDate(7, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(8, row.getGender());
            u.executeUpdate();

            // Insert into admin
            PreparedStatement a = con.prepareStatement(
                    "INSERT INTO admin VALUES (?, ?)");
            a.setString(1, row.getRegistrationNo());
            a.setString(2, PasswordUtil.hashPassword(row.getPassword()));
            a.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create Lecturer
    public boolean createLecturer(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement u = con.prepareStatement(
                    "INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            u.setString(1, row.getRegistrationNo());
            u.setString(2, row.getFirstName());
            u.setString(3, row.getLastName());
            u.setString(4, row.getEmail());
            u.setString(5, row.getAddress());
            u.setString(6, row.getPhoneNumber());
            u.setDate(7, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(8, row.getGender());
            u.executeUpdate();

            PreparedStatement l = con.prepareStatement(
                    "INSERT INTO lecturer VALUES (?, ?, ?, ?)");
            l.setString(1, row.getRegistrationNo());
            l.setString(2, PasswordUtil.hashPassword(row.getPassword()));
            l.setString(3, row.getDepartment());
            l.setString(4, row.getPosition());
            l.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create Student
    public boolean createStudent(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement u = con.prepareStatement(
                    "INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            u.setString(1, row.getRegistrationNo());
            u.setString(2, row.getFirstName());
            u.setString(3, row.getLastName());
            u.setString(4, row.getEmail());
            u.setString(5, row.getAddress());
            u.setString(6, row.getPhoneNumber());
            u.setDate(7, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(8, row.getGender());
            u.executeUpdate();

            PreparedStatement s = con.prepareStatement(
                    "INSERT INTO student VALUES (?, ?, ?, ?, ?, ?)");
            s.setString(1, row.getRegistrationNo());
            s.setString(2, PasswordUtil.hashPassword(row.getPassword()));
            s.setString(3, row.getDepartment());
            s.setString(4, row.getBatch());
            s.setDouble(5, row.getGpa() == null ? 0 : row.getGpa());
            s.setString(6, row.getStatus());
            s.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create Technical Officer
    public boolean createTechnicalOfficer(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement u = con.prepareStatement(
                    "INSERT INTO users VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            u.setString(1, row.getRegistrationNo());
            u.setString(2, row.getFirstName());
            u.setString(3, row.getLastName());
            u.setString(4, row.getEmail());
            u.setString(5, row.getAddress());
            u.setString(6, row.getPhoneNumber());
            u.setDate(7, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(8, row.getGender());
            u.executeUpdate();

            PreparedStatement t = con.prepareStatement(
                    "INSERT INTO tech_officer VALUES (?, ?,?)");
            t.setString(1, row.getRegistrationNo());
            t.setString(2, PasswordUtil.hashPassword(row.getPassword()));
            t.setString(3, row.getDepartment());
            t.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update Admin
    public boolean updateAdmin(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            // Update users table
            PreparedStatement u = con.prepareStatement(
                    "UPDATE users SET firstName=?, lastName=?, email=?, address=?, phoneNumber=?, dateOfBirth=?, gender=? WHERE user_id=?");

            u.setString(1, row.getFirstName());
            u.setString(2, row.getLastName());
            u.setString(3, row.getEmail());
            u.setString(4, row.getAddress());
            u.setString(5, row.getPhoneNumber());
            u.setDate(6, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(7, row.getGender());
            u.setString(8, row.getRegistrationNo());
            u.executeUpdate();

            // Update password (only if provided)
            if (row.getPassword() != null && !row.getPassword().isEmpty()) {
                PreparedStatement a = con.prepareStatement(
                        "UPDATE admin SET password=? WHERE registrationNo=?");

                a.setString(1, PasswordUtil.hashPassword(row.getPassword()));
                a.setString(2, row.getRegistrationNo());
                a.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update Lecturer
    public boolean updateLecturer(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            // Update users table
            PreparedStatement u = con.prepareStatement(
                    "UPDATE users SET firstName=?, lastName=?, email=?, address=?, phoneNumber=?, dateOfBirth=?, gender=? WHERE user_id=?");

            u.setString(1, row.getFirstName());
            u.setString(2, row.getLastName());
            u.setString(3, row.getEmail());
            u.setString(4, row.getAddress());
            u.setString(5, row.getPhoneNumber());
            u.setDate(6, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(7, row.getGender());
            u.setString(8, row.getRegistrationNo());
            u.executeUpdate();

            // Update lecturer table
            PreparedStatement l = con.prepareStatement(
                    "UPDATE lecturer SET department=?, position=? WHERE registrationNo=?");

            l.setString(1, row.getDepartment());
            l.setString(2, row.getPosition());
            l.setString(3, row.getRegistrationNo());
            l.executeUpdate();

            // Optional password update
            if (row.getPassword() != null && !row.getPassword().isEmpty()) {
                PreparedStatement p = con.prepareStatement(
                        "UPDATE lecturer SET password=? WHERE registrationNo=?");

                p.setString(1, PasswordUtil.hashPassword(row.getPassword()));
                p.setString(2, row.getRegistrationNo());
                p.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update Student
    public boolean updateStudent(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            // Update users table
            PreparedStatement u = con.prepareStatement(
                    "UPDATE users SET firstName=?, lastName=?, email=?, address=?, phoneNumber=?, dateOfBirth=?, gender=? WHERE user_id=?");

            u.setString(1, row.getFirstName());
            u.setString(2, row.getLastName());
            u.setString(3, row.getEmail());
            u.setString(4, row.getAddress());
            u.setString(5, row.getPhoneNumber());
            u.setDate(6, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(7, row.getGender());
            u.setString(8, row.getRegistrationNo());
            u.executeUpdate();

            // Update student table
            PreparedStatement s = con.prepareStatement(
                    "UPDATE student SET department=?, batch=?, GPA=?, status=? WHERE registrationNo=?");

            s.setString(1, row.getDepartment());
            s.setString(2, row.getBatch());
            s.setDouble(3, row.getGpa() == null ? 0 : row.getGpa());
            s.setString(4, row.getStatus());
            s.setString(5, row.getRegistrationNo());
            s.executeUpdate();

            // Optional password update
            if (row.getPassword() != null && !row.getPassword().isEmpty()) {
                PreparedStatement p = con.prepareStatement(
                        "UPDATE student SET password=? WHERE registrationNo=?");

                p.setString(1, PasswordUtil.hashPassword(row.getPassword()));
                p.setString(2, row.getRegistrationNo());
                p.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update Technical Officer
    public boolean updateTechnicalOfficer(UserRecord row) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            // Update users table
            PreparedStatement u = con.prepareStatement(
                    "UPDATE users SET firstName=?, lastName=?, email=?, address=?, phoneNumber=?, dateOfBirth=?, gender=? WHERE user_id=?");

            u.setString(1, row.getFirstName());
            u.setString(2, row.getLastName());
            u.setString(3, row.getEmail());
            u.setString(4, row.getAddress());
            u.setString(5, row.getPhoneNumber());
            u.setDate(6, row.getDateOfBirth() == null ? null : Date.valueOf(row.getDateOfBirth()));
            u.setString(7, row.getGender());
            u.setString(8, row.getRegistrationNo());
            u.executeUpdate();

            // Optional password update
            if (row.getPassword() != null && !row.getPassword().isEmpty()) {
                PreparedStatement t = con.prepareStatement(
                        "UPDATE tech_officer SET password=? WHERE registrationNo=?");

                t.setString(1, PasswordUtil.hashPassword(row.getPassword()));
                t.setString(2, row.getRegistrationNo());
                t.executeUpdate();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // delete Admin
    public boolean deleteAdmin(String id) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement role = con.prepareStatement(
                    "DELETE FROM admin WHERE registrationNo=?");
            role.setString(1, id);
            role.executeUpdate();

            PreparedStatement user = con.prepareStatement(
                    "DELETE FROM users WHERE user_id=?");
            user.setString(1, id);

            return user.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // delete Lecturer
    public boolean deleteLecturer(String id) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement role = con.prepareStatement(
                    "DELETE FROM lecturer WHERE registrationNo=?");
            role.setString(1, id);
            role.executeUpdate();

            PreparedStatement user = con.prepareStatement(
                    "DELETE FROM users WHERE user_id=?");
            user.setString(1, id);

            return user.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // delete Student
    public boolean deleteStudent(String id) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement role = con.prepareStatement(
                    "DELETE FROM student WHERE registrationNo=?");
            role.setString(1, id);
            role.executeUpdate();

            PreparedStatement user = con.prepareStatement(
                    "DELETE FROM users WHERE user_id=?");
            user.setString(1, id);

            return user.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // delete TechnicalOfficer
    public boolean deleteTechnicalOfficer(String id) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement role = con.prepareStatement(
                    "DELETE FROM tech_officer WHERE registrationNo=?");
            role.setString(1, id);
            role.executeUpdate();

            PreparedStatement user = con.prepareStatement(
                    "DELETE FROM users WHERE user_id=?");
            user.setString(1, id);

            return user.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
