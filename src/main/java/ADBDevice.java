import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ADBDevice {
    static final Map<String, String> dateMap = new HashMap<>();
    static {
        dateMap.put("Jan", "01");
        dateMap.put("Feb", "02");
        dateMap.put("Mar", "03");
        dateMap.put("Apr", "04");
        dateMap.put("May", "05");
        dateMap.put("Jun", "06");
        dateMap.put("Jul", "07");
        dateMap.put("Aug", "08");
        dateMap.put("Sep", "09");
        dateMap.put("Oct", "10");
        dateMap.put("Nov", "11");
        dateMap.put("Dec", "12");
    }

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

    public Process tap(int x, int y) throws IOException{
        return Runtime.getRuntime().exec(new String[]
                {"adb", "-s", deviceID, "shell", "input", "tap", String.valueOf(x), String.valueOf(y)});
    }

    public Process hold(int x, int y, int d) throws IOException {
        return Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "input", "swipe",
                String.valueOf(x), String.valueOf(y), String.valueOf(x), String.valueOf(y), String.valueOf(d)});
    }

    public Process swipe(int x1, int y1, int x2, int y2, int d) throws IOException {
        return Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "input", "swipe",
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

    public Process pullFile(String fromPathName) throws IOException {
        return Runtime.getRuntime().exec(new String[]{ "adb", "-s", deviceID, "pull", "-a", fromPathName });
    }

    public Process pullFile(String fromPathName, String targetPathName) throws IOException {
        return Runtime.getRuntime().exec(new String[]{ "adb", "-s", deviceID, "pull", "-a", fromPathName, targetPathName });
    }

    public Process keyEvent(int num) throws IOException {
        return Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "input", "keyevent", String.valueOf(num)});
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

    public Process setScreenOffTimeOut(long timeOut) throws IOException {
        return Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "settings", "set", "system",
                "screen_off_timeout", String.valueOf(timeOut)});
    }

    public Dimension getScreenSize() throws IOException {
        Process ADBProcess = Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "wm", "size"});
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
        return new Dimension(Integer.parseInt(result.substring(15, result.indexOf('x'))),
                Integer.parseInt(result.substring(result.indexOf('x')+1, result.length()-2)));
    }

    /*
    public String getCameraImageName(String targetPathName, String imgDate) throws IOException {
        String targetFileName;
        Process pullProcess = Runtime.getRuntime().exec(new String[]{ "adb", "-s", deviceID, "pull", "" });
        String year = imgDate.substring(imgDate.length() - 6, imgDate.length() - 2);
        String fileNameNotIncludeSec;
        int ss;
        if (imgDate.startsWith("Sep", 4)) {
            fileNameNotIncludeSec = "IMG_" + year + dateMap.get(imgDate.substring(4, 7)) +
                    imgDate.substring(9, 11) + "_" + imgDate.substring(12, 14) + imgDate.substring(15, 17);
        }
        else {
            fileNameNotIncludeSec = "IMG_" + year + dateMap.get(imgDate.substring(4, 7)) + imgDate.substring(8, 10)
                    + "_" + imgDate.substring(11, 13) + imgDate.substring(14, 16);
            ss = Integer.parseInt(imgDate.substring(17, 19));
        }
    }*/

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