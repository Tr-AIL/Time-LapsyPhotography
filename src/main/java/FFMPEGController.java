import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class FFMPEGController {
    public static Process start(int frameRate, int videoQuality, String videoName) {
        try {
            return Runtime.getRuntime().exec("ffmpeg -r " + frameRate + " -f image2 -i IMG%d.jpg -crf "
                            + videoQuality + " " + videoName + ".mp4", null, new File("shotImages\\"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
