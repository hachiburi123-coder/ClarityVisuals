#version 150

// Aurora night sky: layered curtains, a real star field, a faint Milky Way
// and a moon halo. Shooting stars are emitted as world-space geometry.

in vec2 vNDC;
out vec4 fragColor;

uniform float time;
uniform vec2 resolution;
uniform vec4 camRight;
uniform vec4 camUp;
uniform vec4 camForward;
uniform float intensity;
uniform float starDensity;
uniform float auroraHeight;
uniform float purple;
uniform vec4 colorA;
uniform vec4 colorB;
uniform vec4 nightColor;
uniform float moon;
uniform float milkyWay;

float hash31(vec3 p) {
    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash31(i), hash31(i + vec3(1, 0, 0)), f.x),
            mix(hash31(i + vec3(0, 1, 0)), hash31(i + vec3(1, 1, 0)), f.x), f.y),
        mix(mix(hash31(i + vec3(0, 0, 1)), hash31(i + vec3(1, 0, 1)), f.x),
            mix(hash31(i + vec3(0, 1, 1)), hash31(i + vec3(1, 1, 1)), f.x), f.y), f.z);
}

float fbm3(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 3; i++) {
        value += noise3(p) * amplitude;
        p = p * 2.03 + vec3(11.7, 3.1, 7.9);
        amplitude *= 0.5;
    }
    return value;
}

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1.0, 0.0)), u.x),
               mix(hash21(i + vec2(0.0, 1.0)), hash21(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm2(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 3; i++) {
        value += noise2(p) * amplitude;
        p = p * 2.07 + vec2(5.2, 9.4);
        amplitude *= 0.5;
    }
    return value;
}

vec3 skyDir() {
    vec2 uv = vNDC;
    float aspect = resolution.x / max(resolution.y, 1.0);
    return normalize(camForward.xyz + uv.x * camRight.xyz * aspect + uv.y * camUp.xyz);
}

float starLayer(vec3 dir, float scale, float density, float layerSeed) {
    vec3 p = dir * scale;
    vec3 cell = floor(p);
    vec3 local = fract(p) - 0.5;
    float seed = hash31(cell + vec3(layerSeed));
    float threshold = 0.9985 - density * 0.0035;
    float star = smoothstep(threshold, 1.0, seed);
    star *= exp(-dot(local, local) * 30.0);
    star *= 0.55 + 0.45 * sin(time * (1.4 + seed * 4.0) + seed * 83.0 + layerSeed);
    return star;
}

float starField(vec3 dir) {
    if (dir.y < -0.06) {
        return 0.0;
    }
    float horizon = smoothstep(-0.04, 0.10, dir.y);
    float layerA = starLayer(dir, 280.0, 0.65, 1.0);
    float layerB = starLayer(dir, 510.0, 0.50, 17.0);
    float layerC = starLayer(dir, 870.0, 0.38, 41.0);
    return (layerA + layerB * 0.72 + layerC * 0.48) * horizon;
}

vec3 auroraRibbon(vec3 dir, float center, float width, float phase, float strength) {
    float angle = atan(dir.x, dir.z);
    vec2 around = normalize(dir.xz + vec2(0.0001));
    float t = time * 0.095 + phase * 2.0;

    float warp = fbm2(around * 2.3 + vec2(dir.y * 1.7 + t * 0.35, t * 0.52 + phase));
    float warpFine = noise2(around * 7.0 - vec2(t * 0.31, dir.y * 2.4 + phase * 1.7));

    float ribbon = sin(angle * 3.0 + warp * 8.2 + t * 1.1 + phase);
    float fold = sin(angle * 7.0 - warpFine * 6.5 - t * 0.63 + phase * 0.7);
    float band = smoothstep(0.10, 0.94, 0.5 + 0.5 * ribbon);
    band *= 0.62 + 0.38 * smoothstep(-0.3, 0.8, fold);

    float height = dir.y - center - ribbon * 0.055 - fold * 0.035;
    float curtain = exp(-height * height / max(width * width, 0.0001));

    float rays = 0.35 + 0.65 * smoothstep(0.20, 0.92,
        fbm2(vec2(angle * 27.0 + warp * 22.0, dir.y * 1.8 - t * 0.24)));
    float fine = 0.70 + 0.30 * sin(angle * 132.0 + warpFine * 34.0 + t * 2.4);
    float fade = smoothstep(0.012, 0.17, dir.y) * (1.0 - smoothstep(0.72, 0.99, dir.y));

    float glow = exp(-height * height / max(width * width * 7.0, 0.0001));
    float value = band * curtain * rays * fine * fade;
    value += band * glow * 0.35 * fade;
    value *= strength;

    float colorMix = clamp((dir.y - center + 0.19) * 1.55 + purple * 0.32, 0.0, 1.0);
    vec3 ribbonColor = mix(colorA.rgb, colorB.rgb, colorMix);
    ribbonColor = mix(ribbonColor, vec3(0.36, 0.90, 0.76), smoothstep(0.35, 0.95, ribbon) * 0.24);
    return ribbonColor * value;
}

void main() {
    vec3 dir = skyDir();
    float horizon = smoothstep(-0.10, 0.34, dir.y);

    vec3 background = mix(nightColor.rgb * 0.56, nightColor.rgb * 0.11, horizon);
    background += colorB.rgb * 0.030 * exp(-max(dir.y, 0.0) * 10.0);
    background += vec3(0.012, 0.020, 0.040) * (1.0 - horizon);

    vec3 color = background;
    color += vec3(0.82, 0.90, 1.0) * starField(dir);

    if (milkyWay > 0.5) {
        vec3 bandNormal = normalize(vec3(0.42, 0.30, -0.86));
        float band = exp(-pow(max(dot(dir, bandNormal), 0.0), 2.0) * 26.0);
        float dust = fbm3(dir * 6.2 + vec3(0.0, 0.0, time * 0.004));
        float wisps = smoothstep(0.33, 0.78, dust);
        vec3 milky = mix(vec3(0.035, 0.045, 0.105), vec3(0.16, 0.14, 0.30), wisps);
        color += milky * band * (0.50 + 0.30 * wisps);
    }

    float heightShift = (auroraHeight - 0.5) * 0.16;
    color += auroraRibbon(dir, 0.20 + heightShift, 0.058, 0.0, 0.86);
    color += auroraRibbon(dir, 0.31 + heightShift, 0.080, 2.4, 0.64);
    color += auroraRibbon(dir, 0.44 + heightShift, 0.052, 4.7, 0.44);

    if (moon > 0.5) {
        vec3 moonDir = normalize(vec3(-0.53, 0.37, -0.77));
        float moonDot = max(dot(dir, moonDir), 0.0);
        float disc = smoothstep(0.99942, 0.99984, moonDot);
        float halo = pow(moonDot, 190.0) * 0.30 + pow(moonDot, 22.0) * 0.035;
        color += vec3(1.0, 0.96, 0.85) * (disc * 1.15 + halo) * moon;
    }

    float nightFade = smoothstep(-0.08, 0.035, dir.y);
    color = mix(colorB.rgb * 0.035, color, nightFade);
    color *= mix(0.40, 1.0, smoothstep(-0.12, 0.10, dir.y));
    color += colorA.rgb * 0.012 * exp(-max(dir.y, 0.0) * 14.0);
    color = clamp(color, 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}
