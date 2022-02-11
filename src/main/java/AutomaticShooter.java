import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class AutomaticShooter {
    private final ADBDevice device;
    private boolean isNoCameraSleep = false;
    private boolean shoot = false;

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

         /*
        Thread pullThread = new Thread(() -> {
            try {
                while (!pull | Thread.currentThread().isInterrupted()) Thread.sleep(50);
                Thread.sleep(st + 1000);
                device.pullFile("/sdcard/DCIM/Camera/" + targetFileName, "shotImages\\IMG" + 1 + ".jpg");
                for(int i = 1; i < (tt/pt); i++) {
                    Thread.sleep(1000L * pt);
                    if(Thread.currentThread().isInterrupted()) break;
                    device.pullFile("/sdcard/DCIM/Camera/" + targetFileName, "shotImages\\IMG" + (i + 1));
                    if(!shootThread.isAlive()) break;
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) { }
        });*/

        Thread shootClockThread = new Thread(() -> {
            long screenOffTimeOut;
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
                device.setScreenOffTimeOut(screenOffTimeOut);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "发生IO错误！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            } catch (InterruptedException ignored) { }
        });

        Thread totalTimeClockThread = new Thread(() -> {
            try {
                Thread.sleep(1000L * tt);
                while (shootThread.isAlive() && shootClockThread.isAlive()) Thread.sleep(100);
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "计时线程出现异常！请联系开发者提交反馈" + e.getMessage());
                throw new RuntimeException(e);
            }
            System.out.println(Thread.activeCount());
        });

        shootThread.start();
        //pullThread.start();
        totalTimeClockThread.start();
        shootClockThread.start();
    }
}