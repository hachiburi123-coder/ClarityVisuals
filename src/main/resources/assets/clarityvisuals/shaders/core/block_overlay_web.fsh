#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;

void main() {
    // Координаты грани блока (0..1) — паттерн привязан к поверхности.
    vec2 uv = texCoord * 6.0;
    uv += vec2(sin(time * 0.7), cos(time * 0.6)) * 0.35;

    vec2 g = abs(fract(uv) - 0.5);
    float line = 1.0 - smoothstep(0.0, 0.09, min(g.x, g.y));

    vec2 g2 = abs(fract(uv * 0.5 + 0.5) - 0.5);
    float line2 = 1.0 - smoothstep(0.0, 0.06, min(g2.x, g2.y));

    vec3 color = baseColor.rgb * (0.35 + line * 1.4 + line2 * 0.6);
    float a = alpha * clamp(0.22 + line * 0.78 + line2 * 0.3, 0.0, 1.0);

    fragColor = vec4(color, a);
}
