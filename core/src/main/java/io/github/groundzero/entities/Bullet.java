package io.github.groundzero.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Bullet fired by the player or AI enemy
 * Bullets move in a given direction based on the gun's angle and deal damage when player collides with bullet
 */
public class Bullet {
    private float x, y; // Bullet's position
    private float dx, dy; // Direction of movement
    private float speed = 8000f; // Bullet speed
    private float prevX, prevY;
    private Texture texture;
    private float rotation;
    private int damage;

    private final float scale = 0.65f;
    private final float verticalOffset = 30f;

    // Creates the bullet with damage value (this one is used by AI enemies)
    public Bullet(float x, float y, float dx, float dy, float rotation) {
        this(x, y, dx, dy, rotation, 10);
    }

    // Creates the bullet with damage value
    public Bullet(float x, float y, float dx, float dy, float rotation, int damage) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.rotation = rotation;
        this.damage = damage;

        // Load bullet texture
        try {
            texture = new Texture("entities/bullet/bullet.png");
        } catch (Exception e) {
            Gdx.app.error("Bullet", "Error loading bullet texture", e);
        }
    }

    // Updates the bullet's position based on its movement direction and speed
    public void update(float deltaTime) {
        x += dx * speed * deltaTime;
        y += dy * speed * deltaTime;
    }

    // Renders the bullet on the screen
    public void render(SpriteBatch batch) {
        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        // Draw the bullet with rotation applied
        batch.draw(texture, x, y + verticalOffset, width / 2f, height / 2f,
            width, height, 1, 1, rotation, 0, 0, (int) width, (int) height, false, false);
    }

    // Getters for bullet properties
    public float getX() { return x; }
    public float getY() { return y; }
    public int getDamage() { return damage; }

    // Get bullet dimensions
    public float getWidth() { return texture.getWidth() * scale; }
    public float getHeight() { return texture.getHeight() * scale; }
    public float getPrevX() { return prevX; }
    public float getPrevY() { return prevY; }

    // Disposes of the bullet texture
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
