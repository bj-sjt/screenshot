package com.itao.screenshot;

import com.itao.screenshot.util.ResourceUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    public static final String CONFIG_DIR; // 配置文件的目录
    public static final String CONFIG_PATH; // 配置文件
    public static final String IMAGE_DIR; // 截图保存的目录
    public static final Config INSTANCE;
    public static final String FILE_NAME ;
    private static final String BORDER_WIDTH;
    private static final String MASK_COLOR;
    private String borderWidth; // 截图边框像素
    private String maskColor; // 这比颜色
    private String fileName; // 截图保存的名字
    private String savePath; // 截图保存的路径
    private boolean autoSave; // 是否自动保存

    static {
        String userHome = System.getProperty("user.home").replace(File.separator,"/");
        InputStream inputStream = ResourceUtil.getResourceAsStream("/config.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FILE_NAME = (String) properties.get("fileName");
        MASK_COLOR = (String) properties.get("maskColor");
        BORDER_WIDTH = (String) properties.get("borderWidth");
        CONFIG_DIR = userHome + "/" + properties.get("configDir");
        IMAGE_DIR = userHome + "/" + properties.get("imageSave");
        CONFIG_PATH = CONFIG_DIR + "/" + properties.get("configName");
        Path configPath = Paths.get(CONFIG_DIR);
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path imagePath = Paths.get(IMAGE_DIR);
        if (!Files.exists(imagePath)) {
            try {
                Files.createDirectories(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        INSTANCE = getInstance();
    }

    private static Config getInstance(){
        Config config = new Config();
        config.setAutoSave(false);
        config.setMaskColor(MASK_COLOR);
        config.setBorderWidth(BORDER_WIDTH);
        config.setFileName(FILE_NAME);
        config.setSavePath(IMAGE_DIR + "/" + FILE_NAME);
        return config;
    }
    private Config() {}

    public String getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(String borderWidth) {
        this.borderWidth = borderWidth;
    }

    public String getMaskColor() {
        return maskColor;
    }

    public void setMaskColor(String maskColor) {
        this.maskColor = maskColor;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean getAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }
}
