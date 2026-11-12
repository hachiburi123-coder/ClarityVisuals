package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_243;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.AtmosphereSurface;
import ru.method.clarityvisuals.utils.render.GroundHeightCache;
import ru.method.clarityvisuals.utils.render.Render3D;
import ru.method.clarityvisuals.utils.render.SakuraSkyRenderer;

/**
 * Sakura petals with true 3D flips, wind gusts and a soft glow. Petals dissolve
 * before touching the ground, so the effect stays airy and does not build up a
 * static carpet under the player.
 */
public class SakuraModule
extends Module {
    private static final class_2960 PETAL = ClarityVisuals.id("client/particles/sakura_petal.png");
    private static final class_2960 GLOW = ClarityVisuals.id("client/xuynya/ghost_bloom.png");
    private static final int MAX_PETALS = 1800;
    /** Переиспользуемые скрэтч-объекты ориентации лепестка: без аллокаций на лепесток за кадр. */
    private static final Quaternionf SCRATCH_ROT = new Quaternionf();
    private static final Vector3f SCRATCH_RIGHT = new Vector3f();
    private static final Vector3f SCRATCH_UP = new Vector3f();

    private final EnumSetting<SakuraPreset> preset = new EnumSetting<SakuraPreset>("modules.settings.sakura.preset", SakuraPreset.DREAM);
    private final SliderSetting density = new SliderSetting("modules.settings.sakura.density", 1.0f, 0.15f, 3.0f, 0.05f);
    private final SliderSetting radius = new SliderSetting("modules.settings.sakura.radius", 27.0f, 8.0f, 64.0f, 1.0f);
    private final SliderSetting altitude = new SliderSetting("modules.settings.sakura.altitude", 22.0f, 6.0f, 40.0f, 1.0f);
    private final SliderSetting size = new SliderSetting("modules.settings.sakura.size", 1.0f, 0.3f, 2.5f, 0.05f);
    private final SliderSetting fallSpeed = new SliderSetting("modules.settings.sakura.fall_speed", 1.0f, 0.25f, 2.2f, 0.05f);
    private final SliderSetting wind = new SliderSetting("modules.settings.sakura.wind", 1.0f, 0.0f, 3.0f, 0.05f);
    private final SliderSetting swirl = new SliderSetting("modules.settings.sakura.swirl", 1.0f, 0.0f, 2.5f, 0.05f);
    private final SliderSetting opacity = new SliderSetting("modules.settings.sakura.opacity", 1.0f, 0.2f, 2.0f, 0.05f);
    private final SliderSetting glow = new SliderSetting("modules.settings.sakura.glow", 0.42f, 0.0f, 1.0f, 0.02f);
    private final EnumSetting<SakuraColor> color = new EnumSetting<SakuraColor>("modules.settings.sakura.color", SakuraColor.PINK);
    private final ColorSetting customColor = new ColorSetting("modules.settings.sakura.custom_color", new Color(255, 187, 214),
        () -> this.color.jT() == SakuraColor.CUSTOM);
    private final BooleanSetting skyCheck = new BooleanSetting("modules.settings.sakura.sky_check", true);
    private final BooleanSetting pinkLeaves = new BooleanSetting("modules.settings.sakura.pink_leaves", true);
    private final BooleanSetting sky = new BooleanSetting("modules.settings.sakura.sky", true);
    private final SliderSetting skyIntensity = new SliderSetting("modules.settings.sakura.sky_intensity", 1.0f, 0.2f, 1.6f, 0.05f,
        this.sky::jT);
    private final BooleanSetting sun = new BooleanSetting("modules.settings.sakura.sun", true, this.sky::jT);
    private final BooleanSetting clouds = new BooleanSetting("modules.settings.sakura.clouds", true, this.sky::jT);
    private final BooleanSetting warmFog = new BooleanSetting("modules.settings.sakura.warm_fog", true);
    private final SliderSetting fogDensity = new SliderSetting("modules.settings.sakura.fog_density", 0.52f, 0.05f, 1.5f, 0.05f,
        this.warmFog::jT);
    private static SakuraModule instance;

    private final List<Petal> petals = new ArrayList<>();
    private final GroundHeightCache groundCache = new GroundHeightCache();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float clock;
    private int cacheTicks;

    public SakuraModule() {
        super("Sakura", ModuleCategory.ql);
        instance = this;
        this.getSettings().add(this.preset);
        this.getSettings().add(this.density);
        this.getSettings().add(this.radius);
        this.getSettings().add(this.altitude);
        this.getSettings().add(this.size);
        this.getSettings().add(this.fallSpeed);
        this.getSettings().add(this.wind);
        this.getSettings().add(this.swirl);
        this.getSettings().add(this.opacity);
        this.getSettings().add(this.glow);
        this.getSettings().add(this.color);
        this.getSettings().add(this.customColor);
        this.getSettings().add(this.skyCheck);
        this.getSettings().add(this.pinkLeaves);
        this.getSettings().add(this.sky);
        this.getSettings().add(this.skyIntensity);
        this.getSettings().add(this.sun);
        this.getSettings().add(this.clouds);
        this.getSettings().add(this.warmFog);
        this.getSettings().add(this.fogDensity);
    }

    public static boolean shouldTintLeaves() {
        SakuraModule module = instance;
        return module != null && module.isToggled() && ((Boolean) module.pinkLeaves.jT()).booleanValue();
    }

    public static int tintLeafColor(int original) {
        int red = original >> 16 & 0xFF;
        int green = original >> 8 & 0xFF;
        int blue = original & 0xFF;
        float luma = (red * 0.299f + green * 0.587f + blue * 0.114f) / 255.0f;
        // Vanilla leaf tints are quite dark, so map them onto a bright rose
        // palette instead of multiplying the original green directly.
        float v = Math.min(1.0f, Math.max(0.0f, (luma - 0.16f) / 0.56f));
        int outRed = 255;
        int outGreen = (int) (128.0f + 82.0f * v);
        int outBlue = (int) (178.0f + 54.0f * v);
        return 0xFF000000 | outRed << 16 | outGreen << 8 | outBlue;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastFrameNanos = 0L;
        this.clearAll();
        AtmosphereSurface.getInstance().reset();
        this.reloadLeafTint();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.clearAll();
        AtmosphereSurface.getInstance().reset();
        this.reloadLeafTint();
    }

    private void reloadLeafTint() {
        if (!((Boolean) this.pinkLeaves.jT()).booleanValue()) {
            return;
        }
        try {
            if (SakuraModule.aiG.field_1687 != null && SakuraModule.aiG.field_1769 != null) {
                // Leaf colours are baked into chunk vertex data, so toggling the
                // module has to invalidate the chunk renderer as well.
                SakuraModule.aiG.field_1769.method_3279();
            }
        } catch (Throwable ignored) {
        }
    }

    private void clearAll() {
        this.petals.clear();
        this.groundCache.clear();
    }

    private float presetDensity() {
        return switch (this.preset.jT()) {
            case HANAMI -> 1.45f;
            case PETAL_STORM -> 2.25f;
            default -> 1.0f;
        };
    }

    private float presetWind() {
        return switch (this.preset.jT()) {
            case HANAMI -> 0.75f;
            case PETAL_STORM -> 2.0f;
            default -> 1.0f;
        };
    }

    private int targetPetals() {
        float r = ((Float) this.radius.jT()).floatValue();
        float count = r * r * 0.62f * ((Float) this.density.jT()).floatValue() * this.presetDensity();
        return Math.min(MAX_PETALS, Math.max(220, (int) count));
    }

    private float random(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private int surfaceY(int x, int z) {
        long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
        int cached = this.groundCache.get(key);
        if (cached != GroundHeightCache.MISS) {
            return cached;
        }
        int top;
        try {
            top = SakuraModule.aiG.field_1687.method_8624(class_2902.class_2903.field_13197, x, z);
        } catch (Throwable ignored) {
            top = SakuraModule.aiG.field_1687.method_31607();
        }
        this.groundCache.put(key, top);
        return top;
    }

    private boolean skyVisible(double x, double y, double z) {
        if (!((Boolean) this.skyCheck.jT()).booleanValue() || SakuraModule.aiG.field_1687 == null) {
            return true;
        }
        return this.surfaceY((int) Math.floor(x), (int) Math.floor(z)) <= (int) Math.floor(y) + 1;
    }

    private float[] resolveTint() {
        int value = switch (this.color.jT()) {
            case WHITE -> 0xFFFFFBFF;
            case AUTUMN -> 0xFFFFA45F;
            case CUSTOM -> ((Color) this.customColor.jT()).getRGB();
            default -> 0xFFFFD4E6;
        };
        return new float[]{
            ((value >> 16) & 0xFF) / 255.0f,
            ((value >> 8) & 0xFF) / 255.0f,
            (value & 0xFF) / 255.0f
        };
    }

    private void spawnPetal(class_243 center) {
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * r;

        Petal petal = new Petal();
        petal.x = center.field_1352 + Math.cos(angle) * dist;
        petal.z = center.field_1350 + Math.sin(angle) * dist;
        petal.y = center.field_1351 + this.random(-4.0f, alt);
        if (!this.skyVisible(petal.x, petal.y, petal.z)) {
            return;
        }
        petal.mass = this.random(0.65f, 1.35f);
        petal.size = this.random(0.65f, 1.35f);
        petal.vy = -this.random(0.55f, 1.35f) * petal.mass;
        petal.phase = this.random(0.0f, 62.8f);
        petal.yaw = this.random(0.0f, 6.283f);
        petal.pitch = this.random(-1.3f, 1.3f);
        petal.roll = this.random(0.0f, 6.283f);
        petal.spinYaw = this.random(-1.25f, 1.25f);
        petal.spinPitch = this.random(-1.8f, 1.8f);
        petal.spinRoll = this.random(-2.2f, 2.2f);
        petal.sway = this.random(0.5f, 1.5f);
        petal.brightness = this.random(0.72f, 1.0f);
        petal.ground = this.surfaceY((int) Math.floor(petal.x), (int) Math.floor(petal.z));
        this.petals.add(petal);
    }

    private void updatePetals(class_243 center, float dt) {
        int target = this.targetPetals();
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        this.petals.removeIf(p -> {
            double dx = p.x - center.field_1352;
            double dz = p.z - center.field_1350;
            float rr = r + 6.0f;
            return p.dead || dx * dx + dz * dz > rr * rr || p.y < center.field_1351 - alt - 14.0;
        });

        boolean fill = this.petals.size() < target / 2;
        int budget = Math.min(Math.max(0, target - this.petals.size()), fill ? 700 : 220);
        for (int i = 0; i < budget; i++) {
            this.spawnPetal(center);
        }

        float speed = ((Float) this.fallSpeed.jT()).floatValue();
        float windMul = ((Float) this.wind.jT()).floatValue() * this.presetWind();
        float swirlMul = ((Float) this.swirl.jT()).floatValue();
        for (int i = 0, count = this.petals.size(); i < count; i++) {
            Petal p = this.petals.get(i);
            double gust = 0.55 + 0.45 * Math.sin(this.clock * 0.45 + p.phase * 0.13)
                + 0.15 * Math.sin(this.clock * 1.8 + p.phase);
            double windX = windMul * 2.7 * gust;
            double windZ = windMul * 1.45 * Math.sin(this.clock * 0.27 + p.phase * 0.07) * gust;
            double swayX = Math.sin(this.clock * 1.3 + p.phase) * p.sway * 0.55;
            double swayZ = Math.cos(this.clock * 1.05 + p.phase * 1.4) * p.sway * 0.45;

            double dx = p.x - center.field_1352;
            double dz = p.z - center.field_1350;
            double distance = Math.sqrt(dx * dx + dz * dz) + 0.001;
            double swirlX = -dz / distance * swirlMul * 0.35 * Math.sin(this.clock * 0.4 + p.phase);
            double swirlZ = dx / distance * swirlMul * 0.35 * Math.sin(this.clock * 0.4 + p.phase);

            p.vx += ((windX + swayX + swirlX) - p.vx) * Math.min(1.0, dt * 1.7);
            p.vz += ((windZ + swayZ + swirlZ) - p.vz) * Math.min(1.0, dt * 1.7);
            p.vy -= 0.75 * p.mass * dt;
            if (p.vy < -1.9 * p.mass) {
                p.vy = -1.9 * p.mass;
            }
            p.yaw += p.spinYaw * dt;
            p.pitch += p.spinPitch * dt;
            p.roll += p.spinRoll * dt;

            double nx = p.x + p.vx * dt * speed;
            double ny = p.y + p.vy * dt * speed;
            double nz = p.z + p.vz * dt * speed;
            if (ny <= p.ground + 0.08) {
                p.dead = true;
                continue;
            }
            p.x = nx;
            p.y = ny;
            p.z = nz;
        }
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (SakuraModule.fullNullCheck()) {
            this.clearAll();
            return;
        }
        class_4184 camera = SakuraModule.aiG.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastFrameNanos == 0L) {
            this.lastFrameNanos = now;
        }
        float dt = (float) ((now - this.lastFrameNanos) / 1_000_000_000.0);
        this.lastFrameNanos = now;
        dt = Math.min(0.05f, Math.max(0.0f, dt));
        this.clock += dt;

        class_243 cam = camera.method_19326();
        this.updatePetals(cam, dt);

        if (((Boolean) this.sky.jT()).booleanValue()) {
            SakuraSkyRenderer.render(this.clock,
                ((Float) this.skyIntensity.jT()).floatValue(),
                ((Boolean) this.sun.jT()).booleanValue(),
                ((Boolean) this.clouds.jT()).booleanValue());
        }
        if (((Boolean) this.warmFog.jT()).booleanValue()) {
            this.renderWarmFog(camera, cam);
        }

        class_4587 ms = stage.l();
        float[] tint = this.resolveTint();
        if (!this.petals.isEmpty()) {
            this.drawPetals(ms, cam, tint);
        }
    }

    private void renderWarmFog(class_4184 camera, class_243 cam) {
        try {
            org.joml.Matrix4f viewRot = new org.joml.Matrix4f().rotation(camera.method_23767());
            org.joml.Matrix4f viewMatrix = new org.joml.Matrix4f(viewRot).invert();
            org.joml.Matrix4f projectionMatrix = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix();
            AtmosphereSurface.Parameters parameters = new AtmosphereSurface.Parameters();
            int baseY = this.surfaceY((int) Math.floor(cam.field_1352), (int) Math.floor(cam.field_1350)) + 1;
            parameters.mode = AtmosphereSurface.MODE_FOG;
            parameters.density = ((Float) this.fogDensity.jT()).floatValue() * 0.94f;
            parameters.height = 7.0f;
            parameters.maxDistance = 62.0f;
            parameters.speed = 0.35f;
            parameters.baseY = Math.min(baseY, (int) Math.floor(cam.field_1351));
            parameters.time = this.clock;
            parameters.red = 0.96f;
            parameters.green = 0.70f;
            parameters.blue = 0.70f;
            AtmosphereSurface.getInstance().apply(camera, viewMatrix, projectionMatrix, parameters);
        } catch (Throwable ignored) {
        }
    }

    private void drawPetals(class_4587 ms, class_243 cam, float[] tint) {
        float r = ((Float) this.radius.jT()).floatValue();
        float fadeStart = r * 0.60f;
        float sizeMul = ((Float) this.size.jT()).floatValue();
        float alphaMul = ((Float) this.opacity.jT()).floatValue();
        float glowMul = ((Float) this.glow.jT()).floatValue();
        for (int i = 0, count = this.petals.size(); i < count; i++) {
            Petal p = this.petals.get(i);
            double dx = p.x - cam.field_1352;
            double dy = p.y - cam.field_1351;
            double dz = p.z - cam.field_1350;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > r + 4.0) {
                continue;
            }
            float distanceFade = distance <= fadeStart ? 1.0f
                : 1.0f - Math.min(1.0f, Math.max(0.0f, (float) ((distance - fadeStart) / (r - fadeStart + 1.0f))));
            if (distanceFade <= 0.02f) {
                continue;
            }
            float nearFade = distance < 1.25 ? (float) (distance / 1.25) : 1.0f;
            float groundFade = Math.min(1.0f, Math.max(0.0f, (float) ((p.y - p.ground - 0.6) / 2.4)));
            groundFade = groundFade * groundFade * (3.0f - 2.0f * groundFade);
            float alpha = Math.min(0.94f, alphaMul * p.brightness * distanceFade * nearFade * groundFade * 0.92f);
            if (alpha <= 0.008f) {
                continue;
            }

            SakuraModule.SCRATCH_ROT.rotationYXZ(p.yaw, p.pitch, p.roll);
            SakuraModule.SCRATCH_RIGHT.set(1.0f, 0.0f, 0.0f).rotate(SakuraModule.SCRATCH_ROT);
            SakuraModule.SCRATCH_UP.set(0.0f, 1.0f, 0.0f).rotate(SakuraModule.SCRATCH_ROT);
            float rx = SakuraModule.SCRATCH_RIGHT.x;
            float ry = SakuraModule.SCRATCH_RIGHT.y;
            float rz = SakuraModule.SCRATCH_RIGHT.z;
            float ux = SakuraModule.SCRATCH_UP.x;
            float uy = SakuraModule.SCRATCH_UP.y;
            float uz = SakuraModule.SCRATCH_UP.z;
            float halfW = 0.105f * p.size * sizeMul;
            float halfH = 0.175f * p.size * sizeMul;

            int bottom = argb(alpha * 0.80f, tint[0] * 0.86f, tint[1] * 0.74f, tint[2] * 0.82f);
            int top = argb(alpha, tint[0], tint[1], tint[2]);
            Render3D.drawTexturedQuadCustomRel(ms, PETAL,
                (float) (p.x - rx * halfW - ux * halfH - cam.field_1352),
                (float) (p.y - ry * halfW - uy * halfH - cam.field_1351),
                (float) (p.z - rz * halfW - uz * halfH - cam.field_1350), 0.0f, 1.0f, bottom,
                (float) (p.x + rx * halfW - ux * halfH - cam.field_1352),
                (float) (p.y + ry * halfW - uy * halfH - cam.field_1351),
                (float) (p.z + rz * halfW - uz * halfH - cam.field_1350), 1.0f, 1.0f, bottom,
                (float) (p.x + rx * halfW + ux * halfH - cam.field_1352),
                (float) (p.y + ry * halfW + uy * halfH - cam.field_1351),
                (float) (p.z + rz * halfW + uz * halfH - cam.field_1350), 1.0f, 0.0f, top,
                (float) (p.x - rx * halfW + ux * halfH - cam.field_1352),
                (float) (p.y - ry * halfW + uy * halfH - cam.field_1351),
                (float) (p.z - rz * halfW + uz * halfH - cam.field_1350), 0.0f, 0.0f, top, true);

            if (glowMul > 0.02f && alpha * 0.10f * glowMul > 0.02f) {
                int glowColor = argb(alpha * 0.10f * glowMul, tint[0], tint[1], tint[2]);
                Render3D.renderBillboardTexture(p.x, p.y, p.z, halfH * 3.2f, GLOW, glowColor, true, true);
            }
        }
    }

    private static int argb(float alpha, float red, float green, float blue) {
        int a = (int) (Math.min(1.0f, Math.max(0.0f, alpha)) * 255.0f);
        int r = (int) (Math.min(1.0f, Math.max(0.0f, red)) * 255.0f);
        int g = (int) (Math.min(1.0f, Math.max(0.0f, green)) * 255.0f);
        int b = (int) (Math.min(1.0f, Math.max(0.0f, blue)) * 255.0f);
        return a << 24 | r << 16 | g << 8 | b;
    }

    static enum SakuraPreset
    implements NamedOption {
        DREAM("modules.settings.sakura.preset.dream"),
        HANAMI("modules.settings.sakura.preset.hanami"),
        PETAL_STORM("modules.settings.sakura.preset.storm");

        private final String name;

        SakuraPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    static enum SakuraColor
    implements NamedOption {
        PINK("modules.settings.sakura.color.pink"),
        WHITE("modules.settings.sakura.color.white"),
        AUTUMN("modules.settings.sakura.color.autumn"),
        CUSTOM("modules.settings.sakura.color.custom");

        private final String name;

        SakuraColor(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private static final class Petal {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        double ground;
        float mass = 1.0f;
        float size = 1.0f;
        float phase;
        float yaw;
        float pitch;
        float roll;
        float spinYaw;
        float spinPitch;
        float spinRoll;
        float sway = 1.0f;
        float brightness = 1.0f;
        boolean dead;
    }
}
