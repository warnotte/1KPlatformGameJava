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
    public float originalX, originalY;
    public float moveRange = 100f;
    public float moveSpeed = 30f;
    public int hitCount = 0;
    public int maxHits = 3;
    public float timer = 0f;
    public boolean visible = true;
    public float conveyorSpeed = 50f;

    // Breakable tuning
    public float breakTotalTime = 2.5f;
    private float breakContactTime = 0f;

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
                x = originalX + (float) Math.sin(timer * moveSpeed / 30f) * moveRange;
                break;
            case MOVING_VERTICAL:
                y = originalY + (float) Math.sin(timer * moveSpeed / 30f) * (moveRange * 0.5f);
                break;
            case DISAPPEARING:
                float cycle = timer % 3f;
                visible = cycle < 2f;
                break;
            default:
                break;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!visible || (type == PlatformType.BREAKABLE && hitCount >= maxHits)) {
            return;
        }

        Color platformColor = getPlatformColor();
        shapeRenderer.setColor(platformColor);
        shapeRenderer.rect(x, y, width, height);
        renderSpecialEffects(shapeRenderer);
    }

    private Color getPlatformColor() {
        switch (type) {
            case SOLID:
                return new Color(0.6f, 0.4f, 0.2f, 1f);
            case MOVING_HORIZONTAL:
                return new Color(0.2f, 0.4f, 0.8f, 1f);
            case MOVING_VERTICAL:
                return new Color(0.2f, 0.7f, 0.3f, 1f);
            case BREAKABLE: {
                float damage = getBreakProgress();
                float g = Math.max(0.2f, 0.6f - damage * 0.55f);
                float b = Math.max(0.0f, 0.2f - damage * 0.2f);
                return new Color(1f, g, b, 1f);
            }
            case BOUNCY: {
                float pulse = (float) (Math.sin(timer * 5f) * 0.2f + 0.8f);
                return new Color(1f, pulse * 0.4f, pulse, 1f);
            }
            case ICE:
                return new Color(0.7f, 0.9f, 1f, 0.8f);
            case CONVEYOR:
                return new Color(0.6f, 0.3f, 0.8f, 1f);
            case DISAPPEARING: {
                float cycle = timer % 3f;
                float alpha = 1f;
                if (cycle > 1.5f && cycle < 2f) {
                    alpha = (2f - cycle) * 2f;
                } else if (cycle > 2f && cycle < 2.5f) {
                    alpha = (cycle - 2f) * 2f;
                }
                return new Color(1f, 1f, 0.3f, alpha);
            }
            default:
                return Color.GRAY;
        }
    }

    private void renderSpecialEffects(ShapeRenderer shapeRenderer) {
        switch (type) {
            case ICE:
                shapeRenderer.setColor(1f, 1f, 1f, 0.6f);
                shapeRenderer.rect(x + 2f, y + height - 3f, width - 4f, 2f);
                break;
            case CONVEYOR:
                shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
                float arrowY = y + height / 2f;
                boolean goingRight = conveyorSpeed > 0f;
                for (int i = 0; i < 3; i++) {
                    float arrowX = x + 10f + i * (width - 20f) / 2f;
                    if (goingRight) {
                        shapeRenderer.triangle(
                            arrowX, arrowY - 3f,
                            arrowX + 6f, arrowY,
                            arrowX, arrowY + 3f
                        );
                    } else {
                        shapeRenderer.triangle(
                            arrowX + 6f, arrowY - 3f,
                            arrowX, arrowY,
                            arrowX + 6f, arrowY + 3f
                        );
                    }
                }
                break;
            case BOUNCY:
                shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 1f);
                for (int i = 0; i < 3; i++) {
                    float springY = y + 3f + i * 4f;
                    shapeRenderer.rect(x + 2f, springY, 3f, 2f);
                    shapeRenderer.rect(x + width - 5f, springY, 3f, 2f);
                }
                break;
            case BREAKABLE:
                float progress = getBreakProgress();
                if (progress > 0f) {
                    shapeRenderer.setColor(1f, 0.3f, 0.1f, 0.2f * (0.5f + progress));
                    shapeRenderer.rect(x, y, width, height);
                    int cracks = Math.min(maxHits, Math.max(1, (int) Math.ceil(progress * maxHits)));
                    shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.45f + 0.35f * progress);
                    for (int i = 0; i < cracks; i++) {
                        float fraction = (float) (i + 1) / (cracks + 1);
                        float crackX = x + 5f + fraction * (width - 10f);
                        shapeRenderer.rect(crackX, y + 2f, 1f, height - 4f);
                        shapeRenderer.rect(crackX - 3f, y + height / 2f, 6f, 1f);
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean intersects(float playerX, float playerY, float playerWidth, float playerHeight) {
        if (!visible || (type == PlatformType.BREAKABLE && hitCount >= maxHits)) {
            return false;
        }
        return playerX < x + width &&
               playerX + playerWidth > x &&
               playerY < y + height &&
               playerY + playerHeight > y;
    }

    public void handlePlayerContact(PlayerGDX player, float delta, boolean landedThisFrame, ParticleSystem particles) {
        switch (type) {
            case BREAKABLE:
                if (hitCount >= maxHits) {
                    return;
                }
                float stageDuration = getBreakStageDuration();
                boolean stageTriggered = false;
                breakContactTime += delta;
                while (breakContactTime >= stageDuration && hitCount < maxHits) {
                    breakContactTime -= stageDuration;
                    hitCount++;
                    stageTriggered = true;
                    if (particles != null) {
                        particles.spawnFragments(player.x, y + height, new Color(1f, 0.7f, 0.35f, 1f), 6 + hitCount * 2, ParticleSystem.ParticleLayer.FOREGROUND);
                    }
                    if (hitCount >= maxHits) {
                        visible = false;
                        player.onGround = false;
                        player.groundPlatform = null;
                        player.isJumping = true;
                        player.timeOnGround = 0f;
                        player.vy = Math.min(player.vy, -1f);
                        breakContactTime = stageDuration;
                        if (particles != null) {
                            particles.spawnFragments(x + width / 2f, y + height / 2f, new Color(1f, 0.45f, 0.15f, 1f), 22, ParticleSystem.ParticleLayer.FOREGROUND);
                        }
                        break;
                    }
                }
                if (stageTriggered && hitCount < maxHits && particles != null) {
                    particles.spawnDust(player.x, y + height, new Color(1f, 0.65f, 0.35f, 1f), ParticleSystem.ParticleLayer.FOREGROUND);
                }
                break;
            case BOUNCY:
                if (landedThisFrame) {
                    player.vy = 15f;
                    player.isJumping = true;
                    player.onGround = false;
                    player.groundPlatform = null;
                    player.timeOnGround = 0f;
                    if (particles != null) {
                        particles.spawnDust(player.x, y + height, new Color(1f, 0.55f, 0.85f, 1f), ParticleSystem.ParticleLayer.FOREGROUND);
                    }
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
