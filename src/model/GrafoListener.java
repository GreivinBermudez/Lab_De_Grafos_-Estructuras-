package model;

/**
 * Observador del grafo: la GUI lo implementa para repintarse y regenerar la
 * matriz cuando el modelo cambia. Los paneles no modifican la matriz por su
 * cuenta; modifican el {@link Grafo} y escuchan sus cambios.
 */
public interface GrafoListener {

    /** Se invoca cada vez que cambia el grafo (vertices, aristas, pesos, dirigido). */
    void grafoCambio();
}
