package com.squares;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 30;
    private static final int SAFEZONE_SIZE = 150;

    private Set<KeyCode> pressedKeys = new HashSet<>();
    private List<Enemy> enemies = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Create the safe zone
        Rectangle safeZone = new Rectangle((WIDTH / 2 - SAFEZONE_SIZE / 2), (HEIGHT / 2 - SAFEZONE_SIZE / 2), 150, 150);
        safeZone.setStyle("-fx-fill: lightgreen; -fx-opacity: 0.5;");
        root.getChildren().add(safeZone);

        // Create an enemy(ies) outside the safe zone
        // Enemy enemy = new Enemy(safeZone, WIDTH, HEIGHT);
        // root.getChildren().add(enemy.getBody());
        for (int i = 0; i < 15; i++) {
            Enemy enemy = new Enemy(safeZone, WIDTH, HEIGHT, 300);
            enemies.add(enemy);
            root.getChildren().add(enemy.getRect());
        }

        // Create the player
        Player player = new Player((WIDTH / 2 - PLAYER_SIZE / 2), (HEIGHT / 2 - PLAYER_SIZE / 2), PLAYER_SIZE, PLAYER_SIZE, 450);
        Rectangle playerHitbox = player.getRect();
        root.getChildren().add(playerHitbox);


        // Add a key to pressedKeys when it is pressed
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));

        // Remove the key when the user releases it
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) lastTime = now;
                double deltaSeconds = (now - lastTime) / 1000000000.0;
                lastTime = now;

                double speedPerSecond = player.getSpeed();
                double distance = speedPerSecond * deltaSeconds;

                // Normalize diagonal movement
                double dx = 0;
                double dy = 0;

                if (pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP)) dy = -1;
                if (pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN)) dy = 1;
                if (pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT)) dx = -1;
                if (pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT)) dx = 1;

                double length = Math.sqrt(dx * dx + dy * dy);
                if (length != 0) {
                    dx /= length;
                    dy /= length;
                }

                for ( Enemy enemy : enemies) {
                    enemy.update(deltaSeconds);
                }

                player.move(dx * distance, dy * distance);
            }
        };

        gameLoop.start();


        primaryStage.setTitle("My First JavaFX Window");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
