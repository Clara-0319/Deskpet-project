package com.example.deskpet;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Move extends Thread {
    private long time;
    private final ImageView imageView;
    private final int direID;
    double x;
    double maxx;
    double width;
    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    Stage stage;
    private final EventListener listen;
    private final int petID;
    boolean exit;
    private boolean isStarted = false; // 添加标志位，用于确保线程只启动一次
    private final Lock stageLock = new ReentrantLock();
    private final Lock imageViewLock = new ReentrantLock();

    public Move(long time, ImageView imgView, int dire, Stage primaryStage, EventListener el) {
        if (imgView == null || primaryStage == null || el == null) {
            throw new IllegalArgumentException("传入Move类构造函数的参数不能为null"); // 验证参数合法性
        }
        this.time = time;
        imageView = imgView;
        direID = dire;
        stage = primaryStage;
        listen = el;
        petID = listen.petID;
        exit = false;
    }

    @Override
    public void start() {
        if (!isStarted) { // 只有当线程没启动过时才启动
            super.start();
            isStarted = true;
        }
    }

    public void run() {
        // 点击就停下
        /*使用listen.petID而不是定义一个变量int petID = listen.petID;
         *是因为在运动过程中点击“切换宠物”时实际的petID会改变，所以使用listen.petID就可以做到同步改变。
         *若下面使用listen.mainimg(petID,0)显示的就是点击“切换宠物”前的宠物，这个petID就是旧的petID。
         */
        imageViewLock.lock();
        try {
            imageView.addEventHandler(MouseEvent.MOUSE_PRESSED,
                    e -> {
                        exit = true;
                        listen.mainimg(listen.petID, 0);
                    });
        } finally {
            imageViewLock.unlock();
        }
        while (!exit) {
            // 如果petID!=listen.petID，则已“切换宠物”，此时要结束运动。
            if (petID!= listen.petID) {
                exit = true;
                return;
            }
            width = imageView.getBoundsInLocal().getMaxX();
            x = stage.getX();
            maxx = screenBounds.getMaxX();
            double speed = 15;
            if (x + speed + width >= maxx || x - speed <= 0 || time <= 0) {
                this.interrupt();
                listen.mainimg(listen.petID, 0);
                return;
            }
            stageLock.lock();
            try {
                if (direID == 0) {    // 向左走
                    stage.setX(x - speed);
                } else if (direID == 1) {    // 向右走
                    stage.setX(x + speed);
                }
            } finally {
                stageLock.unlock();
            }
            time -= 300;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // 设置线程结束标志位
                exit = true;
                imageViewLock.lock();
                try {
                    imageView.removeEventHandler(MouseEvent.MOUSE_PRESSED,
                            _ -> {
                                exit = true;
                                listen.mainimg(listen.petID, 0);
                            });
                } finally {
                    imageViewLock.unlock();
                }
                System.err.println("线程被中断，宠物移动停止");
            }
        }
    }
}