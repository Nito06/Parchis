package com.parchis.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa el estado de una sala de juego.
 */
public class SalaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private int capacidadMaxima; // 2, 3 o 4 jugadores
    private List<JugadorDTO> jugadores;
    private boolean enJuego;

    public SalaDTO() {
        this.jugadores = new ArrayList<>();
    }

    public SalaDTO(String codigo, int capacidadMaxima) {
        this.codigo = codigo;
        this.capacidadMaxima = capacidadMaxima;
        this.jugadores = new ArrayList<>();
        this.enJuego = false;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public List<JugadorDTO> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorDTO> jugadores) {
        this.jugadores = jugadores;
    }

    public boolean isEnJuego() {
        return enJuego;
    }

    public void setEnJuego(boolean enJuego) {
        this.enJuego = enJuego;
    }

    public boolean estaLlena() {
        return jugadores.size() >= capacidadMaxima;
    }

    public boolean tieneColorOcupado(String color) {
        if (color == null) return false;
        return jugadores.stream().anyMatch(j -> color.equalsIgnoreCase(j.getColor()));
    }

    @Override
    public String toString() {
        return "SalaDTO{" + "codigo='" + codigo + "', capacidad=" + capacidadMaxima + ", jugadores=" + jugadores.size() + '}';
    }
}
