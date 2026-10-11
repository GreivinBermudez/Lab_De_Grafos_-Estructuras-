package test;

import model.ConversorGrafo;
import model.Grafo;
import ui.editor.GrafoEditorPanel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Pruebas del lienzo interactivo de la Opcion 2. Los gestos del mouse se
 * prueban a traves de su API publica (la que invocan los manejadores).
 */
class GrafoEditorPanelTest {

    private GrafoEditorPanel crearEditor() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Requiere entorno grafico");
        AtomicReference<GrafoEditorPanel> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new GrafoEditorPanel()));
        return ref.get();
    }

    @Test
    @DisplayName("El editor abre vacio")
    void editorIniciaVacio() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        assertTrue(editor.getGrafo().estaVacio());
        assertEquals(0, editor.getGrafo().getNumVertices());
    }

    @Test
    @DisplayName("crearVerticeEn agrega el vertice y guarda su posicion")
    void crearVerticeEnPosicion() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        AtomicInteger indice = new AtomicInteger(-1);
        SwingUtilities.invokeAndWait(() -> indice.set(editor.crearVerticeEn(new Point(50, 60))));
        assertEquals(0, indice.get());
        assertEquals(1, editor.getGrafo().getNumVertices());
        assertEquals(new Point(50, 60), editor.getPosicion(0));
    }

    @Test
    @DisplayName("crearArista une dos vertices (simetrica en no dirigido)")
    void crearArista() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60));
            editor.crearVerticeEn(new Point(150, 60));
            assertTrue(editor.crearArista(0, 1));
        });
        Grafo grafo = editor.getGrafo();
        assertTrue(grafo.hayArista(0, 1));
        assertTrue(grafo.hayArista(1, 0));
        assertEquals(1, ConversorGrafo.aMatriz(grafo)[0][1]);
    }

    @Test
    @DisplayName("Arrastrar un vertice sobre si mismo no crea bucle")
    void aristaASiMismoNo() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60));
            assertFalse(editor.crearArista(0, 0));
        });
        assertFalse(editor.getGrafo().esBucle(0));
    }

    @Test
    @DisplayName("En dirigido la arista creada es una sola flecha")
    void dirigidoUnaFlecha() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60));
            editor.crearVerticeEn(new Point(150, 60));
            editor.getGrafo().setDirigido(true);
            assertTrue(editor.crearArista(0, 1));
        });
        Grafo grafo = editor.getGrafo();
        assertTrue(grafo.isDirigido());
        assertTrue(grafo.hayArista(0, 1));
        assertFalse(grafo.hayArista(1, 0));
    }

    @Test
    @DisplayName("Eliminar un vertice reajusta la matriz y las posiciones")
    void eliminarVerticeReajusta() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60));
            editor.crearVerticeEn(new Point(150, 60));
            editor.crearVerticeEn(new Point(250, 60));
            editor.crearArista(1, 2);
            assertTrue(editor.eliminarVerticeEn(new Point(50, 60)));
        });
        Grafo grafo = editor.getGrafo();
        assertEquals(2, grafo.getNumVertices());
        assertEquals("B", grafo.getEtiqueta(0));
        assertEquals("C", grafo.getEtiqueta(1));
        assertTrue(grafo.hayArista(0, 1)); // la arista B-C bajo de indice
        assertEquals(new Point(150, 60), editor.getPosicion(0));
        assertEquals(new Point(250, 60), editor.getPosicion(1));
        assertFalse(editor.eliminarVerticeEn(new Point(400, 400)));
    }

    @Test
    @DisplayName("eliminarAristaEn encuentra la arista por el punto medio")
    void eliminarAristaPorPunto() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60));
            editor.crearVerticeEn(new Point(150, 60));
            editor.crearArista(0, 1);
        });
        assertNull(editor.aristaEn(new Point(100, 200)));
        SwingUtilities.invokeAndWait(() -> assertTrue(editor.eliminarAristaEn(new Point(100, 60))));
        assertTrue(editor.getGrafo().getAristas().isEmpty());
        assertNull(editor.aristaEn(new Point(100, 60)));
    }

    @Test
    @DisplayName("cambiarPesoArista valida peso >= 1 y que la arista exista")
    void cambiarPeso() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60));
            editor.crearVerticeEn(new Point(150, 60));
            editor.crearVerticeEn(new Point(250, 60));
            editor.crearArista(0, 1);
            assertTrue(editor.cambiarPesoArista(0, 1, 7));
            assertFalse(editor.cambiarPesoArista(0, 1, 0));
            assertFalse(editor.cambiarPesoArista(0, 2, 5)); // no hay arista 0-2
        });
        assertEquals(7, editor.getGrafo().getPeso(0, 1));
    }

    @Test
    @DisplayName("Los cambios del editor notifican a los listeners")
    void notificaciones() throws Exception {
        GrafoEditorPanel editor = crearEditor();
        AtomicInteger avisos = new AtomicInteger();
        editor.getGrafo().addListener(avisos::incrementAndGet);
        SwingUtilities.invokeAndWait(() -> {
            editor.crearVerticeEn(new Point(50, 60)); // 1
            editor.crearVerticeEn(new Point(150, 60)); // 2
            editor.crearArista(0, 1);                 // 3
        });
        assertEquals(3, avisos.get());
    }
}
