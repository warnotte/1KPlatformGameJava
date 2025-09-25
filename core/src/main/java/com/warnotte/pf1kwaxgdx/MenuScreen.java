package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Input;

public class MenuScreen implements Screen {
    private final GameMain game;
    private SpriteBatch batch;
    private BitmapFont font;
    private int selected = 0; // 0 = Jouer, 1 = Quitter
    private String[] options = {"Jouer", "Quitter"};
    private float inputCooldown = 0;
    private String message = null;

    public MenuScreen(GameMain game) {
        this(game, null);
    }

    public MenuScreen(GameMain game, String message) {
        this.game = game;
        this.message = message;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        inputCooldown -= delta;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.getData().setScale(2);
        font.draw(batch, "1K Platform Game", 100, 350);
        if (message != null) {
            font.setColor(1, 0.2f, 0.2f, 1);
            font.draw(batch, message, 100, 310);
        }
        for (int i = 0; i < options.length; i++) {
            if (i == selected) {
                font.setColor(1, 1, 0, 1);
            } else {
                font.setColor(1, 1, 1, 1);
            }
            font.draw(batch, options[i], 120, 250 - i * 50);
        }
        batch.end();
        handleInput();
    }

    private void handleInput() {
        if (inputCooldown > 0) return;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selected = (selected + options.length - 1) % options.length;
            inputCooldown = 0.15f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selected = (selected + 1) % options.length;
            inputCooldown = 0.15f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selected == 0) {
                game.setScreen(new GameScreen(game));
            } else if (selected == 1) {
                Gdx.app.exit();
            }
            inputCooldown = 0.2f;
        }
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {
        batch.dispose();
        font.dispose();
    }
    @Override
    public void dispose() {}
}
