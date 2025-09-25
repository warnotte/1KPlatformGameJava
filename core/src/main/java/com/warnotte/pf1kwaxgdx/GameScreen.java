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

        // --- Logique d'évolution du joueur améliorée ---
        float speed = 2.2f;
        float jumpPower = 8.5f;
        float gravity = 0.45f;
        float maxFallSpeed = 8f;

        // Entrées clavier
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.Q);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // Gestion du saut
        if (up && !gs.player.isJumping && gs.player.y <= gs.level.getCase(gs.player.x).hauteur + 0.1f) {
            gs.player.vy = jumpPower;
            gs.player.isJumping = true;
        }

        // Mouvement horizontal avec collision
        float nextX = gs.player.x;
        if (left) {
            float testX = gs.player.x - speed;
            CaseGDX c = gs.level.getCase(testX);
            if (c != null && c.type != CaseGDX.TypeCase.HOLE) {
                nextX = testX;
            }
        }
        if (right) {
            float testX = gs.player.x + speed;
            CaseGDX c = gs.level.getCase(testX);
            if (c != null && c.type != CaseGDX.TypeCase.HOLE) {
                nextX = testX;
            }
        }
        gs.player.x = nextX;

        // Gravité et saut
        gs.player.vy -= gravity;
        if (gs.player.vy < -maxFallSpeed) gs.player.vy = -maxFallSpeed;
        gs.player.y += gs.player.vy;

        // Collision sol
        float ground = gs.level.getCase(gs.player.x).hauteur;
        if (gs.player.y <= ground) {
            gs.player.y = ground;
            gs.player.vy = 0f;
            gs.player.isJumping = false;
        }

        // Si le joueur tombe dans un trou ou sort du niveau, reset
        CaseGDX cUnder = gs.level.getCase(gs.player.x);
        if (cUnder == null || cUnder.type == CaseGDX.TypeCase.HOLE || gs.player.y < -30) {
            // Reset position
            gs.player.x = 0;
            gs.player.y = gs.level.getCase(0).hauteur;
            gs.player.vy = 0f;
            gs.player.isJumping = false;
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
