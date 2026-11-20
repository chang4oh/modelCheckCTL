package modelCheckCTL.tests;
import javax.imageio.ImageIO;
import java.awt.*;
import java.nio.file.Path;
public final class ScreenCapture {
    public static void main(String[] args) throws Exception {
        Thread.sleep(3000);
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        ImageIO.write(new Robot().createScreenCapture(new Rectangle(0,0,d.width,d.height)), "png", Path.of(args[0]).toFile());
    }
}
