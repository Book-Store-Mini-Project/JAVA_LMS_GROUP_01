module com.example.java_lms_group_01 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    requires java.sql;


    opens com.example.java_lms_group_01 to javafx.fxml;
    opens com.example.java_lms_group_01.Controller to javafx.fxml;
    exports com.example.java_lms_group_01;
}
