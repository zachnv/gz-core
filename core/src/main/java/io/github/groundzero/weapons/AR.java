package io.github.groundzero.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.github.groundzero.entities.Bullet;

/**
 * AR class. Handles shooting, reloading, and rendering of this weapon
 * Bobbing animation effect and manages the ammo for the player
 */
public class AR {
    // AR fields
    private float x, y;
    private boolean facingRight = true;
    private Texture gunTexture;
    private Texture reloadTexture;
    private final float scale = 3f;
    private float gunAngle = 0f;
    private float rawAngle = 0f;
    private Sound gunSound;
    private static final float GUNSHOT_VOLUME = 0.05f;

    // Ammo and reloading fields
    private final int magazineCapacity = 30;
    private int magazineAmmo = magazineCapacity;
    private int reserveAmmo = 90;
    private final float reloadTime = 1.2f;
    private float reloadTimer = 0f;
    private boolean reloading = false;

    // Animation fields for bobbing effect
    private float animationTimer = 0f;
    private final float bobbingAmplitude = 0.5f;
    private final float bobbingFrequency = 2 * MathUtils.PI;

    /**
     * Initializes the AR at the given position
     * Loads textures and sounds for the AR
     *
     * @param x The initial X position of the AR
     * @param y The initial Y position of the AR
     */
    public AR(float x, float y) {
        this.x = x;
        this.y = y;

        // Load textures and handle errors if any
        try {
            gunTexture = new Texture("entities/AR/AR.png");
        } catch (Exception e) {
            Gdx.app.error("AR", "Error loading AR texture!", e);
        }

        try {
            reloadTexture = new Texture("entities/AR/ARreload.png");
        } catch (Exception e) {
            Gdx.app.error("AR", "Error loading ARreload texture!", e);
        }

        // Load gunshot sound and handle errors if any
        try {
            gunSound = Gdx.audio.newSound(Gdx.files.internal("entities/AR/sounds/gunshot.wav"));
        } catch (Exception e) {
            Gdx.app.error("AR", "Error loading gunshot sound!", e);
        }
    }

    /**
     * Updates the AR state. Handles the bobbing animation, reloading, and gun angle
     *
     * @param playerX The X position of the player
     * @param playerY The Y position of the player
     * @param playerFacingRight Whether the player is facing right
     * @param aimingAngle The aiming angle based on player input
     * @param deltaTime The time passed since the last frame
     */
    public void update(float playerX, float playerY, boolean playerFacingRight, float aimingAngle, float deltaTime) {
        this.x = playerX;
        this.y = playerY;
        this.facingRight = playerFacingRight;
        this.rawAngle = aimingAngle;
        this.gunAngle = (!facingRight) ? 360 - rawAngle : rawAngle;

        // Animation timer for bobbing effect
        animationTimer += deltaTime;

        // Automatic reloading when magazine = empty
        if (!reloading && magazineAmmo == 0 && reserveAmmo > 0) {
            reloading = true;
            reloadTimer = 0f;
        }

        if (reloading) {
            reloadTimer += deltaTime;
            if (reloadTimer >= reloadTime) {
                int bulletsNeeded = magazineCapacity - magazineAmmo;
                int bulletsToLoad = Math.min(bulletsNeeded, reserveAmmo);
                magazineAmmo += bulletsToLoad;
                reserveAmmo -= bulletsToLoad;
                reloading = false;
                reloadTimer = 0f;
            }
        }
    }

    /**
     * Reloading when the player presses "R"
     */
    public void triggerReload() {
        if (!reloading && magazineAmmo < magazineCapacity && reserveAmmo > 0) {
            reloading = true;
            reloadTimer = 0f;
        }
    }

    /**
     * Shoots a bullet in the direction the AR is pointing
     *
     * @param targetX The X position of the shooting target
     * @param targetY The Y position of the shooting target
     * @return Bullet object representing the shot, or null if the AR is reloading or out of ammo
     */
    public Bullet shoot(float targetX, float targetY) {
        // If reloading or out of ammo, don't shoot
        if (reloading || magazineAmmo <= 0) {
            return null;
        }

        // Play gunshot sound
        if (gunSound != null) {
            gunSound.play(GUNSHOT_VOLUME);
        }

        magazineAmmo--; // Decrease ammo in the magazine

        // Calculate the bullet's starting position and direction
        float muzzleDistance = 30f;
        float angleRadians = (float) Math.toRadians(rawAngle);
        float startX = x + (float) Math.cos(angleRadians) * muzzleDistance * (facingRight ? 1 : -1);
        float startY = y + (float) Math.sin(angleRadians) * muzzleDistance;
        float dx = (float) Math.cos(angleRadians) * (facingRight ? 1 : -1);
        float dy = (float) Math.sin(angleRadians);

        // Returns new Bullet object
        return new Bullet(startX, startY, dx, dy, gunAngle, 15);
    }

    /**
     * Renders the AR to the screen, including the bobbing animation and reload animation
     *
     * @param batch The SpriteBatch used to render the AR texture
     */
    public void render(SpriteBatch batch) {
        // Use reload texture during reloading, adjust X position for the AR
        Texture currentTexture = reloading ? reloadTexture : gunTexture;
        float gunDrawX = x + 8f;
        // Calculate vertical offset for bobbing effect
        float verticalOffset = MathUtils.sin(animationTimer * bobbingFrequency) * bobbingAmplitude;
        float gunDrawY = y + 15f + verticalOffset;

        // If the AR is facing left, adjust its X position
        if (!facingRight) {
            gunDrawX -= 32f;
        }

        // Get texture width and height
        float width = currentTexture.getWidth() * scale;
        float height = currentTexture.getHeight() * scale;
        float originX = width / 2f;
        float originY = height / 2f;
        boolean flipX = !facingRight; // Flip the texture horizontally if facing left

        // Draw the AR texture
        batch.draw(currentTexture, gunDrawX, gunDrawY, originX, originY,
            width, height, 1, 1, gunAngle,
            0, 0, currentTexture.getWidth(), currentTexture.getHeight(), flipX, false);
    }

    /**
     * Disposes the AR's resources
     */
    public void dispose() {
        if (gunTexture != null) gunTexture.dispose();
        if (reloadTexture != null) reloadTexture.dispose();
        if (gunSound != null) gunSound.dispose();
    }

    // Getters for the AR's position and ammo count
    public float getX() { return x; }
    public float getY() { return y; }
    public int getMagazineAmmo() { return magazineAmmo; }
    public int getReserveAmmo() { return reserveAmmo; }

    /**
     * Adds the given amount of ammo to the reserve
     *
     * @param amount The amount of ammo to add
     */
    public void addReserveAmmo(int amount) {
        reserveAmmo += amount;
    }
}
