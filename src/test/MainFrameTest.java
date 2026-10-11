package test;

import app.MainFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.editor.PanelOpcion2;
import ui.matriz.PanelOpcion1;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Prueba de humo de la ventana principal: comprueba que se construye, que
 * muestra las dos opciones y que cada pestana carga el panel correcto.
 *
 * <p>Se omite automaticamente cuando no hay entorno grafico (por ejemplo en
 * un servidor de integracion continua sin pantalla).</p>
 */
class MainFrameTest {

    @Test
    @DisplayName("MainFrame abre con las dos opciones reales cargadas")
    void laVentanaAbreConLasDosOpciones() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Requiere entorno grafico");

        AtomicReference<MainFrame> referencia = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            MainFrame ventana = new MainFrame();
            referencia.set(ventana);
        });

        MainFrame ventana = referencia.get();
        assertNotNull(ventana);
        assertEquals(2, ventana.getNumeroOpciones());
        assertTrue(ventana.getPestanas().getTitleAt(0).contains("Opcion 1"));
        assertTrue(ventana.getPestanas().getTitleAt(1).contains("Opcion 2"));
        assertTrue(ventana.getPestanas().getComponentAt(0) instanceof PanelOpcion1,
                "la pestana 1 debe cargar PanelOpcion1");
        assertTrue(ventana.getPestanas().getComponentAt(1) instanceof PanelOpcion2,
                "la pestana 2 debe cargar PanelOpcion2");
        assertNotNull(ventana.getJMenuBar());

        SwingUtilities.invokeAndWait(ventana::dispose);
    }
}
