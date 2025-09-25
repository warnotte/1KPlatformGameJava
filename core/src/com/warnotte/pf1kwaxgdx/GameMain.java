package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class GameMain extends ApplicationAdapter {
    @Override
    public void create() {
        // Initialisation du jeu (chargement des assets, etc.)
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Logique et rendu du jeu
    }

    @Override
    public void dispose() {
        // Libération des ressources
    }
}
