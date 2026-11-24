package modelCheckCTL.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import modelCheckCTL.controller.MainController;
import modelCheckCTL.view.MainWindow;
import org.junit.jupiter.api.Test;

/** Controller wiring checks; skipped where no graphical display is available. */
class ControllerTest {
    @Test
    void loadingModelPopulatesStateSelector() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Swing GUI requires a display");
        SwingUtilities.invokeAndWait(() -> {
            MainWindow window = new MainWindow();
            try {
                MainController controller = new MainController(window);
                controller.load(Path.of("tests/fixtures/Model 1.txt"));
                assertEquals("s1", window.currentState());
                window.setInputs("s4", "AXq");
                assertEquals("s4", window.currentState());
            } catch (Exception error) {
                throw new RuntimeException(error);
            } finally {
                window.dispose();
            }
        });
    }

    @Test
    void clickingCheckDisplaysActualFormulaResult() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Swing GUI requires a display");
        SwingUtilities.invokeAndWait(() -> {
            MainWindow window = new MainWindow();
            try {
                MainController controller = new MainController(window);
                controller.load(Path.of("tests/fixtures/Model 1.txt"));
                window.setInputs("s1", "EXq");
                window.triggerCheck();
                assertTrue(hasText(window.getContentPane(), "Property HOLDS in state s1"));
            } catch (Exception error) {
                throw new RuntimeException(error);
            } finally {
                window.dispose();
            }
        });
    }

    private static boolean hasText(Component component, String expected) {
        if (component instanceof JTextArea area && area.getText().contains(expected)) return true;
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) if (hasText(child, expected)) return true;
        }
        return false;
    }
}
