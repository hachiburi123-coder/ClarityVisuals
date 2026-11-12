#version 330 core

// Depth-aware atmospheric pass used by LowFog and Sandstorm.
// Mode 0: world-space ground mist.
// Mode 1: desert storm with distance haze, wind ripples and flying dust.

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
uniform float u_Mode;
uniform float u_Density;
uniform float u_Height;
uniform float u_Speed;
uniform float u_MaxDistance;
uniform float u_Ground;
uniform float u_BaseY;
uniform vec3 u_Color;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

vec2 safeUv(vec2 uv) {
    return clamp(uv, vec2(0.001), vec2(0.999));
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
        p = p * 2.03 + vec2(3.7, 8.1);
        amplitude *= 0.5;
    }
    return value;
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
    if (dot(normal, centerPosition) > 0.0) {
        normal = -normal;
    }
    return normal;
}

vec3 skyDust(vec2 uv, vec4 original) {
    float aspect = u_Resolution.x / max(u_Resolution.y, 1.0);
    vec2 p = uv * vec2(aspect, 1.0);
    float t = u_Time * u_Speed;
    float n = fbm(p * 2.1 + vec2(-t * 0.12, t * 0.02));
    float veil = smoothstep(0.22, 0.82, n);
    float alpha = clamp(u_Density * 0.40 * (0.35 + 0.65 * veil), 0.0, 0.80);
    vec3 horizon = u_Color * vec3(0.76, 0.66, 0.50);
    horizon += vec3(0.11, 0.080, 0.035) * (1.0 - smoothstep(0.0, 0.65, uv.y));
    return mix(original.rgb, horizon, alpha);
}

void main() {
    vec4 original = texture(u_SceneTexture, vUv);
    float depth = texture(u_DepthTexture, vUv).r;
    int mode = int(round(u_Mode));

    if (depth >= 0.999995) {
        if (mode == 1) {
            fragColor = vec4(skyDust(vUv, original), original.a);
        } else {
            fragColor = original;
        }
        return;
    }

    vec3 viewPosition = reconstructViewPosition(vUv, depth);
    float distanceToCamera = length(viewPosition);
    if (distanceToCamera < 0.24) {
        fragColor = original;
        return;
    }

    vec3 worldPosition = reconstructWorldPosition(viewPosition);
    vec3 viewNormal = reconstructViewNormal(vUv, viewPosition);
    vec3 worldNormal = normalize(mat3(u_InverseViewMatrix) * viewNormal);

    if (mode == 0) {
        // Low fog: several soft world-space banks hugging the terrain.
        float aboveBase = max(worldPosition.y - u_BaseY, 0.0);
        float heightFade = exp(-aboveBase / max(u_Height * 0.44, 0.6));
        float valley = 1.0 - smoothstep(u_BaseY, u_BaseY + u_Height, worldPosition.y);
        heightFade = max(heightFade, valley * 0.82);

        float rangeFade = smoothstep(1.0, 4.5, distanceToCamera)
            * (1.0 - smoothstep(u_MaxDistance * 0.72, u_MaxDistance, distanceToCamera));
        if (rangeFade <= 0.002) {
            fragColor = original;
            return;
        }

        float t = u_Time * u_Speed;
        vec2 p = worldPosition.xz * 0.038;
        vec2 flow = vec2(t * 0.020, t * 0.008);
        float warp = fbm(p * 0.85 + flow * 0.75 + vec2(11.3, 4.2));
        float broad = fbm(p * 1.45 + warp * 2.1 + flow);
        float fine = fbm(p * 4.4 - flow * 1.4 + vec2(3.7, 8.1));
        float mist = smoothstep(0.28, 0.86, broad * 0.72 + fine * 0.28);
        float banks = smoothstep(0.18, 0.82, fbm(p * 0.42 + flow * 0.34 + 7.1));

        float alpha = u_Density * 0.72 * heightFade * rangeFade * mist * (0.46 + 0.54 * banks);
        alpha *= smoothstep(0.35, 1.35, distanceToCamera);
        if (alpha <= 0.002) {
            fragColor = original;
            return;
        }

        float sceneLight = dot(original.rgb, vec3(0.2126, 0.7152, 0.0722));
        vec3 fogColor = u_Color * (0.88 + 0.18 * broad + 0.16 * sceneLight);
        fogColor += vec3(0.035, 0.050, 0.060) * banks * heightFade;
        vec3 result = mix(original.rgb, fogColor, clamp(alpha, 0.0, 0.88));
        fragColor = vec4(clamp(result, 0.0, 1.0), original.a);
        return;
    }

    if (mode == 2) {
        // Alien atmosphere: a cold teal ground mist crossed by breathing
        // bioluminescent veins. The veins are thin iso-lines of two warped
        // noise fields, so they branch organically instead of forming a grid.
        vec3 result = original.rgb;
        float t = u_Time * u_Speed;
        vec2 wp = worldPosition.xz;
        vec2 flow = vec2(t * 0.018, -t * 0.011);
        float broad = fbm(wp * 0.085 + flow);
        float fine = fbm(wp * 0.34 - flow * 1.4);

        float upFacing = smoothstep(0.18, 0.62, clamp(worldNormal.y, 0.0, 1.0));
        float groundFade = smoothstep(0.40, 1.60, distanceToCamera)
            * (1.0 - smoothstep(u_MaxDistance * 0.90, u_MaxDistance, distanceToCamera));
        float heightGround = 1.0 - smoothstep(u_BaseY + 0.40, u_BaseY + 4.0, worldPosition.y);
        float groundMask = max(upFacing, heightGround * 0.82) * groundFade;

        if (groundMask > 0.002) {
            vec3 groundColor = original.rgb * vec3(0.42, 0.82, 0.88);
            groundColor += vec3(0.012, 0.060, 0.070) * smoothstep(0.35, 0.85, broad);

            vec2 vp = wp * 0.30 + vec2(broad * 2.1, fine * 2.1);
            float veinA = fbm(vp + vec2(t * 0.024, -t * 0.016));
            float veinB = fbm(vp * 2.35 - vec2(t * 0.040, t * 0.020));
            float iso = abs(veinA * 0.70 + veinB * 0.30 - 0.52);
            float vein = 1.0 - smoothstep(0.006, 0.060, iso);
            float iso2 = abs(fbm(vp * 1.7 + 9.3 + broad * 1.8) - 0.55);
            float branch = 1.0 - smoothstep(0.004, 0.038, iso2);
            float pulse = 0.55 + 0.45 * sin(u_Time * 1.9 + broad * 13.0 + fine * 7.0);

            vec3 veinColor = u_Color * (0.60 + 0.55 * pulse);
            veinColor = mix(veinColor, vec3(1.0, 0.32, 0.88), branch * 0.28);
            float veinAlpha = (vein * 0.78 + branch * 0.42) * groundMask * u_Ground
                * (0.35 + 0.65 * pulse);
            result = mix(result, groundColor, groundMask * 0.52);
            result = mix(result, veinColor, clamp(veinAlpha, 0.0, 0.88));
            result += veinColor * vein * groundMask * u_Ground * 0.10 * pulse;
        }

        float haze = 1.0 - exp(-distanceToCamera * (0.014 * u_Density + 0.004));
        float nearMist = (1.0 - smoothstep(0.0, 6.0, distanceToCamera)) * 0.16;
        float mist = smoothstep(0.18, 0.82, broad * 0.62 + fine * 0.38);
        float heightFade = exp(-max(worldPosition.y - u_BaseY, 0.0) / max(u_Height * 0.55, 0.6));
        float alpha = u_Density * (haze * 0.46 + nearMist) * (0.35 + 0.65 * mist);
        alpha *= max(heightFade, 0.18);
        alpha = clamp(alpha, 0.0, 0.72);
        vec3 mistColor = u_Color * (0.55 + 0.35 * mist) + vec3(0.015, 0.055, 0.070) * heightFade;
        result = mix(result, mistColor, alpha);

        float vignette = 1.0 - smoothstep(0.28, 1.05, length(vUv - 0.5));
        result *= 0.92 + 0.08 * vignette;
        fragColor = vec4(clamp(result, 0.0, 1.0), original.a);
        return;
    }

    // Sandstorm: wind-carved dunes first, then the airborne dust in front.
    vec3 result = original.rgb;
    float upFacing = smoothstep(0.16, 0.64, clamp(worldNormal.y, 0.0, 1.0));
    float groundFade = smoothstep(0.45, 1.9, distanceToCamera)
        * (1.0 - smoothstep(u_MaxDistance * 0.86, u_MaxDistance, distanceToCamera));
    // Depth fallback: even when the depth-derived normal is noisy, floor tiles
    // close to the storm base height still receive a visible layer of sand.
    float heightGround = 1.0 - smoothstep(u_BaseY + 0.35, u_BaseY + 3.2, worldPosition.y);
    float groundMask = max(upFacing, heightGround * 0.86) * groundFade;

    if (groundMask > 0.002 && u_Ground > 0.002) {
        float t = u_Time * u_Speed;
        vec2 wp = worldPosition.xz;
        vec2 drift = vec2(t * 0.020, -t * 0.012);

        // Large, domain-warped dunes. Replacing periodic sine ridges with fbm
        // removes the repeating grid that used to be visible on the floor.
        vec2 p = wp * 0.052 + drift;
        float warpA = fbm(p * 0.72 + vec2(4.3, 1.7));
        float warpB = fbm(p * 1.15 - vec2(7.1, 3.9) + drift * 0.4);
        float dunes = fbm(p + vec2(warpA * 1.8, warpB * 1.5));
        float dunes2 = fbm(p * 2.35 + vec2(warpB * 1.7, -warpA * 1.3) + drift * 0.8);
        float crest = smoothstep(0.48, 0.78, dunes * 0.70 + dunes2 * 0.30);

        // Fine ripples follow the dune flow instead of forming a pattern.
        float ripple = fbm(vec2(wp.x * 0.55 + warpA * 3.2, wp.y * 2.6 + warpB * 2.4) + drift * 1.6);
        float grain = valueNoise(wp * 7.5 + drift * 2.6);
        float sandMask = smoothstep(0.25, 0.88, ripple * 0.64 + grain * 0.22 + crest * 0.14);

        vec3 sandColor = u_Color * (0.76 + 0.30 * dunes + 0.10 * grain);
        sandColor += vec3(0.070, 0.048, 0.020) * smoothstep(0.55, 0.95, dunes);
        sandColor *= 0.94 + 0.06 * sandMask;

        // Soften the underlying block texture so the ground reads as one
        // continuous sand field rather than a grid of square blocks.
        vec2 texel = 1.0 / max(u_Resolution, vec2(1.0));
        vec3 softScene = result * 0.42;
        softScene += texture(u_SceneTexture, safeUv(vUv + vec2(texel.x * 1.5, 0.0))).rgb * 0.145;
        softScene += texture(u_SceneTexture, safeUv(vUv - vec2(texel.x * 1.5, 0.0))).rgb * 0.145;
        softScene += texture(u_SceneTexture, safeUv(vUv + vec2(0.0, texel.y * 1.5))).rgb * 0.145;
        softScene += texture(u_SceneTexture, safeUv(vUv - vec2(0.0, texel.y * 1.5))).rgb * 0.145;
        result = mix(result, softScene, groundMask * 0.34);

        float groundAlpha = u_Ground * u_Density * 0.54 * groundMask * (0.30 + 0.70 * sandMask);
        result = mix(result, sandColor, clamp(groundAlpha, 0.0, 0.86));
    }

    float aspect = u_Resolution.x / max(u_Resolution.y, 1.0);
    vec2 screen = vUv * vec2(aspect, 1.0);
    float t = u_Time * u_Speed;
    float sheet1 = fbm(screen * 2.0 + vec2(-t * 0.34, t * 0.035) + 4.0);
    float sheet2 = fbm(screen * 5.4 + vec2(-t * 0.72, t * 0.070) + 9.0);
    float dust = smoothstep(0.20, 0.86, sheet1 * 0.64 + sheet2 * 0.36);

    float haze = 1.0 - exp(-distanceToCamera * (0.022 * u_Density + 0.006));
    float nearDust = (1.0 - smoothstep(0.0, 7.0, distanceToCamera)) * 0.20;
    float alpha = u_Density * (haze * 0.56 + nearDust) * (0.24 + 0.76 * dust);
    alpha = clamp(alpha, 0.0, 0.80);

    vec3 dustColor = u_Color * (0.72 + 0.34 * dust);
    dustColor += vec3(0.035, 0.022, 0.008) * (1.0 - smoothstep(0.0, 0.55, vUv.y));
    result = mix(result, dustColor, alpha);

    float vignette = 1.0 - smoothstep(0.24, 1.02, length(vUv - 0.5));
    result *= 0.92 + 0.08 * vignette;
    fragColor = vec4(clamp(result, 0.0, 1.0), original.a);
}
