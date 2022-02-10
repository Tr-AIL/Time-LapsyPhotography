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
        dateMap.put("Ap", "04");
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
    private final Thread shootThread;
    private boolean shoot = false;

    public AutomaticShooter(ADBDevice device) {
        this.device = device;
        shootThread = new Thread(() -> {
            int i = 1;
             do {
                while(!shoot) {}
                try {
                    device.keyEvent(ADBDevice.KeyEventNumber.CAMERA);
                    String oriDate = device.adbCommand("shell date");
                    String targetFileName;
                    String year = oriDate.substring(oriDate.length() - 4);
                    if(oriDate.startsWith("Sep", 5)) targetFileName = "IMG_" + year + dateMap.get(oriDate.
                            substring(5, 8)) + oriDate.substring(10, 12) + "_" + oriDate.substring(13, 15) + oriDate.
                            substring(16, 18) + oriDate.substring(19, 21) + ".jpg";
                    else targetFileName = "IMG_" + year + dateMap.get(oriDate.substring(5, 8)) + oriDate.substring(9, 11)
                            + "_" + oriDate.substring(12, 14) + oriDate.substring(15, 17) + oriDate.substring(18, 20) + ".jpg";
                    device.pullFile("/sdcard/DCIM/Camera/" + targetFileName, "\\shotImages\\IMG" + i);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈");
                    System.exit(1);
                }
                i++;
                shoot = false;
            } while (!Thread.currentThread().isInterrupted());
        });
        shootThread.start();
    }

    public void startWithoutCameraSleep(int pt, int tt, int fr) {
        if(pt > 5) isNoCameraSleep = true;
        start(pt, tt, fr);
    }

    public void start(int pt, int tt, int fr) {
        Thread shootClock = new Thread(() -> {
            Dimension d = null;
            long screenOffTimeOut = 0;
            try {
                screenOffTimeOut = device.getScreenOffTimeOut();
                d = device.getScreenSize();
                device.setScreenOffTimeOut(pt+5);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈");
                System.exit(1);
            }
            if (isNoCameraSleep) {
                do {
                    shoot = true;
                    try {
                        Thread.sleep(1000L * pt - 2000);
                        device.tap(d.width / 2, d.height / 2);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈");
                        System.exit(1);
                    }
                } while (!Thread.currentThread().isInterrupted());
            } else {
                do {
                    shoot = true;
                    try {
                        Thread.sleep(1000L * pt);
                    } catch (InterruptedException e) {
                        break;
                    }
                } while (!Thread.currentThread().isInterrupted());
            }
            try {
                device.setScreenOffTimeOut(screenOffTimeOut);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈");
                System.exit(1);
            }
        });

        Thread totalClock = new Thread(() -> {
            try {
                Thread.sleep(1000L *tt);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "计时线程出现异常！请联系开发者提交反馈");
                System.exit(1);
            }
            shootClock.interrupt();
            shootThread.interrupt();
        });

        totalClock.start();
        shootClock.start();
    }
}
