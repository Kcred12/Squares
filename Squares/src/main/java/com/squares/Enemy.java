package com.squares;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.concurrent.ThreadLocalRandom;

public class Enemy {

    private final Rectangle rect;
    private final double speed;

    // Velocity in px/sec
    private double vx;
    private double vy;

    // Random shuffle timer
    private double timeUntilShuffle; // seconds
    private static final boolean ENABLE_SHUFFLE = false; // turn on if you want occasional re-rolls
    private static final double SHUFFLE_MIN = 10.0;      // used only if ENABLE_SHUFFLE = true
    private static final double SHUFFLE_MAX = 20.0;

    // Anti-stuck: low-motion watchdog
    private double prevX, prevY;
    private double stillTime = 0.0;

    // Tunables
    private static final double LOW_MOTION_DIST = 0.5;  // px
    private static final double LOW_MOTION_TIME = 0.6;  // s
    private static final double KICK_MAG = 2.0;         // px
    private static final double WALL_EPS = 0.01;        // px

    public Enemy(double x, double y, double size, double speed) {
        this.rect = new Rectangle(x, y, size, size);
        this.rect.setFill(Color.CRIMSON);
        this.speed = speed;

        setRandomDirection();
        timeUntilShuffle = randomShuffleDelay();

        prevX = x;
        prevY = y;
    }

    public Rectangle getRect() { return rect; }

    public void update(double dt, double sceneWidth, double sceneHeight) {
        // Move
        double newX = rect.getX() + vx * dt;
        double newY = rect.getY() + vy * dt;

        // Walls -> reflect & clamp
        if (newX <= 0 || newX + rect.getWidth() >= sceneWidth) {
            vx = -vx;
            newX = Math.max(WALL_EPS, Math.min(newX, sceneWidth - rect.getWidth() - WALL_EPS));
        }
        if (newY <= 0 || newY + rect.getHeight() >= sceneHeight) {
            vy = -vy;
            newY = Math.max(WALL_EPS, Math.min(newY, sceneHeight - rect.getHeight() - WALL_EPS));
        }

        rect.setX(newX);
        rect.setY(newY);

        // Corner escape (both axes near wall)
        boolean nearLeft   = rect.getX() <= WALL_EPS;
        boolean nearRight  = rect.getX() + rect.getWidth() >= sceneWidth - WALL_EPS;
        boolean nearTop    = rect.getY() <= WALL_EPS;
        boolean nearBottom = rect.getY() + rect.getHeight() >= sceneHeight - WALL_EPS;
        if ((nearLeft || nearRight) && (nearTop || nearBottom)) {
            // aim away from corner + nudge
            double ax = nearRight ? -1 : (nearLeft ? 1 : Math.signum(vx));
            double ay = nearBottom ? -1 : (nearTop ? 1 : Math.signum(vy));
            setDirection(ax, ay);
            kick(KICK_MAG);
        }

        // Optional random shuffle (disabled by default)
        if (ENABLE_SHUFFLE) {
            timeUntilShuffle -= dt;
            if (timeUntilShuffle <= 0) {
                setRandomDirection();
                timeUntilShuffle = randomShuffleDelay();
            }
        }

        // Low-motion watchdog
        double moved = Math.hypot(rect.getX() - prevX, rect.getY() - prevY);
        if (moved < LOW_MOTION_DIST) {
            stillTime += dt;
            if (stillTime >= LOW_MOTION_TIME) {
                setRandomDirection();
                kick(KICK_MAG);
                stillTime = 0;
            }
        } else {
            stillTime = 0;
        }
        prevX = rect.getX();
        prevY = rect.getY();
    }

    // Collision helpers
    public void moveBy(double mx, double my) { rect.setX(rect.getX() + mx); rect.setY(rect.getY() + my); }
    public void swapVX(Enemy other) { double t = this.vx; this.vx = other.vx; other.vx = t; }
    public void swapVY(Enemy other) { double t = this.vy; this.vy = other.vy; other.vy = t; }

    public void kick(double magnitude) {
        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        rect.setX(rect.getX() + Math.cos(angle) * magnitude);
        rect.setY(rect.getY() + Math.sin(angle) * magnitude);
    }

    // Accessors
    public double getX() { return rect.getX(); }
    public double getY() { return rect.getY(); }
    public double getW() { return rect.getWidth(); }
    public double getH() { return rect.getHeight(); }

    // Internals
    private void setRandomDirection() {
        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;

        // Avoid purely axis-aligned
        if (Math.abs(vx) < 0.15 * speed) vx = Math.copySign(0.2 * speed, (vx == 0 ? 1 : vx));
        if (Math.abs(vy) < 0.15 * speed) vy = Math.copySign(0.2 * speed, (vy == 0 ? 1 : vy));
    }

    private void setDirection(double dx, double dy) {
        double len = Math.hypot(dx, dy);
        if (len == 0) { setRandomDirection(); return; }
        vx = (dx / len) * speed;
        vy = (dy / len) * speed;
    }

    private double randomShuffleDelay() {
        return ThreadLocalRandom.current().nextDouble(SHUFFLE_MIN, SHUFFLE_MAX);
    }
}
