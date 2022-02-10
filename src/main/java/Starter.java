import ADB.ADBDevice;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Starter {
    public static String path = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(() -> {
            TerminalFrame mainFrame = new TerminalFrame();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            int width = 800;
            int height = 600;
            int x = (toolkit.getScreenSize().width-width)/2;
            int y = (toolkit.getScreenSize().height-height)/2;
            mainFrame.setBounds(x, y, width, height);
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setTitle("Photography Control Terminal");
            mainFrame.setResizable(false);
            mainFrame.setVisible(true);
        });
    }
}

class TerminalFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 400;

    public TerminalFrame() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}