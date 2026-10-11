package ui.matriz;

import model.ConversorGrafo;
import model.Grafo;
import model.MatrizInvalidaException;
import ui.dibujo.GrafoPanel;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.util.EventObject;

/**
 * Opcion 1: convertir una matriz de adyacencia en un grafo dibujado.
 *
 * <p>La matriz se edita con la tabla de la izquierda: todas sus celdas son
 * modificables y solo aceptan enteros mayores o iguales que 0. Con el grafo
 * no dirigido, editar una celda actualiza la simetrica sola. Los controles
 * permiten cambiar el tamano (conservando los datos), el tipo de grafo,
 * generar el grafo, llenar la matriz al azar, limpiar y copiar/pegar la
 * matriz como texto (para llevarla a la Opcion 2).</p>
 */
public class PanelOpcion1 extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int TAMANO_MINIMO = 0;
    private static final int TAMANO_MAXIMO = 12;
    private static final int TAMANO_INICIAL = 4;
    private static final double PROBABILIDAD_ALEATORIA = 0.4;

    private final MatrizTableModel modeloMatriz;
    private final JTable tablaMatriz;
    private final JSpinner spnTamano;
    private final JCheckBox chkDirigido;
    private final GrafoPanel panelGrafo;
    private final JLabel lblEstado;

    /** Construye la Opcion 1: matriz 4x4 en ceros, no dirigida, y su grafo dibujado. */
    public PanelOpcion1() {
        super(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloMatriz = new MatrizTableModel(TAMANO_INICIAL, true);
        tablaMatriz = crearTabla();
        panelGrafo = new GrafoPanel();
        panelGrafo.setBorder(BorderFactory.createTitledBorder("Grafo generado"));

        spnTamano = new JSpinner(new SpinnerNumberModel(TAMANO_INICIAL, TAMANO_MINIMO, TAMANO_MAXIMO, 1));
        spnTamano.addChangeListener(e -> cambiarTamano());

        chkDirigido = new JCheckBox("Grafo dirigido");
        chkDirigido.addActionListener(e -> cambiarTipoGrafo());

        lblEstado = new JLabel(" ");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        tablaMatriz.setFillsViewportHeight(true);
        JScrollPane scrollMatriz = new JScrollPane(tablaMatriz);
        scrollMatriz.setBorder(BorderFactory.createTitledBorder("Matriz de adyacencia (edite las celdas)"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollMatriz, panelGrafo);
        split.setResizeWeight(0.42);

        add(crearControles(), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);

        ajustarColumnas();
        generarGrafo(); // el panel abre mostrando el grafo inicial
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    /** Cambia el numero de vertices conservando los datos de la matriz. */
    private void cambiarTamano() {
        int nuevo = (Integer) spnTamano.getValue();
        if (nuevo != modeloMatriz.getTamano()) {
            modeloMatriz.redimensionar(nuevo);
            ajustarColumnas();
        }
    }

    /** Sincroniza el modo dirigido de la matriz con el checkbox. */
    private void cambiarTipoGrafo() {
        modeloMatriz.setSimetrica(!chkDirigido.isSelected());
    }

    /** Convierte la matriz actual en grafo y lo dibuja. */
    public boolean generarGrafo() {
        try {
<<<<<<< HEAD
=======
            // Pasa la matriz de la tabla por el conversor (que la valida) y
            // entrega el resultado al lienzo para que lo dibuje.
>>>>>>> Mariano
            Grafo nuevo = ConversorGrafo.desdeMatriz(modeloMatriz.getMatriz(), chkDirigido.isSelected());
            panelGrafo.setGrafo(nuevo);
            actualizarEstado();
            return true;
        } catch (MatrizInvalidaException ex) {
<<<<<<< HEAD
=======
            // La validación falló: se muestra el motivo al usuario en un diálogo.
>>>>>>> Mariano
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Matriz invalida",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Llena la matriz al azar y regenera el grafo. */
    public void generarAleatoria() {
        int n = modeloMatriz.getTamano();
        modeloMatriz.cargar(ConversorGrafo.aleatoria(n, chkDirigido.isSelected(), PROBABILIDAD_ALEATORIA));
        ajustarColumnas();
        generarGrafo();
    }

    /** Pone la matriz en ceros y regenera el grafo vacio. */
    public void limpiar() {
        modeloMatriz.cargar(ConversorGrafo.matrizVacia(modeloMatriz.getTamano()));
        ajustarColumnas();
        generarGrafo();
    }

    /** Copia la matriz al portapapeles como texto (tabulado por fila). */
    public void copiarMatriz() {
        try {
            String texto = ConversorGrafo.aTexto(modeloMatriz.getMatriz());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo acceder al portapapeles.",
                    "Copiar", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Pega una matriz de texto desde el portapapeles y la dibuja. */
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
            modeloMatriz.cargar(nueva);
            spnTamano.setValue(nueva.length);
            ajustarColumnas();
            generarGrafo();
        } catch (MatrizInvalidaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Matriz invalida",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el portapapeles: " + ex.getMessage(),
                    "Pegar", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ------------------------------------------------------------------
    // Construccion de la GUI
    // ------------------------------------------------------------------

    /** Fila de controles: tamano, tipo de grafo y botones de accion. */
    private JPanel crearControles() {
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controles.add(new JLabel("Vertices:"));
        controles.add(spnTamano);
        controles.add(chkDirigido);
        controles.add(crearBoton("Generar grafo", e -> generarGrafo()));
        controles.add(crearBoton("Aleatoria", e -> generarAleatoria()));
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

    /** Crea la tabla con el editor que solo admite digitos y celdas centradas. */
    private JTable crearTabla() {
        JTable tabla = new JTable(modeloMatriz);
        tabla.setRowHeight(26);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setDefaultEditor(Object.class, new EditorCeldaMatriz());
        tabla.setDefaultRenderer(Object.class, new RenderizadorCentrado());
        return tabla;
    }

    /** Ajusta el ancho preferido de las columnas al tamano actual. */
    private void ajustarColumnas() {
        TableColumnModel columnas = tablaMatriz.getColumnModel();
        int n = modeloMatriz.getTamano();
        for (int i = 0; i < columnas.getColumnCount(); i++) {
            columnas.getColumn(i).setPreferredWidth(i == 0 ? 64 : (n > 8 ? 40 : 52));
        }
    }

    /** Refresca la barra de estado con el resumen del grafo generado. */
    private void actualizarEstado() {
        Grafo g = panelGrafo.getGrafo();
        lblEstado.setText(String.format("Grafo %s con %d vertices y %d aristas",
                g.isDirigido() ? "dirigido" : "no dirigido",
                g.getNumVertices(), g.getAristas().size()));
    }

    // ------------------------------------------------------------------
    // Accesores utiles para las pruebas
    // ------------------------------------------------------------------

    /** Modelo de la tabla de la matriz. */
    public MatrizTableModel getModeloMatriz() {
        return modeloMatriz;
    }

    /** Lienzo donde se dibuja el grafo generado. */
    public GrafoPanel getPanelGrafo() {
        return panelGrafo;
    }

    /** Selector del numero de vertices de la matriz. */
    public JSpinner getSpnTamano() {
        return spnTamano;
    }

    /** Casilla que activa o desactiva el modo dirigido. */
    public JCheckBox getChkDirigido() {
        return chkDirigido;
    }

    // ------------------------------------------------------------------
    // Editor y renderizador de celdas
    // ------------------------------------------------------------------

    /** Celdas centradas, con la etiqueta de la fila algo mas tenue. */
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

    /** Editor que solo admite digitos (0-9) y maximo 4 caracteres. */
    private static final class EditorCeldaMatriz extends DefaultCellEditor {

        private static final long serialVersionUID = 1L;

        EditorCeldaMatriz() {
            super(new JTextField());
            setClickCountToStart(1);
            JTextField campo = (JTextField) getComponent();
            campo.setHorizontalAlignment(JTextField.CENTER);
            ((AbstractDocument) campo.getDocument()).setDocumentFilter(new FiltroDigitos());
        }

        @Override
        public Object getCellEditorValue() {
            String texto = ((JTextField) getComponent()).getText().trim();
            if (texto.isEmpty()) {
                return Integer.valueOf(0);
            }
            try {
                return Integer.valueOf(Integer.parseInt(texto));
            } catch (NumberFormatException e) {
                return Integer.valueOf(0);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable tabla, Object valor,
                                                     boolean seleccionada, int fila, int columna) {
            Component celda = super.getTableCellEditorComponent(tabla, valor, seleccionada, fila, columna);
            ((JTextField) getComponent()).selectAll();
            return celda;
        }
    }

    /** Filtro de documento que deja pasar unicamente digitos. */
    private static final class FiltroDigitos extends DocumentFilter {

        private static final int LONGITUD_MAXIMA = 4;

        @Override
        public void insertString(FilterBypass fb, int desplazamiento, String texto, AttributeSet atributos) {
            reemplazar(fb, desplazamiento, 0, texto);
        }

        @Override
        public void replace(FilterBypass fb, int desplazamiento, int longitud, String texto, AttributeSet atributos) {
            reemplazar(fb, desplazamiento, longitud, texto);
        }

        @Override
        public void remove(FilterBypass fb, int desplazamiento, int longitud) {
            try {
                fb.remove(desplazamiento, longitud);
            } catch (BadLocationException e) {
                // no deberia ocurrir: se ignora
            }
        }

        private void reemplazar(FilterBypass fb, int desplazamiento, int longitud, String texto) {
<<<<<<< HEAD
=======
            // Se quitan todos los caracteres que no sean digitos (0-9) del texto
            // que se intenta escribir, para que solo entren numeros.
>>>>>>> Mariano
            String digitos = texto.replaceAll("[^0-9]", "");
            try {
                Document doc = fb.getDocument();
                String actual = doc.getText(0, doc.getLength());
                StringBuilder resultado = new StringBuilder(actual);
                resultado.replace(desplazamiento, desplazamiento + longitud, digitos);
<<<<<<< HEAD
=======
                // Solo se aplica si el resultado sigue dentro del limite de
                // caracteres; si no, se descarta el cambio (no se escribe nada).
>>>>>>> Mariano
                if (resultado.length() <= LONGITUD_MAXIMA) {
                    fb.replace(desplazamiento, longitud, digitos, null);
                }
            } catch (BadLocationException e) {
                // no deberia ocurrir: se ignora
            }
        }
    }
}
