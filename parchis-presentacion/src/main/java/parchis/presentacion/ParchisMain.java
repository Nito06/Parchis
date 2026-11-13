package parchis.presentacion;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada principal de la aplicación Parchís en Maven.
 */
public class ParchisMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ParchisFames().setVisible(true);
        });
    }
}
