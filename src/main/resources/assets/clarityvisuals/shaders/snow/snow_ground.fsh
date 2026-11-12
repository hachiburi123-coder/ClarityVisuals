#version 330 core

// Winter snow cover: screen-space snow on upward faces.
// Adapted from "BEAUTIFUL REALISTIC SNOW FIELD" look (soft drifts +
// layered grain), rendered like the Rain wet floor: over the real frame
// using reconstructed world position / normals.

in vec2 vUv;
layout(location = 0) out vec4 fragColor;

uniform sampler2D u_SceneTexture;
uniform sampler2D u_DepthTexture;
uniform vec2 u_Resolution;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ProjectionMatrix;
uniform mat4 u_InverseProjectionMatrix;
uniform mat4 u_InverseViewMatrix;
uniform float u_Time;
uniform float u_Coverage;
uniform float u_Brightness;
uniform float u_Sparkle;
uniform float u_SnowAmount;
uniform float u_MaxDistance;
uniform int u_Sparkles;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

vec2 safeUv(vec2 uv) {
    return clamp(uv, vec2(0.001), vec2(0.999));
}

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += valueNoise(p) * amplitude;
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

vec2 snowWarp(vec2 p) {
    vec2 q;
    q.x = fbm(p * 0.18 + vec2(4.7, 13.1));
    q.y = fbm(p * 0.18 - vec2(11.3, 3.7));
    return p + (q - 0.5) * 4.0;
}

float snowDetail(vec2 p) {
    float a = valueNoise(p * 5.0);
    float b = valueNoise(p * 11.0);
    float c = valueNoise(p * 24.0);
    return a * 0.60 + b * 0.28 + c * 0.12;
}

vec3 reconstructViewPosition(vec2 uv, float depth) {
    vec4 clip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 view = u_InverseProjectionMatrix * clip;
    float invW = abs(view.w) > 0.000001 ? 1.0 / view.w : 1.0;
    return view.xyz * invW;
}

vec3 reconstructWorldPosition(vec3 viewPosition) {
    return (u_InverseViewMatrix * vec4(viewPosition, 1.0)).xyz;
}

vec3 reconstructViewNormal(vec2 uv, vec3 centerPosition) {
    vec2 texel = 1.0 / max(u_Resolution, vec2(1.0));

    float depthL = texture(u_DepthTexture, safeUv(uv - vec2(texel.x, 0.0))).r;
    float depthR = texture(u_DepthTexture, safeUv(uv + vec2(texel.x, 0.0))).r;
    float depthD = texture(u_DepthTexture, safeUv(uv - vec2(0.0, texel.y))).r;
    float depthU = texture(u_DepthTexture, safeUv(uv + vec2(0.0, texel.y))).r;

    vec3 posL = reconstructViewPosition(uv - vec2(texel.x, 0.0), min(depthL, 0.999999));
    vec3 posR = reconstructViewPosition(uv + vec2(texel.x, 0.0), min(depthR, 0.999999));
    vec3 posD = reconstructViewPosition(uv - vec2(0.0, texel.y), min(depthD, 0.999999));
    vec3 posU = reconstructViewPosition(uv + vec2(0.0, texel.y), min(depthU, 0.999999));

    vec3 vL = centerPosition - posL;
    vec3 vR = posR - centerPosition;
    vec3 vD = centerPosition - posD;
    vec3 vU = posU - centerPosition;
    vec3 tangentX = dot(vL, vL) < dot(vR, vR) ? vL : vR;
    vec3 tangentY = dot(vD, vD) < dot(vU, vU) ? vD : vU;

    vec3 normal = cross(tangentX, tangentY);
    float normalLength = length(normal);
    if (normalLength <= 0.000001) {
        return vec3(0.0, 0.0, 1.0);
    }
    normal /= normalLength;
    if (dot(normal, centerPosition) > 0.0) normal = -normal;
    return normal;
}

void main() {
    vec4 original = texture(u_SceneTexture, vUv);
    float depth = texture(u_DepthTexture, vUv).r;
    if (depth >= 0.999995) {
        fragColor = original;
        return;
    }

    vec3 viewPosition = reconstructViewPosition(vUv, depth);
    float distanceToCamera = length(viewPosition);
    if (distanceToCamera > u_MaxDistance || distanceToCamera < 0.32) {
        fragColor = original;
        return;
    }

    vec3 viewNormal = reconstructViewNormal(vUv, viewPosition);
    vec3 worldNormal = normalize(mat3(u_InverseViewMatrix) * viewNormal);
    vec3 worldPosition = reconstructWorldPosition(viewPosition);

    // Only upward faces get snow, same strict ground test as wet floor.
    float upFacing = clamp(worldNormal.y, 0.0, 1.0);
    float isGround = smoothstep(0.35, 0.85, upFacing);
    if (isGround <= 0.003) {
        fragColor = original;
        return;
    }

    // Snow field (from snow shader: warped large drifts + medium folds + grain).
    vec2 warped = snowWarp(worldPosition.xz);
    float large = fbm(warped * 0.115);
    float medium = fbm(warped * 0.32);
    float small = fbm(warped * 0.95);
    float fine = valueNoise(warped * 4.0);

    float field = large * 0.62 + medium * 0.27 + small * 0.11;
    field += (fine - 0.5) * 0.03;

    float threshold = mix(0.74, 0.26, saturate(u_Coverage));
    float snow = smoothstep(threshold - 0.11, threshold + 0.09, field) * isGround;

    float rangeFade = 1.0 - smoothstep(u_MaxDistance * 0.70, u_MaxDistance, distanceToCamera);
    float nearFade = smoothstep(0.32, 0.85, distanceToCamera);
    float mask = snow * rangeFade * nearFade;
    if (mask <= 0.002) {
        fragColor = original;
        return;
    }

    // Snow shading (same palette as the snow reference).
    vec3 snowBright = vec3(0.985, 0.992, 1.000);
    vec3 snowMid = vec3(0.880, 0.925, 0.975);
    vec3 snowShadowColor = vec3(0.540, 0.640, 0.760);

    float sceneLum = luminance(original.rgb);
    float lighting = 0.70 + worldNormal.y * 0.22 + sceneLum * 0.22;
    float illumination = smoothstep(0.48, 1.18, lighting);
    vec3 color = mix(snowMid, snowBright, illumination);

    float slope = 1.0 - worldNormal.y;
    float valley = smoothstep(0.03, 0.42, slope);
    color = mix(color, snowShadowColor, valley * 0.42);

    float largeVariation = fbm(worldPosition.xz * 0.22);
    color *= 0.965 + largeVariation * 0.045;

    float mediumTexture = fbm(worldPosition.xz * 0.70);
    color = mix(color * 0.985, color * 1.015, mediumTexture);

    float detail = snowDetail(worldPosition.xz);
    color *= 0.985 + detail * 0.025;

    color *= u_Brightness;

    // Snow sparkles: sparse twinkling glints, no full-screen glitter.
    if (u_Sparkles == 1 && u_Sparkle > 0.001) {
        vec2 cellId = floor(worldPosition.xz * 14.0);
        float cell = hash21(cellId);
        float twinkle = pow(0.5 + 0.5 * sin(u_Time * (1.5 + cell * 3.5) + cell * 41.0), 8.0);
        float glint = step(0.93, cell) * twinkle;
        color += vec3(1.0, 0.99, 0.97) * glint * u_Sparkle * 0.55;
    }

    float amount = clamp(mask * u_SnowAmount, 0.0, 1.0);

    // Soft blue ambient in slopes so snow sits in the scene.
    float ambientSnow = smoothstep(0.0, 0.55, 1.0 - worldNormal.y);
    vec3 snowColored = mix(color, color * vec3(0.90, 0.95, 1.02), ambientSnow * 0.25);

    vec3 result = mix(original.rgb, snowColored, amount);

    // Gentle distance softening far away.
    float distanceFade = smoothstep(8.0, 42.0, length(worldPosition.xz - u_InverseViewMatrix[3].xz));
    result = mix(result, vec3(0.91, 0.95, 0.99), distanceFade * 0.10 * amount);

    fragColor = vec4(clamp(result, 0.0, 1.0), original.a);
}
