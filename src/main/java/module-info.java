module com.jscheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.slf4j;

    opens com.jscheduler to javafx.fxml;
    opens com.jscheduler.controller to javafx.fxml;
    opens com.jscheduler.model to com.fasterxml.jackson.databind;

    exports com.jscheduler;
    exports com.jscheduler.controller;
    exports com.jscheduler.model;
    exports com.jscheduler.service;
    exports com.jscheduler.repository;
    exports com.jscheduler.util;
    exports com.jscheduler.config;
}
