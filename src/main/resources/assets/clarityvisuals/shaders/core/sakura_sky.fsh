#version 150

in vec2 vNDC;
out vec4 fragColor;

uniform float time;
uniform vec2 resolution;
uniform vec4 camRight;
uniform vec4 camUp;
uniform vec4 camForward;
uniform vec4 sunDirection;
uniform float intensity;
uniform float sun;
uniform float clouds;
uniform vec4 topColor;
uniform vec4 horizonColor;
uniform vec4 sunColor;

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
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
    for (int i = 0; i < 5; i++) {
        value += noise2(p) * amplitude;
        p = p * 2.03 + vec2(5.1, 9.7);
        amplitude *= 0.5;
    }
    return value;
}

vec3 skyDirection() {
    float aspect = resolution.x / max(resolution.y, 1.0);
    return normalize(camForward.xyz + vNDC.x * camRight.xyz * aspect + vNDC.y * camUp.xyz);
}

void main() {
    vec3 dir = skyDirection();
    float above = smoothstep(-0.09, 0.48, dir.y);
    vec3 color = mix(horizonColor.rgb, topColor.rgb, pow(above, 0.72));

    // Soft warm light pooling along the horizon.
    float horizonGlow = exp(-max(dir.y, 0.0) * 8.5);
    color += vec3(0.16, 0.075, 0.035) * horizonGlow * (0.45 + 0.55 * intensity);

    // A broad rose band just above the horizon keeps the whole sky warm.
    float rose = exp(-pow((dir.y - 0.035) * 5.2, 2.0));
    color += vec3(0.17, 0.035, 0.080) * rose * (0.35 + 0.65 * intensity);

    // The dawn sun has a fixed world direction, independent of the camera.
    vec3 sunDir = normalize(sunDirection.xyz);
    float sunDot = max(dot(dir, sunDir), 0.0);
    float disc = smoothstep(0.99955, 0.99993, sunDot);
    float glow = pow(sunDot, 220.0) * 0.86 + pow(sunDot, 22.0) * 0.20 + pow(sunDot, 4.0) * 0.022;
    color += sunColor.rgb * ((disc * 1.65 + glow) * sun);

    // Layered, slow clouds catching the warm light.
    float horizon = max(dir.y + 0.16, 0.06);
    vec2 p = dir.xz / horizon * 0.78;
    float n = fbm2(p * 0.72 + vec2(time * 0.007, time * 0.0025));
    float detail = fbm2(p * 2.1 - vec2(time * 0.014, time * 0.004));
    float cloud = smoothstep(0.43, 0.78, n * 0.76 + detail * 0.24);
    cloud *= smoothstep(0.01, 0.20, dir.y) * (1.0 - smoothstep(0.78, 0.99, dir.y));
    vec3 cloudColor = mix(vec3(1.0, 0.62, 0.70), vec3(1.0, 0.88, 0.91), detail);
    cloudColor += sunColor.rgb * pow(sunDot, 12.0) * 0.28;
    color = mix(color, cloudColor, cloud * 0.54 * clouds);

    // Peach haze hugging the horizon.
    float haze = 1.0 - smoothstep(-0.02, 0.16, dir.y);
    color = mix(color, mix(horizonColor.rgb, sunColor.rgb, 0.34), haze * 0.32);

    // Keep the very top of the sky airy and slightly violet.
    color += vec3(0.035, 0.025, 0.075) * smoothstep(0.55, 1.0, dir.y);

    color *= max(0.2, intensity);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
