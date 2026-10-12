package ui.matriz;

import model.ConversorGrafo;
import model.Grafo;

import javax.swing.table.AbstractTableModel;

/**
 * Modelo de la tabla que edita la matriz de adyacencia (Opcion 1).
 *
 * <p>La columna 0 muestra la etiqueta del vertice (no editable) y las
 * columnas 1..n son las celdas de la matriz, todas editables con enteros
 * mayores o iguales que 0. Cuando el grafo es no dirigido editar una celda
 * actualiza tambien su simetrica, de modo que la matriz nunca deja de ser
 * valida para {@link ConversorGrafo#desdeMatriz(int[][], boolean)}.</p>
 */
public class MatrizTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private int[][] matriz;
    /** true cuando el grafo es no dirigido: la matriz se mantiene simetrica. */
    private boolean simetrica;

    /** Crea el modelo con una matriz de ceros del tamano indicado. */
    public MatrizTableModel(int tamanoInicial, boolean simetrica) {
        this.matriz = ConversorGrafo.matrizVacia(tamanoInicial);
        this.simetrica = simetrica;
    }

    /** Copia defensiva de la matriz actual. */
    public int[][] getMatriz() {
        return ConversorGrafo.clonar(matriz);
    }

    /** Tamano actual de la matriz (numero de vertices). */
    public int getTamano() {
        return matriz.length;
    }

    /** Indica si el modelo mantiene la matriz simetrica (grafo no dirigido). */
    public boolean isSimetrica() {
        return simetrica;
    }

    /**
     * Cambia el modo del grafo. Al volver a no dirigido la matriz se
     * simetriza con el maximo de cada pareja para no perder aristas.
     */
    public void setSimetrica(boolean simetrica) {
        if (this.simetrica == simetrica) {
            return;
        }
        this.simetrica = simetrica;
        if (simetrica) {
            int n = matriz.length;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    int peso = Math.max(matriz[i][j], matriz[j][i]);
                    matriz[i][j] = peso;
                    matriz[j][i] = peso;
                }
            }
        }
        fireTableDataChanged();
    }

    /** Reemplaza la matriz completa por una copia de la recibida. */
    public void cargar(int[][] nueva) {
        matriz = ConversorGrafo.clonar(nueva);
        fireTableStructureChanged();
    }

    /** Cambia el tamano conservando los datos existentes. */
    public void redimensionar(int nuevoTamano) {
        matriz = ConversorGrafo.redimensionar(matriz, nuevoTamano);
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return matriz.length;
    }

    @Override
    public int getColumnCount() {
        return matriz.length + 1;
    }

    @Override
    public String getColumnName(int columna) {
        return columna == 0 ? "Vertice" : Grafo.etiquetaPorDefecto(columna - 1);
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return columna == 0 ? String.class : Integer.class;
    }

    /** Todas las celdas de la matriz son editables (la columna de etiquetas no). */
    @Override
    public boolean isCellEditable(int fila, int columna) {
        return columna > 0;
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        if (columna == 0) {
            return Grafo.etiquetaPorDefecto(fila);
        }
        return Integer.valueOf(matriz[fila][columna - 1]);
    }

    /**
     * Guarda el peso en la celda de la matriz. Los valores invalidos (texto no
     * numerico o negativos) se ignoran y conservan el valor anterior; en modo
     * simetrico se actualiza tambien la celda espejo.
     */
    @Override
    public void setValueAt(Object valor, int fila, int columna) {
        if (columna == 0) {
            return; // la columna de etiquetas no se edita
        }
        int peso = parsear(valor);
        if (peso < 0) {
            return; // valor invalido: se conserva el anterior
        }
        int j = columna - 1; // la columna 0 es la etiqueta, la matriz arranca en la 1
        matriz[fila][j] = peso;
        // En modo simetrico (no dirigido) se actualiza tambien la celda espejo
        // [j][fila] y se avisa a la tabla para repintar ambas celdas.
        if (simetrica && fila != j) {
            matriz[j][fila] = peso;
            fireTableCellUpdated(j, fila + 1);
        }
        fireTableCellUpdated(fila, columna);
    }

    /** Convierte el valor a entero; -1 si no es un numero valido. */
    private int parsear(Object valor) {
        try {
            if (valor instanceof Number) {
                return ((Number) valor).intValue();
            }
            String texto = String.valueOf(valor).trim();
            if (texto.isEmpty()) {
                return 0; // celda vacia se interpreta como "sin arista"
            }
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1; // -1 es la marca de "no numerico", que setValueAt ignora
        }
    }
}
