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

    public void startWithoutCameraSleep(long st, int pt, int tt, int fr) {
        if(pt > 2) isNoCameraSleep = true;
        start(st, pt, tt, fr);
    }

    public void start(long st, int pt, int tt, int fr) {
         Thread shootThread = new Thread(() -> {
            for (int i = 0; i < (tt/pt); i++) {
                try {
                    while (!shoot) {
                        Thread.sleep(50);
                    }
                    System.out.println("shooting");
                    device.keyEvent(ADBDevice.KeyEventNumber.CAMERA);
                    String oriDate = device.adbCommand("shell date");
                    String year = oriDate.substring(oriDate.length() - 6);
                    if (oriDate.startsWith("Sep", 4)) targetFileName = "IMG_" + year + dateMap.get(oriDate.
                            substring(5, 8)) + oriDate.substring(9, 11) + "_" + oriDate.substring(12, 14) + oriDate.
                            substring(15, 17) + oriDate.substring(18, 20) + ".jpg";
                    else targetFileName = "IMG_" + year + dateMap.get(oriDate.substring(4, 7)) + oriDate.substring(8, 10)
                                + "_" + oriDate.substring(11, 13) + oriDate.substring(14, 16) + oriDate.substring(17, 19) + ".jpg";
                    System.out.println(targetFileName);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                    throw new RuntimeException(e);
                } catch (InterruptedException ignored) {}
                shoot = false;
            }
        });

        Thread pullThread = new Thread(() -> {
            try {
                String s;
                while (!(((s = device.adbCommand("shell ls /sdcard/DCIM/Camera/" + targetFileName)).substring(0, s.length() - 2).
                        equals("/sdcard/DCIM/Camera/" + targetFileName)) | Thread.currentThread().isInterrupted())) {
                    System.out.println(targetFileName);
                    Thread.sleep(50);
                }
                System.out.println("pulling");
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

        Thread shootClockThread = new Thread(() -> {
            long screenOffTimeOut = 0;
            try {
                screenOffTimeOut = device.getScreenOffTimeOut();
                Dimension d = device.getScreenSize();
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

        Thread totalTimeClockThread = new Thread(() -> {
            try {
                Thread.sleep(1000L *tt);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "计时线程出现异常！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            }
            while (true) if(!(shootThread.isAlive() && pullThread.isAlive() && shootClockThread.isAlive())) break;
            System.out.println(Thread.activeCount());
        });

        shootThread.start();
        pullThread.start();
        totalTimeClockThread.start();
        shootClockThread.start();
    }
}