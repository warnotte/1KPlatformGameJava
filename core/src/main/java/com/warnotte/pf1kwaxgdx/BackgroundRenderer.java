package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

public class BackgroundRenderer {
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch cloudBatch;
    private List<Cloud> clouds;
    private List<Mountain> mountains;
    private float timeOfDay = 0f; // 0-24h cycle
    private float sunX, sunY;

    private static class Cloud {
        float x, y, width, height, speed, alpha;
        int layer;
        Texture texture;

        Cloud(float x, float y, float width, float height, float speed, float alpha, int layer, Texture texture) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.alpha = alpha;
            this.layer = layer;
            this.texture = texture;
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
        this.cloudBatch = new SpriteBatch();
        generateClouds();
        generateMountains();
    }

    private void generateClouds() {
        clouds = new ArrayList<>();
        for (int layer = 0; layer < 3; layer++) {
            int count = layer == 0 ? 4 : layer == 1 ? 5 : 6;
            for (int i = 0; i < count; i++) {
                float width = randomCloudWidth(layer);
                float height = width * MathUtils.random(0.35f, 0.55f);
                float x = MathUtils.random(-220f, 1400f);
                float y = randomCloudY(layer, 320f);
                float speed = randomCloudSpeed(layer);
                float alpha = randomCloudAlpha(layer);
                Texture texture = createCloudTexture(width, height);
                texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                clouds.add(new Cloud(x, y, width, height, speed, alpha, layer, texture));
            }
        }
    }

    private void generateMountains() {
        mountains = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            float x = i * 150 - 100;
            float height = 80 + MathUtils.random() * 120f;
            mountains.add(new Mountain(x, 0, 200, height, new Color(0.2f, 0.3f, 0.6f, 0.8f)));
        }

        for (int i = 0; i < 8; i++) {
            float x = i * 120 - 50;
            float height = 60 + MathUtils.random() * 100f;
            mountains.add(new Mountain(x, 0, 160, height, new Color(0.3f, 0.4f, 0.7f, 0.9f)));
        }
    }

    public void render(OrthographicCamera camera, float delta) {
        timeOfDay += delta * 2.0f;
        if (timeOfDay > 24f) timeOfDay = 0f;

        float sunAngle = (timeOfDay / 24f) * MathUtils.PI2;
        sunX = camera.position.x + 200 * MathUtils.cos(sunAngle);
        sunY = camera.position.y + 150 + 100 * MathUtils.sin(sunAngle);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderSkyGradient(camera);
        renderSun(camera);
        renderMountains(camera);
        shapeRenderer.end();

        renderClouds(camera, delta);
    }

    private void renderSkyGradient(OrthographicCamera camera) {
        Color skyTop, skyBottom;

        Color dayTop = new Color(0.5f, 0.8f, 1f, 1f);
        Color dayBottom = new Color(0.7f, 0.9f, 1f, 1f);
        Color nightTop = new Color(0.1f, 0.1f, 0.3f, 1f);
        Color nightBottom = new Color(0.2f, 0.2f, 0.4f, 1f);
        Color sunsetTop = new Color(0.8f, 0.4f, 0.2f, 1f);
        Color sunsetBottom = new Color(1f, 0.6f, 0.3f, 1f);
        Color sunriseTop = new Color(0.9f, 0.6f, 0.4f, 1f);
        Color sunriseBottom = new Color(1f, 0.8f, 0.6f, 1f);

        if (timeOfDay >= 5 && timeOfDay < 7) {
            float t = (timeOfDay - 5) / 2f;
            if (t < 0.5f) {
                float blend = t * 2f;
                skyTop = blendColors(nightTop, sunriseTop, blend);
                skyBottom = blendColors(nightBottom, sunriseBottom, blend);
            } else {
                float blend = (t - 0.5f) * 2f;
                skyTop = blendColors(sunriseTop, dayTop, blend);
                skyBottom = blendColors(sunriseBottom, dayBottom, blend);
            }
        } else if (timeOfDay >= 7 && timeOfDay < 17) {
            skyTop = dayTop;
            skyBottom = dayBottom;
        } else if (timeOfDay >= 17 && timeOfDay < 19) {
            float t = (timeOfDay - 17) / 2f;
            if (t < 0.5f) {
                float blend = t * 2f;
                skyTop = blendColors(dayTop, sunsetTop, blend);
                skyBottom = blendColors(dayBottom, sunsetBottom, blend);
            } else {
                float blend = (t - 0.5f) * 2f;
                skyTop = blendColors(sunsetTop, nightTop, blend);
                skyBottom = blendColors(sunsetBottom, nightBottom, blend);
            }
        } else {
            skyTop = nightTop;
            skyBottom = nightBottom;
        }

        float viewWidth = camera.viewportWidth;
        float viewHeight = camera.viewportHeight;
        float startX = camera.position.x - viewWidth / 2f;
        float startY = camera.position.y - viewHeight / 2f;

        int strips = 24;
        for (int i = 0; i < strips; i++) {
            float ratio = (float) i / strips;
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
        if (sunY > camera.position.y - camera.viewportHeight / 2f) {
            Color sunColor = (timeOfDay >= 6 && timeOfDay <= 18)
                ? new Color(1f, 1f, 0.3f, 0.8f)
                : new Color(0.9f, 0.9f, 0.9f, 0.6f);
            shapeRenderer.setColor(sunColor);
            shapeRenderer.circle(sunX, sunY, 25);
        }
    }

    private void renderMountains(OrthographicCamera camera) {
        float parallaxFactor1 = 0.3f;
        float parallaxFactor2 = 0.5f;

        for (int i = 0; i < mountains.size(); i++) {
            Mountain m = mountains.get(i);
            float parallaxFactor = (i < 6) ? parallaxFactor1 : parallaxFactor2;
            float offsetX = camera.position.x * parallaxFactor;
            shapeRenderer.setColor(m.color);
            float[] vertices = {
                m.x - offsetX, m.y,
                m.x - offsetX + m.width / 2f, m.y + m.height,
                m.x - offsetX + m.width, m.y
            };
            shapeRenderer.triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
        }
    }

    private void renderClouds(OrthographicCamera camera, float delta) {
        float viewLeft = camera.position.x - camera.viewportWidth / 2f - 180f;
        float viewRight = camera.position.x + camera.viewportWidth / 2f + 180f;

        cloudBatch.setProjectionMatrix(camera.combined);
        cloudBatch.begin();
        for (Cloud cloud : clouds) {
            cloud.x += cloud.speed * delta;

            if (cloud.x > viewRight) {
                cloud.x = viewLeft - cloud.width - MathUtils.random(40f, 160f);
                cloud.y = randomCloudY(cloud.layer, camera.position.y);
                cloud.alpha = randomCloudAlpha(cloud.layer);
                cloud.speed = randomCloudSpeed(cloud.layer);
            } else if (cloud.x + cloud.width < viewLeft) {
                cloud.x = viewRight + MathUtils.random(40f, 160f);
                cloud.y = randomCloudY(cloud.layer, camera.position.y);
                cloud.alpha = randomCloudAlpha(cloud.layer);
                cloud.speed = randomCloudSpeed(cloud.layer);
            }

            cloudBatch.setColor(1f, 1f, 1f, cloud.alpha);
            cloudBatch.draw(cloud.texture, cloud.x, cloud.y, cloud.width, cloud.height);
        }
        cloudBatch.setColor(Color.WHITE);
        cloudBatch.end();
    }


private Texture createCloudTexture(float targetWidth, float targetHeight) {
    int pixWidth = MathUtils.clamp(MathUtils.round(targetWidth), 96, 320);
    int pixHeight = MathUtils.clamp(MathUtils.round(targetHeight), 64, 200);
    Pixmap pixmap = new Pixmap(pixWidth, pixHeight, Pixmap.Format.RGBA8888);
    pixmap.setColor(0f, 0f, 0f, 0f);
    pixmap.fill();

    int puffs = MathUtils.random(5, 7);
    for (int i = 0; i < puffs; i++) {
        float cx = MathUtils.random(pixWidth * 0.25f, pixWidth * 0.75f);
        float cy = MathUtils.random(pixHeight * 0.45f, pixHeight * 0.85f);
        float radius = MathUtils.random(pixHeight * 0.22f, pixHeight * 0.45f);
        drawSoftCircle(pixmap, cx, cy, radius, 0.36f);
    }

    int highlights = MathUtils.random(2, 3);
    for (int i = 0; i < highlights; i++) {
        float cx = MathUtils.random(pixWidth * 0.3f, pixWidth * 0.7f);
        float cy = MathUtils.random(pixHeight * 0.65f, pixHeight * 0.95f);
        float radius = MathUtils.random(pixHeight * 0.18f, pixHeight * 0.3f);
        drawSoftCircle(pixmap, cx, cy, radius, 0.55f);
    }

    applyEdgeFade(pixmap, 0.22f);

    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return texture;
}

private void drawSoftCircle(Pixmap pixmap, float centerX, float centerY, float radius, float baseAlpha) {
    int steps = 4;
    for (int i = 0; i < steps; i++) {
        float t = (float) i / steps;
        float stepRadius = radius * (1f - t * 0.3f);
        float alpha = baseAlpha * (1f - t * 0.5f);
        pixmap.setColor(1f, 1f, 1f, alpha);
        pixmap.fillCircle(MathUtils.round(centerX), MathUtils.round(centerY), MathUtils.round(stepRadius));
    }
}

private void applyEdgeFade(Pixmap pixmap, float strength) {
    int width = pixmap.getWidth();
    int height = pixmap.getHeight();
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int rgba = pixmap.getPixel(x, y);
            if ((rgba & 0x000000ff) == 0) {
                continue;
            }
            Color color = new Color();
            Color.rgba8888ToColor(color, rgba);
            float distX = Math.min(x, width - 1 - x) / (float) width;
            float distY = Math.min(y, height - 1 - y) / (float) height;
            float edge = Math.min(distX, distY);
            edge = MathUtils.clamp(edge * 2.2f, 0f, 1f);
            edge = MathUtils.clamp(edge, 0f, 1f);
            edge = edge * edge * (3f - 2f * edge);
            float fade = MathUtils.lerp(1f - strength, 1f, edge);
            color.a *= MathUtils.clamp(fade, 0f, 1f);
            pixmap.drawPixel(x, y, Color.rgba8888(color));
        }
    }
}

private float randomCloudWidth(int layer) {

        switch (layer) {
            case 0: return MathUtils.random(170f, 230f);
            case 1: return MathUtils.random(150f, 210f);
            default: return MathUtils.random(130f, 190f);
        }
    }

    private float randomCloudSpeed(int layer) {
        switch (layer) {
            case 0: return MathUtils.random(4f, 7f);
            case 1: return MathUtils.random(6f, 11f);
            default: return MathUtils.random(8f, 14f);
        }
    }

    private float randomCloudAlpha(int layer) {
        float base;
        switch (layer) {
            case 0: base = 0.35f; break;
            case 1: base = 0.45f; break;
            default: base = 0.55f; break;
        }
        return MathUtils.clamp(base + MathUtils.random(-0.08f, 0.08f), 0.2f, 0.75f);
    }

    private float randomCloudY(int layer, float referenceY) {
        float base;
        float range;
        switch (layer) {
            case 0:
                base = referenceY + 120f;
                range = 70f;
                break;
            case 1:
                base = referenceY + 80f;
                range = 80f;
                break;
            default:
                base = referenceY + 50f;
                range = 70f;
                break;
        }
        return base + MathUtils.random(-range * 0.5f, range * 0.5f);
    }

    private Color blendColors(Color color1, Color color2, float ratio) {
        float r = color1.r + (color2.r - color1.r) * ratio;
        float g = color1.g + (color2.g - color1.g) * ratio;
        float b = color1.b + (color2.b - color1.b) * ratio;
        return new Color(r, g, b, 1f);
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public void dispose() {
        if (clouds != null) {
            for (Cloud cloud : clouds) {
                if (cloud.texture != null) {
                    cloud.texture.dispose();
                }
            }
        }
        cloudBatch.dispose();
    }
}
