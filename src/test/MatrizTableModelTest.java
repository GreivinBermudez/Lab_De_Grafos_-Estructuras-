package test;

import ui.matriz.MatrizTableModel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de {@link MatrizTableModel} (Opcion 1). No requieren
 * entorno grafico: el modelo de tabla se prueba de forma directa.
 */
class MatrizTableModelTest {

    @Test
    @DisplayName("Estructura inicial: n filas, n+1 columnas y etiquetas correctas")
    void estructuraInicial() {
        MatrizTableModel m = new MatrizTableModel(4, true);
        assertEquals(4, m.getRowCount());
        assertEquals(5, m.getColumnCount());
        assertEquals("Vertice", m.getColumnName(0));
        assertEquals("A", m.getColumnName(1));
        assertEquals("D", m.getColumnName(4));
        assertEquals("A", m.getValueAt(0, 0));
        assertEquals(Integer.valueOf(0), m.getValueAt(2, 3));
    }

    @Test
    @DisplayName("Todas las celdas de la matriz son editables; la columna de etiquetas no")
    void celdasEditables() {
        MatrizTableModel m = new MatrizTableModel(3, true);
        assertFalse(m.isCellEditable(0, 0));
        for (int f = 0; f < 3; f++) {
            for (int c = 1; c <= 3; c++) {
                assertTrue(m.isCellEditable(f, c), "celda (" + f + "," + c + ") debe ser editable");
            }
        }
    }

    @Test
    @DisplayName("No dirigido: editar [A][B] actualiza tambien [B][A]")
    void simetriaAutomatica() {
        MatrizTableModel m = new MatrizTableModel(3, true);
        m.setValueAt(7, 0, 2); // celda tabla (0,2) = matriz[0][1]
        assertEquals(Integer.valueOf(7), m.getValueAt(0, 2));
        assertEquals(Integer.valueOf(7), m.getValueAt(1, 1)); // matriz[1][0]
        assertEquals(7, m.getMatriz()[0][1]);
        assertEquals(7, m.getMatriz()[1][0]);
    }

    @Test
    @DisplayName("Dirigido: la celda editada es independiente de su simetrica")
    void sinSimetriaEnDirigido() {
        MatrizTableModel m = new MatrizTableModel(3, false);
        m.setValueAt(7, 0, 2);
        assertEquals(Integer.valueOf(7), m.getValueAt(0, 2));
        assertEquals(Integer.valueOf(0), m.getValueAt(1, 1));
    }

    @Test
    @DisplayName("Valores invalidos (texto, negativos) se ignoran; vacio vale 0")
    void valoresInvalidos() {
        MatrizTableModel m = new MatrizTableModel(2, true);
        m.setValueAt(3, 0, 1); // matriz[0][0]
        m.setValueAt("abc", 0, 1);
        assertEquals(Integer.valueOf(3), m.getValueAt(0, 1));
        m.setValueAt("-5", 0, 1);
        assertEquals(Integer.valueOf(3), m.getValueAt(0, 1));
        m.setValueAt("", 0, 1);
        assertEquals(Integer.valueOf(0), m.getValueAt(0, 1));
    }

    @Test
    @DisplayName("Redimensionar conserva los datos al crecer y al reducir")
    void redimensionarConservaDatos() {
        MatrizTableModel m = new MatrizTableModel(3, true);
        m.setValueAt(1, 0, 2); // matriz[0][1]
        m.setValueAt(2, 1, 3); // matriz[1][2]
        m.redimensionar(5);
        assertEquals(5, m.getTamano());
        assertEquals(6, m.getColumnCount());
        assertEquals(Integer.valueOf(1), m.getValueAt(0, 2));
        assertEquals(Integer.valueOf(2), m.getValueAt(1, 3));
        assertEquals(Integer.valueOf(0), m.getValueAt(4, 5));
        m.redimensionar(2);
        assertEquals(2, m.getTamano());
        assertEquals(Integer.valueOf(1), m.getValueAt(0, 2));
    }

    @Test
    @DisplayName("cargar reemplaza la matriz y getMatriz devuelve una copia defensiva")
    void cargarYClonar() {
        MatrizTableModel m = new MatrizTableModel(2, true);
        int[][] datos = {{0, 1}, {1, 0}};
        m.cargar(datos);
        assertEquals(2, m.getTamano());
        assertEquals(Integer.valueOf(1), m.getValueAt(0, 2));
        int[][] copia = m.getMatriz();
        copia[0][1] = 99;
        assertEquals(1, m.getMatriz()[0][1]);
    }

    @Test
    @DisplayName("Pasar de dirigido a no dirigido simetriza con el maximo")
    void setSimetricaSimetriza() {
        MatrizTableModel m = new MatrizTableModel(2, false);
        m.setValueAt(5, 0, 2); // matriz[0][1] = 5
        m.setValueAt(2, 1, 1); // matriz[1][0] = 2
        m.setSimetrica(true);
        assertEquals(Integer.valueOf(5), m.getValueAt(0, 2));
        assertEquals(Integer.valueOf(5), m.getValueAt(1, 1));
    }

    @Test
    @DisplayName("Matriz de 0 vertices: 0 filas y solo la columna de etiquetas")
    void matrizVacia() {
        MatrizTableModel m = new MatrizTableModel(0, true);
        assertEquals(0, m.getRowCount());
        assertEquals(1, m.getColumnCount());
        assertEquals("Vertice", m.getColumnName(0));
    }

    @Test
    @DisplayName("getColumnClass: String en etiquetas, Integer en la matriz")
    void clasesDeColumna() {
        MatrizTableModel m = new MatrizTableModel(2, true);
        assertEquals(String.class, m.getColumnClass(0));
        assertEquals(Integer.class, m.getColumnClass(1));
    }

    @Test
    @DisplayName("La tabla es notificada de cada cambio (celda + simetrica)")
    void notificacionDeCambios() {
        MatrizTableModel m = new MatrizTableModel(2, true);
        AtomicInteger avisos = new AtomicInteger();
        m.addTableModelListener(e -> avisos.incrementAndGet());
        m.setValueAt(4, 0, 2); // celda + simetrica = 2 avisos
        assertEquals(2, avisos.get());
        m.redimensionar(3); // cambio de estructura = 1 aviso
        assertEquals(3, avisos.get());
    }
}
