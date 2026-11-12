package ru.method.clarityvisuals.modules.impl.render.killeffect;

import static ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectRenderer.*;

/**
 * Body bloom: the fallen stays as a constellation of glowing orbs laid out
 * along the body silhouette, then scatters across the floor with real physics.
 */
final class KillEffectStylesB {
    private KillEffectStylesB() {
    }

    /** Orb anchors tracing a human silhouette: (x, y, z, radius) per row. */
    private static final float[][] BLOOM_ANCHORS = {
            {0.00f, 1.78f, 0.00f, 0.46f},
            {0.00f, 1.44f, 0.00f, 0.40f},
            {-0.18f, 1.36f, 0.00f, 0.32f},
            {0.18f, 1.36f, 0.00f, 0.32f},
            {0.00f, 1.06f, 0.00f, 0.36f},
            {-0.15f, 0.82f, 0.00f, 0.34f},
            {0.15f, 0.82f, 0.00f, 0.34f},
            {-0.30f, 1.50f, 0.00f, 0.30f},
            {0.30f, 1.50f, 0.00f, 0.30f},
            {-0.38f, 1.14f, 0.00f, 0.26f},
            {0.38f, 1.14f, 0.00f, 0.26f},
            {-0.42f, 0.76f, 0.00f, 0.24f},
            {0.42f, 0.76f, 0.00f, 0.24f},
            {-0.13f, 0.52f, 0.00f, 0.30f},
            {0.13f, 0.52f, 0.00f, 0.30f},
            {-0.15f, 0.28f, 0.00f, 0.26f},
            {0.15f, 0.28f, 0.00f, 0.26f},
            {-0.16f, 0.08f, 0.00f, 0.24f},
            {0.16f, 0.08f, 0.00f, 0.24f},
            {-0.45f, 0.62f, 0.10f, 0.14f},
            {0.50f, 0.50f, -0.12f, 0.15f},
            {-0.28f, 0.95f, -0.16f, 0.12f},
            {0.33f, 1.02f, 0.14f, 0.13f},
            {0.05f, 0.35f, 0.20f, 0.16f},
            {-0.05f, 0.18f, -0.22f, 0.13f}
    };
    private static final int BLOOM_BODY_COUNT = 19;

    /**
     * Body bloom: the fallen stays as a constellation of glowing orbs laid out
     * along the body silhouette. They detach one by one and scatter with real
     * drop physics — gravity, floor rebounds with restitution, splash rings,
     * rolling drag and a final settle.
     */
    static void bodyBloom(Ctx c) {
        int primary = c.effect.primary();
        int secondary = c.effect.secondary();
        int accent = c.effect.accent();
        float fade = env(c);
        float overall = fade * (1.0f - smooth(clamp((c.t - 0.72f) / 0.28f)));
        ringAt(c, 0.02, 1.55f, col(secondary, 0.16f * overall), true);

        float gravity = 8.6f;
        float dt = 1.0f / 90.0f;
        float spawnScale = smooth(clamp(c.age / 0.18f));
        for (int k = 0; k < BLOOM_ANCHORS.length; k++) {
            float[] anchor = BLOOM_ANCHORS[k];
            float x0 = anchor[0];
            float y0 = anchor[1];
            float z0 = anchor[2];
            float size = anchor[3] * c.scale * spawnScale;
            if (size <= 0.01f) {
                continue;
            }
            boolean spill = k >= BLOOM_BODY_COUNT;
            float delay = spill ? 0.02f + 0.05f * rnd(c, k, 9)
                    : Math.max(0.0f, (1.92f - y0) * 0.105f) + 0.08f * rnd(c, k, 10);
            float localAge = c.age - delay;
            int orbColor = switch (k % 3) {
                case 0 -> primary;
                case 1 -> accent;
                default -> secondary;
            };
            float alpha = overall;
            if (localAge <= 0.0f) {
                // Still part of the body: a soft breathing bob at the anchor.
                float bob = 0.03f * (float) Math.sin(c.age * 2.6f + k);
                sprite(c, x0, y0 + bob, z0, size, BLOB, col(orbColor, alpha), true, false);
                sprite(c, x0, y0 + bob, z0, size * 1.7f, GLOW, col(orbColor, alpha * 0.35f), true, true);
                sprite(c, x0, y0 + bob, z0, size * 0.4f, STAR, col(0xFFFFFF, alpha * 0.8f), true, true);
                continue;
            }

            // Launch: body orbs roll outward off the silhouette with a little
            // upward pop (the head gets a jump), spill orbs fly loose.
            float side = x0 < -0.01f ? -1.0f : (x0 > 0.01f ? 1.0f : (rnd(c, k, 11) > 0.5f ? 1.0f : -1.0f));
            float push = spill ? 1.25f : 1.0f;
            float vx = side * (0.45f + 1.1f * rnd(c, k, 12)) * push;
            float vz = (rnd(c, k, 13) - 0.5f) * 1.7f * push;
            float vy = 0.3f + 0.8f * rnd(c, k, 14) + (k == 0 ? 0.9f : 0.15f);
            float radius = size * 0.5f;
            float px = x0;
            float py = y0;
            float pz = z0;
            boolean resting = false;
            float sinceBounce = 0.3f;
            float bounceStrength = 0.0f;
            int steps = Math.min(430, Math.round(localAge / dt));
            for (int s = 0; s < steps; s++) {
                if (!resting) {
                    vy -= gravity * dt;
                    px += vx * dt;
                    py += vy * dt;
                    pz += vz * dt;
                    if (py < radius) {
                        float impact = -vy;
                        py = radius;
                        if (impact > 0.55f) {
                            // Rebound: bounce back up losing a bit of pace.
                            vy = impact * 0.48f;
                            vx *= 0.68f;
                            vz *= 0.68f;
                            sinceBounce = 0.0f;
                            bounceStrength = Math.min(1.0f, impact * 0.3f);
                        } else {
                            // Too weak to bounce again, start rolling.
                            resting = true;
                            vy = 0.0f;
                        }
                    }
                } else {
                    px += vx * dt;
                    pz += vz * dt;
                    vx *= 0.985f;
                    vz *= 0.985f;
                }
                sinceBounce += dt;
            }

            // Soft bloom ball with a fake top light.
            sprite(c, px, py, pz, size, BLOB, col(orbColor, alpha), true, false);
            sprite(c, px, py, pz, size * 1.7f, GLOW, col(orbColor, alpha * 0.35f), true, true);
            sprite(c, px, py + size * 0.22f, pz, size * 0.4f, STAR, col(0xFFFFFF, alpha * 0.8f), true, true);
            if (!resting && sinceBounce < 0.2f && bounceStrength > 0.05f) {
                float ringP = sinceBounce / 0.2f;
                float ringR = radius * (2.0f + 3.5f * ringP);
                flatAt(c, px, pz, 0.02, ringR, RING,
                        col(orbColor, bounceStrength * (1.0f - ringP) * 0.55f * fade), true);
            }
        }
    }
}
