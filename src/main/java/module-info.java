module at.technikum.javafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens at.technikum.javafx to javafx.fxml;
    opens at.technikum.javafx.view to javafx.fxml;
    opens at.technikum.javafx.entity;

    exports at.technikum.javafx;
    exports at.technikum.javafx.view;
    exports at.technikum.javafx.viewmodel;
}
