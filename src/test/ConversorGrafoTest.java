package test;

import model.Arista;
import model.ConversorGrafo;
import model.Grafo;
import model.MatrizInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias del {@link ConversorGrafo} (Modulo 1): validaciones,
 * conversiones matriz/grafo y casos extremos.
 */
class ConversorGrafoTest {

    @Test
    @DisplayName("Matriz 3x3 no dirigida: editar [0][1] crea la arista A-B y su simetrica")
    void matrizNoDirigida() throws MatrizInvalidaException {
        int[][] m = {
                {0, 1, 0},
                {1, 0, 0},
                {0, 0, 0}
        };
        Grafo g = ConversorGrafo.desdeMatriz(m, false);
        assertEquals(3, g.getNumVertices());
        assertEquals("A", g.getEtiqueta(0));
        assertTrue(g.hayArista(0, 1));
        assertTrue(g.hayArista(1, 0));
        assertEquals(1, g.getAristas().size());
    }

    @Test
    @DisplayName("Opcion dirigido: [0][1]=1 y [1][0]=0 produce una sola flecha")
    void matrizDirigida() throws MatrizInvalidaException {
        int[][] m = {
                {0, 1},
                {0, 0}
        };
        Grafo g = ConversorGrafo.desdeMatriz(m, true);
        List<Arista> aristas = g.getAristas();
        assertEquals(1, aristas.size());
        assertEquals(0, aristas.get(0).getOrigen());
        assertEquals(1, aristas.get(0).getDestino());
    }

    @Test
    @DisplayName("Matriz vacia (0x0) produce un grafo sin vertices")
    void matrizVacia() throws MatrizInvalidaException {
        Grafo g = ConversorGrafo.desdeMatriz(new int[0][0], false);
        assertEquals(0, g.getNumVertices());
        assertTrue(g.estaVacio());
        assertArrayEquals(new int[0][0], ConversorGrafo.aMatriz(g));
    }

    @Test
    @DisplayName("Matriz 1x1 con bucle en la diagonal")
    void unVerticeConBucle() throws MatrizInvalidaException {
        int[][] m = {{1}};
        Grafo g = ConversorGrafo.desdeMatriz(m, false);
        assertEquals(1, g.getNumVertices());
        assertTrue(g.esBucle(0));
        assertArrayEquals(m, ConversorGrafo.aMatriz(g));
    }

    @Test
    @DisplayName("Ida y vuelta: matriz -> grafo -> matriz devuelve la misma matriz")
    void idaYVueltaNoDirigido() throws MatrizInvalidaException {
        int[][] m = {
                {0, 1, 0, 2},
                {1, 0, 1, 0},
                {0, 1, 0, 1},
                {2, 0, 1, 1}
        };
        Grafo g = ConversorGrafo.desdeMatriz(m, false);
        assertArrayEquals(m, ConversorGrafo.aMatriz(g));
    }

    @Test
    @DisplayName("Ida y vuelta en grafo dirigido con bucles")
    void idaYVueltaDirigido() throws MatrizInvalidaException {
        int[][] m = {
                {1, 3, 0},
                {0, 0, 2},
                {4, 0, 0}
        };
        Grafo g = ConversorGrafo.desdeMatriz(m, true);
        assertArrayEquals(m, ConversorGrafo.aMatriz(g));
    }

    @Test
    @DisplayName("Grafo completo de 3 vertices: 3 aristas y matriz de unos salvo la diagonal")
    void grafoCompleto() throws MatrizInvalidaException {
        int[][] m = {
                {0, 1, 1},
                {1, 0, 1},
                {1, 1, 0}
        };
        Grafo g = ConversorGrafo.desdeMatriz(m, false);
        assertTrue(g.esCompleto());
        assertTrue(g.esConexo());
        assertEquals(3, g.getAristas().size());
        assertArrayEquals(m, ConversorGrafo.aMatriz(g));
    }

    @Test
    @DisplayName("Matriz no cuadrada es rechazada con un mensaje claro")
    void matrizNoCuadrada() {
        int[][] m = {
                {0, 1},
                {1, 0, 1}
        };
        MatrizInvalidaException error =
                assertThrows(MatrizInvalidaException.class, () -> ConversorGrafo.validar(m, false));
        assertTrue(error.getMessage().contains("cuadrada"));
    }

    @Test
    @DisplayName("Valores negativos son rechazados")
    void valoresNegativos() {
        int[][] m = {
                {0, -3},
                {-3, 0}
        };
        MatrizInvalidaException error =
                assertThrows(MatrizInvalidaException.class, () -> ConversorGrafo.validar(m, false));
        assertTrue(error.getMessage().contains("negativos"));
    }

    @Test
    @DisplayName("Matriz no simetrica en grafo no dirigido: mensaje con las celdas implicadas")
    void matrizNoSimetrica() {
        int[][] m = {
                {0, 1, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        MatrizInvalidaException error =
                assertThrows(MatrizInvalidaException.class, () -> ConversorGrafo.validar(m, false));
        assertEquals("La matriz no es simetrica: [0][1] != [1][0]", error.getMessage());

        // la misma matriz si es valida en modo dirigido
        assertDoesNotThrow(() -> ConversorGrafo.validar(m, true));
    }

    @Test
    @DisplayName("Matriz null es rechazada")
    void matrizNull() {
        assertThrows(MatrizInvalidaException.class, () -> ConversorGrafo.validar(null, false));
        assertThrows(IllegalArgumentException.class, () -> ConversorGrafo.aMatriz(null));
    }

    @Test
    @DisplayName("redimensionar conserva los datos al crecer y al reducir (caso 2 y 7)")
    void redimensionarConservaDatos() {
        int[][] m = {
                {0, 1, 0},
                {1, 0, 1},
                {0, 1, 0}
        };
        int[][] grande = ConversorGrafo.redimensionar(m, 5);
        assertEquals(5, grande.length);
        assertEquals(1, grande[0][1]);
        assertEquals(1, grande[1][2]);
        assertEquals(0, grande[0][4]);
        assertDoesNotThrow(() -> ConversorGrafo.validar(grande, false));

        int[][] reducida = ConversorGrafo.redimensionar(grande, 4);
        assertEquals(4, reducida.length);
        assertEquals(1, reducida[0][1]);
        assertEquals(1, reducida[1][2]);
        assertDoesNotThrow(() -> ConversorGrafo.validar(reducida, false));
    }

    @Test
    @DisplayName("matrizVacia y clonar")
    void utilidadesDeMatriz() {
        int[][] vacia = ConversorGrafo.matrizVacia(3);
        assertEquals(3, vacia.length);
        assertEquals(0, vacia[2][2]);

        vacia[1][2] = 4;
        int[][] copia = ConversorGrafo.clonar(vacia);
        copia[1][2] = 0;
        assertEquals(4, vacia[1][2], "clonar debe ser una copia profunda");
    }

    @Test
    @DisplayName("aleatoria genera siempre matrices validas")
    void aleatoriaEsValida() throws MatrizInvalidaException {
        for (int i = 0; i < 25; i++) {
            ConversorGrafo.validar(ConversorGrafo.aleatoria(4, false, 0.5), false);
            ConversorGrafo.validar(ConversorGrafo.aleatoria(4, true, 0.5), true);
        }
        assertThrows(IllegalArgumentException.class, () -> ConversorGrafo.aleatoria(3, false, 2.0));
    }

    @Test
    @DisplayName("aTexto y desdeTexto: copiar la matriz y volver a leerla da el mismo grafo")
    void textoIdaYVuelta() throws MatrizInvalidaException {
        int[][] m = {
                {0, 1, 0},
                {1, 0, 2},
                {0, 2, 0}
        };
        String texto = ConversorGrafo.aTexto(m);
        assertNotNull(texto);
        assertArrayEquals(m, ConversorGrafo.desdeTexto(texto, false));
        assertArrayEquals(new int[0][0], ConversorGrafo.desdeTexto("   ", false));
        assertThrows(MatrizInvalidaException.class, () -> ConversorGrafo.desdeTexto("0 abc\n1 0", false));
        assertThrows(MatrizInvalidaException.class, () -> ConversorGrafo.desdeTexto("0 1\n0 0", false));
        assertDoesNotThrow(() -> ConversorGrafo.desdeTexto("0 1\n0 0", true));
    }
}
