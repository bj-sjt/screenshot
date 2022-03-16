package com.itao.screenshot;

import com.itao.screenshot.util.ResourceUtil;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShotStage {
    private double startX;
    private double startY;
    private double endX;
    private double endY;
    private final Config config;
    private final Stage primaryStage;
    private final Stage stage;

    private HBox hBox;
    private HBox top;
    private HBox bottom;
    private HBox left;
    private HBox right;
    private boolean flag = false;

    public ShotStage(Stage stage, Config config) {
        this.primaryStage = stage;
        this.stage = new Stage();
        this.config = config;
        init();
    }

    private void init() {
        AnchorPane root = new AnchorPane();
        BorderStroke stroke = new BorderStroke(
                Color.BLUE,
                BorderStrokeStyle.SOLID,
                null,
                new BorderWidths(3)
        );
        Border border = new Border(stroke);
        root.setBorder(border);
        Scene sc = new Scene(root);
        root.setStyle("-fx-background-color: #b5b5b501");
        sc.setFill(Color.valueOf("#ffff5500"));
        stage.setScene(sc);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint(""); // 设置全屏时的提示信息
        stage.show();
        sc.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
                primaryStage.setIconified(false);
            }
        });
        getScreenShot(root);
    }

    /**
     * 获取截图的坐标和宽高
     */
    private void getScreenShot(AnchorPane root) {
        pressedEvent(root);
        // 检测拖动事件
        root.setOnDragDetected(e -> root.startFullDrag()); // 使用此节点作为手势源启动完整的按下-拖动-释放手势
        dragEvent(root);
        exitedEvent(root);
    }

    /**
     * 拖动退出事件
     */
    private void exitedEvent(AnchorPane root) {
        root.setOnMouseDragExited(e -> {
            endX = e.getSceneX();
            endY = e.getSceneY();
            HBox hBox = new HBox();
            Button cut = new Button();
            cut.setPrefWidth(50);
            ImageView cutImage = new ImageView(ResourceUtil.getExternal("/cut.png"));
            cutImage.setPreserveRatio(false);
            cutImage.setFitWidth(30);
            cutImage.setFitHeight(20);
            cut.setGraphic(cutImage);
            cut.setTooltip(new Tooltip("复制到剪切板"));
            Button save = new Button();
            save.setPrefWidth(50);
            ImageView saveImage = new ImageView(ResourceUtil.getExternal("/save.png"));
            saveImage.setPreserveRatio(false);
            saveImage.setFitWidth(30);
            saveImage.setFitHeight(20);
            save.setGraphic(saveImage);
            save.setTooltip(new Tooltip("保存"));
            hBox.getChildren().addAll(save, cut);
            root.getChildren().add(hBox);
            AnchorPane.setTopAnchor(hBox, e.getScreenY());
            AnchorPane.setLeftAnchor(hBox, e.getSceneX() - cut.getPrefWidth() - save.getPrefWidth());
            root.setStyle("-fx-background-color: #b5b5b500");
            cut.setOnAction(event -> {
                clipboard();
            });
            save.setOnAction(event -> {
                save();
            });
        });
    }

    /**
     * 拖动事件
     */
    private void dragEvent(AnchorPane root) {
        root.setOnMouseDragOver(e -> {
            double width = e.getSceneX() - startX;
            double height = e.getScreenY() - startY;
            System.out.println("width: " + width + " - height: " + height);
            Label label = new Label("宽度：" + width + "  " + "高度：" + height);
            label.setTextFill(Color.valueOf("#ffffff"));
            label.setStyle("-fx-background-color: #000000");
            label.setPrefHeight(20);
            root.getChildren().add(label);
            AnchorPane.setTopAnchor(label, startY - label.getPrefHeight());
            AnchorPane.setLeftAnchor(label, startX);
            hBox.setPrefWidth(width);
            hBox.setPrefHeight(height);
        });
    }

    /**
     * 鼠标按下事件
     */
    private void pressedEvent(AnchorPane root) {
        root.setOnMousePressed(e -> {
            if (flag) {
                stage.close();
            }
            flag = true;
            root.setBorder(null);
            root.getChildren().clear();
            Node node = createLayout();
            root.getChildren().add(node);
            startX = e.getSceneX();
            startY = e.getSceneY();
            System.out.println("startX: " + startX + " - startY: " + startY);
            top.setPrefHeight(startY);
            top.setPrefWidth(stage.getWidth());
            left.setPrefWidth(startX);
            left.prefHeightProperty().bind(hBox.heightProperty());
            right.prefHeightProperty().bind(hBox.heightProperty());
            right.prefWidthProperty().bind(stage.widthProperty().subtract(left.getPrefWidth()).subtract(hBox.getPrefWidth()));
            bottom.setPrefWidth(stage.getWidth());
            bottom.prefHeightProperty().bind(stage.widthProperty().subtract(top.getPrefHeight()).subtract(hBox.getPrefWidth()));
        });
    }

    private Node createLayout(){
        BorderPane bp = new BorderPane();
        top = new HBox();
        top.setStyle("-fx-background-color: " + config.getMaskColor() + "30");
        bottom = new HBox();
        bottom.setStyle("-fx-background-color: " + config.getMaskColor() + "30");
        left = new HBox();
        left.setStyle("-fx-background-color: " + config.getMaskColor() + "30");
        right = new HBox();
        right.setStyle("-fx-background-color: " + config.getMaskColor() + "30");
        hBox = new HBox();
        bp.setTop(top);
        bp.setBottom(bottom);
        bp.setLeft(left);
        bp.setRight(right);
        bp.setCenter(hBox);
        hBox.setBackground(null);
        BorderStroke stroke = new BorderStroke(
                Color.RED,
                BorderStrokeStyle.SOLID,
                null,
                new BorderWidths(Double.parseDouble(config.getBorderWidth()))
        );
        Border border = new Border(stroke);
        hBox.setBorder(border);
        return bp;
    }

    /**
     * 截图
     */
    private BufferedImage screenshot() throws AWTException {
        stage.close();
        double w = endX - startX;
        double h = endY - startY;
        Robot robot = new Robot();
        Rectangle rectangle = new Rectangle((int) startX, (int) startY, (int) w, (int) h);
        return robot.createScreenCapture(rectangle);
    }

    /**
     * 手动保存截图
     */
    private void save() {
        try {
            BufferedImage bufferedImage = screenshot();
            Path path = Paths.get(config.getSavePath());
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            String dir = path.getParent().toFile().getAbsolutePath();
            File file = new File(config.getSavePath());
            String fileName = file.getName();
            int start = fileName.indexOf("$");
            int end = fileName.lastIndexOf("$");
            if (start != -1 && start == end){
                // ImageIO.write(bufferedImage,"png", file);
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(new File(dir));
                fc.setTitle("保存图片");
                fc.setInitialFileName(fileName);
                File saveFile = fc.showSaveDialog(stage);
                if (saveFile != null) {
                    ImageIO.write(bufferedImage,"png", saveFile);
                }

            }
            if (start != -1 && start != end) {
                String format = fileName.substring(start + 1, end);
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                String date = sdf.format(new Date());
                String newName = fileName.substring(0, start) + date + fileName.substring(end + 1);
                // ImageIO.write(bufferedImage,"png", new File(dir + "/" + newName));
                FileChooser fc = new FileChooser();
                fc.setInitialDirectory(new File(dir));
                fc.setTitle("保存图片");
                fc.setInitialFileName(newName);
                System.out.println(newName);
                File saveFile = fc.showSaveDialog(stage);
                if (saveFile != null) {
                    ImageIO.write(bufferedImage,"png", new File(dir + "/" + newName));
                }
            }
            primaryStage.setIconified(false);
        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 将截图复制到剪切板
     */
    private void clipboard() {
        try {
            BufferedImage bufferedImage = screenshot();
            if (config.getAutoSave()) {
                Path path = Paths.get(config.getSavePath());
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                String dir = path.getParent().toFile().getAbsolutePath();
                File file = new File(config.getSavePath());
                String fileName = file.getName();
                int start = fileName.indexOf("$");
                int end = fileName.lastIndexOf("$");
                if (start != -1 && start == end){
                    ImageIO.write(bufferedImage,"png", file);
                }
                if (start != -1 && start != end) {
                    String format = fileName.substring(start + 1, end);
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    String date = sdf.format(new Date());
                    String newName = fileName.substring(0, start) + date + fileName.substring(end + 1);
                    ImageIO.write(bufferedImage,"png", new File(dir + "/" + newName));
                }
            }
            WritableImage writableImage = SwingFXUtils.toFXImage(bufferedImage, null);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putImage(writableImage);
            clipboard.setContent(content);
            primaryStage.setIconified(false);
        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
