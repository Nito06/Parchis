package com.parchis.excepciones;

/**
 * Excepción lanzada en el Flujo Alterno A2 del CU-01:
 * Cuando se intenta ingresar a una sala inexistente o con código inválido.
 */
public class SalaNoEncontradaException extends ParchisException {
    public SalaNoEncontradaException(String codigoSala) {
        super("Flujo Alterno A2: La sala con código '" + codigoSala + "' no existe o no se encuentra disponible.");
    }
}
