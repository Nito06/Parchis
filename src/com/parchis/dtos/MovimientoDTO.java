package com.parchis.dtos;

import java.io.Serializable;

/**
 * DTO que representa el movimiento de una ficha para ser transmitido en red por eventos.
 */
public class MovimientoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int jugador; // 0=Rojo, 1=Verde, 2=Azul, 3=Amarillo
    private int idFicha; // 0..3
    private int pasos;

    public MovimientoDTO() {
    }

    public MovimientoDTO(int jugador, int idFicha, int pasos) {
        this.jugador = jugador;
        this.idFicha = idFicha;
        this.pasos = pasos;
    }

    public int getJugador() {
        return jugador;
    }

    public void setJugador(int jugador) {
        this.jugador = jugador;
    }

    public int getIdFicha() {
        return idFicha;
    }

    public void setIdFicha(int idFicha) {
        this.idFicha = idFicha;
    }

    public int getPasos() {
        return pasos;
    }

    public void setPasos(int pasos) {
        this.pasos = pasos;
    }

    @Override
    public String toString() {
        return "MovimientoDTO{jugador=" + jugador + ", ficha=" + idFicha + ", pasos=" + pasos + '}';
    }
}
