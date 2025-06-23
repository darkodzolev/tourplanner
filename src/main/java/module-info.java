module at.technikum.javafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    opens at.technikum.javafx to javafx.fxml;
    opens at.technikum.javafx.view to javafx.fxml;
    opens at.technikum.javafx.entity;

    exports at.technikum.javafx;
    exports at.technikum.javafx.view;
    exports at.technikum.javafx.viewmodel;
}
