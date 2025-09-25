package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Platform {

    public enum PlatformType {
        SOLID,             // Marron - Plateforme classique
        MOVING_HORIZONTAL, // Bleu - Bouge horizontalement
        MOVING_VERTICAL,   // Vert - Monte et descend
        BREAKABLE,         // Orange - Se casse après passages
        BOUNCY,            // Rose - Trampoline
        ICE,               // Cyan - Glissante
        CONVEYOR,          // Violet - Tapis roulant
        DISAPPEARING       // Jaune - Apparaît/disparaît
    }

    // Position et dimensions
    public float x, y, width, height;
    public PlatformType type;

    // Propriétés spécifiques selon le type
    public float originalX, originalY;      // Position de départ pour plateformes mobiles
    public float moveRange = 100f;          // Distance de déplacement
    public float moveSpeed = 30f;           // Vitesse de déplacement
    public int hitCount = 0;                // Nombre de fois touchée (pour BREAKABLE)
    public int maxHits = 3;                 // Hits max avant destruction
    public float timer = 0f;                // Timer pour animations/cycles
    public boolean visible = true;          // Pour plateformes qui disparaissent
    public float conveyorSpeed = 50f;       // Vitesse du tapis roulant

    // Breakable platform tuning
    public float breakTotalTime = 2.5f;     // Temps total (en secondes) avant de casser si on reste dessus
    private float breakContactTime = 0f;    // Temps accumulé sur l'étape courante

    public Platform(float x, float y, float width, float height, PlatformType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.originalX = x;
        this.originalY = y;
    }

    public void update(float delta) {
        timer += delta;

        switch (type) {
            case MOVING_HORIZONTAL:
                // Mouvement sinusoïdal horizontal
                x = originalX + (float) Math.sin(timer * moveSpeed / 30f) * moveRange;
                break;

            case MOVING_VERTICAL:
                // Mouvement sinusoïdal vertical
                y = originalY + (float) Math.sin(timer * moveSpeed / 30f) * (moveRange * 0.5f);
                break;

            case DISAPPEARING:
                // Cycle apparition/disparition (2 sec visible, 1 sec invisible)
                float cycle = timer % 3f;
                visible = cycle < 2f;
                break;

            default:
                // Autres types n'ont pas d'animation automatique
                break;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!visible || (type == PlatformType.BREAKABLE && hitCount >= maxHits)) {
            return; // Ne pas dessiner si invisible ou cassée
        }

        // Couleur selon le type
        Color platformColor = getPlatformColor();
        shapeRenderer.setColor(platformColor);

        // Corps principal de la plateforme
        shapeRenderer.rect(x, y, width, height);

        // Effets visuels spéciaux selon le type
        renderSpecialEffects(shapeRenderer);
    }

    private Color getPlatformColor() {
        switch (type) {
            case SOLID:
                return new Color(0.6f, 0.4f, 0.2f, 1f);    // Marron
            case MOVING_HORIZONTAL:
                return new Color(0.2f, 0.4f, 0.8f, 1f);    // Bleu
            case MOVING_VERTICAL:
                return new Color(0.2f, 0.7f, 0.3f, 1f);    // Vert
            case BREAKABLE: {
                float damage = getBreakProgress();
                float g = Math.max(0.2f, 0.6f - damage * 0.5f);
                float b = Math.max(0.0f, 0.2f - damage * 0.2f);
                return new Color(1f, g, b, 1f);            // Orange -> Rouge
            }
            case BOUNCY:
                // Pulsation rose
                float pulse = (float) (Math.sin(timer * 5) * 0.2f + 0.8f);
                return new Color(1f, pulse * 0.4f, pulse, 1f); // Rose pulsant
            case ICE:
                return new Color(0.7f, 0.9f, 1f, 0.8f);    // Cyan translucide
            case CONVEYOR:
                return new Color(0.6f, 0.3f, 0.8f, 1f);    // Violet
            case DISAPPEARING: {
                float cycle = timer % 3f;
                float alpha = 1f;
                if (cycle > 1.5f && cycle < 2f) {
                    alpha = (2f - cycle) * 2f; // Disparition graduelle
                } else if (cycle > 2f && cycle < 2.5f) {
                    alpha = (cycle - 2f) * 2f; // Apparition graduelle
                }
                return new Color(1f, 1f, 0.3f, alpha);     // Jaune avec transparence
            }
            default:
                return Color.GRAY;
        }
    }

    private void renderSpecialEffects(ShapeRenderer shapeRenderer) {
        switch (type) {
            case ICE:
                // Effet brillant sur la glace
                shapeRenderer.setColor(1f, 1f, 1f, 0.6f);
                shapeRenderer.rect(x + 2, y + height - 3, width - 4, 2);
                break;

            case CONVEYOR:
                // Flèches pour montrer la direction
                shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
                float arrowY = y + height / 2;
                boolean goingRight = conveyorSpeed > 0;

                for (int i = 0; i < 3; i++) {
                    float arrowX = x + 10 + i * (width - 20) / 2;

                    if (goingRight) {
                        // Flèche vers la droite
                        shapeRenderer.triangle(
                            arrowX, arrowY - 3,
                            arrowX + 6, arrowY,
                            arrowX, arrowY + 3
                        );
                    } else {
                        // Flèche vers la gauche
                        shapeRenderer.triangle(
                            arrowX + 6, arrowY - 3,
                            arrowX, arrowY,
                            arrowX + 6, arrowY + 3
                        );
                    }
                }
                break;

            case BOUNCY:
                // Ressorts sur les côtés
                shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
                // Ressort gauche
                for (int i = 0; i < 3; i++) {
                    float springY = y + 3 + i * 4;
                    shapeRenderer.rect(x + 2, springY, 3, 2);
                }
                // Ressort droit
                for (int i = 0; i < 3; i++) {
                    float springY = y + 3 + i * 4;
                    shapeRenderer.rect(x + width - 5, springY, 3, 2);
                }
                break;

            case BREAKABLE: {
                float progress = getBreakProgress();
                if (progress > 0f) {
                    shapeRenderer.setColor(1f, 0.3f, 0.1f, 0.25f * progress);
                    shapeRenderer.rect(x, y, width, height);

                    int cracks = Math.min(maxHits, Math.max(1, (int) Math.ceil(progress * maxHits)));
                    shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.5f + 0.3f * progress);
                    for (int i = 0; i < cracks; i++) {
                        float fraction = (float) (i + 1) / (cracks + 1);
                        float crackX = x + 5 + fraction * (width - 10);
                        shapeRenderer.rect(crackX, y + 2, 1, height - 4);
                        shapeRenderer.rect(crackX - 3, y + height / 2, 6, 1);
                    }
                }
                break;
            }

            default:
                break;
        }
    }

    // Vérifier collision avec le joueur
    public boolean intersects(float playerX, float playerY, float playerWidth, float playerHeight) {
        if (!visible || (type == PlatformType.BREAKABLE && hitCount >= maxHits)) {
            return false;
        }

        return playerX < x + width &&
               playerX + playerWidth > x &&
               playerY < y + height &&
               playerY + playerHeight > y;
    }

    public void handlePlayerContact(PlayerGDX player, float delta, boolean landedThisFrame) {
        switch (type) {
            case BREAKABLE:
                if (hitCount >= maxHits) {
                    return;
                }
                breakContactTime += delta;
                float stageDuration = getBreakStageDuration();
                while (breakContactTime >= stageDuration && hitCount < maxHits) {
                    breakContactTime -= stageDuration;
                    hitCount++;
                    if (hitCount >= maxHits) {
                        visible = false;
                        player.onGround = false;
                        player.groundPlatform = null;
                        player.isJumping = true;
                        player.timeOnGround = 0f;
                        player.vy = Math.min(player.vy, -1f);
                        breakContactTime = stageDuration;
                        break;
                    }
                }
                break;

            case BOUNCY:
                if (landedThisFrame) {
                    player.vy = 15f; // Plus fort que le saut normal
                    player.isJumping = true;
                    player.onGround = false;
                    player.groundPlatform = null;
                    player.timeOnGround = 0f;
                }
                break;

            case CONVEYOR:
                if (player.onGround) {
                    player.x += conveyorSpeed * delta;
                }
                break;

            default:
                break;
        }
    }

    public void onPlayerLeave(PlayerGDX player) {
        if (type == PlatformType.BREAKABLE && hitCount < maxHits) {
            float stageDuration = getBreakStageDuration();
            breakContactTime = Math.min(breakContactTime, stageDuration * 0.8f);
        }
    }

    private float getBreakStageDuration() {
        int stages = Math.max(1, maxHits);
        return breakTotalTime / stages;
    }

    private float getBreakProgressRaw() {
        if (type != PlatformType.BREAKABLE) {
            return 0f;
        }
        float stageDuration = getBreakStageDuration();
        return hitCount + (stageDuration <= 0f ? 0f : breakContactTime / stageDuration);
    }

    public float getBreakProgress() {
        if (type != PlatformType.BREAKABLE) {
            return 0f;
        }
        float totalStages = Math.max(1, maxHits);
        float progress = getBreakProgressRaw() / totalStages;
        return Math.min(progress, 1f);
    }
}
