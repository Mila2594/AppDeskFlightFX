package com.milacanete.flightsfx.utils;

import com.milacanete.flightsfx.model.Flight;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Clase encargada de gestionar la persistencia de la información de vuelos,
 * Esta clase proporciona métodos para leer y guardar vuelos desde y hacia el archivo "flights.txt".
 *  * <p> Utiliza la clase {@link Flight} para representar los vuelos,
 *  contienen información como el número de vuelo, destino, fecha y hora de salida y duración.
 */

public class FileUtils {

    // Crea un logger para la clase
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    /**
     * Obtiene la ruta absoluta del archivo flights.txt ubicado en el directorio principal del proyecto.
     * @return la ruta del archivo flights.txt.
     */
    private static Path getFlightsFile() {
        return Paths.get(System.getProperty("user.dir"),"flights.txt");
    }

    /**
     * Lee el contenido del archivo flights.txt y convierte cada línea en un objeto {@link Flight}.
     * En caso de error en una línea específica, se registra una advertencia y se omite dicha línea.
     * @return una lista de objetos {@link Flight} cargados desde el archivo.
     * Si el archivo no existe u ocurre un error, se devuelve una lista vacía.
     */
    private static List<Flight> loadFlights() {
        Path filePath = getFlightsFile();

        if (!Files.exists(filePath)){
            System.out.printf("Error: fichero %s no encontrado",filePath.getFileName().toString());
            return Collections.emptyList();
        }

        //try con recurso, se lee el archivo y se convierte en un stream
        try(Stream<String> lines = Files.lines(filePath)){
            return lines
                    .map(line -> {
                        String[] parts = line.split(";");
                        if (parts.length != 4) return null;
                        try {
                            return new Flight(
                                    parts[0], //flight_number
                                    parts[1], //destination
                                    LocalDateTime.parse(parts[2], DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), //departure_time
                                    LocalTime.parse(parts[3], DateTimeFormatter.ofPattern("H:mm")) //duration
                                    );
                        } catch (Exception ex) {
                            logger.log(Level.WARNING, "Error al parsear una línea: " + line, ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al leer el archivo \"" + filePath.getFileName().toString() + "\"", ex);
            return Collections.emptyList();
        }

    }

    /**
     * Método público que permite acceder a la lista de vuelos almacenada en el archivo flights.txt.
     * @return una lista de objetos {@link Flight}.
     * Si ocurre un error o el archivo no existe, se devuelve una lista vacía.
     */
    public static List<Flight> getFlights() {
        return loadFlights();
    }

    /**
     * Guarda una lista de vuelos en el archivo flights.txt. Convierte los atributos de cada vuelo
     * al formato adecuado antes de escribirlos:
     * Fecha y hora de salida: formato "dd/MM/yyyy HH:mm".
     * Duración: formato "H:mm".
     * @param flights lista de vuelos a guardar en el archivo.
     */
    private static void saveFlights(List<Flight> flights) {
        Path filePath = getFlightsFile();

        //try con recurso, se abre el archivo para escritura con un PrintWriter
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            flights.forEach(flight -> {
                    String formatter = String.format("%s;%s;%s;%s",
                            flight.getFlightNumber(),
                            flight.getDestination(),
                            flight.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            flight.getDuration().format(DateTimeFormatter.ofPattern("H:mm"))); //formato es hh:mm, para el patrón se usa H
                    writer.println(formatter);
            });

        }catch (Exception ex) {
            logger.log(Level.SEVERE, "Error al guardar datos en  \"" + filePath.getFileName().toString() + "\"", ex);
        }
    }

    /**
     * Método público que permite guardar una lista de vuelos en el archivo flights.txt.
     * @param flights lista de vuelos a guardar.
     */
    public static void saveFlightsToFile(List<Flight> flights) {
        saveFlights(flights);
    }
}
