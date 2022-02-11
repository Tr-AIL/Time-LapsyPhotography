import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ADBDevice {
    private final String deviceID;

    private ADBDevice(String deviceID) {
        this.deviceID = deviceID;
    }

    public String adbCommand(String cmd) throws IOException {
        Process ADBProcess = Runtime.getRuntime().exec("adb -s " + deviceID + " " + cmd);
        InputStream ADBInputStream = ADBProcess.getInputStream();
        StringBuilder originalSB = new StringBuilder();
        try {
            int b;
            while(true) { if((b = ADBInputStream.read()) != -1) break; }
            originalSB.append((char) b);
            while((b = ADBInputStream.read()) != -1) { originalSB.append((char) b); }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误，请联系开发者提交反馈\n" + e.getMessage());
            System.exit(1);
        }
        return originalSB.toString();
    }

    public long getScreenOffTimeOut() throws IOException {
        Process ADBProcess = Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "settings", "get",
                "system", "screen_off_timeout"});
        InputStream ADBInputStream = ADBProcess.getInputStream();
        StringBuilder originalSB = new StringBuilder();
        try {
            int b;
            while(true) { if((b = ADBInputStream.read()) != -1) break; }
            originalSB.append((char) b);
            while((b = ADBInputStream.read()) != -1) { originalSB.append((char) b); }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误，请联系开发者提交反馈\n" + e.getMessage());
            System.exit(1);
        }
        String result = originalSB.toString();
        return Integer.parseInt(result.substring(0, result.length()-2));
    }

    public void setScreenOffTimeOut(long timeOut) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "settings", "set", "system",
                "screen_off_timeout", String.valueOf(timeOut)});
    }

    public Dimension getScreenSize() throws IOException {
        Process ADBProcess = Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "wm", "size"});
        InputStream ADBInputStream = ADBProcess.getInputStream();
        StringBuilder originalSB = new StringBuilder();
        try {
            int b;
            while(true) { if((b = ADBInputStream.read()) == -1) break; }
            originalSB.append((char) b);
            while((b = ADBInputStream.read()) != -1) { originalSB.append((char) b); }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误，请联系开发者提交反馈\n" + e.getMessage());
            System.exit(1);
        }
        String result = originalSB.toString();
        return new Dimension(Integer.parseInt(result.substring(16, result.indexOf('x'))),
                Integer.parseInt(result.substring(result.indexOf('x')+1, result.length()-2)));
    }

    public void tap(int x, int y) throws IOException{
        Runtime.getRuntime().exec(new String[]
                {"adb", "-s", deviceID, "shell", "input", "tap", String.valueOf(x), String.valueOf(y)});
    }

    public void hold(int x, int y, int d) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "input", "swipe",
                String.valueOf(x), String.valueOf(y), String.valueOf(x), String.valueOf(y), String.valueOf(d)});
    }

    public void swipe(int x1, int y1, int x2, int y2, int d) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "input", "swipe",
                String.valueOf(x1), String.valueOf(y1), String.valueOf(x2), String.valueOf(y2), String.valueOf(d)});
    }

    public Image getCurrentScreenshot() throws IOException {
        Process ADBProcess = Runtime.getRuntime().exec(
                new String[]{"adb", "-s", deviceID, "shell", "/system/bin/screencap", "-p", "/sdcard/screen.png"});
        while(true) { if(!ADBProcess.isAlive()) break; }
        ADBProcess = Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "pull", "-a", "/sdcard/screen.png"});
        while(true) { if(!ADBProcess.isAlive()) break; }
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "rm", "-f", "/sdcard/screen.png"});
        Image screen = Toolkit.getDefaultToolkit().createImage("screen.png");
        new File("screen.png").delete();
        System.out.println("shot");
        return screen;
    }

    public void pullFile(String fromPathName) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "pull", "-a", fromPathName});
    }

    public void pullFile(String fromPathName, String targetPathName) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "pull", "-a", fromPathName, targetPathName});
    }

    public void keyEvent(int num) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "input", "keyevent", String.valueOf(num)});
    }

    public static class KeyEventNumber {
        public static int MENU = 1;
        public static int SOFT_RIGHT = 2;
        public static int HOME = 3;
        public static int BACK = 4;
        public static int CALL = 5;
        public static int ENDCALL = 6;
        public static int STAR = 17;
        public static int POUND = 18;
        public static int VOLUME_UP = 24;
        public static int VOLUME_DOWN = 25;
        public static int POWER = 26;
        public static int CAMERA = 27;
        public static int CLEAR = 28;
        public static int MUTE = 164;
    }

    public static ADBDevice[] getADBDevices() {
        Process ADBProcess = null;
        try {
            ADBProcess = Runtime.getRuntime().exec("adb devices");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "发生I/O错误，请检查应用是否完整或存储设备是否正常连接\n" + e.getMessage());
            System.exit(1);
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(null, "无法运行ADB进程，请检查程序权限\n" + e.getMessage());
            System.exit(1);
        }

        InputStream ADBInputStream = ADBProcess.getInputStream();
        StringBuilder originalSB = new StringBuilder();
        try {
            int b;
            while(true) { if((b = ADBInputStream.read()) != -1) break; }
            originalSB.append((char) b);
            while((b = ADBInputStream.read()) != -1) { originalSB.append((char) b); }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误，请联系开发者提交反馈\n" + e.getMessage());
            System.exit(1);
        }
        String originalString = originalSB.toString();
        ArrayList<ADBDevice> devices = new ArrayList<>();
        for(int i = 0; i < originalString.length(); i++) {
            if(originalString.charAt(i)=='\n') {
                for(int j = i; j < originalString.length(); j++) {
                    if(originalString.charAt(j) == '\t') {
                        devices.add(new ADBDevice(originalString.substring(i+1,j)));
                        i = j;
                        break;
                    }
                }
            }
        }
        ADBDevice[] result = new ADBDevice[devices.size()];
        for (int i = 0; i < devices.size(); i++) result[i] = devices.get(i);
        return result;
    }

    public String toString() {
        return deviceID;
    }
}