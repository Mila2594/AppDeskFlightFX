package com.milacanete.flightsfx;

import com.milacanete.flightsfx.model.Flight;
import com.milacanete.flightsfx.utils.FileUtils;
import com.milacanete.flightsfx.utils.MessageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Controlador principal para la vista de gestión de vuelos.
 * Maneja la lógica para gestionar los vuelos en la aplicación, incluyendo la visualización,
 * adición, eliminación y actualización de vuelos en la tabla.
 * Además, permite aplicar filtros, validar entradas y mostrar alertas de confirmación o errores.
 *
 * <p> La lista de vuelos se obtiene mediante la clase {@link FileUtils},
 * que carga los datos desde un archivo y los mantiene como una lista observable. </p>
 * <p> La clase también utiliza {@link MessageUtils} para mostrar alertas de validación,
 * como la confirmación de acciones y la validación de caracteres prohibidos. </p>
 */
public class FXMLMainViewController {
    /**
     * Botón para agregar un vuelo.
     */
    @FXML
    public Button idAddButton;

    /**
     * Botón para aplicar filtros.
     */
    @FXML
    public Button idApplyFilterButton;

    /**
     * Etiqueta para indicar si un vuelo ya existe.
     */
    @FXML
    public Label idFlightIsExists;

    /**
     * Etiqueta para indicar un carácter prohibido.
     */
    @FXML
    public Label idProhibitedCharacter;

    /**
     * Botón para buscar un vuelo.
     */
    @FXML
    public Button idSearchFlightButton;

    /**
     * Campo de texto para buscar un vuelo.
     */
    @FXML
    public TextField idSearchTextField;

    /**
     * Botón para actualizar información de un vuelo.
     */
    @FXML
    public Button idUpdateFlightButton;

    /**
     * ChoiceBox para seleccionar el criterio de búsqueda.
     */
    @FXML
    public ChoiceBox<String> idOptionSearchChoiceBox;

    /**
     * Botón para navegar a la vista de gráficos.
     */
    @FXML
    public Button idChartViewButton;

    /**
     * Botón para eliminar un vuelo.
     */
    @FXML
    private Button idDeleteButton;

    /**
     * Columna para la partida de un vuelo.
     */
    @FXML
    private TableColumn<Flight, String> idDepartureColumn;

    /**
     * Campo de texto para la partida de un vuelo.
     */
    @FXML
    private TextField idDepartureTextField;

    /**
     * Columna para el destino de un vuelo.
     */
    @FXML
    private TableColumn<Flight, String> idDestinationColumn;

    /**
     * Campo de texto para el destino de un vuelo.
     */
    @FXML
    private TextField idDestinationTextField;

    /**
     * Columna para la duración de un vuelo.
     */
    @FXML
    private TableColumn<Flight, LocalTime> idDurationColumn;

    /**
     * Campo de texto para la duración de un vuelo.
     */
    @FXML
    private TextField idDurationTextField;

    /**
     * ChoiceBox para seleccionar filtros.
     */
    @FXML
    private ChoiceBox<String> idFiltersChoiceBox;

    /**
     * Columna para el número de vuelo.
     */
    @FXML
    private TableColumn<Flight, String> idFlightNumberColumn;

    /**
     * Campo de texto para el número de vuelo.
     */
    @FXML
    private TextField idFlightNumberTextField;

    /**
     * Tabla para mostrar los vuelos.
     */
    @FXML
    private TableView<Flight> idVuelosTableView;

    /**
     * Raíz de la vista.
     */
    @FXML
    private SplitPane rootSplitPane;

    /**
     * Lista observable de vuelos.
     */
    @FXML
    private ObservableList<Flight> flightsObsList;

    //Logger para registrar información y errores
    private static final Logger logger = Logger.getLogger(FXMLMainViewController.class.getName());

    /**
     * Método de inicialización que se ejecuta al cargar la vista.
     * Establece los valores predeterminados de los ChoiceBoxes,
     * carga los vuelos desde el archivo y configura los listeners.
     */
    public void initialize() {

        //listener para cerrar la ventana
        if (rootSplitPane.getScene() != null) {
            Stage stage = (Stage) rootSplitPane.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        }

        //listener para desactivar botón agregar si el botón actualizar se activa, si hay un carácter prohibido o si el vuelo ya existe
        ChangeListener<Boolean> listenerAddButton = (_, _, _) -> {
            boolean isUpdating = !idUpdateFlightButton.isDisable();
            boolean prohibitedCharacter = idProhibitedCharacter.isVisible();
            boolean flightIsExists = idFlightIsExists.isVisible();
            idAddButton.setDisable(isUpdating || prohibitedCharacter || flightIsExists);
        };

        idUpdateFlightButton.disableProperty().addListener(listenerAddButton); //si se activa el botón actualizar se desactiva el botón agregar
        idProhibitedCharacter.visibleProperty().addListener(listenerAddButton); //si hay un carácter prohibido se desactiva el botón agregar
        idFlightIsExists.visibleProperty().addListener(listenerAddButton); //si el vuelo ya existe se desactiva el botón agregar

        //habilitar botón eliminar cuando un item de la tabla este seleccionado y el botón actualizar desactivado
        idVuelosTableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            boolean isUpdating = !idUpdateFlightButton.isDisable();
            idDeleteButton.setDisable(newValue == null || isUpdating);
        });

        //items filtros choice box
        idFiltersChoiceBox.setItems(
                FXCollections.observableArrayList(
                        "Show all flights",
                        "Show flights to currently selected city",
                        "Show long flights",
                        "Show next 5 flights",
                        "Show flight duration average"
                )
        );
        idOptionSearchChoiceBox.setItems(
                FXCollections.observableArrayList(
                        "Flight number",
                        "Destination",
                        "Departure time")
        );

        idFiltersChoiceBox.getSelectionModel().selectFirst();
        idOptionSearchChoiceBox.getSelectionModel().selectFirst();

        //cargar la lista de vuelos
        List<Flight> flightsList = FileUtils.getFlights();
        flightsObsList = FXCollections.observableArrayList(flightsList != null ? flightsList : List.of());
        idVuelosTableView.setItems(flightsObsList);

        // Cargar los datos en la tabla
        idFlightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        idDestinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        idDepartureColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        idDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        formatFlightNumber(idFlightNumberTextField);

        //restringir carácter ;
        restringCharacters();

        //listener en los campos de entrada para verificar si el vuelo ya existe, no se aplica en el proceso de actualización
        List<TextField> fields = listFields();
        fields.forEach(field -> field.textProperty().addListener((_, _, _) -> {
            if (idUpdateFlightButton.isDisable()) {
                restringFlightExists();
            }
        }));

        //listener para el campo de entrada del número de vuelo para verificar si hay caracteres prohibidos
        idFlightNumberTextField.textProperty().addListener((_, _, _) -> {
            if (idUpdateFlightButton.isDisable()) {
                formatFlightNumber(idFlightNumberTextField);
            }
        });

        resetToInitialState(); //asegurar la vista inicial
    }

    /**
     * Método auxiliar.
     * Obtiene los campos de entrada del formulario.
     * @return Una lista de objetos {@link TextField} con los campos del número de vuelo, destino, hora de salida y duración.
     */
    private List<TextField> listFields() {
        return List.of(idFlightNumberTextField, idDestinationTextField, idDepartureTextField, idDurationTextField);
    }

    /**
     *
     * Configura las restricciones para caracteres prohibidos en los campos de texto.
     * Carácter prohibido: ";"
     * Muestra un mensaje de advertencia y elimina el carácter prohibido si se detecta.
     */
    private void restringCharacters() {
        List<TextField> fields = listFields();
        fields.forEach(field -> field.textProperty().addListener((_, _, newValue) -> {
            if (newValue.contains(";")){
                MessageUtils.showWarning("Caracteres prohibidos en los campos de entrada");
                field.setText(newValue.replace(";", ""));
            }
        }));
    }

    /**
     * Formatea el número de vuelo ingresado en el campo de texto para que sea válido.
     * Asegura que el número de vuelo esté en mayúsculas y contenga solo caracteres alfanuméricos.
     * El botón de agregar vuelo se habilita solo si el formato es correcto.
     * @param numberTextField Campo de texto del número de vuelo.
     */
    private void formatFlightNumber(TextField numberTextField) {
        numberTextField.textProperty().addListener((_, _, newText) -> {

            String formattedText = newText.toUpperCase();
            boolean isValid = formattedText.matches("[A-Z0-9]*");
            if (isValid) {
                numberTextField.setText(formattedText);  // Aplica el texto formateado
                idProhibitedCharacter.setVisible(false);  // Oculta el mensaje de error
                idAddButton.setDisable(false); // Habilita el botón
            } else {
                idProhibitedCharacter.setVisible(true);  // Muestra el mensaje de error
                idAddButton.setDisable(true); // Deshabilita el botón
            }
        });
    }

    /**
     * Verifica si el vuelo ya existe en la lista.
     * Formatea la hora de salida y la duración para que coincidan con el formato del registro.
     * Se visibiliza el mensaje de error contenido en un label si el vuelo ya existe, de lo contrario, se oculta.
     */
    private void restringFlightExists() {
        try {
            // Convertir los valores ingresados
            LocalDateTime departureTime = LocalDateTime.parse(idDepartureTextField.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            LocalTime duration = LocalTime.parse(idDurationTextField.getText(), DateTimeFormatter.ofPattern("H:mm")); //formato es hh:mm, para el patrón se usa H

            // Verificar si el vuelo ya existe
            boolean flightExists = validateFlightExists(idFlightNumberTextField.getText(), idDestinationTextField.getText(), departureTime, duration);
            idFlightIsExists.setVisible(flightExists); // Muestra el mensaje de error dentro de la interfaz
        } catch (Exception e) {
            idFlightIsExists.setVisible(false);
        }
    }

    /**
     * Asociado a la acción de clic en el botón "Add".
     * Agrega un nuevo vuelo a la tabla y a la lista de vuelos observables.
     * Válida los campos de entrada para asegurarse de que no estén vacíos y que los datos sean correctos
     * (como el formato de la fecha de salida y la duración del vuelo). Si los datos son válidos, crea un nuevo objeto
     * {@code Flight}, lo agrega a la lista de vuelos observables y guarda la lista en un archivo.
     */
    @FXML
    public void addFlight() {
        String flightNumber = idFlightNumberTextField.getText();
        String destination = idDestinationTextField.getText();
        String departureTimeText = idDepartureTextField.getText();
        String durationText = idDurationTextField.getText();

        //validar campos vacíos
        if (validateFieldsEmpty(listFields())) return;

        //validar formato de fecha de departure
        LocalDateTime departureTime = validateDepartureTime(departureTimeText);
        if (departureTime == null) return;

        //validar formato de duration
        LocalTime duration = validateDuration(durationText);
        if (duration == null) return;

        //validar si el vuelo ya existe utilizando el método listFields y stream
        if (validateFlightExists(flightNumber, destination, departureTime, duration)) return;

        //crear nuevo vuelo, agregarlo a la lista y a la tabla
        Flight newFlight = new Flight(flightNumber, destination, departureTime, duration);
        flightsObsList.add(newFlight);

        //uso de try catch para validar que se guardó el vuelo en el fichero y registrarlo en el log
        try {
            FileUtils.saveFlightsToFile(flightsObsList); //guardar vuelo en el fichero
            clearFields();  //limpiar campos
            idFlightNumberTextField.requestFocus();
        } catch (Exception e) {
            //logger.log(Level.SEVERE, "Error al guardar vuelo", e);
            MessageUtils.showError("Error al guardar vuelo");
        }
    }

    /**
     * Válida si los campos de entrada están vacíos.
     * @param fields lista de Campos de entrada.
     * @return true si algún campo está vacío, de lo contrario false.
     */
    private boolean validateFieldsEmpty(List<TextField> fields) {//crea una lista con los campos
        if (fields.stream().map(TextField::getText).anyMatch(String::isEmpty)) {
            MessageUtils.showError("Ningún de los campos puede estar vacío");
            return true;
        }
        return false;
    }

    /**
     * Válida el formato de la fecha y hora de partida.
     * @param departureTimeText Texto con la fecha de partida.
     * @return LocalDateTime válido o null si el formato es incorrecto.
     */
    private LocalDateTime validateDepartureTime(String departureTimeText) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return LocalDateTime.parse(departureTimeText, formatter);
        } catch (Exception ex) {
            MessageUtils.showError("La fecha de partida debe tener el formato dd/MM/yyyy HH:mm");
            return null;
        }
    }

    /**
     * Válida el formato de la duración del vuelo.
     * @param durationText Texto con la duración.
     * @return LocalTime válido o null si el formato es incorrecto.
     */
    private LocalTime validateDuration(String durationText) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm"); //formato es hh:mm, para el patrón se usa H
            return LocalTime.parse(durationText, formatter);
        } catch (Exception ex) {
            MessageUtils.showError("La duración debe tener el formato hh:mm");
            return null;
        }
    }

    /**
     * Verifica si un vuelo ya existe en la lista observable de vuelos.
     * Realiza la comparación utilizando streams con el método anyMatch.
     * @param flightNumber Número del vuelo.
     * @param destination Destino del vuelo.
     * @param departureTime Hora de salida del vuelo.
     * @param duration Duración del vuelo.
     * @return {@code true} si el vuelo ya existe; de lo contrario, {@code false}.
     */
    private boolean validateFlightExists(String flightNumber, String destination, LocalDateTime departureTime, LocalTime duration) {
        return flightsObsList.stream()
                .anyMatch(flight -> flight.equals(new Flight(flightNumber, destination, departureTime, duration)));
    }

    /**
     * Método auxiliar.
     * Limpia los campos de entrada.
     */
    private void clearFields() {
        idFlightNumberTextField.clear();
        idDestinationTextField.clear();
        idDepartureTextField.clear();
        idDurationTextField.clear();
    }

    /**
     * Asociado a la acción de click en el botón "Delete".
     * Elimina el vuelo seleccionado de la tabla y la lista de vuelos observables.
     * Muestra un mensaje de confirmación al usuario antes de eliminar el vuelo.
     * Sí el usuario confirma la eliminación, se intenta eliminar el vuelo de la tabla,
     * de la lista de vuelos observables y guardar la lista actualizada en el archivo.
     * Sí ocurre un error durante la eliminación, se muestra un mensaje de error.
     */
    @FXML
    public void deleteFlight( ) {
        Flight flight = idVuelosTableView.getSelectionModel().getSelectedItem();
        if (!confirmDeleteFlight(flight)) {
            return;
        }
        try {
            idVuelosTableView.getItems().remove(flight);// eliminar vuelo de la tabla
            flightsObsList.remove(flight); // eliminar vuelo de la lista
            idVuelosTableView.getSelectionModel().clearSelection(); // limpiar campos
            FileUtils.saveFlightsToFile(flightsObsList); // guardar lista de vuelos actualizada en el fichero
            idFlightNumberTextField.requestFocus(); //poner el foco en el field flightNumber
        } catch (Exception e) {
            //logger.log(Level.SEVERE, "No se pudo eliminar el vuelo: " + flight, e);
            MessageUtils.showError("No se pudo eliminar el vuelo.");
        }
    }

    /**
     * Confirma si se desea eliminar el vuelo seleccionado.
     * @param flight Vuelo a eliminar.
     * @return true si se confirma, false de lo contrario.
     */
    private boolean confirmDeleteFlight(Flight flight) {
        return MessageUtils.showConfirmation("¿Desea eliminar el vuelo: \n" + flight + "?");
    }

    /**
     * Asociado a la acción de clic en el botón "Apply Filter".
     * Si no hay vuelos para filtrar, se muestra un mensaje de error.
     * Los filtros disponibles incluyen: mostrar todos los vuelos, mostrar vuelos a una ciudad seleccionada, mostrar vuelos largos,
     * mostrar los próximos 5 vuelos o mostrar el promedio de duración de los vuelos.
     * Aplica un filtro a la tabla de vuelos basado en la opción seleccionada.
     * Si el filtro seleccionado es "Show all flights", se muestra todos los vuelos y restablece la vista a su estado inicial.
     * Si rl filtro seleccionado es "Show flight duration average",
     * se muestra el promedio de duración de los vuelos y se actualiza al filtro predeterminado.
     * Sí la tabla filtrada que se muestra queda vacía, se actualiza la tabla al filter predeterminado.
     */
    @FXML
    public void applyFilter() {

        //si no hay vuelos un mensaje de error de tipo alert
        if (flightsObsList.isEmpty()) {
            MessageUtils.showError("No hay vuelos para filtrar");
            idFiltersChoiceBox.setValue("Show all flights");
            return;
        }

        String selectedFilter = idFiltersChoiceBox.getValue();
        switch (selectedFilter) {
            case "Show all flights" -> {
                showAllFlights();
                resetToInitialState();
            }
            case "Show flights to currently selected city" -> showFlightsToSelectedCity();
            case "Show long flights" -> showLongFlights();
            case "Show next 5 flights" -> showNext5Flights();
            case "Show flight duration average" -> {
                showFlightDurationAverage();
                //restaurar a la tabla de vuelos a su estado inicial después del dialog alert de la duración promedio
                idFiltersChoiceBox.setValue("show all flights");
                showAllFlights();
            }
        }

        // Listener para detectar cambios en la lista de elementos de la tabla
        idVuelosTableView.getItems().addListener((ListChangeListener<Flight>) _ -> {
            // Comprobar si la tabla está vacía
            if (idVuelosTableView.getItems().isEmpty()) {
                idFiltersChoiceBox.setValue("Show all flights");
                showAllFlights();
            }
        });
    }

    /**
     * Muestra todos los vuelos en la tabla.
     * Restablece la vista a su estado inicial y pone el foco en el field flightNumber.
     */
    private void showAllFlights() {
        idVuelosTableView.setItems(flightsObsList);
        idFlightNumberTextField.requestFocus();
    }

    /**
     * Muestra los vuelos hacia la ciudad seleccionada en la tabla.
     * Se valida que se haya seleccionado un vuelo. Si no se ha seleccionado, se muestra un mensaje de error.
     * Si se ha seleccionado un vuelo, se filtran los vuelos hacia la ciudad seleccionada.
     * Se muestran en la tabla la lista filtrada.
     */
    private void showFlightsToSelectedCity() {
        //antes de filtrar validar que esté seleccionado un vuelo
        Flight flight = idVuelosTableView.getSelectionModel().getSelectedItem();
        if (flight == null) {
            MessageUtils.showError("Selecciona un vuelo para filtrar por la ciudad destino");
            return;
        }

        String selectedCity = flight.getDestination(); //nombre de la ciudad
        ObservableList<Flight> filteredFlights = flightsObsList.stream()
                .filter(f -> f.getDestination().equals(selectedCity)) //compara las ciudades
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        idVuelosTableView.setItems(filteredFlights);
        idVuelosTableView.getSelectionModel().clearSelection(); //quitar selección para que botón delete este deshabilitado
    }

    /**
     * Muestra los vuelos con una duración mayor a 3 horas (180 minutos).
     * Con streams, se filtran los vuelos mayores a 3 horas y se muestran en la tabla la lista filtrada.
     */
    private void showLongFlights() {
        ObservableList<Flight> filteredFlights = flightsObsList.stream()
                .filter(flightFiltered -> flightFiltered.getDuration().getHour() * 60 //convertir horas a minutos
                        + flightFiltered.getDuration().getMinute() > 180)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        idVuelosTableView.setItems(filteredFlights);
    }

    /**
     * Muestra los próximos 5 vuelos según la fecha y hora de partida.
     * Con streams se filtran los vuelos con fecha y hora de partida posterior a la fecha y hora actual del sistema.
     * Se muestra la lista filtrada en la tabla.
     */
    private void showNext5Flights() {
        ObservableList<Flight> filteredFlights = flightsObsList.stream()
                .filter(flightFiltered -> flightFiltered.getDepartureTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .limit(5)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        idVuelosTableView.setItems(filteredFlights);
    }

    /**
     * Calcula y muestra la duración promedio de todos los vuelos en horas y minutos.
     * Si no hay vuelos, muestra un mensaje de error.
     * Si hay vuelos, muestra la duración promedio en un alert.
     * Con streams se calcula la duración promedio de todos los vuelos en minutos.
     * Se parsea la duración promedio a horas y minutos.
     */
    private void showFlightDurationAverage() {
        //convertir duration en minutos y promediarlos para obtener la duración media en minutos
        OptionalDouble averageDuration = flightsObsList.stream()
                .mapToDouble(flight -> flight.getDuration().getHour() * 60 + flight.getDuration().getMinute())
                .average();
        //mostrar la duración media en horas y minutos en un alert
        if (averageDuration.isPresent()) {
            //obtener la duración media en horas y minutos
            int averageDurationHours = (int) (averageDuration.getAsDouble() / 60);
            int averageDurationMinutes = (int) (averageDuration.getAsDouble() % 60);
            MessageUtils.showMessage("Duración promedio de todos los vuelos: " +
                    String.format("%02d:%02d", averageDurationHours, averageDurationMinutes));
        } else {
            MessageUtils.showError("No se pudo calcular la duración promedio, porque la lista de vuelos esta vacía");
        }
    }

    /**
     * Asociado a la acción de clic en el botón "Search".
     * Verifica que el campo de búsqueda no este vacío. Si lo está, muestra un mensaje de error.
     * Implementa la función de búsqueda de vuelos.
     */
    @FXML
    public void searchFlight() {
        String selectedOption = idOptionSearchChoiceBox.getValue();
        String flightData = idSearchTextField.getText();

        if (flightData.isEmpty()){
            MessageUtils.showError("No hay dato para buscar");
            return;
        }
        foundFlights(flightData, selectedOption);
    }

    /**
     * Método que realiza la búsqueda de vuelos, según el criterio de búsqueda seleccionado.
     * Filtra la lista de vuelo según el número de vuelo, el destino o la fecha y hora de salida.
     * Si encuentra un o más vuelos, muestra la tabla con los vuelos encontrados, se habilita el botón "Update"
     * y el listener para la selección de la tabla.
     * Sí no encuentra ningúno vuelo, muestra un mensaje de información.
     * @param flightData Información del vuelo a buscar.
     * @param searchBy   Tipo de búsqueda (número, destino, hora de salida).
     */
    private void foundFlights(String flightData, String searchBy) {
        ObservableList<Flight> filteredFlights = FXCollections.observableArrayList();

        switch (searchBy) {
            case "Flight number" -> filteredFlights = flightsObsList.stream()
                    .filter(f -> f.getFlightNumber().equalsIgnoreCase(flightData))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            case "Destination" -> filteredFlights = flightsObsList.stream()
                    .filter(f -> f.getDestination().equalsIgnoreCase(flightData))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            case "Departure time" -> {
                try {
                    LocalDateTime searchDate = LocalDateTime.parse(flightData, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    filteredFlights = flightsObsList.stream()
                            .filter(f -> f.getDepartureTime().equals(searchDate))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                } catch (DateTimeParseException e) {
                    MessageUtils.showError("Formato de fecha incorrecto");
                    return;
                }
            }
        }
        if (!filteredFlights.isEmpty()) {
            idVuelosTableView.setItems(filteredFlights);
            idUpdateFlightButton.setDisable(false);
            idVuelosTableView.getSelectionModel().selectedItemProperty().addListener(this::handleTableSelection);
            idFlightIsExists.setVisible(false);
        }else {
            MessageUtils.showMessage("No se encontraron vuelos");
        }
    }

    /**
     * Maneja la selección de un vuelo en la tabla. Si hay un vuelo seleccionado
     * y el botón de actualización está habilitado, implementa la función de selección de los campos de entrada
     * y se deshabilita el botón agregar.
     * @param observable el valor observable del vuelo seleccionado.
     * @param oldValue el valor previo seleccionado (puede ser null).
     * @param newValue el nuevo valor seleccionado (puede ser null).
     */
    private void handleTableSelection(ObservableValue<? extends Flight> observable, Flight oldValue, Flight newValue) {
        if (newValue != null && !idUpdateFlightButton.isDisable()) {
            selectFlightFromTable();
            idAddButton.setDisable(true);
        }
    }

    /**
     * Extrae los valores del vuelo seleccionado en la tabla y los asigna a los campos de entrada
     * para su edición.
     */
    private void selectFlightFromTable() {
        Flight flight = idVuelosTableView.getSelectionModel().getSelectedItem();
        if (flight != null) {
            idFlightNumberTextField.setText(flight.getFlightNumber());
            idDestinationTextField.setText(flight.getDestination());
            idDepartureTextField.setText(flight.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            idDurationTextField.setText(flight.getDuration().format(DateTimeFormatter.ofPattern("H:mm")));
        }
    }

    /**
     * Asociado a la acción de clic en el botón "Update Flight".
     * Actualiza la información de un vuelo seleccionado en la tabla con los datos proporcionados en los campos de entrada.
     * Verifica que se haya seleccionado un vuelo para actualizar. Sí no se ha seleccionado, muestra un mensaje de error.
     * Verifica que los campos de entrada no estén vacíos y que los datos sean correctos.
     * Revisa si los valores actualizados sean diferentes al vuelo seleccionado en la tabla. Si son iguales,
     * muestra un mensaje de información y no realiza la actualización, consulta si seguir con la actualización.
     * Revisa si el vuelo actualizado ya existe en la lista de vuelos. Si lo está, muestra un mensaje de error.
     * Muestra un alert para confirmar la actualización.
     * Actualiza el vuelo, guarda la lista de vuelos en el archivo y restablece los valores de la vista inicial.
     */
    @FXML
    public void updateFlight() {
        Flight selectedFlight = idVuelosTableView.getSelectionModel().getSelectedItem();
        if ( selectedFlight == null) {
            MessageUtils.showError("Ningún vuelo se ha seleccionado para actualizar");
            return;
        }

        // capturar los datos desde los campos de entrada
        String flightNumberText = idFlightNumberTextField.getText();
        String destinationText = idDestinationTextField.getText();
        String departureTimeText = idDepartureTextField.getText();
        String durationText = idDurationTextField.getText();

        if (validateFieldsEmpty(listFields())) return;

        LocalDateTime departureTimeNew = validateDepartureTime(departureTimeText);
        if (departureTimeNew == null) return;

        LocalTime durationNew = validateDuration(durationText);
        if (durationNew == null) return;

        Flight updateFlight = new Flight(flightNumberText, destinationText, departureTimeNew, durationNew);

        //verificar si el vuelo seleccionado es el mismo que el actual
        if (selectedFlight.equals(updateFlight)) {
            MessageUtils.showMessage("Los datos ingresados son iguales al vuelo seleccionado, no hay información para actualizar");
            if (!MessageUtils.showConfirmation("¿Desea seguir editando el vuelo?")){
                resetToInitialState();
            }
        } else {
            if (flightsObsList.contains(updateFlight)) {
                MessageUtils.showError("El vuelo ingresado ya existe en la lista de vuelos");
                return;
            }
            boolean confirmUpdate = MessageUtils.showConfirmation(String.format("¿Desea actualizar el vuelo?\n Vuelo anterior: %s\n Vuelo nuevo: %s", selectedFlight, updateFlight));
            if (confirmUpdate) {
                int index = flightsObsList.indexOf(selectedFlight);
                flightsObsList.set(index, updateFlight);
                try {
                    FileUtils.saveFlightsToFile(flightsObsList);
                    resetToInitialState();
                }catch (Exception e) {
                    //logger.log(Level.SEVERE, "No se pudo actualizar el vuelo: " + updateFlight, e);
                    MessageUtils.showError("No se pudo actualizar el vuelo.");
                }
            }
        }
    }

    /**
     * Método auxiliar
     * Establece la vista inicial de la aplicación, definiendo valores predeterminados a botones, campos de entrada y choices.
     */
    private void resetToInitialState() {
        idVuelosTableView.setItems(flightsObsList);
        idFiltersChoiceBox.setValue("Show all flights");
        idOptionSearchChoiceBox.setValue("Flight number");
        idAddButton.setDisable(false);
        idDeleteButton.setDisable(true);
        idUpdateFlightButton.setDisable(true);

        idSearchTextField.clear();
        clearFields();

        idVuelosTableView.getSelectionModel().selectedItemProperty().removeListener(this::handleTableSelection);
        idVuelosTableView.getSelectionModel().clearSelection();
    }

    /**
     * Maneja el evento de cierre de la ventana. Guarda la lista de vuelos en un archivo
     * antes de cerrar la aplicación.
     * @param event el evento de cierre de ventana.
     */
    @FXML
    public void handleWindowClose(WindowEvent event) {
        try {
            FileUtils.saveFlightsToFile(flightsObsList);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar la lista de vuelos al cerrar la aplicación", e);
            MessageUtils.showError("Error al guardar la lista de vuelos.");
        }
        event.consume();
    }

    /**
     * Obtiene la lista observable de vuelos.
     * @return la lista observable de vuelos.
     */
    public ObservableList<Flight> getFlightsObsList() {return flightsObsList;}

    /**
     * Maneja el evento de clic en el botón "Chart" y permite ir a la vista del gráfico.
     * Cambia la escena actual de la ventana a la definida en el archivo FXML de la vista principal.
     * Muestra un gráfico con los vuelos cargados si la lista de vuelos no está vacía.
     * @param event el evento de acción que activa la vista del gráfico.
     * @throws Exception sí ocurre algún error al cargar la vista del gráfico.
     */
    @FXML
    public void showChart(ActionEvent event) throws Exception {
        if (flightsObsList.isEmpty()) {
            MessageUtils.showMessage("No se han cargado vuelos para mostrar el gráfico");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/milacanete/flightsfx/FXMLChartView.fxml"));
        Parent view1 = loader.load();
        Scene view1Scene = new Scene(view1);
        view1Scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/CSS/style.css")).toExternalForm());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.hide();
        stage.setScene(view1Scene);
        stage.show();

    }
}


