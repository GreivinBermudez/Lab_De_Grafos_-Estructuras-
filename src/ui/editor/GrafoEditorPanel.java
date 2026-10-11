package ui.editor;

import model.Arista;
import model.Grafo;
import model.GrafoListener;

import ui.dibujo.GrafoPanel;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Lienzo interactivo de la Opcion 2: se dibuja el grafo con el mouse.
 *
 * <p>Gestos soportados:</p>
 * <ul>
 *   <li>Clic en zona vacia: crea un vertice (etiqueta automatica A, B, C...).</li>
 *   <li>Arrastrar un vertice a zona vacia: lo mueve.</li>
 *   <li>Arrastrar de un vertice a otro: crea la arista entre los dos
 *       (el vertice arrastrado regresa a su posicion original).</li>
 *   <li>Clic derecho sobre un vertice o una arista: la elimina.</li>
 *   <li>Doble clic sobre un vertice: cambia su etiqueta; sobre una arista:
 *       cambia su peso.</li>
 * </ul>
 *
 * <p>Cuando hay aristas en los dos sentidos (grafo dirigido), el hit-test de
 * la arista elige la direccion segun el lado de la curva en que este el
 * mouse, igual que las dibuja {@code GrafoPanel}.</p>
 */
public class GrafoEditorPanel extends GrafoPanel implements GrafoListener {

    private static final long serialVersionUID = 1L;

    /** Distancia maxima al soltar para considerarlo clic (y no arrastre). */
    private static final int UMBRAL_CLIC = 6;
    /** Distancia maxima del mouse a una arista para considerarla "sobre ella". */
    private static final int UMBRAL_ARISTA = 6;
    /** Radio del bucle, sincronizado con el que dibuja GrafoPanel. */
    private static final int RADIO_BUCLE = 13;
    /** Ventana de tiempo en la que un vertice recien creado cuenta como el mismo clic doble. */
    private static final long VENTANA_DOBLE_CLIC_MS = 600;

    private static final BasicStroke TRAZO_PREVIEW =
            new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, new float[]{6f, 6f}, 0f);
    private static final Color COLOR_PREVIEW = new Color(0xE6, 0x7E, 0x22);

    /** Vertice presionado con el boton izquierdo (-1 = ninguno). */
    private int verticePresionado = -1;
    /** Posicion original del vertice presionado (para crear aristas sin moverlo). */
    private Point posicionInicial;
    /** Punto que se convierte en vertice si el clic se suelta sin arrastrar. */
    private Point pendienteCrear;
    /** Vertice creado por el ultimo clic (para no confundir el doble clic en zona vacia). */
    private int indiceUltimoCreado = -1;
    private long tiempoUltimoCreado;

    public GrafoEditorPanel() {
        super();
        getGrafo().addListener(this);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        registrarEventos();
    }

    /**
     * Al reemplazar el grafo (por ejemplo al Pegar una matriz) el editor deja
     * de escuchar el anterior y pasa a escuchar el nuevo.
     */
    @Override
    public void setGrafo(Grafo nuevoGrafo) {
        getGrafo().removeListener(this);
        super.setGrafo(nuevoGrafo);
        nuevoGrafo.addListener(this);
    }

    /** El editor se repinta cada vez que el grafo cambia. */
    @Override
    public void grafoCambio() {
        repaint();
    }

    // ------------------------------------------------------------------
    // API publica (la usan los gestos del mouse y las pruebas)
    // ------------------------------------------------------------------

    /** Crea un vertice en el punto indicado y devuelve su indice. */
    public int crearVerticeEn(Point punto) {
        int indice = getGrafo().agregarVertice(null);
        setPosicion(indice, new Point(punto));
        indiceUltimoCreado = indice;
        tiempoUltimoCreado = System.currentTimeMillis();
        return indice;
    }

    /** Crea la arista origen -> destino con peso 1 (si no existia). */
    public boolean crearArista(int origen, int destino) {
        if (origen == destino) {
            return false;
        }
        if (!getGrafo().hayArista(origen, destino)) {
            getGrafo().setPeso(origen, destino, 1);
        }
        return true;
    }

    /** Elimina el vertice bajo el punto; true si habia uno. */
    public boolean eliminarVerticeEn(Point punto) {
        int vertice = verticeEn(punto);
        if (vertice < 0) {
            return false;
        }
        getGrafo().eliminarVertice(vertice);
        eliminarPosicion(vertice);
        return true;
    }

    /** Elimina la arista bajo el punto; true si habia una. */
    public boolean eliminarAristaEn(Point punto) {
        int[] arista = aristaEn(punto);
        if (arista == null) {
            return false;
        }
        getGrafo().eliminarArista(arista[0], arista[1]);
        return true;
    }

    /** Cambia el peso de una arista existente (el peso debe ser >= 1). */
    public boolean cambiarPesoArista(int origen, int destino, int peso) {
        if (peso < 1 || !getGrafo().hayArista(origen, destino)) {
            return false;
        }
        getGrafo().setPeso(origen, destino, peso);
        return true;
    }

    /** Cambia la etiqueta de un vertice (texto vacio o null no cambia nada). */
    public void renombrarVertice(int indice, String etiqueta) {
        if (etiqueta == null) {
            return;
        }
        String texto = etiqueta.trim();
        if (!texto.isEmpty()) {
            getGrafo().setEtiqueta(indice, texto);
        }
    }

    /** Elimina todos los vertices y aristas dibujados. */
    public void limpiarTodo() {
        getGrafo().limpiar();
        posiciones.clear();
        verticePresionado = -1;
        posicionInicial = null;
        pendienteCrear = null;
        repaint();
    }

    /** Devuelve el vertice bajo el punto, ignorando el vertice indicado. */
    public int verticeEn(Point punto, int excluido) {
        if (punto == null) {
            return -1;
        }
        int mejor = -1;
        double mejorDistancia = RADIO_VERTICE + 4;
        for (int i = 0; i < getGrafo().getNumVertices(); i++) {
            if (i == excluido) {
                continue;
            }
            Point p = getPosicion(i);
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

    /**
     * Devuelve la arista bajo el punto como {@code {origen, destino}} o null.
     * Si hay aristas en los dos sentidos se elige la del lado de la curva.
     */
    public int[] aristaEn(Point punto) {
        Grafo g = getGrafo();
        if (punto == null || g == null || g.estaVacio()) {
            return null;
        }
        asegurarPosiciones();
        int[] mejor = null;
        double mejorDistancia = UMBRAL_ARISTA;
        for (Arista arista : g.getAristas()) {
            Point o = getPosicion(arista.getOrigen());
            Point d = getPosicion(arista.getDestino());
            if (o == null || d == null) {
                continue;
            }
            double distancia;
            if (arista.esBucle()) {
                double centroY = o.y - RADIO_VERTICE - RADIO_BUCLE + 5;
                distancia = Math.hypot(punto.x - o.x, punto.y - centroY);
            } else {
                distancia = distanciaASegmento(punto, o, d);
            }
            if (distancia > UMBRAL_ARISTA) {
                continue;
            }
            if (mejor != null && !arista.esBucle()
                    && mejor[0] == arista.getDestino() && mejor[1] == arista.getOrigen()) {
                // Es la inversa de la candidata actual: se decide por el lado de la curva.
                Point mo = getPosicion(mejor[0]);
                Point md = getPosicion(mejor[1]);
                double cruz = (md.x - mo.x) * (punto.y - mo.y) - (md.y - mo.y) * (punto.x - mo.x);
                if (cruz <= 0) {
                    mejor = new int[]{arista.getOrigen(), arista.getDestino()};
                }
                continue;
            }
            if (mejor == null || distancia < mejorDistancia) {
                mejor = new int[]{arista.getOrigen(), arista.getDestino()};
                mejorDistancia = distancia;
            }
        }
        return mejor;
    }

    // ------------------------------------------------------------------
    // Eventos del mouse
    // ------------------------------------------------------------------

    private void registrarEventos() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    procesarClicDerecho(e.getPoint());
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        procesarDobleClic(e.getPoint());
                    } else {
                        procesarClicIzquierdo(e.getPoint());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    procesarSoltado(e.getPoint());
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                procesarArrastre(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                procesarMovimiento(e.getPoint());
            }
        });
    }

    private void procesarClicIzquierdo(Point punto) {
        int vertice = verticeEn(punto);
        if (vertice >= 0) {
            verticePresionado = vertice;
            posicionInicial = new Point(getPosicion(vertice));
        } else if (aristaEn(punto) == null) {
            pendienteCrear = new Point(punto);
        }
    }

    private void procesarSoltado(Point punto) {
        try {
            if (verticePresionado >= 0) {
                int destino = verticeEn(punto, verticePresionado);
                if (destino >= 0) {
                    crearArista(verticePresionado, destino);
                    setPosicion(verticePresionado, posicionInicial); // era un gesto de arista
                }
                setVerticeResaltado(-1);
            } else if (pendienteCrear != null && pendienteCrear.distance(punto) <= UMBRAL_CLIC) {
                crearVerticeEn(pendienteCrear);
            }
        } finally {
            verticePresionado = -1;
            posicionInicial = null;
            pendienteCrear = null;
        }
    }

    private void procesarArrastre(Point punto) {
        if (verticePresionado >= 0) {
            setPosicion(verticePresionado, punto);
            setVerticeResaltado(verticeEn(punto, verticePresionado));
        }
    }

    private void procesarMovimiento(Point punto) {
        int vertice = verticeEn(punto);
        setVerticeResaltado(vertice);
        setCursor(vertice >= 0
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    private void procesarClicDerecho(Point punto) {
        if (!eliminarVerticeEn(punto)) {
            eliminarAristaEn(punto);
        }
    }

    private void procesarDobleClic(Point punto) {
        int vertice = verticeEn(punto);
        if (vertice >= 0) {
            boolean recienCreado = vertice == indiceUltimoCreado
                    && System.currentTimeMillis() - tiempoUltimoCreado < VENTANA_DOBLE_CLIC_MS;
            if (!recienCreado) {
                dialogoRenombrar(vertice);
            }
            return;
        }
        int[] arista = aristaEn(punto);
        if (arista != null) {
            dialogoPeso(arista[0], arista[1]);
        }
    }

    // ------------------------------------------------------------------
    // Cuadros de dialogo
    // ------------------------------------------------------------------

    private void dialogoRenombrar(int indice) {
        String actual = getGrafo().getEtiqueta(indice);
        String entrada = JOptionPane.showInputDialog(this,
                "Etiqueta del vertice " + actual + ":", actual);
        renombrarVertice(indice, entrada);
    }

    private void dialogoPeso(int origen, int destino) {
        Grafo g = getGrafo();
        String titulo = g.getEtiqueta(origen) + (g.isDirigido() ? " -> " : " - ") + g.getEtiqueta(destino);
        String entrada = JOptionPane.showInputDialog(this,
                "Peso de la arista " + titulo + " (entero >= 1):",
                String.valueOf(g.getPeso(origen, destino)));
        if (entrada == null) {
            return;
        }
        try {
            int peso = Integer.parseInt(entrada.trim());
            if (!cambiarPesoArista(origen, destino, peso)) {
                JOptionPane.showMessageDialog(this,
                        "El peso debe ser un entero mayor o igual que 1.",
                        "Peso invalido", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El peso debe ser un entero mayor o igual que 1.",
                    "Peso invalido", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ------------------------------------------------------------------
    // Dibujo
    // ------------------------------------------------------------------

    /** Linea discontinua al vertice destino mientras se arrastra una arista. */
    @Override
    protected void pintarExtras(Graphics2D g2) {
        if (verticePresionado >= 0 && getVerticeResaltado() >= 0) {
            Point origen = getPosicion(verticePresionado);
            Point destino = getPosicion(getVerticeResaltado());
            if (origen != null && destino != null) {
                g2.setColor(COLOR_PREVIEW);
                g2.setStroke(TRAZO_PREVIEW);
                g2.drawLine(origen.x, origen.y, destino.x, destino.y);
            }
        }
    }

    /** Instrucciones de uso cuando el lienzo esta vacio. */
    @Override
    protected void pintarMensajeVacio(Graphics2D g2) {
        String[] lineas = {
                "Clic en zona vacia: crear vertice",
                "Arrastrar de un vertice a otro: crear arista",
                "Arrastrar un vertice a zona vacia: moverlo",
                "Clic derecho: eliminar vertice o arista",
                "Doble clic en un vertice: etiqueta; en una arista: peso"
        };
        g2.setColor(new Color(0x99, 0x99, 0x99));
        FontMetrics fm = g2.getFontMetrics();
        int y = getHeight() / 2 - (lineas.length * (fm.getHeight() + 6)) / 2;
        for (String linea : lineas) {
            int x = (getWidth() - fm.stringWidth(linea)) / 2;
            g2.drawString(linea, x, y);
            y += fm.getHeight() + 6;
        }
    }

    // ------------------------------------------------------------------
    // Geometria
    // ------------------------------------------------------------------

    /** Distancia del punto al segmento a-b. */
    private double distanciaASegmento(Point p, Point a, Point b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double longitud2 = dx * dx + dy * dy;
        if (longitud2 < 1e-9) {
            return p.distance(a);
        }
        double t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / longitud2;
        t = Math.max(0.0, Math.min(1.0, t));
        return Math.hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy));
    }
}
