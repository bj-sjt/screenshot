module com.itao.screenshot {
    requires javafx.controls;
    //requires javafx.fxml;
    //requires javafx.web;
    requires javafx.swing;
    requires fastjson;


    //opens com.itao.screenshot to javafx.fxml;
    opens com.itao.screenshot to fastjson;
    exports com.itao.screenshot;
}