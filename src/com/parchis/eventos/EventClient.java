package com.parchis.eventos;

import com.parchis.dtos.EventoDTO;
import com.parchis.interfaces.IEventPublisher;
import com.parchis.interfaces.IEventSubscriber;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente de Eventos que conecta una computadora al EventBroker.
 * Permite publicar eventos en red y recibir eventos en tiempo real
 * notificando a los suscriptores locales (Observer / Pub-Sub).
 */
public class EventClient implements IEventPublisher {

    private final String host;
    private final int puerto;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean conectado = false;
    private final List<IEventSubscriber> suscriptores = new ArrayList<>();

    public EventClient() {
        this("localhost", EventBroker.PUERTO_DEFAULT);
    }

    public EventClient(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    public synchronized boolean conectar() {
        if (conectado) return true;
        try {
            socket = new Socket(host, puerto);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            conectado = true;

            // Hilo en segundo plano para escuchar eventos del broker
            Thread receptor = new Thread(() -> {
                while (conectado) {
                    try {
                        EventoDTO evento = (EventoDTO) in.readObject();
                        if (evento != null) {
                            notificarSuscriptores(evento);
                        }
                    } catch (Exception ex) {
                        desconectar();
                        break;
                    }
                }
            }, "Hilo-ReceptorEventos");
            receptor.setDaemon(true);
            receptor.start();

            return true;
        } catch (Exception ex) {
            System.err.println("[EventClient] No se pudo conectar al broker (" + host + ":" + puerto + "): " + ex.getMessage());
            return false;
        }
    }

    @Override
    public synchronized void publicarEvento(EventoDTO evento) {
        if (!conectado && !conectar()) {
            System.err.println("[EventClient] No conectado para publicar evento: " + evento.getTipo());
            return;
        }
        try {
            out.writeObject(evento);
            out.flush();
        } catch (Exception ex) {
            desconectar();
        }
    }

    public void suscribir(IEventSubscriber suscriptor) {
        if (suscriptor != null && !suscriptores.contains(suscriptor)) {
            suscriptores.add(suscriptor);
        }
    }

    public void desuscribir(IEventSubscriber suscriptor) {
        suscriptores.remove(suscriptor);
    }

    private void notificarSuscriptores(EventoDTO evento) {
        for (IEventSubscriber s : new ArrayList<>(suscriptores)) {
            try {
                s.onEventoRecibido(evento);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void desconectar() {
        conectado = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
    }

    public boolean isConectado() {
        return conectado;
    }
}
