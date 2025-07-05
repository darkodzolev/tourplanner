module at.technikum.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    requires java.desktop;
    requires java.net.http;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires jasperreports;
    requires commons.logging;
    requires org.apache.commons.collections4;

    opens at.technikum.javafx to javafx.fxml;
    opens at.technikum.javafx.view to javafx.fxml;
    opens at.technikum.javafx.entity;
    opens at.technikum.javafx.service;

    exports at.technikum.javafx;
    exports at.technikum.javafx.view;
    exports at.technikum.javafx.viewmodel;
}