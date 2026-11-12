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

    float bands = pow(0.5 + 0.5 * sin((uv.y * 7.0 - time * 0.9) * 6.28318), 3.0);
    float scan = pow(0.5 + 0.5 * sin((uv.x * 23.0 + time * 0.4) * 6.28318), 8.0);

    vec3 color = baseColor.rgb * (0.35 + bands * 1.7) + vec3(1.0) * (bands * 0.22 + scan * 0.12);
    float a = alpha * (0.25 + 0.75 * bands + scan * 0.1);

    fragColor = vec4(color, a);
}
