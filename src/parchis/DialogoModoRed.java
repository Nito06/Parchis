package parchis;

import com.parchis.dtos.EventoDTO;
import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import com.parchis.dtos.TipoEvento;
import com.parchis.eventos.EventBroker;
import com.parchis.eventos.EventClient;
import com.parchis.eventos.RedUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Diálogo para configurar y lanzar la partida en red.
 * Permite dos modalidades de red:
 *  1. Red Local (LAN / Wi-Fi)
 *  2. Red Virtual / VPN (Tailscale, ZeroTier)
 * Cumple con la arquitectura basada en eventos (EventBroker, EventClient, EventoDTO)
 * y utiliza ControlSala para los flujos de creación y unión a salas.
 */
public class DialogoModoRed extends JDialog {

    public interface ModoRedCallback {
        void onPartidaIniciada(String miColor, String miNombre, EventClient client, boolean esHost, String codigoSala, List<String> coloresActivos);
    }

    private final ModoRedCallback callback;
    private final ControlSala controlSala;

    // Pestaña Host
    private JTextField txtNombreHost;
    private JTextField txtPuertoHost;
    private JRadioButton rbRojoHost, rbVerdeHost, rbAzulHost, rbAmarilloHost;
    private JLabel lblEstadoHost;
    private JButton btnCrearHost;
    private EventClient clientHost;
    private String codigoSalaHost = "SALA-01";

    // Pestaña Cliente
    private JTextField txtIPCliente;
    private JTextField txtPuertoCliente;
    private JTextField txtNombreCliente;
    private JRadioButton rbRojoCli, rbVerdeCli, rbAzulCli, rbAmarilloCli;
    private JLabel lblEstadoCli;
    private JButton btnConectarCli;
    private EventClient clientCliente;

    public DialogoModoRed(JFrame parent, ModoRedCallback callback) {
        super(parent, "Parchís - Configurar Partida en Red (Wi-Fi / Tailscale / ZeroTier)", true);
        this.callback = callback;
        this.controlSala = new ControlSala();

        initComponents();
        setSize(680, 560);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Título superior
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        JLabel lblTitulo = new JLabel("Configuración de Partida en Red", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 41, 59));
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);

        JLabel lblSub = new JLabel("Juega en la misma red Wi-Fi o a distancia mediante Tailscale / ZeroTier", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        pnlHeader.add(lblSub, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        // Pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("🏠 Crear Sala (Host / Servidor)", crearPanelHost());
        tabbedPane.addTab("🔗 Unirse a Sala (Cliente)", crearPanelCliente());

        add(tabbedPane, BorderLayout.CENTER);

        // Botón inferior para cancelar
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnCancelar = new JButton("Cancelar y Volver");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.addActionListener(e -> dispose());
        pnlFooter.add(btnCancelar);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // ==========================================
    // PESTAÑA 1: CREAR SALA (HOST)
    // ==========================================
    private JPanel crearPanelHost() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Caja informativa con las IPs detectadas de la máquina
        JPanel pnlIPs = new JPanel(new BorderLayout(5, 5));
        pnlIPs.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 2, true),
                " 🌐 Direcciones IP de tu PC para compartir "
        ));
        pnlIPs.setBackground(new Color(239, 246, 255));

        StringBuilder sb = new StringBuilder("<html><body style='padding:6px; font-family:sans-serif;'>");
        List<RedUtil.InfoIP> listaIPs = RedUtil.obtenerDireccionesIP();
        for (RedUtil.InfoIP info : listaIPs) {
            String badge = info.getTipo().contains("Tailscale") ? "🛡️ <b>[Tailscale VPN]</b>" :
                           info.getTipo().contains("ZeroTier") ? "🛡️ <b>[ZeroTier VPN]</b>" :
                           info.getTipo().contains("Wi-Fi") ? "📶 <b>[Wi-Fi / LAN]</b>" : "💻 <b>[Local]</b>";
            sb.append(badge).append(" ").append(info.getIp()).append("<br>");
        }
        sb.append("<span style='color:#64748b; font-size:11px;'>Comparte tu IP Wi-Fi si están en la misma casa/oficina, o tu IP Tailscale si juegan por internet.</span></body></html>");

        JLabel lblListaIPs = new JLabel(sb.toString());
        pnlIPs.add(lblListaIPs, BorderLayout.CENTER);

        JButton btnCopiarIP = new JButton("📋 Copiar IP");
        btnCopiarIP.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCopiarIP.addActionListener(e -> {
            String ipRec = RedUtil.getIPRecomendada();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ipRec), null);
            JOptionPane.showMessageDialog(this, "IP copiada al portapapeles: " + ipRec, "IP Copiada", JOptionPane.INFORMATION_MESSAGE);
        });
        JPanel pnlBtnCopiar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtnCopiar.setOpaque(false);
        pnlBtnCopiar.add(btnCopiarIP);
        pnlIPs.add(pnlBtnCopiar, BorderLayout.EAST);

        panel.add(pnlIPs, BorderLayout.NORTH);

        // Formulario de Host
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblNombre = new JLabel("Tu Nombre:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNombreHost = new JTextField("Host-Parchis");
        txtNombreHost.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtNombreHost, gbc);

        // Color
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblColor = new JLabel("Tu Color:");
        lblColor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblColor, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel pnlColores = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ButtonGroup bgHost = new ButtonGroup();
        rbRojoHost = new JRadioButton("Rojo", true);
        rbRojoHost.setForeground(new Color(220, 38, 38));
        rbRojoHost.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbVerdeHost = new JRadioButton("Verde");
        rbVerdeHost.setForeground(new Color(22, 163, 74));
        rbVerdeHost.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbAzulHost = new JRadioButton("Azul");
        rbAzulHost.setForeground(new Color(37, 99, 235));
        rbAzulHost.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbAmarilloHost = new JRadioButton("Amarillo");
        rbAmarilloHost.setForeground(new Color(202, 138, 4));
        rbAmarilloHost.setFont(new Font("Segoe UI", Font.BOLD, 13));

        bgHost.add(rbRojoHost); bgHost.add(rbVerdeHost);
        bgHost.add(rbAzulHost); bgHost.add(rbAmarilloHost);
        pnlColores.add(rbRojoHost); pnlColores.add(rbVerdeHost);
        pnlColores.add(rbAzulHost); pnlColores.add(rbAmarilloHost);
        pnlForm.add(pnlColores, gbc);

        // Puerto
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblPuerto = new JLabel("Puerto de Red:");
        lblPuerto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblPuerto, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPuertoHost = new JTextField(String.valueOf(EventBroker.PUERTO_DEFAULT));
        txtPuertoHost.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtPuertoHost, gbc);

        panel.add(pnlForm, BorderLayout.CENTER);

        // Acciones y estado inferior
        JPanel pnlAccionesHost = new JPanel(new BorderLayout(5, 5));
        lblEstadoHost = new JLabel("Listo para crear la sala.", SwingConstants.CENTER);
        lblEstadoHost.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEstadoHost.setForeground(new Color(71, 85, 105));
        pnlAccionesHost.add(lblEstadoHost, BorderLayout.NORTH);

        btnCrearHost = new JButton("🚀 Crear Sala y Esperar Rival");
        btnCrearHost.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCrearHost.setBackground(new Color(37, 99, 235));
        btnCrearHost.setForeground(Color.WHITE);
        btnCrearHost.setPreferredSize(new Dimension(280, 42));
        btnCrearHost.addActionListener(e -> accionarCrearSalaHost());

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.add(btnCrearHost);
        pnlAccionesHost.add(pnlBtn, BorderLayout.SOUTH);

        panel.add(pnlAccionesHost, BorderLayout.SOUTH);

        return panel;
    }

    private void accionarCrearSalaHost() {
        int puerto = EventBroker.PUERTO_DEFAULT;
        try {
            puerto = Integer.parseInt(txtPuertoHost.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Puerto inválido. Usando " + EventBroker.PUERTO_DEFAULT, "Aviso", JOptionPane.WARNING_MESSAGE);
        }

        String nombre = txtNombreHost.getText().trim();
        if (nombre.isEmpty()) nombre = "Host";

        String color = rbRojoHost.isSelected() ? "Rojo" :
                       rbVerdeHost.isSelected() ? "Verde" :
                       rbAzulHost.isSelected() ? "Azul" : "Amarillo";

        final String finalNombre = nombre;
        final String finalColor = color;
        final int finalPuerto = puerto;

        btnCrearHost.setEnabled(false);
        lblEstadoHost.setText("Iniciando broker de eventos en puerto " + finalPuerto + "...");

        // 1. Iniciar EventBroker en segundo plano
        EventBroker.getInstanciaGlobal(finalPuerto);

        // 2. Conectar EventClient del host
        clientHost = new EventClient("localhost", finalPuerto);
        if (!clientHost.conectar()) {
            lblEstadoHost.setText("❌ Error al conectar al broker local.");
            btnCrearHost.setEnabled(true);
            return;
        }

        // 3. Registrar sala mediante ControlSala y Fábrica (Auditoría Hito 2A/2B)
        controlSala.crearSala(finalNombre, finalColor, 2, new ControlSala.VistaSalaCallback() {
            @Override
            public void onOperacionExitosa(SalaDTO sala, JugadorDTO jugadorActual) {
                codigoSalaHost = sala.getCodigo();
                lblEstadoHost.setText("🟢 Sala [" + codigoSalaHost + "] activa. Esperando a que el rival se conecte...");

                // Suscribirse a eventos para cuando se una el cliente
                clientHost.suscribir(new com.parchis.interfaces.IEventSubscriber() {
                    @Override
                    public void onEventoRecibido(EventoDTO evento) {
                        if (evento.getTipo() == TipoEvento.JUGADOR_UNIDO && !finalNombre.equals(evento.getEmisor())) {
                            String nombreRival = evento.getEmisor();
                            String colorRival = evento.getPayload() != null ? evento.getPayload().toString() : "Verde";

                            SwingUtilities.invokeLater(() -> {
                                lblEstadoHost.setText("¡Rival conectado (" + nombreRival + " - " + colorRival + ")! Iniciando...");

                                // Publicar que la partida inicia con ambos colores
                                List<String> colores = Arrays.asList(finalColor, colorRival);
                                EventoDTO evInicio = new EventoDTO(
                                        TipoEvento.PARTIDA_INICIADA,
                                        finalNombre,
                                        codigoSalaHost,
                                        new ArrayList<>(colores)
                                );
                                clientHost.publicarEvento(evInicio);

                                // Iniciar juego
                                if (callback != null) {
                                    callback.onPartidaIniciada(finalColor, finalNombre, clientHost, true, codigoSalaHost, colores);
                                }
                                dispose();
                            });
                        }
                    }
                });
            }

            @Override
            public void onErrorNegocio(String mensajeError) {
                lblEstadoHost.setText("Aviso: " + mensajeError);
                btnCrearHost.setEnabled(true);
            }
        });
    }

    // ==========================================
    // PESTAÑA 2: UNIRSE A SALA (CLIENTE)
    // ==========================================
    private JPanel crearPanelCliente() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Banner informativo
        JPanel pnlInfo = new JPanel(new BorderLayout(5, 5));
        pnlInfo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(16, 185, 129), 2, true),
                " 🔗 Conectarse al Host "
        ));
        pnlInfo.setBackground(new Color(236, 253, 245));

        JLabel lblInfoCli = new JLabel("<html><body style='padding:6px; font-family:sans-serif;'>"
                + "Ingresa la <b>IP del Host</b> (su IP Wi-Fi si están en la misma red, o su IP Tailscale/ZeroTier si están por VPN).<br>"
                + "<span style='color:#059669; font-size:11px;'>Para probar dos jugadores en esta misma computadora, usa <b>127.0.0.1</b>.</span>"
                + "</body></html>");
        pnlInfo.add(lblInfoCli, BorderLayout.CENTER);

        JButton btnUsarLocal = new JButton("💻 Misma PC (127.0.0.1)");
        btnUsarLocal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnUsarLocal.addActionListener(e -> txtIPCliente.setText("127.0.0.1"));
        JPanel pnlBtnLocal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtnLocal.setOpaque(false);
        pnlBtnLocal.add(btnUsarLocal);
        pnlInfo.add(pnlBtnLocal, BorderLayout.EAST);

        panel.add(pnlInfo, BorderLayout.NORTH);

        // Formulario
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // IP del Host
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblIP = new JLabel("IP del Host:");
        lblIP.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblIP, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtIPCliente = new JTextField(RedUtil.getIPRecomendada());
        txtIPCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtIPCliente, gbc);

        // Puerto
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPuerto = new JLabel("Puerto:");
        lblPuerto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblPuerto, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPuertoCliente = new JTextField(String.valueOf(EventBroker.PUERTO_DEFAULT));
        txtPuertoCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtPuertoCliente, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblNombre = new JLabel("Tu Nombre:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNombreCliente = new JTextField("Rival-Parchis");
        txtNombreCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtNombreCliente, gbc);

        // Color
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblColor = new JLabel("Tu Color:");
        lblColor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblColor, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel pnlColoresCli = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ButtonGroup bgCli = new ButtonGroup();
        rbRojoCli = new JRadioButton("Rojo");
        rbRojoCli.setForeground(new Color(220, 38, 38));
        rbRojoCli.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbVerdeCli = new JRadioButton("Verde", true);
        rbVerdeCli.setForeground(new Color(22, 163, 74));
        rbVerdeCli.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbAzulCli = new JRadioButton("Azul");
        rbAzulCli.setForeground(new Color(37, 99, 235));
        rbAzulCli.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbAmarilloCli = new JRadioButton("Amarillo");
        rbAmarilloCli.setForeground(new Color(202, 138, 4));
        rbAmarilloCli.setFont(new Font("Segoe UI", Font.BOLD, 13));

        bgCli.add(rbRojoCli); bgCli.add(rbVerdeCli);
        bgCli.add(rbAzulCli); bgCli.add(rbAmarilloCli);
        pnlColoresCli.add(rbRojoCli); pnlColoresCli.add(rbVerdeCli);
        pnlColoresCli.add(rbAzulCli); pnlColoresCli.add(rbAmarilloCli);
        pnlForm.add(pnlColoresCli, gbc);

        panel.add(pnlForm, BorderLayout.CENTER);

        // Acciones inferiores
        JPanel pnlAccionesCli = new JPanel(new BorderLayout(5, 5));
        lblEstadoCli = new JLabel("Listo para conectar.", SwingConstants.CENTER);
        lblEstadoCli.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEstadoCli.setForeground(new Color(71, 85, 105));
        pnlAccionesCli.add(lblEstadoCli, BorderLayout.NORTH);

        btnConectarCli = new JButton("🎮 Conectarse a la Sala");
        btnConectarCli.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnConectarCli.setBackground(new Color(16, 185, 129));
        btnConectarCli.setForeground(Color.WHITE);
        btnConectarCli.setPreferredSize(new Dimension(280, 42));
        btnConectarCli.addActionListener(e -> accionarConectarCliente());

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.add(btnConectarCli);
        pnlAccionesCli.add(pnlBtn, BorderLayout.SOUTH);

        panel.add(pnlAccionesCli, BorderLayout.SOUTH);

        return panel;
    }

    private void accionarConectarCliente() {
        String ip = txtIPCliente.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa la IP del Host.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int puerto = EventBroker.PUERTO_DEFAULT;
        try {
            puerto = Integer.parseInt(txtPuertoCliente.getText().trim());
        } catch (NumberFormatException ex) {
            puerto = EventBroker.PUERTO_DEFAULT;
        }

        String nombre = txtNombreCliente.getText().trim();
        if (nombre.isEmpty()) nombre = "Invitado";

        String color = rbRojoCli.isSelected() ? "Rojo" :
                       rbVerdeCli.isSelected() ? "Verde" :
                       rbAzulCli.isSelected() ? "Azul" : "Amarillo";

        final String finalIP = ip;
        final int finalPuerto = puerto;
        final String finalNombre = nombre;
        final String finalColor = color;

        btnConectarCli.setEnabled(false);
        lblEstadoCli.setText("Conectando a " + finalIP + ":" + finalPuerto + "...");

        // Conectar EventClient
        new Thread(() -> {
            clientCliente = new EventClient(finalIP, finalPuerto);
            if (!clientCliente.conectar()) {
                SwingUtilities.invokeLater(() -> {
                    lblEstadoCli.setText("❌ No se pudo conectar a " + finalIP + ":" + finalPuerto + ". Verifica la IP y el Host.");
                    btnConectarCli.setEnabled(true);
                });
                return;
            }

            SwingUtilities.invokeLater(() -> {
                lblEstadoCli.setText("🟢 Conectado al broker. Notificando al Host...");

                // Suscribirse al evento de PARTIDA_INICIADA del Host
                clientCliente.suscribir(new com.parchis.interfaces.IEventSubscriber() {
                    @Override
                    public void onEventoRecibido(EventoDTO evento) {
                        if (evento.getTipo() == TipoEvento.PARTIDA_INICIADA) {
                            SwingUtilities.invokeLater(() -> {
                                @SuppressWarnings("unchecked")
                                List<String> colores = evento.getPayload() instanceof List<?> l ? (List<String>) l : Arrays.asList("Rojo", finalColor);
                                if (callback != null) {
                                    callback.onPartidaIniciada(finalColor, finalNombre, clientCliente, false, "SALA-01", colores);
                                }
                                dispose();
                            });
                        }
                    }
                });

                // Enviar evento de unión al Host
                EventoDTO evUnido = new EventoDTO(
                        TipoEvento.JUGADOR_UNIDO,
                        finalNombre,
                        "SALA-01",
                        finalColor
                );
                clientCliente.publicarEvento(evUnido);

                // Timeout de respaldo por si el host inició antes de responder
                new javax.swing.Timer(2500, evtTimer -> {
                    ((javax.swing.Timer) evtTimer.getSource()).stop();
                    if (isShowing()) {
                        if (callback != null) {
                            callback.onPartidaIniciada(finalColor, finalNombre, clientCliente, false, "SALA-01", Arrays.asList("Rojo", finalColor));
                        }
                        dispose();
                    }
                }).start();
            });
        }).start();
    }
}
