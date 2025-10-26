package com.squares;

import java.util.Random;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Enemy {

    private Rectangle rect; // The shape that appears on screen
    private static final int SIZE = 20; // Size of the enemy square
    private double speed;
    private double vx;
    private double vy;

    public Enemy(Rectangle safeZone, int windowWidth, int windowHeight, double speed) {
        rect = new Rectangle(SIZE, SIZE, Color.RED);
        placeOutsideSafeZone(safeZone, windowWidth, windowHeight);
        this.speed = speed;
        initRandomVelocity();
    }

    // This method handles placing the enemy at a valid location.
    private void placeOutsideSafeZone(Rectangle safeZone, int windowWidth, int windowHeight) {
        Random rand = new Random();

        while (true) {
            double x = rand.nextInt(windowWidth - SIZE);
            double y = rand.nextInt(windowHeight - SIZE);

            rect.setX(x);
            rect.setY(y);

            // Only accept the placement if it does NOT collide with safe zone:
            if (!rect.getBoundsInParent().intersects(safeZone.getBoundsInParent())) {
                break;
            }
        }
    }

    private void initRandomVelocity() {
        Random rand = new Random();
        double angle = rand.nextDouble() * 2 * Math.PI; // Random angle in radians
        vx = speed * Math.cos(angle);
        vy = speed * Math.sin(angle);
    }


    // Allow the main game to access the visual rectangle
    public Rectangle getRect() {
        return rect;
    }

    public double getSpeed() {
        return speed;
    }

    public void update(double deltaTime) {
        double newX = rect.getX() + vx * deltaTime;
        double newY = rect.getY() + vy * deltaTime;

        double paneWidth = rect.getParent().getBoundsInLocal().getWidth();
        double paneHeight = rect.getParent().getBoundsInLocal().getHeight();

        // Bounce off the walls
        if (newX < 0 || newX + SIZE > paneWidth) {
            vx = -vx;
            newX = rect.getX() + vx * deltaTime; // Recalculate position after bounce
        }
        if (newY < 0 || newY + SIZE > paneHeight) {
            vy = -vy;
            newY = rect.getY() + vy * deltaTime; // Recalculate position after bounce
        }

        rect.setX(newX);
        rect.setY(newY);
    }
}
