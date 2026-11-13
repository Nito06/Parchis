package parchis.presentacion;

import com.parchis.dtos.EventoDTO;
import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import com.parchis.dtos.TipoEvento;
import com.parchis.eventos.EventBroker;
import com.parchis.eventos.EventClient;
import com.parchis.eventos.RedUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
 * Diálogo para configurar y lanzar la partida en red con Sala de Espera (Lobby).
 * Soporta de 2 a 4 jugadores conectados en red (Wi-Fi o VPN Tailscale / ZeroTier).
 * Implementa el flujo completo:
 *   1. Configuración de sala y capacidad (2, 3 o 4 jugadores).
 *   2. Sala de espera (Lobby) en tiempo real con lista de jugadores.
 *   3. Validación de Flujos Alternos: A1 (Sala Llena) y A3 (Color Ocupado).
 *   4. Inicio sincronizado disparado por el Host cuando todos están listos.
 */
public class DialogoModoRed extends JDialog {

    public interface ModoRedCallback {
        void onPartidaIniciada(String miColor, String miNombre, EventClient client, boolean esHost, String codigoSala, List<String> coloresActivos);
    }

    private final ModoRedCallback callback;
    private final ControlSala controlSala;

    // CardLayout principal para alternar entre Selección y Lobby
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlContenedorPrincipal = new JPanel(cardLayout);

    // Vistas principales
    private JPanel pnlConfiguracion;
    private JPanel pnlLobbyHost;
    private JPanel pnlLobbyCliente;

    // Formulario Host
    private JTextField txtNombreHost;
    private JTextField txtPuertoHost;
    private JRadioButton rbRojoHost, rbVerdeHost, rbAzulHost, rbAmarilloHost;
    private JRadioButton rb2Jugadores, rb3Jugadores, rb4Jugadores;
    private JLabel lblEstadoHost;
    private JButton btnCrearHost;
    private int capacidadEsperada = 4;
    private String codigoSalaHost = "SALA-01";
    private EventClient clientHost;
    private final List<JugadorDTO> jugadoresConectados = new ArrayList<>();
    private volatile boolean partidaIniciada = false;

    // Componentes de la Sala de Espera (Lobby) del Host
    private JLabel lblLobbyTitulo;
    private JLabel lblLobbyContador;
    private final JPanel[] slotCardsHost = new JPanel[4];
    private final JLabel[] slotLabelsHost = new JLabel[4];
    private JButton btnEmpezarPartidaHost;

    // Formulario Cliente
    private JTextField txtIPCliente;
    private JTextField txtPuertoCliente;
    private JTextField txtNombreCliente;
    private JRadioButton rbRojoCli, rbVerdeCli, rbAzulCli, rbAmarilloCli;
    private JLabel lblEstadoCli;
    private JButton btnConectarCli;
    private EventClient clientCliente;
    private String miNombreCliente;
    private String miColorCliente;

    // Componentes de la Sala de Espera (Lobby) del Cliente
    private JLabel lblClienteLobbyTitulo;
    private JLabel lblClienteLobbyEstado;
    private final JPanel[] slotCardsCli = new JPanel[4];
    private final JLabel[] slotLabelsCli = new JLabel[4];

    public DialogoModoRed(JFrame parent, ModoRedCallback callback) {
        super(parent, "Parchís - Partida en Red (Wi-Fi / Tailscale / ZeroTier)", true);
        this.callback = callback;
        this.controlSala = new ControlSala();

        initComponents();
        setSize(700, 600);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        pnlConfiguracion = crearPanelConfiguracion();
        pnlLobbyHost = crearPanelLobbyHost();
        pnlLobbyCliente = crearPanelLobbyCliente();

        pnlContenedorPrincipal.setLayout(cardLayout);
        pnlContenedorPrincipal.add(pnlConfiguracion, "CONFIG");
        pnlContenedorPrincipal.add(pnlLobbyHost, "LOBBY_HOST");
        pnlContenedorPrincipal.add(pnlLobbyCliente, "LOBBY_CLIENTE");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pnlContenedorPrincipal, BorderLayout.CENTER);

        cardLayout.show(pnlContenedorPrincipal, "CONFIG");
    }

    // ========================================================
    // PANTALLA 1: CONFIGURACIÓN INICIAL (HOST Y CLIENTE)
    // ========================================================
    private JPanel crearPanelConfiguracion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Cabecera
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        JLabel lblTitulo = new JLabel("Configuración de Partida en Red", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(30, 41, 59));
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);

        JLabel lblSub = new JLabel("Juega por Wi-Fi Local o por VPN (Tailscale / ZeroTier) de 2 a 4 jugadores", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 116, 139));
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        panel.add(pnlHeader, BorderLayout.NORTH);

        // Pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("🏠 Crear Sala (Host / Servidor)", crearPestanaHost());
        tabbedPane.addTab("🔗 Unirse a Sala (Cliente)", crearPestanaCliente());
        panel.add(tabbedPane, BorderLayout.CENTER);

        // Pie de página
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnCancelar = new JButton("Cancelar y Volver");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.addActionListener(e -> dispose());
        pnlFooter.add(btnCancelar);
        panel.add(pnlFooter, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPestanaHost() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // IPs detectadas
        JPanel pnlIPs = new JPanel(new BorderLayout(5, 5));
        pnlIPs.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 246), 2, true),
                " 🌐 Direcciones IP de tu PC "
        ));
        pnlIPs.setBackground(new Color(239, 246, 255));

        StringBuilder sb = new StringBuilder("<html><body style='padding:5px; font-family:sans-serif;'>");
        List<RedUtil.InfoIP> listaIPs = RedUtil.obtenerDireccionesIP();
        for (RedUtil.InfoIP info : listaIPs) {
            String badge = info.getTipo().contains("Tailscale") ? "🛡️ <b>[Tailscale VPN]</b>" :
                           info.getTipo().contains("ZeroTier") ? "🛡️ <b>[ZeroTier VPN]</b>" :
                           info.getTipo().contains("Wi-Fi") ? "📶 <b>[Wi-Fi / LAN]</b>" : "💻 <b>[Local]</b>";
            sb.append(badge).append(" ").append(info.getIp()).append("&nbsp;&nbsp;");
        }
        sb.append("<br><span style='color:#64748b; font-size:11px;'>Comparte tu IP con tus amigos según la red en la que estén conectados.</span></body></html>");

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

        // Formulario
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
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

        // Capacidad de jugadores
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblCapacidad = new JLabel("Total Jugadores:");
        lblCapacidad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblCapacidad, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel pnlCap = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        ButtonGroup bgCap = new ButtonGroup();
        rb2Jugadores = new JRadioButton("2 Jugadores");
        rb2Jugadores.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb3Jugadores = new JRadioButton("3 Jugadores");
        rb3Jugadores.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rb4Jugadores = new JRadioButton("4 Jugadores", true);
        rb4Jugadores.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bgCap.add(rb2Jugadores); bgCap.add(rb3Jugadores); bgCap.add(rb4Jugadores);
        pnlCap.add(rb2Jugadores); pnlCap.add(rb3Jugadores); pnlCap.add(rb4Jugadores);
        pnlForm.add(pnlCap, gbc);

        // Puerto
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblPuerto = new JLabel("Puerto:");
        lblPuerto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblPuerto, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPuertoHost = new JTextField(String.valueOf(EventBroker.PUERTO_DEFAULT));
        txtPuertoHost.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtPuertoHost, gbc);

        panel.add(pnlForm, BorderLayout.CENTER);

        // Acciones y estado
        JPanel pnlAccionesHost = new JPanel(new BorderLayout(5, 5));
        lblEstadoHost = new JLabel("Configura las opciones y abre la sala de espera.", SwingConstants.CENTER);
        lblEstadoHost.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEstadoHost.setForeground(new Color(71, 85, 105));
        pnlAccionesHost.add(lblEstadoHost, BorderLayout.NORTH);

        btnCrearHost = new JButton("🚀 Crear Sala de Espera");
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

    private JPanel crearPestanaCliente() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel pnlInfo = new JPanel(new BorderLayout(5, 5));
        pnlInfo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(16, 185, 129), 2, true),
                " 🔗 Conectarse a una Sala Creada "
        ));
        pnlInfo.setBackground(new Color(236, 253, 245));

        JLabel lblInfoCli = new JLabel("<html><body style='padding:6px; font-family:sans-serif;'>"
                + "Ingresa la <b>IP del Host</b> (su IP Wi-Fi si están en la misma red, o su IP Tailscale/ZeroTier si juegan a distancia).<br>"
                + "<span style='color:#059669; font-size:11px;'>Para probar dos instancias en esta misma computadora, usa <b>127.0.0.1</b>.</span>"
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

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblIP = new JLabel("IP del Host:");
        lblIP.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblIP, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtIPCliente = new JTextField(RedUtil.getIPRecomendada());
        txtIPCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtIPCliente, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPuerto = new JLabel("Puerto:");
        lblPuerto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblPuerto, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPuertoCliente = new JTextField(String.valueOf(EventBroker.PUERTO_DEFAULT));
        txtPuertoCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtPuertoCliente, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblNombre = new JLabel("Tu Nombre:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblNombre, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNombreCliente = new JTextField("Rival-Parchis");
        txtNombreCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnlForm.add(txtNombreCliente, gbc);

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

        JPanel pnlAccionesCli = new JPanel(new BorderLayout(5, 5));
        lblEstadoCli = new JLabel("Listo para conectar.", SwingConstants.CENTER);
        lblEstadoCli.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblEstadoCli.setForeground(new Color(71, 85, 105));
        pnlAccionesCli.add(lblEstadoCli, BorderLayout.NORTH);

        btnConectarCli = new JButton("🎮 Unirse a la Sala");
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

    // ========================================================
    // PANTALLA 2: LOBBY / SALA DE ESPERA DEL HOST
    // ========================================================
    private JPanel crearPanelLobbyHost() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Cabecera del Lobby
        JPanel pnlHeader = new JPanel(new BorderLayout(5, 5));
        lblLobbyTitulo = new JLabel("🟢 Sala de Espera: SALA-01", SwingConstants.CENTER);
        lblLobbyTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLobbyTitulo.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblLobbyTitulo, BorderLayout.NORTH);

        lblLobbyContador = new JLabel("Esperando a que se conecten los jugadores...", SwingConstants.CENTER);
        lblLobbyContador.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLobbyContador.setForeground(new Color(100, 116, 139));
        pnlHeader.add(lblLobbyContador, BorderLayout.SOUTH);
        panel.add(pnlHeader, BorderLayout.NORTH);

        // Cuadrícula de 4 ranuras de jugadores (Slots)
        JPanel pnlSlots = new JPanel(new GridLayout(2, 2, 12, 12));
        for (int i = 0; i < 4; i++) {
            slotCardsHost[i] = new JPanel(new BorderLayout());
            slotCardsHost[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 2, true),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            slotCardsHost[i].setBackground(Color.WHITE);

            slotLabelsHost[i] = new JLabel("⏳ Esperando jugador " + (i + 1) + "...", SwingConstants.CENTER);
            slotLabelsHost[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            slotLabelsHost[i].setForeground(new Color(148, 163, 184));
            slotCardsHost[i].add(slotLabelsHost[i], BorderLayout.CENTER);

            pnlSlots.add(slotCardsHost[i]);
        }
        panel.add(pnlSlots, BorderLayout.CENTER);

        // Botonera inferior
        JPanel pnlAcciones = new JPanel(new BorderLayout(10, 10));

        btnEmpezarPartidaHost = new JButton("▶️ Esperando Jugadores (1/4)...");
        btnEmpezarPartidaHost.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEmpezarPartidaHost.setBackground(new Color(148, 163, 184));
        btnEmpezarPartidaHost.setForeground(Color.WHITE);
        btnEmpezarPartidaHost.setEnabled(false);
        btnEmpezarPartidaHost.setPreferredSize(new Dimension(320, 46));
        btnEmpezarPartidaHost.addActionListener(e -> accionarIniciarPartidaHost());

        JPanel pnlBtnCentrado = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtnCentrado.add(btnEmpezarPartidaHost);
        pnlAcciones.add(pnlBtnCentrado, BorderLayout.CENTER);

        JButton btnCancelar = new JButton("Cerrar Sala");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancelar.addActionListener(e -> {
            if (clientHost != null) clientHost.desconectar();
            dispose();
        });
        JPanel pnlBtnCancelar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtnCancelar.add(btnCancelar);
        pnlAcciones.add(pnlBtnCancelar, BorderLayout.SOUTH);

        panel.add(pnlAcciones, BorderLayout.SOUTH);

        return panel;
    }

    // ========================================================
    // PANTALLA 3: LOBBY / SALA DE ESPERA DEL CLIENTE
    // ========================================================
    private JPanel crearPanelLobbyCliente() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel pnlHeader = new JPanel(new BorderLayout(5, 5));
        lblClienteLobbyTitulo = new JLabel("🟢 Conectado a la Sala de Espera", SwingConstants.CENTER);
        lblClienteLobbyTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblClienteLobbyTitulo.setForeground(new Color(15, 23, 42));
        pnlHeader.add(lblClienteLobbyTitulo, BorderLayout.NORTH);

        lblClienteLobbyEstado = new JLabel("Esperando a que el Host inicie la partida...", SwingConstants.CENTER);
        lblClienteLobbyEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblClienteLobbyEstado.setForeground(new Color(100, 116, 139));
        pnlHeader.add(lblClienteLobbyEstado, BorderLayout.SOUTH);
        panel.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlSlots = new JPanel(new GridLayout(2, 2, 12, 12));
        for (int i = 0; i < 4; i++) {
            slotCardsCli[i] = new JPanel(new BorderLayout());
            slotCardsCli[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 2, true),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));
            slotCardsCli[i].setBackground(Color.WHITE);

            slotLabelsCli[i] = new JLabel("⏳ Esperando jugador " + (i + 1) + "...", SwingConstants.CENTER);
            slotLabelsCli[i].setFont(new Font("Segoe UI", Font.BOLD, 14));
            slotLabelsCli[i].setForeground(new Color(148, 163, 184));
            slotCardsCli[i].add(slotLabelsCli[i], BorderLayout.CENTER);

            pnlSlots.add(slotCardsCli[i]);
        }
        panel.add(pnlSlots, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSalir = new JButton("Desconectarse y Salir");
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSalir.addActionListener(e -> {
            if (clientCliente != null) clientCliente.desconectar();
            dispose();
        });
        pnlFooter.add(btnSalir);
        panel.add(pnlFooter, BorderLayout.SOUTH);

        return panel;
    }

    // ========================================================
    // LÓGICA DE NEGOCIO: CREACIÓN DE SALA Y LOBBY (HOST)
    // ========================================================
    private void accionarCrearSalaHost() {
        int puerto = EventBroker.PUERTO_DEFAULT;
        try {
            puerto = Integer.parseInt(txtPuertoHost.getText().trim());
        } catch (NumberFormatException ex) {
            puerto = EventBroker.PUERTO_DEFAULT;
        }

        String nombre = txtNombreHost.getText().trim();
        if (nombre.isEmpty()) nombre = "Host";

        String color = rbRojoHost.isSelected() ? "Rojo" :
                       rbVerdeHost.isSelected() ? "Verde" :
                       rbAzulHost.isSelected() ? "Azul" : "Amarillo";

        capacidadEsperada = rb2Jugadores.isSelected() ? 2 :
                            rb3Jugadores.isSelected() ? 3 : 4;

        final String finalNombre = nombre;
        final String finalColor = color;
        final int finalPuerto = puerto;

        btnCrearHost.setEnabled(false);
        lblEstadoHost.setText("Iniciando broker de eventos en puerto " + finalPuerto + "...");

        // 1. Iniciar Broker
        EventBroker.getInstanciaGlobal(finalPuerto);

        // 2. Conectar Cliente del Host
        clientHost = new EventClient("localhost", finalPuerto);
        if (!clientHost.conectar()) {
            lblEstadoHost.setText("❌ Error al conectar al broker local.");
            btnCrearHost.setEnabled(true);
            return;
        }

        // 3. Registrar Sala mediante ControlSala (Auditoría Hito 2A/2B)
        controlSala.crearSala(finalNombre, finalColor, capacidadEsperada, new ControlSala.VistaSalaCallback() {
            @Override
            public void onOperacionExitosa(SalaDTO sala, JugadorDTO jugadorActual) {
                codigoSalaHost = sala.getCodigo();
                jugadoresConectados.clear();
                jugadoresConectados.add(new JugadorDTO(UUID.randomUUID().toString(), finalNombre, finalColor, true));

                // Configurar vistas del Lobby
                lblLobbyTitulo.setText("🟢 Sala de Espera: " + codigoSalaHost);
                actualizarLobbyHostUI();

                // Cambiar a la vista del Lobby
                cardLayout.show(pnlContenedorPrincipal, "LOBBY_HOST");

                // Escuchar eventos en red para la sala de espera
                configurarSuscripcionLobbyHost(finalNombre);
            }

            @Override
            public void onErrorNegocio(String mensajeError) {
                lblEstadoHost.setText("Aviso: " + mensajeError);
                btnCrearHost.setEnabled(true);
            }
        });
    }

    private void configurarSuscripcionLobbyHost(String finalNombreHost) {
        clientHost.suscribir(evento -> {
            if (partidaIniciada || evento == null) return;

            if (evento.getTipo() == TipoEvento.JUGADOR_UNIDO && !finalNombreHost.equals(evento.getEmisor())) {
                String nombreRival = evento.getEmisor();
                String colorRival = evento.getPayload() != null ? evento.getPayload().toString() : "Verde";

                synchronized (jugadoresConectados) {
                    // 1. Validar si la sala ya está llena (Flujo Alterno A1)
                    if (jugadoresConectados.size() >= capacidadEsperada) {
                        EventoDTO evError = new EventoDTO(
                                TipoEvento.ERROR,
                                finalNombreHost,
                                codigoSalaHost,
                                "La sala " + codigoSalaHost + " ya está llena (máximo " + capacidadEsperada + " jugadores)."
                        );
                        clientHost.publicarEvento(evError);
                        return;
                    }

                    // 2. Validar si el color ya está ocupado (Flujo Alterno A3)
                    for (JugadorDTO j : jugadoresConectados) {
                        if (j.getColor().equalsIgnoreCase(colorRival)) {
                            EventoDTO evError = new EventoDTO(
                                    TipoEvento.ERROR,
                                    finalNombreHost,
                                    codigoSalaHost,
                                    "El color " + colorRival + " ya está ocupado por " + j.getNombre() + ". Elige otro color."
                            );
                            clientHost.publicarEvento(evError);
                            return;
                        }
                    }

                    // 3. Registrar al nuevo jugador en el Lobby
                    JugadorDTO nuevoJugador = new JugadorDTO(UUID.randomUUID().toString(), nombreRival, colorRival, false);
                    jugadoresConectados.add(nuevoJugador);

                    // 4. Notificar a todos los clientes la lista actualizada del lobby
                    EventoDTO evLobby = new EventoDTO(
                            TipoEvento.SALA_ACTUALIZADA,
                            finalNombreHost,
                            codigoSalaHost,
                            new ArrayList<>(jugadoresConectados)
                    );
                    clientHost.publicarEvento(evLobby);
                }

                SwingUtilities.invokeLater(this::actualizarLobbyHostUI);
            }
        });
    }

    private void actualizarLobbyHostUI() {
        int conectados = jugadoresConectados.size();
        lblLobbyContador.setText("Jugadores conectados: " + conectados + " de " + capacidadEsperada);

        // Actualizar tarjetas de ranuras
        for (int i = 0; i < 4; i++) {
            if (i < capacidadEsperada) {
                slotCardsHost[i].setVisible(true);
                if (i < conectados) {
                    JugadorDTO j = jugadoresConectados.get(i);
                    String icono = j.isAnfitrion() ? "👑 " : "🎮 ";
                    slotLabelsHost[i].setText(icono + j.getNombre() + " (" + j.getColor() + ") - LISTO");
                    slotLabelsHost[i].setForeground(getColorAwt(j.getColor()));
                    slotCardsHost[i].setBackground(new Color(240, 253, 244));
                    slotCardsHost[i].setBorder(BorderFactory.createLineBorder(getColorAwt(j.getColor()), 2, true));
                } else {
                    slotLabelsHost[i].setText("⏳ Esperando jugador " + (i + 1) + "...");
                    slotLabelsHost[i].setForeground(new Color(148, 163, 184));
                    slotCardsHost[i].setBackground(Color.WHITE);
                    slotCardsHost[i].setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2, true));
                }
            } else {
                slotCardsHost[i].setVisible(false);
            }
        }

        // Habilitar botón de empezar si hay al menos 2 jugadores
        if (conectados >= 2) {
            btnEmpezarPartidaHost.setEnabled(true);
            btnEmpezarPartidaHost.setBackground(new Color(22, 163, 74));
            if (conectados == capacidadEsperada) {
                btnEmpezarPartidaHost.setText("▶️ ¡Todos Listos! Empezar Partida (" + conectados + "/" + capacidadEsperada + ")");
            } else {
                btnEmpezarPartidaHost.setText("▶️ Empezar con los presentes (" + conectados + "/" + capacidadEsperada + ")");
            }
        } else {
            btnEmpezarPartidaHost.setEnabled(false);
            btnEmpezarPartidaHost.setBackground(new Color(148, 163, 184));
            btnEmpezarPartidaHost.setText("▶️ Esperando Jugadores (" + conectados + "/" + capacidadEsperada + ")...");
        }
    }

    private void accionarIniciarPartidaHost() {
        if (jugadoresConectados.size() < 2) {
            JOptionPane.showMessageDialog(this, "Se necesitan al menos 2 jugadores para iniciar la partida.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        partidaIniciada = true;
        btnEmpezarPartidaHost.setEnabled(false);

        List<String> coloresActivos = new ArrayList<>();
        for (JugadorDTO j : jugadoresConectados) {
            coloresActivos.add(j.getColor());
        }

        // 1. Difundir evento de inicio a todos los clientes conectados
        EventoDTO evInicio = new EventoDTO(
                TipoEvento.PARTIDA_INICIADA,
                txtNombreHost.getText().trim(),
                codigoSalaHost,
                new ArrayList<>(coloresActivos)
        );
        clientHost.publicarEvento(evInicio);

        // 2. Iniciar juego en la pantalla del host
        JugadorDTO hostDto = jugadoresConectados.get(0);
        if (callback != null) {
            callback.onPartidaIniciada(hostDto.getColor(), hostDto.getNombre(), clientHost, true, codigoSalaHost, coloresActivos);
        }
        dispose();
    }

    // ========================================================
    // LÓGICA DE NEGOCIO: UNIRSE A SALA Y LOBBY (CLIENTE)
    // ========================================================
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

        miNombreCliente = txtNombreCliente.getText().trim();
        if (miNombreCliente.isEmpty()) miNombreCliente = "Invitado";

        miColorCliente = rbRojoCli.isSelected() ? "Rojo" :
                         rbVerdeCli.isSelected() ? "Verde" :
                         rbAzulCli.isSelected() ? "Azul" : "Amarillo";

        final String finalIP = ip;
        final int finalPuerto = puerto;

        btnConectarCli.setEnabled(false);
        lblEstadoCli.setText("Conectando a " + finalIP + ":" + finalPuerto + "...");

        new Thread(() -> {
            clientCliente = new EventClient(finalIP, finalPuerto);
            if (!clientCliente.conectar()) {
                SwingUtilities.invokeLater(() -> {
                    lblEstadoCli.setText("❌ No se pudo conectar a " + finalIP + ":" + finalPuerto + ". Verifica que el Host haya abierto la sala.");
                    btnConectarCli.setEnabled(true);
                });
                return;
            }

            SwingUtilities.invokeLater(() -> {
                // Cambiar al Lobby del Cliente
                lblClienteLobbyTitulo.setText("🟢 Conectado a la Sala de Espera");
                lblClienteLobbyEstado.setText("Esperando a que el Host inicie la partida...");
                cardLayout.show(pnlContenedorPrincipal, "LOBBY_CLIENTE");

                // Suscribirse a eventos de la sala
                configurarSuscripcionCliente();

                // Enviar evento de unión al Host
                EventoDTO evUnido = new EventoDTO(
                        TipoEvento.JUGADOR_UNIDO,
                        miNombreCliente,
                        "SALA-01",
                        miColorCliente
                );
                clientCliente.publicarEvento(evUnido);
            });
        }).start();
    }

    private void configurarSuscripcionCliente() {
        clientCliente.suscribir(evento -> {
            if (evento == null) return;

            // Manejo de Errores de Negocio (Sala Llena [A1], Color Ocupado [A3])
            if (evento.getTipo() == TipoEvento.ERROR) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(DialogoModoRed.this,
                            evento.getPayload() != null ? evento.getPayload().toString() : "Error al unirse a la sala",
                            "Aviso de la Sala",
                            JOptionPane.WARNING_MESSAGE);
                    if (clientCliente != null) clientCliente.desconectar();
                    btnConectarCli.setEnabled(true);
                    cardLayout.show(pnlContenedorPrincipal, "CONFIG");
                });
                return;
            }

            // Actualización de la lista de jugadores en el Lobby
            if (evento.getTipo() == TipoEvento.SALA_ACTUALIZADA) {
                if (evento.getPayload() instanceof List<?> lista) {
                    SwingUtilities.invokeLater(() -> actualizarLobbyClienteUI(lista));
                }
            }

            // Inicio de la partida por el Host
            if (evento.getTipo() == TipoEvento.PARTIDA_INICIADA) {
                SwingUtilities.invokeLater(() -> {
                    @SuppressWarnings("unchecked")
                    List<String> colores = evento.getPayload() instanceof List<?> l ? (List<String>) l : Arrays.asList("Rojo", miColorCliente);
                    if (callback != null) {
                        callback.onPartidaIniciada(miColorCliente, miNombreCliente, clientCliente, false, "SALA-01", colores);
                    }
                    dispose();
                });
            }
        });
    }

    private void actualizarLobbyClienteUI(List<?> listaJugadores) {
        lblClienteLobbyEstado.setText("Jugadores en sala: " + listaJugadores.size() + ". Esperando que el Host inicie...");

        for (int i = 0; i < 4; i++) {
            if (i < listaJugadores.size()) {
                Object obj = listaJugadores.get(i);
                if (obj instanceof JugadorDTO j) {
                    String icono = j.isAnfitrion() ? "👑 " : "🎮 ";
                    slotLabelsCli[i].setText(icono + j.getNombre() + " (" + j.getColor() + ") - LISTO");
                    slotLabelsCli[i].setForeground(getColorAwt(j.getColor()));
                    slotCardsCli[i].setBackground(new Color(240, 253, 244));
                    slotCardsCli[i].setBorder(BorderFactory.createLineBorder(getColorAwt(j.getColor()), 2, true));
                }
            } else {
                slotLabelsCli[i].setText("⏳ Esperando jugador " + (i + 1) + "...");
                slotLabelsCli[i].setForeground(new Color(148, 163, 184));
                slotCardsCli[i].setBackground(Color.WHITE);
                slotCardsCli[i].setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2, true));
            }
        }
    }

    private Color getColorAwt(String color) {
        if (color == null) return Color.DARK_GRAY;
        return switch (color.toLowerCase().trim()) {
            case "rojo" -> new Color(220, 38, 38);
            case "verde" -> new Color(22, 163, 74);
            case "azul" -> new Color(37, 99, 235);
            case "amarillo" -> new Color(202, 138, 4);
            default -> Color.DARK_GRAY;
        };
    }
}
