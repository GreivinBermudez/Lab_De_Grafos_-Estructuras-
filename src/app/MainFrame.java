package app;

import ui.editor.PanelOpcion2;
import ui.matriz.PanelOpcion1;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Ventana principal. Contiene las dos opciones del proyecto en pestanas
 * (Opcion 1: matriz de adyacencia a grafo; Opcion 2: dibujar grafo y
 * generar la matriz) mas el menu de Archivo y Ayuda.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JTabbedPane pestanas;

    public MainFrame() {
        super("Grafo y matriz de adyacencia");
        this.pestanas = new JTabbedPane();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(crearBarraDeMenu());

        pestanas.addTab("Opcion 1 - Matriz a grafo", new PanelOpcion1());
        pestanas.setToolTipTextAt(0, "Editar la matriz de adyacencia y ver el grafo dibujado");

        pestanas.addTab("Opcion 2 - Dibujar grafo", new PanelOpcion2());
        pestanas.setToolTipTextAt(1, "Dibujar vertices y aristas con el mouse y ver la matriz generada");

        setContentPane(pestanas);
        setMinimumSize(new Dimension(960, 640));
        pack();
        setLocationRelativeTo(null);
    }

    /** Pestanas de la ventana (se usan en las pruebas de integracion). */
    public JTabbedPane getPestanas() {
        return pestanas;
    }

    /** Atajo para el numero de opciones visibles en la ventana principal. */
    public int getNumeroOpciones() {
        return pestanas.getTabCount();
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    private JMenuBar crearBarraDeMenu() {
        JMenuBar barra = new JMenuBar();

        // Menu Archivo: mnemonico Alt+A y atajo Ctrl+Q para salir.
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemSalir.addActionListener(e -> dispose()); // cierra solo esta ventana
        menuArchivo.add(itemSalir);

        // Menu Ayuda: dos dialogos informativos con un separador entre ellos.
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic(KeyEvent.VK_Y);
        JMenuItem itemComoUsar = new JMenuItem("Como usar la aplicacion");
        itemComoUsar.addActionListener(e -> mostrarComoUsar());
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de");
        itemAcercaDe.addActionListener(e -> mostrarAcercaDe());
        menuAyuda.add(itemComoUsar);
        menuAyuda.addSeparator();
        menuAyuda.add(itemAcercaDe);

        barra.add(menuArchivo);
        barra.add(menuAyuda);
        return barra;
    }

    private void mostrarComoUsar() {
        String texto = "Opcion 1 - Matriz a grafo:\n"
                + "  Edite las celdas de la matriz (0 = sin arista, >0 = arista/peso), cambie\n"
                + "  el tamano con el selector y pulse \"Generar grafo\". En grafos no\n"
                + "  dirigidos la celda simetrica se actualiza sola.\n\n"
                + "Opcion 2 - Dibujar grafo:\n"
                + "  Clic en zona vacia: crea un vertice. Arrastrar un vertice: lo mueve.\n"
                + "  Arrastrar de un vertice a otro: crea una arista. Clic derecho: eliminar.\n"
                + "  Doble clic en un vertice: cambia su etiqueta; en una arista: su peso.\n"
                + "  La matriz de adyacencia se genera y actualiza en tiempo real.\n\n"
                + "Consejo: Copiar y Pegar permiten llevar la matriz de una opcion a la otra.";
        JOptionPane.showMessageDialog(this, texto, "Como usar la aplicacion",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAcercaDe() {
        String texto = "Grafo y matriz de adyacencia\n"
                + "Aplicacion de escritorio en Java (Swing, Java2D).\n\n"
                + "Opcion 1: convertir una matriz de adyacencia editable en un grafo dibujado.\n"
                + "Opcion 2: dibujar un grafo con el mouse y generar su matriz de adyacencia.\n\n"
                + "Estructura: app (ventana principal), model (Grafo y conversor),\n"
                + "ui.matriz y ui.dibujo (Opcion 1), ui.editor (Opcion 2), test (JUnit 5).";
        JOptionPane.showMessageDialog(this, texto, "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }
}
