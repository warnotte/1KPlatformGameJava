package com.warnotte.pf1kwaxgdx;

public class PostProcessSettings {
    public float barrelStrength = 0.14f;
    public float scanlineIntensity = 0.08f;
    public float maskIntensity = 0.12f;
    public float vignetteIntensity = 0.3f;
    public float chromaAmount = 0.25f;
    public float flickerAmount = 0.016f;
    public float noiseAmount = 0.024f;

    public static final float MIN_BARREL = 0f;
    public static final float MAX_BARREL = 0.3f;
    public static final float MIN_SCANLINE = 0f;
    public static final float MAX_SCANLINE = 0.12f;
    public static final float MIN_MASK = 0f;
    public static final float MAX_MASK = 0.25f;
    public static final float MIN_VIGNETTE = 0f;
    public static final float MAX_VIGNETTE = 0.6f;
    public static final float MIN_CHROMA = 0f;
    public static final float MAX_CHROMA = 0.4f;
    public static final float MIN_FLICKER = 0f;
    public static final float MAX_FLICKER = 0.03f;
    public static final float MIN_NOISE = 0f;
    public static final float MAX_NOISE = 0.05f;

    public void clampAll() {
        barrelStrength = clamp(barrelStrength, MIN_BARREL, MAX_BARREL);
        scanlineIntensity = clamp(scanlineIntensity, MIN_SCANLINE, MAX_SCANLINE);
        maskIntensity = clamp(maskIntensity, MIN_MASK, MAX_MASK);
        vignetteIntensity = clamp(vignetteIntensity, MIN_VIGNETTE, MAX_VIGNETTE);
        chromaAmount = clamp(chromaAmount, MIN_CHROMA, MAX_CHROMA);
        flickerAmount = clamp(flickerAmount, MIN_FLICKER, MAX_FLICKER);
        noiseAmount = clamp(noiseAmount, MIN_NOISE, MAX_NOISE);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static String createBar(float value, float min, float max) {
        int segments = 12;
        float ratio = (value - min) / (max - min);
        ratio = Math.max(0f, Math.min(1f, ratio));
        int filled = Math.round(ratio * segments);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < segments; i++) {
            sb.append(i < filled ? "#" : "-");
        }
        sb.append("]");
        return sb.toString();
    }
}
