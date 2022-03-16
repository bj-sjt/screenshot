package com.itao.screenshot;

import com.alibaba.fastjson.JSONObject;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;

public class SetupStage {

    private static final String BORDER_WIDTH ="borderWidth";
    private static final String FILE_NAME = "fileName";
    private static final String SAVE_PATH = "savePath";
    private final Stage stage;
    private Config config;

    public SetupStage(Stage stage) {
        this.stage = stage;
        File file = new File(Config.CONFIG_PATH);
        if (file.exists()) {
            try (
                    FileReader fr = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fr)
            ) {
                StringBuilder sb = new StringBuilder();
                bufferedReader.lines().forEach(sb::append);
                config = JSONObject.parseObject(sb.toString(), Config.class);
            } catch (IOException e) {
                config = Config.INSTANCE;
            }
        } else {
            config = Config.INSTANCE;
        }
        init();
    }

    private void init() {
        VBox root = new VBox(10);
        VBox display = display();
        VBox save = save();
        root.getChildren().addAll(display, save);
        Scene scene = new Scene(root);
        // 设置快捷键 Ctrl + Alt + C
        KeyCodeCombination kc = new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.ALT_DOWN);
        scene.getAccelerators().put(kc, () -> screenshot(stage));
        stage.setScene(scene);
        stage.setWidth(473);
    }

    /**
     * 截图时遮罩颜色和边框设置
     */
    private VBox display() {
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        HBox border = new HBox(10);
        border.setAlignment(Pos.CENTER_LEFT);
        Label bk = new Label("边框宽度");
        TextField borderWidth = new TextField(config.getBorderWidth());
        borderWidth.setId(BORDER_WIDTH);
        addListener(borderWidth);
        borderWidth.setPrefWidth(40);
        Label xs = new Label("像素");
        border.getChildren().addAll(bk, borderWidth, xs);

        HBox mask = new HBox(10);
        mask.setAlignment(Pos.CENTER_LEFT);
        Label zz = new Label("遮罩颜色");
        ColorPicker cp = new ColorPicker(Color.valueOf(config.getMaskColor()));
        cp.setStyle("-fx-color-label-visible: false");
        addListener(cp);
        mask.getChildren().addAll(zz, cp);
        vBox.getChildren().addAll(border, mask);
        return vBox;
    }

    /**
     * 文件名和输出路径设置
     */
    private VBox save() {
        VBox vBox = new VBox(10);
        HBox fileNameBox = new HBox(10);
        fileNameBox.setAlignment(Pos.CENTER_LEFT);
        Label fileNameLabel = new Label("文件名");
        TextField fileName = new TextField(config.getFileName());
        fileName.setId(FILE_NAME);
        addListener(fileName);
        fileName.setPrefWidth(400);
        fileNameBox.getChildren().addAll(fileNameLabel, fileName);

        HBox saveBox = new HBox(10);
        saveBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox cb = new CheckBox();
        cb.setSelected(config.getAutoSave());
        Label save = new Label("自动保存");
        saveBox.getChildren().addAll(cb, save);

        HBox pathBox = new HBox(10);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        Label path = new Label("路径");
        TextField savePath = new TextField(config.getSavePath());
        savePath.setId(SAVE_PATH);
        addListener(savePath);
        savePath.setPrefWidth(410);
        pathBox.getChildren().addAll(path, savePath);

        HBox fileBox = new HBox(10);
        Button openBtn = new Button("打开文件夹");
        addListener(openBtn, savePath);
        Button chooseBtn = new Button("选择文件夹");
        addListener(chooseBtn, savePath);
        fileBox.getChildren().addAll(openBtn, chooseBtn);
        fileBox.setAlignment(Pos.CENTER_RIGHT);
        if (!config.getAutoSave()) {
            savePath.setDisable(true);
            openBtn.setDisable(true);
            chooseBtn.setDisable(true);
        }
        vBox.getChildren().addAll(fileNameBox, saveBox, pathBox, fileBox);
        addListener(cb, savePath, openBtn, chooseBtn);
        return vBox;
    }

    private void screenshot(Stage stage){
        stage.setIconified(true);
        new ShotStage(stage, config);
    }


    /**
     * 给输入框添加监听 (边框像素、 文件名、保存路径)
     */
    private void addListener(TextField tf) {
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!"".equals(newValue)) {
                if (FILE_NAME.equals(tf.getId())){
                    config.setFileName(newValue);
                } else if(BORDER_WIDTH.equals(tf.getId()) && newValue.matches("^[0-9]*$")){
                    config.setBorderWidth(newValue);
                } else if(SAVE_PATH.equals(tf.getId())) {
                    config.setSavePath(newValue);
                }
                saveConfig();
            }
        });
    }

    /**
     * 给按钮添加监听(打开文件夹、选择文件夹)
     */
    private void addListener(Button button, TextField savePath) {
        String text = button.getText();
        if ("打开文件夹".equals(text)) {
            button.setOnAction(e -> {
                try {
                    String fileDir = config.getSavePath().substring(0, config.getSavePath().lastIndexOf("/"));
                    Desktop.getDesktop().open(new File(fileDir));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        } else if ("选择文件夹".equals(text)) {
            button.setOnAction(e -> {
                String fileDir = config.getSavePath().substring(0, config.getSavePath().lastIndexOf("/"));
                DirectoryChooser dc = new DirectoryChooser ();
                dc.setTitle("选择文件夹");
                dc.setInitialDirectory(new File(fileDir));
                File file = dc.showDialog(stage);
                if (file != null) {
                    savePath.setText(file.getAbsolutePath().replace(File.separator, "/") + "/" +config.getFileName());
                }
            });
        }
    }

    /**
     * 给多选框天机按钮(自动保存)
     */
    private void addListener(CheckBox cb, TextField savePath, Button openBtn, Button chooseBtn) {
        cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
            config.setAutoSave(newValue);
            saveConfig();
            if (!newValue) {
                savePath.setDisable(true);
                openBtn.setDisable(true);
                chooseBtn.setDisable(true);
            } else {
                savePath.setDisable(false);
                openBtn.setDisable(false);
                chooseBtn.setDisable(false);
            }
        });
    }

    /**
     * 给颜色选择器添加监听
     */
    private void addListener(ColorPicker cp) {
        cp.valueProperty().addListener((observable, oldValue, newValue) -> {
            config.setMaskColor(newValue.toString());
            saveConfig();
        });
    }

    /**
     * 保存配置
     */
    private void saveConfig() {
        String jsonString = JSONObject.toJSONString(config, true);
        File file = new File(Config.CONFIG_PATH);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        stage.show();
    }

}
