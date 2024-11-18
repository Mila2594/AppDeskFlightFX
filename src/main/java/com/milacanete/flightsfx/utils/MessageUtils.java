package com.milacanete.flightsfx.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * La clase {@code MessageUtils} proporciona métodos estáticos para mostrar alertas de diferentes tipos
 * (error, información, confirmación, advertencia) al usuario. Estos métodos utilizan las alertas
 * de JavaFX para mostrar mensajes en cuadros de diálogo modales.
 * Esta clase se utiliza para centralizar la lógica de presentación de mensajes y mantener el código
 * limpio y reutilizable.
 */
public class MessageUtils {

    /**
     * Muestra un cuadro de diálogo de error con el mensaje proporcionado.
     * @param message el mensaje que se mostrará en el cuadro de diálogo de error.
     */
    public static void showError(String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("Error");
        dialog.setHeaderText("Error");
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo de información con el mensaje proporcionado.
     * @param message el mensaje que se mostrará en el cuadro de diálogo de información.
     */
    public static void showMessage(String message) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Información");
        dialog.setHeaderText("Información");
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    /**
     * Muestra un cuadro de diálogo de confirmación con el mensaje proporcionado.
     * El método espera una respuesta del usuario y devuelve {@code true} si el usuario
     * hace clic en el botón "OK", o {@code false} si hace clic en "Cancelar" o cierra el cuadro de diálogo.
     * @param message el mensaje que se mostrará en el cuadro de diálogo de confirmación.
     * @return {@code true} si el usuario confirma la acción, {@code false} si la cancela.
     */
    public static boolean showConfirmation(String message) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Confirmación");
        dialog.setHeaderText("Confirmación");
        dialog.setContentText(message);
        return dialog.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    /**
     * Muestra un cuadro de diálogo de advertencia con el mensaje proporcionado.
     * @param message el mensaje que se mostrará en el cuadro de diálogo de advertencia.
     */
    public static void showWarning(String message) {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("Advertencia");
        dialog.setHeaderText("Advertencia");
        dialog.setContentText(message);
        dialog.showAndWait();
    }


}
