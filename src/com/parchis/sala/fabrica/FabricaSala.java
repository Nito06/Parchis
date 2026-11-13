package com.parchis.sala.fabrica;

import com.parchis.interfaces.IFachadaSala;
import com.parchis.sala.mock.FachadaSalaMock;

/**
 * Fábrica para el subsistema de Sala.
 * Permite obtener la instancia de la fachada (Mock en Hito 2A, Real en Hito 2B)
 * garantizando que las vistas y controladores jamás nombren a la clase concreta ni al mock.
 */
public class FabricaSala {

    private static IFachadaSala instanciaFachada;

    /**
     * Retorna la instancia de IFachadaSala configurada.
     */
    public static synchronized IFachadaSala getFachada() {
        if (instanciaFachada == null) {
            // Hito 2A: Se utiliza el Mock en memoria
            instanciaFachada = FachadaSalaMock.getInstance();
            // Hito 2B: Se cambiará aquí por new FachadaSalaReal(...) sin tocar vistas
        }
        return instanciaFachada;
    }

    /**
     * Permite inyectar una fachada específica (por ejemplo en pruebas unitarias).
     */
    public static synchronized void setFachada(IFachadaSala fachada) {
        instanciaFachada = fachada;
    }
}
