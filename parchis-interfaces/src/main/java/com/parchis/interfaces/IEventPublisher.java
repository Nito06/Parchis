package com.parchis.interfaces;

import com.parchis.dtos.EventoDTO;

/**
 * Publicador de eventos en el estilo arquitectónico basado en eventos.
 */
public interface IEventPublisher {

    void publicarEvento(EventoDTO evento);
}
