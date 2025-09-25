package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameStateGDX {
    public PlayerGDX player;
    public Level level;
    public GameStateGDX() {
        player = new PlayerGDX();
        //t  level = TestLevelGenerator.createTestLevel(); // Niveau de test - à réactiver pour débugger
        level = new LevelGenerator(System.currentTimeMillis()).generateLevel(); // Générateur amélioré
    }
}
