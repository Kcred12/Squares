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

    // Movement methods
    public void moveUp() {
        rect.setY(rect.getY() - speed);
    }

    public void moveDown() {
        rect.setY(rect.getY() + speed);
    }

    public void moveLeft() {
        rect.setX(rect.getX() - speed);
    }

    public void moveRight() {
        rect.setX(rect.getX() + speed);
    }
}
