package com.parchis.dtos;

import java.io.Serializable;

/**
 * DTO para representar el estado general de una partida activa.
 */
public class PartidaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigoSala;
    private int turnoActual; // 0=Rojo, 1=Verde, 2=Azul, 3=Amarillo
    private int ultimoValorDado;
    private boolean finalizada;
    private String ganador;

    public PartidaDTO() {
    }

    public PartidaDTO(String codigoSala) {
        this.codigoSala = codigoSala;
        this.turnoActual = 0;
        this.ultimoValorDado = 0;
        this.finalizada = false;
    }

    public String getCodigoSala() {
        return codigoSala;
    }

    public void setCodigoSala(String codigoSala) {
        this.codigoSala = codigoSala;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    public int getUltimoValorDado() {
        return ultimoValorDado;
    }

    public void setUltimoValorDado(int ultimoValorDado) {
        this.ultimoValorDado = ultimoValorDado;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public void setFinalizada(boolean finalizada) {
        this.finalizada = finalizada;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }
}
