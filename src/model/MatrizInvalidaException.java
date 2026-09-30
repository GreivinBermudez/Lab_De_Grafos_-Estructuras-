package model;

/**
 * Se lanza cuando una matriz de adyacencia no cumple las reglas acordadas
 * (cuadrada, valores no negativos y simetrica cuando el grafo no es dirigido).
 *
 * <p>Los mensajes deben ser claros porque la GUI (Modulos 2 y 3) los muestra
 * directamente al usuario en un JOptionPane.</p>
 */
public class MatrizInvalidaException extends Exception {

    private static final long serialVersionUID = 1L;

    public MatrizInvalidaException(String mensaje) {
        super(mensaje);
    }
}
