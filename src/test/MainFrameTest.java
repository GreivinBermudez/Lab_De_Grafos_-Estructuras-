package test;

import app.MainFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Prueba de humo de la ventana principal (Modulo 1): comprueba que se construye,
 * que muestra las dos opciones y que no lanza excepciones aunque los Modulos 2 y
 * 3 todavia no esten integrados.
 *
 * <p>Se omite automaticamente cuando no hay entorno grafico (por ejemplo en un
 * servidor de integracion continua sin pantalla).</p>
 */
class MainFrameTest {

    @Test
    @DisplayName("MainFrame abre con las dos opciones (Opcion 1 y Opcion 2)")
    void laVentanaAbresLasDosOpciones() throws Exception {
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
        assertNotNull(ventana.getGrafo());
        assertNotNull(ventana.getJMenuBar());

        SwingUtilities.invokeAndWait(ventana::dispose);
    }
}
