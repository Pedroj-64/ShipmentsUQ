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

    opens co.edu.uniquindio.sameday.shipmentsuqsameday to javafx.fxml;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.model;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.model.enums;
    exports co.edu.uniquindio.sameday.shipmentsuqsameday.model.interfaces;
}