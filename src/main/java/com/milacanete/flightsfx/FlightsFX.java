package com.milacanete.flightsfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

/**
 * Clase principal de la aplicación FlightsFX para la gestión de vuelos.
 * Extiende de {@link javafx.application.Application} y se encarga de inicializar y mostrar
 * la ventana principal de la aplicación utilizando JavaFX
 * donde los usuarios pueden ver y gestionar información sobre los vuelos.
 */
public class FlightsFX extends Application {

    /**
     * Método principal de inicio de la aplicación.
     * Configura y muestra la ventana principal de la aplicación.
     * @param stage escenario principal proporcionado por JavaFX.
     * @throws Exception sí ocurre un error al cargar el archivo FXML o los recursos CSS.
     */
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(FlightsFX.class.getResource("FXMLMainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/CSS/style.css")).toExternalForm());
        stage.setTitle("FlightsFX App");
        stage.setMinWidth(615);
        stage.setMinHeight(460);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método principal de inicio de la aplicación.
     * Llama al método {@link javafx.application.Application#launch(String...)} para iniciar JavaFX.
     * @param args argumentos de línea de comandos, no utilizados en esta aplicación.
     */
    public static void main(String[] args) {
        launch();
    }
}
