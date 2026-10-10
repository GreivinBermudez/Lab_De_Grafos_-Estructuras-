package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicacion (Modulo 1).
 *
 * <p>Abre la ventana principal con las dos opciones del proyecto:
 * Opcion 1 (matriz a grafo, Modulo 2) y Opcion 2 (dibujar grafo, Modulo 3).</p>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Se intenta el look and feel del sistema; si falla se usa el de Java.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el look and feel del sistema: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
