package io.github.groundzero;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.groundzero.screens.MainMenuScreen;
import io.github.groundzero.entities.Player;
import io.github.groundzero.level.Level;
import io.github.groundzero.game.HUD;

/**
 * Main game class that handles the game and manages screens, player, level, and HUD
 * Extends from the libGDX Game class to handle the game (create, render, dispose)
 */
public class GroundZero extends Game {
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private HUD hud;

    // Called when the game is started
    // It sets up the player, level, HUD, and starts with the Main Menu screen
    @Override
    public void create() {
        batch = new SpriteBatch();
        player = new Player(100, 100);
        // Load tilemap
        level = new Level("map/groundzero.tmx");
        hud = new HUD();

        // Start the game with the Main Menu Screen
        setScreen(new MainMenuScreen(this));
    }

    // Called every frame to render the game
    @Override
    public void render() {
        super.render();
    }

    // Disposes of all resources (textures, sounds, etc.) when the game is closed
    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        level.dispose();
        hud.dispose();
    }

    // Getter methods to access private fields
    public SpriteBatch getBatch() {
        return batch;
    }
    public Player getPlayer() {
        return player;
    }
    public Level getLevel() {
        return level;
    }
    public HUD getHUD() {
        return hud;
    }
}
