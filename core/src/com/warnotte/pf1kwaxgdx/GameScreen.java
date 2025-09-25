package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

// Version simplifiée de GameState pour la démo
class GameStateGDX {
    public PlayerGDX player;
    public GameStateGDX() {
        player = new PlayerGDX();
    }
}

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private GameStateGDX gs;

    @Override
    public void show() {
        batch = new SpriteBatch();
        gs = new GameStateGDX();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        gs.player.render(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

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
        batch.dispose();
        gs.player.dispose();
    }
}
