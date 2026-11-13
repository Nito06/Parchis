package com.parchis.dtos;

import java.io.Serializable;

/**
 * DTO para la información de un jugador en la sala y partida.
 */
public class JugadorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private String color; // "Rojo", "Verde", "Azul", "Amarillo"
    private boolean anfitrion;
    private boolean listo;

    public JugadorDTO() {
    }

    public JugadorDTO(String id, String nombre, String color, boolean anfitrion) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.anfitrion = anfitrion;
        this.listo = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isAnfitrion() {
        return anfitrion;
    }

    public void setAnfitrion(boolean anfitrion) {
        this.anfitrion = anfitrion;
    }

    public boolean isListo() {
        return listo;
    }

    public void setListo(boolean listo) {
        this.listo = listo;
    }

    @Override
    public String toString() {
        return "JugadorDTO{" + "nombre='" + nombre + "', color='" + color + "', anfitrion=" + anfitrion + '}';
    }
}
