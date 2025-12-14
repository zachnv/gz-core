package io.github.groundzero.entities;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import io.github.groundzero.utils.InputHandler;
import io.github.groundzero.weapons.AR;

/**
 * Player class in the game.
 * Handles movement, textures, animations, shooting, and health
 */
public class Player {
    private float x, y;
    private float speed = 180f;
    private int health = 100;
    private int score = 0;
    private final float scale = 4.0f;

    // Sprint multiplier
    private final float sprintMultiplier = 1.25f;

    // Animation variables
    private Animation<TextureRegion> runAnimation;
    private float stateTime = 0f;

    // Textures for player and crosshair
    private Texture idleTexture;
    private Texture[] runTextures;
    private Texture crosshairTexture;

    // Dead texture
    private Texture deadTexture;

    // Bullet list
    private ArrayList<Bullet> bullets = new ArrayList<>();

    // Crosshair offset
    private float crosshairOffsetX, crosshairOffsetY;

    private enum State {
        IDLE,
        RUNNING
    }

    private State currentState = State.IDLE;
    private boolean facingRight = true;

    private float cooldownTimer = 0f;
    private final float shootCooldown = 0.2f;

    // Weapon (AR) object
    private AR ar;

    /**
     * Adjusts the player's position by the given amounts
     *
     * @param dx Amount to move in X
     * @param dy Amount to move in Y
     */
    public void pushBy(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    public Player(float x, float y) {
        this.x = x;
        this.y = y;

        // Load idle texture
        try {
            idleTexture = new Texture("entities/player/player_idle_1.png");
        } catch (Exception e) {
            Gdx.app.error("Player", "Error loading idle texture!", e);
        }

        // Load running textures
        try {
            runTextures = new Texture[5];
            runTextures[0] = new Texture("entities/player/player_run_1.png");
            runTextures[1] = new Texture("entities/player/player_run_2.png");
            runTextures[2] = new Texture("entities/player/player_run_3.png");
            runTextures[3] = new Texture("entities/player/player_run_4.png");
            runTextures[4] = new Texture("entities/player/player_run_5.png");
        } catch (Exception e) {
            Gdx.app.error("Player", "Error loading running textures!", e);
        }

        // Build running animation
        TextureRegion[] runFrames = new TextureRegion[runTextures.length];
        for (int i = 0; i < runTextures.length; i++) {
            if (runTextures[i] != null) {
                runFrames[i] = new TextureRegion(runTextures[i]);
            }
        }
        runAnimation = new Animation<TextureRegion>(0.1f, runFrames);
        runAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load crosshair texture
        try {
            crosshairTexture = new Texture("entities/player/crosshair.png");
        } catch (Exception e) {
            Gdx.app.error("Player", "Error loading crosshair texture!", e);
        }

        // Load dead texture
        try {
            deadTexture = new Texture("entities/player/player_dead.png");
        } catch (Exception e) {
            Gdx.app.error("Player", "Error loading dead texture!", e);
        }

        // Initialize crosshair offset
        crosshairOffsetX = 0;
        crosshairOffsetY = 0;

        // Initialize weapon
        ar = new AR(x, y);
    }

    public void update(float deltaTime) {
        // If dead, skip update
        if (health <= 0) return;

        // Determine if sprinting
        boolean sprinting = InputHandler.isKeyPressed(Input.Keys.SHIFT_LEFT);
        float moveDelta = deltaTime * (sprinting ? sprintMultiplier : 1f);

        stateTime += moveDelta;  // speed up animation when sprinting
        float newX = x;
        float newY = y;
        boolean moving = false;

        // Handle movement input
        if (InputHandler.isKeyPressed(Input.Keys.W)) {
            newY += speed * moveDelta;
            moving = true;
        }
        if (InputHandler.isKeyPressed(Input.Keys.S)) {
            newY -= speed * moveDelta;
            moving = true;
        }
        if (InputHandler.isKeyPressed(Input.Keys.A)) {
            newX -= speed * moveDelta;
            moving = true;
        }
        if (InputHandler.isKeyPressed(Input.Keys.D)) {
            newX += speed * moveDelta;
            moving = true;
        }

        // Update animation state
        currentState = moving ? State.RUNNING : State.IDLE;
        x = newX;
        y = newY;

        // Update cooldown timer for shooting
        cooldownTimer += deltaTime;

        // Check if the player presses "R" to reload
        if (InputHandler.isKeyPressed(Input.Keys.R)) {
            ar.triggerReload();
        }

        // Update crosshair position
        crosshairOffsetX += Gdx.input.getDeltaX();
        crosshairOffsetY -= Gdx.input.getDeltaY();

        // Clamp crosshair position within the screen
        float targetX = getCenterX() + crosshairOffsetX;
        float targetY = getCenterY() + crosshairOffsetY;
        float camLeft = getCenterX() - Gdx.graphics.getWidth() / 2f;
        float camRight = getCenterX() + Gdx.graphics.getWidth() / 2f;
        float camBottom = getCenterY() - Gdx.graphics.getHeight() / 2f;
        float camTop = getCenterY() + Gdx.graphics.getHeight() / 2f;

        targetX = MathUtils.clamp(targetX, camLeft, camRight);
        targetY = MathUtils.clamp(targetY, camBottom, camTop);

        crosshairOffsetX = targetX - getCenterX();
        crosshairOffsetY = targetY - getCenterY();

        // Determine facing direction based on crosshair
        facingRight = (targetX >= getCenterX());

        // Calculate weapon angle
        float angle = MathUtils.atan2(crosshairOffsetY, crosshairOffsetX) * MathUtils.radiansToDegrees;
        if (!facingRight) {
            angle = 180 - angle;
        }

        // Update weapon
        ar.update(x, y, facingRight, angle, deltaTime);

        // Handle shooting
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && cooldownTimer >= shootCooldown) {
            Bullet bullet = ar.shoot(getCenterX() + crosshairOffsetX, getCenterY() + crosshairOffsetY);
            if (bullet != null) {
                bullets.add(bullet);
                cooldownTimer = 0f;
            }
        }

        // Update bullets and remove OOB bullets
        for (int i = 0; i < bullets.size(); i++) {
            Bullet bullet = bullets.get(i);
            bullet.update(deltaTime);
            float camLeftBound = getCenterX() - Gdx.graphics.getWidth() / 2;
            float camRightBound = getCenterX() + Gdx.graphics.getWidth() / 2;
            float camBottomBound = getCenterY() - Gdx.graphics.getHeight() / 2;
            float camTopBound = getCenterY() + Gdx.graphics.getHeight() / 2;
            if (bullet.getX() < camLeftBound - 50 || bullet.getX() > camRightBound + 50 ||
                bullet.getY() < camBottomBound - 50 || bullet.getY() > camTopBound + 50) {
                bullet.dispose();
                bullets.remove(i);
                i--;
            }
        }
    }

    /**
     * Renders the player and bullets
     */
    public void render(SpriteBatch batch) {
        // If dead, render dead texture and return
        if (health <= 0) {
            batch.draw(deadTexture, x, y,
                deadTexture.getWidth() * scale,
                deadTexture.getHeight() * scale);
            return;
        }

        TextureRegion currentFrame;
        if (currentState == State.RUNNING) {
            currentFrame = runAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = new TextureRegion(idleTexture);
        }

        if (facingRight) {
            batch.draw(currentFrame, x, y,
                currentFrame.getRegionWidth() * scale,
                currentFrame.getRegionHeight() * scale);
        } else {
            batch.draw(currentFrame, x + currentFrame.getRegionWidth() * scale,
                y, -currentFrame.getRegionWidth() * scale,
                currentFrame.getRegionHeight() * scale);
        }

        ar.render(batch);
        for (Bullet b : bullets) {
            b.render(batch);
        }
    }

    /**
     * Draws the crosshair on top of everything
     */
    public void renderCrosshair(SpriteBatch batch) {
        float crosshairX = getCenterX() + crosshairOffsetX;
        float crosshairY = getCenterY() + crosshairOffsetY;
        batch.draw(crosshairTexture,
            crosshairX - crosshairTexture.getWidth() / 2f,
            crosshairY - crosshairTexture.getHeight() / 2f);
    }

    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public void heal(int amount) {
        health = Math.min(100, health + amount);
    }

    public void addReserveAmmo(int ammo) {
        ar.addReserveAmmo(ammo);
    }

    public void dispose() {
        if (idleTexture != null) idleTexture.dispose();
        for (Texture t : runTextures) {
            if (t != null) t.dispose();
        }
        if (crosshairTexture != null) crosshairTexture.dispose();
        if (deadTexture != null) deadTexture.dispose();
        ar.dispose();
        for (Bullet b : bullets) {
            b.dispose();
        }
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return (idleTexture != null ? idleTexture.getWidth() * scale : 0);
    }

    public float getHeight() {
        return (idleTexture != null ? idleTexture.getHeight() * scale : 0);
    }

    public float getCenterX() {
        return x + getWidth() / 2f;
    }

    public float getCenterY() {
        return y + getHeight() / 2f;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int getMagazineAmmo() {
        return ar.getMagazineAmmo();
    }

    public int getReserveAmmo() {
        return ar.getReserveAmmo();
    }
}
