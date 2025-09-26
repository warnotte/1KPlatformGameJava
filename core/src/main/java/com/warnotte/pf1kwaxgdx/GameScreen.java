package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GameScreen implements Screen {
    private final GameMain game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GameStateGDX gs;
    private OrthographicCamera camera;
    private boolean debugView = true;
    private BackgroundRenderer backgroundRenderer;
    private boolean useTestLevel = false;
    private int lives = 3;

    private RetroPostProcessor postProcessor;
    private ParticleSystem particleSystem;
    private PostProcessSettings postSettings;
    private boolean showPostUi = false;
    private float shaderTime = 0f;
    private List<SliderControl> sliderControls;
    private SliderControl activeSlider;
    private final Matrix4 screenMatrix = new Matrix4();
    private final OverlayGeometry overlayGeometry = new OverlayGeometry();

    public GameScreen(GameMain game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        gs = new GameStateGDX();
        gs.player.resetGroundState();
        lives = 3;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 900, 500);
        camera.update();
        backgroundRenderer = new BackgroundRenderer(shapeRenderer);
        particleSystem = new ParticleSystem();
        postSettings = new PostProcessSettings();
        postSettings.clampAll();
        initializePostProcessControls();
        postProcessor = new RetroPostProcessor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), postSettings);
        shaderTime = 0f;
    }

    @Override
    public void render(float delta) {
        shaderTime += delta;
        if (postProcessor != null) {
            postProcessor.begin();
        } else {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            debugView = !debugView;
            if (debugView) {
                camera.setToOrtho(false, 900, 500);
            } else {
                camera.setToOrtho(false, 320, 180);
            }
        }

        if (!showPostUi && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            useTestLevel = !useTestLevel;
            if (useTestLevel) {
                gs.level = TestLevelGenerator.createTestLevel();
            } else {
                gs.level = new LevelGenerator(System.currentTimeMillis()).generateLevel();
            }
            gs.player.x = 50f;
            gs.player.y = 100f;
            gs.player.vy = 0f;
            gs.player.isJumping = false;
            gs.player.resetGroundState();
            lives = 3;
            if (particleSystem != null) {
                particleSystem.clear();
            }
        }


        if (!debugView) {
            float camX = gs.player.x;
            float camY = gs.player.y;
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            float minX = halfW;
            float maxX = gs.level.getLevelWidth() - halfW;
            camX = Math.max(minX, Math.min(maxX, camX));
            camY = Math.max(halfH, Math.min(300f, camY));
            camera.position.set(camX, camY, 0f);
        } else {
            camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        handlePostProcessUiInput(delta);

        gs.level.update(delta);

        float speed = 2.2f;
        float jumpPower = 8.5f;
        float gravity = 0.45f;
        float maxFallSpeed = 8f;

        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.Q);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        float nextX = gs.player.x;
        if (left) {
            nextX -= speed;
        }
        if (right) {
            nextX += speed;
        }

        gs.player.vy -= gravity;
        if (gs.player.vy < -maxFallSpeed) {
            gs.player.vy = -maxFallSpeed;
        }
        float nextY = gs.player.y + gs.player.vy;

        if (up && !gs.player.isJumping) {
            Level.PlatformCollisionResult testJump = gs.level.checkCollisions(gs.player, gs.player.x, gs.player.y - 1f);
            if (testJump.onGround) {
                gs.player.vy = jumpPower;
                gs.player.isJumping = true;
                nextY = gs.player.y + gs.player.vy;
            }
        }

        boolean wasOnGround = gs.player.onGround;
        Platform previousPlatform = gs.player.groundPlatform;

        Level.PlatformCollisionResult collision = gs.level.checkCollisions(gs.player, nextX, nextY);
        gs.player.x = collision.newX;
        gs.player.y = collision.newY;

        if (collision.onGround && collision.contactPlatform != null) {
            Platform contactPlatform = collision.contactPlatform;
            boolean platformChanged = previousPlatform != contactPlatform;
            boolean landedThisFrame = !wasOnGround || platformChanged;

            if (platformChanged && previousPlatform != null) {
                previousPlatform.onPlayerLeave(gs.player);
            }

            gs.player.onGround = true;
            gs.player.groundPlatform = contactPlatform;
            if (platformChanged) {
                gs.player.timeOnGround = 0f;
            }
            gs.player.timeOnGround += delta;

            contactPlatform.handlePlayerContact(gs.player, delta, landedThisFrame, particleSystem);

            if (gs.player.onGround) {
                if (contactPlatform.type == Platform.PlatformType.MOVING_HORIZONTAL) {
                    float platformDeltaX = (float) Math.cos(contactPlatform.timer * contactPlatform.moveSpeed / 30f)
                        * contactPlatform.moveRange * contactPlatform.moveSpeed / 30f * delta;
                    gs.player.x += platformDeltaX;
                }
                if (landedThisFrame && particleSystem != null) {
                    Color dustColor = contactPlatform.type == Platform.PlatformType.ICE
                        ? new Color(0.9f, 0.95f, 1f, 1f)
                        : new Color(0.8f, 0.72f, 0.6f, 1f);
                    particleSystem.spawnDust(gs.player.x, contactPlatform.y + contactPlatform.height, dustColor, ParticleSystem.ParticleLayer.BACKGROUND);
                }
            } else {
                contactPlatform.onPlayerLeave(gs.player);
                gs.player.timeOnGround = 0f;
                gs.player.isJumping = true;
            }
        } else {
            if (previousPlatform != null) {
                previousPlatform.onPlayerLeave(gs.player);
            }
            gs.player.resetGroundState();
            gs.player.isJumping = true;
        }

        MenuScreen pendingScreen = null;

        if (gs.player.y < -30f) {
            lives--;
            if (lives > 0) {
                gs.player.x = 50f;
                gs.player.y = 100f;
                gs.player.vy = 0f;
                gs.player.isJumping = false;
                gs.player.resetGroundState();
                if (particleSystem != null) {
                    particleSystem.spawnDust(gs.player.x, 100f, new Color(0.8f, 0.7f, 0.6f, 1f), ParticleSystem.ParticleLayer.BACKGROUND);
                }
            } else {
                pendingScreen = new MenuScreen(game, "Game Over");
            }
        }

        if (pendingScreen == null && gs.player.x >= gs.level.getLevelWidth() - 150f) {
            pendingScreen = new MenuScreen(game, "Good Game!");
        }

        backgroundRenderer.render(camera, delta);
        gs.level.render(shapeRenderer);
        if (particleSystem != null) {
            particleSystem.update(delta);
        }

        batch.begin();
        if (particleSystem != null) {
            particleSystem.render(batch, ParticleSystem.ParticleLayer.BACKGROUND);
        }
        gs.player.movingLeft = left && !right;
        gs.player.movingRight = right && !left;
        gs.player.render(batch, delta);
        if (particleSystem != null) {
            particleSystem.render(batch, ParticleSystem.ParticleLayer.FOREGROUND);
        }
        batch.end();

        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "Vies : " + lives, 20f, 460f);
        float currentTime = backgroundRenderer.getTimeOfDay();
        int hours = (int) currentTime;
        int minutes = (int) ((currentTime - hours) * 60f);
        String timeString = String.format("%02d:%02d", hours, minutes);
        font.draw(batch, "Heure : " + timeString, 20f, 420f);
        String levelMode = useTestLevel ? "Mode: TEST" : "Mode: GENERE";
        font.draw(batch, levelMode, 20f, 380f);
        font.getData().setScale(1f);
        font.draw(batch, "[R] Changer niveau | [TAB] Vue camera", 20f, 350f);
        batch.end();

        if (showPostUi) {
            OverlayGeometry geo = prepareOverlayGeometry();
            updateSliderBounds(geo);
            renderPostProcessOverlayPanel(geo);
            renderPostProcessOverlay(batch, geo);
        }

        if (postProcessor != null) {
            postProcessor.end(shaderTime);
        }

        if (pendingScreen != null) {
            game.setScreen(pendingScreen);
        }
    }

    private void initializePostProcessControls() {
        if (sliderControls == null) {
            sliderControls = new ArrayList<>();
        } else {
            sliderControls.clear();
        }
        sliderControls.add(new SliderControl("Barrel", PostProcessSettings.MIN_BARREL, PostProcessSettings.MAX_BARREL,
            () -> postSettings.barrelStrength, value -> postSettings.barrelStrength = value));
        sliderControls.add(new SliderControl("Scanlines", PostProcessSettings.MIN_SCANLINE, PostProcessSettings.MAX_SCANLINE,
            () -> postSettings.scanlineIntensity, value -> postSettings.scanlineIntensity = value));
        sliderControls.add(new SliderControl("Shadow Mask", PostProcessSettings.MIN_MASK, PostProcessSettings.MAX_MASK,
            () -> postSettings.maskIntensity, value -> postSettings.maskIntensity = value));
        sliderControls.add(new SliderControl("Vignette", PostProcessSettings.MIN_VIGNETTE, PostProcessSettings.MAX_VIGNETTE,
            () -> postSettings.vignetteIntensity, value -> postSettings.vignetteIntensity = value));
        sliderControls.add(new SliderControl("Chroma", PostProcessSettings.MIN_CHROMA, PostProcessSettings.MAX_CHROMA,
            () -> postSettings.chromaAmount, value -> postSettings.chromaAmount = value));
        sliderControls.add(new SliderControl("Noise", PostProcessSettings.MIN_NOISE, PostProcessSettings.MAX_NOISE,
            () -> postSettings.noiseAmount, value -> postSettings.noiseAmount = value));
        sliderControls.add(new SliderControl("Flicker", PostProcessSettings.MIN_FLICKER, PostProcessSettings.MAX_FLICKER,
            () -> postSettings.flickerAmount, value -> postSettings.flickerAmount = value));
        activeSlider = null;
    }

    private void handlePostProcessUiInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showPostUi = !showPostUi;
            if (!showPostUi) {
                activeSlider = null;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            postSettings = new PostProcessSettings();
            postSettings.clampAll();
            initializePostProcessControls();
            if (postProcessor != null) {
                postProcessor.setSettings(postSettings);
            }
        }
        if (!showPostUi || postSettings == null || sliderControls == null || sliderControls.isEmpty()) {
            activeSlider = null;
            return;
        }

        OverlayGeometry geo = prepareOverlayGeometry();
        geo.panelLeft = 32f;
        geo.panelTop = geo.screenHeight - 96f;
        updateSliderBounds(geo);

        boolean leftPressed = Gdx.input.isButtonPressed(Buttons.LEFT);
        if (!leftPressed) {
            activeSlider = null;
            return;
        }

        float pointerX = Gdx.input.getX();
        float pointerY = geo.screenHeight - Gdx.input.getY();

        if (pointerX < geo.panelLeft - 16f || pointerX > geo.panelLeft + geo.panelWidth + 16f
                || pointerY > geo.panelTop + 16f || pointerY < geo.panelBottom - 16f) {
            activeSlider = null;
            return;
        }

        if (activeSlider == null) {
            for (SliderControl slider : sliderControls) {
                if (slider.contains(pointerX, pointerY)) {
                    activeSlider = slider;
                    break;
                }
            }
        }

        if (activeSlider != null) {
            float width = activeSlider.areaRight - activeSlider.areaLeft;
            if (width > 0f) {
                float t = MathUtils.clamp((pointerX - activeSlider.areaLeft) / width, 0f, 1f);
                float value = MathUtils.lerp(activeSlider.min, activeSlider.max, t);
                activeSlider.setter.accept(value);
                postSettings.clampAll();
                if (postProcessor != null) {
                    postProcessor.setSettings(postSettings);
                }
            }
        }
    }

    private OverlayGeometry prepareOverlayGeometry() {
        overlayGeometry.screenWidth = Gdx.graphics.getWidth();
        overlayGeometry.screenHeight = Gdx.graphics.getHeight();
        overlayGeometry.panelPadding = 18f;
        overlayGeometry.spacing = 34f;
        overlayGeometry.headerHeight = 42f;
        overlayGeometry.sliderWidth = Math.min(360f, overlayGeometry.screenWidth - 280f);
        overlayGeometry.sliderBarHeight = 20f;
        overlayGeometry.panelLeft = 32f;
        overlayGeometry.panelTop = overlayGeometry.screenHeight - 72f;
        overlayGeometry.sliderLeft = overlayGeometry.panelLeft + 220f;
        if (overlayGeometry.sliderLeft + overlayGeometry.sliderWidth + 36f > overlayGeometry.screenWidth) {
            overlayGeometry.sliderWidth = overlayGeometry.screenWidth - overlayGeometry.sliderLeft - 36f;
        }
        overlayGeometry.panelWidth = (overlayGeometry.sliderLeft - overlayGeometry.panelLeft) + overlayGeometry.sliderWidth + 52f;
        overlayGeometry.sliderCount = sliderControls != null ? sliderControls.size() : 0;
        screenMatrix.setToOrtho2D(0f, 0f, overlayGeometry.screenWidth, overlayGeometry.screenHeight);
        return overlayGeometry;
    }

    private void updateSliderBounds(OverlayGeometry geo) {
        if (sliderControls == null) {
            geo.panelHeight = 0f;
            geo.panelBottom = geo.panelTop;
            return;
        }
        float textY = geo.panelTop - geo.headerHeight;
        for (SliderControl slider : sliderControls) {
            slider.textY = textY;
            slider.barCenterY = textY - geo.spacing * 0.4f;
            slider.areaLeft = geo.sliderLeft;
            slider.areaRight = geo.sliderLeft + geo.sliderWidth;
            slider.areaBottom = slider.barCenterY - geo.sliderBarHeight * 0.5f;
            slider.areaTop = slider.barCenterY + geo.sliderBarHeight * 0.5f;
            textY -= geo.spacing;
        }
        geo.panelHeight = geo.headerHeight + sliderControls.size() * geo.spacing + geo.panelPadding;
        geo.panelBottom = geo.panelTop - geo.panelHeight;
    }

    private void renderPostProcessOverlayPanel(OverlayGeometry geo) {
        if (sliderControls == null || sliderControls.isEmpty()) {
            return;
        }
        screenMatrix.setToOrtho2D(0f, 0f, geo.screenWidth, geo.screenHeight);
        Matrix4 previousMatrix = new Matrix4(shapeRenderer.getProjectionMatrix());
        shapeRenderer.setProjectionMatrix(screenMatrix);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float backgroundLeft = geo.panelLeft - geo.panelPadding;
        float backgroundBottom = geo.panelBottom - geo.panelPadding;
        float backgroundWidth = geo.panelWidth + geo.panelPadding * 2f;
        float backgroundHeight = geo.panelHeight + geo.panelPadding * 2f;
        shapeRenderer.setColor(0f, 0f, 0f, 0.65f);
        shapeRenderer.rect(backgroundLeft, backgroundBottom, backgroundWidth, backgroundHeight);
        shapeRenderer.setColor(0.85f, 0.85f, 0.9f, 0.8f);
        shapeRenderer.rect(backgroundLeft, backgroundBottom + backgroundHeight - 2f, backgroundWidth, 2f);
        for (SliderControl slider : sliderControls) {
            shapeRenderer.setColor(0.18f, 0.18f, 0.22f, 0.85f);
            shapeRenderer.rect(slider.areaLeft, slider.areaBottom, geo.sliderWidth, geo.sliderBarHeight);
            float ratio = MathUtils.clamp((slider.getter.get() - slider.min) / (slider.max - slider.min), 0f, 1f);
            shapeRenderer.setColor(0.55f, 0.78f, 1f, 0.9f);
            shapeRenderer.rect(slider.areaLeft, slider.areaBottom, geo.sliderWidth * ratio, geo.sliderBarHeight);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(previousMatrix);
    }

    private void renderPostProcessOverlay(SpriteBatch batch, OverlayGeometry geo) {
        if (postSettings == null || sliderControls == null || sliderControls.isEmpty()) {
            return;
        }
        screenMatrix.setToOrtho2D(0f, 0f, geo.screenWidth, geo.screenHeight);
        Matrix4 previousMatrix = new Matrix4(batch.getProjectionMatrix());
        batch.setProjectionMatrix(screenMatrix);
        batch.begin();
        float prevScaleX = font.getData().scaleX;
        float prevScaleY = font.getData().scaleY;
        Color prevColor = new Color(font.getColor());
        font.getData().setScale(0.95f, 0.95f);
        font.setColor(0.95f, 0.95f, 0.85f, 1f);

        float headerY = geo.panelTop;
        float textLeft = geo.panelLeft;
        font.draw(batch, "CRT tuning (F1 bascule, F2 reset)", textLeft, headerY);
        headerY -= geo.spacing * 0.6f;
        font.draw(batch, "Cliquez et glissez les curseurs", textLeft, headerY);

        for (SliderControl slider : sliderControls) {
            float value = slider.getter.get();
            String bar = PostProcessSettings.createBar(value, slider.min, slider.max);
            font.draw(batch, String.format("%s %s  %.3f", slider.label, bar, value), textLeft, slider.textY);
        }

        font.getData().setScale(prevScaleX, prevScaleY);
        font.setColor(prevColor);
        batch.end();
        batch.setProjectionMatrix(previousMatrix);
    }

    private static class SliderControl {
        final String label;
        final float min;
        final float max;
        final Supplier<Float> getter;
        final Consumer<Float> setter;
        float areaLeft;
        float areaRight;
        float areaTop;
        float areaBottom;
        float textY;
        float barCenterY;

        SliderControl(String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
            this.label = label;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
        }

        boolean contains(float x, float y) {
            return x >= areaLeft && x <= areaRight && y >= areaBottom && y <= areaTop;
        }
    }

    private static class OverlayGeometry {
        float screenWidth;
        float screenHeight;
        float panelLeft;
        float panelTop;
        float panelWidth;
        float panelHeight;
        float panelBottom;
        float panelPadding;
        float sliderLeft;
        float sliderWidth;
        float sliderBarHeight;
        float spacing;
        float headerHeight;
        int sliderCount;
    }

    @Override
    public void resize(int width, int height) {
        if (postProcessor != null) {
            postProcessor.resize(width, height);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (backgroundRenderer != null) {
            backgroundRenderer.dispose();
            backgroundRenderer = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (particleSystem != null) {
            particleSystem.dispose();
            particleSystem = null;
        }
        if (postProcessor != null) {
            postProcessor.dispose();
            postProcessor = null;
        }
        if (gs != null) {
            gs.player.dispose();
        }
    }
}








