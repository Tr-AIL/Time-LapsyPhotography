import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AutomaticShooter {
    private final ADBDevice device;
    private boolean isNoCameraSleep = false;
    private boolean shoot = false;
    private int fileCounts = 0;
    private String beforeString;
    private String afterString;

    public AutomaticShooter(ADBDevice device) {
        this.device = device;
    }

    public void startWithoutCameraSleep(int periodTime, int totalTime, int frameRate, int videoQuality, String videoRate) {
        if(periodTime > 2) isNoCameraSleep = true;
        start(periodTime, totalTime, frameRate, videoQuality, videoRate);
    }

    public void start(int periodTime, int totalTime, int frameRate, int videoQuality, String videoName) {
        try {
            beforeString = device.adbCommand("shell ls /sdcard/DCIM/Camera/");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
            throw new RuntimeException(e);
        }

        fileCounts = totalTime / periodTime;

        System.out.println("开始拍摄...");

        Thread shootThread = new Thread(() -> {
            for (int i = 0; i < fileCounts; i++) {
                try {
                    while (!shoot) Thread.sleep(50);
                    device.keyEvent(ADBDevice.KeyEventNumber.CAMERA);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                    throw new RuntimeException(e);
                } catch (InterruptedException ignored) {}
                shoot = false;
            }
        });

        Thread shootClockThread = new Thread(() -> {
            long screenOffTimeOut;
            try {
                screenOffTimeOut = device.getScreenOffTimeOut();
                Dimension d = device.getScreenSize();
                device.setScreenOffTimeOut(periodTime+5);
                if(isNoCameraSleep) {
                    for (int i = 0; i < fileCounts; i++) {
                        shoot = true;
                        Thread.sleep(1000L * periodTime - 2000);
                        device.tap(d.width / 2, d.height / 2);
                        Thread.sleep(2000);
                    }
                } else {
                    for(int i = 0; i < fileCounts; i++) {
                        shoot = true;
                        Thread.sleep(1000L * periodTime);
                    }
                }
                device.setScreenOffTimeOut(screenOffTimeOut);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) { }
        });

        Thread totalTimeClockThread = new Thread(() -> {
            try {
                Thread.sleep(1000L * totalTime);
                while (shootThread.isAlive() && shootClockThread.isAlive()) Thread.sleep(100);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "计时线程出现异常！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            }
            pullFile();
            File lastIMG = new File("shotImages\\IMG" + fileCounts + ".jpg");
            try {
                while(!lastIMG.exists()) Thread.sleep(100);
                System.out.println("拍摄完成，正在合成...");
                Process ffmpeg = FFMPEGController.start(frameRate, videoQuality, videoName);
                while(ffmpeg.isAlive()) Thread.sleep(200);
                System.out.println("合成完毕！");
            } catch (InterruptedException ignored) {}
        });

        shootThread.start();
        totalTimeClockThread.start();
        shootClockThread.start();
    }

    private void pullFile() {
        try {
            afterString = device.adbCommand("shell ls /sdcard/DCIM/Camera/");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
            throw new RuntimeException(e);
        }

        List<String> fileList = new LinkedList<>();

        if(beforeString.contains("VID") && beforeString.contains("IMG")) {
            int firstVidIndex = beforeString.indexOf("VID");
            int beforeLastImgIndex;
            int afterFirstImgIndex;

            for(int i = firstVidIndex; ; i--) {
                if(beforeString.startsWith("IMG", i)) {
                    beforeLastImgIndex = i;
                    break;
                }
            }

            for(int i = beforeLastImgIndex; ; i++) {
                if(beforeString.charAt(i) == '\r') {
                    afterFirstImgIndex = i + 2;
                    break;
                }
            }

            String neededFiles = afterString.substring(afterFirstImgIndex);

            int fileNameLength;
            for(int i = 0; ; i++) {
                if(neededFiles.charAt(i) == '\r') {
                    fileNameLength = i;
                    break;
                }
            }

            for(int i = 0; i < fileCounts; i++) {
                int start = i * (fileNameLength + 2);
                fileList.add(neededFiles.substring(start, start + fileNameLength));
            }
        }

        if(!beforeString.contains("IMG")) {
            int fileNameLength;

            for(int i = afterString.indexOf("IMG"); ; i++) {
                if(afterString.charAt(i) == '\r') {
                    fileNameLength = i - afterString.indexOf("IMG");
                    break;
                }
            }

            for(int i = 0; i < fileCounts; i++) fileList.add(afterString.substring(i * fileNameLength, (i + 1) * fileNameLength - 1));
        }


        if(!beforeString.contains("VID")) {
            int beforeLastImgIndex;
            int afterFirstImgIndex;

            for(int i = beforeString.length() - 1; ; i--) {
                if(beforeString.startsWith("IMG", i)) {
                    beforeLastImgIndex = i;
                    break;
                }
            }

            for(int i = beforeLastImgIndex; ; i++) {
                if(beforeString.charAt(i) == '\r') {
                    afterFirstImgIndex = i + 2;
                    break;
                }
            }

            String neededFiles = afterString.substring(afterFirstImgIndex);

            int fileNameLength;
            for(int i = 0; ; i++) {
                if(neededFiles.charAt(i) == '\r') {
                    fileNameLength = i;
                    break;
                }
            }

            for(int i = 0; i < fileCounts; i++) {
                int start = i * (fileNameLength + 2);
                fileList.add(neededFiles.substring(start, start + fileNameLength));
            }
        }

        for(int i = 0; i< fileList.size(); i++) {
            try {
                String fileName = fileList.get(i);
                device.pullFile("/sdcard/DCIM/Camera/" + fileName, "shotImages\\IMG" + (i + 1) + ".jpg");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}