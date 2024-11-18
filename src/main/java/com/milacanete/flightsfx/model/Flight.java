package com.milacanete.flightsfx.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Representa un vuelo con información sobre el número de vuelo, destino, hora de salida y duración.
 */
public class Flight {

    private String flightNumber;
    private String destination;
    private LocalDateTime departureTime;
    private LocalTime duration;

    /**
     * Constructor que inicializa el vuelo solo con su número.
     * @param flightNumber número del vuelo.
     */
    public Flight(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    /**
     * Constructor que inicializa el vuelo con todos sus atributos.
     * @param flightNumber número del vuelo.
     * @param destination destino del vuelo.
     * @param departureTime hora y fecha de salida del vuelo.
     * @param duration duración del vuelo.
     */
    public Flight(String flightNumber, String destination, LocalDateTime departureTime, LocalTime duration) {
        this.flightNumber = flightNumber;
        this.destination = destination;
        this.departureTime = departureTime;
        this.duration = duration;
    }

    /**
     * Obtiene el número del vuelo.
     * @return número del vuelo.
     */
    public String getFlightNumber() {
        return flightNumber;
    }

    /**
     * Obtiene el destino del vuelo.
     * @return destino del vuelo.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Obtiene la hora y fecha de salida del vuelo.
     * @return hora y fecha de salida del vuelo.
     */
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Obtiene la duración del vuelo.
     * @return duración del vuelo.
     */
    public LocalTime getDuration() {
        return duration;
    }

    /**
     * Determina si dos objetos Flight son iguales basándose en su número de vuelo, destino,
     * hora de salida y duración. La comparación del destino no es sensible a mayúsculas.
     * @param o objeto a comparar.
     * @return {@code true} si ambos vuelos son iguales, de lo contrario {@code false}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flight flight = (Flight) o;

        return Objects.equals(this.flightNumber, flight.flightNumber) &&
                Objects.equals(this.destination.toUpperCase(), flight.destination.toUpperCase()) &&
                Objects.equals(this.departureTime, flight.departureTime) &&
                Objects.equals(this.duration, flight.duration);
    }

    /**
     * Representa el vuelo como una cadena en el siguiente formato:
     * "número de vuelo; destino; fecha y hora de salida; duración".
     * @return una cadena con la información del vuelo.
     */
    @Override
    public String toString() {
        return flightNumber + ';' + destination + ';' + departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ';' + duration;
    }
}
