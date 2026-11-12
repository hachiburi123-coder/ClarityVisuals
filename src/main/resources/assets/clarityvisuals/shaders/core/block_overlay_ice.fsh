#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;

vec2 hash2(vec2 pos) {
    vec3 p3 = fract(vec3(pos.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

void main() {
    // Координаты грани блока (0..1) — паттерн привязан к поверхности.
    vec2 uv = texCoord * 5.0 + vec2(time * 0.03, -time * 0.02);

    vec2 ip = floor(uv);
    vec2 fp = fract(uv);
    float f1 = 8.0;
    float f2 = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 g = vec2(float(x), float(y));
            vec2 o = hash2(ip + g);
            vec2 r = g + o - fp;
            float d = dot(r, r);
            if (d < f1) {
                f2 = f1;
                f1 = d;
            } else if (d < f2) {
                f2 = d;
            }
        }
    }
    float edge = sqrt(f2) - sqrt(f1);
    float crack = 1.0 - smoothstep(0.0, 0.14, edge);
    float cellshade = f1 * 2.2;

    vec3 frost = vec3(0.82, 0.93, 1.0);
    vec3 color = mix(baseColor.rgb * 0.55 + vec3(0.08, 0.12, 0.16), frost, clamp(crack * 0.9 + cellshade * 0.25, 0.0, 1.0));

    float a = alpha * (0.3 + 0.7 * clamp(crack + cellshade * 0.4, 0.0, 1.0));

    fragColor = vec4(color, a);
}
