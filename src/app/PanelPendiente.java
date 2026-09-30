package app;

import model.ConversorGrafo;
import model.Grafo;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Panel provisional que se muestra en las pestanas mientras el modulo
 * correspondiente (Modulo 2 o Modulo 3) no este integrado en la rama main.
 *
 * <p>Sirve para dos cosas:</p>
 * <ul>
 *   <li>Recordar cual es el contrato que debe entregar ese modulo.</li>
 *   <li>Demostrar que el nucleo del Modulo 1 (Grafo + ConversorGrafo) ya
 *       funciona: la tabla de abajo se genera con el modelo real.</li>
 * </ul>
 *
 * <p>Cuando el panel real exista, {@link MainFrame} lo carga solo y este panel
 * deja de usarse; no hay que borrar nada.</p>
 */
public class PanelPendiente extends JPanel {

    private static final long serialVersionUID = 1L;

    public PanelPendiente(String titulo, String modulo, String claseEsperada,
                          String paquete, String entregables, String error) {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(crearEncabezado(titulo, modulo, claseEsperada, paquete, entregables, error), BorderLayout.NORTH);
        add(crearEjemploDelModelo(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado(String titulo, String modulo, String claseEsperada,
                                   String paquete, String entregables, String error) {
        JPanel cabecera = new JPanel(new GridLayout(0, 1, 0, 4));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, lblTitulo.getFont().getSize2D() + 4f));
        cabecera.add(lblTitulo);

        cabecera.add(new JLabel("Panel pendiente de integracion (" + modulo + ")."));
        cabecera.add(new JLabel("Se cargara automaticamente la clase: " + claseEsperada
                + "   [paquete " + paquete + "]"));
        cabecera.add(new JLabel("Entregables esperados: " + entregables));

        if (error != null) {
            JLabel lblError = new JLabel("No se pudo cargar el panel del modulo: " + error);
            lblError.setForeground(new Color(0xB0, 0x20, 0x20));
            cabecera.add(lblError);
        }
        return cabecera;
    }

    /**
     * Tabla de ejemplo construida con el modelo real del Modulo 1, para comprobar
     * que el nucleo (Grafo, ConversorGrafo, eventos) funciona.
     */
    private JPanel crearEjemploDelModelo() {
        Grafo demo = new Grafo(false);
        demo.agregarVertice("A");
        demo.agregarVertice("B");
        demo.agregarVertice("C");
        demo.setPeso(0, 1, 1); // A - B
        demo.setPeso(1, 2, 1); // B - C

        int[][] matriz = ConversorGrafo.aMatriz(demo);
        String[] etiquetas = demo.getEtiquetas().toArray(new String[0]);

        Object[] columnas = new Object[etiquetas.length + 1];
        columnas[0] = "Vertice";
        for (int j = 0; j < etiquetas.length; j++) {
            columnas[j + 1] = etiquetas[j];
        }

        Object[][] filas = new Object[etiquetas.length][etiquetas.length + 1];
        for (int i = 0; i < etiquetas.length; i++) {
            filas[i][0] = etiquetas[i];
            for (int j = 0; j < etiquetas.length; j++) {
                filas[i][j + 1] = matriz[i][j];
            }
        }

        JTable tabla = new JTable(new DefaultTableModel(filas, columnas) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        });
        tabla.setRowHeight(24);
        tabla.setEnabled(false);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createTitledBorder(
                "Ejemplo generado con el modelo del Modulo 1: Grafo + ConversorGrafo.aMatriz(grafo)"));
        contenedor.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return contenedor;
    }
}
