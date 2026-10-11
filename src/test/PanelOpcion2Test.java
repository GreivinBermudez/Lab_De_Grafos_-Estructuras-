package test;

import ui.editor.PanelOpcion2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Pruebas funcionales del panel de la Opcion 2: dibujar el grafo y generar
 * la matriz de adyacencia en tiempo real.
 */
class PanelOpcion2Test {

    private PanelOpcion2 crearPanel() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Requiere entorno grafico");
        AtomicReference<PanelOpcion2> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new PanelOpcion2()));
        return ref.get();
    }

    @Test
    @DisplayName("El panel abre con el lienzo vacio y la matriz sin filas")
    void panelAbreVacio() throws Exception {
        PanelOpcion2 panel = crearPanel();
        assertTrue(panel.getPanelEditor().getGrafo().estaVacio());
        assertEquals(0, panel.getModeloMatriz().getRowCount());
        assertEquals(1, panel.getModeloMatriz().getColumnCount());
        assertFalse(panel.getChkDirigido().isSelected());
    }

    @Test
    @DisplayName("La matriz se genera en vivo al dibujar dos vertices y una arista")
    void matrizEnVivo() throws Exception {
        PanelOpcion2 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> {
            panel.getPanelEditor().crearVerticeEn(new Point(50, 60));
            panel.getPanelEditor().crearVerticeEn(new Point(150, 60));
            panel.getPanelEditor().crearArista(0, 1);
        });
        assertEquals(2, panel.getModeloMatriz().getRowCount());
        assertEquals(3, panel.getModeloMatriz().getColumnCount());
        assertEquals(Integer.valueOf(1), panel.getModeloMatriz().getValueAt(0, 2)); // matriz[0][1]
        assertEquals(Integer.valueOf(1), panel.getModeloMatriz().getValueAt(1, 1)); // matriz[1][0]
        assertEquals("A", panel.getModeloMatriz().getValueAt(0, 0));
    }

    @Test
    @DisplayName("En modo dirigido la matriz generada es asimetrica")
    void matrizDirigida() throws Exception {
        PanelOpcion2 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> {
            panel.getPanelEditor().crearVerticeEn(new Point(50, 60));
            panel.getPanelEditor().crearVerticeEn(new Point(150, 60));
            panel.getChkDirigido().doClick(); // dispara el ActionListener
            panel.getPanelEditor().crearArista(0, 1);
        });
        assertTrue(panel.getPanelEditor().getGrafo().isDirigido());
        assertEquals(Integer.valueOf(1), panel.getModeloMatriz().getValueAt(0, 2));
        assertEquals(Integer.valueOf(0), panel.getModeloMatriz().getValueAt(1, 1));
    }

    @Test
    @DisplayName("La matriz generada es de solo lectura")
    void matrizNoEditable() throws Exception {
        PanelOpcion2 panel = crearPanel();
        assertFalse(panel.getModeloMatriz().isCellEditable(0, 0));
        assertFalse(panel.getModeloMatriz().isCellEditable(0, 1));
        assertFalse(panel.getModeloMatriz().isCellEditable(1, 2));
    }

    @Test
    @DisplayName("Limpiar borra el grafo y la matriz queda sin filas")
    void limpiar() throws Exception {
        PanelOpcion2 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> {
            panel.getPanelEditor().crearVerticeEn(new Point(50, 60));
            panel.getPanelEditor().crearVerticeEn(new Point(150, 60));
            panel.getPanelEditor().crearArista(0, 1);
            panel.limpiar();
        });
        assertTrue(panel.getPanelEditor().getGrafo().estaVacio());
        assertEquals(0, panel.getModeloMatriz().getRowCount());
    }
}
