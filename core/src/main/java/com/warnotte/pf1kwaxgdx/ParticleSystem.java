package com.warnotte.pf1kwaxgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class ParticleSystem {
    public enum ParticleLayer { BACKGROUND, FOREGROUND }

    private static class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float life;
        float maxLife;
        float gravity;
        float size;
        float damping;
        Color color;
        ParticleLayer layer;
    }

    private final Array<Particle> particles = new Array<>();
    private final Texture pixel;
    private final Color tmpColor = new Color();
    private int maxParticles = 500;

    public ParticleSystem() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = Math.max(64, maxParticles);
    }

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life += delta;
            if (p.life >= p.maxLife) {
                particles.removeIndex(i);
                continue;
            }
            p.vy += p.gravity * delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            float dampingFactor = Math.max(0f, 1f - p.damping * delta);
            p.vx *= dampingFactor;
            p.vy *= dampingFactor;
        }
    }

    public void render(SpriteBatch batch, ParticleLayer targetLayer) {
        Color previous = new Color(batch.getColor());
        for (int i = 0; i < particles.size; i++) {
            Particle p = particles.get(i);
            if (p.layer != targetLayer) {
                continue;
            }
            float lifeRatio = Math.max(0f, 1f - (p.life / p.maxLife));
            tmpColor.set(p.color);
            tmpColor.a *= lifeRatio;
            batch.setColor(tmpColor);
            float half = p.size * 0.5f;
            batch.draw(pixel, p.x - half, p.y - half, p.size, p.size);
        }
        batch.setColor(previous);
    }

    public void spawnDust(float x, float y, Color baseColor, ParticleLayer layer) {
        int count = 8 + MathUtils.random(4);
        for (int i = 0; i < count; i++) {
            Particle p = createParticle(x + MathUtils.random(-10f, 10f), y + MathUtils.random(-2f, 6f), baseColor, layer);
            p.vx = MathUtils.random(-18f, 18f);
            p.vy = MathUtils.random(22f, 46f);
            p.gravity = -120f;
            p.maxLife = MathUtils.random(0.35f, 0.6f);
            p.size = MathUtils.random(3f, 6f);
            p.damping = 1.6f;
            enqueue(p);
        }
    }

    public void spawnFragments(float x, float y, Color baseColor, int amount, ParticleLayer layer) {
        int count = MathUtils.clamp(amount, 4, 40);
        for (int i = 0; i < count; i++) {
            Particle p = createParticle(x + MathUtils.random(-6f, 6f), y + MathUtils.random(-4f, 8f), baseColor, layer);
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(35f, 90f);
            p.vx = MathUtils.cos(angle) * speed;
            p.vy = MathUtils.sin(angle) * speed;
            p.gravity = -160f;
            p.maxLife = MathUtils.random(0.55f, 1.1f);
            p.size = MathUtils.random(3.5f, 7.5f);
            p.damping = 0.9f;
            enqueue(p);
        }
    }

    private Particle createParticle(float x, float y, Color baseColor, ParticleLayer layer) {
        Particle particle = new Particle();
        particle.x = x;
        particle.y = y;
        particle.layer = layer;
        particle.color = new Color(baseColor);
        return particle;
    }

    private void enqueue(Particle particle) {
        if (particles.size >= maxParticles) {
            particles.removeIndex(0);
        }
        particles.add(particle);
    }

    public void clear() {
        particles.clear();
    }

    public void dispose() {
        clear();
        pixel.dispose();
    }
}
