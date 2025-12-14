package io.github.groundzero.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.groundzero.entities.Player;

/**
 * MedKit item dropped by an enemy
 * It can be picked up by the player to restore health
 */
public class MedKit {
    private float x, y; // Position of the medkit
    private Texture medkitTexture; // Medkit texture
    private Texture shadowTexture; // Shadow texture
    private boolean pickedUp = false; // Checks whether the medkit has been picked up

    // Bobbing animation fields
    private float animationTimer = 0f;
    private final float bobbingAmplitude = 2f;
    private final float bobbingFrequency = 1 * MathUtils.PI;

    // Scaling factors for medkit textures
    private float medkitScale = 2.5f;
    private float shadowScale = 2.5f;

    public MedKit(float x, float y) {
        this.x = x;
        this.y = y;
        // Load medkit and shadow textures
        medkitTexture = new Texture("entities/items/medkit/medkit.png");
        shadowTexture = new Texture("entities/items/other/shadow.png");
    }

    /**
     * Updates the medkit's bobbing animation and checks for player collision
     * If the player collides with the medkit and is damaged, the medkit heals the player to full health
     *
     * @param deltaTime Time since the last frame
     * @param player The player object
     */
    public void update(float deltaTime, Player player) {
        animationTimer += deltaTime;
        // Only check for pickup if player's health is not full
        if (player.getHealth() < 100) {
            Rectangle medkitBounds = getBounds();
            Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            if (medkitBounds.overlaps(playerBounds)) {
                player.heal(100); // Heal player back to full health
                pickedUp = true;
            }
        }
    }

    /**
     * Renders the medkit and its shadow
     *
     * @param batch The SpriteBatch used for drawing
     */
    public void render(SpriteBatch batch) {
        if (pickedUp) return;
        float bobbingOffset = MathUtils.sin(animationTimer * bobbingFrequency) * bobbingAmplitude;
        // Draw the shadow using the shadow scale
        batch.draw(shadowTexture, x - 5, y - 7, shadowTexture.getWidth() * shadowScale, shadowTexture.getHeight() * shadowScale);
        // Draw the medkit with the bobbing offset
        batch.draw(medkitTexture, x, y + bobbingOffset, medkitTexture.getWidth() * medkitScale, medkitTexture.getHeight() * medkitScale);
    }

    // Returns the bounding rectangle for collision detection
    public Rectangle getBounds() {
        return new Rectangle(x, y, medkitTexture.getWidth(), medkitTexture.getHeight());
    }

    // Indicates if the medkit has been picked up
    public boolean isPickedUp() { return pickedUp; }

    // Scaling for medkit textures
    public void setMedkitScale(float scale) { this.medkitScale = scale; }
    public void setShadowScale(float scale) { this.shadowScale = scale; }

    // Disposes of the medkit's textures
    public void dispose() {
        medkitTexture.dispose();
        shadowTexture.dispose();
    }
}
