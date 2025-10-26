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
        rect.setFill(Color.web("#FF007C"));
        rect.setStroke(Color.web("#FF00C8"));
        rect.setStrokeWidth(2); 
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

    public void update(double dt, double sceneWidth, double sceneHeight) {
            double newX = rect.getX() + vx * dt;
            double newY = rect.getY() + vy * dt;

            if (newX + rect.getWidth() > sceneWidth) {
                double overflow = newX + rect.getWidth() - sceneWidth;  // how far past the wall
                newX = sceneWidth - rect.getWidth() - overflow;        // reflect inside
                vx *= -1;                                              // reverse velocity
            }

            if (newX < 0) {
                double overflow = -newX;        // how far past the wall
                newX = 0 + overflow;            // reflect inside
                vx *= -1;                        // reverse velocity
            }

            if (newY + rect.getHeight() > sceneHeight) {
                double overflow = newY + rect.getHeight() - sceneHeight;
                newY = sceneHeight - rect.getHeight() - overflow;
                vy *= -1;
            }

            if (newY < 0) {
                double overflow = -newY;
                newY = 0 + overflow;
                vy *= -1;
            }

            rect.setX(newX);
            rect.setY(newY);
        }

}
