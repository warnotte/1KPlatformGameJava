package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameScreen implements Screen {
    private final GameMain game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GameStateGDX gs;
    private OrthographicCamera camera;
    private boolean debugView = true; // true = vue large, false = vue centrée joueur
    private BackgroundRenderer backgroundRenderer;
    private boolean useTestLevel = false; // Bascule entre niveau test et généré

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
        gs.player.resetGroundState();
        lives = 3;
        // Caméra : vue large par défaut
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 900, 500); // Vue large (debug)
        camera.update();

        // Initialiser le rendu d'arrière-plan
        backgroundRenderer = new BackgroundRenderer(shapeRenderer);
    }

    @Override
    public void render(float delta) {
        // Bascule de mode caméra avec TAB
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            debugView = !debugView;
            if (debugView) {
                camera.setToOrtho(false, 900, 500); // Vue large
            } else {
                camera.setToOrtho(false, 320, 180); // Vue zoomée (modifiable)
            }
        }

        // Bascule entre niveau test et généré avec touche R (Restart)
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            useTestLevel = !useTestLevel;

            // Régénérer le niveau
            if (useTestLevel) {
                gs.level = TestLevelGenerator.createTestLevel();
            } else {
                gs.level = new LevelGenerator(System.currentTimeMillis()).generateLevel();
            }

            // Repositionner le joueur au début
            gs.player.x = 50;
            gs.player.y = 100;
            gs.player.vy = 0f;
            gs.player.isJumping = false;
            gs.player.resetGroundState();
            lives = 3;
        }

        // Mise à jour de la caméra
        if (!debugView) {
            // Suivi du joueur (centré, mais limité aux bords du niveau)
            float camX = gs.player.x;
            float camY = gs.player.y;
            float halfW = camera.viewportWidth / 2f;
            float halfH = camera.viewportHeight / 2f;
            float minX = halfW;
            float maxX = gs.level.getLevelWidth() - halfW;
            camX = Math.max(minX, Math.min(maxX, camX));
            camY = Math.max(halfH, Math.min(300, camY)); // Limite verticale simple
            camera.position.set(camX, camY, 0);
        } else {
            camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Mise à jour du niveau (plateformes animées)
        gs.level.update(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Gestion des entrées clavier ---

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

        // Gravité et vitesse verticale
        gs.player.vy -= gravity;
        if (gs.player.vy < -maxFallSpeed) {
            gs.player.vy = -maxFallSpeed;
        }
        float nextY = gs.player.y + gs.player.vy;

        // Gestion du saut
        if (up && !gs.player.isJumping) {
            Level.PlatformCollisionResult testJump = gs.level.checkCollisions(gs.player, gs.player.x, gs.player.y - 1);
            if (testJump.onGround) {
                gs.player.vy = jumpPower;
                gs.player.isJumping = true;
                nextY = gs.player.y + gs.player.vy; // Recalculer avec nouvelle vitesse
            }
        }

        boolean wasOnGround = gs.player.onGround;
        Platform previousPlatform = gs.player.groundPlatform;

        // V�rifier les collisions avec la nouvelle position
        Level.PlatformCollisionResult collision = gs.level.checkCollisions(gs.player, nextX, nextY);

        // Appliquer la nouvelle position
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

            contactPlatform.handlePlayerContact(gs.player, delta, landedThisFrame);

            if (gs.player.onGround) {
                if (contactPlatform.type == Platform.PlatformType.MOVING_HORIZONTAL) {
                    float platformDeltaX = (float) Math.cos(contactPlatform.timer * contactPlatform.moveSpeed / 30f)
                            * contactPlatform.moveRange * contactPlatform.moveSpeed / 30f * delta;
                    gs.player.x += platformDeltaX;
                }
                // Pour les plateformes verticales, le suivi est déjà géré par la collision
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

        // Si le joueur tombe tout en bas de l'écran, perdre une vie
        if (gs.player.y < -30) {
            lives--;
            if (lives > 0) {
                gs.player.x = 50; // Position de départ
                gs.player.y = 100;
                gs.player.vy = 0f;
                gs.player.isJumping = false;
                gs.player.resetGroundState();
            } else {
                game.setScreen(new MenuScreen(game, "Game Over"));
                return;
            }
        }

        // Si le joueur atteint la fin du niveau, victoire
        if (gs.player.x >= gs.level.getLevelWidth() - 150) {
            game.setScreen(new MenuScreen(game, "Good Game!"));
            return;
        }

        // Affichage de l'arrière-plan dynamique
        backgroundRenderer.render(camera, delta);

        // Affichage du niveau (plateformes)
        gs.level.render(shapeRenderer);

        // Affichage du joueur (sprite animé)
        batch.begin();
        gs.player.movingLeft = left && !right;
        gs.player.movingRight = right && !left;
        gs.player.render(batch, delta);
        batch.end();

        // Affichage des vies et de l'heure
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(1, 1, 1, 1);
        font.draw(batch, "Vies : " + lives, 20, 460);

        float currentTime = backgroundRenderer.getTimeOfDay();
        int hours = (int) currentTime;
        int minutes = (int) ((currentTime - hours) * 60);
        String timeString = String.format("%02d:%02d", hours, minutes);
        font.draw(batch, "Heure : " + timeString, 20, 420);

        String levelMode = useTestLevel ? "Mode: TEST" : "Mode: GENERE";
        font.draw(batch, levelMode, 20, 380);
        font.getData().setScale(1.0f);
        font.draw(batch, "[R] Changer niveau | [TAB] Vue camera", 20, 350);
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
