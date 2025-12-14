package io.github.groundzero.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.groundzero.GroundZero;

/**
 * Displays the main menu of the game, with the background image, background music, and buttons to start or exit the game
 * The background also moves based on the mouse position. The screen handles user input to navigate to the game or exit the application
 */
public class MainMenuScreen implements Screen {
    private GroundZero game;
    private Stage stage;
    private Skin skin;
    private Texture background;
    private SpriteBatch batch;

    // Background music
    private Music backgroundMusic;

    // Move background around with mouse
    private final float MOUSE_OFFSET_FACTOR = 0.05f;

    public MainMenuScreen(GroundZero game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Load the background image and UI
        background = new Texture(Gdx.files.internal("menu/menubackground.png"));
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Play background music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("menu/music/groundzero.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.05f);
        backgroundMusic.play();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Font for title
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Bender_Bold.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 82;
        titleParameter.minFilter = Texture.TextureFilter.Linear;
        titleParameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont titleFont = generator.generateFont(titleParameter);
        skin.add("titleFont", titleFont);

        // Font for buttons
        FreeTypeFontGenerator buttonFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Bender_Regular.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter buttonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        buttonFontParameter.size = 48;
        buttonFontParameter.minFilter = Texture.TextureFilter.Linear;
        buttonFontParameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont buttonFont = buttonFontGenerator.generateFont(buttonFontParameter);
        buttonFontGenerator.dispose();

        skin.add("buttonFont", buttonFont);

        TextButton.TextButtonStyle playExitButtonStyle = new TextButton.TextButtonStyle();
        playExitButtonStyle.font = skin.getFont("buttonFont");

        playExitButtonStyle.up = null;
        playExitButtonStyle.down = null;
        playExitButtonStyle.checked = skin.newDrawable("white", Color.BLACK);
        playExitButtonStyle.over = skin.newDrawable("white", 245, 245, 245, 0.075f);

        skin.add("playExitButtonStyle", playExitButtonStyle);

        BitmapFont font = skin.getFont("default-font");
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Table layout to center the UI
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Title label and play/exit/instructions buttons
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = skin.getFont("titleFont");
        Label title = new Label("GROUND ZERO", titleStyle);
        title.setFontScale(1f);

        TextButton playButton = new TextButton("PLAY", skin, "playExitButtonStyle");
        TextButton exitButton = new TextButton("EXIT", skin, "playExitButtonStyle");
        TextButton instructionsButton = new TextButton("INSTRUCTIONS", skin, "playExitButtonStyle");

        // UI tables with padding and spacing
        table.add(title).padBottom(30);
        table.row();
        table.add(playButton).size(220, 60).padTop(60);
        table.row();
        table.add(exitButton).size(220, 60).padTop(20);
        table.row();
        table.row();
        table.row();
        table.add(instructionsButton).size(300, 50).padTop(20);

        stage.addActor(table);

        // Click listener on the play button to change screen
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new io.github.groundzero.screens.GameScreen(game));
                dispose();
            }
        });

        // Click listener on the exit button to exit the game
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Click listener on the instructions button to show instructions menu
        instructionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                String instructionsText =
                    "Click play and then it takes you into the game screen where you spawn with an AR, 120 bullets,\n" +
                        "and can walk around using (W, A, S, D) and left-click on mouse to shoot, when your mag is empty, the gun will automatically reload,\n" +
                        "or if you would like to reload when needed, just press \"R\", and also this is self explanatory,\n" +
                        "but the bullets go wherever your crosshair is aiming.";
                Dialog dialog = new Dialog("Instructions", skin);
                dialog.text(instructionsText);
                dialog.button("OK");
                dialog.show(stage);
            }
        });
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Retrieve the current mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        mouseY = screenHeight - mouseY;

        // Scale makes sure that the background always covers the main menu screen
        float bgWidth = background.getWidth();
        float bgHeight = background.getHeight();
        float scale = Math.max(screenWidth / bgWidth, screenHeight / bgHeight) * 1.1f;
        float scaledBgWidth = bgWidth * scale;
        float scaledBgHeight = bgHeight * scale;

        // Calculate the maximum margins
        float marginX = (scaledBgWidth - screenWidth) / 2f;
        float marginY = (scaledBgHeight - screenHeight) / 2f;

        // Calculate the offset
        float offsetX = (mouseX - screenWidth / 2f) * MOUSE_OFFSET_FACTOR;
        float offsetY = (mouseY - screenHeight / 2f) * MOUSE_OFFSET_FACTOR;

        // Margins for background
        offsetX = Math.max(-marginX, Math.min(marginX, offsetX));
        offsetY = Math.max(-marginY, Math.min(marginY, offsetY));

        // Keeps background centered
        float drawX = -(scaledBgWidth - screenWidth) / 2f + offsetX;
        float drawY = -(scaledBgHeight - screenHeight) / 2f + offsetY;

        batch.begin();
        batch.draw(background, drawX, drawY, scaledBgWidth, scaledBgHeight);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }     // Update the main menu screen with new screen dimensions

    // These methods handle the screen events related to the input and game state, will be used later on
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
        skin.dispose();
        batch.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
