package io.github.groundzero.utils;

import io.github.groundzero.entities.Enemy;
import io.github.groundzero.entities.Bullet;
import io.github.groundzero.entities.Player;
import io.github.groundzero.level.Level;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Manages the spawning of enemies and handles their behaviors
 * It spawns enemies off-screen and checks for collisions with player bullets
 */
public class EnemySpawner {
    private final Level level; // Call level for enemy collision checks
    private float spawnInterval = 5.0f; // Time between spawns
    private float spawnTimer = 0f; // Timer for spawn time
    private ArrayList<Enemy> enemies; // ArrayList to store all active enemies

    /**
     * @param level The game level (for collision checking)
     */
    public EnemySpawner(Level level) {
        this.level = level;
        enemies = new ArrayList<>();
    }

    /**
     * Updates the enemy spawner
     * Checks for collisions between player bullets and enemies
     *
     * @param deltaTime The time passed since the last frame
     * @param player The player to base enemy spawning on
     */
    public void update(float deltaTime, Player player) {
        spawnTimer += deltaTime;
        // Spawn new enemies
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            if (MathUtils.randomBoolean(0.5f)) {
                int enemyCount = MathUtils.random(1, 2);
                for (int i = 0; i < enemyCount; i++) {
                    spawnEnemy(player);
                }
            }
        }
        // Update each enemy
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime, player);
        }
    }

    // Spawns an enemy off-screen
    private void spawnEnemy(Player player) {
        float margin = 50f;
        float playerCenterX = player.getCenterX();
        float playerCenterY = player.getCenterY();
        float left = playerCenterX - Gdx.graphics.getWidth() / 2f;
        float right = playerCenterX + Gdx.graphics.getWidth() / 2f;
        float bottom = playerCenterY - Gdx.graphics.getHeight() / 2f;
        float top = playerCenterY + Gdx.graphics.getHeight() / 2f;

        float spawnX, spawnY;
        switch (MathUtils.random(0,3)) {
            case 0: spawnX = left - margin;  spawnY = MathUtils.random(bottom, top); break;
            case 1: spawnX = right + margin; spawnY = MathUtils.random(bottom, top); break;
            case 2: spawnY = top + margin;   spawnX = MathUtils.random(left, right); break;
            default:spawnY = bottom - margin;spawnX = MathUtils.random(left, right); break;
        }

        enemies.add(new Enemy(spawnX, spawnY, level));
    }

    /**
     * Checks for collisions between player's bullets and all enemies
     *
     * @param playerBullets The list of bullets fired by the player
     */
    public void checkPlayerBulletCollisions(ArrayList<Bullet> playerBullets) {
        for (Enemy enemy : enemies) {
            if (enemy.getHealth() <= 0) continue;
            Iterator<Bullet> it = playerBullets.iterator();
            while (it.hasNext()) {
                Bullet b = it.next();
                float bx = b.getX() + b.getWidth()/2f;
                float by = b.getY() + b.getHeight()/2f;
                if (bx > enemy.getX() && bx < enemy.getX() + enemy.getWidth()
                    && by > enemy.getY() && by < enemy.getY() + enemy.getHeight()) {
                    enemy.takeDamage(b.getDamage());
                    b.dispose();
                    it.remove();
                }
            }
        }
    }

    // Renders all active enemies
    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
    }

    // Disposes of all enemies
    public void dispose() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
    }
}
