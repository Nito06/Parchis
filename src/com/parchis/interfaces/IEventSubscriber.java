package com.parchis.interfaces;

import com.parchis.dtos.EventoDTO;

/**
 * Suscriptor de eventos en el estilo arquitectónico basado en eventos.
 */
public interface IEventSubscriber {

    void onEventoRecibido(EventoDTO evento);
}
