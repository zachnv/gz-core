package io.github.groundzero.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import io.github.groundzero.entities.Player;
import com.badlogic.gdx.math.MathUtils;

/**
 * The HUD class takes care of all the rendering and displaying
 * of the UI such as the health bar and ammo count, and score (not added yet)
 * Also keeps proper positioning based on the camera's viewport
 */
public class HUD {
    private BitmapFont ammoFont;
    private Texture healthbarBorderTexture;
    private Texture healthbarTexture;

    // Border position offset
    private float healthBarOffsetX = 30f;
    private float healthBarOffsetY = 30f;

    // Health bar inner offset
    private float healthBarInnerOffsetX = 35.5f;
    private float healthBarInnerOffsetY = 2f;

    // Scaling for health bar textures
    private float healthBarScaleX = 2.2f;
    private float healthBarScaleY = 2.2f;

    public HUD() {
        // Load the ammo font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 42;
        ammoFont = generator.generateFont(parameter);
        generator.dispose();

        // Load the health bar border texture
        try {
            healthbarBorderTexture = new Texture("entities/HUD/healthbarborder.png");
        } catch (Exception e) {
            Gdx.app.error("HUD", "Error loading healthbar border texture!", e);
        }

        // Load the health bar texture
        try {
            healthbarTexture = new Texture("entities/HUD/healthbar.png");
        } catch (Exception e) {
            Gdx.app.error("HUD", "Error loading healthbar texture!", e);
        }
    }

    /**
     * Renders the HUD elements
     *
     * @param batch The SpriteBatch used for drawing
     * @param player The player containing HUD data
     * @param camera The camera viewport is used for positioning
     */
    public void render(SpriteBatch batch, Player player, OrthographicCamera camera) {
        // Bottom left corner of the camera's view
        float camLeft = camera.position.x - camera.viewportWidth / 2f;
        float camBottom = camera.position.y - camera.viewportHeight / 2f;

        // Final position for the health bar border
        float borderX = camLeft + healthBarOffsetX;
        float borderY = camBottom + healthBarOffsetY;

        // Draw the border texture
        if (healthbarBorderTexture != null) {
            batch.draw(healthbarBorderTexture, borderX, borderY,
                healthbarBorderTexture.getWidth() * healthBarScaleX,
                healthbarBorderTexture.getHeight() * healthBarScaleY);
        }

        // Clamp health percentage between 0 and 1
        float healthPercentage = MathUtils.clamp(player.getHealth() / 100f, 0f, 1f);

        // Position the health bar relative to the border
        float healthBarX = borderX + healthBarInnerOffsetX;
        float healthBarY = borderY + healthBarInnerOffsetY;

        // Draw the health bar texture
        if (healthbarTexture != null) {
            TextureRegion healthRegion = new TextureRegion(healthbarTexture);
            int scaledHealthWidth = (int) (healthbarTexture.getWidth() * healthPercentage);
            healthRegion.setRegionWidth(scaledHealthWidth);
            batch.draw(healthRegion, healthBarX, healthBarY,
                scaledHealthWidth * healthBarScaleX,
                healthbarTexture.getHeight() * healthBarScaleY);
        }

        // Draw ammo count in the bottom right
        String ammoText = player.getMagazineAmmo() + "|" + player.getReserveAmmo();
        GlyphLayout layout = new GlyphLayout(ammoFont, ammoText);
        float textWidth = layout.width;
        float textHeight = layout.height;
        float ammoX = camera.viewportWidth - textWidth - 30f;
        float ammoY = textHeight + 30f;
        ammoFont.draw(batch, ammoText, ammoX, ammoY);
    }

    // Setters for positioning and scaling
    public void setHealthBarBorderPosition(float offsetX, float offsetY) {
        this.healthBarOffsetX = offsetX;
        this.healthBarOffsetY = offsetY;
    }

    public void setHealthBarInnerPosition(float offsetX, float offsetY) {
        this.healthBarInnerOffsetX = offsetX;
        this.healthBarInnerOffsetY = offsetY;
    }

    public void setHealthBarScale(float scaleX, float scaleY) {
        this.healthBarScaleX = scaleX;
        this.healthBarScaleY = scaleY;
    }

    // Disposes of ammo and textures
    public void dispose() {
        ammoFont.dispose();
        if (healthbarBorderTexture != null) {
            healthbarBorderTexture.dispose();
        }
        if (healthbarTexture != null) {
            healthbarTexture.dispose();
        }
    }
}
