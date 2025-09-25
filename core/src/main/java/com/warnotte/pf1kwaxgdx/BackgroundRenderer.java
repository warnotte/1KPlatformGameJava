package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

public class BackgroundRenderer {
    private ShapeRenderer shapeRenderer;
    private List<Cloud> clouds;
    private List<Mountain> mountains;
    private float timeOfDay = 0f; // 0-24h cycle
    private float sunX, sunY;

    // Classes internes pour les éléments
    private static class Cloud {
        float x, y, width, height, speed;
        float alpha;

        Cloud(float x, float y, float width, float height, float speed) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.alpha = 0.3f + (float)Math.random() * 0.4f; // Transparence variable
        }
    }

    private static class Mountain {
        float x, y, width, height;
        Color color;

        Mountain(float x, float y, float width, float height, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    public BackgroundRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        generateClouds();
        generateMountains();
    }

    private void generateClouds() {
        clouds = new ArrayList<>();
        // Générer plusieurs couches de nuages avec vitesses différentes
        for (int i = 0; i < 8; i++) {
            float x = (float)Math.random() * 1200 - 200;
            float y = 300 + (float)Math.random() * 150;
            float width = 40 + (float)Math.random() * 60;
            float height = 20 + (float)Math.random() * 30;
            float speed = 5 + (float)Math.random() * 15; // Vitesse variable
            clouds.add(new Cloud(x, y, width, height, speed));
        }
    }

    private void generateMountains() {
        mountains = new ArrayList<>();
        // Générer plusieurs couches de montagnes (parallaxe)

        // Couche arrière (plus sombre, plus lointaine)
        for (int i = 0; i < 6; i++) {
            float x = i * 150 - 100;
            float height = 80 + (float)Math.random() * 120;
            mountains.add(new Mountain(x, 0, 200, height, new Color(0.2f, 0.3f, 0.6f, 0.8f)));
        }

        // Couche avant (plus claire, plus proche)
        for (int i = 0; i < 8; i++) {
            float x = i * 120 - 50;
            float height = 60 + (float)Math.random() * 100;
            mountains.add(new Mountain(x, 0, 160, height, new Color(0.3f, 0.4f, 0.7f, 0.9f)));
        }
    }

    public void render(OrthographicCamera camera, float delta) {
        timeOfDay += delta * 2.0f; // Cycle jour/nuit plus rapide pour les tests
        if (timeOfDay > 24f) timeOfDay = 0f;

        // Calcul position soleil
        float sunAngle = (timeOfDay / 24f) * 6.28f; // 2π
        sunX = camera.position.x + 200 * (float)Math.cos(sunAngle);
        sunY = camera.position.y + 150 + 100 * (float)Math.sin(sunAngle);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 1. Dégradé de ciel
        renderSkyGradient(camera);

        // 2. Soleil
        renderSun(camera);

        // 3. Montagnes (parallaxe)
        renderMountains(camera);

        // 4. Nuages mouvants
        renderClouds(camera, delta);

        shapeRenderer.end();
    }

    private void renderSkyGradient(OrthographicCamera camera) {
        // Couleurs selon l'heure avec transitions douces
        Color skyTop, skyBottom;

        // Couleurs de base pour chaque période
        Color dayTop = new Color(0.5f, 0.8f, 1f, 1f);        // Bleu jour
        Color dayBottom = new Color(0.7f, 0.9f, 1f, 1f);
        Color nightTop = new Color(0.1f, 0.1f, 0.3f, 1f);    // Bleu nuit
        Color nightBottom = new Color(0.2f, 0.2f, 0.4f, 1f);
        Color sunsetTop = new Color(0.8f, 0.4f, 0.2f, 1f);   // Orange coucher de soleil
        Color sunsetBottom = new Color(1f, 0.6f, 0.3f, 1f);
        Color sunriseTop = new Color(0.9f, 0.6f, 0.4f, 1f);  // Rose lever de soleil
        Color sunriseBottom = new Color(1f, 0.8f, 0.6f, 1f);

        if (timeOfDay >= 5 && timeOfDay < 7) {
            // Lever de soleil (5h-7h) : transition nuit -> rose -> jour
            float t = (timeOfDay - 5) / 2f;
            if (t < 0.5f) {
                // Nuit vers lever de soleil
                float blend = t * 2f;
                skyTop = blendColors(nightTop, sunriseTop, blend);
                skyBottom = blendColors(nightBottom, sunriseBottom, blend);
            } else {
                // Lever de soleil vers jour
                float blend = (t - 0.5f) * 2f;
                skyTop = blendColors(sunriseTop, dayTop, blend);
                skyBottom = blendColors(sunriseBottom, dayBottom, blend);
            }
        } else if (timeOfDay >= 7 && timeOfDay < 17) {
            // Jour complet
            skyTop = dayTop;
            skyBottom = dayBottom;
        } else if (timeOfDay >= 17 && timeOfDay < 19) {
            // Coucher de soleil (17h-19h) : jour -> orange -> nuit
            float t = (timeOfDay - 17) / 2f;
            if (t < 0.5f) {
                // Jour vers coucher de soleil
                float blend = t * 2f;
                skyTop = blendColors(dayTop, sunsetTop, blend);
                skyBottom = blendColors(dayBottom, sunsetBottom, blend);
            } else {
                // Coucher de soleil vers nuit
                float blend = (t - 0.5f) * 2f;
                skyTop = blendColors(sunsetTop, nightTop, blend);
                skyBottom = blendColors(sunsetBottom, nightBottom, blend);
            }
        } else {
            // Nuit complète
            skyTop = nightTop;
            skyBottom = nightBottom;
        }

        // Simuler un dégradé avec des rectangles de couleurs dégradées
        float viewWidth = camera.viewportWidth;
        float viewHeight = camera.viewportHeight;
        float startX = camera.position.x - viewWidth/2;
        float startY = camera.position.y - viewHeight/2;

        int strips = 20; // Nombre de bandes pour simuler le dégradé
        for (int i = 0; i < strips; i++) {
            float ratio = (float)i / strips;
            Color currentColor = new Color();
            currentColor.r = skyBottom.r + (skyTop.r - skyBottom.r) * ratio;
            currentColor.g = skyBottom.g + (skyTop.g - skyBottom.g) * ratio;
            currentColor.b = skyBottom.b + (skyTop.b - skyBottom.b) * ratio;
            currentColor.a = 1f;

            shapeRenderer.setColor(currentColor);
            float stripHeight = viewHeight / strips;
            shapeRenderer.rect(startX, startY + i * stripHeight, viewWidth, stripHeight);
        }
    }

    private void renderSun(OrthographicCamera camera) {
        if (sunY > camera.position.y - camera.viewportHeight/2) {
            Color sunColor;
            if (timeOfDay >= 6 && timeOfDay <= 18) {
                sunColor = new Color(1f, 1f, 0.3f, 0.8f); // Jaune jour
            } else {
                sunColor = new Color(0.9f, 0.9f, 0.9f, 0.6f); // Blanc/gris nuit (lune)
            }
            shapeRenderer.setColor(sunColor);
            shapeRenderer.circle(sunX, sunY, 25);
        }
    }

    private void renderMountains(OrthographicCamera camera) {
        float parallaxFactor1 = 0.3f; // Couche arrière bouge lentement
        float parallaxFactor2 = 0.5f; // Couche avant bouge un peu plus vite

        for (int i = 0; i < mountains.size(); i++) {
            Mountain m = mountains.get(i);
            float parallaxFactor = (i < 6) ? parallaxFactor1 : parallaxFactor2;
            float offsetX = camera.position.x * parallaxFactor;

            shapeRenderer.setColor(m.color);

            // Triangle simple pour simuler une montagne
            float[] vertices = {
                m.x - offsetX, m.y,
                m.x - offsetX + m.width/2, m.y + m.height,
                m.x - offsetX + m.width, m.y
            };
            shapeRenderer.triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
        }
    }

    private void renderClouds(OrthographicCamera camera, float delta) {
        for (Cloud cloud : clouds) {
            // Mouvement des nuages
            cloud.x += cloud.speed * delta;

            // Réapparaître à gauche quand ils sortent à droite
            if (cloud.x > camera.position.x + camera.viewportWidth) {
                cloud.x = camera.position.x - camera.viewportWidth - cloud.width;
            }

            // Dessiner le nuage comme plusieurs ellipses
            shapeRenderer.setColor(1f, 1f, 1f, cloud.alpha);

            // Corps principal du nuage
            shapeRenderer.ellipse(cloud.x, cloud.y, cloud.width, cloud.height);
            // Parties additionnelles pour effet nuage
            shapeRenderer.ellipse(cloud.x + cloud.width * 0.3f, cloud.y + cloud.height * 0.2f,
                                cloud.width * 0.6f, cloud.height * 0.8f);
            shapeRenderer.ellipse(cloud.x + cloud.width * 0.6f, cloud.y - cloud.height * 0.1f,
                                cloud.width * 0.7f, cloud.height * 0.9f);
        }
    }

    // Fonction utilitaire pour mélanger deux couleurs
    private Color blendColors(Color color1, Color color2, float ratio) {
        float r = color1.r + (color2.r - color1.r) * ratio;
        float g = color1.g + (color2.g - color1.g) * ratio;
        float b = color1.b + (color2.b - color1.b) * ratio;
        return new Color(r, g, b, 1f);
    }

    // Getter pour l'heure (pour l'affichage)
    public float getTimeOfDay() {
        return timeOfDay;
    }
}