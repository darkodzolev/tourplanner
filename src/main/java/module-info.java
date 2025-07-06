module at.technikum.javafx {
    // JavaFX modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    // JDK modules
    requires java.desktop;
    requires java.net.http;
    requires java.sql;

    // Spring modules
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.jdbc;
    requires spring.orm;
    requires spring.tx;
    requires spring.data.commons;
    requires spring.data.jpa;

    // JPA and Hibernate
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Connection pool
    requires com.zaxxer.hikari;

    // JSON processing
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    // Logging
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.slf4j;

    // JasperReports and utils
    requires jasperreports;
    requires commons.logging;
    requires org.apache.commons.collections4;

    // Export for use in FXML, DI, or testing
    exports at.technikum.javafx;
    exports at.technikum.javafx.view;
    exports at.technikum.javafx.config to spring.context;
    exports at.technikum.javafx.viewmodel;
    exports at.technikum.javafx.service;

    // Open for reflection (JavaFX, Spring, JPA)
    opens at.technikum.javafx to javafx.fxml, spring.core, spring.beans;
    opens at.technikum.javafx.config to spring.core, spring.beans;
    opens at.technikum.javafx.view to javafx.fxml;
    opens at.technikum.javafx.entity;
    opens at.technikum.javafx.service to spring.core, spring.beans, spring.aop, spring.tx;
    opens at.technikum.javafx.event to spring.beans, spring.context;
}