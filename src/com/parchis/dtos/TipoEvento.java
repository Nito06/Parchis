package com.parchis.dtos;

import java.io.Serializable;

/**
 * Tipos de eventos para la arquitectura basada en eventos.
 */
public enum TipoEvento implements Serializable {
    MENSAJE_CHAT,
    SALA_CREADA,
    JUGADOR_UNIDO,
    JUGADOR_LISTO,
    PARTIDA_INICIADA,
    DADO_LANZADO,
    FICHA_MOVIDA,
    FICHA_COMIDA,
    TURNO_CAMBIADO,
    PARTIDA_FINALIZADA,
    ERROR
}
