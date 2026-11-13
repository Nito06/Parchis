package com.parchis.interfaces;

import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;

/**
 * Interfaz para escuchar los eventos notificados por la sala.
 */
public interface ISalaListener {

    void onJugadorUnido(JugadorDTO nuevoJugador, SalaDTO salaActualizada);

    void onPartidaIniciada(SalaDTO sala);

    void onError(String mensajeError);
}
