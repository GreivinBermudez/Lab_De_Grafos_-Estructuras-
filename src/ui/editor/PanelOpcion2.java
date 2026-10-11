package ui.editor;

import model.ConversorGrafo;
import model.Grafo;
import model.GrafoListener;
import model.MatrizInvalidaException;
import ui.matriz.MatrizTableModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;

/**
 * Opcion 2: dibujar un grafo con el mouse y generar su matriz de adyacencia.
 *
 * <p>A la izquierda esta el lienzo interactivo ({@link GrafoEditorPanel}) y a
 * la derecha la matriz generada, de solo lectura, que se regenera en tiempo
 * real en cada cambio del grafo (via {@link GrafoListener}). Los controles
 * permiten alternar dirigido/no dirigido, limpiar y copiar/pegar la matriz
 * como texto para enlazar con la Opcion 1.</p>
 */
public class PanelOpcion2 extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Maximo de vertices al pegar una matriz (el editor no limita el dibujo). */
    private static final int TAMANO_MAXIMO = 30;

    private final GrafoEditorPanel panelEditor;
    private final MatrizTableModel modeloMatriz;
    private final JTable tablaMatriz;
    private final JCheckBox chkDirigido;
    private final JLabel lblEstado;
    /** Escucha el grafo actual para regenerar la matriz en vivo. */
    private final GrafoListener oyenteMatriz = this::refrescarMatriz;

    /** Construye la Opcion 2: lienzo vacio y la matriz generada al lado. */
    public PanelOpcion2() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelEditor = new GrafoEditorPanel();
        panelEditor.setBorder(BorderFactory.createTitledBorder("Lienzo: dibuje el grafo con el mouse"));

        modeloMatriz = new MatrizTableModel(0, false) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false; // la matriz de la Opcion 2 es generada, no editable
            }
        };
        tablaMatriz = new JTable(modeloMatriz);
        tablaMatriz.setRowHeight(26);
        tablaMatriz.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaMatriz.setFillsViewportHeight(true);
        tablaMatriz.setDefaultRenderer(Object.class, new RenderizadorCentrado());

        chkDirigido = new JCheckBox("Grafo dirigido");
        chkDirigido.addActionListener(e -> aplicarTipoGrafo());

        lblEstado = new JLabel(" ");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JScrollPane scrollMatriz = new JScrollPane(tablaMatriz);
        scrollMatriz.setBorder(BorderFactory.createTitledBorder("Matriz de adyacencia generada"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEditor, scrollMatriz);
        split.setResizeWeight(0.58);

        add(crearControles(), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);

        panelEditor.getGrafo().addListener(oyenteMatriz);
        refrescarMatriz();
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    /** Aplica el tipo de grafo (dirigido/no dirigido) seleccionado en la casilla. */
    private void aplicarTipoGrafo() {
<<<<<<< HEAD
=======
        // Simplemente cambia la bandera del grafo; el propio Grafo se encarga
        // de notificar a los oyentes y de simetrizar la matriz si hace falta.
>>>>>>> Mariano
        panelEditor.getGrafo().setDirigido(chkDirigido.isSelected());
    }

    /** Elimina todo lo dibujado (la matriz se regenera sola). */
    public void limpiar() {
        panelEditor.limpiarTodo();
    }

    /** Copia la matriz generada al portapapeles como texto. */
    public void copiarMatriz() {
        try {
            String texto = ConversorGrafo.aTexto(ConversorGrafo.aMatriz(panelEditor.getGrafo()));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo acceder al portapapeles.",
                    "Copiar", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Pega una matriz de texto desde el portapapeles y la convierte en el
     * grafo dibujado (los vertices se colocan en circulo automaticamente).
     */
    public void pegarMatriz() {
        try {
            Transferable contenido = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (contenido == null || !contenido.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                JOptionPane.showMessageDialog(this, "El portapapeles no contiene una matriz de texto.",
                        "Pegar", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String texto = (String) contenido.getTransferData(DataFlavor.stringFlavor);
            int[][] nueva = ConversorGrafo.desdeTexto(texto, chkDirigido.isSelected());
            if (nueva.length > TAMANO_MAXIMO) {
                JOptionPane.showMessageDialog(this,
                        "La matriz pegada excede el maximo de " + TAMANO_MAXIMO + " vertices.",
                        "Pegar", JOptionPane.WARNING_MESSAGE);
                return;
            }
            adoptarGrafo(ConversorGrafo.desdeMatriz(nueva, chkDirigido.isSelected()));
        } catch (MatrizInvalidaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Matriz invalida",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el portapapeles: " + ex.getMessage(),
                    "Pegar", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Sustituye el grafo del editor (al Pegar) y reengancha la matriz en vivo. */
    private void adoptarGrafo(Grafo nuevo) {
        Grafo anterior = panelEditor.getGrafo();
        if (anterior != null) {
<<<<<<< HEAD
            anterior.removeListener(oyenteMatriz);
        }
        panelEditor.setGrafo(nuevo); // el editor reengancha su propio listener
        nuevo.addListener(oyenteMatriz);
=======
            // Se deja de escuchar al grafo viejo para no acumular oyentes
            // huérfanos (sino, al pegar varias veces se dispararía el refresco
            // varias veces por cada cambio).
            anterior.removeListener(oyenteMatriz);
        }
        panelEditor.setGrafo(nuevo); // el editor reengancha su propio listener
        nuevo.addListener(oyenteMatriz); // y aquí se conecta la matriz al nuevo
>>>>>>> Mariano
        refrescarMatriz();
    }

    // ------------------------------------------------------------------
    // Actualizacion de la matriz y del estado
    // ------------------------------------------------------------------

    /** Regenera la matriz y el estado a partir del grafo actual. */
    private void refrescarMatriz() {
        modeloMatriz.cargar(ConversorGrafo.aMatriz(panelEditor.getGrafo()));
        ajustarColumnas();
        actualizarEstado();
    }

    /** Ajusta el ancho preferido de las columnas al tamano actual. */
    private void ajustarColumnas() {
        TableColumnModel columnas = tablaMatriz.getColumnModel();
        int n = modeloMatriz.getTamano();
        for (int i = 0; i < columnas.getColumnCount(); i++) {
            columnas.getColumn(i).setPreferredWidth(i == 0 ? 64 : (n > 8 ? 40 : 52));
        }
    }

    /** Refresca la barra de estado con el resumen del grafo dibujado. */
    private void actualizarEstado() {
        Grafo g = panelEditor.getGrafo();
        lblEstado.setText(String.format("Grafo %s con %d vertices y %d aristas",
                g.isDirigido() ? "dirigido" : "no dirigido",
                g.getNumVertices(), g.getAristas().size()));
    }

    // ------------------------------------------------------------------
    // Construccion de la GUI
    // ------------------------------------------------------------------

    /** Fila de controles: tipo de grafo y botones de accion. */
    private JPanel crearControles() {
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controles.add(chkDirigido);
        controles.add(crearBoton("Limpiar", e -> limpiar()));
        controles.add(crearBoton("Copiar", e -> copiarMatriz()));
        controles.add(crearBoton("Pegar", e -> pegarMatriz()));
        return controles;
    }

    /** Crea un boton con su accion asociada. */
    private JButton crearBoton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        return boton;
    }

    // ------------------------------------------------------------------
    // Accesores utiles para las pruebas
    // ------------------------------------------------------------------

    /** Lienzo donde se dibuja el grafo con el mouse. */
    public GrafoEditorPanel getPanelEditor() {
        return panelEditor;
    }

    /** Modelo de solo lectura con la matriz generada. */
    public MatrizTableModel getModeloMatriz() {
        return modeloMatriz;
    }

    /** Casilla que activa o desactiva el modo dirigido. */
    public JCheckBox getChkDirigido() {
        return chkDirigido;
    }

    /** Celdas centradas, igual que en la Opcion 1. */
    private static final class RenderizadorCentrado extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                                                      boolean seleccionada, boolean conFoco,
                                                      int fila, int columna) {
            Component celda = super.getTableCellRendererComponent(tabla, valor, seleccionada, conFoco, fila, columna);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (columna == 0) {
                celda.setForeground(new Color(0x60, 0x60, 0x60));
            }
            return celda;
        }
    }
}
