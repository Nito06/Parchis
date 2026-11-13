package com.parchis.eventos;

import com.parchis.dtos.EventoDTO;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Event Broker central para el estilo arquitectónico basado en eventos.
 * Escucha conexiones de clientes (computadoras en red), recibe eventos publicados
 * y los retransmite a todos los suscriptores conectados.
 */
public class EventBroker {

    public static final int PUERTO_DEFAULT = 9999;
    private final int puerto;
    private final Set<ClienteHandler> clientesConectados = Collections.synchronizedSet(new HashSet<>());
    private volatile boolean activo = false;
    private ServerSocket serverSocket;

    public EventBroker() {
        this(PUERTO_DEFAULT);
    }

    public EventBroker(int puerto) {
        this.puerto = puerto;
    }

    public void iniciar() {
        activo = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puerto);
                System.out.println("[EventBroker] Servidor de eventos iniciado en el puerto: " + puerto);

                while (activo) {
                    Socket socket = serverSocket.accept();
                    ClienteHandler handler = new ClienteHandler(socket);
                    clientesConectados.add(handler);
                    handler.start();
                }
            } catch (Exception ex) {
                if (activo) {
                    System.err.println("[EventBroker] Error en el broker: " + ex.getMessage());
                }
            }
        }, "Hilo-EventBroker").start();
    }

    public void detener() {
        activo = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClienteHandler ch : clientesConectados) {
                ch.cerrar();
            }
            clientesConectados.clear();
        } catch (Exception ignored) {}
    }

    public void retransmitirEvento(EventoDTO evento, ClienteHandler emisor) {
        System.out.println("[EventBroker] Difundiendo evento: " + evento.getTipo() + " de " + evento.getEmisor());
        synchronized (clientesConectados) {
            for (ClienteHandler cliente : clientesConectados) {
                cliente.enviarEvento(evento);
            }
        }
    }

    private class ClienteHandler extends Thread {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                while (activo && !socket.isClosed()) {
                    EventoDTO evento = (EventoDTO) in.readObject();
                    if (evento != null) {
                        retransmitirEvento(evento, this);
                    }
                }
            } catch (Exception ex) {
                // Cliente desconectado
            } finally {
                cerrar();
            }
        }

        public void enviarEvento(EventoDTO evento) {
            try {
                if (out != null) {
                    out.writeObject(evento);
                    out.flush();
                }
            } catch (Exception ex) {
                cerrar();
            }
        }

        public void cerrar() {
            try {
                clientesConectados.remove(this);
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        EventBroker broker = new EventBroker();
        broker.iniciar();
    }
}
