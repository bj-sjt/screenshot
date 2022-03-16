package com.itao.screenshot;

import com.itao.screenshot.util.ResourceUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;

public class App extends Application {

    private boolean isOpen;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false); // 为 false时 即使primaryStage.close() 程序也不会退出
        primaryStage.hide();

        systemTray(primaryStage);

    }

    /**
     * 制作系统托盘
     */
    private void systemTray(Stage primaryStage) {
        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(ResourceUtil.getPath("/icon.png"));
        PopupMenu popupMenu = new PopupMenu();
        MenuItem openItem = new MenuItem("打开设置");
        MenuItem closeItem = new MenuItem("关闭");
        popupMenu.add(openItem);
        popupMenu.add(closeItem);
        TrayIcon trayIcon = new TrayIcon(image, "screenshot", popupMenu);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }

        openItem.addActionListener(event -> {
            if (!isOpen){
                isOpen = true;
                Platform.runLater(() -> new SetupStage(primaryStage).show());
            }
        });

        closeItem.addActionListener(event -> {
            Platform.runLater(() -> {
                Platform.setImplicitExit(true);
                primaryStage.close();
            });
            systemTray.remove(trayIcon);
        });
    }
}