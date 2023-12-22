package com.example.demo5;

import javafx.scene.control.TextInputDialog;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.Random;

interface ImageFactory {
    Image createImage(String path);
}

class FileImageFactory implements ImageFactory {
    @Override
    public Image createImage(String path) {
        try {
            return new Image(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

class RandomSingleton {
    private static final Random instance = new Random();

    private RandomSingleton() {
        // Приватный конструктор, чтобы предотвратить создание экземпляров извне.
    }

    public static Random getInstance() {
        return instance;
    }
}

interface WinningStrategy {
    boolean isWinner(int[] arr);
}

class DefaultWinningStrategy implements WinningStrategy {
    @Override
    public boolean isWinner(int[] arr) {
        return arr[0] == arr[1] && arr[0] == arr[2];
    }
}

public class SlotMachine extends Application {
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerLoss; // Добавленный объект MediaPlayer для звука проигрыша
    private double initialBalance = 0;
    private Group group = new Group();
    private Button spin = new Button("Spin");
    private Label label1 = new Label("Amount Won This Spin ");
    private Label label2 = new Label("Total Amount Won ");
    private TextField t1 = new TextField();
    private int[] arr = new int[3];
    private String[] p = {
            "C:\\Users\\user\\IdeaProjects\\demo5\\src\\main\\resources\\cherry.jpg",
            "C:\\Users\\user\\IdeaProjects\\demo5\\src\\main\\resources\\lemon.jpg",
            "C:\\Users\\user\\IdeaProjects\\demo5\\src\\main\\resources\\orange1.jpg"
    };
    private double balance = 0;
    private ImageFactory imageFactory = new FileImageFactory();
    private Random random = RandomSingleton.getInstance();
    private WinningStrategy winningStrategy = new DefaultWinningStrategy();
    private Image[] images = new Image[3];
    private ImageView[] imageViews = new ImageView[3];
    private double result = 0, total = 0;
    private int x = 170, y = 20;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Slot Machine Simulation");

        // Пользователь вводит изначальный баланс
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Initial Balance");
        dialog.setHeaderText("Enter your initial balance:");
        Optional<String> userInput = dialog.showAndWait();

        userInput.ifPresent(input -> {
            try {
                initialBalance = Double.parseDouble(input);
                balance = initialBalance;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });

        setUI();
        spin.setOnAction(e -> handleSpinButton());

        // Инициализация MediaPlayer для звука проигрыша
        Media winSound = new Media(getClass().getResource("/mixkit-payout-award-1934.wav").toString());
        Media lossSound = new Media(getClass().getResource("/mixkit-player-losing-or-failing-2042.wav").toString());
        mediaPlayer = new MediaPlayer(winSound);
        mediaPlayerLoss = new MediaPlayer(lossSound);

        Scene scene = new Scene(group, 700, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void setUI() {
        x = 170;

        try {
            // Очищаем предыдущие изображения
            group.getChildren().clear();

            for (int i = 0; i < 3; i++) {
                images[i] = createImage(p[i]);
                imageViews[i] = createImageView(images[i]);
            }

            Label amountLabel = new Label("Amount inserted: $");
            amountLabel.setFont(new Font("Times new Roman", 17));
            amountLabel.setLayoutX(100);
            amountLabel.setLayoutY(200);
            t1.setLayoutX(270);
            t1.setLayoutY(195);
            label1.setLayoutX(460);
            label1.setLayoutY(170);
            label2.setLayoutX(460);
            label2.setLayoutY(200);
            spin.setLayoutX(250);
            spin.setLayoutY(250);

            group.getChildren().addAll(amountLabel, t1, label1, label2, spin);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        group.getChildren().add(imageView);
        x += 120;
        return imageView;
    }

    private Image createImage(String path) {
        return imageFactory.createImage(path);
    }

    private void handleSpinButton() {
        double amount = 0;
        try {
            if (!t1.getText().isEmpty()) {
                amount = Double.parseDouble(t1.getText());
            }

            // Проверка, что есть достаточно средств на счету
            if (balance < 1) {
                label1.setText("Not enough funds. Game over.");
                spin.setDisable(true);  // Отключаем кнопку Spin
                mediaPlayerLoss.play();
                return;
            }

            // Списываем 1 доллар при каждом спине
            balance -= 1;

            // Generate a new combination of symbols
            arr = getArr();

            // Update the existing ImageView objects with the new images
            for (int i = 0; i < 3; i++) {
                images[i] = createImage(p[arr[i]]);
                imageViews[i].setImage(images[i]);
            }

            if (amount > 0 && winningStrategy.isWinner(arr)) {
                result = (amount * 2);
                balance += result;  // Увеличиваем баланс при выигрыше
                label1.setText("Amount Won This Spin: " + String.valueOf(result));
                label2.setText("Total Amount Won: " + String.valueOf(balance));
                mediaPlayer.play();
            } else {
                label1.setText("Amount Won This Spin: 0");  // Изменено для корректного отображения выигрыша 0 при проигрыше
            }

            // Обновляем отображение баланса
            updateBalanceLabel();

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            label1.setText("Invalid input for amount");
        }
    }

    private void updateBalanceLabel() {
        label2.setText("Balance: $" + String.valueOf(balance));
    }

    private int[] getArr() {
        Random random = RandomSingleton.getInstance();
        int[] newArr = new int[3];
        for (int i = 0; i < 3; i++) {
            newArr[i] = random.nextInt(0, 3);
        }
        return newArr;
    }
}
