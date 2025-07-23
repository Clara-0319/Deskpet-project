package com.example.deskpet;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;


public class UI implements Runnable {
    private ImageView imageView;
    private int petID;
    private final EventListener listen;
    private VBox messageBox;
    private CheckboxMenuItem itemWalkable= new CheckboxMenuItem("自由行走");
    private CheckboxMenuItem autoPlay = new CheckboxMenuItem("自娱自乐");
    private CheckboxMenuItem itemSay=new CheckboxMenuItem("碎碎念");
    private final Stage primaryStage;
    Thread thread;
    double x;
    String[] lxhStrings= {
            "好无聊。。。",
            "陪我玩会儿吧~",
            "《罗小黑战记》怎么还没更新",
            "想师父了",
            "不就是拿了颗珠子嘛，至于把我打回猫形嘛"
    };
    String[] biuStrings = {
            "想吃东西。。",
            "biu~",
            "揉揉小肚几",
            "比丢这么可爱，怎么可以欺负比丢"
    };
    public UI(ImageView view, int pet, EventListener el, Stage s) {
        imageView = view;
        petID = pet;
        listen = el;
        primaryStage = s;
    }

    //添加系统托盘
    public void setTray(Stage stage) {

        SystemTray tray = SystemTray.getSystemTray();
        BufferedImage image = null; // 托盘图标，先初始化为null
        try {
            // 为托盘添加一个右键弹出菜单
            PopupMenu popMenu = new PopupMenu();
            popMenu.setFont(new Font("微软雅黑", Font.PLAIN, 18));

            MenuItem itemSwitch = new MenuItem("切换宠物");
            itemSwitch.addActionListener(e -> switchPet());

            itemWalkable = new CheckboxMenuItem("自由行走");
            autoPlay = new CheckboxMenuItem("自娱自乐");
            itemSay = new CheckboxMenuItem("碎碎念");

            // 逻辑控制各个菜单项的状态
            itemWalkable.addItemListener(il -> {
                if (itemWalkable.getState()) {
                    autoPlay.setEnabled(false);
                    itemSay.setEnabled(false);
                    //调试
                    System.out.println("itemWalkable被选中，autoPlay和itemSay已设为不可用");
                } else {
                    autoPlay.setEnabled(true);
                    itemSay.setEnabled(true);
                    //调试
                    System.out.println("itemWalkable被选中，autoPlay和itemSay已设为不可用");
                }
            });
            autoPlay.addItemListener(il -> {
                if (autoPlay.getState()) {
                    itemWalkable.setEnabled(false);
                    itemSay.setEnabled(false);
                } else {
                    itemWalkable.setEnabled(true);
                    itemSay.setEnabled(true);
                }
            });
            itemSay.addItemListener(il -> {
                if (itemSay.getState()) {
                    itemWalkable.setEnabled(false);
                    autoPlay.setEnabled(false);
                } else {
                    itemWalkable.setEnabled(true);
                    autoPlay.setEnabled(true);
                }
            });

            MenuItem itemShow = new MenuItem("显示");
            itemShow.addActionListener(e -> Platform.runLater(stage::show));

            MenuItem itemHide = new MenuItem("隐藏");
            itemHide.addActionListener(e -> {
                Platform.setImplicitExit(false);
                Platform.runLater(stage::hide);
            });

            MenuItem itemExit = new MenuItem("退出");
            itemExit.addActionListener(e -> end());

            // 将菜单项添加到弹出菜单
            popMenu.add(itemSwitch);
            popMenu.addSeparator();
            popMenu.add(itemWalkable);
            popMenu.add(autoPlay);
            popMenu.add(itemSay);
            popMenu.addSeparator();
            popMenu.add(itemShow);
            popMenu.add(itemHide);
            popMenu.add(itemExit);

            //调试菜单
            // 在添加完所有菜单项后添加以下调试代码
            System.out.println("PopupMenu 中的菜单项数量: " + popMenu.getItemCount());
            for (int i = 0; i < popMenu.getItemCount(); i++) {
                MenuItem item = popMenu.getItem(i);
                System.out.println("菜单项 " + i + " 的文本: " + item.getLabel());
            }

            // 设置托盘图标
            image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("icon.png")));
            TrayIcon trayIcon = new TrayIcon(image, "桌面宠物", popMenu);
            trayIcon.setToolTip("桌面宠物");
            trayIcon.setImageAutoSize(true); // 自动调整图片大小
            tray.add(trayIcon);
        } catch (IOException e) {
            // 针对图标文件读取异常，给出更友好的提示信息
            JOptionPane.showMessageDialog(null, "读取托盘图标文件失败，请检查icon.png文件是否存在及格式是否正确，错误信息：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } catch (AWTException e) {
            // 针对添加托盘图标到系统托盘异常，给出相应提示
            JOptionPane.showMessageDialog(null, "将托盘图标添加到系统托盘时出现异常，可能是系统不支持托盘功能，错误信息：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            // 如果图标读取失败，避免后续使用未初始化的image对象导致空指针异常等问题
            if (image == null) {
                // 可以考虑在这里进行一些合理的默认处理，比如使用一个内置的默认图标（如果有的话），或者直接提示用户无法正常显示托盘图标等操作
                // 以下只是示例，假设存在一个默认图标default_icon.png
                try {
                    image = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("default_icon.png")));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "无法加载默认托盘图标，应用可能无法正常显示托盘图标，错误信息：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    //切换宠物
    private void switchPet() {
        imageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, listen);//移除原宠物的事件
        //切换宠物ID
        if(petID == 0) {
            petID = 1; //切换成比丢
            imageView.setFitHeight(150);
            imageView.setFitWidth(150);
        }
        else {
            petID = 0; //切换成罗小黑
            imageView.setFitHeight(200);
            imageView.setFitWidth(200);
        }
//		listen = new EventListener(imageView,petID);
        /*
         *修改listen.petID是为了修复bug: 在运行三个功能之一时点击切换宠物，图片会切换，但宠物动作不会停止
         *且动作完成后恢复的主图还是上一个宠物，直到下一个动作执行才变正常。
         *原因在于那三个功能调用listen.loadimg()时传递的是旧petID。
         */
        listen.petID = petID;
        listen.mainimg(petID,0);//切换至该宠物的主图（图片编号为0）
        //因为listen更新了，所以要重新添加点击事件
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, listen);
    }
    //退出程序时展示动画
    void end() {
        listen.mainimg(petID,99);//播放宠物的告别动画————编号为99的图片
        double time;
        //罗小黑的告别动画1.5秒，比丢的3秒
        if(petID == 0) time = 1.5;
        else time = 3;
        //要用Platform.runLater，不然会报错Not on FX application thread;
        Platform.runLater(() ->setMsg("再见~"));
        //动画结束后执行退出
        new Timeline(new KeyFrame(
                Duration.seconds(time),
                ae ->System.exit(0)))
                .play();
    }
    //添加聊天气泡
    public void addMessageBox(String message) {
        Label bubble = new Label(message);
        //设置气泡的宽度。如果没有这句，就会根据内容多少来自适应宽度
        bubble.setPrefWidth(100);
        bubble.setWrapText(true);//自动换行
        bubble.setStyle("-fx-background-color: DarkTurquoise; -fx-background-radius: 8px;");
        bubble.setPadding(new Insets(7));//标签的内边距的宽度
        bubble.setFont(new javafx.scene.text.Font(14));
        Polygon triangle = new Polygon(
                0.0, 0.0,
                8.0, 10.0,
                16.0, 0.0);//分别设置三角形三个顶点的X和Y
        triangle.setFill(Color.DARKTURQUOISE);
        messageBox = new VBox();
//      VBox.setMargin(triangle, new Insets(0, 50, 0, 0));//设置三角形的位置，默认居中
        messageBox.getChildren().addAll(bubble, triangle);
        messageBox.setAlignment(Pos.BOTTOM_CENTER);
        messageBox.setStyle("-fx-background:transparent;");
        //设置相对于父容器的位置
        messageBox.setLayoutX(0);
        messageBox.setLayoutY(0);
        messageBox.setVisible(true);
        //设置气泡的显示时间
        new Timeline(new KeyFrame(
                Duration.seconds(8),
                ae ->{messageBox.setVisible(false);}))
                .play();
    }

    //用多线程来实现 经过随机时间间隔执行“自动行走”“自娱自乐”“碎碎念”的功能
    public void run() {
        while(true) {
            Random rand = new Random();
            //随机发生自动事件，以下设置间隔为9~24秒。要注意这个时间间隔包含了动画播放的时间
            long time = (rand.nextInt(15)+10)*1000;
            System.out.println("Waiting time:"+time);
            if (itemWalkable != null && itemWalkable.getState() && listen.gifID == 0) {
                walk();
            }
            else if(autoPlay != null && autoPlay.getState() && listen.gifID == 0) {
                play();
            }
            else if(itemSay != null&&itemSay.getState() && listen.gifID == 0) {
                //随机选择要说的话。因为目前只有两个宠物，所以可以用三目运算符
                String str = (petID == 0) ? lxhStrings[rand.nextInt(5)]:biuStrings[rand.nextInt(4)];
                Platform.runLater(() ->setMsg(str));
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /*
     * 执行"碎碎念"的功能——在宠物上方显示对话气泡
     * 不默认开启是考虑到用户可能不想被打扰
     */
    public void setMsg(String msg) {

        Label lbl = (Label) messageBox.getChildren().get(0);
        lbl.setText(msg);
        messageBox.setVisible(true);
        //设置气泡的显示时间
        new Timeline(new KeyFrame(
                Duration.seconds(4),
                ae ->{messageBox.setVisible(false);}))
                .play();
    }

    /*
     * 执行"自行走动"的功能——在水平方向上走动
     * 不默认开启是考虑到用户可能只想宠物安静呆着
     */
    void walk(){
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        x = primaryStage.getX();//stage的左边缘坐标
        double maxx = screenBounds.getMaxX();//获取屏幕的大小
        double width = imageView.getBoundsInLocal().getWidth();//获取imageView的宽度，也可使用.getMaxX();
        Random rand = new Random();
        double speed=10;//每次移动的距离
        //如果将要到达屏幕边缘就停下
        if(x+speed+width >= maxx | x-speed<=0)
            return;
        //随机决定移动的时间，单位微秒ms
        long time = (rand.nextInt(4)+3)*1000;
        System.out.println("Walking time:"+time);
        int direID = rand.nextInt(2);//随机决定方向，0为左，1为右
        //切换至对应方向的行走图
        Image newimage;
        if(petID == 0)
            newimage = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/lxh/罗小黑w" + direID + ".gif")));
        else {
            newimage = new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("/biu/biuw" + direID + ".gif")));
        }
        imageView.setImage(newimage);
        //移动
        Move move = new Move(time, imageView, direID, primaryStage, listen);
        thread = new Thread(move);
        thread.start();
    }
    /*
     * 执行"自娱自乐"的功能——空闲时随机做动作
     * 这样就不用受部位数量的限制，也不会让宠物显得呆板
     * 不默认开启是考虑到用户可能只想宠物安静呆着
     */
    void play() {
        Random rand = new Random();
        int gifID;
        double time = 4;
        //gifID是根据图片文件夹中用途未定义的图片和已设定的动作个数来确定的
        if(petID == 0) {
            gifID = rand.nextInt(7)+5;
        }
        else
            gifID = rand.nextInt(7)+7;
        listen.loadImg(petID, gifID, time);
    }
    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public VBox getMessageBox() {
        return messageBox;
    }

    public void setMessageBox(VBox messageBox) {
        this.messageBox = messageBox;
    }
}

