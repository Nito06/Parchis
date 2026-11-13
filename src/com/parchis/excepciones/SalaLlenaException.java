package com.parchis.excepciones;

public class SalaLlenaException extends ParchisException {
    public SalaLlenaException(String codigoSala) {
        super("Flujo Alterno A1: La sala '" + codigoSala + "' ya alcanzó el límite máximo de jugadores permitidos.");
    }
}
