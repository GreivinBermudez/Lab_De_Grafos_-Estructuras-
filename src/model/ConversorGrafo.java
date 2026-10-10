package model;

/**
 * Conversor entre la matriz de adyacencia ({@code int[][]}) y el modelo
 * {@link Grafo}. Toda la GUI pasa por aqui: ningun panel calcula la matriz por
 * su cuenta (seccion 3 del documento de modulos).
 *
 * <p>Clase de utilidades: no se instancia.</p>
 */
public final class ConversorGrafo {

    private ConversorGrafo() {
    }

    /**
     * Construye un grafo a partir de una matriz de adyacencia.
     *
     * @param m        matriz cuadrada con valores no negativos; si el grafo no es
     *                 dirigido debe ser simetrica
     * @param dirigido true para grafo dirigido
     * @return grafo con las etiquetas por defecto A, B, C...
     * @throws MatrizInvalidaException si la matriz no cumple las reglas
     */
    public static Grafo desdeMatriz(int[][] m, boolean dirigido) throws MatrizInvalidaException {
        validar(m, dirigido);
        Grafo g = new Grafo(dirigido);
        int n = m.length;
        for (int i = 0; i < n; i++) {
            g.agregarVertice(Grafo.etiquetaPorDefecto(i));
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (m[i][j] != 0) {
                    g.setPeso(i, j, m[i][j]);
                }
            }
        }
        return g;
    }

    /**
     * Devuelve la matriz de adyacencia del grafo. El orden de filas y columnas
     * es el mismo que el de los vertices del grafo.
     */
    public static int[][] aMatriz(Grafo g) {
        if (g == null) {
            throw new IllegalArgumentException("El grafo no puede ser null");
        }
        int n = g.getNumVertices();
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (g.hayArista(i, j)) {
                    m[i][j] = g.getPeso(i, j);
                }
            }
        }
        return m;
    }

    /**
     * Valida una matriz de adyacencia: cuadrada, valores enteros no negativos y
     * simetrica cuando el grafo no es dirigido.
     *
     * @throws MatrizInvalidaException con un mensaje listo para mostrar en la GUI
     */
    public static void validar(int[][] m, boolean dirigido) throws MatrizInvalidaException {
        if (m == null) {
            throw new MatrizInvalidaException("La matriz no puede ser null");
        }
        int n = m.length;
        for (int i = 0; i < n; i++) {
            if (m[i] == null || m[i].length != n) {
                throw new MatrizInvalidaException(
                        "La matriz debe ser cuadrada: la fila " + i + " no tiene " + n + " columnas");
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (m[i][j] < 0) {
                    throw new MatrizInvalidaException(
                            "La matriz no admite valores negativos: [" + i + "][" + j + "] = " + m[i][j]);
                }
            }
        }
        if (!dirigido) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (m[i][j] != m[j][i]) {
                        throw new MatrizInvalidaException(
                                "La matriz no es simetrica: [" + i + "][" + j + "] != [" + j + "][" + i + "]");
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Utilidades para la GUI (Modulos 2 y 3)
    // ------------------------------------------------------------------

    /** Matriz cuadrada de ceros de tamano n x n. */
    public static int[][] matrizVacia(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("El tamano no puede ser negativo: " + n);
        }
        return new int[n][n];
    }

    /** Copia profunda de una matriz. */
    public static int[][] clonar(int[][] m) {
        if (m == null) {
            throw new IllegalArgumentException("La matriz no puede ser null");
        }
        int[][] copia = new int[m.length][];
        for (int i = 0; i < m.length; i++) {
            copia[i] = (m[i] == null) ? null : m[i].clone();
        }
        return copia;
    }

    /**
     * Cambia el tamano de la matriz conservando los datos existentes (caso de
     * prueba 2 de la seccion 8). Si crece se rellena con ceros; si se reduce se
     * recorta. La simetria se conserva en ambos casos.
     */
    public static int[][] redimensionar(int[][] m, int nuevoTamano) {
        if (m == null) {
            throw new IllegalArgumentException("La matriz no puede ser null");
        }
        if (nuevoTamano < 0) {
            throw new IllegalArgumentException("El tamano no puede ser negativo: " + nuevoTamano);
        }
        int[][] nueva = new int[nuevoTamano][nuevoTamano];
        int filas = Math.min(nuevoTamano, m.length);
        for (int i = 0; i < filas; i++) {
            int columnas = Math.min(nuevoTamano, m[i].length);
            System.arraycopy(m[i], 0, nueva[i], 0, columnas);
        }
        return nueva;
    }

    /**
     * Matriz aleatoria valida (boton "Aleatoria" de la Opcion 1).
     *
     * @param n            numero de vertices
     * @param dirigido     si es false la matriz se genera simetrica
     * @param probabilidad probabilidad [0..1] de que cada celda tenga arista
     * @return matriz cuadrada valida para {@link #validar(int[][], boolean)}
     */
    public static int[][] aleatoria(int n, boolean dirigido, double probabilidad) {
        if (n < 0) {
            throw new IllegalArgumentException("El tamano no puede ser negativo: " + n);
        }
        if (probabilidad < 0 || probabilidad > 1) {
            throw new IllegalArgumentException("La probabilidad debe estar entre 0 y 1: " + probabilidad);
        }
        java.util.Random rnd = new java.util.Random();
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (dirigido) {
                    if (rnd.nextDouble() < probabilidad) {
                        m[i][j] = 1;
                    }
                } else if (j >= i && rnd.nextDouble() < probabilidad) {
                    m[i][j] = 1;
                    m[j][i] = 1;
                }
            }
        }
        return m;
    }

    /** Texto con una fila por linea y celdas separadas por tabulador. */
    public static String aTexto(int[][] m) {
        if (m == null) {
            throw new IllegalArgumentException("La matriz no puede ser null");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (j > 0) {
                    sb.append('\t');
                }
                sb.append(m[i][j]);
            }
            if (i < m.length - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * Lee una matriz desde texto (celdas separadas por espacios, comas, punto y
     * coma o tabuladores; una fila por linea). Se usa al pegar en la Opcion 1 la
     * matriz copiada en la Opcion 2.
     *
     * @return matriz validada, o una matriz 0 x 0 si el texto esta vacio
     * @throws MatrizInvalidaException si el texto no es una matriz valida
     */
    public static int[][] desdeTexto(String texto, boolean dirigido) throws MatrizInvalidaException {
        if (texto == null || texto.trim().isEmpty()) {
            return new int[0][0];
        }
        String[] lineas = texto.trim().split("\\R");
        int[][] m = new int[lineas.length][];
        for (int i = 0; i < lineas.length; i++) {
            String[] celdas = lineas[i].trim().split("[\\s,;]+");
            m[i] = new int[celdas.length];
            for (int j = 0; j < celdas.length; j++) {
                try {
                    m[i][j] = Integer.parseInt(celdas[j].trim());
                } catch (NumberFormatException e) {
                    throw new MatrizInvalidaException(
                            "Valor no numerico en la fila " + i + ": \"" + celdas[j] + "\"");
                }
            }
        }
        validar(m, dirigido);
        return m;
    }
}
