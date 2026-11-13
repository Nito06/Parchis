package com.parchis.eventos;

import com.parchis.dtos.EventoDTO;
import com.parchis.dtos.MovimientoDTO;
import com.parchis.dtos.TipoEvento;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventosJuegoTest {

    private static EventBroker broker;
    private static final int PUERTO_TEST = 9998;

    @BeforeAll
    public static void iniciar() {
        broker = new EventBroker(PUERTO_TEST);
        broker.iniciar();
    }

    @AfterAll
    public static void detener() {
        if (broker != null) {
            broker.detener();
        }
    }

    @Test
    public void testSincronizacionMovimientoYDado() throws InterruptedException {
        EventClient host = new EventClient("localhost", PUERTO_TEST);
        EventClient cliente = new EventClient("localhost", PUERTO_TEST);

        assertTrue(host.conectar(), "Host debe conectar");
        assertTrue(cliente.conectar(), "Cliente debe conectar");

        CountDownLatch latchDado = new CountDownLatch(1);
        CountDownLatch latchMov = new CountDownLatch(1);
        AtomicInteger dadoRecibido = new AtomicInteger(0);
        AtomicBoolean movRecibido = new AtomicBoolean(false);

        cliente.suscribir(evento -> {
            if (evento.getTipo() == TipoEvento.DADO_LANZADO) {
                dadoRecibido.set((int) evento.getPayload());
                latchDado.countDown();
            } else if (evento.getTipo() == TipoEvento.FICHA_MOVIDA) {
                MovimientoDTO mov = (MovimientoDTO) evento.getPayload();
                if (mov.getJugador() == 0 && mov.getIdFicha() == 1 && mov.getPasos() == 6) {
                    movRecibido.set(true);
                    latchMov.countDown();
                }
            }
        });

        // 1. Host publica dado lanzado = 6
        host.publicarEvento(new EventoDTO(TipoEvento.DADO_LANZADO, "Host", "SALA-01", 6));
        assertTrue(latchDado.await(3, TimeUnit.SECONDS), "El cliente debe recibir el dado");
        assertEquals(6, dadoRecibido.get());

        // 2. Host publica movimiento de ficha (jugador=0, ficha=1, pasos=6)
        host.publicarEvento(new EventoDTO(TipoEvento.FICHA_MOVIDA, "Host", "SALA-01", new MovimientoDTO(0, 1, 6)));
        assertTrue(latchMov.await(3, TimeUnit.SECONDS), "El cliente debe recibir el movimiento de ficha");
        assertTrue(movRecibido.get());

        host.desconectar();
        cliente.desconectar();
    }
}
