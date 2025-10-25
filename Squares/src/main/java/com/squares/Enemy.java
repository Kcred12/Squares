package com.squares;

import java.util.Random;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Enemy {

    private Rectangle body; // The shape that appears on screen
    private static final int SIZE = 20; // Size of the enemy square

    public Enemy(Rectangle safeZone, int windowWidth, int windowHeight) {
        body = new Rectangle(SIZE, SIZE, Color.RED);
        placeOutsideSafeZone(safeZone, windowWidth, windowHeight);
    }

    // This method handles placing the enemy at a valid location.
    private void placeOutsideSafeZone(Rectangle safeZone, int windowWidth, int windowHeight) {
        Random rand = new Random();

        while (true) {
            double x = rand.nextInt(windowWidth - SIZE);
            double y = rand.nextInt(windowHeight - SIZE);

            body.setX(x);
            body.setY(y);

            // Only accept the placement if it does NOT collide with safe zone:
            if (!body.getBoundsInParent().intersects(safeZone.getBoundsInParent())) {
                break;
            }
        }
    }

    // Allow the main game to access the visual rectangle
    public Rectangle getBody() {
        return body;
    }
}
