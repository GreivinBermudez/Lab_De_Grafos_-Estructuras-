package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Modelo de un grafo: guarda la matriz de adyacencia interna ({@code int[][]})
 * y las etiquetas de los vertices en el mismo orden que filas y columnas.
 *
<<<<<<< HEAD
=======
 * <p><b>Role en la arquitectura (patron Modelo-Vista):</b> esta clase es el
 * "cerebro" de la aplicacion y NO conoce la interfaz. Toda modificacion
 * notifica a los {@link GrafoListener} registrados y es la GUI quien se
 * repinta al escuchar esos cambios; por eso ningun panel edita la matriz
 * directamente, siempre se pasa por aqui. Es el contrato congelado que
 * comparten los 3 modulos del proyecto.</p>
 *
>>>>>>> Mariano
 * <p>Reglas del modelo:</p>
 * <ul>
 *   <li>Un {@code 0} en {@code [i][j]} significa que no hay arista; un valor
 *       mayor que 0 es arista y, si el grafo es ponderado, su peso.</li>
 *   <li>Grafo no dirigido: la matriz es simetrica; {@code setPeso(i, j, p)}
 *       actualiza tambien {@code [j][i]}.</li>
 *   <li>Grafo dirigido: cada celda es independiente.</li>
 *   <li>Los bucles (diagonal principal) estan permitidos.</li>
 *   <li>Toda modificacion notifica a los {@link GrafoListener} registrados.</li>
 * </ul>
 */
public class Grafo {

    /** Matriz de adyacencia: matriz[i][j] = peso de la arista i -> j (0 = sin arista). */
    private int[][] matriz;
    /** Etiquetas de los vertices en el mismo orden que las filas/columnas. */
    private final List<String> etiquetas;
    /** true si las aristas son dirigidas. */
    private boolean dirigido;
    /** Observadores (GUI) que se avisan en cada cambio. */
    private final List<GrafoListener> listeners;

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
        this.matriz = new int[0][0];
        this.etiquetas = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // Configuracion
    // ------------------------------------------------------------------

    /** Indica si las aristas del grafo son dirigidas. */
    public boolean isDirigido() {
        return dirigido;
    }

    /**
     * Cambia el tipo de grafo. Al pasar de dirigido a no dirigido la matriz se
     * simetriza tomando el mayor de {@code [i][j]} y {@code [j][i]} para no
     * perder aristas ya dibujadas.
     */
    public void setDirigido(boolean dirigido) {
        if (this.dirigido == dirigido) {
            return;
        }
        this.dirigido = dirigido;
        if (!dirigido) {
            simetrizar();
        }
        notificar();
    }

    /** Deja la matriz simetrica tomando el maximo de cada pareja de celdas. */
    private void simetrizar() {
        int n = getNumVertices();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int peso = Math.max(matriz[i][j], matriz[j][i]);
                matriz[i][j] = peso;
                matriz[j][i] = peso;
            }
        }
    }

    // ------------------------------------------------------------------
    // Vertices
    // ------------------------------------------------------------------

    /** Numero de vertices actuales (dimension de la matriz). */
    public int getNumVertices() {
        return etiquetas.size();
    }

    /**
     * Agrega un vertice al final (conserva los datos existentes) y devuelve su
     * indice. Si {@code etiqueta} es null o vacia se usa la etiqueta por
     * defecto (A, B, C...).
     *
     * @return indice del vertice nuevo
     */
    public int agregarVertice(String etiqueta) {
        int n = getNumVertices();
        // Como la matriz es cuadrada, para sumar 1 vertice hay que crear una
        // matriz (n+1)x(n+1) y copiar los datos viejos fila por fila
        // (System.arraycopy es la copia rapida). La nueva fila y la nueva
        // columna quedan en ceros, y 0 significa "sin arista" para el vertice
        // recien agregado; por eso conserva los datos existentes.
        int[][] nueva = new int[n + 1][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matriz[i], 0, nueva[i], 0, n);
        }
        matriz = nueva;
        String texto = (etiqueta == null || etiqueta.trim().isEmpty())
                ? etiquetaPorDefecto(n)
                : etiqueta.trim();
        etiquetas.add(texto);
        notificar();
        return n;
    }

    /**
     * Elimina el vertice {@code indice} y reajusta la matriz (se quitan la fila
     * y la columna correspondientes; los vertices posteriores bajan de indice).
     */
    public void eliminarVertice(int indice) {
        verificarIndice(indice);
        int n = getNumVertices();
        int[][] nueva = new int[n - 1][n - 1];
        // Se copia la matriz "saltando" la fila y la columna del vertice
        // eliminado. Los dos contadores (filaNueva/colNueva) van escribiendo
        // compactando: los vertices con indice mayor quedan recorridos una
        // posicion hacia abajo (por eso GrafoPanel debe corregir sus
        // posiciones al borrar un vertice).
        int filaNueva = 0;
        for (int i = 0; i < n; i++) {
            if (i == indice) {
                continue; // se omite la fila del vertice eliminado
            }
            int colNueva = 0;
            for (int j = 0; j < n; j++) {
                if (j == indice) {
                    continue; // se omite la columna del vertice eliminado
                }
                nueva[filaNueva][colNueva++] = matriz[i][j];
            }
            filaNueva++;
        }
        matriz = nueva;
        etiquetas.remove(indice);
        notificar();
    }

    public String getEtiqueta(int indice) {
        verificarIndice(indice);
        return etiquetas.get(indice);
    }

    public void setEtiqueta(int indice, String etiqueta) {
        verificarIndice(indice);
        etiquetas.set(indice, (etiqueta == null || etiqueta.trim().isEmpty())
                ? etiquetaPorDefecto(indice)
                : etiqueta.trim());
        notificar();
    }

    /** Etiquetas en el orden de la matriz (lista de solo lectura). */
    public List<String> getEtiquetas() {
        return Collections.unmodifiableList(etiquetas);
    }

    /** Etiqueta automatica usada cuando el usuario no indica ninguna. */
    public static String etiquetaPorDefecto(int indice) {
        if (indice >= 0 && indice < 26) {
            return String.valueOf((char) ('A' + indice));
        }
        return "V" + indice;
    }

    // ------------------------------------------------------------------
    // Aristas y pesos
    // ------------------------------------------------------------------

    /**
     * Crea, actualiza o borra la arista {@code origen -> destino}
     * ({@code peso = 0} borra la arista). En grafos no dirigidos se actualiza
     * tambien la celda simetrica.
     *
     * @param peso valor mayor o igual que 0
     * @throws IllegalArgumentException si el peso es negativo o un indice no existe
     */
    public void setPeso(int origen, int destino, int peso) {
        if (peso < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo: " + peso);
        }
        verificarIndice(origen);
        verificarIndice(destino);
        matriz[origen][destino] = peso;
        // En un grafo NO dirigido la arista es la misma en los dos sentidos,
        // por lo que ademas de [origen][destino] se escribe su celda espejo
        // [destino][origen] para que la matriz nunca deje de ser simetrica.
        if (!dirigido) {
            matriz[destino][origen] = peso;
        }
        notificar();
    }

    /** Alias de {@link #setPeso(int, int, int)} con peso 1 (arista sin peso). */
    public void agregarArista(int origen, int destino) {
        setPeso(origen, destino, 1);
    }

    /** Elimina la arista origen -> destino (y su simetrica en no dirigidos). */
    public void eliminarArista(int origen, int destino) {
        setPeso(origen, destino, 0);
    }

    /** Peso de la arista origen -> destino (0 = sin arista). */
    public int getPeso(int origen, int destino) {
        verificarIndice(origen);
        verificarIndice(destino);
        return matriz[origen][destino];
    }

    /** Indica si existe la arista origen -> destino. */
    public boolean hayArista(int origen, int destino) {
        return getPeso(origen, destino) > 0;
    }

    /** Indica si el vertice tiene un bucle (arista consigo mismo). */
    public boolean esBucle(int indice) {
        return getPeso(indice, indice) > 0;
    }

    /**
     * Grado del vertice: numero de aristas que inciden en el.
     *
     * <p>En grafos no dirigidos la celda simetrica es la misma arista, se cuenta una
     * sola vez. En grafos dirigidos se cuentan aristas entrantes y salientes. Los
     * bucles cuentan 2 en ambos casos.</p>
     */
    public int getGrado(int indice) {
        verificarIndice(indice);
        int n = getNumVertices();
        int grado = 0;
        // Se recorre la fila (aristas que salen) y, si es dirigido, tambien la
        // columna (aristas que entran). En no dirigidos [i][j] y [j][i] son la
        // misma arista, por eso solo se cuenta una vez la fila.
        for (int j = 0; j < n; j++) {
            if (j == indice) {
                continue; // el bucle se trata aparte, al final
            }
            if (matriz[indice][j] > 0) {
                grado++;
            }
            if (dirigido && matriz[j][indice] > 0) {
                grado++;
            }
        }
        if (matriz[indice][indice] > 0) {
            grado += 2; // el bucle incide dos veces en el mismo vertice
        }
        return grado;
    }

    /** Aristas que salen del vertice (los bucles cuentan 1). Util en grafos dirigidos. */
    public int getGradoSalida(int indice) {
        verificarIndice(indice);
        int n = getNumVertices();
        int grado = 0;
        for (int j = 0; j < n; j++) {
            if (matriz[indice][j] > 0) {
                grado++;
            }
        }
        return grado;
    }

    /** Aristas que llegan al vertice (los bucles cuentan 1). Util en grafos dirigidos. */
    public int getGradoEntrada(int indice) {
        verificarIndice(indice);
        int n = getNumVertices();
        int grado = 0;
        for (int j = 0; j < n; j++) {
            if (matriz[j][indice] > 0) {
                grado++;
            }
        }
        return grado;
    }

    /**
     * Aristas actuales del grafo. En grafos no dirigidos cada arista aparece una
     * sola vez (con {@code origen <= destino}); los bucles tambien aparecen una vez.
     */
    public List<Arista> getAristas() {
        int n = getNumVertices();
        List<Arista> aristas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matriz[i][j] <= 0) {
                    continue;
                }
                if (!dirigido && j < i) {
                    continue; // ya se agrego como (j, i)
                }
                aristas.add(new Arista(i, j, matriz[i][j]));
            }
        }
        return aristas;
    }

    // ------------------------------------------------------------------
    // Utilidades para la GUI
    // ------------------------------------------------------------------

    /** Borra todos los vertices y aristas (boton "Limpiar"). */
    public void limpiar() {
        matriz = new int[0][0];
        etiquetas.clear();
        notificar();
    }

    /** Indica si el grafo no tiene vertices. */
    public boolean estaVacio() {
        return getNumVertices() == 0;
    }

    /** Grafo completo: todas las parejas de vertices distintos tienen arista. */
    public boolean esCompleto() {
        int n = getNumVertices();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && matriz[i][j] <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Grafo conexo. En no dirigidos basta con llegar a todos desde un vertice;
     * en dirigidos se exige conectividad fuerte (ida y vuelta).
     */
    public boolean esConexo() {
        int n = getNumVertices();
        if (n <= 1) {
            return true; // un grafo sin vertices o con 1 solo siempre es conexo
        }
        // No dirigido: basta con alcanzar a todos desde un solo vertice.
        // Dirigido: se exige conectividad FUERTE, o sea que todos se alcancen
        // tanto en sentido directo como inverso (ida y vuelta).
        if (!dirigido) {
            return alcanza(0, false) == n;
        }
        return alcanza(0, false) == n && alcanza(0, true) == n;
    }

    /** Vertices alcanzados desde inicio (o que alcanzan a inicio, si se recorre
     *  en sentido inverso) con una busqueda en profundidad iterativa. */
    private int alcanza(int inicio, boolean recorrerInverso) {
        int n = getNumVertices();
        boolean[] visto = new boolean[n]; // evita visitar un vertice dos veces
        Deque<Integer> pila = new ArrayDeque<>();
        pila.push(inicio); // DFS iterativo con pila (evita recursion profunda)
        visto[inicio] = true;
        int contador = 1; // el propio inicio ya esta alcanzado
        while (!pila.isEmpty()) {
            int v = pila.pop();
            for (int w = 0; w < n; w++) {
                // En modo inverso se leen las aristas al reves: matriz[w][v]
                // en lugar de matriz[v][w], para recorrer hacia "atras".
                int peso = recorrerInverso ? matriz[w][v] : matriz[v][w];
                if (peso > 0 && !visto[w]) { // peso > 0 significa que hay arista
                    visto[w] = true;
                    contador++;
                    pila.push(w);
                }
            }
        }
        return contador;
    }

    // ------------------------------------------------------------------
    // Escucha de cambios (GrafoListener)
    // ------------------------------------------------------------------

    /** Registra un observador de los cambios del grafo (no admite duplicados). */
    public void addListener(GrafoListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /** Da de baja un observador registrado. */
    public void removeListener(GrafoListener l) {
        listeners.remove(l);
    }

    /** Notifica a los observadores. */
    protected void notificar() {
        // copia defensiva: un listener puede quitarse a si mismo al reaccionar
        // (por eso se recorre una copia de la lista y no la original, que
        // provocaria ConcurrentModificationException).
        for (GrafoListener l : new ArrayList<>(listeners)) {
            l.grafoCambio();
        }
    }

    /** Lanza IllegalArgumentException si el indice no corresponde a un vertice. */
    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= getNumVertices()) {
            throw new IllegalArgumentException(
                    "Indice de vertice fuera de rango: " + indice
                            + " (el grafo tiene " + getNumVertices() + " vertices)");
        }
    }

    @Override
    public String toString() {
        int n = getNumVertices();
        StringBuilder sb = new StringBuilder("Grafo(")
                .append(dirigido ? "dirigido" : "no dirigido")
                .append(", ").append(n).append(" vertices)");
        for (int i = 0; i < n; i++) {
            sb.append(System.lineSeparator());
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%4d", matriz[i][j]));
            }
        }
        return sb.toString();
    }
}
