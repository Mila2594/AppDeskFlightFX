package com.milacanete.flightsfx;

import com.milacanete.flightsfx.model.Flight;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Controlador para la vista de gráficos en el módulo de vuelos.
 * Gestiona la visualización de un gráfico circular basado en los destinos de vuelo
 * y permite la navegación de regreso a la vista principal.
 */
public class FXMLChartViewController {
    @FXML
    public PieChart idFlightsPieChart;

    @FXML
    public Button idGoToBackButton;

    /**
     * Inicializa el controlador y configura el gráfico circular con los datos de vuelos agrupados por destino.
     * Este método se ejecuta automáticamente al cargar el archivo FXML correspondiente.
     */
    @FXML
    public void initialize() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milacanete/flightsfx/FXMLMainView.fxml"));

        try {
            Parent root = loader.load();
        } catch (Exception e) {
            Logger.getLogger(FXMLChartViewController.class.getName()).log(Level.SEVERE, null, e);
        }
        // Obtiene el controlador principal y la lista de vuelos
        FXMLMainViewController controller = loader.getController();
        List<Flight> flights = controller.getFlightsObsList();

        idFlightsPieChart.setTitle("Destinos");
        idFlightsPieChart.getData().clear();

        // Agrupa los vuelos por destino y cuenta la cantidad de vuelos para cada destino
        Map<String, Long> result = flights.stream()
                .collect(Collectors.groupingBy(
                        Flight::getDestination,
                        Collectors.counting()
                ));
        // Agrega los datos procesados al gráfico circular
        result.forEach((destination, count) ->
                idFlightsPieChart.getData().add(new PieChart.Data(destination, count)));
    }

    /**
     * Maneja el evento de clic en el botón "Volver" y permite regresar a la vista principal.
     * Cambia la escena actual de la ventana a la definida en el archivo FXML de la vista principal.
     * @param event el evento que desencadena la acción, asociado al botón.
     * @throws Exception si ocurre un error al cargar la vista principal.
     */
    public void goToBack(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milacanete/flightsfx/FXMLMainView.fxml"));
        Parent view1 = loader.load();
        Scene view1Scene = new Scene(view1);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        view1Scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/CSS/style.css")).toExternalForm());
        stage.hide();
        stage.setScene(view1Scene);
        stage.show();
    }
}
