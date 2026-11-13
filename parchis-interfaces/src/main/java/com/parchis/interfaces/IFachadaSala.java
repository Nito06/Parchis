package com.parchis.interfaces;

import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import com.parchis.excepciones.ColorOcupadoException;
import com.parchis.excepciones.ParchisException;
import com.parchis.excepciones.SalaLlenaException;
import com.parchis.excepciones.SalaNoEncontradaException;

/**
 * Contrato principal de la fachada del subsistema Sala.
 * Define las operaciones requeridas por el CU-01 (Crear / Unirse a Sala)
 * y la coordinación de la partida.
 */
public interface IFachadaSala {

    /**
     * Crea una nueva sala con un anfitrión y capacidad designada.
     */
    SalaDTO crearSala(JugadorDTO anfitrion, int capacidadMaxima) throws ParchisException;

    /**
     * Une a un jugador a una sala existente validando disponibilidad y color.
     * Puede lanzar SalaNoEncontradaException (Alterno A2), ColorOcupadoException (Alterno A3)
     * o SalaLlenaException (Alterno A1).
     */
    SalaDTO unirseASala(String codigoSala, JugadorDTO jugador)
            throws SalaNoEncontradaException, ColorOcupadoException, SalaLlenaException, ParchisException;

    /**
     * Consulta el estado actual de una sala.
     */
    SalaDTO consultarSala(String codigoSala) throws SalaNoEncontradaException;

    /**
     * Inicia formalmente la partida en la sala.
     */
    boolean iniciarPartida(String codigoSala) throws ParchisException;

    /**
     * Registra un observador/listener para recibir eventos de la sala.
     */
    void registrarListener(ISalaListener listener);
}
