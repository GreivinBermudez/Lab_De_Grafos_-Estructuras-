package model;

/**
 * Observador del grafo: la GUI lo implementa para repintarse y regenerar la
 * matriz cuando el modelo cambia. Los paneles no modifican la matriz por su
 * cuenta; modifican el {@link Grafo} y escuchan sus cambios.
<<<<<<< HEAD
=======
 *
 * <p>Es el corazon del patron <b>Observador</b> (Observer) del proyecto: el
 * {@link Grafo} es el sujeto que notifica y estas clases son los observadores.
 * Gracias a esto la Opcion 1 y la Opcion 2 mantienen la matriz y el dibujo
 * siempre sincronizados sin acoplarse entre si.</p>
>>>>>>> Mariano
 */
public interface GrafoListener {

    /** Se invoca cada vez que cambia el grafo (vertices, aristas, pesos, dirigido). */
    void grafoCambio();
}
