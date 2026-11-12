package ru.method.clarityvisuals.modules.impl.render.killeffect;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectCatalog.Effect;
import ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectManager.ActiveEffect;
import ru.method.clarityvisuals.utils.render.Render3D;

/**
 * Procedural renderer for every effect in {@link KillEffectCatalog}. The
 * original Blockbench models are not part of this project, so each effect is
 * rebuilt from the client's particle textures, coloured geometry and light
 * trails while keeping the original catalogue order and identities.
 */
public final class KillEffectRenderer implements MinecraftClientAccess {
    static final class_2960 GLOW = ClarityVisuals.id("client/xuynya/ghost_bloom.png");
    static final class_2960 RING = ClarityVisuals.id("client/other/circle.png");
    static final class_2960 MIST = ClarityVisuals.id("client/particles/mist_puff.png");
    static final class_2960 SAND = ClarityVisuals.id("client/particles/sand_puff.png");
    static final class_2960 PETAL = ClarityVisuals.id("client/particles/sakura_petal.png");
    static final class_2960 STAR = ClarityVisuals.id("client/particles/star.png");
    static final class_2960 SNOW = ClarityVisuals.id("client/particles/snow.png");
    static final class_2960 SPARK = ClarityVisuals.id("client/particles/firefly.png");
    static final class_2960 FEATHER = ClarityVisuals.id("client/killeffect/feather_white.png");
    static final class_2960 SQUARE = ClarityVisuals.id("client/killeffect/square.png");
    static final class_2960 BUBBLE = ClarityVisuals.id("client/killeffect/bubble.png");
    static final class_2960 SLASH = ClarityVisuals.id("client/killeffect/slash.png");
    static final class_2960 BLOB = ClarityVisuals.id("client/killeffect/blob.png");

    static final float TAU = (float) (Math.PI * 2.0);
    private static final Set<Effect> FAILED = EnumSet.noneOf(Effect.class);

    private KillEffectRenderer() {
    }

    public static void render(class_4587 stack, List<ActiveEffect> effects) {
        for (ActiveEffect active : effects) {
            Effect effect = active.effect();
            if (FAILED.contains(effect) || active.isFinished() || !near(active.position())) {
                continue;
            }
            try {
                KillEffectStyles.render(new Ctx(stack, active));
            } catch (RuntimeException exception) {
                FAILED.add(effect);
                ClarityVisuals.LOGGER.error("Failed to render kill effect {}", effect.id(), exception);
            }
        }
    }

    private static boolean near(class_243 position) {
        if (aiG.field_1773 == null) {
            return false;
        }
        return aiG.field_1773.method_19418().method_19326().method_1025(position) < 14400.0;
    }

    static final class Ctx {
        final class_4587 stack;
        final Effect effect;
        final class_243 origin;
        final float yaw;
        final float age;
        final float t;
        final float scale;
        final long seed;
        final double cos;
        final double sin;

        Ctx(class_4587 stack, ActiveEffect active) {
            this.stack = stack;
            this.effect = active.effect();
            this.origin = active.position();
            this.yaw = active.spawnYaw();
            this.age = active.timeSeconds();
            this.t = clamp(this.age / this.effect.duration());
            this.scale = this.effect.scale();
            this.seed = active.seed();
            this.cos = Math.cos(Math.toRadians(this.yaw));
            this.sin = Math.sin(Math.toRadians(this.yaw));
        }

        class_243 at(double x, double y, double z) {
            double rx = x * this.cos - z * this.sin;
            double rz = x * this.sin + z * this.cos;
            return new class_243(this.origin.field_1352 + rx, this.origin.field_1351 + y, this.origin.field_1350 + rz);
        }
    }

    static float env(Ctx c) {
        return smooth(clamp(c.t / 0.12f)) * (1.0f - smooth(clamp((c.t - 0.58f) / 0.42f)));
    }

    static float clamp(float value) {
        return value < 0.0f ? 0.0f : (value > 1.0f ? 1.0f : value);
    }

    static float smooth(float value) {
        float x = clamp(value);
        return x * x * (3.0f - 2.0f * x);
    }

    static float easeOut(float value) {
        float x = 1.0f - clamp(value);
        return 1.0f - x * x * x;
    }

    static float fract(float value) {
        return value - (float) Math.floor(value);
    }

    static float rnd(Ctx c, int index, int salt) {
        long value = c.seed
                + 0x9E3779B97F4A7C15L * (index + 1L)
                + 0xC2B2AE3D27D4EB4FL * (salt + 1L);
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 40) / 16777216.0f;
    }

    static int col(int rgb, float alpha) {
        int a = Math.round(clamp(alpha) * 255.0f);
        return a << 24 | (rgb & 0xFFFFFF);
    }

    static int shade(int argb, float factor) {
        int a = argb & 0xFF000000;
        int r = Math.min(255, Math.max(0, Math.round(((argb >>> 16) & 0xFF) * factor)));
        int g = Math.min(255, Math.max(0, Math.round(((argb >>> 8) & 0xFF) * factor)));
        int b = Math.min(255, Math.max(0, Math.round((argb & 0xFF) * factor)));
        return a | r << 16 | g << 8 | b;
    }

    static int mix(int first, int second, float factor) {
        float f = clamp(factor);
        int a = Math.round(((first >>> 24) & 0xFF) * (1.0f - f) + ((second >>> 24) & 0xFF) * f);
        int r = Math.round(((first >>> 16) & 0xFF) * (1.0f - f) + ((second >>> 16) & 0xFF) * f);
        int g = Math.round(((first >>> 8) & 0xFF) * (1.0f - f) + ((second >>> 8) & 0xFF) * f);
        int b = Math.round((first & 0xFF) * (1.0f - f) + (second & 0xFF) * f);
        return a << 24 | r << 16 | g << 8 | b;
    }

    static int rainbow(float hue) {
        return Color.HSBtoRGB(hue - (float) Math.floor(hue), 0.78f, 1.0f) & 0xFFFFFF;
    }

    static Color argb(int argb) {
        return new Color(argb, true);
    }

    static void sprite(Ctx c, double x, double y, double z, float size, class_2960 texture, int color, boolean depth, boolean additive) {
        if (size <= 0.01f || (color >>> 24) == 0) {
            return;
        }
        Render3D.renderBillboardTexture(c.at(x, y, z), size * c.scale, texture, color, depth, additive);
    }

    static void flatAt(Ctx c, double x, double z, double y, float radius, class_2960 texture, int color, boolean additive) {
        if (radius <= 0.01f) {
            return;
        }
        Render3D.renderOrientedTexture(c.at(x, y, z), radius * 2.0f * c.scale, texture, color, class_2350.field_11036, true, additive);
    }

    static void flat(Ctx c, double y, float radius, class_2960 texture, int color, boolean additive) {
        flatAt(c, 0.0, 0.0, y, radius, texture, color, additive);
    }

    static void ringAt(Ctx c, double y, float radius, int color, boolean additive) {
        flatAt(c, 0.0, 0.0, y, radius * 1.08f, RING, color, additive);
    }

    /** Vertical circle facing the player, used for portals and clock faces. */
    static void vring(Ctx c, double y, float radius, int color, float width, boolean depth) {
        int steps = 48;
        class_243 previous = null;
        class_243 first = null;
        for (int i = 0; i <= steps; i++) {
            float angle = i / (float) steps * TAU;
            class_243 point = c.at(Math.cos(angle) * radius, y + Math.sin(angle) * radius, 0.0);
            if (previous != null) {
                line(c, previous, point, color, width, depth);
            } else {
                first = point;
            }
            previous = point;
        }
        if (first != null && previous != null) {
            line(c, previous, first, color, width, depth);
        }
    }

    static void pillar(Ctx c, int color, float radius, float height, float alpha, int count) {
        float strength = alpha * env(c);
        if (strength <= 0.004f || radius <= 0.0f || height <= 0.0f) {
            return;
        }
        // A volumetric light column built from crossed gradient quads (bright at
        // the ground, fading to nothing at the top). Stacked discs produced
        // visible banding stripes, the gradient quads read as one smooth beam.
        float w = radius * c.scale;
        int planes = Math.max(8, count * 2);
        for (int i = 0; i < planes; i++) {
            float angle = i / (float) planes * TAU;
            float cs = (float) Math.cos(angle);
            float sn = (float) Math.sin(angle);
            float hw = w * 0.55f;
            double rx = cs * w;
            double rz = sn * w;
            class_243 baseA = c.at(rx - sn * hw, 0.0, rz + cs * hw);
            class_243 baseB = c.at(rx + sn * hw, 0.0, rz - cs * hw);
            class_243 topA = c.at(rx - sn * hw, height, rz + cs * hw);
            class_243 topB = c.at(rx + sn * hw, height, rz - cs * hw);
            float shimmer = 0.26f + 0.06f * (float) Math.sin(c.age * 3.2f + i * 1.31f);
            int bottom = col(color, strength * shimmer);
            int top = col(color, 0.0f);
            Render3D.drawGlowGradientQuad(c.stack, baseA, topA, topB, baseB, bottom, top, top, bottom, false);
        }
        sprite(c, 0.0, 0.02, 0.0, radius * 3.0f, GLOW, col(color, strength * 0.3f), false, true);
        sprite(c, 0.0, height * 0.92, 0.0, radius * 1.7f, GLOW, col(color, strength * 0.2f), false, true);
    }

    static void burst(Ctx c, class_2960 texture, int color, int count, float radius, float height, float size, float gravity, boolean additive) {
        float e = easeOut(c.t);
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float angle = rnd(c, i, 1) * TAU;
            float distance = radius * (0.12f + 0.88f * e) * (0.55f + 0.45f * rnd(c, i, 2));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.35 + height * e * (0.25 + 0.75 * rnd(c, i, 3)) - gravity * e * e;
            if (y < -0.15) {
                y = -0.15;
            }
            float alpha = fade * (0.45f + 0.55f * rnd(c, i, 4));
            sprite(c, x, y, z, size * (0.7f + 0.6f * rnd(c, i, 5)), texture, col(color, alpha), true, additive);
        }
    }

    static void rise(Ctx c, class_2960 texture, int color, int count, float radius, float height, float size, boolean additive) {
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float phase = fract(c.t * 1.25f + rnd(c, i, 1));
            float angle = rnd(c, i, 2) * TAU + c.age * (0.5f + 0.9f * rnd(c, i, 3));
            float distance = radius * (0.2f + 0.8f * rnd(c, i, 4));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.05 + phase * height;
            float alpha = (1.0f - phase) * fade * (0.4f + 0.6f * rnd(c, i, 5));
            sprite(c, x, y, z, size * (0.7f + 0.6f * rnd(c, i, 6)), texture, col(color, alpha), true, additive);
        }
    }

    static void fall(Ctx c, class_2960 texture, int color, int count, float radius, float height, float size, boolean additive) {
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float phase = fract(c.t * (0.75f + 0.5f * rnd(c, i, 1)) + rnd(c, i, 2));
            float angle = rnd(c, i, 3) * TAU + c.age * 0.6f;
            float distance = radius * (0.15f + 0.85f * Math.min(1.0f, phase * 1.4f));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.05 + height * (1.0f - phase);
            float alpha = fade * (1.0f - phase * 0.65f) * (0.4f + 0.6f * rnd(c, i, 4));
            sprite(c, x, y, z, size * (0.7f + 0.6f * rnd(c, i, 5)), texture, col(color, alpha), true, additive);
        }
    }

    static void swirl(Ctx c, class_2960 texture, int color, int count, float radius, float height, float size, float speed, boolean inward, boolean additive) {
        float e = easeOut(c.t);
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float direction = rnd(c, i, 2) > 0.5f ? 1.0f : -1.0f;
            float angle = rnd(c, i, 1) * TAU + c.age * speed * direction;
            float distance = radius * (inward ? 1.0f - 0.75f * e : 0.25f + 0.75f * e) * (0.5f + 0.5f * rnd(c, i, 3));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.1 + height * (inward ? 1.0f - e : e) * (0.25 + 0.75 * rnd(c, i, 4));
            float alpha = fade * (0.35f + 0.65f * rnd(c, i, 5));
            sprite(c, x, y, z, size * (0.7f + 0.6f * rnd(c, i, 6)), texture, col(color, alpha), true, additive);
        }
    }

    static void line(Ctx c, class_243 first, class_243 second, int color, float width, boolean depth) {
        float worldWidth = Math.max(0.01f, width * c.scale);
        Render3D.drawLine(c.stack, first, second, color, worldWidth, depth);
        // Lightweight bloom: wide accent strokes get a soft additive halo, thin
        // filaments stay crisp so the scene does not turn into a smear.
        int alpha = color >>> 24;
        if (alpha >= 20 && width >= 0.05f) {
            Render3D.drawNeonLine(c.stack, first, second, color & 0xFFFFFF, worldWidth * 4.4f, alpha / 255.0f * 0.4f);
        }
    }

    static void bolt(Ctx c, double x0, double y0, double z0, double x1, double y1, double z1, int color, float width, int salt) {
        int steps = 8;
        class_243 previous = c.at(x0, y0, z0);
        for (int i = 1; i <= steps; i++) {
            float p = i / (float) steps;
            float jitter = 0.42f * (1.0f - Math.abs(p * 2.0f - 1.0f));
            double x = x0 + (x1 - x0) * p + (rnd(c, salt * 31 + i, 1) - 0.5) * jitter;
            double y = y0 + (y1 - y0) * p + (rnd(c, salt * 31 + i, 2) - 0.5) * jitter;
            double z = z0 + (z1 - z0) * p + (rnd(c, salt * 31 + i, 3) - 0.5) * jitter;
            class_243 next = c.at(x, y, z);
            line(c, previous, next, col(color, 0.9f - 0.35f * p), width * (1.0f - 0.55f * p), false);
            previous = next;
        }
    }

    static void cube(Ctx c, double x, double y, double z, float size, int color, boolean depth) {
        box(c, x, y, z, size, size, size, color, depth);
    }

    /** Twelve edges of a box, used for holograms and wireframe props. */
    static void outlineBox(Ctx c, double x, double y, double z, float hx, float hy, float hz, int color, float width, boolean depth) {
        float sx = hx * c.scale;
        float sy = hy * c.scale;
        float sz = hz * c.scale;
        class_243 a = c.at(x - sx, y - sy, z - sz);
        class_243 b = c.at(x + sx, y - sy, z - sz);
        class_243 d = c.at(x + sx, y - sy, z + sz);
        class_243 e = c.at(x - sx, y - sy, z + sz);
        class_243 f = c.at(x - sx, y + sy, z - sz);
        class_243 g = c.at(x + sx, y + sy, z - sz);
        class_243 h = c.at(x + sx, y + sy, z + sz);
        class_243 i = c.at(x - sx, y + sy, z + sz);
        line(c, a, b, color, width, depth);
        line(c, b, d, color, width, depth);
        line(c, d, e, color, width, depth);
        line(c, e, a, color, width, depth);
        line(c, f, g, color, width, depth);
        line(c, g, h, color, width, depth);
        line(c, h, i, color, width, depth);
        line(c, i, f, color, width, depth);
        line(c, a, f, color, width, depth);
        line(c, b, g, color, width, depth);
        line(c, d, h, color, width, depth);
        line(c, e, i, color, width, depth);
    }

    /** Rectangular box with independent half extents, used for props and figures. */
    static void box(Ctx c, double x, double y, double z, float hx, float hy, float hz, int color, boolean depth) {
        if (hx <= 0.002f || hy <= 0.002f || hz <= 0.002f || (color >>> 24) == 0) {
            return;
        }
        float sx = hx * c.scale;
        float sy = hy * c.scale;
        float sz = hz * c.scale;
        class_243 a = c.at(x - sx, y - sy, z - sz);
        class_243 b = c.at(x + sx, y - sy, z - sz);
        class_243 d = c.at(x + sx, y - sy, z + sz);
        class_243 e = c.at(x - sx, y - sy, z + sz);
        class_243 f = c.at(x - sx, y + sy, z - sz);
        class_243 g = c.at(x + sx, y + sy, z - sz);
        class_243 h = c.at(x + sx, y + sy, z + sz);
        class_243 i = c.at(x - sx, y + sy, z + sz);
        int top = shade(color, 1.3f);
        int sideA = shade(color, 0.98f);
        int sideB = shade(color, 0.72f);
        int bottom = shade(color, 0.55f);
        Render3D.drawQuad(c.stack, a, b, d, e, bottom, depth);
        Render3D.drawQuad(c.stack, f, i, h, g, top, depth);
        Render3D.drawQuad(c.stack, a, e, i, f, sideA, depth);
        Render3D.drawQuad(c.stack, b, g, h, d, sideB, depth);
        Render3D.drawQuad(c.stack, a, f, g, b, sideA, depth);
        Render3D.drawQuad(c.stack, e, d, h, i, sideB, depth);
    }

    private static final float[] HUMAN_X = {0.0f, 0.0f, 0.0f, -0.29f, 0.29f, -0.13f, 0.13f};
    private static final float[] HUMAN_Y = {1.82f, 1.27f, 1.27f, 1.25f, 1.25f, 0.58f, 0.58f};
    private static final float[] HUMAN_Z = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] HUMAN_HX = {0.17f, 0.19f, 0.11f, 0.08f, 0.08f, 0.09f, 0.09f};
    private static final float[] HUMAN_HY = {0.17f, 0.26f, 0.06f, 0.23f, 0.23f, 0.30f, 0.30f};
    private static final float[] HUMAN_HZ = {0.17f, 0.11f, 0.11f, 0.09f, 0.09f, 0.10f, 0.10f};

    /**
     * A blocky humanoid built from cubes. {@code explode} scatters the limbs
     * outward and fades them, which most of the death themed effects use.
     */
    static void humanoid(Ctx c, int color, float alpha, float explode, boolean depth) {
        humanoidAt(c, 0.0, 0.0, 0.0, color, alpha, explode, depth);
    }

    static void humanoidAt(Ctx c, double offsetX, double offsetY, double offsetZ, int color, float alpha, float explode, boolean depth) {
        float e = clamp(explode);
        if (alpha <= 0.01f) {
            return;
        }
        for (int part = 0; part < HUMAN_X.length; part++) {
            float ox = HUMAN_X[part] * (1.0f + 2.6f * e);
            float oy = HUMAN_Y[part] + e * (0.35f + 1.1f * rnd(c, part + 40, 1));
            float oz = HUMAN_Z[part] + e * (rnd(c, part + 50, 2) - 0.5f) * 1.8f;
            float shrink = 1.0f - e * 0.55f;
            int partColor = part == 0 && e < 0.4f ? shade(color, 1.18f) : color;
            box(c, offsetX + ox, offsetY + oy, offsetZ + oz, HUMAN_HX[part] * shrink, HUMAN_HY[part] * shrink,
                    HUMAN_HZ[part] * shrink, col(partColor, alpha * (1.0f - e * 0.3f)), depth);
        }
    }

    /** Wireframe humanoid used by the hologram effect. */
    static void humanoidFrame(Ctx c, int color, float alpha, float width, boolean depth) {
        for (int part = 0; part < HUMAN_X.length; part++) {
            outlineBox(c, HUMAN_X[part], HUMAN_Y[part], HUMAN_Z[part], HUMAN_HX[part], HUMAN_HY[part],
                    HUMAN_HZ[part], col(color, alpha), width, depth);
        }
    }

    /**
     * Small 3x5 voxel font. Used by the arcade and knockout effects so they
     * read as actual signs instead of another particle ring.
     */
    static void text(Ctx c, String value, double x, double y, double z, float pixel, int color, boolean depth) {
        String text = value.toUpperCase();
        int width = text.length() * 4 - 1;
        for (int index = 0; index < text.length(); index++) {
            String[] glyph = glyph(text.charAt(index));
            if (glyph == null) {
                continue;
            }
            for (int row = 0; row < 5; row++) {
                for (int column = 0; column < 3; column++) {
                    if (glyph[row].charAt(column) != '1') {
                        continue;
                    }
                    double px = x + ((index * 4 + column) - width * 0.5 + 0.5) * pixel;
                    double py = y + (2 - row) * pixel;
                    planeSquare(c, px, py, z, pixel * 0.52f, 0.0f, color, depth);
                }
            }
        }
    }

    private static String[] glyph(char character) {
        return switch (character) {
            case 'A' -> new String[]{"010", "101", "111", "101", "101"};
            case 'B' -> new String[]{"110", "101", "110", "101", "110"};
            case 'C' -> new String[]{"011", "100", "100", "100", "011"};
            case 'D' -> new String[]{"110", "101", "101", "101", "110"};
            case 'E' -> new String[]{"111", "100", "110", "100", "111"};
            case 'F' -> new String[]{"111", "100", "110", "100", "100"};
            case 'G' -> new String[]{"011", "100", "101", "101", "011"};
            case 'H' -> new String[]{"101", "101", "111", "101", "101"};
            case 'I' -> new String[]{"111", "010", "010", "010", "111"};
            case 'J' -> new String[]{"001", "001", "001", "101", "010"};
            case 'K' -> new String[]{"101", "101", "110", "101", "101"};
            case 'L' -> new String[]{"100", "100", "100", "100", "111"};
            case 'M' -> new String[]{"101", "111", "111", "101", "101"};
            case 'N' -> new String[]{"101", "111", "111", "111", "101"};
            case 'O' -> new String[]{"111", "101", "101", "101", "111"};
            case 'P' -> new String[]{"110", "101", "110", "100", "100"};
            case 'Q' -> new String[]{"010", "101", "101", "110", "011"};
            case 'R' -> new String[]{"110", "101", "110", "101", "101"};
            case 'S' -> new String[]{"011", "100", "010", "001", "110"};
            case 'T' -> new String[]{"111", "010", "010", "010", "010"};
            case 'U' -> new String[]{"101", "101", "101", "101", "111"};
            case 'V' -> new String[]{"101", "101", "101", "101", "010"};
            case 'W' -> new String[]{"101", "101", "111", "111", "101"};
            case 'X' -> new String[]{"101", "101", "010", "101", "101"};
            case 'Y' -> new String[]{"101", "101", "010", "010", "010"};
            case 'Z' -> new String[]{"111", "001", "010", "100", "111"};
            default -> null;
        };
    }

    static void diamond(Ctx c, double x, double y, double z, float size, float spin, int color, boolean depth) {
        if (size <= 0.005f || (color >>> 24) == 0) {
            return;
        }
        float s = size * c.scale;
        float cs = (float) Math.cos(spin);
        float sn = (float) Math.sin(spin);
        class_243 top = c.at(x, y + s * 1.6, z);
        class_243 bottom = c.at(x, y - s * 1.6, z);
        class_243 r1 = c.at(x + cs * s, y, z + sn * s);
        class_243 l1 = c.at(x - cs * s, y, z - sn * s);
        class_243 r2 = c.at(x - sn * s, y, z + cs * s);
        class_243 l2 = c.at(x + sn * s, y, z - cs * s);
        Render3D.drawQuad(c.stack, top, r1, bottom, l1, color, depth);
        Render3D.drawQuad(c.stack, top, r2, bottom, l2, shade(color, 1.3f), depth);
    }

    static void planeSquare(Ctx c, double x, double y, double z, float size, float angle, int color, boolean depth) {
        float s = size * c.scale;
        float cs = (float) Math.cos(angle);
        float sn = (float) Math.sin(angle);
        class_243 p0 = c.at(x - cs * s, y - s, z - sn * s);
        class_243 p1 = c.at(x + cs * s, y - s, z + sn * s);
        class_243 p2 = c.at(x + cs * s, y + s, z + sn * s);
        class_243 p3 = c.at(x - cs * s, y + s, z - sn * s);
        Render3D.drawQuad(c.stack, p0, p1, p2, p3, color, depth);
    }

    static void paperTri(Ctx c, double x, double y, double z, float size, float angle, int color, boolean depth) {
        float s = size * c.scale;
        float cs = (float) Math.cos(angle);
        float sn = (float) Math.sin(angle);
        class_243 p0 = c.at(x - cs * s, y - s, z - sn * s);
        class_243 p1 = c.at(x + cs * s, y - s, z + sn * s);
        class_243 p2 = c.at(x + cs * s * 0.1, y + s, z + sn * s * 0.1);
        Render3D.renderTriangle(c.stack, p0, p1, p2, argb(color), depth);
    }

    static void gear(Ctx c, double y, float radius, float angle, int color, float alpha, int teeth) {
        float fade = env(c) * alpha;
        ringAt(c, y, radius, col(color, fade), true);
        ringAt(c, y, radius * 0.7f, col(color, fade * 0.55f), true);
        for (int i = 0; i < teeth; i++) {
            float a = angle + i * TAU / teeth;
            cube(c, Math.cos(a) * radius, y, Math.sin(a) * radius, radius * 0.15f, col(color, fade), true);
        }
    }

    static void tendril(Ctx c, int salt, int color, float height, float reach, float width, boolean leaves) {
        float fade = env(c);
        class_243 previous = c.at(0.0, 0.05, 0.0);
        int steps = 12;
        for (int i = 1; i <= steps; i++) {
            float p = i / (float) steps;
            float angle = salt * 1.618f + p * 2.4f + c.age * 0.7f;
            float bulge = (float) Math.sin(p * Math.PI * 0.92);
            float distance = reach * (0.18f + 0.82f * bulge) * (0.8f + 0.2f * (float) Math.sin(c.age * 1.6f + salt));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.05 + p * height;
            class_243 next = c.at(x, y, z);
            line(c, previous, next, col(color, fade * (0.8f - 0.35f * p)), width * (1.0f - 0.35f * p), false);
            if (leaves && i % 3 == 0) {
                sprite(c, x, y, z, 0.22f + 0.12f * p, PETAL, col(color, fade * 0.55f), true, false);
            }
            previous = next;
        }
    }

    static void vine(Ctx c, int salt, int color, float height, float reach, float width, boolean leaves) {
        float fade = env(c);
        class_243 previous = c.at(0.0, 0.04, 0.0);
        int steps = 13;
        for (int i = 1; i <= steps; i++) {
            float p = i / (float) steps;
            float angle = salt * 1.37f + p * 4.2f + c.age * 0.55f;
            float distance = reach * (0.05f + 0.95f * p) * (0.75f + 0.25f * (float) Math.sin(c.age * 1.4f + salt));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.05 + p * height;
            class_243 next = c.at(x, y, z);
            line(c, previous, next, col(color, fade * (0.85f - 0.3f * p)), width * (1.0f - 0.3f * p), false);
            if (leaves && i % 2 == 0) {
                sprite(c, x, y, z, 0.24f + 0.12f * p, PETAL, col(color, fade * 0.6f), true, false);
            }
            previous = next;
        }
    }

    static void pixelField(Ctx c, int color, int count, float spread, float height, float size) {
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float x = (rnd(c, i, 1) - 0.5f) * spread;
            float z = (rnd(c, i, 2) - 0.5f) * spread;
            float phase = fract(c.t * (0.7f + 0.8f * rnd(c, i, 3)) + rnd(c, i, 4));
            double y = 0.05 + phase * height;
            float alpha = fade * (0.35f + 0.65f * rnd(c, i, 5)) * (1.0f - phase * 0.6f);
            cube(c, x, y, z, size * (0.7f + 0.6f * rnd(c, i, 6)), col(color, alpha), true);
        }
    }

    static void colorBurst(Ctx c, int count, float radius, float height, float size, float gravity) {
        float e = easeOut(c.t);
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float angle = rnd(c, i, 1) * TAU;
            float distance = radius * (0.12f + 0.88f * e) * (0.4f + 0.6f * rnd(c, i, 2));
            double x = Math.cos(angle) * distance;
            double z = Math.sin(angle) * distance;
            double y = 0.35 + height * e * (0.2 + 0.8 * rnd(c, i, 3)) - gravity * e * e;
            float alpha = fade * (0.5f + 0.5f * rnd(c, i, 4));
            int hue = rainbow(i / (float) count);
            sprite(c, x, y, z, size * (0.7f + 0.6f * rnd(c, i, 5)), STAR, col(hue, alpha), true, false);
        }
    }

    /** Overshoot easing: flies past the target, then settles back. */
    static float overshoot(float value) {
        float x = clamp(value) - 1.0f;
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * x * x * x + c1 * x * x;
    }

    /** Springy elastic easing for the last stretch of an animation. */
    static float elastic(float value) {
        float x = clamp(value);
        if (x >= 1.0f) {
            return 1.0f;
        }
        if (x <= 0.0f) {
            return 0.0f;
        }
        return (float) (Math.pow(2.0, 9.0 * (x - 1.0)) * Math.sin((x - 0.075) * (TAU / 0.34)));
    }

    /** 0..1 sine pump, used for blinking signs and breathing props. */
    static float pump(Ctx c, float speed) {
        return 0.5f + 0.5f * (float) Math.sin(c.age * speed);
    }

    /**
     * Single-beat bloom flash used as the finishing touch of an effect: a
     * bright core plus a soft halo that swell and die over {@code progress}
     * (0..1, invisible outside the beat).
     */
    static void flashPulse(Ctx c, class_2960 texture, double x, double y, double z, float size, int color, float progress) {
        float p = clamp(progress);
        if (p <= 0.0f || p >= 1.0f) {
            return;
        }
        float power = (float) Math.sin(p * Math.PI);
        sprite(c, x, y, z, size * (0.8f + 0.5f * p), texture, col(color, power * 0.6f), true, true);
        sprite(c, x, y, z, size * (1.7f + 0.9f * p), GLOW, col(color, power * 0.26f), true, true);
    }

    /**
     * Rising double helix of soft sprites winding around the subject. Used as
     * a flourish so aura effects feel alive instead of static.
     */
    static void helix(Ctx c, class_2960 texture, int color, int count, float radius, float height, float size, float speed, boolean additive) {
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float phase = fract(c.t * speed + i / (float) count + (i % 2) * 0.5f);
            float angle = i / (float) count * TAU * 2.0f + phase * 2.2f + (i % 2) * (float) Math.PI;
            float bodyPhase = 0.4f + 0.6f * (float) Math.sin(phase * Math.PI);
            float distance = radius * (0.35f + 0.65f * bodyPhase);
            double y = 0.15 + phase * height;
            float alpha = fade * (float) Math.sin(phase * Math.PI) * (0.6f + 0.4f * rnd(c, i, 1));
            sprite(c, Math.cos(angle) * distance, y, Math.sin(angle) * distance,
                    size * (0.7f + 0.5f * bodyPhase), texture, col(color, alpha), true, additive);
        }
    }

    /**
     * Radial crack polylines branching out of a center point. Replaces flat
     * cross lines with jittered fracture webs (glass, stone, ice).
     */
    static void cracks(Ctx c, double centerX, double centerY, double centerZ,
                       int count, float reach, int color, float alpha, float width, int salt) {
        float fade = env(c);
        for (int i = 0; i < count; i++) {
            float angle = rnd(c, i, salt) * TAU;
            float lean = (rnd(c, i, salt + 1) - 0.5f) * 1.2f;
            float distance = reach * (0.35f + 0.65f * rnd(c, i, salt + 2));
            class_243 start = c.at(centerX, centerY, centerZ);
            class_243 previous = start;
            int steps = 4;
            for (int j = 1; j <= steps; j++) {
                float p = j / (float) steps;
                float bend = lean * p * p;
                float radius = distance * p * (0.86f + 0.28f * rnd(c, i, salt + 3 + j));
                double x = centerX + Math.cos(angle + bend) * radius;
                double z = centerZ + Math.sin(angle + bend) * radius;
                double y = centerY + (rnd(c, i, salt + 7 + j) - 0.5f) * reach * 0.22f * p;
                class_243 next = c.at(x, y, z);
                line(c, previous, next, col(color, alpha * fade * (1.0f - 0.4f * p)), width * (1.0f - 0.35f * p), false);
                previous = next;
            }
        }
    }
}
