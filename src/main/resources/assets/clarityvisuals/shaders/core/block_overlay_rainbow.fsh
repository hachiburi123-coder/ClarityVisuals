#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;
uniform vec4 baseColor;
uniform float alpha;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    // Координаты грани блока (0..1) — паттерн привязан к поверхности.
    vec2 uv = texCoord;

    float hue = fract(uv.x * 0.6 + uv.y * 0.6 + time * 0.07);
    vec3 rainbow = hsv2rgb(vec3(hue, 0.75, 1.0));

    float luma = dot(baseColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 color = rainbow * (0.55 + 0.45 * luma) + baseColor.rgb * 0.12;

    float wave = 0.5 + 0.5 * sin((uv.x + uv.y) * 12.56 + time * 1.5);
    float a = alpha * (0.45 + 0.35 * wave);

    fragColor = vec4(color, a);
}
