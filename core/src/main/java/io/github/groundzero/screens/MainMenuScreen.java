package io.github.groundzero.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
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

    // uiPixel overlay
    private Texture uiPixel;

    // Enter to play
    private TextButton playButton;

    // Fade to game screen
    private boolean isTransitioning = false;
    private float fadeAlpha = 0f;
    private final float FADE_DURATION = 0.75f;

    // Music fade out
    private float musicVolume = 0.05f;

    public MainMenuScreen(GroundZero game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // Load the background image and UI
        background = new Texture(Gdx.files.internal("menu/menubackground.png"));
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Create a 1x1 texture for overlays
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        uiPixel = new Texture(pm);
        pm.dispose();

        // Play background music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("menu/music/menumusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(musicVolume);
        backgroundMusic.play();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Font for title
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/stalker.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter titleParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        titleParameter.size = 144;
        titleParameter.minFilter = Texture.TextureFilter.Linear;
        titleParameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont titleFont = generator.generateFont(titleParameter);
        skin.add("titleFont", titleFont);
        generator.dispose();

        // Font for buttons
        FreeTypeFontGenerator buttonFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Bender_Regular.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter buttonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        buttonFontParameter.size = 48;
        buttonFontParameter.minFilter = Texture.TextureFilter.Linear;
        buttonFontParameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont buttonFont = buttonFontGenerator.generateFont(buttonFontParameter);
        skin.add("buttonFont", buttonFont);

        // Font for subtitle/hint
        FreeTypeFontGenerator.FreeTypeFontParameter hintFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        hintFontParameter.size = 22;
        hintFontParameter.minFilter = Texture.TextureFilter.Linear;
        hintFontParameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont hintFont = buttonFontGenerator.generateFont(hintFontParameter);
        skin.add("hintFont", hintFont);

        buttonFontGenerator.dispose();

        TextButton.TextButtonStyle playExitButtonStyle = new TextButton.TextButtonStyle();
        playExitButtonStyle.font = skin.getFont("buttonFont");

        // White text only until hover, then invert to white background with black text
        playExitButtonStyle.up = null;
        playExitButtonStyle.over = skin.newDrawable("default-round", Color.WHITE);
        playExitButtonStyle.down = skin.newDrawable("default-round", new Color(0.92f, 0.92f, 0.92f, 1f));

        playExitButtonStyle.fontColor = Color.WHITE;
        playExitButtonStyle.overFontColor = Color.BLACK;
        playExitButtonStyle.downFontColor = Color.BLACK;

        skin.add("playExitButtonStyle", playExitButtonStyle);

        BitmapFont font = skin.getFont("default-font");
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Table layout to center the UI
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        // Card/panel to give contrast over the background
        Table card = new Table();

        // Border around the card
        Table cardBorder = new Table();
        cardBorder.add(card);

        // Title label and play/exit/instructions buttons
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = skin.getFont("titleFont");
        Label title = new Label("GROUND ZERO", titleStyle);
        title.setFontScale(1f);

        Label.LabelStyle hintStyle = new Label.LabelStyle();
        hintStyle.font = skin.getFont("hintFont");
        hintStyle.fontColor = new Color(1, 1, 1, 0.7f);
        Label hint = new Label("PRESS ENTER TO PLAY", hintStyle);

        playButton = new TextButton("PLAY", skin, "playExitButtonStyle");
        TextButton exitButton = new TextButton("EXIT", skin, "playExitButtonStyle");
        //TextButton instructionsButton = new TextButton("INSTRUCTIONS", skin, "playExitButtonStyle");

        // Let buttons scale smoothly on hover
        playButton.setTransform(true);
        playButton.setOrigin(Align.center);
        exitButton.setTransform(true);
        exitButton.setOrigin(Align.center);

        // UI tables with padding and spacing
        card.add(title).padBottom(18);
        card.row();
        card.add(hint).padBottom(22);
        card.row();
        card.add(playButton).width(270).height(72).padTop(6);
        card.row();
        card.add(exitButton).width(270).height(72).padTop(14);
        card.row();
        card.row();
        //card.row();
        //card.add(instructionsButton).size(300, 50).padTop(20);

        root.add(cardBorder);
        stage.addActor(root);

        // Fade in the menu UI
        cardBorder.getColor().a = 0f;
        cardBorder.addAction(Actions.fadeIn(0.35f));

        // Click listener on the play button to change screen
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                startGameTransition();
            }

            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor fromActor) {
                playButton.addAction(Actions.scaleTo(1.03f, 1.03f, 0.08f));
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor toActor) {
                playButton.addAction(Actions.scaleTo(1f, 1f, 0.08f));
            }
        });

        // Click listener on the exit button to exit the game
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                Gdx.app.exit();
            }

            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor fromActor) {
                exitButton.addAction(Actions.scaleTo(1.03f, 1.03f, 0.08f));
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, Actor toActor) {
                exitButton.addAction(Actions.scaleTo(1f, 1f, 0.08f));
            }
        });

        // Click listener on the instructions button to show instructions menu
        /**instructionsButton.addListener(new ClickListener() {
        @Override
        public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
        String instructionsText =
        "Click play and then it takes you into the game screen where you spawn with an AR, 120 bullets,\n" +
        "and can walk around using (W, A, S, D) and left-click on mouse to shoot, when your mag is empty, the gun will automatically reload,\n" +
        "or if you would like to reload when needed, just press \"R\".";
        Dialog dialog = new Dialog("Instructions", skin);
        dialog.text(instructionsText);
        dialog.button("OK");
        dialog.show(stage);
        }
        });*/
    }

    // Fade into game screen
    private void startGameTransition() {
        if (isTransitioning) return;
        isTransitioning = true;
        stage.getRoot().setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Press enter to play
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGameTransition();
        }

        // Fade into game screen
        if (isTransitioning) {
            fadeAlpha += delta / FADE_DURATION;
            // Fade out menu music
            musicVolume -= delta / FADE_DURATION * 0.05f;
            if (musicVolume < 0f) musicVolume = 0f;
            backgroundMusic.setVolume(musicVolume);
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                game.setScreen(new io.github.groundzero.screens.GameScreen(game));
                dispose();
                return;
            }
        }

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

        // Overlay for readability
        batch.setColor(0f, 0f, 0f, 0.18f);
        batch.draw(uiPixel, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        batch.end();

        stage.act(delta);
        stage.draw();

        // Fade into game screen
        if (fadeAlpha > 0f) {
            batch.begin();
            batch.setColor(0f, 0f, 0f, fadeAlpha);
            batch.draw(uiPixel, 0, 0, screenWidth, screenHeight);
            batch.setColor(Color.WHITE);
            batch.end();
        }
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
        if (uiPixel != null) uiPixel.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}
