# Lab_De_Grafos_-Estructuras-

Aplicacion de escritorio en Java (Swing + Java2D) para graficar grafos y su matriz de
adyacencia. El trabajo esta dividido en 3 modulos (ver `division_modulos_grafos_java.pdf`):

| Modulo | Responsabilidad | Paquetes |
|--------|-----------------|----------|
| **1** | Nucleo (modelo), aplicacion principal, pruebas e integracion | `model`, `app`, `test` |
| 2 | Opcion 1: matriz a grafo + renderizador `GrafoPanel` | `ui.matriz`, `ui.dibujo` |
| 3 | Opcion 2: dibujar grafo con el mouse y generar la matriz | `ui.editor` |

## Estado: Modulo 1 terminado

Rama sugerida de trabajo: `feature/modulo1-modelo`.

```
src/
 +-- app/          Main.java, MainFrame.java, PanelPendiente.java
 +-- model/        Grafo.java, Arista.java, ConversorGrafo.java,
 |                 MatrizInvalidaException.java, GrafoListener.java
 +-- test/         GrafoTest.java, ConversorGrafoTest.java, MainFrameTest.java
lib/               junit-platform-console-standalone-1.11.4.jar (solo para las pruebas)
```

### Que hace ya la ventana principal

`MainFrame` abre con dos pestanas (Opcion 1 y Opcion 2) y los menus
`Archivo > Salir` y `Ayuda > Como usar la aplicacion / Acerca de`.

Los paneles de las dos opciones se cargan **por reflexion** desde
`ui.matriz.PanelOpcion1` y `ui.editor.PanelOpcion2`. Mientras esas clases no existan se
muestra un panel informativo (`app.PanelPendiente`) con el contrato esperado y una tabla
de ejemplo generada con el modelo real. Cuando los Modulos 2 y 3 suban sus clases, la
ventana las usara automaticamente: **no hay que modificar `MainFrame`**.

Constructores aceptados por el cargador: `(model.Grafo)` o vacio (en ese orden).

## Como ejecutarlo en IntelliJ

1. `Git > Clone` el repositorio y abrir el proyecto: el modulo ya tiene `src` como carpeta
   de fuentes y el jar de JUnit 5 en `lib/` como dependencia.
2. Ejecutar la clase `app.Main` (boton Run).
3. Pruebas: clic derecho sobre la carpeta `src/test` > `Run 'All Tests'`.

### Desde consola (sin IntelliJ)

```powershell
# compilar
javac -encoding UTF-8 -cp lib\junit-platform-console-standalone-1.11.4.jar -d out\production\Lab_De_Grafos_-Estructuras- (Get-ChildItem -Recurse src -Filter *.java | % FullName)

# ejecutar las pruebas
java -jar lib\junit-platform-console-standalone-1.11.4.jar execute --class-path out\production\Lab_De_Grafos_-Estructuras- --scan-class-path --disable-ansi-colors --details=summary

# abrir la aplicacion
java -cp out\production\Lab_De_Grafos_-Estructuras- app.Main
```

## Contrato congelado entre modulos (seccion 4 del PDF)

```java
// model/Grafo.java
public class Grafo {
    public Grafo(boolean dirigido);
    public boolean isDirigido();
    public void setDirigido(boolean dirigido);
    public int  getNumVertices();
    public int  agregarVertice(String etiqueta);      // devuelve su indice
    public void eliminarVertice(int indice);
    public String getEtiqueta(int indice);
    public void setPeso(int origen, int destino, int peso);  // 0 = sin arista
    public int  getPeso(int origen, int destino);
    public boolean hayArista(int origen, int destino);
    public List<Arista> getAristas();
    public void addListener(GrafoListener l);
}

// model/ConversorGrafo.java
public final class ConversorGrafo {
    public static Grafo desdeMatriz(int[][] m, boolean dirigido) throws MatrizInvalidaException;
    public static int[][] aMatriz(Grafo g);
    public static void validar(int[][] m, boolean dirigido) throws MatrizInvalidaException;
}

// model/GrafoListener.java
public interface GrafoListener { void grafoCambio(); }
```

### Metodos extra ya disponibles (los necesitan los Modulos 2 y 3)

- `Grafo`: `setEtiqueta`, `getEtiquetas`, `removeListener`, `agregarArista(origen, destino)`,
  `eliminarArista(origen, destino)`, `esBucle(v)`, `getGrado(v)`, `getGradoEntrada(v)`,
  `getGradoSalida(v)`, `limpiar()`, `estaVacio()`, `esCompleto()`, `esConexo()` y
  `etiquetaPorDefecto(int)`.
- `ConversorGrafo`: `matrizVacia(n)`, `clonar(m)`, `redimensionar(m, n)` (conserva los datos
  al cambiar el tamano de la matriz), `aleatoria(n, dirigido, probabilidad)`, `aTexto(m)` y
  `desdeTexto(texto, dirigido)` para copiar/pegar la matriz entre las dos opciones.

### Reglas de diseno acordadas

- Un `0` en `[i][j]` significa "sin arista"; un valor mayor que 0 significa arista (y su peso).
- Grafo no dirigido: la matriz es simetrica, `setPeso(i, j, p)` actualiza tambien `[j][i]`.
- Grafo dirigido: cada celda es independiente.
- Bucles (diagonal principal) permitidos; se dibujan como un lazo sobre el vertice.
- Ningun panel modifica la matriz directamente: modifica el `Grafo` y escucha con `GrafoListener`.

## Pruebas incluidas (seccion 8 del PDF)

`GrafoTest`, `ConversorGrafoTest` y `MainFrameTest` (31 pruebas):

- Matriz vacia (0x0), 1 vertice, bucle en la diagonal, grafo completo y conexo.
- Matriz no cuadrada, valores negativos y no simetrica con el mensaje
  `La matriz no es simetrica: [0][1] != [1][0]`.
- Ida y vuelta: matriz -> grafo -> matriz devuelve la misma matriz.
- Agregar y quitar vertices conserva los datos previos (casos 2 y 7).
- Grafo dirigido con una sola flecha (`[0][1]=1`, `[1][0]=0`).
- Notificacion de cambios a los `GrafoListener`.
- Prueba de humo de la ventana principal: abre con las dos opciones sin excepciones
  (se omite sola si no hay entorno grafico).

Los casos 4, 6 y 7 en su parte grafica (dibujo del bucle, trazado con el mouse) son
manuales de los Modulos 2 y 3; su logica ya esta cubierta aqui.

## Trabajo con Git

- Ramas: `main` (siempre funcional), `feature/modulo1-modelo`, `feature/modulo2-matriz`,
  `feature/modulo3-editor`.
- Integrar solo mediante Pull Request revisado por otra persona y `Pull/Rebase` de `main`
  antes de subir.
- Regla de oro: cada persona edita solo los archivos de su paquete. Si hace falta un metodo
  nuevo en `Grafo`, se le pide al dueno del Modulo 1.
- `.gitignore` incluye `.idea/`, `*.iml`, `out/`, `build/` y `*.class`.

## Siguiente paso (etapa 3 del plan)

Cuando el Modulo 2 publique `ui.dibujo.GrafoPanel` y `ui.matriz.PanelOpcion1`, y el Modulo 3
publique `ui.editor.GrafoEditorPanel` y `ui.editor.PanelOpcion2`, basta con recompilar: la
ventana principal los cargara sola. Si un panel tuviera un error al construirse, la ventana
muestra el aviso en la pestana en lugar de cerrarse.
