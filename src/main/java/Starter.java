import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Starter {
    public static String path = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(() -> {
            TerminalFrame mainFrame = new TerminalFrame();
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setTitle("Photography Control Terminal");
            mainFrame.setResizable(false);
            mainFrame.setVisible(true);
        });
    }
}

class ScreenDisplay extends JComponent {
    private final ADBDevice device;
    private Image image;

    public ScreenDisplay(ADBDevice device) {
        this.device = device;
        setBounds(0, 0, 800, 600);
        new Thread(() -> {
            try {
                image = this.device.getCurrentScreenshot();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        setVisible(true);
    }

    @Override
    public void paintComponents(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, 0, 0, this);
    }
}

class TerminalFrame extends JFrame {
    private final int DEFAULT_WIDTH;
    private final int DEFAULT_HEIGHT;

    public TerminalFrame() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        DEFAULT_WIDTH = 800;
        DEFAULT_HEIGHT = 600;
        int x = (toolkit.getScreenSize().width-DEFAULT_WIDTH)/2;
        int y = (toolkit.getScreenSize().height-DEFAULT_HEIGHT)/2;
        setBounds(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        ScreenDisplay display = new ScreenDisplay(ADBDevice.getADBDevices()[0]);
        add(display);
    }
}