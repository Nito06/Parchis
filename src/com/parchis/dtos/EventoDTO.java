package com.parchis.dtos;

import java.io.Serializable;

/**
 * DTO que encapsula un evento distribuido transmitido a través del bus de eventos.
 */
public class EventoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private TipoEvento tipo;
    private String emisor;
    private String codigoSala;
    private Object payload;
    private long timestamp;

    public EventoDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public EventoDTO(TipoEvento tipo, String emisor, String codigoSala, Object payload) {
        this.tipo = tipo;
        this.emisor = emisor;
        this.codigoSala = codigoSala;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getCodigoSala() {
        return codigoSala;
    }

    public void setCodigoSala(String codigoSala) {
        this.codigoSala = codigoSala;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EventoDTO{" + "tipo=" + tipo + ", emisor=" + emisor + ", codigoSala=" + codigoSala + ", payload=" + payload + '}';
    }
}
