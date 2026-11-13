package parchis.presentacion;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Tablero interactivo y clásico para Parchís.
 * Trazado mediante Java 2D (Graphics2D) con antialiasing, proporciones clásicas,
 * fichas tridimensionales estilizadas y animación de movimiento mediante hilos (Threads).
 */
public class TableroPanel extends JPanel {

    public interface TableroListener {
        void onMovimientoFinalizado();
        void onMensaje(String mensaje);
        void onFichaComida(String jugadorComedor, String jugadorComido);
        void onVictoria(String jugadorGanador);
        default void onFichaMovidaLocal(int jugador, int idFicha, int pasos) {}
    }

    public static final int JUGADOR_ROJO = 0;
    public static final int JUGADOR_VERDE = 1;
    public static final int JUGADOR_AZUL = 2;
    public static final int JUGADOR_AMARILLO = 3;

    public static final String[] NOMBRES_JUGADORES = {"Rojo", "Verde", "Azul", "Amarillo"};
    public static final Color[] COLORES_JUGADORES = {
        new Color(220, 38, 38),   // Rojo
        new Color(22, 163, 74),   // Verde
        new Color(37, 99, 235),   // Azul
        new Color(234, 179, 8)    // Amarillo
    };

    public static final Color[] COLORES_SUAVES = {
        new Color(254, 226, 226), // Rojo suave
        new Color(220, 252, 231), // Verde suave
        new Color(219, 234, 254), // Azul suave
        new Color(254, 249, 195)  // Amarillo suave
    };

    // Salidas oficiales según Parchís tradicional
    public static final int SALIDA_AMARILLO = 5;
    public static final int SALIDA_AZUL = 22;
    public static final int SALIDA_ROJO = 39;
    public static final int SALIDA_VERDE = 56;

    // Entradas a meta oficiales
    public static final int ENTRADA_AZUL = 17;
    public static final int ENTRADA_ROJO = 34;
    public static final int ENTRADA_VERDE = 51;
    public static final int ENTRADA_AMARILLO = 68;

    // Los 12 seguros del tablero
    public static final int[] SEGUROS = {5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68};

    // Coordenadas calculadas
    private final Point2D.Double[] casillasPista = new Point2D.Double[69]; // 1 a 68
    private final Point2D.Double[][] casillasMeta = new Point2D.Double[4][9]; // 4 jugadores x (1 a 8)
    private final Point2D.Double[][] nidosBases = new Point2D.Double[4][4]; // 4 jugadores x 4 fichas

    // Modelo de fichas
    public static class Ficha {
        public final int jugador; // 0..3
        public final int id;      // 0..3
        public boolean enBase = true;
        public int pasosRecorridos = -1; // 0 = casilla de salida, 63 = entrada a meta, 64..70 pasillo, 71 = coronada
        public boolean coronada = false;

        public double animX = 0;
        public double animY = 0;
        public boolean animando = false;

        public Ficha(int jugador, int id) {
            this.jugador = jugador;
            this.id = id;
        }

        public int getCasillaActual() {
            if (enBase || coronada || pasosRecorridos > 63) return -1;
            int salida = switch (jugador) {
                case JUGADOR_AMARILLO -> SALIDA_AMARILLO;
                case JUGADOR_AZUL -> SALIDA_AZUL;
                case JUGADOR_ROJO -> SALIDA_ROJO;
                case JUGADOR_VERDE -> SALIDA_VERDE;
                default -> 1;
            };
            return ((salida - 1 + pasosRecorridos) % 68) + 1;
        }
    }

    private final List<Ficha> todasLasFichas = new ArrayList<>();
    private final boolean[] jugadorActivo = new boolean[4];
    private int turnoActual = 0;
    private int valorDado = 0;
    private boolean esperandoMovimiento = false;
    private boolean animacionEnProgreso = false;
    private boolean partidaFinalizada = false;

    private TableroListener listener;
    private Ficha fichaHover = null;
    private float pulseAlpha = 0f;
    private boolean pulseGrowing = true;
    private javax.swing.Timer pulseTimer;

    public TableroPanel() {
        setPreferredSize(new Dimension(500, 500));
        setMinimumSize(new Dimension(500, 500));
        setBackground(new Color(248, 246, 240));

        calcularGeometria();
        inicializarFichas();
        iniciarTimerPulso();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarClick(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                manejarHover(e.getX(), e.getY());
            }
        });
    }

    public void setTableroListener(TableroListener listener) {
        this.listener = listener;
    }

    private void calcularGeometria() {
        // Pista exterior (Casillas 1 a 68)
        // Casillas 1 a 8: Brazo derecho, carril inferior (x: 500 -> 300, y ≈ 283.3)
        for (int i = 1; i <= 8; i++) {
            double x = 500.0 - (i - 0.5) * 25.0;
            double y = 283.33;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casillas 9 a 16: Brazo inferior, carril derecho (x ≈ 283.3, y: 300 -> 500)
        for (int i = 9; i <= 16; i++) {
            int k = i - 8;
            double x = 283.33;
            double y = 300.0 + (k - 0.5) * 25.0;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casilla 17: Extremo brazo inferior (Giro y seguro)
        casillasPista[17] = new Point2D.Double(250.0, 487.5);

        // Casillas 18 a 25: Brazo inferior, carril izquierdo (x ≈ 216.7, y: 500 -> 300)
        for (int i = 18; i <= 25; i++) {
            int k = i - 17;
            double x = 216.67;
            double y = 500.0 - (k - 0.5) * 25.0;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casillas 26 a 33: Brazo izquierdo, carril inferior (x: 200 -> 0, y ≈ 283.3)
        for (int i = 26; i <= 33; i++) {
            int k = i - 25;
            double x = 200.0 - (k - 0.5) * 25.0;
            double y = 283.33;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casilla 34: Extremo brazo izquierdo (Giro y seguro)
        casillasPista[34] = new Point2D.Double(12.5, 250.0);

        // Casillas 35 a 42: Brazo izquierdo, carril superior (x: 0 -> 200, y ≈ 216.7)
        for (int i = 35; i <= 42; i++) {
            int k = i - 34;
            double x = (k - 0.5) * 25.0;
            double y = 216.67;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casillas 43 a 50: Brazo superior, carril izquierdo (x ≈ 216.7, y: 200 -> 0)
        for (int i = 43; i <= 50; i++) {
            int k = i - 42;
            double x = 216.67;
            double y = 200.0 - (k - 0.5) * 25.0;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casilla 51: Extremo brazo superior (Giro y seguro)
        casillasPista[51] = new Point2D.Double(250.0, 12.5);

        // Casillas 52 a 59: Brazo superior, carril derecho (x ≈ 283.3, y: 0 -> 200)
        for (int i = 52; i <= 59; i++) {
            int k = i - 51;
            double x = 283.33;
            double y = (k - 0.5) * 25.0;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casillas 60 a 67: Brazo derecho, carril superior (x: 300 -> 500, y ≈ 216.7)
        for (int i = 60; i <= 67; i++) {
            int k = i - 59;
            double x = 300.0 + (k - 0.5) * 25.0;
            double y = 216.67;
            casillasPista[i] = new Point2D.Double(x, y);
        }

        // Casilla 68: Extremo brazo derecho (Giro y seguro)
        casillasPista[68] = new Point2D.Double(487.5, 250.0);

        // Pasillos de meta (1 a 7 pasos de pasillo + 8 triángulo central)
        // Amarillo (Brazo derecho hacia el centro x: 500 -> 250, y=250)
        for (int k = 1; k <= 7; k++) {
            casillasMeta[JUGADOR_AMARILLO][k] = new Point2D.Double(487.5 - k * 25.0, 250.0);
        }
        casillasMeta[JUGADOR_AMARILLO][8] = new Point2D.Double(278.0, 250.0); // Triángulo centro

        // Azul (Brazo inferior hacia el centro y: 500 -> 250, x=250)
        for (int k = 1; k <= 7; k++) {
            casillasMeta[JUGADOR_AZUL][k] = new Point2D.Double(250.0, 487.5 - k * 25.0);
        }
        casillasMeta[JUGADOR_AZUL][8] = new Point2D.Double(250.0, 278.0); // Triángulo centro

        // Rojo (Brazo izquierdo hacia el centro x: 0 -> 250, y=250)
        for (int k = 1; k <= 7; k++) {
            casillasMeta[JUGADOR_ROJO][k] = new Point2D.Double(12.5 + k * 25.0, 250.0);
        }
        casillasMeta[JUGADOR_ROJO][8] = new Point2D.Double(222.0, 250.0); // Triángulo centro

        // Verde (Brazo superior hacia el centro y: 0 -> 250, x=250)
        for (int k = 1; k <= 7; k++) {
            casillasMeta[JUGADOR_VERDE][k] = new Point2D.Double(250.0, 12.5 + k * 25.0);
        }
        casillasMeta[JUGADOR_VERDE][8] = new Point2D.Double(250.0, 222.0); // Triángulo centro

        // Nidos de las bases (4 fichas por base)
        double[][] centrosBases = {
            {100.0, 100.0}, // Rojo (Arriba Izq)
            {400.0, 100.0}, // Verde (Arriba Der)
            {100.0, 400.0}, // Azul (Abajo Izq)
            {400.0, 400.0}  // Amarillo (Abajo Der)
        };

        double offset = 32.0;
        for (int j = 0; j < 4; j++) {
            double cx = centrosBases[j][0];
            double cy = centrosBases[j][1];
            nidosBases[j][0] = new Point2D.Double(cx - offset, cy - offset);
            nidosBases[j][1] = new Point2D.Double(cx + offset, cy - offset);
            nidosBases[j][2] = new Point2D.Double(cx - offset, cy + offset);
            nidosBases[j][3] = new Point2D.Double(cx + offset, cy + offset);
        }
    }

    private void inicializarFichas() {
        todasLasFichas.clear();
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                Ficha f = new Ficha(j, i);
                Point2D.Double pt = nidosBases[j][i];
                f.animX = pt.x;
                f.animY = pt.y;
                todasLasFichas.add(f);
            }
        }
    }

    private void iniciarTimerPulso() {
        pulseTimer = new javax.swing.Timer(40, e -> {
            if (esperandoMovimiento && !animacionEnProgreso) {
                if (pulseGrowing) {
                    pulseAlpha += 0.05f;
                    if (pulseAlpha >= 0.85f) pulseGrowing = false;
                } else {
                    pulseAlpha -= 0.05f;
                    if (pulseAlpha <= 0.25f) pulseGrowing = true;
                }
                repaint();
            }
        });
        pulseTimer.start();
    }

    public void configurarPartida(int numJugadores, String colorHumano) {
        this.partidaFinalizada = false;
        for (int i = 0; i < 4; i++) jugadorActivo[i] = false;

        int indiceHumano = 0;
        if (colorHumano.equalsIgnoreCase("Rojo")) indiceHumano = 0;
        else if (colorHumano.equalsIgnoreCase("Verde")) indiceHumano = 1;
        else if (colorHumano.equalsIgnoreCase("Azul")) indiceHumano = 2;
        else if (colorHumano.equalsIgnoreCase("Amarillo")) indiceHumano = 3;

        jugadorActivo[indiceHumano] = true;

        if (numJugadores == 2) {
            jugadorActivo[(indiceHumano + 2) % 4] = true;
        } else if (numJugadores == 3) {
            jugadorActivo[(indiceHumano + 1) % 4] = true;
            jugadorActivo[(indiceHumano + 2) % 4] = true;
        } else {
            for (int i = 0; i < 4; i++) jugadorActivo[i] = true;
        }

        inicializarFichas();
        repaint();
    }

    public void configurarPartidaConJugadores(boolean[] activos) {
        this.partidaFinalizada = false;
        for (int i = 0; i < 4; i++) {
            jugadorActivo[i] = activos != null && i < activos.length && activos[i];
        }
        inicializarFichas();
        repaint();
    }

    public void setTurno(int turno, int dado) {
        if (partidaFinalizada) return;
        this.turnoActual = turno;
        this.valorDado = dado;
        this.esperandoMovimiento = true;

        // Comprobar si hay movimientos posibles
        List<Ficha> movibles = getFichasMovibles(turno, dado);
        if (movibles.isEmpty()) {
            this.esperandoMovimiento = false;
            if (listener != null) {
                listener.onMensaje("¡No tienes movimientos válidos con el " + dado + "! Pasa el turno.");
                new Thread(() -> {
                    try {
                        Thread.sleep(1100);
                    } catch (InterruptedException ignored) {}
                    SwingUtilities.invokeLater(() -> {
                        if (!partidaFinalizada && listener != null) {
                            listener.onMovimientoFinalizado();
                        }
                    });
                }).start();
            }
        } else {
            if (listener != null) {
                listener.onMensaje("¡Salió un " + dado + "! Haz clic en una ficha resaltada para moverla.");
            }
        }
        repaint();
    }

    public boolean isAnimacionEnProgreso() {
        return animacionEnProgreso;
    }

    public int getFichasEnBase(int jugador) {
        int count = 0;
        for (Ficha f : todasLasFichas) {
            if (f.jugador == jugador && f.enBase) count++;
        }
        return count;
    }

    public int getFichasCoronadas(int jugador) {
        int count = 0;
        for (Ficha f : todasLasFichas) {
            if (f.jugador == jugador && f.coronada) count++;
        }
        return count;
    }

    public List<Ficha> getFichasMovibles(int jugador, int dado) {
        List<Ficha> lista = new ArrayList<>();
        if (!jugadorActivo[jugador] || dado <= 0) return lista;

        for (Ficha f : todasLasFichas) {
            if (f.jugador != jugador || f.coronada) continue;

            if (f.enBase) {
                // En Parchís oficial sales de la base con un 5
                if (dado == 5) {
                    lista.add(f);
                }
            } else {
                // En pista o meta: no debe pasarse del centro (paso 71)
                if (f.pasosRecorridos + dado <= 71) {
                    lista.add(f);
                }
            }
        }
        return lista;
    }

    private Point2D.Double getPosicionFicha(Ficha f) {
        if (f.animando) {
            return new Point2D.Double(f.animX, f.animY);
        }
        if (f.enBase) {
            return nidosBases[f.jugador][f.id];
        }
        if (f.coronada) {
            return casillasMeta[f.jugador][8];
        }
        if (f.pasosRecorridos <= 63) {
            int casilla = f.getCasillaActual();
            return casillasPista[casilla];
        } else {
            int pasoMeta = f.pasosRecorridos - 63; // 1 a 8
            if (pasoMeta > 8) pasoMeta = 8;
            return casillasMeta[f.jugador][pasoMeta];
        }
    }

    private void manejarHover(int mx, int my) {
        if (!esperandoMovimiento || animacionEnProgreso) {
            setCursor(Cursor.getDefaultCursor());
            fichaHover = null;
            return;
        }

        List<Ficha> movibles = getFichasMovibles(turnoActual, valorDado);
        Ficha hovered = null;
        for (Ficha f : movibles) {
            Point2D.Double pt = getPosicionFicha(f);
            if (pt.distance(mx, my) <= 15) {
                hovered = f;
                break;
            }
        }

        if (hovered != fichaHover) {
            fichaHover = hovered;
            setCursor(fichaHover != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }
    }

    private void manejarClick(int mx, int my) {
        if (partidaFinalizada || !esperandoMovimiento || animacionEnProgreso) return;

        List<Ficha> movibles = getFichasMovibles(turnoActual, valorDado);
        for (Ficha f : movibles) {
            Point2D.Double pt = getPosicionFicha(f);
            if (pt.distance(mx, my) <= 18) {
                if (listener != null) {
                    listener.onFichaMovidaLocal(f.jugador, f.id, valorDado);
                }
                ejecutarMovimientoConHilo(f, valorDado);
                break;
            }
        }
    }

    public boolean isPartidaFinalizada() {
        return partidaFinalizada;
    }

    public void setPartidaFinalizada(boolean partidaFinalizada) {
        this.partidaFinalizada = partidaFinalizada;
        if (partidaFinalizada) {
            this.esperandoMovimiento = false;
            this.animacionEnProgreso = false;
            setCursor(Cursor.getDefaultCursor());
            repaint();
        }
    }

    public Ficha getFicha(int jugador, int id) {
        for (Ficha f : todasLasFichas) {
            if (f.jugador == jugador && f.id == id) {
                return f;
            }
        }
        return null;
    }

    public void moverFichaRemota(int jugador, int idFicha, int pasos) {
        Ficha f = getFicha(jugador, idFicha);
        if (f != null) {
            this.valorDado = pasos;
            this.turnoActual = jugador;
            ejecutarMovimientoConHilo(f, pasos);
        }
    }

    /**
     * Mueve la ficha casilla a casilla suavemente utilizando un hilo secundario (Thread).
     */
    public void ejecutarMovimientoConHilo(Ficha ficha, int pasos) {
        esperandoMovimiento = false;
        animacionEnProgreso = true;
        setCursor(Cursor.getDefaultCursor());

        Thread hiloMovimiento = new Thread(() -> {
            try {
                if (ficha.enBase) {
                    // Sale de la base a la casilla de salida
                    ficha.animando = true;
                    Point2D.Double inicio = nidosBases[ficha.jugador][ficha.id];
                    int salida = switch (ficha.jugador) {
                        case JUGADOR_AMARILLO -> SALIDA_AMARILLO;
                        case JUGADOR_AZUL -> SALIDA_AZUL;
                        case JUGADOR_ROJO -> SALIDA_ROJO;
                        case JUGADOR_VERDE -> SALIDA_VERDE;
                        default -> 1;
                    };
                    Point2D.Double destino = casillasPista[salida];

                    // Animación suave de salto de base a salida
                    int frames = 12;
                    for (int i = 1; i <= frames; i++) {
                        double t = (double) i / frames;
                        ficha.animX = inicio.x + (destino.x - inicio.x) * t;
                        ficha.animY = inicio.y + (destino.y - inicio.y) * t;
                        SwingUtilities.invokeLater(this::repaint);
                        Thread.sleep(20);
                    }

                    ficha.enBase = false;
                    ficha.pasosRecorridos = 0;
                    ficha.animando = false;
                    SwingUtilities.invokeLater(this::repaint);
                    Thread.sleep(120);

                    // Verificar captura en la casilla de salida
                    verificarCaptura(ficha);

                } else {
                    // Movimiento casilla por casilla en la pista o meta
                    ficha.animando = true;
                    for (int p = 1; p <= pasos; p++) {
                        Point2D.Double puntoAnterior = getCoordenadaPorPasos(ficha.jugador, ficha.pasosRecorridos);
                        ficha.pasosRecorridos++;
                        Point2D.Double puntoSiguiente = getCoordenadaPorPasos(ficha.jugador, ficha.pasosRecorridos);

                        // Animación suave entre casilla y casilla
                        int framesPorCasilla = 7;
                        for (int f = 1; f <= framesPorCasilla; f++) {
                            double t = (double) f / framesPorCasilla;
                            ficha.animX = puntoAnterior.x + (puntoSiguiente.x - puntoAnterior.x) * t;
                            ficha.animY = puntoAnterior.y + (puntoSiguiente.y - puntoAnterior.y) * t;
                            SwingUtilities.invokeLater(this::repaint);
                            Thread.sleep(18);
                        }

                        if (ficha.pasosRecorridos >= 71) {
                            ficha.coronada = true;
                            break;
                        }
                    }

                    ficha.animando = false;
                    SwingUtilities.invokeLater(this::repaint);
                    Thread.sleep(100);

                    if (ficha.coronada) {
                        if (listener != null) {
                            listener.onMensaje("¡Una ficha de " + NOMBRES_JUGADORES[ficha.jugador] + " ha entrado a la meta!");
                        }
                    } else {
                        // Verificar captura
                        verificarCaptura(ficha);
                    }
                }

                // Verificar si el jugador actual ganó (coronó sus 4 fichas)
                if (getFichasCoronadas(ficha.jugador) == 4) {
                    this.partidaFinalizada = true;
                    this.esperandoMovimiento = false;
                    animacionEnProgreso = false;
                    SwingUtilities.invokeLater(() -> {
                        repaint();
                        if (listener != null) {
                            listener.onVictoria(NOMBRES_JUGADORES[ficha.jugador]);
                        }
                    });
                    return;
                }

            } catch (InterruptedException ex) {
                ficha.animando = false;
            } finally {
                animacionEnProgreso = false;
                SwingUtilities.invokeLater(() -> {
                    repaint();
                    if (!partidaFinalizada && listener != null) {
                        listener.onMovimientoFinalizado();
                    }
                });
            }
        });

        hiloMovimiento.setDaemon(true);
        hiloMovimiento.start();
    }

    private Point2D.Double getCoordenadaPorPasos(int jugador, int pasos) {
        if (pasos <= 63) {
            int salida = switch (jugador) {
                case JUGADOR_AMARILLO -> SALIDA_AMARILLO;
                case JUGADOR_AZUL -> SALIDA_AZUL;
                case JUGADOR_ROJO -> SALIDA_ROJO;
                case JUGADOR_VERDE -> SALIDA_VERDE;
                default -> 1;
            };
            int casilla = ((salida - 1 + pasos) % 68) + 1;
            return casillasPista[casilla];
        } else {
            int metaStep = pasos - 63;
            if (metaStep > 8) metaStep = 8;
            return casillasMeta[jugador][metaStep];
        }
    }

    private void verificarCaptura(Ficha fichaMovida) {
        if (fichaMovida.pasosRecorridos > 63) return; // En meta está seguro

        int casilla = fichaMovida.getCasillaActual();
        if (esSeguro(casilla)) return; // Casilla de seguro

        for (Ficha rival : todasLasFichas) {
            if (rival.jugador != fichaMovida.jugador && !rival.enBase && !rival.coronada && rival.pasosRecorridos <= 63) {
                if (rival.getCasillaActual() == casilla) {
                    // ¡Captura! Rival vuelve a base
                    rival.enBase = true;
                    rival.pasosRecorridos = -1;
                    Point2D.Double nido = nidosBases[rival.jugador][rival.id];
                    rival.animX = nido.x;
                    rival.animY = nido.y;

                    if (listener != null) {
                        listener.onFichaComida(NOMBRES_JUGADORES[fichaMovida.jugador], NOMBRES_JUGADORES[rival.jugador]);
                    }
                    break;
                }
            }
        }
    }

    private boolean esSeguro(int casilla) {
        for (int s : SEGUROS) {
            if (s == casilla) return true;
        }
        return false;
    }

    // =========================================================================
    // DIBUJADO DEL TABLERO (Graphics2D)
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Antialiasing y calidad de renderizado
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fondo general
        g2.setColor(new Color(248, 246, 240));
        g2.fillRect(0, 0, w, h);

        // 1. Trazar las 4 bases
        dibujarBases(g2);

        // 2. Trazar los 4 pasillos de meta y el centro
        dibujarPasillosYCentro(g2);

        // 3. Trazar la cuadrícula de las 68 casillas exteriores
        dibujarCasillasPista(g2);

        // 4. Dibujar los nidos de fichas en cada base
        dibujarNidosBases(g2);

        // 5. Dibujar las fichas interactivas
        dibujarFichas(g2);

        // Borde exterior elegante
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(1, 1, w - 3, h - 3);

        g2.dispose();
    }

    private void dibujarBases(Graphics2D g2) {
        int baseSize = 200;

        // Arriba Izquierda: ROJO
        dibujarBaseEsquina(g2, 0, 0, baseSize, baseSize, COLORES_JUGADORES[JUGADOR_ROJO], "ROJO");

        // Arriba Derecha: VERDE
        dibujarBaseEsquina(g2, 300, 0, baseSize, baseSize, COLORES_JUGADORES[JUGADOR_VERDE], "VERDE");

        // Abajo Izquierda: AZUL
        dibujarBaseEsquina(g2, 0, 300, baseSize, baseSize, COLORES_JUGADORES[JUGADOR_AZUL], "AZUL");

        // Abajo Derecha: AMARILLO
        dibujarBaseEsquina(g2, 300, 300, baseSize, baseSize, COLORES_JUGADORES[JUGADOR_AMARILLO], "AMARILLO");
    }

    private void dibujarBaseEsquina(Graphics2D g2, int x, int y, int bw, int bh, Color colorBase, String texto) {
        // Marco de color
        g2.setColor(colorBase);
        g2.fillRect(x, y, bw, bh);

        // Línea divisoria oscura
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, bw, bh);

        // Zona circular blanca interior
        int pad = 24;
        int diam = bw - (pad * 2);
        g2.setColor(Color.WHITE);
        g2.fillOval(x + pad, y + pad, diam, diam);

        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x + pad, y + pad, diam, diam);

        // Texto decorativo central
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(texto);
        int th = fm.getAscent();
        g2.setColor(colorBase.darker());
        g2.drawString(texto, x + (bw - tw) / 2, y + (bh + th) / 2 - 3);
    }

    private void dibujarPasillosYCentro(Graphics2D g2) {
        // Centro (100x100) en (200, 200)
        // 4 Triángulos convergentes
        int cx = 250;
        int cy = 250;

        // Triángulo Rojo (Izquierda)
        Polygon triRojo = new Polygon(new int[]{200, cx, 200}, new int[]{200, cy, 300}, 3);
        g2.setColor(COLORES_JUGADORES[JUGADOR_ROJO]);
        g2.fill(triRojo);

        // Triángulo Verde (Arriba)
        Polygon triVerde = new Polygon(new int[]{200, cx, 300}, new int[]{200, cy, 200}, 3);
        g2.setColor(COLORES_JUGADORES[JUGADOR_VERDE]);
        g2.fill(triVerde);

        // Triángulo Amarillo (Derecha)
        Polygon triAmarillo = new Polygon(new int[]{300, cx, 300}, new int[]{200, cy, 300}, 3);
        g2.setColor(COLORES_JUGADORES[JUGADOR_AMARILLO]);
        g2.fill(triAmarillo);

        // Triángulo Azul (Abajo)
        Polygon triAzul = new Polygon(new int[]{200, cx, 300}, new int[]{300, cy, 300}, 3);
        g2.setColor(COLORES_JUGADORES[JUGADOR_AZUL]);
        g2.fill(triAzul);

        // Bordes de los triángulos
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(triRojo);
        g2.draw(triVerde);
        g2.draw(triAmarillo);
        g2.draw(triAzul);

        // Emblema central de victoria
        g2.setColor(new Color(255, 215, 0));
        g2.fillOval(cx - 12, cy - 12, 24, 24);
        g2.setColor(new Color(30, 30, 30));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - 12, cy - 12, 24, 24);

        // Pasillos de meta
        // Pasillo Verde (Arriba: x de 233.3 a 266.7, y de 0 a 200)
        dibujarPasilloVertical(g2, 233.33, 0, 33.33, 200, COLORES_JUGADORES[JUGADOR_VERDE], true);

        // Pasillo Azul (Abajo: x de 233.3 a 266.7, y de 300 a 500)
        dibujarPasilloVertical(g2, 233.33, 300, 33.33, 200, COLORES_JUGADORES[JUGADOR_AZUL], false);

        // Pasillo Rojo (Izquierda: y de 233.3 a 266.7, x de 0 a 200)
        dibujarPasilloHorizontal(g2, 0, 233.33, 200, 33.33, COLORES_JUGADORES[JUGADOR_ROJO], true);

        // Pasillo Amarillo (Derecha: y de 233.3 a 266.7, x de 300 a 500)
        dibujarPasilloHorizontal(g2, 300, 233.33, 200, 33.33, COLORES_JUGADORES[JUGADOR_AMARILLO], false);
    }

    private void dibujarPasilloVertical(Graphics2D g2, double x, double y, double w, double h, Color c, boolean baja) {
        for (int i = 0; i < 7; i++) {
            double sy = baja ? y + (i + 1) * 25.0 : y + (7 - i - 1) * 25.0;
            g2.setColor(c);
            g2.fill(new Rectangle2D.Double(x, sy, w, 25.0));

            g2.setColor(new Color(30, 30, 30));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new Rectangle2D.Double(x, sy, w, 25.0));

            // Número en blanco
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString(String.valueOf(i + 1), (float) (x + w / 2 - 3), (float) (sy + 16));
        }
    }

    private void dibujarPasilloHorizontal(Graphics2D g2, double x, double y, double w, double h, Color c, boolean vaDerecha) {
        for (int i = 0; i < 7; i++) {
            double sx = vaDerecha ? x + (i + 1) * 25.0 : x + (7 - i - 1) * 25.0;
            g2.setColor(c);
            g2.fill(new Rectangle2D.Double(sx, y, 25.0, h));

            g2.setColor(new Color(30, 30, 30));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new Rectangle2D.Double(sx, y, 25.0, h));

            // Número en blanco
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString(String.valueOf(i + 1), (float) (sx + 10), (float) (y + h / 2 + 4));
        }
    }

    private void dibujarCasillasPista(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.2f));

        // Dibujar las 68 casillas con líneas y marcas de seguro / salida
        for (int i = 1; i <= 68; i++) {
            Point2D.Double pt = casillasPista[i];
            boolean seguro = esSeguro(i);
            boolean salida = (i == SALIDA_ROJO || i == SALIDA_VERDE || i == SALIDA_AZUL || i == SALIDA_AMARILLO);

            // Casilla de salida
            if (salida) {
                Color colSalida = switch (i) {
                    case SALIDA_ROJO -> COLORES_SUAVES[JUGADOR_ROJO];
                    case SALIDA_VERDE -> COLORES_SUAVES[JUGADOR_VERDE];
                    case SALIDA_AZUL -> COLORES_SUAVES[JUGADOR_AZUL];
                    case SALIDA_AMARILLO -> COLORES_SUAVES[JUGADOR_AMARILLO];
                    default -> Color.WHITE;
                };
                g2.setColor(colSalida);
                dibujarRectanguloCasilla(g2, i, true);

                // Círculo coloreado en el centro
                Color circulo = switch (i) {
                    case SALIDA_ROJO -> COLORES_JUGADORES[JUGADOR_ROJO];
                    case SALIDA_VERDE -> COLORES_JUGADORES[JUGADOR_VERDE];
                    case SALIDA_AZUL -> COLORES_JUGADORES[JUGADOR_AZUL];
                    case SALIDA_AMARILLO -> COLORES_JUGADORES[JUGADOR_AMARILLO];
                    default -> Color.BLACK;
                };
                g2.setColor(circulo);
                g2.fillOval((int) pt.x - 7, (int) pt.y - 7, 14, 14);

            } else if (seguro) {
                // Seguro estándar
                g2.setColor(new Color(230, 230, 235));
                dibujarRectanguloCasilla(g2, i, true);

                // Círculo gris de seguro
                g2.setColor(new Color(120, 120, 130));
                g2.fillOval((int) pt.x - 6, (int) pt.y - 6, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillOval((int) pt.x - 2, (int) pt.y - 2, 4, 4);

            } else {
                // Casilla normal
                dibujarRectanguloCasilla(g2, i, false);
            }

            // Borde de la casilla
            g2.setColor(new Color(40, 40, 40));
            dibujarRectanguloCasilla(g2, i, false);

            // Número sutil de casilla
            if (i % 5 == 0 && !salida) {
                g2.setColor(new Color(130, 130, 130));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                g2.drawString(String.valueOf(i), (float) pt.x - 5, (float) pt.y + 3);
            }
        }
    }

    private void dibujarRectanguloCasilla(Graphics2D g2, int i, boolean rellenar) {
        // Determina el ancho y alto del rectángulo según el brazo
        double x, y, bw, bh;
        Point2D.Double pt = casillasPista[i];

        if (i == 17 || i == 51) {
            // Extremos verticales
            bw = 33.33;
            bh = 25.0;
            x = pt.x - bw / 2;
            y = pt.y - bh / 2;
        } else if (i == 34 || i == 68) {
            // Extremos horizontales
            bw = 25.0;
            bh = 33.33;
            x = pt.x - bw / 2;
            y = pt.y - bh / 2;
        } else if ((i >= 1 && i <= 8) || (i >= 26 && i <= 33) || (i >= 35 && i <= 42) || (i >= 60 && i <= 67)) {
            // Brazos horizontales: casillas de 25px ancho x 33.33px alto
            bw = 25.0;
            bh = 33.33;
            x = pt.x - bw / 2;
            y = pt.y - bh / 2;
        } else {
            // Brazos verticales: casillas de 33.33px ancho x 25px alto
            bw = 33.33;
            bh = 25.0;
            x = pt.x - bw / 2;
            y = pt.y - bh / 2;
        }

        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, bw, bh);
        if (rellenar) {
            g2.fill(rect);
        } else {
            g2.draw(rect);
        }
    }

    private void dibujarNidosBases(Graphics2D g2) {
        int r = 13;
        for (int j = 0; j < 4; j++) {
            Color colorBorde = COLORES_JUGADORES[j];
            for (int i = 0; i < 4; i++) {
                Point2D.Double pt = nidosBases[j][i];
                // Círculo de nido
                g2.setColor(new Color(240, 240, 240));
                g2.fillOval((int) pt.x - r, (int) pt.y - r, r * 2, r * 2);

                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval((int) pt.x - r, (int) pt.y - r, r * 2, r * 2);
            }
        }
    }

    private void dibujarFichas(Graphics2D g2) {
        List<Ficha> movibles = (esperandoMovimiento && !animacionEnProgreso)
                ? getFichasMovibles(turnoActual, valorDado)
                : List.of();

        // 1. Resaltado / pulso de fichas seleccionables
        for (Ficha f : movibles) {
            Point2D.Double pt = getPosicionFicha(f);
            int radioGlow = 18;
            g2.setColor(new Color(255, 215, 0, (int) (pulseAlpha * 255)));
            g2.setStroke(new BasicStroke(3.5f));
            g2.drawOval((int) pt.x - radioGlow, (int) pt.y - radioGlow, radioGlow * 2, radioGlow * 2);
        }

        // 2. Dibujar cada ficha con apariencia 3D
        for (Ficha f : todasLasFichas) {
            if (!jugadorActivo[f.jugador]) continue;

            Point2D.Double pt = getPosicionFicha(f);
            double fx = pt.x;
            double fy = pt.y;

            // Manejar solapamiento de dos fichas en la misma casilla
            if (!f.enBase && !f.coronada && !f.animando) {
                int casilla = f.getCasillaActual();
                for (Ficha otra : todasLasFichas) {
                    if (otra != f && !otra.enBase && !otra.coronada && !otra.animando && otra.getCasillaActual() == casilla) {
                        if (f.id > otra.id || (f.id == otra.id && f.jugador > otra.jugador)) {
                            fx += 5;
                            fy -= 5;
                        } else {
                            fx -= 5;
                            fy += 5;
                        }
                        break;
                    }
                }
            }

            dibujarFichaIndividual(g2, f, fx, fy, f == fichaHover);
        }
    }

    private void dibujarFichaIndividual(Graphics2D g2, Ficha f, double x, double y, boolean hover) {
        int radio = hover ? 12 : 11;
        int diametro = radio * 2;

        // Sombra suave proyectada
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval((int) x - radio + 2, (int) y - radio + 3, diametro, diametro);

        // Degradado radial 3D para efecto de ficha de plástico/madera
        Color base = COLORES_JUGADORES[f.jugador];
        Color claro = base.brighter();
        Color oscuro = base.darker();

        RadialGradientPaint rgp = new RadialGradientPaint(
                new Point2D.Double(x - radio * 0.35, y - radio * 0.35),
                (float) radio * 1.3f,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{claro, base, oscuro}
        );
        g2.setPaint(rgp);
        g2.fillOval((int) x - radio, (int) y - radio, diametro, diametro);

        // Borde oscuro
        g2.setColor(new Color(25, 25, 25));
        g2.setStroke(new BasicStroke(hover ? 2f : 1.5f));
        g2.drawOval((int) x - radio, (int) y - radio, diametro, diametro);

        // Anillo interior sutil
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval((int) x - (radio - 4), (int) y - (radio - 4), (radio - 4) * 2, (radio - 4) * 2);

        // Número de ficha en el centro
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String txt = String.valueOf(f.id + 1);
        int tw = fm.stringWidth(txt);
        int th = fm.getAscent();
        g2.drawString(txt, (float) (x - tw / 2.0), (float) (y + th / 2.0 - 2));
    }
}
