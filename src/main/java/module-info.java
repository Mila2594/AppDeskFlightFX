/**
 * Módulo principal de la aplicación FlightsFX que gestiona vuelos
 * y su visualización utilizando JavaFX.
 */
module com.milacanete.flightsfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.logging;

    opens com.milacanete.flightsfx to javafx.fxml;
    exports com.milacanete.flightsfx;
    exports com.milacanete.flightsfx.model;
    exports com.milacanete.flightsfx.utils;


}