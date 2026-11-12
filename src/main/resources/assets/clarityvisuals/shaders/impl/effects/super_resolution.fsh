#version 330 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D InputTexture;
uniform vec2 TexelSize;
uniform float Sharpness;
uniform int FilterMode;

vec3 sampleCAS(sampler2D tex, vec2 coords, vec2 d, float sharpness) {
    vec3 a = texture(tex, coords + vec2(0.0, -d.y)).rgb;
    vec3 b = texture(tex, coords + vec2(-d.x, 0.0)).rgb;
    vec3 c = texture(tex, coords).rgb;
    vec3 d_px = texture(tex, coords + vec2(d.x, 0.0)).rgb;
    vec3 e = texture(tex, coords + vec2(0.0, d.y)).rgb;

    float la = dot(a, vec3(0.2126, 0.7152, 0.0722));
    float lb = dot(b, vec3(0.2126, 0.7152, 0.0722));
    float lc = dot(c, vec3(0.2126, 0.7152, 0.0722));
    float ld = dot(d_px, vec3(0.2126, 0.7152, 0.0722));
    float le = dot(e, vec3(0.2126, 0.7152, 0.0722));

    float minL = min(min(min(la, lb), min(lc, ld)), le);
    float maxL = max(max(max(la, lb), max(lc, ld)), le);

    float peak = -0.25 * sharpness;
    float w = clamp(peak * sqrt(clamp((minL + 0.001) / (maxL + 0.001), 0.0, 1.0)), -0.25, 0.0);

    vec3 result = (a * w + b * w + d_px * w + e * w + c) / (1.0 + 4.0 * w);
    return clamp(result, 0.0, 1.0);
}

void main() {
    if (FilterMode == 0) {
        outColor = vec4(sampleCAS(InputTexture, uv, TexelSize, Sharpness), 1.0);
    } else {
        outColor = vec4(texture(InputTexture, uv).rgb, 1.0);
    }
}
