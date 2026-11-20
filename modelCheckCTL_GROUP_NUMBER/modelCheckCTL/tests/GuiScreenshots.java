package modelCheckCTL.tests;

import modelCheckCTL.controller.MainController;
import modelCheckCTL.view.MainWindow;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;

/** UI screenshot driver: launches the actual app and clicks its real Check button. */
public final class GuiScreenshots {
    private static MainWindow window;
    private static MainController controller;
    private static Robot robot;
    private static Path dest;

    public static void main(String[] args) throws Exception {
        dest=Path.of(args[0]);Files.createDirectories(dest);
        robot=new Robot(); robot.setAutoDelay(90);
        SwingUtilities.invokeAndWait(() -> {
            window=new MainWindow(); controller=new MainController(window);
            window.setSize(890,710);window.setLocation(100,55);window.setVisible(true);
        });
        check("model1-true.png", "tests/fixtures/Model 1.txt", "s1", "EXq");
        check("model1-false.png", "tests/fixtures/Model 1.txt", "s2", "EXq");
        check("critical-section.png", "tests/fixtures/Model 4.txt", "s1", "AG(t1 -> AF c1)");
        check("microwave.png", "tests/microwave/microwave.txt", "s1", "AG(Start -> AF Heat)");
        SwingUtilities.invokeAndWait(() -> {
            try { controller.load(Path.of("tests/fixtures/Broken Model 3.txt")); }
            catch (Exception e) { SwingUtilities.invokeLater(() -> window.showError(e.getMessage())); }
        });
        Thread.sleep(850);
        capture("validation-error-popup.png");
        SwingUtilities.invokeAndWait(() -> window.dispose());
        System.out.println("Captured 5 actual Swing GUI screenshots in " + dest);
    }
    private static void check(String png, String path, String state, String expression) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                controller.load(Path.of(path));
                window.setInputs(state, expression);
                window.triggerCheck();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        Thread.sleep(250);
        capture(png);
    }
    private static void capture(String name) throws Exception {
        Dimension dim=Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage screenshot=robot.createScreenCapture(new Rectangle(0,0,dim.width,dim.height));
        ImageIO.write(screenshot,"png",dest.resolve(name).toFile());
    }
}
