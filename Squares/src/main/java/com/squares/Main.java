package com.squares;

import java.util.HashSet;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private Set<KeyCode> pressedKeys = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);


        Player player = new Player(50, 50, 30, 30, 300);
        Rectangle r = player.getRect();
        root.getChildren().add(r);

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

                // Check what movement keys are being held down
                player.move(dx * distance, dy * distance);
            }
        };

        gameLoop.start();


        primaryStage.setTitle("My First JavaFX Window");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
