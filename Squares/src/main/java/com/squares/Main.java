package com.squares;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.animation.AnimationTimer;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends Application {

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private static final int ENEMY_COUNT = 6;           // number of enemies
    private static final double PLAYER_SIZE = 30;       // player/enemy size
    private static final double PLAYER_SPEED = 300;     // px/s
    private static final double PLAYER_EXCLUSION_HALF = 120; // exclusion square half-size

    // === Dash settings ===
    private static final double DASH_SPEED = 900.0;     // dash speed (px/s)
    private static final double DASH_DURATION = 0.15;   // seconds
    private static final double DASH_COOLDOWN = 0.60;   // seconds

    // Dash state
    private double dashTimer = 0.0;
    private double dashCooldownTimer = 0.0;
    private double dashDirX = 1.0, dashDirY = 0.0;  // direction during a dash
    private double lastDirX = 1.0, lastDirY = 0.0;  // last non-zero move direction

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);
        scene.setFill(javafx.scene.paint.Color.web("#5e5e5e"));

        // Center the player
        double startX = (scene.getWidth()  - PLAYER_SIZE) / 2.0;
        double startY = (scene.getHeight() - PLAYER_SIZE) / 2.0;
        Player player = new Player(startX, startY, PLAYER_SIZE, PLAYER_SIZE, PLAYER_SPEED);
        Rectangle r = player.getRect();
        root.getChildren().add(r);

        // Exclusion zone (square) centered on the player
        double exclusionLeft   = (scene.getWidth()  / 2.0) - PLAYER_EXCLUSION_HALF;
        double exclusionTop    = (scene.getHeight() / 2.0) - PLAYER_EXCLUSION_HALF;
        double exclusionRight  = (scene.getWidth()  / 2.0) + PLAYER_EXCLUSION_HALF;
        double exclusionBottom = (scene.getHeight() / 2.0) + PLAYER_EXCLUSION_HALF;

        // Enemies (same size/speed as player), spawn outside exclusion zone
        double enemySize = PLAYER_SIZE;
        double enemySpeed = PLAYER_SPEED;
        List<Enemy> enemies = new ArrayList<>();

        for (int i = 0; i < ENEMY_COUNT; i++) {
            double x, y;
            int attempts = 0;
            while (true) {
                x = ThreadLocalRandom.current().nextDouble(0, scene.getWidth()  - enemySize);
                y = ThreadLocalRandom.current().nextDouble(0, scene.getHeight() - enemySize);

                boolean overlapsExclusion =
                        aabbOverlap(x, y, enemySize, enemySize,
                                    exclusionLeft, exclusionTop,
                                    exclusionRight - exclusionLeft, exclusionBottom - exclusionTop);

                if (!overlapsExclusion) break;
                if (++attempts > 50) break; // fail-safe
            }
            Enemy e = new Enemy(x, y, enemySize, enemySpeed);
            enemies.add(e);
            root.getChildren().add(e.getRect());
        }

        // Input
        scene.setOnKeyPressed(e -> {
            pressedKeys.add(e.getCode());

            // Start dash on SPACE (key press; not on key hold)
            if (e.getCode() == KeyCode.SPACE) {
                if (dashCooldownTimer <= 0 && dashTimer <= 0) {
                    // Use current input direction if any; else use lastDir
                    double ix = 0, iy = 0;
                    boolean up    = pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP);
                    boolean down  = pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN);
                    boolean left  = pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT);
                    boolean right = pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT);

                    ix = (right ? 1 : 0) + (left ? -1 : 0);
                    iy = (down  ? 1 : 0) + (up   ? -1 : 0);

                    double len = Math.hypot(ix, iy);
                    if (len != 0) {
                        ix /= len; iy /= len;
                        dashDirX = ix; dashDirY = iy;
                    } else {
                        // No input? dash in last move direction
                        dashDirX = lastDirX; dashDirY = lastDirY;
                    }

                    // If somehow zero, default to right
                    double dashLen = Math.hypot(dashDirX, dashDirY);
                    if (dashLen == 0) { dashDirX = 1; dashDirY = 0; }

                    dashTimer = DASH_DURATION;
                    dashCooldownTimer = DASH_COOLDOWN;
                }
            }
        });

        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));

        AnimationTimer gameLoop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Clamp to prevent tunneling or sticky frames
                if (dt > 0.05) dt = 0.05;

                // Update dash timers
                if (dashCooldownTimer > 0) dashCooldownTimer -= dt;
                if (dashTimer > 0) dashTimer -= dt;

                // Player movement direction from input
                boolean up    = pressedKeys.contains(KeyCode.W) || pressedKeys.contains(KeyCode.UP);
                boolean down  = pressedKeys.contains(KeyCode.S) || pressedKeys.contains(KeyCode.DOWN);
                boolean left  = pressedKeys.contains(KeyCode.A) || pressedKeys.contains(KeyCode.LEFT);
                boolean right = pressedKeys.contains(KeyCode.D) || pressedKeys.contains(KeyCode.RIGHT);

                double ix = (right ? 1 : 0) + (left ? -1 : 0);
                double iy = (down  ? 1 : 0) + (up   ? -1 : 0);

                double len = Math.hypot(ix, iy);
                if (len != 0) {
                    ix /= len; iy /= len;
                    // remember last non-zero direction
                    lastDirX = ix; lastDirY = iy;
                }

                // Choose movement based on dash state
                double moveX, moveY;
                if (dashTimer > 0) {
                    // Dashing: fixed dash direction & speed, ignore input
                    double dashDist = DASH_SPEED * dt;
                    moveX = dashDirX * dashDist;
                    moveY = dashDirY * dashDist;
                } else {
                    // Normal movement
                    double distance = PLAYER_SPEED * dt;
                    moveX = ix * distance;
                    moveY = iy * distance;
                }

                player.move(moveX, moveY, scene.getWidth(), scene.getHeight());

                // Update enemies
                for (Enemy e : enemies) {
                    e.update(dt, scene.getWidth(), scene.getHeight());
                    // (If you added collision flash earlier)
                    e.getRect().setOpacity(Math.min(1.0, e.getRect().getOpacity() + dt * 2));
                }

                // Enemy-Enemy collisions: iterative solver
                final int SOLVER_ITERATIONS = 6;
                final double SEP_EPS = 0.3;

                for (int iter = 0; iter < SOLVER_ITERATIONS; iter++) {
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy a = enemies.get(i);
                        for (int j = i + 1; j < enemies.size(); j++) {
                            Enemy b = enemies.get(j);
                            if (intersects(a, b)) {
                                resolveEnemyCollision(a, b, SEP_EPS);
                            }
                        }
                    }
                }
            }
        };

        primaryStage.setTitle("Squares 0.0.2");
        primaryStage.setScene(scene);
        primaryStage.show();

        root.setFocusTraversable(true);
        root.requestFocus();

        gameLoop.start();
    }

    // Simple AABB overlap (x,y are top-left; w,h are sizes)
    private boolean aabbOverlap(double ax, double ay, double aw, double ah,
                                double bx, double by, double bw, double bh) {
        return ax < bx + bw &&
               ax + aw > bx &&
               ay < by + bh &&
               ay + ah > by;
    }

    // AABB intersection for enemies
    private boolean intersects(Enemy a, Enemy b) {
        return a.getX() < b.getX() + b.getW() &&
               a.getX() + a.getW() > b.getX() &&
               a.getY() < b.getY() + b.getH() &&
               a.getY() + a.getH() > b.getY();
    }

    // Robust enemy collision resolution + (optional) brief flash
    private void resolveEnemyCollision(Enemy a, Enemy b, double sepEps) {
        double ax1 = a.getX(), ay1 = a.getY(), aw = a.getW(), ah = a.getH();
        double bx1 = b.getX(), by1 = b.getY(), bw = b.getW(), bh = b.getH();

        double ax2 = ax1 + aw, ay2 = ay1 + ah;
        double bx2 = bx1 + bw, by2 = by1 + bh;

        double overlapX = Math.min(ax2, bx2) - Math.max(ax1, bx1);
        double overlapY = Math.min(ay2, by2) - Math.max(ay1, by1);
        if (overlapX <= 0 || overlapY <= 0) return;

        if (overlapX < overlapY) {
            double half = (overlapX + sepEps) / 2.0;
            if (ax1 < bx1) { a.moveBy(-half, 0); b.moveBy(+half, 0); }
            else           { a.moveBy(+half, 0); b.moveBy(-half, 0); }
            a.swapVX(b); // swap X velocities
        } else {
            double half = (overlapY + sepEps) / 2.0;
            if (ay1 < by1) { a.moveBy(0, -half); b.moveBy(0, +half); }
            else           { a.moveBy(0, +half); b.moveBy(0, -half); }
            a.swapVY(b); // swap Y velocities
        }

        // If you kept the collision flash:
        a.getRect().setOpacity(0.6);
        b.getRect().setOpacity(0.6);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
