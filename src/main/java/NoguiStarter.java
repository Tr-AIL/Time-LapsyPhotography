import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Scanner;

public class NoguiStarter {
    public static void main(String[] args) {
        ADBDevice[] devices = ADBDevice.getADBDevices();
        if(devices.length == 0) {
            System.out.println("请连接ADB设备！");
            System.exit(0);
        }
        Scanner in = new Scanner(System.in);
        ADBDevice targetDevice;
        int periodTime;
        int totalTime;
        int frameRate;
        boolean noCameraSleep;
        if(devices.length != 1) {
            for (int i = 0; i < devices.length; i++) System.out.println(i + ":" + devices[i]);
            System.out.println("存在多个ADB设备，请选择你要连接的设备（输入序号并回车）：");
            int i;
            while(true) {
                try{
                    i = in.nextInt();
                    if(i < devices.length) {
                        targetDevice = devices[i];
                        break;
                    }
                    System.out.println("数字不在范围内！请重新输入：");
                } catch (InputMismatchException e) {
                    System.out.println("输入类型错误！请重新输入：");
                    in.nextLine();
                }
            }
        }
        else targetDevice = devices[0];
        AutomaticShooter shooter = new AutomaticShooter(targetDevice);

        System.out.println("请输入拍摄间隔（单位：秒，不要小于快门时间）：");
        while(true) {
            try{
                periodTime = in.nextInt();
                break;
            } catch (InputMismatchException e) {
                System.out.println("输入类型错误！请重新输入：");
                in.nextLine();
            }
        }

        System.out.println("请输入总拍摄时间（单位：秒，不要小于拍摄间隔）：");
        while(true) {
            try{
                totalTime = in.nextInt();
                break;
            } catch (InputMismatchException e) {
                System.out.println("输入类型错误！请重新输入：");
                in.nextLine();
            }
        }

        System.out.println("请输入输出视频帧率（fps）：");
        while(true) {
            try{
                frameRate = in.nextInt();
                break;
            } catch (InputMismatchException e) {
                System.out.println("输入类型错误！请重新输入：");
                in.nextLine();
            }
        }

        System.out.println("部分手机在一定时间无操作后会使相机休眠，是否在相机休眠时（拍摄前2秒）唤醒？（y/n）");
        while(true) {
            String s = in.nextLine();
            if(Objects.equals(s, "y")) {
                noCameraSleep = true;
                break;
            }
            if(Objects.equals(s, "n")) {
                noCameraSleep = false;
                break;
            }
            else System.out.println("请重新输入！");
        }

        if(noCameraSleep) shooter.startWithoutCameraSleep(periodTime, totalTime, frameRate);
        else shooter.start(periodTime, totalTime, frameRate);
    }
}
