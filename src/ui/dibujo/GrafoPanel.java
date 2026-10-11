package ui.dibujo;

import model.Arista;
import model.Grafo;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lienzo reutilizable que dibuja un {@link Grafo} con Java2D.
 *
 * <p>Los vertices se dibujan como circulos con su etiqueta, las aristas como
 * lineas (con flecha si el grafo es dirigido), los bucles como un lazo sobre
 * el vertice y los pesos mayores que 1 como numeros junto a la arista. Si hay
 * aristas en los dos sentidos entre dos vertices se dibujan curvadas para que
 * no se solapen.</p>
 *
 * <p>Los vertices sin posicion manual se colocan en un circulo centrado en el
 * panel, de modo que la Opcion 1 solo tiene que reemplazar el grafo. La
 * Opcion 2 extiende esta clase para dibujar con el mouse: usa
 * {@link #setPosicion(int, Point)}, {@link #verticeEn(Point)} y el gancho
 * {@link #pintarExtras(Graphics2D)}.</p>
 */
public class GrafoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Radio de los circulos que representan los vertices. */
    public static final int RADIO_VERTICE = 20;
    /** Margen del layout circular automatico. */
    private static final int MARGEN = 34;
    /** Radio minimo del circulo automatico en paneles muy pequenos. */
    private static final double RADIO_MINIMO = 30;

    private static final Color COLOR_RELLENO = new Color(0xD6, 0xEA, 0xF8);
    private static final Color COLOR_RESALTADO = new Color(0xF5, 0xB0, 0x41);
    private static final Color COLOR_BORDE = new Color(0x1B, 0x4F, 0x8A);
    private static final Color COLOR_ARISTA = new Color(0x44, 0x44, 0x44);
    private static final Color COLOR_PESO = new Color(0x8A, 0x1C, 0x1C);
    private static final Color COLOR_MENSAJE = new Color(0x99, 0x99, 0x99);
    private static final BasicStroke TRAZO_ARISTA = new BasicStroke(2f);
    private static final BasicStroke TRAZO_VERTICE = new BasicStroke(2f);

    protected Grafo grafo;
    /** Posiciones manuales por indice de vertice (las demas se auto-colocan). */
    protected final Map<Integer, Point> posiciones;
    /** Vertice resaltado al dibujar (-1 = ninguno). Lo usa el editor. */
    protected int verticeResaltado;

    /** Crea el lienzo con un grafo no dirigido vacio. */
    public GrafoPanel() {
        this(new Grafo(false));
    }

    /** Crea el lienzo que dibuja el grafo indicado. */
    public GrafoPanel(Grafo grafo) {
        this.grafo = grafo;
        this.posiciones = new HashMap<>();
        this.verticeResaltado = -1;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(520, 400));
    }

    /** Grafo que se esta dibujando. */
    public Grafo getGrafo() {
        return grafo;
    }

    /** Reemplaza el grafo dibujado (las posiciones manuales se descartan). */
    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        this.verticeResaltado = -1;
        posiciones.clear();
        repaint();
    }

    /** Asigna la posicion manual de un vertice. */
    public void setPosicion(int indice, Point punto) {
        if (indice >= 0 && punto != null) {
            posiciones.put(indice, punto);
        }
        repaint();
    }

    /** Posicion actual del vertice o null si aun no tiene. */
    public Point getPosicion(int indice) {
        return posiciones.get(indice);
    }

    /**
     * Quita la posicion del vertice eliminado y recorre las posiciones de los
     * vertices con indice mayor (iguales a como reajusta la matriz el Grafo).
     */
    /**
     * Quita la posicion del vertice eliminado y recorre las posiciones de los
     * vertices con indice mayor (iguales a como reajusta la matriz el Grafo).
     * Al borrar el vertice i, todos los que estaban despues bajan una posicion,
     * asi que sus claves en el mapa deben restarse 1 para no "desincronizarse".
     */
    public void eliminarPosicion(int indice) {
        posiciones.remove(indice);
        Map<Integer, Point> nuevas = new HashMap<>();
        for (Map.Entry<Integer, Point> entrada : posiciones.entrySet()) {
            int clave = entrada.getKey() > indice ? entrada.getKey() - 1 : entrada.getKey();
            nuevas.put(clave, entrada.getValue());
        }
        posiciones.clear();
        posiciones.putAll(nuevas);
        repaint();
    }

    /** Vertice resaltado actualmente (-1 = ninguno). */
    public int getVerticeResaltado() {
        return verticeResaltado;
    }

    /** Resalta un vertice al dibujar (-1 quita el resaltado). */
    public void setVerticeResaltado(int indice) {
        this.verticeResaltado = indice;
        repaint();
    }

    /** Devuelve el indice del vertice bajo el punto (-1 si no hay ninguno). */
    public int verticeEn(Point punto) {
        if (grafo == null || punto == null) {
            return -1;
        }
        asegurarPosiciones(); // garantiza que todos los vertices tengan coordenadas
        int mejor = -1;
        double mejorDistancia = RADIO_VERTICE + 4; // margen un poco mayor al radio
        // Se busca el vertice MAS CERCANO al punto dentro del umbral, para que
        // si dos circulos se solapan gane el de en medio.
        for (int i = 0; i < grafo.getNumVertices(); i++) {
            Point p = posiciones.get(i);
            if (p == null) {
                continue;
            }
            double distancia = p.distance(punto);
            if (distancia <= mejorDistancia) {
                mejorDistancia = distancia;
                mejor = i;
            }
        }
        return mejor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (grafo == null || grafo.estaVacio()) {
            pintarMensajeVacio(g2);
        } else {
            asegurarPosiciones();
            for (Arista arista : grafo.getAristas()) {
                pintarArista(g2, arista);
            }
            for (int i = 0; i < grafo.getNumVertices(); i++) {
                pintarVertice(g2, i);
            }
        }
        pintarExtras(g2);
        g2.dispose();
    }

    /** Mensaje cuando no hay vertices. El editor lo cambia por sus instrucciones. */
    protected void pintarMensajeVacio(Graphics2D g2) {
        g2.setColor(COLOR_MENSAJE);
        FontMetrics fm = g2.getFontMetrics();
        String texto = "Grafo vacio";
        int x = (getWidth() - fm.stringWidth(texto)) / 2;
        g2.drawString(texto, x, getHeight() / 2);
    }

    /** Gancho para que el editor (Opcion 2) dibuje previews encima de todo. */
    protected void pintarExtras(Graphics2D g2) {
    }

    /** Coloca en circulo los vertices que aun no tienen posicion. */
    protected void asegurarPosiciones() {
        int n = grafo.getNumVertices();
        // Se listan solo los vertices "huecos" (sin coordenadas asignadas a mano).
        List<Integer> sinPosicion = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!posiciones.containsKey(i)) {
                sinPosicion.add(i);
            }
        }
        if (sinPosicion.isEmpty()) {
            return; // todos ya tienen posicion, no hay que hacer nada
        }
        if (n == 1) {
            posiciones.put(sinPosicion.get(0), new Point(getWidth() / 2, getHeight() / 2));
            return;
        }
        // Layout circular: se distribuyen los vertices faltantes en la circunferencia
        // de un circulo centrado en el panel. El angulo arranca en -PI/2 (arriba) y
        // se reparte uniformemente (2*PI/k) entre los que faltan.
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        double radio = Math.min(getWidth(), getHeight()) / 2.0 - MARGEN - RADIO_VERTICE;
        if (radio < RADIO_MINIMO) {
            radio = RADIO_MINIMO; // radio minimo para paneles muy pequenos
        }
        int m = sinPosicion.size();
        for (int k = 0; k < m; k++) {
            double angulo = 2 * Math.PI * k / m - Math.PI / 2;
            posiciones.put(sinPosicion.get(k),
                    new Point((int) (cx + radio * Math.cos(angulo)), (int) (cy + radio * Math.sin(angulo))));
        }
    }

    /** Dibuja una arista: linea, curva (si hay ida y vuelta) o bucle, con flecha y peso. */
    private void pintarArista(Graphics2D g2, Arista arista) {
        Point o = posiciones.get(arista.getOrigen());
        Point d = posiciones.get(arista.getDestino());
        if (o == null || d == null) {
            return;
        }
        g2.setColor(COLOR_ARISTA);
        g2.setStroke(TRAZO_ARISTA);
        if (arista.esBucle()) {
            pintarBucle(g2, arista, o);
            return;
        }
        double dx = d.x - o.x;
        double dy = d.y - o.y;
        double distancia = Math.hypot(dx, dy);
        if (distancia < 1) {
            return; // los dos centros casi coinciden: no hay nada que trazar
        }
        // Vector unitario (ux, uy) de origen a destino, para recortar la linea
        // por ambos extremos y que empiece/termine en el borde del circulo y no
        // en el centro (evita que la linea atraviese los vertices).
        double ux = dx / distancia;
        double uy = dy / distancia;
        double recorte = RADIO_VERTICE + 2;
        double x1 = o.x + ux * recorte;
        double y1 = o.y + uy * recorte;
        double x2 = d.x - ux * recorte;
        double y2 = d.y - uy * recorte;
        // Si existe la arista contraria (A->B y B->A en un grafo dirigido), en
        // vez de una linea recta se dibuja una CURVA desplazada lateralmente,
        // para que las dos flechas no se solapen.
        boolean doble = grafo.isDirigido() && grafo.hayArista(arista.getDestino(), arista.getOrigen());
        if (doble) {
            double separacion = 0.18 * distancia;
            // El punto de control se aleja de la recta en la direccion
            // perpendicular (-uy, ux) para "abrir" la curva hacia un lado.
            double cx = (x1 + x2) / 2 - uy * separacion;
            double cy = (y1 + y2) / 2 + ux * separacion;
            g2.draw(new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2));
            pintarFlecha(g2, x2, y2, x2 - cx, y2 - cy);
            if (arista.getPeso() > 1) {
                double fx = (x1 + x2) / 2 - uy * separacion * 1.35;
                double fy = (y1 + y2) / 2 + ux * separacion * 1.35;
                pintarPeso(g2, arista.getPeso(), fx, fy);
            }
        } else {
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
            if (grafo.isDirigido()) {
                pintarFlecha(g2, x2, y2, x2 - x1, y2 - y1);
            }
            if (arista.getPeso() > 1) {
                pintarPeso(g2, arista.getPeso(), (x1 + x2) / 2, (y1 + y2) / 2);
            }
        }
    }

    /** Bucle (arista de un vertice a si mismo) como un lazo sobre el vertice. */
    private void pintarBucle(Graphics2D g2, Arista arista, Point v) {
        double radioBucle = 13;
        double cx = v.x;
        double cy = v.y - RADIO_VERTICE - radioBucle + 5;
        g2.draw(new Ellipse2D.Double(cx - radioBucle, cy - radioBucle, 2 * radioBucle, 2 * radioBucle));
        if (grafo.isDirigido()) {
            pintarFlecha(g2, cx + radioBucle * 0.75, cy + radioBucle * 0.66, 0.45, 0.9);
        }
        if (arista.getPeso() > 1) {
            pintarPeso(g2, arista.getPeso(), cx, cy - radioBucle - 10);
        }
    }

    /** Punta de flecha rellena en (x, y) mirando hacia la direccion dada. */
    /** Punta de flecha rellena en (x, y) mirando hacia la direccion dada. */
    private void pintarFlecha(Graphics2D g2, double puntaX, double puntaY, double dirX, double dirY) {
        // Se calcula el angulo de la direccion con atan2 y a partir de el se
        // construye un triangulo: la punta apunta hacia (dirX, dirY) y la base
        // se aleja "largo" pixels atras, con un semiancho "mitad".
        double angulo = Math.atan2(dirY, dirX);
        double largo = 11;
        double mitad = 4.5;
        double baseX = puntaX - largo * Math.cos(angulo);
        double baseY = puntaY - largo * Math.sin(angulo);
        // Los dos vertices laterales de la base se desplazan en la perpendicular
        // (+seno, -coseno) y (-seno, +coseno) del angulo de la direccion.
        Path2D flecha = new Path2D.Double();
        flecha.moveTo(puntaX, puntaY);
        flecha.lineTo(baseX + mitad * Math.sin(angulo), baseY - mitad * Math.cos(angulo));
        flecha.lineTo(baseX - mitad * Math.sin(angulo), baseY + mitad * Math.cos(angulo));
        flecha.closePath();
        g2.fill(flecha); // se rellena el triangulo para que se vea solido
    }

    /** Peso de la arista dentro de una pastilla blanca para que se lea. */
    private void pintarPeso(Graphics2D g2, int peso, double x, double y) {
        String texto = String.valueOf(peso);
        FontMetrics fm = g2.getFontMetrics();
        int ancho = fm.stringWidth(texto) + 8;
        int alto = fm.getAscent() + 4;
        g2.setColor(Color.WHITE);
        g2.fillRoundRect((int) x - ancho / 2, (int) y - alto / 2, ancho, alto, alto, alto);
        g2.setColor(COLOR_PESO);
        g2.drawString(texto, (int) x - fm.stringWidth(texto) / 2, (int) (y + fm.getAscent() / 2 - 1));
    }

    /** Dibuja el circulo del vertice con su etiqueta centrada. */
    private void pintarVertice(Graphics2D g2, int indice) {
        Point p = posiciones.get(indice);
        if (p == null) {
            return;
        }
        boolean resaltado = indice == verticeResaltado;
        g2.setColor(resaltado ? COLOR_RESALTADO : COLOR_RELLENO);
        g2.fillOval(p.x - RADIO_VERTICE, p.y - RADIO_VERTICE, 2 * RADIO_VERTICE, 2 * RADIO_VERTICE);
        g2.setColor(resaltado ? COLOR_BORDE.darker() : COLOR_BORDE);
        g2.setStroke(TRAZO_VERTICE);
        g2.drawOval(p.x - RADIO_VERTICE, p.y - RADIO_VERTICE, 2 * RADIO_VERTICE, 2 * RADIO_VERTICE);
        String etiqueta = grafo.getEtiqueta(indice);
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.BLACK);
        g2.drawString(etiqueta, p.x - fm.stringWidth(etiqueta) / 2, p.y + fm.getAscent() / 2 - 2);
    }
}
