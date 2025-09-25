package com.warnotte.pf1kwaxgdx;

public class TestLevelGenerator {

    public static Level createTestLevel() {
        Level level = new Level();
        level.setLevelWidth(1400f); // Augmenté pour accueillir toutes les plateformes

        float currentX = 0f;
        float baseY = 50f;

        // Plateforme de départ
        level.addPlatform(new Platform(currentX, baseY, 80, 20, Platform.PlatformType.SOLID));
        currentX += 120f;

        // Test 1: Plateforme solide normale
        Platform solid = new Platform(currentX, baseY, 60, 20, Platform.PlatformType.SOLID);
        level.addPlatform(solid);
        currentX += 100f;

        // Test 2: Plateforme qui bouge horizontalement
        Platform movingH = new Platform(currentX, baseY, 50, 20, Platform.PlatformType.MOVING_HORIZONTAL);
        movingH.moveRange = 80f;
        movingH.moveSpeed = 20f;
        level.addPlatform(movingH);
        currentX += 150f; // Plus d'espace pour le mouvement

        // Test 3: Plateforme qui bouge verticalement
        Platform movingV = new Platform(currentX, baseY, 50, 20, Platform.PlatformType.MOVING_VERTICAL);
        movingV.moveRange = 60f;
        movingV.moveSpeed = 15f;
        level.addPlatform(movingV);
        currentX += 100f;

        // Test 4: Plateforme cassable
        Platform breakable = new Platform(currentX, baseY, 50, 20, Platform.PlatformType.BREAKABLE);
        breakable.maxHits = 3;
        breakable.breakTotalTime = 2.8f;
        level.addPlatform(breakable);
        currentX += 100f;

        // Test 5: Trampoline
        Platform bouncy = new Platform(currentX, baseY, 50, 25, Platform.PlatformType.BOUNCY);
        level.addPlatform(bouncy);
        currentX += 100f;

        // Test 6: Plateforme de glace
        Platform ice = new Platform(currentX, baseY, 60, 15, Platform.PlatformType.ICE);
        level.addPlatform(ice);
        currentX += 100f;

        // Test 7: Tapis roulant vers la droite
        Platform conveyorRight = new Platform(currentX, baseY, 60, 20, Platform.PlatformType.CONVEYOR);
        conveyorRight.conveyorSpeed = 50f; // Positif = droite
        level.addPlatform(conveyorRight);
        currentX += 100f;

        // Test 8: Tapis roulant vers la gauche
        Platform conveyorLeft = new Platform(currentX, baseY, 60, 20, Platform.PlatformType.CONVEYOR);
        conveyorLeft.conveyorSpeed = -50f; // Négatif = gauche
        level.addPlatform(conveyorLeft);
        currentX += 100f;

        // Test 9: Plateforme qui disparaît
        Platform disappearing = new Platform(currentX, baseY, 50, 20, Platform.PlatformType.DISAPPEARING);
        disappearing.timer = 0f; // Commence visible
        level.addPlatform(disappearing);
        currentX += 100f;

        // Plateforme finale large et solide
        level.addPlatform(new Platform(currentX, baseY + 30, 80, 30, Platform.PlatformType.SOLID));

        return level;
    }
}