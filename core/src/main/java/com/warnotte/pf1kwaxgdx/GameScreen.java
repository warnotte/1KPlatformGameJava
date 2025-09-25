
package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Import des classes du projet
import com.warnotte.pf1kwaxgdx.GameMain;
import com.warnotte.pf1kwaxgdx.GameStateGDX;
import com.warnotte.pf1kwaxgdx.CaseGDX;
import com.warnotte.pf1kwaxgdx.MenuScreen;

public class GameScreen implements Screen {
    private final GameMain game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GameStateGDX gs;

    private int lives = 3;

    public GameScreen(GameMain game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        gs = new GameStateGDX();
        lives = 3;
    }

    @Override
    public void render(float delta) {

        // --- Gestion des cases dynamiques ---
        boolean playerColleDynamic = false;
        for (int i = 0; i < gs.level.getNbrCases(); i++) {
            CaseGDX c = gs.level.casesList.get(i);
            if (c.type == CaseGDX.TypeCase.DYNAMIC) {
                float caseX = i * gs.level.caseWidth;
                float caseCenter = caseX + gs.level.caseWidth / 2f;
                boolean playerOnCase = Math.abs(gs.player.x - caseCenter) < gs.level.caseWidth / 2f && Math.abs(gs.player.y - c.hauteur) < 0.1f && gs.player.vy == 0f;
                if (playerOnCase) {
                    // Descendre la case si le joueur est immobile dessus
                    c.hauteur -= 1.2f;
                    // Le joueur colle à la case descendante
                    gs.player.y = c.hauteur;
                    playerColleDynamic = true;
                } else {
                    // Remonter la case si le joueur n'est pas dessus
                    if (c.hauteur < c.hauteurInitiale) {
                        c.hauteur += 0.5f;
                        if (c.hauteur > c.hauteurInitiale) c.hauteur = c.hauteurInitiale;
                    }
                }
            }
        }
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

        // Gestion du saut : seulement si le joueur est sur une case solide (pas un trou)
        CaseGDX caseSousJoueur = gs.level.getCase(gs.player.x);
        boolean surSolide = caseSousJoueur != null && caseSousJoueur.type != CaseGDX.TypeCase.HOLE;
        if (up && !gs.player.isJumping && surSolide && gs.player.y <= caseSousJoueur.hauteur + 0.1f) {
            gs.player.vy = jumpPower;
            gs.player.isJumping = true;
        }



        // Mouvement horizontal libre (pas de blocage sur les trous)
        float playerCenterX = gs.player.x;
        CaseGDX currentCase = gs.level.getCase(playerCenterX);
        CaseGDX nextCaseLeft = gs.level.getCase(playerCenterX - speed);
        CaseGDX nextCaseRight = gs.level.getCase(playerCenterX + speed);
        float currentGround = currentCase != null ? currentCase.hauteur : 0;
        float nextGroundLeft = nextCaseLeft != null ? nextCaseLeft.hauteur : 0;
        float nextGroundRight = nextCaseRight != null ? nextCaseRight.hauteur : 0;
        float nextX = gs.player.x;
        float playerBottom = gs.player.y;
        float playerTop = gs.player.y + 6; // hauteur du carré

        if (left) {
            float testX = playerCenterX - speed;
            if (currentCase != null && currentCase.type == CaseGDX.TypeCase.HOLE) {
                // Autoriser le déplacement si la case suivante est aussi un trou
                if (nextCaseLeft != null && nextCaseLeft.type == CaseGDX.TypeCase.HOLE) {
                    nextX = testX;
                } else {
                    // Autoriser le passage si le joueur est en l'air (saut)
                    if (gs.player.vy > 0 || !gs.player.isJumping) {
                        // On saute ou tombe : autoriser le passage si la hauteur du joueur permet d'atterrir
                        if (nextCaseLeft != null && nextCaseLeft.type != CaseGDX.TypeCase.HOLE && playerBottom > nextGroundLeft) {
                            nextX = testX;
                        }
                    }
                }
            } else {
                if (nextGroundLeft - currentGround <= 6.5f || playerBottom > nextGroundLeft) {
                    nextX = testX;
                }
            }
        }
        if (right) {
            float testX = playerCenterX + speed;
            if (currentCase != null && currentCase.type == CaseGDX.TypeCase.HOLE) {
                if (nextCaseRight != null && nextCaseRight.type == CaseGDX.TypeCase.HOLE) {
                    nextX = testX;
                } else {
                    if (gs.player.vy > 0 || !gs.player.isJumping) {
                        if (nextCaseRight != null && nextCaseRight.type != CaseGDX.TypeCase.HOLE && playerBottom > nextGroundRight) {
                            nextX = testX;
                        }
                    }
                }
            } else {
                if (nextGroundRight - currentGround <= 6.5f || playerBottom > nextGroundRight) {
                    nextX = testX;
                }
            }
        }
        gs.player.x = nextX;

        // Gravité et saut
        gs.player.vy -= gravity;
        if (gs.player.vy < -maxFallSpeed) gs.player.vy = -maxFallSpeed;
        gs.player.y += gs.player.vy;

        // Collision sol uniquement s’il y a une case sous le joueur
        CaseGDX groundCase = gs.level.getCase(playerCenterX);
        float ground = (groundCase != null && groundCase.type != CaseGDX.TypeCase.HOLE) ? groundCase.hauteur : -1000f;
        boolean onGround = false;
        if (groundCase != null && groundCase.type != CaseGDX.TypeCase.HOLE && gs.player.y <= ground) {
            gs.player.y = ground;
            gs.player.vy = 0f;
            gs.player.isJumping = false;
            onGround = true;
        }

        // Si le joueur tombe tout en bas de l’écran, perdre une vie
        if (gs.player.y < -30) {
            lives--;
            if (lives > 0) {
                gs.player.x = 0;
                gs.player.y = gs.level.getCase(0).hauteur;
                gs.player.vy = 0f;
                gs.player.isJumping = false;
            } else {
                // Retour au menu principal avec message Game Over
                game.setScreen(new MenuScreen(game, "Game Over"));
                return;
            }
        }

        // Si le joueur atteint la dernière case, victoire
        int lastCaseIndex = gs.level.getNbrCases() - 1;
        float lastCaseX = lastCaseIndex * gs.level.caseWidth;
        if (gs.player.x >= lastCaseX) {
            game.setScreen(new MenuScreen(game, "Good Game!"));
            return;
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

    // Affichage du joueur (rectangle centré sur la position x du joueur)
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(1, 0, 0, 1);
    shapeRenderer.rect(gs.player.x - 3, gs.player.y, 6, 6);
    shapeRenderer.end();

    // Affichage des vies
    batch.begin();
    font.getData().setScale(1.5f);
    font.setColor(1, 1, 1, 1);
    font.draw(batch, "Vies : " + lives, 20, 460);
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
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        gs.player.dispose();
    }
}
