package com.parchis.sala.mock;

import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import com.parchis.excepciones.ColorOcupadoException;
import com.parchis.excepciones.SalaLlenaException;
import com.parchis.excepciones.SalaNoEncontradaException;
import com.parchis.interfaces.IFachadaSala;
import com.parchis.sala.fabrica.FabricaSala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias formales para el CU-01 (Administrar Sala).
 * Valida el Flujo Básico y los Flujos Alternos A2 y A3 requeridos por el manual.
 */
public class FachadaSalaMockTest {

    private IFachadaSala fachada;

    @BeforeEach
    public void setUp() {
        // Obtenida ÚNICAMENTE a través de la fábrica
        this.fachada = FabricaSala.getFachada();
    }

    @Test
    @DisplayName("CU-01 Flujo Básico: Crear Sala exitosamente")
    public void testCrearSalaExito() throws Exception {
        JugadorDTO anfitrion = new JugadorDTO("J1", "Hugo", "Rojo", true);
        SalaDTO sala = fachada.crearSala(anfitrion, 4);

        assertNotNull(sala);
        assertNotNull(sala.getCodigo());
        assertEquals(4, sala.getCapacidadMaxima());
        assertEquals(1, sala.getJugadores().size());
        assertEquals("Hugo", sala.getJugadores().get(0).getNombre());
        assertEquals("Rojo", sala.getJugadores().get(0).getColor());
    }

    @Test
    @DisplayName("CU-01 Flujo Básico: Unirse a Sala existente con color libre")
    public void testUnirseASalaExito() throws Exception {
        JugadorDTO anfitrion = new JugadorDTO("J1", "Hugo", "Rojo", true);
        SalaDTO sala = fachada.crearSala(anfitrion, 4);

        JugadorDTO jugador2 = new JugadorDTO("J2", "Luis", "Azul", false);
        SalaDTO salaActualizada = fachada.unirseASala(sala.getCodigo(), jugador2);

        assertEquals(2, salaActualizada.getJugadores().size());
        assertTrue(salaActualizada.tieneColorOcupado("Azul"));
    }

    @Test
    @DisplayName("CU-01 Flujo Alterno A2: Falla cuando la sala no existe")
    public void testAlternoA2_SalaNoEncontrada() {
        JugadorDTO jugador = new JugadorDTO("J3", "Ana", "Verde", false);

        assertThrows(SalaNoEncontradaException.class, () -> {
            fachada.unirseASala("SALA_INEXISTENTE_999", jugador);
        }, "Debe lanzar SalaNoEncontradaException para el Flujo Alterno A2");
    }

    @Test
    @DisplayName("CU-01 Flujo Alterno A3: Falla cuando el color ya está ocupado")
    public void testAlternoA3_ColorOcupado() throws Exception {
        JugadorDTO anfitrion = new JugadorDTO("J1", "Hugo", "Rojo", true);
        SalaDTO sala = fachada.crearSala(anfitrion, 4);

        // Intento de unirse con el mismo color "Rojo" ya tomado por Hugo
        JugadorDTO jugadorDuplicado = new JugadorDTO("J4", "Pedro", "Rojo", false);

        assertThrows(ColorOcupadoException.class, () -> {
            fachada.unirseASala(sala.getCodigo(), jugadorDuplicado);
        }, "Debe lanzar ColorOcupadoException para el Flujo Alterno A3");
    }

    @Test
    @DisplayName("CU-01 Flujo Alterno A1: Falla cuando la sala está llena")
    public void testAlternoA1_SalaLlena() throws Exception {
        JugadorDTO anfitrion = new JugadorDTO("J1", "Hugo", "Rojo", true);
        SalaDTO sala = fachada.crearSala(anfitrion, 2); // Capacidad 2

        JugadorDTO j2 = new JugadorDTO("J2", "Luis", "Verde", false);
        fachada.unirseASala(sala.getCodigo(), j2);

        // Intento de meter un tercer jugador en sala de 2
        JugadorDTO j3 = new JugadorDTO("J3", "Maria", "Azul", false);
        assertThrows(SalaLlenaException.class, () -> {
            fachada.unirseASala(sala.getCodigo(), j3);
        });
    }
}
