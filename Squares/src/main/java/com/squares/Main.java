package com.squares;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();

        Player player = new Player(50, 50, 30, 30, 5);
        Rectangle r = player.getRect();
        root.getChildren().add(r);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("My First JavaFX Window");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
