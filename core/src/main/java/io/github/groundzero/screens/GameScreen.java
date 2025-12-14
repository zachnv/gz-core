package io.github.groundzero.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.github.groundzero.GroundZero;
import io.github.groundzero.entities.Player;
import io.github.groundzero.utils.EnemySpawner;
import io.github.groundzero.level.Level;
import io.github.groundzero.game.HUD;

/**
 * GameScreen is the main gameplay screen
 * It renders the level, player, enemies, HUD, and all game logic
 * It also manages the camera movement, collision detection, and ambient sounds
 */
public class GameScreen implements Screen {
    private GroundZero game;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private HUD hud;
    private EnemySpawner enemySpawner;

    // Ambient sounds
    private Music ambientSounds;

    // Death timer for when the player dies
    private float deathTimer = 0f;
    private static final float DEATH_DELAY = 2f;

    public GameScreen(GroundZero game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Game world camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // HUD camera
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize level, player, HUD, and enemy spawner
        level = new Level("map/groundzero.tmx");
        player = new Player(2200, 2300);
        hud = new HUD();
        enemySpawner = new EnemySpawner(level);

        // Ambient sounds
        ambientSounds = Gdx.audio.newMusic(Gdx.files.internal("sounds/ambient/ambientsounds.mp3"));
        ambientSounds.setLooping(true);
        ambientSounds.setVolume(0.7f);
        ambientSounds.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // If player is dead, death timer and return to main menu
        if (player.getHealth() <= 0) {
            deathTimer += delta;
            if (deathTimer >= DEATH_DELAY) {
                game.setScreen(new io.github.groundzero.screens.MainMenuScreen(game));
                dispose();
                return;
            }
        } else {
            float oldX = player.getX();
            float oldY = player.getY();

            player.update(delta);
            if (isPlayerColliding()) {
                player.setPosition(oldX, oldY);
            }

            enemySpawner.update(delta, player);
            enemySpawner.checkPlayerBulletCollisions(player.getBullets());
        }

        // Camera to follow the player
        camera.position.set(
            player.getX() + player.getWidth() / 2f,
            player.getY() + player.getHeight() / 2f,
            0
        );
        camera.update();

        // render base layers (ground, collision, other)
        level.renderBase(camera);

        // draw enemies & player
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        enemySpawner.render(batch);
        player.render(batch);

        // fade trees around the player
        Rectangle playerBounds = new Rectangle(
            player.getX(),
            player.getY(),
            player.getWidth(),
            player.getHeight()
        );
        level.renderTrees(camera, batch, playerBounds);
        player.renderCrosshair(batch);
        batch.end();

        // Render HUD
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        hud.render(batch, player, uiCamera);
        batch.end();
    }

    /**
     * Checks if the player is colliding with a wall
     * Uses the "collision" layer from the TiledMap
     * @return true if the player is colliding with a wall, false otherwise
     */
    private boolean isPlayerColliding() {
        float playerX = player.getX();
        float playerY = player.getY();
        float playerWidth = player.getWidth();
        float playerHeight = player.getHeight();

        if (level.isWallAt(playerX, playerY)
            || level.isWallAt(playerX + playerWidth, playerY)
            || level.isWallAt(playerX, playerY + playerHeight)
            || level.isWallAt(playerX + playerWidth, playerY + playerHeight)) {
            return true;
        }
        return false;
    }

    /**
     * Called when the game screen is resized
     * This updates the camera to match
     *
     * @param width The new width of the screen
     * @param height The new height of the screen
     */
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        uiCamera.setToOrtho(false, width, height);
    }

    @Override public void show() { Gdx.input.setCursorCatched(true); }
    @Override public void hide() { Gdx.input.setCursorCatched(false); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        hud.dispose();
        if (ambientSounds != null) {
            ambientSounds.stop();
            ambientSounds.dispose();
        }
        level.dispose();
        player.dispose();
        enemySpawner.dispose();
    }
}
