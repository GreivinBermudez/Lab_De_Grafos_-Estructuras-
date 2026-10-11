package model;

/**
 * Se lanza cuando una matriz de adyacencia no cumple las reglas del modelo
 * (cuadrada, valores no negativos y simetrica cuando el grafo no es dirigido).
 *
 * <p>Los mensajes deben ser claros porque la GUI los muestra directamente al
 * usuario en un JOptionPane.</p>
 */
public class MatrizInvalidaException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Crea la excepcion con un mensaje listo para mostrar al usuario. */
    public MatrizInvalidaException(String mensaje) {
        super(mensaje);
    }
}
