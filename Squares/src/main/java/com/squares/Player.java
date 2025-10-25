package com.squares;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player {

    private Rectangle rect;
    private double speed;

    public Player(double x, double y, double width, double height, double speed) {
        rect = new Rectangle(x, y, width, height);
        rect.setFill(Color.BLUE);
        this.speed = speed;
    }

    public Rectangle getRect() {
        return rect;
    }

    public double getSpeed() {
        return speed;
    }

    // Movement method with window boundary collision
    public void move(double dx, double dy, double sceneWidth, double sceneHeight) {
        double newX = rect.getX() + dx;
        double newY = rect.getY() + dy;

        // Keep player inside window boundaries
        if (newX < 0) newX = 0;
        if (newY < 0) newY = 0;
        if (newX + rect.getWidth() > sceneWidth) newX = sceneWidth - rect.getWidth();
        if (newY + rect.getHeight() > sceneHeight) newY = sceneHeight - rect.getHeight();

        rect.setX(newX);
        rect.setY(newY);
    }
}
