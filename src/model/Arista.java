package model;

import java.util.Objects;

/**
 * Arista del grafo.
 *
 * <p>Si el grafo es no dirigido, cada arista se guarda una sola vez (con
 * origen &lt;= destino) y representa tambien la arista inversa. Si el grafo es
 * dirigido, la arista va de {@code origen} a {@code destino}.</p>
 */
public class Arista {

    /** Vertice de origen (indice en el grafo). */
    private final int origen;
    /** Vertice de destino (indice en el grafo). */
    private final int destino;
    /** Peso de la arista (mayor que 0). */
    private final int peso;

    /** Crea la arista origen -> destino con el peso indicado. */
    public Arista(int origen, int destino, int peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    /** Indice del vertice de origen. */
    public int getOrigen() {
        return origen;
    }

    /** Indice del vertice de destino. */
    public int getDestino() {
        return destino;
    }

    /** Peso de la arista (siempre mayor que 0). */
    public int getPeso() {
        return peso;
    }

    /** true si la arista une un vertice consigo mismo (bucle). */
    public boolean esBucle() {
        return origen == destino;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Arista)) {
            return false;
        }
        Arista a = (Arista) otro;
        return origen == a.origen && destino == a.destino && peso == a.peso;
    }

    @Override
    public int hashCode() {
        return Objects.hash(origen, destino, peso);
    }

    @Override
    public String toString() {
        return "Arista{" + origen + " -> " + destino + ", peso=" + peso + "}";
    }
}
