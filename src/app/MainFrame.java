package app;

import model.Grafo;

import javax.swing.JFrame;
import javax.swing.JComponent;
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
 * Ventana principal (Modulo 1). Contiene las dos opciones del proyecto en
 * pestanas y el menu de ayuda.
 *
 * <p>Integracion con los otros modulos: los paneles se cargan por reflexion
 * desde sus paquetes ({@code ui.matriz.PanelOpcion1} y {@code ui.editor.PanelOpcion2}).
 * Mientras esas clases no existan se muestra un {@link PanelPendiente}, de modo
 * que la ventana siempre abre sin errores y la integracion (etapa 3 del plan) no
 * requiere tocar este archivo.</p>
 *
 * <p>Si el constructor es {@code (Grafo)} se le pasa el grafo compartido; si el
 * panel real define un constructor vacio, tambien se acepta.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Clase que entregara el Modulo 2 (Opcion 1: matriz a grafo). */
    public static final String CLASE_OPCION1 = "ui.matriz.PanelOpcion1";
    /** Clase que entregara el Modulo 3 (Opcion 2: dibujar grafo). */
    public static final String CLASE_OPCION2 = "ui.editor.PanelOpcion2";

    private final transient Grafo grafo;
    private final JTabbedPane pestanas;

    public MainFrame() {
        super("Grafo y matriz de adyacencia");
        this.grafo = new Grafo(false);
        this.pestanas = new JTabbedPane();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(crearBarraDeMenu());

        pestanas.addTab("Opcion 1 - Matriz a grafo", crearPanelDeModulo(
                CLASE_OPCION1,
                "Opcion 1 - Matriz a grafo",
                "Modulo 2",
                "ui.matriz y ui.dibujo",
                "PanelMatriz, MatrizTableModel, GrafoPanel (solo lectura), PanelOpcion1"));
        pestanas.setToolTipTextAt(0, "Editar la matriz de adyacencia y ver el grafo dibujado (Modulo 2)");

        pestanas.addTab("Opcion 2 - Dibujar grafo", crearPanelDeModulo(
                CLASE_OPCION2,
                "Opcion 2 - Dibujar grafo",
                "Modulo 3",
                "ui.editor",
                "GrafoEditorPanel (extiende GrafoPanel), PanelOpcion2 y la tabla de la matriz generada"));
        pestanas.setToolTipTextAt(1, "Dibujar vertices y aristas con el mouse y ver la matriz generada (Modulo 3)");

        setContentPane(pestanas);
        setMinimumSize(new Dimension(960, 640));
        pack();
        setLocationRelativeTo(null);
    }

    /** Modelo compartido por las dos opciones (lo usa el panel que acepta {@code (Grafo)}). */
    public Grafo getGrafo() {
        return grafo;
    }

    /** Pestanas de la ventana (se usa en las pruebas de integracion). */
    public JTabbedPane getPestanas() {
        return pestanas;
    }

    /** Atajo para el numero de opciones visibles en la ventana principal. */
    public int getNumeroOpciones() {
        return pestanas.getTabCount();
    }

    // ------------------------------------------------------------------
    // Integracion con los Modulos 2 y 3
    // ------------------------------------------------------------------

    /**
     * Intenta crear el panel del modulo indicado. Si la clase todavia no existe
     * (o falla al construirse) devuelve un {@link PanelPendiente} explicativo en
     * lugar de lanzar una excepcion: la ventana principal nunca debe romperse.
     */
    private JComponent crearPanelDeModulo(String nombreClase, String titulo, String modulo,
                                          String paquete, String entregables) {
        String error = null;
        try {
            Class<?> clase = Class.forName(nombreClase);
            try {
                return (JComponent) clase.getConstructor(Grafo.class).newInstance(grafo);
            } catch (NoSuchMethodException sinConstructorConGrafo) {
                try {
                    return (JComponent) clase.getConstructor().newInstance();
                } catch (NoSuchMethodException sinConstructorVacio) {
                    error = nombreClase + " no tiene constructor (Grafo) ni constructor vacio";
                }
            }
        } catch (ClassNotFoundException aunNoIntegrado) {
            error = null; // todavia no existe: es normal durante el desarrollo en paralelo
        } catch (ReflectiveOperationException fallo) {
            Throwable causa = fallo;
            if (fallo instanceof java.lang.reflect.InvocationTargetException
                    && ((java.lang.reflect.InvocationTargetException) fallo).getTargetException() != null) {
                causa = ((java.lang.reflect.InvocationTargetException) fallo).getTargetException();
            }
            error = causa.getClass().getSimpleName() + ": " + causa.getMessage();
        } catch (ClassCastException noEsComponenteSwing) {
            error = nombreClase + " no extiende javax.swing.JComponent";
        }
        return new PanelPendiente(titulo, modulo, nombreClase, paquete, entregables, error);
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    private JMenuBar crearBarraDeMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemSalir.addActionListener(e -> dispose());
        menuArchivo.add(itemSalir);

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
                + "  Edite las celdas de la matriz de adyacencia (0 = sin arista, >0 = arista/peso),\n"
                + "  cambie el tamano con los controles y pulse \"Generar grafo\".\n"
                + "  En grafos no dirigidos la celda simetrica se actualiza sola.\n\n"
                + "Opcion 2 - Dibujar grafo:\n"
                + "  Clic en zona vacia: crea un vertice. Arrastrar un vertice: lo mueve.\n"
                + "  Arrastrar de un vertice a otro: crea una arista. Clic derecho: eliminar.\n"
                + "  La matriz de adyacencia se muestra y actualiza en tiempo real.";
        JOptionPane.showMessageDialog(this, texto, "Como usar la aplicacion", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAcercaDe() {
        String texto = "Grafo y matriz de adyacencia\n"
                + "Aplicacion de escritorio en Java (Swing, Java2D).\n\n"
                + "Trabajo por modulos:\n"
                + "  Modulo 1 - Nucleo y aplicacion principal (model, app, test)\n"
                + "  Modulo 2 - Opcion 1: matriz a grafo (ui.matriz, ui.dibujo)\n"
                + "  Modulo 3 - Opcion 2: dibujar grafo (ui.editor)\n\n"
                + "Version del nucleo: 1.0";
        JOptionPane.showMessageDialog(this, texto, "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }
}
