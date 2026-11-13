package com.parchis.excepciones;

/**
 * Excepción lanzada en el Flujo Alterno A3 del CU-01:
 * Cuando un jugador intenta seleccionar un color que ya fue tomado por otro integrante de la sala.
 */
public class ColorOcupadoException extends ParchisException {
    public ColorOcupadoException(String color) {
        super("Flujo Alterno A3: El color '" + color + "' ya ha sido seleccionado por otro jugador en esta sala.");
    }
}
