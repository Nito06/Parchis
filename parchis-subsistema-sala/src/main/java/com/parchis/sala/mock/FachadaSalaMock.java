package com.parchis.sala.mock;

import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import com.parchis.excepciones.ColorOcupadoException;
import com.parchis.excepciones.ParchisException;
import com.parchis.excepciones.SalaLlenaException;
import com.parchis.excepciones.SalaNoEncontradaException;
import com.parchis.interfaces.IFachadaSala;
import com.parchis.interfaces.ISalaListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación Mock de la Fachada de Sala para el Hito 2A.
 * Almacena el estado de las salas en memoria y simula tanto el flujo básico
 * como los flujos alternos A2 (Sala no encontrada) y A3 (Color ocupado).
 */
public class FachadaSalaMock implements IFachadaSala {

    private static FachadaSalaMock instancia;
    private final Map<String, SalaDTO> salasEnMemoria = new ConcurrentHashMap<>();
    private final List<ISalaListener> listeners = new ArrayList<>();
    private int contadorSalas = 100;

    public static synchronized FachadaSalaMock getInstance() {
        if (instancia == null) {
            instancia = new FachadaSalaMock();
        }
        return instancia;
    }

    private FachadaSalaMock() {
        // Datos iniciales de prueba en memoria para probar de inmediato
        SalaDTO salaDemo = new SalaDTO("SALA-777", 4);
        JugadorDTO anfitrion = new JugadorDTO("JUG-1", "Carlos", "Rojo", true);
        salaDemo.getJugadores().add(anfitrion);
        salasEnMemoria.put(salaDemo.getCodigo(), salaDemo);
    }

    @Override
    public SalaDTO crearSala(JugadorDTO anfitrion, int capacidadMaxima) throws ParchisException {
        if (anfitrion == null) {
            throw new ParchisException("El jugador anfitrión no puede ser nulo.");
        }
        if (capacidadMaxima < 2 || capacidadMaxima > 4) {
            throw new ParchisException("La capacidad de la sala debe ser entre 2 y 4 jugadores.");
        }

        contadorSalas++;
        String codigoSala = "SALA-" + contadorSalas;
        SalaDTO nuevaSala = new SalaDTO(codigoSala, capacidadMaxima);
        anfitrion.setAnfitrion(true);
        nuevaSala.getJugadores().add(anfitrion);

        salasEnMemoria.put(codigoSala, nuevaSala);

        notificarJugadorUnido(anfitrion, nuevaSala);
        return nuevaSala;
    }

    @Override
    public SalaDTO unirseASala(String codigoSala, JugadorDTO jugador)
            throws SalaNoEncontradaException, ColorOcupadoException, SalaLlenaException, ParchisException {

        // Simulación explícita o detección de Alterno A2: Sala no encontrada
        if (codigoSala == null || codigoSala.trim().isEmpty() || "ERROR_A2".equalsIgnoreCase(codigoSala)) {
            throw new SalaNoEncontradaException(codigoSala != null ? codigoSala : "");
        }

        SalaDTO sala = salasEnMemoria.get(codigoSala.trim().toUpperCase());
        if (sala == null) {
            throw new SalaNoEncontradaException(codigoSala);
        }

        // Alterno A1: Sala llena
        if (sala.estaLlena()) {
            throw new SalaLlenaException(codigoSala);
        }

        // Simulación explícita o detección de Alterno A3: Color ya ocupado
        if ("ERROR_A3".equalsIgnoreCase(jugador.getColor()) || sala.tieneColorOcupado(jugador.getColor())) {
            throw new ColorOcupadoException(jugador.getColor());
        }

        // Flujo exitoso: agregar jugador
        jugador.setAnfitrion(false);
        sala.getJugadores().add(jugador);

        notificarJugadorUnido(jugador, sala);
        return sala;
    }

    @Override
    public SalaDTO consultarSala(String codigoSala) throws SalaNoEncontradaException {
        if (codigoSala == null || !salasEnMemoria.containsKey(codigoSala)) {
            throw new SalaNoEncontradaException(codigoSala);
        }
        return salasEnMemoria.get(codigoSala);
    }

    @Override
    public boolean iniciarPartida(String codigoSala) throws ParchisException {
        SalaDTO sala = salasEnMemoria.get(codigoSala);
        if (sala == null) {
            throw new SalaNoEncontradaException(codigoSala);
        }
        if (sala.getJugadores().size() < 2) {
            throw new ParchisException("Se requieren al menos 2 jugadores para iniciar la partida.");
        }

        sala.setEnJuego(true);
        notificarPartidaIniciada(sala);
        return true;
    }

    @Override
    public void registrarListener(ISalaListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notificarJugadorUnido(JugadorDTO jugador, SalaDTO sala) {
        for (ISalaListener l : listeners) {
            l.onJugadorUnido(jugador, sala);
        }
    }

    private void notificarPartidaIniciada(SalaDTO sala) {
        for (ISalaListener l : listeners) {
            l.onPartidaIniciada(sala);
        }
    }
}
