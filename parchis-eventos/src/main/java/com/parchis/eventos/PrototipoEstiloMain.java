package com.parchis.eventos;

import com.parchis.dtos.EventoDTO;
import com.parchis.dtos.TipoEvento;

/**
 * Prototipo ejecutable para evidenciar el Hito 2A:
 * "Hagan que dos computadoras se manden un mensaje con su estilo (Basado en Eventos)".
 *
 * Puede ejecutarse en modo 'broker', 'cliente1' o 'cliente2' (o automáticamente los dos).
 */
public class PrototipoEstiloMain {

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================================");
        System.out.println("   PROTOTIPO DEL ESTILO BASADO EN EVENTOS (PARCHÍS EN RED)");
        System.out.println("   Hito 2A: Demostración de comunicación distribuida por eventos");
        System.out.println("==================================================================");

        // 1. Iniciar el Broker de eventos local
        EventBroker broker = new EventBroker(9999);
        broker.iniciar();
        Thread.sleep(300);

        // 2. Simular Computadora A (Jugador 1 - Hugo)
        EventClient clienteA = new EventClient("localhost", 9999);
        clienteA.conectar();
        clienteA.suscribir(evento -> {
            System.out.println("--> [Computadora A recibió evento]: Tipo=" + evento.getTipo()
                    + " | De=" + evento.getEmisor() + " | Mensaje=" + evento.getPayload());
        });

        // 3. Simular Computadora B (Jugador 2 - Rival en red)
        EventClient clienteB = new EventClient("localhost", 9999);
        clienteB.conectar();
        clienteB.suscribir(evento -> {
            System.out.println("--> [Computadora B recibió evento]: Tipo=" + evento.getTipo()
                    + " | De=" + evento.getEmisor() + " | Mensaje=" + evento.getPayload());
        });

        Thread.sleep(400);

        // 4. Computadora A publica un evento
        System.out.println("\n[Computadora A] Publicando evento: SALA_CREADA...");
        clienteA.publicarEvento(new EventoDTO(TipoEvento.SALA_CREADA, "Hugo (Rojo)", "SALA-101", "¡Sala SALA-101 creada con éxito!"));

        Thread.sleep(600);

        // 5. Computadora B responde publicando otro evento
        System.out.println("\n[Computadora B] Publicando evento: JUGADOR_UNIDO...");
        clienteB.publicarEvento(new EventoDTO(TipoEvento.JUGADOR_UNIDO, "Carlos (Azul)", "SALA-101", "Me uní a la sala SALA-101 con color Azul"));

        Thread.sleep(600);

        // 6. Computadora A publica tirada de dado
        System.out.println("\n[Computadora A] Publicando evento: DADO_LANZADO...");
        clienteA.publicarEvento(new EventoDTO(TipoEvento.DADO_LANZADO, "Hugo (Rojo)", "SALA-101", "Sacó un 5 en el dado"));

        Thread.sleep(800);
        System.out.println("\nDemostración del prototipo finalizada con éxito.");
        broker.detener();
        System.exit(0);
    }
}
