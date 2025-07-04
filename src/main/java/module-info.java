module at.technikum.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires jasperreports;
    requires commons.logging;
    requires org.apache.commons.collections4;

    opens at.technikum.javafx to javafx.fxml;
    opens at.technikum.javafx.view to javafx.fxml;
    opens at.technikum.javafx.entity;
    opens at.technikum.javafx.service to jasperreports;

    exports at.technikum.javafx;
    exports at.technikum.javafx.view;
    exports at.technikum.javafx.viewmodel;
}