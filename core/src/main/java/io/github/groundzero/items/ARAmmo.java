package io.github.groundzero.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import io.github.groundzero.entities.Player;

/**
 * AR Ammo Pack item dropped by an enemy
 * When picked up, it adds 30 bullets to the player's reserve ammo
 */
public class ARAmmo {
    private float x, y; // Position of the ammo pack
    private Texture ammoTexture; // Ammo pack texture
    private Texture shadowTexture; // Shadow texture
    private boolean pickedUp = false; // Checks whether the ammo pack has been picked up

    // Bobbing animation fields
    private float animationTimer = 0f;
    private final float bobbingAmplitude = 2f;
    private final float bobbingFrequency = 1 * MathUtils.PI;

    // Scaling factors for textures
    private float ammoScale = 2.5f;
    private float shadowScale = 2.5f;

    // Amount of ammo provided by this pack
    public static final int AMMO_AMOUNT = 30;

    public ARAmmo(float x, float y) {
        this.x = x;
        this.y = y;
        // Load the ammo pack and shadow textures
        ammoTexture = new Texture("entities/items/ammo_pack/ammo_pack.png");
        shadowTexture = new Texture("entities/items/other/shadow.png");
    }

    /**
     * Updates the ammo pack's bobbing animation and checks for collision with the player
     * On collision, it adds ammo to the player's reserve and marks itself as picked up
     *
     * @param deltaTime Time since the last frame
     * @param player The player object
     */
    public void update(float deltaTime, Player player) {
        animationTimer += deltaTime;
        // Check for collision with the player only if not already picked up
        if (!pickedUp) {
            Rectangle ammoBounds = getBounds();
            Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            if (ammoBounds.overlaps(playerBounds)) {
                player.addReserveAmmo(AMMO_AMOUNT); // Increase player's reserve ammo by 30
                pickedUp = true;
            }
        }
    }

    /**
     * Renders the ammo pack and its shadow
     *
     * @param batch The SpriteBatch used for drawing
     */
    public void render(SpriteBatch batch) {
        if (pickedUp) return;
        float bobbingOffset = MathUtils.sin(animationTimer * bobbingFrequency) * bobbingAmplitude;
        // Draw the shadow texture
        batch.draw(shadowTexture, x - 5, y - 7, shadowTexture.getWidth() * shadowScale, shadowTexture.getHeight() * shadowScale);
        // Draw the ammo pack texture with the bobbing effect
        batch.draw(ammoTexture, x, y + bobbingOffset, ammoTexture.getWidth() * ammoScale, ammoTexture.getHeight() * ammoScale);
    }

    /**
     * Returns the rectangle for collision
     *
     * @return Rectangle representing the ammo pack bounds
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, ammoTexture.getWidth() * ammoScale, ammoTexture.getHeight() * ammoScale);
    }

    /**
     * Indicates if the ammo pack has been picked up
     *
     * @return true if picked up; false otherwise
     */
    public boolean isPickedUp() {
        return pickedUp;
    }

    /**
     * Disposes of the ammo pack's textures
     */
    public void dispose() {
        ammoTexture.dispose();
        shadowTexture.dispose();
    }

    // Optional setters for scaling adjustments
    public void setAmmoScale(float scale) { this.ammoScale = scale; }
    public void setShadowScale(float scale) { this.shadowScale = scale; }
}
