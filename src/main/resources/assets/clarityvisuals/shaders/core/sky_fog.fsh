#version 150

/*
 * ShaderFog — procedural 3D skies.
 * Modes (fogMode): 0 = Night Sky, 1 = Day Sky, 2 = Morning Sky, 3 = Space.
 * The ray direction is rebuilt from the camera basis and the real FOV, so the
 * whole scene has true 3D parallax when the camera rotates. Clouds are layered
 * noise planes with wind drift and slow shape evolution (third noise axis = time).
 */

in vec2 vNDC;
out vec4 fragColor;

uniform float time;
uniform vec2 resolution;
uniform vec4 fogColor;
uniform float intensity;
uniform float fogMode;
uniform vec4 camRight;
uniform vec4 camUp;
uniform vec4 camForward;
uniform float fovScale;

vec3 gLightDir = vec3(0.0, 1.0, 0.0);

// ------------------------------------------------------------------ noise

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float hash13(vec3 p3) {
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3, p3.zyx + 31.32);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 hash33(vec3 p3) {
    p3 = fract(p3 * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yxz + 33.33);
    return fract((p3.xxy + p3.yxx) * p3.zyx);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), u.x),
               mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), u.x), u.y);
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(hash13(i), hash13(i + vec3(1.0, 0.0, 0.0)), u.x),
                   mix(hash13(i + vec3(0.0, 1.0, 0.0)), hash13(i + vec3(1.0, 1.0, 0.0)), u.x), u.y),
               mix(mix(hash13(i + vec3(0.0, 0.0, 1.0)), hash13(i + vec3(1.0, 0.0, 1.0)), u.x),
                   mix(hash13(i + vec3(0.0, 1.0, 1.0)), hash13(i + vec3(1.0, 1.0, 1.0)), u.x), u.y), u.z);
}

float fbm2(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int k = 0; k < 5; k++) {
        v += a * noise2(p);
        p = p * 2.03 + 17.13;
        a *= 0.5;
    }
    return v;
}

float fbm3(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int k = 0; k < 4; k++) {
        v += a * noise3(p);
        p = p * 2.02 + 11.31;
        a *= 0.5;
    }
    return v;
}

vec3 skyGradient(float y, vec3 zen, vec3 mid, vec3 hor) {
    float yy = clamp(y, -0.35, 1.0);
    vec3 c = mix(mid, zen, smoothstep(0.18, 0.95, yy));
    return mix(hor, c, smoothstep(-0.06, 0.3, yy));
}

// ------------------------------------------------------------------ clouds

float cloudDensity(vec2 uv, float t, float evolve, float warp) {
    vec2 w = vec2(noise2(uv * 0.5 + vec2(t * 0.01, 0.0)), noise2(uv * 0.5 + 4.73));
    float d = fbm3(vec3((uv + w * warp) * 0.32, t * evolve));
    d += 0.4 * fbm3(vec3(uv * 1.15 + w * 0.5 + 31.7, t * evolve * 1.9)) - 0.2;
    return d;
}

vec4 cloudLayer(vec3 rd, float h, float scale, float wind, float cov, float evolve, float warp,
                vec3 litCol, vec3 shadeCol, vec3 hazeCol, float t, float amt, float fadeK) {
    if (rd.y < 0.012) {
        return vec4(0.0);
    }
    float tt = h / rd.y;
    vec2 uv = rd.xz * (tt * scale) + vec2(t * wind, t * wind * 0.55);
    float d = cloudDensity(uv, t, evolve, warp);
    float alpha = smoothstep(cov, cov + 0.42, d);
    if (alpha < 0.003) {
        return vec4(0.0);
    }
    vec2 ldir = normalize(gLightDir.xz + vec2(0.001, 0.0007)) * 0.5;
    float dl = cloudDensity(uv + ldir, t, evolve, warp);
    float lit = clamp(1.0 - (dl - d) * 1.7, 0.0, 1.0);
    lit = lit * lit * (3.0 - 2.0 * lit);
    vec3 col = mix(shadeCol, litCol, lit);
    float fade = exp(-tt * fadeK);
    col = mix(hazeCol, col, fade);
    alpha *= 0.3 + 0.7 * fade;
    alpha *= 0.5 + 0.5 * clamp(amt, 0.0, 1.0);
    return vec4(col, clamp(alpha, 0.0, 1.0));
}

// -------------------------------------------------------------- celestial

vec3 moonDirOf(float t) {
    float a = t * 0.004 + 1.2;
    return normalize(vec3(cos(a) * 0.85, 0.45 + 0.18 * sin(a * 0.6), sin(a) * 0.85));
}

vec3 daySunDirOf(float t) {
    float a = t * 0.004 + 0.6;
    return normalize(vec3(cos(a), 0.62 + 0.12 * sin(a * 0.7), sin(a)));
}

vec3 morningSunDirOf(float t) {
    float a = t * 0.0035 + 2.2;
    return normalize(vec3(cos(a) * 0.9, 0.13 + 0.05 * sin(a * 0.5), sin(a) * 0.9));
}

vec3 stars(vec3 sd, float t, float amt) {
    vec3 acc = vec3(0.0);
    for (int k = 0; k < 3; k++) {
        float fk = float(k);
        float cs = 30.0 + fk * 42.0;
        float dens = 0.1 - fk * 0.022;
        vec3 p = sd * cs + fk * 17.31;
        vec3 id = floor(p);
        vec3 f = fract(p) - 0.5;
        vec3 rnd = hash33(id);
        float sel = step(1.0 - dens, rnd.x);
        vec3 off = (rnd - 0.5) * 0.8;
        float d = length(f - off);
        float b = 0.3 + 0.7 * rnd.y;
        b *= 0.72 + 0.28 * sin(t * (1.2 + 3.5 * rnd.z) + rnd.x * 6.2831);
        vec3 scol = mix(vec3(0.72, 0.82, 1.0), vec3(1.0, 0.88, 0.72), step(0.55, rnd.z));
        scol = mix(scol, vec3(1.0), 0.45);
        float core = exp(-d * d * 750.0);
        float glow = exp(-d * d * 55.0) * 0.3;
        acc += scol * (core * 1.6 + glow) * b * sel;
    }
    return acc * (0.45 + 0.65 * clamp(amt, 0.0, 1.1));
}

vec3 milkyWay(vec3 sd, float t, float strength) {
    vec3 n = normalize(vec3(0.32, 0.78, 0.54));
    float d = abs(dot(sd, n));
    float band = exp(-d * d * 16.0);
    float cl = fbm3(sd * 2.6 + vec3(7.31) + t * 0.002);
    float dust = smoothstep(0.3, 0.78, fbm3(sd * 5.3 - vec3(3.17)));
    float m = band * (0.32 + 0.95 * cl) * (1.0 - 0.72 * dust);
    vec3 col = mix(vec3(0.55, 0.68, 1.0), vec3(1.0, 0.86, 0.68), cl);
    return col * m * strength;
}

vec3 moonBody(vec3 rd, vec3 md, float amt) {
    float mu = dot(rd, md);
    float om = max(1.0 - mu, 0.0);
    vec3 col = vec3(0.0);
    col += vec3(0.55, 0.65, 0.95) * exp(-om * 10.0) * 0.22;
    col += vec3(0.85, 0.88, 1.0) * exp(-om * 80.0) * 0.55;
    float disc = smoothstep(0.99955, 0.99985, mu);
    if (disc > 0.001) {
        vec3 ux = normalize(cross(md, vec3(0.0, 1.0, 0.0)));
        vec3 uy = cross(ux, md);
        vec2 muv = vec2(dot(rd, ux), dot(rd, uy)) * 34.0;
        float maria = fbm2(muv * 1.8 + 3.7);
        float crater = fbm2(muv * 4.6 - 8.2);
        vec3 surf = mix(vec3(0.82, 0.83, 0.88), vec3(0.5, 0.54, 0.62), smoothstep(0.35, 0.72, maria));
        surf *= 0.86 + 0.28 * crater;
        col += surf * disc;
    }
    return col * (0.5 + 0.65 * clamp(amt, 0.0, 1.1));
}

vec3 daySun(vec3 rd, vec3 sd0, float amt) {
    float mu = dot(rd, sd0);
    float om = max(1.0 - mu, 0.0);
    vec3 col = vec3(0.0);
    col += vec3(1.0, 0.9, 0.72) * exp(-om * 16.0) * 0.4;
    col += vec3(1.0, 0.97, 0.88) * exp(-om * 110.0) * 0.85;
    col += vec3(1.0, 0.98, 0.9) * smoothstep(0.99978, 0.99992, mu) * 2.4;
    return col * (0.55 + 0.5 * clamp(amt, 0.0, 1.0));
}

vec3 morningSun(vec3 rd, vec3 sd0, float amt) {
    float mu = dot(rd, sd0);
    float om = max(1.0 - mu, 0.0);
    vec3 col = vec3(0.0);
    col += vec3(1.0, 0.55, 0.28) * exp(-om * 4.5) * 0.5;
    col += vec3(1.0, 0.72, 0.4) * exp(-om * 24.0) * 0.7;
    col += vec3(1.0, 0.85, 0.55) * smoothstep(0.9996, 0.99985, mu) * 2.0;
    return col * (0.5 + 0.55 * clamp(amt, 0.0, 1.0));
}

vec3 aurora(vec3 rd, float t, float amt, vec3 tint) {
    if (rd.y < 0.03) {
        return vec3(0.0);
    }
    vec2 au = rd.xz / max(rd.y, 0.03);
    float band = fbm2(vec2(au.x * 1.05 - t * 0.02, au.y * 0.85));
    float rays = fbm2(vec2(au.x * 3.2 - t * 0.055, au.y * 0.35 + band * 1.4));
    float m = pow(max(band - 0.44, 0.0) * 1.9 + 1.0E-4, 2.1) * (0.45 + 0.7 * rays);
    m *= exp(-max(rd.y - 0.06, 0.0) * 1.7);
    m *= smoothstep(0.03, 0.16, rd.y);
    vec3 g = mix(vec3(0.06, 0.85, 0.52), vec3(0.4, 0.32, 0.95), clamp(rd.y * 2.4, 0.0, 1.0));
    g = mix(g, tint * 1.25, 0.28);
    return g * m * 0.4 * (0.45 + 0.6 * clamp(amt, 0.0, 1.0));
}

// ------------------------------------------------------------------ scenes

vec3 nightScene(vec3 rd, float t, float amt, vec3 tint) {
    vec3 col = skyGradient(rd.y, vec3(0.012, 0.02, 0.052), vec3(0.028, 0.05, 0.115), vec3(0.075, 0.115, 0.21));
    vec3 sd = normalize(rd);
    col += stars(sd, t, amt);
    col += milkyWay(sd, t, 0.5);
    vec3 md = moonDirOf(t);
    col += moonBody(rd, md, amt);
    col += aurora(rd, t, amt, tint);
    gLightDir = md;
    vec4 c1 = cloudLayer(rd, 1100.0, 0.0016, 0.01, 0.66, 0.02, 0.8,
                         vec3(0.5, 0.56, 0.72), vec3(0.06, 0.08, 0.16), col * 0.9, t, amt, 0.00013);
    col = mix(col, c1.rgb, c1.a);
    vec4 c2 = cloudLayer(rd, 380.0, 0.0032, 0.016, 0.6, 0.035, 1.0,
                         vec3(0.42, 0.48, 0.65), vec3(0.045, 0.06, 0.12), col * 0.9, t, amt, 0.00022);
    col = mix(col, c2.rgb, c2.a);
    return col;
}

vec3 dayScene(vec3 rd, float t, float amt, vec3 tint) {
    vec3 sd0 = daySunDirOf(t);
    gLightDir = sd0;
    vec3 col = skyGradient(rd.y, vec3(0.13, 0.34, 0.76), vec3(0.35, 0.57, 0.89), vec3(0.72, 0.83, 0.94));
    col = mix(col, tint, 0.045);
    col += daySun(rd, sd0, amt);
    col += vec3(0.9, 0.95, 1.0) * exp(-abs(rd.y) * 9.0) * 0.1;
    vec4 c1 = cloudLayer(rd, 950.0, 0.0021, 0.011, 0.64, 0.022, 0.8,
                         vec3(1.03, 1.02, 1.0), vec3(0.58, 0.65, 0.78), col * 0.98, t, amt, 0.00013);
    col = mix(col, c1.rgb, c1.a);
    vec4 c2 = cloudLayer(rd, 320.0, 0.0042, 0.02, 0.52, 0.04, 1.0,
                         vec3(1.1, 1.08, 1.04), vec3(0.5, 0.56, 0.7), col * 0.98, t, amt, 0.00022);
    col = mix(col, c2.rgb, c2.a);
    return col;
}

vec3 morningScene(vec3 rd, float t, float amt, vec3 tint) {
    vec3 sd0 = morningSunDirOf(t);
    gLightDir = sd0;
    vec3 col = skyGradient(rd.y, vec3(0.23, 0.32, 0.58), vec3(0.55, 0.47, 0.6), vec3(0.98, 0.6, 0.36));
    col = mix(col, tint, 0.09);
    col += morningSun(rd, sd0, amt);
    col += vec3(1.0, 0.55, 0.3) * exp(-abs(rd.y) * 7.0) * 0.14;
    vec4 c1 = cloudLayer(rd, 1000.0, 0.002, 0.01, 0.62, 0.02, 0.8,
                         vec3(1.0, 0.85, 0.72), vec3(0.4, 0.42, 0.58), col * 0.97, t, amt, 0.00013);
    col = mix(col, c1.rgb, c1.a);
    vec4 c2 = cloudLayer(rd, 340.0, 0.004, 0.018, 0.54, 0.038, 1.0,
                         vec3(1.05, 0.8, 0.62), vec3(0.34, 0.36, 0.52), col * 0.97, t, amt, 0.00022);
    col = mix(col, c2.rgb, c2.a);
    return col;
}

vec3 spaceScene(vec3 rd0, float t, float amt, vec3 tint) {
    // the whole scene slowly drifts around the world Y axis
    float rot = t * 0.0022;
    float cs = cos(rot);
    float sn = sin(rot);
    vec3 rd = normalize(vec3(rd0.x * cs - rd0.z * sn, rd0.y, rd0.x * sn + rd0.z * cs));

    vec3 col = vec3(0.005, 0.006, 0.014);

    // nebula
    float n1 = fbm3(rd * 2.1 + vec3(t * 0.0028, 0.0, 0.0));
    float n2 = fbm3(rd * 4.4 - n1 * 1.5 + 5.23);
    vec3 neb = mix(vec3(0.32, 0.1, 0.55), vec3(0.03, 0.32, 0.6), n2);
    neb = mix(neb, tint * 1.1, 0.22);
    col += neb * pow(max(n1 - 0.3, 0.0) * 1.85 + 1.0E-4, 2.3) * 0.85;
    col *= 1.0 - 0.55 * smoothstep(0.42, 0.78, fbm3(rd * 3.3 + 9.94));

    // galaxy band + star fields
    col += milkyWay(rd, t, 1.0);
    col += stars(rd, t, amt) * 1.3;
    col += stars(normalize(rd * 1.13 + vec3(0.017)), t * 1.9 + 41.0, amt) * 0.55;

    // ringed planet
    vec3 C = vec3(-0.42, 0.3, -0.6) * 60.0;
    float R = 10.5;
    vec3 sunD = normalize(vec3(0.7, 0.35, 0.4));
    float b = dot(rd, C);
    float cc = dot(C, C) - R * R;
    float disc2 = b * b - cc;
    float planetT = -1.0;
    vec3 rn = normalize(vec3(0.25, 0.88, 0.38));
    if (disc2 > 0.0) {
        float sq = sqrt(disc2);
        float tN = b - sq;
        if (tN > 0.0) {
            planetT = tN;
            vec3 P = rd * tN;
            vec3 n = normalize(P - C);
            // Сатурн: газовый гигант. Ось вращения = нормаль колец,
            // поэтому полосы идут параллельно кольцам.
            float ax = dot(n, rn);
            vec3 eqX = normalize(cross(rn, vec3(1.0, 0.0, 0.0)));
            vec3 eqZ = cross(rn, eqX);
            // вращение планеты вокруг оси (Родригес), затем дифференциальный
            // сдвиг полос: каждая широта течёт со своей скоростью
            float rot = t * 0.024;
            float cr = cos(rot);
            float sr = sin(rot);
            vec3 nr = normalize(n * cr + cross(rn, n) * sr);
            float rot2 = t * 0.024 * (1.0 + 0.45 * sin(ax * 6.0 + 0.8));
            float c2r = cos(rot2);
            float s2r = sin(rot2);
            vec3 ns = normalize(nr * c2r + cross(rn, nr) * s2r);
            float su = dot(ns, eqX);
            float sv = dot(ns, eqZ);
            // искажение границ полос вихрями
            float wob = fbm3(nr * 2.6 + vec3(t * 0.006, 0.0, 0.0))
                      + 0.45 * fbm3(nr * 6.5 - vec3(t * 0.011, 0.0, 0.0));
            float band = ax * 11.0 + 1.15 * wob;
            // палитра полос Сатурна: кремовый, золотой, охра, серо-голубой
            float bm = fbm2(vec2(band * 0.5, 3.7));
            vec3 surf = mix(vec3(0.74, 0.60, 0.40), vec3(0.87, 0.77, 0.56), smoothstep(0.30, 0.70, bm));
            surf = mix(surf, vec3(0.58, 0.45, 0.31), smoothstep(0.58, 0.86, fbm2(vec2(band * 0.85, 7.3))) * 0.85);
            surf = mix(surf, vec3(0.64, 0.64, 0.62), smoothstep(0.52, 0.78, fbm2(vec2(band * 0.4, 11.1))) * 0.55);
            surf = mix(surf, vec3(0.93, 0.87, 0.70), smoothstep(0.62, 0.88, fbm2(vec2(band * 0.65, 1.9))) * 0.7);
            // вытянутые вдоль течения штрихи
            float streaks = fbm3(vec3(su * 2.0, ax * 9.0, sv * 2.0) + vec3(0.0, t * 0.005, 0.0));
            surf *= 0.88 + 0.26 * streaks;
            // овальные шторма: светлый и тёмный, вращаются вместе с планетой
            float lon1 = atan(dot(nr, eqZ), dot(nr, eqX));
            float storm1 = smoothstep(0.42, 0.05, length(vec2((lon1 - 2.1) * 0.9, (ax - 0.34) * 2.6)));
            surf = mix(surf, vec3(0.96, 0.93, 0.86), storm1 * 0.75);
            float storm2 = smoothstep(0.36, 0.04, length(vec2((lon1 + 2.2) * 0.9, (ax + 0.3) * 3.0)));
            surf = mix(surf, vec3(0.46, 0.36, 0.26), storm2 * 0.6);
            // затемнённые полюса
            surf = mix(surf, vec3(0.42, 0.44, 0.47), smoothstep(0.78, 0.98, abs(ax)) * 0.55);
            // освещение с мягким терминатором
            float ndl = dot(n, sunD);
            float dayv = smoothstep(-0.12, 0.25, ndl);
            // тень колец от солнца
            float rsh = 0.0;
            float denom2 = dot(sunD, rn);
            if (abs(denom2) > 1.0E-4) {
                float ts2 = dot(C - P, rn) / denom2;
                if (ts2 > 0.0) {
                    float rr2 = length(P + sunD * ts2 - C);
                    rsh = smoothstep(13.5, 14.2, rr2) * (1.0 - smoothstep(19.6, 20.4, rr2));
                }
            }
            // лимбовое затемнение — у газовых гигантов край диска темнее
            float mu = clamp(dot(n, -rd), 0.0, 1.0);
            float limb = 0.5 + 0.5 * pow(mu, 0.5);
            float lightv = (0.05 + 1.12 * clamp(ndl, 0.0, 1.0)) * (1.0 - 0.72 * rsh);
            col = surf * lightv * limb;
            // тёплое рассеяние у терминатора
            col += vec3(0.55, 0.34, 0.16) * pow(max(1.0 - abs(ndl), 0.0), 3.0) * 0.22;
            // золотистая атмосферная кайма
            float fres = pow(max(1.0 - max(dot(n, -rd), 0.0), 0.0) + 1.0E-4, 2.4);
            col += vec3(0.95, 0.78, 0.5) * fres * (0.28 + 0.8 * dayv);
        }
    }

    // atmosphere halo around the planet silhouette (warm, like Saturn's haze)
    float closest = sqrt(max(cc + R * R - b * b, 0.0));
    float halo = 1.0 - smoothstep(R * 0.98, R + 7.0, closest);
    col += vec3(0.95, 0.78, 0.5) * halo * halo * 0.16
         * (0.6 + 0.4 * clamp(dot(rd, sunD) * 0.5 + 0.5, 0.0, 1.0));

    // rings (hidden where the planet body is in front of them)
    float denom = dot(rd, rn);
    if (abs(denom) > 1.0E-4) {
        float tr = dot(C, rn) / denom;
        if (tr > 0.0 && (planetT < 0.0 || tr < planetT)) {
            vec3 Q = rd * tr - C;
            float rr = length(Q);
            if (rr > 13.5 && rr < 20.5) {
                float bandv = fbm2(vec2(rr * 1.7, 0.37));
                float rings = smoothstep(0.34, 0.6, bandv)
                            * (1.0 - smoothstep(19.2, 20.5, rr))
                            * smoothstep(13.5, 14.6, rr);
                // Cassini-like division
                rings *= 1.0 - 0.9 * smoothstep(16.8, 17.0, rr) * (1.0 - smoothstep(17.35, 17.6, rr));
                rings *= 0.75 + 0.25 * fbm2(vec2(rr * 5.0, 2.0));
                if (rings > 0.003) {
                    vec3 rcol = mix(vec3(0.72, 0.6, 0.46), vec3(0.92, 0.86, 0.72), bandv);
                    float lightv = 0.22 + 0.9 * clamp(dot(normalize(Q), sunD) * 0.5 + 0.5, 0.0, 1.0);
                    col = mix(col, rcol * lightv, rings * 0.9);
                }
            }
        }
    }
    return col;
}

// -------------------------------------------------------------------- main

void main() {
    float amt = clamp(intensity, 0.0, 1.5);
    if (amt < 0.005) {
        discard;
    }
    float aspect = resolution.x / max(resolution.y, 1.0);
    vec3 rd = normalize(camForward.xyz + camRight.xyz * (vNDC.x * fovScale * aspect)
                        + camUp.xyz * (vNDC.y * fovScale));
    vec3 tint = clamp(fogColor.rgb, vec3(0.0), vec3(1.0));
    int mode = int(floor(fogMode + 0.5));
    vec3 col;
    if (mode == 1) {
        col = dayScene(rd, time, amt, tint);
    } else if (mode == 2) {
        col = morningScene(rd, time, amt, tint);
    } else if (mode == 3) {
        col = spaceScene(rd, time, amt, tint);
    } else {
        col = nightScene(rd, time, amt, tint);
    }
    if (any(isnan(col))) {
        fragColor = vec4(tint, 1.0);
        return;
    }
    fragColor = vec4(clamp(col, vec3(0.0), vec3(1.0)), 1.0);
}
