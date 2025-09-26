#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform float u_time;
uniform float u_barrelStrength;
uniform float u_scanlineIntensity;
uniform float u_maskIntensity;
uniform float u_vignetteIntensity;
uniform float u_chromaAmount;
uniform float u_flickerAmount;
uniform float u_noiseAmount;

varying vec4 v_color;
varying vec2 v_texCoord;

float random(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 applyScanlines(vec2 uv, vec3 color, float intensity) {
    float rowPhase = sin((uv.y * u_resolution.y + 0.5) * 3.14159265);
    float rowAttenuation = 0.5 + 0.5 * rowPhase;
    float columnPhase = sin(uv.x * u_resolution.x * 6.2831853);
    float combined = 1.0 - intensity * (0.65 * rowAttenuation + 0.35 * (0.5 + 0.5 * columnPhase));
    return color * combined;
}

vec3 applyShadowMask(vec2 uv, vec3 color, float maskIntensity) {
    float triad = fract(uv.x * u_resolution.x / 3.0);
    vec3 mask = vec3(1.0);
    if (triad < 0.333) {
        mask = vec3(1.0, 0.82, 0.7);
    } else if (triad < 0.666) {
        mask = vec3(0.7, 1.0, 0.82);
    } else {
        mask = vec3(0.82, 0.7, 1.0);
    }
    return mix(color, color * mask, maskIntensity);
}

void main() {
    vec2 uv = v_texCoord;
    vec2 centered = uv - 0.5;
    float radius = dot(centered, centered);
    vec2 barrel = centered * (u_barrelStrength * radius);
    vec2 warped = clamp(uv + barrel, 0.001, 0.999);

    vec3 base = texture2D(u_texture, warped).rgb;
    base = applyScanlines(warped, base, u_scanlineIntensity);
    base = applyShadowMask(warped, base, u_maskIntensity);

    float chromaOffset = 0.0015;
    vec3 chroma = vec3(
        texture2D(u_texture, warped + vec2(-chromaOffset, 0.0)).r,
        texture2D(u_texture, warped).g,
        texture2D(u_texture, warped + vec2(chromaOffset, 0.0)).b
    );
    base = mix(base, chroma, u_chromaAmount);

    float vignette = smoothstep(0.78, 0.28, length(centered));
    base *= mix(1.0, 0.7, vignette * u_vignetteIntensity);

    float flicker = 1.0 + u_flickerAmount * sin(u_time * 9.0);
    float temporalNoise = random(vec2(warped.x * 1.7 + u_time * 0.85, warped.y * 2.3 - u_time * 1.2));
    float spatialNoise = random(warped * u_resolution.xy * vec2(0.75, 1.1));
    float grain = (temporalNoise + spatialNoise - 1.0) * 0.5 * u_noiseAmount;
    base = (base + grain) * flicker;

    gl_FragColor = vec4(base, 1.0) * v_color;
}
