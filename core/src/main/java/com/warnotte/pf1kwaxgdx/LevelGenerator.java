package com.warnotte.pf1kwaxgdx;

import java.util.Random;

public class LevelGenerator {
    private Random random;
    private float levelWidth;
    private float maxJumpDistance = 120f; // Distance max que le joueur peut sauter
    private float maxJumpHeight = 80f;    // Hauteur max que le joueur peut atteindre

    public LevelGenerator(long seed) {
        this.random = new Random(seed);
        this.levelWidth = 1200f;
    }

    public Level generateLevel() {
        Level level = new Level();
        level.setLevelWidth(levelWidth);

        // Plateforme de départ (toujours solide)
        level.addPlatform(new Platform(0, 50, 80, 20, Platform.PlatformType.SOLID));

        float currentX = 100f;
        float currentY = 50f;

        // Générer les plateformes du niveau
        while (currentX < levelWidth - 150f) {
            Platform.PlatformType type = chooseRandomPlatformType();
            float[] nextPos = generateNextPlatformPosition(currentX, currentY);
            float nextX = nextPos[0];
            float nextY = nextPos[1];
            float width = getPlatformWidth(type);
            float height = getPlatformHeight(type);

            Platform platform = new Platform(nextX, nextY, width, height, type);
            customizePlatform(platform, type);

            level.addPlatform(platform);

            currentX = nextX;
            currentY = nextY;

            // Ajouter parfois des plateformes bonus difficiles d'accès
            if (random.nextFloat() < 0.3f && currentX > 300f) {
                addBonusPlatform(level, currentX, currentY);
            }
        }

        // Plateforme de fin (toujours solide et large)
        level.addPlatform(new Platform(levelWidth - 100, 70, 100, 25, Platform.PlatformType.SOLID));

        return level;
    }

    private Platform.PlatformType chooseRandomPlatformType() {
        float rand = random.nextFloat();

        // Distribution pondérée plus équilibrée - plus de plateformes normales
        if (rand < 0.5f) return Platform.PlatformType.SOLID;         // 50% - Base solide
        else if (rand < 0.65f) return Platform.PlatformType.MOVING_HORIZONTAL; // 15%
        else if (rand < 0.75f) return Platform.PlatformType.MOVING_VERTICAL;   // 10%
        else if (rand < 0.85f) return Platform.PlatformType.BREAKABLE;         // 10%
        else if (rand < 0.9f) return Platform.PlatformType.BOUNCY;             // 5%
        else if (rand < 0.93f) return Platform.PlatformType.ICE;               // 3%
        else if (rand < 0.97f) return Platform.PlatformType.CONVEYOR;          // 4%
        else return Platform.PlatformType.DISAPPEARING;                        // 3%
    }

    private float[] generateNextPlatformPosition(float currentX, float currentY) {
        // Distance horizontale : plus courte et plus prévisible
        float minGap = 30f;
        float maxGap = 80f; // Beaucoup plus court que maxJumpDistance
        float deltaX = minGap + random.nextFloat() * (maxGap - minGap);

        // Hauteur : variation moins extrême
        float minY = 30f; // Hauteur minimum du sol
        float maxY = 150f; // Hauteur maximum plus basse
        float maxDelta = 40f; // Variation max entre plateformes réduite

        float deltaY = (random.nextFloat() - 0.5f) * 2f * maxDelta;
        float nextY = Math.max(minY, Math.min(maxY, currentY + deltaY));

        // Vérifier la faisabilité du saut
        if (!isJumpFeasible(currentX, currentY, currentX + deltaX, nextY)) {
            // Ajuster la position pour rendre le saut possible
            if (nextY > currentY + 50f) {
                nextY = currentY + 50f; // Limiter la montée
            } else if (nextY < currentY - 80f) {
                nextY = currentY - 80f; // Limiter la descente
            }
            deltaX = Math.min(70f, deltaX); // Limiter distance horizontale
        }

        return new float[]{currentX + deltaX, nextY};
    }

    private boolean isJumpFeasible(float x1, float y1, float x2, float y2) {
        float distance = x2 - x1;
        float heightDiff = y2 - y1;

        // Vérifier si c'est dans les limites physiques du joueur
        return distance <= maxJumpDistance &&
               heightDiff <= maxJumpHeight &&
               heightDiff >= -100f; // Peut descendre assez bas
    }

    private float getPlatformWidth(Platform.PlatformType type) {
        switch (type) {
            case MOVING_HORIZONTAL:
            case MOVING_VERTICAL:
                return 40 + random.nextFloat() * 30; // Plateformes mobiles plus petites
            case BREAKABLE:
                return 35 + random.nextFloat() * 15; // Fragiles et petites
            case BOUNCY:
                return 30 + random.nextFloat() * 20; // Trampolines compacts
            case DISAPPEARING:
                return 25 + random.nextFloat() * 25; // Variables
            default:
                return 50 + random.nextFloat() * 40; // Taille standard
        }
    }

    private float getPlatformHeight(Platform.PlatformType type) {
        switch (type) {
            case BOUNCY:
                return 25 + random.nextFloat() * 10; // Plus épaisses pour l'effet ressort
            case ICE:
                return 8 + random.nextFloat() * 5;   // Plus fines
            case DISAPPEARING:
                return 10 + random.nextFloat() * 8;  // Variables
            default:
                return 15 + random.nextFloat() * 10; // Épaisseur standard
        }
    }

    private void customizePlatform(Platform platform, Platform.PlatformType type) {
        switch (type) {
            case MOVING_HORIZONTAL:
                platform.moveRange = 60 + random.nextFloat() * 80;  // Distance de mouvement
                platform.moveSpeed = 20 + random.nextFloat() * 30;  // Vitesse
                break;

            case MOVING_VERTICAL:
                platform.moveRange = 40 + random.nextFloat() * 50;  // Mouvement vertical plus petit
                platform.moveSpeed = 15 + random.nextFloat() * 25;
                break;

            case BREAKABLE:
                platform.maxHits = 2 + random.nextInt(3);           // 2-4 coups pour casser
                platform.breakTotalTime = 2.2f + random.nextFloat() * 1.3f;
                break;

            case CONVEYOR:
                platform.conveyorSpeed = (random.nextBoolean() ? 1 : -1) * (30 + random.nextFloat() * 40);
                break;

            case DISAPPEARING:
                platform.timer = random.nextFloat() * 3f;           // Décalage aléatoire du cycle
                break;

            default:
                break;
        }
    }

    private void addBonusPlatform(Level level, float nearX, float nearY) {
        // Ajouter une plateforme bonus difficile d'accès (au-dessus ou sur le côté)
        float bonusX = nearX + (random.nextBoolean() ? 1 : -1) * (80 + random.nextFloat() * 40);
        float bonusY = nearY + 60 + random.nextFloat() * 50;

        // Types spéciaux pour les bonus
        Platform.PlatformType[] bonusTypes = {
            Platform.PlatformType.BOUNCY,
            Platform.PlatformType.CONVEYOR,
            Platform.PlatformType.DISAPPEARING
        };
        Platform.PlatformType bonusType = bonusTypes[random.nextInt(bonusTypes.length)];

        Platform bonus = new Platform(bonusX, bonusY, 40, 15, bonusType);
        customizePlatform(bonus, bonusType);
        level.addPlatform(bonus);
    }
}