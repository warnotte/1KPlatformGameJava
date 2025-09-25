package com.warnotte.pf1kwaxgdx;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private List<Platform> platforms;
    private float levelWidth;
    private float levelHeight;

    public Level() {
        platforms = new ArrayList<>();
        levelWidth = 1000f; // Largeur par défaut
        levelHeight = 500f; // Hauteur par défaut
    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    public void update(float delta) {
        // Mettre à jour toutes les plateformes (animations, mouvements)
        for (Platform platform : platforms) {
            platform.update(delta);
        }
    }

    public void render(com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        for (Platform platform : platforms) {
            platform.render(shapeRenderer);
        }
        shapeRenderer.end();
    }

    // Collision detection avec le joueur
    public PlatformCollisionResult checkCollisions(PlayerGDX player, float newX, float newY) {
        PlatformCollisionResult result = new PlatformCollisionResult();
        result.newX = newX;
        result.newY = newY;
        result.onGround = false;
        result.contactPlatform = null;

        float playerWidth = 6f;  // Largeur approximative du joueur
        float playerHeight = 6f; // Hauteur approximative du joueur

        for (Platform platform : platforms) {
            if (platform.intersects(newX - playerWidth/2, newY, playerWidth, playerHeight)) {

                // Déterminer le type de collision
                float overlapLeft = (newX + playerWidth/2) - platform.x;
                float overlapRight = (platform.x + platform.width) - (newX - playerWidth/2);
                float overlapTop = (newY + playerHeight) - platform.y;
                float overlapBottom = (platform.y + platform.height) - newY;

                // Trouver la plus petite pénétration
                float minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                          Math.min(overlapTop, overlapBottom));

                if (minOverlap == overlapBottom && player.vy <= 0) {
                    // Collision par le haut (joueur atterrit)
                    result.newY = platform.y + platform.height;
                    result.onGround = true;
                    result.contactPlatform = platform;
                    player.vy = 0f;
                    player.isJumping = false;

                    // Traitement spécial pour la plateforme de glace
                    if (platform.type != Platform.PlatformType.ICE) {
                        // Plateforme normale - arrêt complet
                    } else {
                        // Glace - conserver une partie du momentum horizontal
                        // (sera géré dans GameScreen)
                    }

                } else if (minOverlap == overlapTop && player.vy >= 0) {
                    // Collision par le bas (joueur frappe le plafond)
                    result.newY = platform.y - playerHeight;
                    player.vy = 0f;

                } else if (minOverlap == overlapLeft) {
                    // Collision par la droite
                    result.newX = platform.x - playerWidth/2;

                } else if (minOverlap == overlapRight) {
                    // Collision par la gauche
                    result.newX = platform.x + platform.width + playerWidth/2;
                }

                // Actions spéciales de la plateforme
                if (result.onGround) {
                    platform.onPlayerContact(player);
                }
            }
        }

        return result;
    }

    // Trouver la plateforme la plus proche sous une position X
    public Platform getNearestPlatformBelow(float x, float y) {
        Platform nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Platform platform : platforms) {
            if (x >= platform.x && x <= platform.x + platform.width &&
                platform.y < y) {
                float distance = y - (platform.y + platform.height);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = platform;
                }
            }
        }

        return nearest;
    }

    // Classe pour le résultat des collisions
    public static class PlatformCollisionResult {
        public float newX, newY;
        public boolean onGround;
        public Platform contactPlatform;
    }

    // Getters
    public List<Platform> getPlatforms() {
        return platforms;
    }

    public float getLevelWidth() {
        return levelWidth;
    }

    public void setLevelWidth(float levelWidth) {
        this.levelWidth = levelWidth;
    }

    public float getLevelHeight() {
        return levelHeight;
    }

    public void setLevelHeight(float levelHeight) {
        this.levelHeight = levelHeight;
    }
}