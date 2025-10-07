module co.edu.uniquindio.sameday.shipmentsuqsameday {
    requires static lombok;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires jdk.jsobject;

    opens co.edu.uniquindio.sameday.shipmentsuqsameday to javafx.fxml;
    opens co.edu.uniquindio.sameday.shipmentsuqsameday.viewController to javafx.fxml;
    opens co.edu.uniquindio.sameday.shipmentsuqsameday.controller to javafx.fxml;
    opens co.edu.uniquindio.sameday.shipmentsuqsameday.model to javafx.base;
    opens co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums to javafx.base;
    opens co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces to javafx.base;
    opens co.edu.uniquindio.sameday.shipmentsuqsameday.model.util.utilWeb to javafx.web;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.model;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.viewController;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.controller;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.util;
    
}