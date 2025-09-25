package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Version simplifiée de GameState pour la démo
class GameStateGDX {
    public PlayerGDX player;
    public LevelGDX level;
    public GameStateGDX() {
        player = new PlayerGDX();
        level = new LevelGeneratorSimpleGDX(25).generateLevel();
    }
}

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private GameStateGDX gs;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        gs = new GameStateGDX();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Gestion des entrées clavier ---
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.Q);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // --- Logique d'évolution du joueur ---
        float speed = 2f;
        if (left) gs.player.x -= speed;
        if (right) gs.player.x += speed;
        // Saut simple (pas de gravité avancée ici)
        if (up && gs.player.y <= gs.level.getCase(gs.player.x).hauteur + 0.1f) {
            gs.player.y += 18f;
        }
        // Gravité
        float ground = gs.level.getCase(gs.player.x).hauteur;
        if (gs.player.y > ground) {
            gs.player.y -= 2.5f;
            if (gs.player.y < ground) gs.player.y = ground;
        }

        // Affichage du niveau (cases)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < gs.level.getNbrCases(); i++) {
            CaseGDX c = gs.level.casesList.get(i);
            float x = i * gs.level.caseWidth;
            float y = 0;
            // Plateforme
            if (c.type == CaseGDX.TypeCase.STATIC) {
                shapeRenderer.setColor(0.8f, 0.67f, 0.4f, 1f); // marron clair
                shapeRenderer.rect(x, y, gs.level.caseWidth, c.hauteur);
            } else if (c.type == CaseGDX.TypeCase.DYNAMIC) {
                shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f); // vert
                shapeRenderer.rect(x, y, gs.level.caseWidth, c.hauteur);
            } // HOLE = rien
            // Arbre
            if (c.isTree && c.type != CaseGDX.TypeCase.HOLE) {
                shapeRenderer.setColor(0.5f, 0.25f, 0.1f, 1f); // tronc
                shapeRenderer.rect(x + gs.level.caseWidth/2 - 2, c.hauteur, 4, 10);
                shapeRenderer.setColor(0.1f, 0.7f, 0.1f, 1f); // feuillage
                shapeRenderer.circle(x + gs.level.caseWidth/2, c.hauteur + 12, 6);
            }
        }
        shapeRenderer.end();

        // Affichage du joueur (rectangle temporaire)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(gs.player.x + 5, gs.player.y, 6, 6);
        shapeRenderer.end();
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
        if (shapeRenderer != null) shapeRenderer.dispose();
        gs.player.dispose();
    }
}
