package parchis.presentacion;

import java.awt.Color;
import java.awt.Window;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Panel principal de la partida de Parchís.
 * Integra el TableroPanel interactivo con animaciones por hilos,
 * control de turnos, tirada de dados y soporte para jugador humano y rivales.
 */
public class JuegoPanel extends javax.swing.JPanel {

    // Control del Juego
    private int turnoActual = 0;
    private int valorDado = 0;
    private boolean yaLanzoDado = false;

    // Control de Jugadores Activos
    private int numeroJugadoresTotales = 4;
    private String colorHumano = "Rojo";
    private int indiceHumano = 0;
    private final boolean[] jugadorActivo = new boolean[4]; // [0=Rojo, 1=Verde, 2=Azul, 3=Amarillo]

    public JuegoPanel() {
        initComponents();
        configurarTablero();

        // Inicialización por defecto en caso de abrirse directamente
        if (colorHumano == null) {
            inicializarJuego(4, "Rojo");
        }
    }

    private void configurarTablero() {
        tableroPanel.setTableroListener(new TableroPanel.TableroListener() {
            @Override
            public void onMovimientoFinalizado() {
                actualizarEstadoFichas();
                cambiarTurno();
            }

            @Override
            public void onMensaje(String mensaje) {
                lblMensaje.setText(mensaje);
            }

            @Override
            public void onFichaComida(String jugadorComedor, String jugadorComido) {
                lblMensaje.setText("¡" + jugadorComedor + " se comió una ficha de " + jugadorComido + "!");
            }

            @Override
            public void onVictoria(String jugadorGanador) {
                lblMensaje.setText("¡¡VICTORIA!! " + jugadorGanador + " ha ganado la partida.");
                btnLanzarDado.setEnabled(false);
                javax.swing.JOptionPane.showMessageDialog(JuegoPanel.this,
                        "¡Felicidades a " + jugadorGanador + "! Ha completado el recorrido con todas sus fichas.",
                        "Fin de la partida",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableroPanel = new TableroPanel();
        lblTituloTurno = new javax.swing.JLabel();
        lblNombreTurno = new javax.swing.JLabel();
        lblDADO = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnLanzarDado = new javax.swing.JButton();
        lblTituloJugadores = new javax.swing.JLabel();
        lblEstadoRojo = new javax.swing.JLabel();
        lblEstadoVerde = new javax.swing.JLabel();
        lblEstadoAzul = new javax.swing.JLabel();
        lblEstadoAmarillo = new javax.swing.JLabel();
        lblMensaje = new javax.swing.JLabel();
        btnVolverMenu = new javax.swing.JButton();

        lblTituloTurno.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTituloTurno.setText("TURNO ACTUAL");

        lblNombreTurno.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblNombreTurno.setText("JUGADOR ROJO");

        lblDADO.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblDADO.setText("DADO");

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Elementos/Dado_1.png"))); // NOI18N

        btnLanzarDado.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnLanzarDado.setText("Lanzar Dado");
        btnLanzarDado.addActionListener(this::btnLanzarDadoActionPerformed);

        lblTituloJugadores.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTituloJugadores.setText("JUGADORES");

        lblEstadoRojo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblEstadoRojo.setText("Rojo: 4 Fichas");

        lblEstadoVerde.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblEstadoVerde.setText("Verde: 4 Fichas");

        lblEstadoAzul.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblEstadoAzul.setText("Azul: 4 fichas");

        lblEstadoAmarillo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblEstadoAmarillo.setText("Amarillo: 4 Fichas");

        lblMensaje.setFont(new java.awt.Font("Segoe UI", 0, 20)); // NOI18N
        lblMensaje.setText("Lanza el dado para comenzar");

        btnVolverMenu.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnVolverMenu.setText("Volver al Menu");
        btnVolverMenu.addActionListener(this::btnVolverMenuActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableroPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(lblMensaje, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTituloTurno))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(70, 70, 70)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblEstadoAzul)
                                    .addComponent(lblEstadoVerde)
                                    .addComponent(lblEstadoAmarillo)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(lblEstadoRojo))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(92, 92, 92)
                                        .addComponent(lblDADO))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(64, 64, 64)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btnLanzarDado)
                                            .addComponent(jLabel1)
                                            .addComponent(lblTituloJugadores)
                                            .addComponent(btnVolverMenu))))
                                .addGap(0, 31, Short.MAX_VALUE)))
                        .addContainerGap(14, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(lblNombreTurno)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(lblTituloTurno)
                        .addGap(40, 40, 40)
                        .addComponent(lblNombreTurno)
                        .addGap(26, 26, 26)
                        .addComponent(lblDADO)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(btnLanzarDado)
                        .addGap(26, 26, 26)
                        .addComponent(lblTituloJugadores)
                        .addGap(18, 18, 18)
                        .addComponent(lblEstadoRojo)
                        .addGap(18, 18, 18)
                        .addComponent(lblEstadoVerde)
                        .addGap(18, 18, 18)
                        .addComponent(lblEstadoAzul))
                    .addComponent(tableroPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lblEstadoAmarillo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVolverMenu)
                    .addComponent(lblMensaje))
                .addContainerGap(32, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnLanzarDadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLanzarDadoActionPerformed
        lanzarDadoYProcesar();
    }//GEN-LAST:event_btnLanzarDadoActionPerformed

    private void lanzarDadoYProcesar() {
        btnLanzarDado.setEnabled(false);
        lblMensaje.setText("Lanzando dado...");

        try {
            jLabel1.setIcon(new ImageIcon(getClass().getResource("/Elementos/Girando_Dado.png")));
        } catch (Exception ignored) {}

        Timer timer = new Timer(500, evt -> {
            valorDado = (int) (Math.random() * 6) + 1;
            try {
                String ruta = "/Elementos/Dado_" + valorDado + ".png";
                jLabel1.setIcon(new ImageIcon(getClass().getResource(ruta)));
            } catch (Exception ignored) {}

            yaLanzoDado = true;
            tableroPanel.setTurno(turnoActual, valorDado);
            ((Timer) evt.getSource()).stop();
        });

        timer.setRepeats(false);
        timer.start();
    }

    private void btnVolverMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverMenuActionPerformed
        Window ventanaActual = SwingUtilities.getWindowAncestor(this);
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
        new ParchisFames().setVisible(true);
    }//GEN-LAST:event_btnVolverMenuActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLanzarDado;
    private javax.swing.JButton btnVolverMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblDADO;
    private javax.swing.JLabel lblEstadoAmarillo;
    private javax.swing.JLabel lblEstadoAzul;
    private javax.swing.JLabel lblEstadoRojo;
    private javax.swing.JLabel lblEstadoVerde;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblNombreTurno;
    private TableroPanel tableroPanel;
    private javax.swing.JLabel lblTituloJugadores;
    private javax.swing.JLabel lblTituloTurno;
    // End of variables declaration//GEN-END:variables

    private void configurarJugadoresActivos() {
        for (int i = 0; i < 4; i++) jugadorActivo[i] = false;

        indiceHumano = 0;
        if (colorHumano.equalsIgnoreCase("Rojo")) indiceHumano = 0;
        else if (colorHumano.equalsIgnoreCase("Verde")) indiceHumano = 1;
        else if (colorHumano.equalsIgnoreCase("Azul")) indiceHumano = 2;
        else if (colorHumano.equalsIgnoreCase("Amarillo")) indiceHumano = 3;

        jugadorActivo[indiceHumano] = true;

        if (numeroJugadoresTotales == 2) {
            jugadorActivo[(indiceHumano + 2) % 4] = true;
        } else if (numeroJugadoresTotales == 3) {
            jugadorActivo[(indiceHumano + 1) % 4] = true;
            jugadorActivo[(indiceHumano + 2) % 4] = true;
        } else {
            for (int i = 0; i < 4; i++) jugadorActivo[i] = true;
        }

        tableroPanel.configurarPartida(numeroJugadoresTotales, colorHumano);
        actualizarEstadoFichas();
    }

    public void inicializarJuego(int numJugadores, String colorElegido) {
        this.numeroJugadoresTotales = numJugadores;
        this.colorHumano = colorElegido;

        configurarJugadoresActivos();
        turnoActual = indiceHumano;

        prepararTurno();
    }

    private void actualizarEstadoFichas() {
        lblEstadoRojo.setText("Rojo: " + tableroPanel.getFichasEnBase(0) + " en base | " + tableroPanel.getFichasCoronadas(0) + " en meta");
        lblEstadoVerde.setText("Verde: " + tableroPanel.getFichasEnBase(1) + " en base | " + tableroPanel.getFichasCoronadas(1) + " en meta");
        lblEstadoAzul.setText("Azul: " + tableroPanel.getFichasEnBase(2) + " en base | " + tableroPanel.getFichasCoronadas(2) + " en meta");
        lblEstadoAmarillo.setText("Amarillo: " + tableroPanel.getFichasEnBase(3) + " en base | " + tableroPanel.getFichasCoronadas(3) + " en meta");

        // Ocultar etiquetas de jugadores inactivos
        lblEstadoRojo.setVisible(jugadorActivo[0]);
        lblEstadoVerde.setVisible(jugadorActivo[1]);
        lblEstadoAzul.setVisible(jugadorActivo[2]);
        lblEstadoAmarillo.setVisible(jugadorActivo[3]);
    }

    private void prepararTurno() {
        yaLanzoDado = false;

        switch (turnoActual) {
            case 0 -> {
                lblNombreTurno.setText("JUGADOR ROJO");
                lblNombreTurno.setForeground(Color.RED);
            }
            case 1 -> {
                lblNombreTurno.setText("JUGADOR VERDE");
                lblNombreTurno.setForeground(new Color(0, 153, 0));
            }
            case 2 -> {
                lblNombreTurno.setText("JUGADOR AZUL");
                lblNombreTurno.setForeground(Color.BLUE);
            }
            case 3 -> {
                lblNombreTurno.setText("JUGADOR AMARILLO");
                lblNombreTurno.setForeground(new Color(204, 153, 0));
            }
        }

        if (turnoActual == indiceHumano) {
            // Turno del jugador humano
            btnLanzarDado.setEnabled(true);
            lblMensaje.setText("¡Es tu turno! Lanza el dado.");
        } else {
            // Turno de bot / rival automático
            btnLanzarDado.setEnabled(false);
            lblMensaje.setText("Turno de la computadora (" + TableroPanel.NOMBRES_JUGADORES[turnoActual] + ")...");
            ejecutarTurnoBot();
        }
    }

    private void ejecutarTurnoBot() {
        Thread botThread = new Thread(() -> {
            try {
                Thread.sleep(800);
                SwingUtilities.invokeLater(() -> {
                    lblMensaje.setText(TableroPanel.NOMBRES_JUGADORES[turnoActual] + " está lanzando el dado...");
                    try {
                        jLabel1.setIcon(new ImageIcon(getClass().getResource("/Elementos/Girando_Dado.png")));
                    } catch (Exception ignored) {}
                });

                Thread.sleep(600);
                valorDado = (int) (Math.random() * 6) + 1;

                SwingUtilities.invokeLater(() -> {
                    try {
                        String ruta = "/Elementos/Dado_" + valorDado + ".png";
                        jLabel1.setIcon(new ImageIcon(getClass().getResource(ruta)));
                    } catch (Exception ignored) {}
                });

                Thread.sleep(500);

                // Obtener fichas movibles del bot
                List<TableroPanel.Ficha> movibles = tableroPanel.getFichasMovibles(turnoActual, valorDado);
                if (movibles.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        lblMensaje.setText(TableroPanel.NOMBRES_JUGADORES[turnoActual] + " sacó un " + valorDado + " pero no puede mover.");
                    });
                    Thread.sleep(1100);
                    SwingUtilities.invokeLater(this::cambiarTurno);
                } else {
                    // Elegir la mejor ficha: priorizar sacar de base o la más adelantada
                    TableroPanel.Ficha seleccionada = movibles.get(0);
                    for (TableroPanel.Ficha f : movibles) {
                        if (f.enBase) {
                            seleccionada = f;
                            break;
                        } else if (f.pasosRecorridos > seleccionada.pasosRecorridos) {
                            seleccionada = f;
                        }
                    }

                    TableroPanel.Ficha finalFicha = seleccionada;
                    SwingUtilities.invokeLater(() -> {
                        lblMensaje.setText(TableroPanel.NOMBRES_JUGADORES[turnoActual] + " mueve su ficha " + (finalFicha.id + 1) + ".");
                        tableroPanel.ejecutarMovimientoConHilo(finalFicha, valorDado);
                    });
                }

            } catch (InterruptedException ignored) {}
        });

        botThread.setDaemon(true);
        botThread.start();
    }

    private void cambiarTurno() {
        do {
            turnoActual++;
            if (turnoActual > 3) {
                turnoActual = 0;
            }
        } while (!jugadorActivo[turnoActual]);

        prepararTurno();
    }
}
