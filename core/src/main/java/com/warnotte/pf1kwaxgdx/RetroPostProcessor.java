package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

public class RetroPostProcessor implements Disposable {
    private FrameBuffer frameBuffer;
    private TextureRegion frameRegion;
    private final SpriteBatch screenBatch;
    private final Matrix4 projection = new Matrix4();
    private ShaderProgram shader;
    private int width;
    private int height;
    private PostProcessSettings settings;

    public RetroPostProcessor(int width, int height, PostProcessSettings settings) {
        this.screenBatch = new SpriteBatch();
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(
            Gdx.files.internal("shaders/retro.vert"),
            Gdx.files.internal("shaders/retro.frag")
        );
        if (!shader.isCompiled()) {
            throw new IllegalStateException("Retro shader compilation failed: " + shader.getLog());
        }
        this.settings = settings;
        resize(width, height);
    }

    public void setSettings(PostProcessSettings settings) {
        this.settings = settings;
    }

    public void begin() {
        if (frameBuffer == null) {
            return;
        }
        frameBuffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void end(float timeSeconds) {
        if (frameBuffer == null) {
            return;
        }
        frameBuffer.end();
        Texture texture = frameBuffer.getColorBufferTexture();
        frameRegion.setTexture(texture);
        shader.bind();
        shader.setUniformf("u_resolution", (float) width, (float) height);
        shader.setUniformf("u_time", timeSeconds);
        if (settings != null) {
            shader.setUniformf("u_barrelStrength", settings.barrelStrength);
            shader.setUniformf("u_scanlineIntensity", settings.scanlineIntensity);
            shader.setUniformf("u_maskIntensity", settings.maskIntensity);
            shader.setUniformf("u_vignetteIntensity", settings.vignetteIntensity);
            shader.setUniformf("u_chromaAmount", settings.chromaAmount);
            shader.setUniformf("u_flickerAmount", settings.flickerAmount);
            shader.setUniformf("u_noiseAmount", settings.noiseAmount);
        }

        screenBatch.setProjectionMatrix(projection);
        screenBatch.setShader(shader);
        screenBatch.begin();
        screenBatch.draw(frameRegion, 0f, 0f, width, height);
        screenBatch.end();
        screenBatch.setShader(null);
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (this.width == width && this.height == height && frameBuffer != null) {
            return;
        }
        disposeFrame();
        this.width = width;
        this.height = height;
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        frameRegion = new TextureRegion(frameBuffer.getColorBufferTexture());
        frameRegion.flip(false, true);
        projection.setToOrtho2D(0f, 0f, width, height);
    }

    private void disposeFrame() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
            frameBuffer = null;
        }
        frameRegion = null;
    }

    @Override
    public void dispose() {
        disposeFrame();
        screenBatch.dispose();
        if (shader != null) {
            shader.dispose();
            shader = null;
        }
    }
}
