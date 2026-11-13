
package parchis.presentacion;

import com.parchis.dtos.JugadorDTO;
import com.parchis.dtos.SalaDTO;
import javax.swing.JOptionPane;

public class SeleccionPanel extends javax.swing.JPanel {
    // Comunicación con la ventana principal
    public interface NavegacionListener {
        void iniciarPartida(int numeroJugadores, String color);
        void volverAlMenu();
    }

    private NavegacionListener listener;

    // Grupos para evitar seleccionar varias opciones
    private javax.swing.ButtonGroup grupoColores;
    private javax.swing.ButtonGroup grupoJugadores;
    
  public SeleccionPanel() {
    this(null);
}

public SeleccionPanel(NavegacionListener listener) {

    initComponents();

    this.listener = listener;

    // Crear grupos de selección
    grupoColores = new javax.swing.ButtonGroup();

    grupoColores.add(rbRojo);
    grupoColores.add(rbVerde);
    grupoColores.add(rbAzul);
    grupoColores.add(rbAmarillo);

    grupoJugadores = new javax.swing.ButtonGroup();

    grupoJugadores.add(rbDos);
    grupoJugadores.add(rbTres);
    grupoJugadores.add(rbCuatro);

    // Conectar botones
    btnAceptar.addActionListener(e -> aceptarSeleccion());

    btnVolver.addActionListener(e -> volverMenu());
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        rbRojo = new javax.swing.JRadioButton();
        rbVerde = new javax.swing.JRadioButton();
        rbAzul = new javax.swing.JRadioButton();
        rbAmarillo = new javax.swing.JRadioButton();
        lblJugadores = new javax.swing.JLabel();
        rbDos = new javax.swing.JRadioButton();
        rbTres = new javax.swing.JRadioButton();
        rbCuatro = new javax.swing.JRadioButton();
        btnAceptar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("Selecciona tu color");

        rbRojo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbRojo.setForeground(new java.awt.Color(255, 0, 0));
        rbRojo.setText("Rojo");
        rbRojo.addActionListener(this::rbRojoActionPerformed);

        rbVerde.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbVerde.setForeground(new java.awt.Color(0, 150, 0));
        rbVerde.setText("Verde");

        rbAzul.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbAzul.setForeground(new java.awt.Color(0, 0, 255));
        rbAzul.setText("Azul");

        rbAmarillo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbAmarillo.setForeground(new java.awt.Color(200, 160, 0));
        rbAmarillo.setText("Amarillo");

        lblJugadores.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblJugadores.setText("Numeros de Jugadores");

        rbDos.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbDos.setText("2");

        rbTres.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbTres.setText("3");

        rbCuatro.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        rbCuatro.setText("4");

        btnAceptar.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnAceptar.setText("Aceptar");

        btnVolver.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        btnVolver.setText("Volver");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(64, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnVolver)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAceptar))
                    .addComponent(lblTitulo))
                .addGap(47, 47, 47))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbRojo)
                            .addComponent(rbVerde))
                        .addGap(53, 53, 53)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbAmarillo)
                            .addComponent(rbAzul)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblJugadores)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(rbDos)
                                .addGap(18, 18, 18)
                                .addComponent(rbTres)
                                .addGap(18, 18, 18)
                                .addComponent(rbCuatro)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbRojo)
                    .addComponent(rbAzul))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbAmarillo)
                    .addComponent(rbVerde))
                .addGap(18, 18, 18)
                .addComponent(lblJugadores)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbDos)
                    .addComponent(rbTres)
                    .addComponent(rbCuatro))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVolver)
                    .addComponent(btnAceptar))
                .addGap(21, 21, 21))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbRojoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbRojoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rbRojoActionPerformed

// FUNCION DEL BOTON ACEPTAR
private void aceptarSeleccion() {

    // Comprobar que se haya seleccionado un color
    if (grupoColores.getSelection() == null) {

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Por favor selecciona un color.",
            "Advertencia",
            javax.swing.JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    // Comprobar que se haya seleccionado el numero de jugadores
    if (grupoJugadores.getSelection() == null) {

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Por favor selecciona el numero de jugadores.",
            "Advertencia",
            javax.swing.JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    // Obtener el color seleccionado
    String color = "";

    if (rbRojo.isSelected()) {
        color = "Rojo";
    } 
    else if (rbVerde.isSelected()) {
        color = "Verde";
    } 
    else if (rbAzul.isSelected()) {
        color = "Azul";
    } 
    else if (rbAmarillo.isSelected()) {
        color = "Amarillo";
    }

    // Obtener el numero de jugadores
    int numeroJugadores = 0;

    if (rbDos.isSelected()) {
        numeroJugadores = 2;
    } 
    else if (rbTres.isSelected()) {
        numeroJugadores = 3;
    } 
    else if (rbCuatro.isSelected()) {
        numeroJugadores = 4;
    }

    // Enviar los datos a la siguiente pantalla
    // Conexión mediante ControlSala (patrón Fachada + Fábrica)
    final int finalNum = numeroJugadores;
    final String finalColor = color;
    ControlSala controlSala = new ControlSala();
    controlSala.crearSala("Jugador 1", finalColor, finalNum, new ControlSala.VistaSalaCallback() {
        @Override
        public void onOperacionExitosa(SalaDTO sala, JugadorDTO jugadorActual) {
            if (listener != null) {
                listener.iniciarPartida(finalNum, finalColor);
            }
        }

        @Override
        public void onErrorNegocio(String mensajeError) {
            JOptionPane.showMessageDialog(SeleccionPanel.this, mensajeError, "Aviso de Sala (Flujo Alterno)", JOptionPane.WARNING_MESSAGE);
        }
    });
    if (false) {
        listener.iniciarPartida(numeroJugadores, color);

    } else {

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "¡Configuracion realizada correctamente!\n\n"
            + "Color: " + color + "\n"
            + "Jugadores: " + numeroJugadores
            + "\n\nLa pantalla de juego aun no esta conectada."
        );
    }
}


// FUNCION DEL BOTON VOLVER
private void volverMenu() {

    if (listener != null) {

        listener.volverAlMenu();

    } else {

        javax.swing.JOptionPane.showMessageDialog(
            this,
            "El menu principal aun no esta conectado."
        );
    }
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel lblJugadores;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JRadioButton rbAmarillo;
    private javax.swing.JRadioButton rbAzul;
    private javax.swing.JRadioButton rbCuatro;
    private javax.swing.JRadioButton rbDos;
    private javax.swing.JRadioButton rbRojo;
    private javax.swing.JRadioButton rbTres;
    private javax.swing.JRadioButton rbVerde;
    // End of variables declaration//GEN-END:variables
}
