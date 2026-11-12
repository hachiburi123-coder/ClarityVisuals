#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;

float hash(vec2 pos) {
    vec3 p3 = fract(vec3(pos.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * vnoise(pos);
        pos = pos * 2.03 + vec2(19.7, -7.3);
        a *= 0.5;
    }
    return v;
}

void main() {
    // Координаты грани блока (0..1) — паттерн привязан к поверхности.
    vec2 uv = texCoord * 3.0;
    float t = time * 0.5;

    float f = fbm(uv + vec2(t * 0.25, -t * 0.2));
    float g = fbm(uv * 1.7 - vec2(t * 0.15, t * 0.3) + f);

    vec3 emerald = vec3(0.05, 0.85, 0.42);
    vec3 deep = vec3(0.01, 0.12, 0.07);
    vec3 color = mix(deep, emerald, clamp(g * 1.35, 0.0, 1.0));
    color = mix(color, baseColor.rgb * 1.1, 0.35);
    color *= 0.65 + 0.7 * f;

    float a = alpha * (0.3 + 0.7 * g);

    fragColor = vec4(color, a);
}
