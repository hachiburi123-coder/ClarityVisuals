#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;

void main() {
    // Координаты грани блока (0..1) — паттерн привязан к поверхности.
    vec2 uv = texCoord * 4.0;

    float w1 = sin(uv.x * 2.1 + time * 0.9) * sin(uv.y * 2.7 - time * 1.2);
    float w2 = sin((uv.x + uv.y) * 3.3 - time * 0.7);
    float wave = w1 * 0.5 + 0.5 + w2 * 0.25;
    wave = clamp(wave, 0.0, 1.0);

    float foam = smoothstep(0.75, 0.98, wave);

    vec3 deep = baseColor.rgb * 0.35 + vec3(0.0, 0.05, 0.1);
    vec3 crest = baseColor.rgb * 1.25 + vec3(0.12, 0.28, 0.32);
    vec3 color = mix(deep, crest, wave) + vec3(0.75, 0.9, 0.95) * foam * 0.5;

    float a = alpha * (0.3 + 0.7 * wave);

    fragColor = vec4(color, a);
}
