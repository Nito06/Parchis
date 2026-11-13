package parchis.presentacion;

import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import com.parchis.excepciones.ColorOcupadoException;
import com.parchis.excepciones.ParchisException;
import com.parchis.excepciones.SalaLlenaException;
import com.parchis.excepciones.SalaNoEncontradaException;
import com.parchis.interfaces.IFachadaSala;
import com.parchis.interfaces.ISalaListener;
import com.parchis.sala.fabrica.FabricaSala;
import java.util.UUID;

/**
 * Controlador de la capa de Presentación para el CU-01 (Administrar Sala).
 * Se conecta a la fachada ÚNICAMENTE por medio de la fábrica (FabricaSala.getFachada()),
 * cumpliendo la regla de que las vistas y controladores jamás nombran al mock.
 */
public class ControlSala {

    public interface VistaSalaCallback {
        void onOperacionExitosa(SalaDTO sala, JugadorDTO jugadorActual);
        void onErrorNegocio(String mensajeError);
    }

    private final IFachadaSala fachadaSala;
    private SalaDTO salaActual;
    private JugadorDTO jugadorLocal;

    public ControlSala() {
        // Conexión por medio de la fábrica
        this.fachadaSala = FabricaSala.getFachada();
    }

    public void registrarListener(ISalaListener listener) {
        fachadaSala.registrarListener(listener);
    }

    /**
     * Flujo Básico: Crear una nueva sala.
     */
    public void crearSala(String nombre, String color, int capacidad, VistaSalaCallback callback) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = "Jugador-" + (int)(Math.random() * 900 + 100);
            }
            this.jugadorLocal = new JugadorDTO(UUID.randomUUID().toString(), nombre, color, true);
            this.salaActual = fachadaSala.crearSala(jugadorLocal, capacidad);

            if (callback != null) {
                callback.onOperacionExitosa(salaActual, jugadorLocal);
            }
        } catch (ParchisException ex) {
            if (callback != null) {
                callback.onErrorNegocio(ex.getMessage());
            }
        }
    }

    /**
     * Unirse a una sala existente (Gestiona Flujos Alternos A1, A2 y A3).
     */
    public void unirseASala(String codigoSala, String nombre, String color, VistaSalaCallback callback) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = "Invitado-" + (int)(Math.random() * 900 + 100);
            }
            this.jugadorLocal = new JugadorDTO(UUID.randomUUID().toString(), nombre, color, false);
            this.salaActual = fachadaSala.unirseASala(codigoSala, jugadorLocal);

            if (callback != null) {
                callback.onOperacionExitosa(salaActual, jugadorLocal);
            }

        } catch (SalaNoEncontradaException ex) {
            // Flujo Alterno A2
            if (callback != null) {
                callback.onErrorNegocio(ex.getMessage());
            }
        } catch (ColorOcupadoException ex) {
            // Flujo Alterno A3
            if (callback != null) {
                callback.onErrorNegocio(ex.getMessage());
            }
        } catch (SalaLlenaException ex) {
            // Flujo Alterno A1
            if (callback != null) {
                callback.onErrorNegocio(ex.getMessage());
            }
        } catch (ParchisException ex) {
            if (callback != null) {
                callback.onErrorNegocio("Error general: " + ex.getMessage());
            }
        }
    }

    public SalaDTO getSalaActual() {
        return salaActual;
    }

    public JugadorDTO getJugadorLocal() {
        return jugadorLocal;
    }
}
