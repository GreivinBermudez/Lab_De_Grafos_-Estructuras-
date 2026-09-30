package test;

import model.Arista;
import model.Grafo;
import model.GrafoListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del modelo {@link Grafo} (Modulo 1).
 */
class GrafoTest {

    private Grafo grafo;

    @BeforeEach
    void preparar() {
        grafo = new Grafo(false);
    }

    @Test
    @DisplayName("Grafo nuevo: 0 vertices y sin aristas")
    void grafoVacio() {
        assertEquals(0, grafo.getNumVertices());
        assertTrue(grafo.estaVacio());
        assertTrue(grafo.getAristas().isEmpty());
        assertFalse(grafo.isDirigido());
    }

    @Test
    @DisplayName("Un solo vertice sin aristas")
    void unVertice() {
        int indice = grafo.agregarVertice("A");
        assertEquals(0, indice);
        assertEquals(1, grafo.getNumVertices());
        assertEquals("A", grafo.getEtiqueta(0));
        assertEquals(0, grafo.getGrado(0));
        assertTrue(grafo.getAristas().isEmpty());
    }

    @Test
    @DisplayName("Etiquetas por defecto A, B, C y etiqueta V26 a partir del 27")
    void etiquetasPorDefecto() {
        grafo.agregarVertice(null);
        grafo.agregarVertice("");
        grafo.agregarVertice("   ");
        grafo.agregarVertice("D");
        assertEquals("A", grafo.getEtiqueta(0));
        assertEquals("B", grafo.getEtiqueta(1));
        assertEquals("C", grafo.getEtiqueta(2));
        assertEquals("D", grafo.getEtiqueta(3));
        assertEquals("V26", Grafo.etiquetaPorDefecto(26));
    }

    @Test
    @DisplayName("No dirigido: setPeso actualiza tambien la celda simetrica")
    void noDirigidoEsSimetrico() {
        grafo.agregarVertice("A");
        grafo.agregarVertice("B");
        grafo.setPeso(0, 1, 1);
        assertEquals(1, grafo.getPeso(0, 1));
        assertEquals(1, grafo.getPeso(1, 0));
        assertTrue(grafo.hayArista(1, 0));
        assertEquals(1, grafo.getGrado(0));
        assertEquals(1, grafo.getGrado(1));
        assertEquals(1, grafo.getAristas().size());
    }

    @Test
    @DisplayName("Dirigido: cada celda es independiente (una sola flecha A -> B)")
    void dirigidoNoEsSimetrico() {
        Grafo dirigido = new Grafo(true);
        dirigido.agregarVertice("A");
        dirigido.agregarVertice("B");
        dirigido.setPeso(0, 1, 1);
        assertEquals(1, dirigido.getPeso(0, 1));
        assertEquals(0, dirigido.getPeso(1, 0));
        assertFalse(dirigido.hayArista(1, 0));
        assertEquals(1, dirigido.getGrado(0));
        assertEquals(1, dirigido.getGrado(1)); // grado combinado: B recibe la arista A -> B
        assertEquals(1, dirigido.getGradoSalida(0));
        assertEquals(0, dirigido.getGradoEntrada(0));
        assertEquals(0, dirigido.getGradoSalida(1));
        assertEquals(1, dirigido.getGradoEntrada(1));
    }

    @Test
    @DisplayName("Bucle en la diagonal: cuenta 2 en el grado y se lista una vez")
    void bucle() {
        grafo.agregarVertice("A");
        grafo.agregarVertice("B");
        grafo.setPeso(0, 0, 1);
        assertTrue(grafo.esBucle(0));
        assertFalse(grafo.esBucle(1));
        assertEquals(2, grafo.getGrado(0));
        List<Arista> aristas = grafo.getAristas();
        assertEquals(1, aristas.size());
        assertTrue(aristas.get(0).esBucle());
    }

    @Test
    @DisplayName("Eliminar un vertice intermedio reajusta la matriz y los indices")
    void eliminarVerticeReajusta() {
        grafo.agregarVertice("A");
        grafo.agregarVertice("B");
        grafo.agregarVertice("C");
        grafo.setPeso(0, 1, 1); // A - B
        grafo.setPeso(1, 2, 1); // B - C
        grafo.setPeso(0, 2, 1); // A - C

        grafo.eliminarVertice(1); // se elimina B

        assertEquals(2, grafo.getNumVertices());
        assertEquals("A", grafo.getEtiqueta(0));
        assertEquals("C", grafo.getEtiqueta(1));
        assertTrue(grafo.hayArista(0, 1));
        assertEquals(1, grafo.getAristas().size());
    }

    @Test
    @DisplayName("Cambiar de dirigido a no dirigido simetriza la matriz")
    void cambioADirigidoYVuelta() {
        Grafo dirigido = new Grafo(true);
        dirigido.agregarVertice("A");
        dirigido.agregarVertice("B");
        dirigido.setPeso(0, 1, 2);
        assertFalse(dirigido.hayArista(1, 0));

        dirigido.setDirigido(false);
        assertFalse(dirigido.isDirigido());
        assertEquals(2, dirigido.getPeso(0, 1));
        assertEquals(2, dirigido.getPeso(1, 0));
        assertEquals(1, dirigido.getAristas().size());
    }

    @Test
    @DisplayName("Peso 0 borra la arista y las aristas se listan una sola vez")
    void eliminarArista() {
        grafo.agregarVertice("A");
        grafo.agregarVertice("B");
        grafo.agregarArista(0, 1);
        assertEquals(1, grafo.getAristas().size());

        grafo.eliminarArista(0, 1);
        assertFalse(grafo.hayArista(0, 1));
        assertFalse(grafo.hayArista(1, 0));
        assertTrue(grafo.getAristas().isEmpty());
    }

    @Test
    @DisplayName("Pesos negativos e indices invalidos lanzan IllegalArgumentException")
    void validacionesDeEntrada() {
        grafo.agregarVertice("A");
        assertThrows(IllegalArgumentException.class, () -> grafo.setPeso(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> grafo.getPeso(0, 5));
        assertThrows(IllegalArgumentException.class, () -> grafo.getEtiqueta(3));
        assertThrows(IllegalArgumentException.class, () -> grafo.eliminarVertice(1));
        assertThrows(IllegalArgumentException.class, () -> grafo.getGrado(-1));
    }

    @Test
    @DisplayName("Los listeners se avisan en cada cambio y se pueden quitar")
    void listeners() {
        AtomicInteger avisos = new AtomicInteger();
        GrafoListener listener = avisos::incrementAndGet;
        grafo.addListener(listener);

        int indiceA = grafo.agregarVertice("A");   // 1
        grafo.agregarVertice("B");                 // 2
        grafo.setPeso(indiceA, 1, 1);              // 3
        assertEquals(3, avisos.get());

        grafo.removeListener(listener);
        grafo.setPeso(indiceA, 1, 0);
        assertEquals(3, avisos.get());
        assertTrue(grafo.getAristas().isEmpty());
    }

    @Test
    @DisplayName("Grafo completo y conexo; no completo cuando le falta una arista")
    void completoYConexo() {
        Grafo completo = new Grafo(false);
        for (int i = 0; i < 3; i++) {
            completo.agregarVertice(null);
        }
        completo.setPeso(0, 1, 1);
        completo.setPeso(1, 2, 1);
        assertTrue(completo.esConexo());
        assertFalse(completo.esCompleto());

        completo.setPeso(0, 2, 1);
        assertTrue(completo.esCompleto());
    }

    @Test
    @DisplayName("Grafo dirigido con caminos de ida y vuelta es conexo")
    void conexoEnDirigido() {
        Grafo dirigido = new Grafo(true);
        dirigido.agregarVertice("A");
        dirigido.agregarVertice("B");
        dirigido.setPeso(0, 1, 1);
        assertFalse(dirigido.esConexo());

        dirigido.setPeso(1, 0, 1);
        assertTrue(dirigido.esConexo());
    }

    @Test
    @DisplayName("Limpiar deja el grafo sin vertices ni aristas")
    void limpiar() {
        grafo.agregarVertice("A");
        grafo.agregarVertice("B");
        grafo.setPeso(0, 1, 1);
        grafo.limpiar();
        assertTrue(grafo.estaVacio());
        assertEquals(0, grafo.getNumVertices());
        assertTrue(grafo.getAristas().isEmpty());
    }

    @Test
    @DisplayName("Agregar y quitar vertices conserva los datos previos (caso 2 de la seccion 8)")
    void agregarConservaDatos() {
        grafo.agregarVertice("A");
        grafo.agregarVertice("B");
        grafo.agregarVertice("C");
        grafo.setPeso(0, 1, 1);
        grafo.setPeso(0, 2, 1);

        grafo.agregarVertice("D");
        grafo.agregarVertice("E");
        assertEquals(5, grafo.getNumVertices());
        assertEquals(1, grafo.getPeso(0, 1));
        assertEquals(1, grafo.getPeso(0, 2));

        grafo.eliminarVertice(4); // se vuelve a 4 vertices
        assertEquals(4, grafo.getNumVertices());
        assertEquals(1, grafo.getPeso(0, 1));
        assertEquals("D", grafo.getEtiqueta(3));
    }
}
