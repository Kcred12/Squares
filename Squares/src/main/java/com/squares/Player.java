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

    // Movement method
    public void move(double dx, double dy) {
        double newX = rect.getX() + dx;
        double newY = rect.getY() + dy;

        double paneWidth = rect.getParent().getBoundsInLocal().getWidth();
        double paneHeight = rect.getParent().getBoundsInLocal().getHeight();

        // Ensure the player stays within bounds
        if (newX < 0) newX = 0;
        if (newX + rect.getWidth() > paneWidth) newX = paneWidth - rect.getWidth();

        if (newY < 0) newY = 0;
        if (newY + rect.getHeight() > paneHeight) newY = paneHeight - rect.getHeight();

        rect.setX(newX);
        rect.setY(newY);
    }
}
