#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform float time;
uniform vec2 screenSize;

const float DITHER_LEVELS = 1.25;
const float MOTION        = 1.0;
const float SPEED         = 0.09;
const float COLOR_SPEED   = 0.0035;
const float SHAPE_MOTION  = 1.0;
const float SHEEN         = 0.75;
const int EXTRA_RIBBONS   = 2;

float square(float x)
{
    return x * x;
}

float fourthPower(float x)
{
    float x2 = x * x;
    return x2 * x2;
}

// An elliptical patch of light, with independent horizontal
// and vertical falloff.
float gaussian(vec2 p, vec2 center, vec2 extent)
{
    vec2 q = (p - center) / extent;
    return exp(-dot(q, q));
}

// A quadratic ribbon edge: x = a + b*y + c*y*y.
float curve(float y, float a, float b, float c)
{
    return a + b * y + c * y * y;
}

float lineLight(float distanceToCurve, float width)
{
    return exp(-abs(distanceToCurve) / width);
}

// Each layer gets a different phase so its highlight travels
// independently of the others.
float travelingSheen(float along, float layer, float clock)
{
    float center = 0.50 + 0.66 * sin(clock * 0.47 - layer * 0.80);
    float width = 0.15 + 0.025 * sin(layer * 1.7 + clock * 0.19);
    float q = (along - center) / width;

    return exp(-q * q) * SHEEN;
}

// Rotate RGB around the gray axis. Neutral highlights remain
// unchanged by the rotation.
vec3 hueShift(vec3 color, float hue)
{
    vec3 axis = normalize(vec3(1.0));
    float a = hue * 6.2831853;

    vec3 shifted = color * cos(a)
                 + cross(axis, color) * sin(a)
                 + axis * dot(axis, color) * (1.0 - cos(a));

    return max(shifted, 0.0);
}

void mainImage(out vec4 outColor, in vec2 fragCoord, in vec2 iResolution, in float iTime)
{
    vec2 uv = fragCoord / iResolution.xy;
    uv.y = 1.0 - uv.y;

    float t = iTime * SPEED;
    float breath = sin(t) * MOTION;
    float evolution = t * MOTION * SHAPE_MOTION;

    // Move the coordinate field so every layer shares the same
    // overall rotation, drift and breathing motion.
    float angle = evolution * 0.16
                + 0.40 * sin(evolution * 0.31);

    mat2 turn = mat2(cos(angle), -sin(angle),
                    sin(angle),  cos(angle));

    float zoom = 1.10 + 0.12 * sin(evolution * 0.27);
    vec2 p = turn * (uv - 0.5) * zoom + vec2(0.60, 0.48);

    p += vec2(0.10 * sin(evolution * 0.34),
              0.09 * sin(evolution * 0.23));

    // Low-frequency warping bends the simple curves into flowing
    // shapes. The second warp uses the already-warped x coordinate.
    p.x += 0.18 * sin(evolution * 0.38)
         * sin(p.y * 3.8 + evolution * 0.28);
    p.y += 0.08 * sin(evolution * 0.30)
         * sin(p.x * 4.0 - evolution * 0.17);
    p.x += 0.025 * breath * sin(uv.y * 2.6 + 0.3);
    p.y += 0.012 * sin(t * 0.73) * MOTION;

    // Broad pools of color establish the lighting beneath the silk.
    vec3 col = vec3(0.016, 0.025, 0.030);

    col += vec3(0.12, 0.19, 0.24)
         * gaussian(p, vec2(0.26, -0.08), vec2(0.76, 0.47));
    col += vec3(0.22, 0.35, 0.44)
         * gaussian(p, vec2(0.29, -0.12), vec2(0.32, 0.20));
    col += vec3(0.43, 0.43, 0.41)
         * gaussian(p, vec2(0.63, -0.07), vec2(0.26, 0.34));
    col += vec3(0.15, 0.14, 0.12)
         * gaussian(p, vec2(0.63, 0.27), vec2(0.30, 0.37));
    col += vec3(0.38, 0.235, 0.11)
         * gaussian(p, vec2(0.93, 0.35), vec2(0.23, 0.29));
    col += vec3(0.065, 0.038, 0.012)
         * gaussian(p, vec2(0.84, 0.65), vec2(0.23, 0.36));
    col += vec3(0.13, 0.13, 0.12)
         * gaussian(p, vec2(0.61, -0.055), vec2(0.18, 0.18));
    col += vec3(0.095, 0.080, 0.025)
         * gaussian(p, vec2(0.88, 0.35), vec2(0.085, 0.21));

    float y = p.y;
    float aa = 0.7 / iResolution.x;

    // Main silk sheets, each enclosed by two curved edges.
    for (int i = 0; i < 3; ++i)
    {
        float f = float(i);

        float left = curve(
            y,
            0.901 + 0.042 * f,
            -0.030 - 0.070 * f,
            -0.19 - 0.018 * f
        );
        float right = curve(
            y,
            1.19 + 0.045 * f,
            -0.32 - 0.032 * f,
            -0.36
        );

        float flex = 0.075 * sin(t * 0.83 + f * 0.9)
                   * sin(y * 3.14159) * MOTION;
        left += flex;
        right += flex * 0.45;

        float lo = min(left, right);
        float hi = max(left, right);

        // Two softened steps form a mask between the edges.
        float band = smoothstep(lo - aa, lo + aa, p.x)
                   * (1.0 - smoothstep(hi - aa, hi + aa, p.x));

        // Local position across the sheet, from one edge to the other.
        float across = clamp(
            (p.x - lo) / max(hi - lo, 0.001), 0.0, 1.0
        );

        float silk = pow(1.0 - across, 1.7) * 0.8 + 0.12;
        float light = gaussian(
            p, vec2(0.90, 0.34), vec2(0.15, 0.32)
        );

        col += band * silk * light * vec3(0.16, 0.125, 0.052);
        col *= 1.0 - band * 0.055 * smoothstep(0.56, 0.95, y);
        col += lineLight(p.x - left, 0.0006)
             * light * vec3(0.055, 0.038, 0.013);

        float sheen = travelingSheen(y, f, t * MOTION);
        float ridge = exp(-square((across - 0.24) / 0.28));

        // A fourth-power envelope has a flatter center than a Gaussian.
        // It lights a long stretch of silk before fading toward the ends.
        float taper = exp(-fourthPower((y - 0.45) / 0.66));

        col += vec3(0.115, 0.102, 0.076)
             * band * ridge * sheen * taper;
        col += vec3(0.15, 0.14, 0.11)
             * lineLight(p.x - left, max(aa, 0.0012))
             * sheen * taper;
    }

    // Wider ribbons fill the open side of the composition.
    for (int i = 0; i < EXTRA_RIBBONS; ++i)
    {
        float f = float(i);

        float center = curve(
            y, 0.30 + 0.18 * f, 0.24 - 0.065 * f, -0.26
        );
        center += 0.045 * sin(t * 0.58 + f * 1.4)
                * sin(y * 2.8 + f * 0.35) * MOTION;

        float width = 0.055 + 0.012 * f
                    + 0.014 * sin(y * 3.2 + f);
        float lo = center - width;
        float hi = center + width;

        float band = smoothstep(lo - aa, lo + aa, p.x)
                   * (1.0 - smoothstep(hi - aa, hi + aa, p.x));
        float across = clamp(
            (p.x - lo) / (2.0 * width), 0.0, 1.0
        );

        float taper = exp(-fourthPower((y - 0.43) / 0.72));
        float satin = exp(-square((across - 0.30) / 0.36));
        float sheen = travelingSheen(y, f + 3.8, t * MOTION);

        vec3 tint = mix(
            vec3(0.30, 0.39, 0.46),
            vec3(0.66, 0.47, 0.20),
            smoothstep(0.08, 0.78, y)
        );

        // Slight darkening beneath the highlights separates overlapping
        // layers without needing actual geometry or cast shadows.
        col *= 1.0 - band * taper * 0.07;
        col += tint * band * taper
             * (0.12 + satin * (0.20 + 0.40 * sheen));

        col += mix(tint, vec3(0.85, 0.83, 0.72), 0.45)
             * taper * lineLight(p.x - lo, max(aa, 0.0010))
             * (0.12 + 0.35 * sheen);
        col += tint * taper
             * lineLight(p.x - hi, max(aa, 0.0015)) * 0.09;
    }

    // Fine filaments have their own curves and motion phases.
    for (int i = 0; i < 6; ++i)
    {
        float f = float(i);
        float x;

        if      (i == 0) x = curve(y, 0.904, -0.095, -0.205);
        else if (i == 1) x = curve(y, 0.950, -0.150, -0.200);
        else if (i == 2) x = curve(y, 0.913, -0.025, -0.350);
        else if (i == 3) x = curve(y, 0.746,  0.280, -0.530);
        else if (i == 4) x = curve(y, 0.923, -0.230, -0.180);
        else             x = curve(y, 0.897, -0.012, -0.390);

        x += 0.065 * sin(t * (0.6 + f * 0.08) + f)
           * sin(y * 3.14159) * MOTION;

        // Approximate width compensation as the curves bend.
        float d = (p.x - x) / sqrt(1.0 + square(0.3 + y * 0.5));
        float envelope = exp(-square((y - 0.37) / 0.32));
        float strength = i < 3 ? 1.0 : 0.15;

        vec3 tint = mix(
            vec3(0.72, 0.77, 0.78),
            vec3(1.0, 0.64, 0.24),
            smoothstep(0.0, 0.75, y)
        );

        col += tint * strength * (0.035 + envelope * 0.75)
             * lineLight(d, max(aa, 0.00065));
        col += vec3(1.0, 0.55, 0.13)
             * strength * envelope * 0.08 * lineLight(d, 0.008);

        float glint = travelingSheen(y, f * 0.55, t * MOTION);
        vec3 pearl = mix(tint, vec3(1.0, 0.96, 0.88), 0.65);

        col += pearl * strength * glint * envelope * 0.20
             * lineLight(d, max(aa, 0.0010));
        col += pearl * strength * glint * envelope * 0.012
             * lineLight(d, 0.006);
    }

    // A shallow fan crosses beneath the main ribbons.
    for (int i = 0; i < 3; ++i)
    {
        float f = float(i);
        float x = p.x;

        float edge = 1.20 - 0.48 * x * x - 0.028 * f
                   + 0.055 * sin(t * 0.65 + f) * MOTION;
        float d = p.y - edge;

        float light = gaussian(
            p, vec2(0.87, 0.80), vec2(0.18, 0.14)
        );
        float sheet = smoothstep(-0.065, 0.0, d)
                    * (1.0 - smoothstep(0.0, aa, d));

        col += vec3(0.13, 0.088, 0.036) * sheet * light;
        col += vec3(0.75, 0.45, 0.16)
             * lineLight(d, 0.0010) * light
             * (i == 2 ? 0.8 : 0.12);

        float sheen = travelingSheen(x, f + 2.4, t * MOTION);

        col += vec3(0.10, 0.085, 0.062) * sheet * sheen
             * exp(-square((x - 0.76) / 0.4));
        col += vec3(0.18, 0.15, 0.10)
             * lineLight(d, max(aa, 0.0011)) * sheen * light;
    }

    col += vec3(0.055, 0.038, 0.012)
         * sin(t * 1.15) * MOTION
         * gaussian(
             p,
             vec2(0.88 + 0.025 * sin(t), 0.38),
             vec2(0.17, 0.30)
         );

    // Slowly cycle the palette, with a small spatial variation so
    // different parts of the silk do not change color in lockstep.
    float hue = iTime * COLOR_SPEED * MOTION;
    hue += 0.09 * sin(t * 0.22) * sin(p.y * 2.3 + p.x);
    col = hueShift(col, hue);

    // Fine monochrome grain reduces banding in the soft gradients.
    float grain = fract(sin(dot(fragCoord + mod(iTime * 60.0, 3600.0), vec2(12.9898, 78.233))) * 43758.5453);
    col += (grain * 2.0 - 1.0) * (DITHER_LEVELS / 255.0);

    outColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}

void main() {
    vec2 fragCoord = texCoord * screenSize;
    vec4 outColor;
    mainImage(outColor, fragCoord, screenSize, time);
    fragColor = outColor;
}
