#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;

void main() {
    // Координаты грани блока (0..1) — паттерн привязан к поверхности.
    vec2 uv = texCoord;

    float d = fract((uv.x + uv.y) * 3.0 + time * 0.22);
    float stripe = smoothstep(0.0, 0.12, d) * (1.0 - smoothstep(0.38, 0.5, d));
    float stripe2 = smoothstep(0.5, 0.62, d) * (1.0 - smoothstep(0.88, 1.0, d));

    vec3 pink = vec3(1.0, 0.62, 0.78);
    vec3 color = mix(baseColor.rgb * 0.75, pink, stripe * 0.85);
    color = mix(color, vec3(1.0, 0.95, 0.97), stripe2 * 0.5);

    float wave = 0.5 + 0.5 * sin(time * 2.0 + (uv.x - uv.y) * 9.0);
    float a = alpha * (0.4 + 0.45 * max(stripe, stripe2) + 0.15 * wave);

    fragColor = vec4(color, a);
}
