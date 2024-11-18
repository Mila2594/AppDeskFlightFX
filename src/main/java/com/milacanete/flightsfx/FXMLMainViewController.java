package com.milacanete.flightsfx;

import com.milacanete.flightsfx.model.Flight;
import com.milacanete.flightsfx.utils.FileUtils;
import com.milacanete.flightsfx.utils.MessageUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
 * Proporciona la lógica para manejar eventos, aplicar filtros,
 * agregar, eliminar y actualizar vuelos en la tabla.
 */
public class FXMLMainViewController {

    @FXML
    public Button idAddButton;

    @FXML
    public Button idApplyFilterButton;

    @FXML
    public Label idFlightIsExists;

    @FXML
    public Label idProhibitedCharacter;

    @FXML
    public Button idSearchFlightButton;

    @FXML
    public TextField idSearchTextField;

    @FXML
    public Button idUpdateFlightButton;

    @FXML
    public ChoiceBox<String> idOptionSearchChoiceBox;

    @FXML
    public Button idChartViewButton;

    @FXML
    private Button idDeleteButton;

    @FXML
    private TableColumn<Flight, String> idDepartureColumn;

    @FXML
    private TextField idDepartureTextField;

    @FXML
    private TableColumn<Flight, String> idDestinationColumn;

    @FXML
    private TextField idDestinationTextField;

    @FXML
    private TableColumn<Flight, LocalTime> idDurationColumn;

    @FXML
    private TextField idDurationTextField;

    @FXML
    private ChoiceBox<String> idFiltersChoiceBox;

    @FXML
    private TableColumn<Flight, String> idFlightNumberColumn;

    @FXML
    private TextField idFlightNumberTextField;

    @FXML
    private TableView<Flight> idVuelosTableView;

    @FXML
    private SplitPane rootSplitPane;

    @FXML
    private ObservableList<Flight> flightsObsList;

    private static final Logger logger = Logger.getLogger(FXMLMainViewController.class.getName());

    /**
     * Método de inicialización que configura los listeners, carga los datos iniciales
     * y establece los valores predeterminados de los ChoiceBoxes.
     */
    public void initialize() {

        if (rootSplitPane.getScene() != null) {
            Stage stage = (Stage) rootSplitPane.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        }

        //listener para desactivar botón agregar si el botón actualizar se activa
        ChangeListener<Boolean> listenerAddButton = (_, _, _) -> {
            boolean isUpdating = !idUpdateFlightButton.isDisable();
            boolean prohibitedCharacter = idProhibitedCharacter.isVisible();
            boolean flightIsExists = idFlightIsExists.isVisible();
            idAddButton.setDisable(isUpdating || prohibitedCharacter || flightIsExists);
        };

        idUpdateFlightButton.disableProperty().addListener(listenerAddButton);
        idProhibitedCharacter.visibleProperty().addListener(listenerAddButton);
        idFlightIsExists.visibleProperty().addListener(listenerAddButton);

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
        idFlightNumberTextField.textProperty().addListener((_, _, _) -> {
            if (idUpdateFlightButton.isDisable()) {
                formatFlightNumber(idFlightNumberTextField);
            }
        });

        resetToInitialState();
    }

    /**
     * Obtiene los campos de entrada del formulario.
     * @return Una lista con los TextFields para el número de vuelo, destino, hora de salida y duración.
     */
    private List<TextField> listFields() {
        return List.of(idFlightNumberTextField, idDestinationTextField, idDepartureTextField, idDurationTextField);
    }

    /**
     * Configura las restricciones para caracteres prohibidos en los campos de texto.
     * Carácter prohibido: ";"
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
     * @param numberTextField Campo de texto del número de vuelo.
     */
    private void formatFlightNumber(TextField numberTextField) {
        numberTextField.textProperty().addListener((_, _, newText) -> {

            String formattedText = newText.toUpperCase();
            boolean isValid = formattedText.matches("[A-Z0-9]*");
            if (isValid) {
                numberTextField.setText(formattedText);  // Aplica el texto formateado
                idProhibitedCharacter.setVisible(false);  // Oculta el mensaje de error
                idAddButton.setDisable(false);
            } else {
                idProhibitedCharacter.setVisible(true);  // Muestra el mensaje de error
                idAddButton.setDisable(true);
            }
        });
    }

    /**
     * Verifica si el vuelo ya existe en la lista.
     */
    private void restringFlightExists() {
        try {
            // Convertir los valores ingresados
            LocalDateTime departureTime = LocalDateTime.parse(idDepartureTextField.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            LocalTime duration = LocalTime.parse(idDurationTextField.getText(), DateTimeFormatter.ofPattern("H:mm"));
            boolean flightExists = validateFlightExists(idFlightNumberTextField.getText(), idDestinationTextField.getText(), departureTime, duration);
            idFlightIsExists.setVisible(flightExists);
        } catch (Exception e) {
            idFlightIsExists.setVisible(false);
        }
    }

    /**
     * Asociado a la acción del botón "Add".
     * Agrega un nuevo vuelo a la tabla y a la lista de vuelos observables.
     * Este método valida los campos de entrada para asegurarse de que no estén vacíos y que los datos sean correctos
     * (como el formato de la fecha de salida y la duración del vuelo). Si los datos son válidos, crea un nuevo objeto
     * {@code Flight}, lo agrega a la lista de vuelos observables y guarda la lista en un archivo.
     */
    @FXML
    void addFlight() {
        String flightNumber = idFlightNumberTextField.getText();
        String destination = idDestinationTextField.getText();
        String departureTimeText = idDepartureTextField.getText();
        String durationText = idDurationTextField.getText();

        //validar campos vacíos
        if (validateFieldsEmpty(flightNumber, destination, departureTimeText,durationText)) return;

        //validar formato de fecha de partida
        LocalDateTime departureTime = validateDepartureTime(departureTimeText);
        if (departureTime == null) return;

        //validar formato de duración
        LocalTime duration = validateDuration(durationText);
        if (duration == null) return;

        //validar si el vuelo ya existe
        if (validateFlightExists(flightNumber, destination, departureTime, duration)) return;

        //crear nuevo vuelo, agregarlo a la lista y a la tabla
        Flight newFlight = new Flight(flightNumber, destination, departureTime, duration);
        flightsObsList.add(newFlight);

        //uso de try catch para validar que se guardó el vuelo en el fichero y registrarlo en el log
        try {
            FileUtils.saveFlightsToFile(flightsObsList); //guardar vuelo en el fichero
            clearFields();  //limpiar campos
            idFlightNumberTextField.requestFocus();
            logger.info("Vuelo guardado: " + newFlight); // registrar vuelo en el log
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar vuelo", e);
            MessageUtils.showError("Error al guardar vuelo");
        }
    }

    /**
     * Válida si los campos de entrada están vacíos.
     * @param flightNumber  Número de vuelo.
     * @param destination   Destino del vuelo.
     * @param departureTime Hora de partida.
     * @param duration      Duración del vuelo.
     * @return true si algún campo está vacío, de lo contrario false.
     */
    private boolean validateFieldsEmpty(String flightNumber, String destination, String departureTime, String duration) {
        List<String> fields = List.of(flightNumber, destination, departureTime, duration);
        if (fields.stream().anyMatch(String::isEmpty)) {
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
            return LocalTime.parse(durationText, formatter);
        } catch (Exception ex) {
            MessageUtils.showError("La duración debe tener el formato hh:mm");
            return null;
        }
    }

    /**
     * Verifica si un vuelo ya existe en la lista observable de vuelos.
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
     * Limpia los campos de entrada después de agregar un vuelo.
     */
    private void clearFields() {
        idFlightNumberTextField.clear();
        idDestinationTextField.clear();
        idDepartureTextField.clear();
        idDurationTextField.clear();
    }

    /**
     * Asociado a la acción de clic en el botón "Delete".
     * Elimina el vuelo seleccionado de la tabla y la lista de vuelos observables.
     * Este método permite al usuario eliminar un vuelo previamente seleccionado en la tabla de vuelos. Antes de proceder
     * con la eliminación, se solicita una confirmación al usuario. Si el vuelo es eliminado con éxito, la lista de vuelos
     * se guarda en el archivo actualizado. Si ocurre un error durante la eliminación, se muestra un mensaje de error.
     */
    @FXML
    public void deleteFlight( ) {
        Flight flight = idVuelosTableView.getSelectionModel().getSelectedItem();
        if (!confirmDeleteFlight(flight)) {
            logger.info("Eliminación cancelada por el usuario.");
            return;
        }
        try {
            idVuelosTableView.getItems().remove(flight);// eliminar vuelo de la tabla
            flightsObsList.remove(flight); // eliminar vuelo de la lista
            idVuelosTableView.getSelectionModel().clearSelection(); // limpiar campos
            FileUtils.saveFlightsToFile(flightsObsList); // guardar lista de vuelos actualizada en el fichero
            idFlightNumberTextField.requestFocus(); //poner el foco en el field flightNumber
            logger.info("Vuelo eliminado: " + flight);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se pudo eliminar el vuelo: " + flight, e);
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
     * Aplica un filtro a la tabla de vuelos basado en la opción seleccionada.
     * Este método permite al usuario aplicar un filtro predefinido a la tabla de vuelos. Los filtros disponibles incluyen:
     * mostrar todos los vuelos, mostrar vuelos a una ciudad seleccionada, mostrar vuelos largos, mostrar los próximos 5 vuelos
     * o mostrar el promedio de duración de los vuelos. Si no hay vuelos para filtrar, se muestra un mensaje de error.
     */
    @FXML
    public void applyFilter() {

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
                idFiltersChoiceBox.setValue("show all flights");
                showAllFlights();
            }
        }

        if (idVuelosTableView.getItems().isEmpty()) {
            idFiltersChoiceBox.setValue("Show all flights");
            showAllFlights();
        }
    }

    /**
     * Restaura la tabla a su estado inicial, mostrando todos los vuelos.
     */
    //filtro "Show all flights"
    private void showAllFlights() {
        idVuelosTableView.setItems(flightsObsList);
        idFlightNumberTextField.requestFocus();
    }

    /**
     * Muestra los vuelos hacia la ciudad seleccionada en la tabla.
     */
    private void showFlightsToSelectedCity() {
        Flight flight = idVuelosTableView.getSelectionModel().getSelectedItem();
        if (flight == null) {
            MessageUtils.showError("Selecciona un vuelo para filtrar por la ciudad destino");
            return;
        }

        String selectedCity = flight.getDestination();
        ObservableList<Flight> filteredFlights = flightsObsList.stream()
                .filter(f -> f.getDestination().equals(selectedCity))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        idVuelosTableView.setItems(filteredFlights);
        idVuelosTableView.getSelectionModel().clearSelection();

    }

    /**
     * Muestra los vuelos con una duración mayor a 3 horas (180 minutos).
     */
    private void showLongFlights() {
        ObservableList<Flight> filteredFlights = flightsObsList.stream()
                .filter(flightFiltered -> flightFiltered.getDuration().getHour() * 60
                        + flightFiltered.getDuration().getMinute() > 180)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        idVuelosTableView.setItems(filteredFlights);
    }

    /**
     * Muestra los próximos 5 vuelos según la hora de salida.
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
     */
    private void showFlightDurationAverage() {
        //convertir duraciones en minutos y promediarlos para obtener la duración media en minutos
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
            MessageUtils.showError("No se pudo calcular la duración promedio, porque la lista de vuelos esta vacia");
        }
    }

    /**
     * Asociado a la acción de clic en el botón "Search".
     * Busca vuelos basados en la opción seleccionada y el texto ingresado.
     * Este método realiza una búsqueda de vuelos en la lista de vuelos observables según el texto proporcionado por el usuario
     * y la opción seleccionada (por ejemplo, buscar por número de vuelo, destino o hora de salida). Si el campo de búsqueda
     * está vacío, se muestra un mensaje de error.
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
     * Filtra los vuelos basados en el criterio de búsqueda.
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
     * y el botón de actualización no está deshabilitado, se selecciona el vuelo
     * y se deshabilita el botón de añadir.
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
     * Este método permite al usuario actualizar un vuelo previamente seleccionado en la tabla. Se valida si los campos
     * de entrada están vacíos o si el formato de los datos es incorrecto. Si los datos son válidos, se verifica si el vuelo
     * actualizado ya existe en la lista de vuelos. Si no existe, se actualiza el vuelo y se guarda la lista en el archivo.
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

        if (validateFieldsEmpty(flightNumberText, destinationText, departureTimeText, durationText)) return;

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
                    logger.info("Vuelo actualizado: " + updateFlight);
                    resetToInitialState();
                }catch (Exception e) {
                    logger.log(Level.SEVERE, "No se pudo actualizar el vuelo: " + updateFlight, e);
                    MessageUtils.showError("No se pudo actualizar el vuelo.");
                }
            }
        }
    }

    /**
     * Restaura el estado inicial de la aplicación, limpiando los campos de entrada,
     * actualizando la tabla y deshabilitando ciertos botones.
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
     * @throws Exception si ocurre algún error al cargar la vista del gráfico.
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


