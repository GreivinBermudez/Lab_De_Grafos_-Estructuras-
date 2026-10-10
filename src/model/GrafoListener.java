package model;

/**
 * Observador del grafo. La GUI implementa esta interfaz para repintar y volver
 * a generar la matriz cuando el modelo cambia (regla acordada: ningun panel
 * modifica la matriz directamente, siempre modifica el Grafo y escucha).
 */
public interface GrafoListener {

    /** Se invoca cada vez que cambia el grafo (vertices, aristas, pesos, dirigido). */
    void grafoCambio();
}
