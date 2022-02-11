import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AutomaticShooter {
    private static final Map<String, String> dateMap = new HashMap<>();
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

    private final ADBDevice device;
    private boolean isNoCameraSleep = false;
    private boolean shoot = false;
    private String targetFileName = null;

    public AutomaticShooter(ADBDevice device) {
        this.device = device;
    }

    public void startWithoutCameraSleep(int pt, int tt, int fr) {
        if(pt > 2) isNoCameraSleep = true;
        start(pt, tt, fr);
    }

    public void start(int pt, int tt, int fr) {
         Thread shootThread = new Thread(() -> {
            for (int i = 0; i < (tt/pt); i++) {
                while (true) if(shoot) break;
                try {
                    device.keyEvent(ADBDevice.KeyEventNumber.CAMERA);
                    String oriDate = device.adbCommand("shell date");
                    String year = oriDate.substring(oriDate.length() - 4);
                    if (oriDate.startsWith("Sep", 5)) targetFileName = "IMG_" + year + dateMap.get(oriDate.
                            substring(5, 8)) + oriDate.substring(10, 12) + "_" + oriDate.substring(13, 15) + oriDate.
                            substring(16, 18) + oriDate.substring(19, 21) + ".jpg";
                    else
                        targetFileName = "IMG_" + year + dateMap.get(oriDate.substring(5, 8)) + oriDate.substring(9, 11)
                                + "_" + oriDate.substring(12, 14) + oriDate.substring(15, 17) + oriDate.substring(18, 20) + ".jpg";
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });

        Thread pullThread = new Thread(() -> {
            try {
                while(true) if((device.adbCommand("shell ls /sdcard/DCIM/Camera/" + targetFileName).
                        equals("/sdcard/DCIM/Camera/" + targetFileName)) | Thread.currentThread().isInterrupted()) break;
                device.pullFile("/sdcard/DCIM/Camera/" + targetFileName, "\\shotImages\\IMG" + 1);
                for(int i = -1; i < (tt/pt); i++) {
                    Thread.sleep(pt);
                    if(Thread.currentThread().isInterrupted()) break;
                    device.pullFile("/sdcard/DCIM/Camera/" + targetFileName, "\\shotImages\\IMG" + (i+3));
                    if(!shootThread.isAlive()) break;
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) { }
        });

        Thread shootClock = new Thread(() -> {
            long screenOffTimeOut = 0;
            try {
                Dimension d;
                screenOffTimeOut = device.getScreenOffTimeOut();
                d = device.getScreenSize();
                device.setScreenOffTimeOut(pt+5);
                if(isNoCameraSleep) {
                    for (int i = 0; i < (tt/pt); i++) {
                        shoot = true;
                        Thread.sleep(1000L * pt - 2000);
                        device.tap(d.width / 2, d.height / 2);
                        Thread.sleep(2000);
                    }
                } else {
                    for(int i = 0; i < (tt/pt); i++) {
                        shoot = true;
                        Thread.sleep(1000L * pt);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) { }

            try {
                device.setScreenOffTimeOut(screenOffTimeOut);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + ex.getMessage());
                throw new RuntimeException(ex);
            }
        });

        Thread totalClock = new Thread(() -> {
            try {
                Thread.sleep(1000L *tt);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "计时线程出现异常！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            }
            while (true) if(!(shootThread.isAlive() && pullThread.isAlive() && shootClock.isAlive())) break;
            System.out.println(Thread.activeCount());
        });

        shootThread.start();
        pullThread.start();
        totalClock.start();
        shootClock.start();
    }
}