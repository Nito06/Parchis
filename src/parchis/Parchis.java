
package parchis;

import javax.swing.SwingUtilities;

public class Parchis {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ParchisFames().setVisible(true);
        });
    }
}
