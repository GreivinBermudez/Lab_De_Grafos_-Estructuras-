package test;

import model.Grafo;
import ui.dibujo.GrafoPanel;
import ui.matriz.PanelOpcion1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Prueba funcional del panel de la Opcion 1: matriz editable a grafo.
 * Se omite sola si no hay entorno grafico.
 */
class PanelOpcion1Test {

    private PanelOpcion1 crearPanel() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Requiere entorno grafico");
        AtomicReference<PanelOpcion1> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new PanelOpcion1()));
        return ref.get();
    }

    @Test
    @DisplayName("El panel abre con la matriz 4x4 y el grafo inicial dibujado")
    void panelInicial() throws Exception {
        PanelOpcion1 panel = crearPanel();
        assertEquals(4, panel.getModeloMatriz().getTamano());
        Grafo grafo = panel.getPanelGrafo().getGrafo();
        assertEquals(4, grafo.getNumVertices());
        assertTrue(grafo.getAristas().isEmpty());
    }

    @Test
    @DisplayName("Editar una celda y generar crea la arista en los dos sentidos")
    void editarYGenerar() throws Exception {
        PanelOpcion1 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> panel.getModeloMatriz().setValueAt(1, 0, 2)); // matriz[0][1]
        SwingUtilities.invokeAndWait(panel::generarGrafo);
        Grafo grafo = panel.getPanelGrafo().getGrafo();
        assertTrue(grafo.hayArista(0, 1));
        assertTrue(grafo.hayArista(1, 0));
        assertEquals(1, grafo.getAristas().size());
    }

    @Test
    @DisplayName("Modo dirigido: una sola flecha, sin celda simetrica")
    void modoDirigido() throws Exception {
        PanelOpcion1 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> panel.getChkDirigido().doClick()); // doClick dispara el ActionListener
        SwingUtilities.invokeAndWait(() -> panel.getModeloMatriz().setValueAt(1, 0, 2));
        SwingUtilities.invokeAndWait(panel::generarGrafo);
        Grafo grafo = panel.getPanelGrafo().getGrafo();
        assertTrue(grafo.isDirigido());
        assertTrue(grafo.hayArista(0, 1));
        assertFalse(grafo.hayArista(1, 0));
    }

    @Test
    @DisplayName("Cambiar el tamano conserva la matriz y el grafo se regenera")
    void cambiarTamano() throws Exception {
        PanelOpcion1 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> panel.getModeloMatriz().setValueAt(1, 0, 2));
        SwingUtilities.invokeAndWait(() -> panel.getSpnTamano().setValue(6));
        assertEquals(6, panel.getModeloMatriz().getTamano());
        assertEquals(1, panel.getModeloMatriz().getMatriz()[0][1]);
        SwingUtilities.invokeAndWait(panel::generarGrafo);
        Grafo grafo = panel.getPanelGrafo().getGrafo();
        assertEquals(6, grafo.getNumVertices());
        assertTrue(grafo.hayArista(0, 1));
    }

    @Test
    @DisplayName("Limpiar deja la matriz en ceros y el grafo sin aristas")
    void limpiar() throws Exception {
        PanelOpcion1 panel = crearPanel();
        SwingUtilities.invokeAndWait(() -> panel.getModeloMatriz().setValueAt(1, 0, 2));
        SwingUtilities.invokeAndWait(panel::limpiar);
        assertEquals(0, panel.getModeloMatriz().getMatriz()[0][1]);
        assertTrue(panel.getPanelGrafo().getGrafo().getAristas().isEmpty());
        assertEquals(4, panel.getPanelGrafo().getGrafo().getNumVertices());
    }
}
