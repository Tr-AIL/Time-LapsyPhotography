package ADB;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ADBDevice {
    private final String deviceID;

    private ADBDevice(String deviceID) {
        this.deviceID = deviceID;
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
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "shell", "/system/bin/screencap", "-p", "/sdcard/screen.png"});
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "pull", "-a", "/sdcard/screen.png"});
        Image screen = Toolkit.getDefaultToolkit().createImage("screen.png");
        new File("screen.png").delete();
        return screen;
    }

    public void pullFile(String fromPathName) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "pull", "-a", fromPathName});
    }

    public void pullFile(String fromPathName, String targetPathName) throws IOException {
        Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceID, "pull", "-a", fromPathName, targetPathName});
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
        int totalBytes;
        while (true) {
            try {
                if (ADBInputStream.available() != -1) {
                    totalBytes = ADBInputStream.available();
                    break;
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "程序发生逻辑错误，请联系开发者提交反馈\n" + e.getMessage());
                System.exit(1);
            }
        }

        byte[] bytes = new byte[totalBytes];
        try {
            ADBInputStream.read(bytes);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生I/O错误，请联系开发者提交反馈\n" + e.getMessage());
        }

        StringBuilder devicesSB = new StringBuilder();
        for(byte b : bytes) {
            devicesSB.append(b);
        }
        String devices = devicesSB.toString();
        return null;
    }
}
