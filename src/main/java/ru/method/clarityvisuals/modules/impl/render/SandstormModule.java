package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_243;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
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
import ru.method.clarityvisuals.utils.render.Render3D;

/**
 * Desert storm: fast sand streaks, soft rolling dust and a shader pass that
 * adds distance haze plus wind-carved ripples on every upward face.
 */
public class SandstormModule
extends Module {
    private static final class_2960 PUFF = ClarityVisuals.id("client/particles/sand_puff.png");
    private static final int MAX_GRAINS = 3600;
    private static final int MAX_PUFFS = 100;

    private final EnumSetting<StormPreset> preset = new EnumSetting<StormPreset>("modules.settings.sandstorm.preset", StormPreset.HABOOB);
    private final SliderSetting density = new SliderSetting("modules.settings.sandstorm.density", 1.0f, 0.15f, 3.0f, 0.05f);
    private final SliderSetting radius = new SliderSetting("modules.settings.sandstorm.radius", 31.0f, 8.0f, 64.0f, 1.0f);
    private final SliderSetting altitude = new SliderSetting("modules.settings.sandstorm.altitude", 24.0f, 6.0f, 44.0f, 1.0f);
    private final SliderSetting size = new SliderSetting("modules.settings.sandstorm.size", 1.0f, 0.3f, 2.5f, 0.05f);
    private final SliderSetting wind = new SliderSetting("modules.settings.sandstorm.wind", 1.0f, 0.0f, 3.0f, 0.05f);
    private final SliderSetting opacity = new SliderSetting("modules.settings.sandstorm.opacity", 1.0f, 0.2f, 2.0f, 0.05f);
    private final SliderSetting haze = new SliderSetting("modules.settings.sandstorm.haze", 1.0f, 0.05f, 2.5f, 0.05f);
    private final SliderSetting ground = new SliderSetting("modules.settings.sandstorm.ground", 1.0f, 0.0f, 2.0f, 0.05f);
    private final SliderSetting distance = new SliderSetting("modules.settings.sandstorm.distance", 82.0f, 16.0f, 150.0f, 2.0f);
    private final ColorSetting color = new ColorSetting("modules.settings.sandstorm.color", new Color(207, 168, 112));
    private final BooleanSetting puffs = new BooleanSetting("modules.settings.sandstorm.puffs", true);
    private final BooleanSetting skyCheck = new BooleanSetting("modules.settings.sandstorm.sky_check", true);

    private final List<Grain> grains = new ArrayList<>();
    private final List<Puff> puffList = new ArrayList<>();
    private final Map<Long, Integer> groundCache = new HashMap<>();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float clock;
    private int cacheTicks;

    public SandstormModule() {
        super("Sandstorm", ModuleCategory.ql);
        this.getSettings().add(this.preset);
        this.getSettings().add(this.density);
        this.getSettings().add(this.radius);
        this.getSettings().add(this.altitude);
        this.getSettings().add(this.size);
        this.getSettings().add(this.wind);
        this.getSettings().add(this.opacity);
        this.getSettings().add(this.haze);
        this.getSettings().add(this.ground);
        this.getSettings().add(this.distance);
        this.getSettings().add(this.color);
        this.getSettings().add(this.puffs);
        this.getSettings().add(this.skyCheck);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastFrameNanos = 0L;
        this.clearAll();
        AtmosphereSurface.getInstance().reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.clearAll();
        AtmosphereSurface.getInstance().reset();
    }

    private void clearAll() {
        this.grains.clear();
        this.puffList.clear();
        this.groundCache.clear();
    }

    private float presetDensity() {
        return switch (this.preset.jT()) {
            case DESERT_WIND -> 0.62f;
            case APOCALYPSE -> 1.90f;
            default -> 1.15f;
        };
    }

    private float presetWind() {
        return switch (this.preset.jT()) {
            case DESERT_WIND -> 0.75f;
            case APOCALYPSE -> 2.35f;
            default -> 1.25f;
        };
    }

    private int targetGrains() {
        float r = ((Float) this.radius.jT()).floatValue();
        int count = (int) (r * r * 1.15f * ((Float) this.density.jT()).floatValue() * this.presetDensity());
        return Math.min(MAX_GRAINS, Math.max(320, count));
    }

    private int targetPuffs() {
        int count = (int) (40.0f * ((Float) this.density.jT()).floatValue() * this.presetDensity());
        return Math.min(MAX_PUFFS, Math.max(8, count));
    }

    private float random(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private int surfaceY(int x, int z) {
        long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
        Integer cached = this.groundCache.get(key);
        if (cached != null) {
            return cached;
        }
        int top;
        try {
            top = SandstormModule.aiG.field_1687.method_8624(class_2902.class_2903.field_13197, x, z);
        } catch (Throwable ignored) {
            top = SandstormModule.aiG.field_1687.method_31607();
        }
        if (this.groundCache.size() < 24000) {
            this.groundCache.put(key, top);
        }
        return top;
    }

    private boolean skyVisible(double x, double y, double z) {
        if (!((Boolean) this.skyCheck.jT()).booleanValue() || SandstormModule.aiG.field_1687 == null) {
            return true;
        }
        return this.surfaceY((int) Math.floor(x), (int) Math.floor(z)) <= (int) Math.floor(y) + 1;
    }

    private int tint() {
        Color c = (Color) this.color.jT();
        return 0xFF000000 | c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
    }

    private void spawnGrain(class_243 center) {
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * r;
        Grain grain = new Grain();
        grain.x = center.field_1352 + Math.cos(angle) * dist;
        grain.z = center.field_1350 + Math.sin(angle) * dist;
        grain.y = center.field_1351 + this.random(-3.0f, alt);
        if (!this.skyVisible(grain.x, grain.y, grain.z)) {
            return;
        }
        grain.mass = this.random(0.55f, 1.45f);
        grain.size = this.random(0.6f, 1.5f);
        grain.phase = this.random(0.0f, 62.8f);
        grain.brightness = this.random(0.55f, 1.0f);
        grain.ground = this.surfaceY((int) Math.floor(grain.x), (int) Math.floor(grain.z));
        grain.vx = this.random(2.0f, 6.0f) * this.presetWind();
        grain.vz = this.random(-1.0f, 1.0f) * this.presetWind();
        grain.vy = this.random(-0.35f, 0.35f);
        this.grains.add(grain);
    }

    private void updateGrains(class_243 center, float dt) {
        int target = this.targetGrains();
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        this.grains.removeIf(g -> {
            double dx = g.x - center.field_1352;
            double dz = g.z - center.field_1350;
            float rr = r + 8.0f;
            return g.dead || dx * dx + dz * dz > rr * rr || g.y < center.field_1351 - alt - 16.0;
        });
        int budget = Math.min(Math.max(0, target - this.grains.size()), 760);
        for (int i = 0; i < budget; i++) {
            this.spawnGrain(center);
        }

        float windMul = ((Float) this.wind.jT()).floatValue() * this.presetWind();
        for (int i = 0, count = this.grains.size(); i < count; i++) {
            Grain g = this.grains.get(i);
            double gust = 0.55 + 0.45 * Math.sin(this.clock * 0.70 + g.phase * 0.15)
                + 0.22 * Math.sin(this.clock * 2.1 + g.phase);
            double targetX = windMul * (10.0 + 3.0 * Math.sin(this.clock * 0.35 + g.phase)) * gust;
            double targetZ = windMul * (4.2 * Math.sin(this.clock * 0.27 + g.phase * 0.05)
                + 2.0 * Math.cos(this.clock * 0.9 + g.phase)) * gust;
            double targetY = Math.sin(this.clock * 1.3 + g.phase) * 0.7 + 0.12;

            g.vx += (targetX - g.vx) * Math.min(1.0, dt * 2.5);
            g.vz += (targetZ - g.vz) * Math.min(1.0, dt * 2.5);
            g.vy += (targetY - g.vy) * Math.min(1.0, dt * 2.0);

            g.x += g.vx * dt;
            g.y += g.vy * dt;
            g.z += g.vz * dt;

            if (g.y < g.ground + 0.10) {
                g.y = g.ground + 0.10;
                g.vy = Math.abs(g.vy) * 0.45 + 0.35;
            }
            if (g.y > center.field_1351 + alt + 2.0) {
                g.y = center.field_1351 + alt + 2.0;
                g.vy = -Math.abs(g.vy) * 0.4;
            }
        }
    }

    private void spawnPuff(class_243 center) {
        float r = ((Float) this.radius.jT()).floatValue();
        Puff puff = new Puff();
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * r * 0.95;
        puff.x = center.field_1352 + Math.cos(angle) * dist;
        puff.z = center.field_1350 + Math.sin(angle) * dist;
        int ground = this.surfaceY((int) Math.floor(puff.x), (int) Math.floor(puff.z));
        boolean low = this.random.nextFloat() < 0.58f;
        puff.y = low ? ground + this.random(0.25f, 2.2f) : ground + this.random(2.0f, 9.0f);
        puff.size = low ? this.random(2.2f, 6.2f) : this.random(1.4f, 4.4f);
        puff.alpha = this.random(0.065f, 0.155f);
        puff.vx = this.random(1.4f, 4.0f) * this.presetWind();
        puff.vz = this.random(-1.3f, 1.3f) * this.presetWind();
        puff.vy = this.random(-0.05f, 0.15f);
        puff.phase = this.random(0.0f, 62.8f);
        puff.life = 0.0f;
        puff.maxLife = this.random(3.0f, 7.0f);
        this.puffList.add(puff);
    }

    private void updatePuffs(class_243 center, float dt) {
        if (!((Boolean) this.puffs.jT()).booleanValue()) {
            this.puffList.clear();
            return;
        }
        float r = ((Float) this.radius.jT()).floatValue();
        this.puffList.removeIf(p -> {
            p.life += dt;
            double dx = p.x - center.field_1352;
            double dz = p.z - center.field_1350;
            return p.life > p.maxLife || dx * dx + dz * dz > r * r * 1.4;
        });
        int budget = Math.min(Math.max(0, this.targetPuffs() - this.puffList.size()), 8);
        for (int i = 0; i < budget; i++) {
            this.spawnPuff(center);
        }
        float windMul = ((Float) this.wind.jT()).floatValue() * this.presetWind();
        for (int i = 0, count = this.puffList.size(); i < count; i++) {
            Puff p = this.puffList.get(i);
            p.x += (p.vx + windMul * 2.0) * dt;
            p.z += p.vz * dt;
            p.y += (p.vy + Math.sin(this.clock * 0.6 + p.phase) * 0.06) * dt;
        }
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (SandstormModule.fullNullCheck()) {
            this.clearAll();
            return;
        }
        class_4184 camera = SandstormModule.aiG.field_1773.method_19418();
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
        if (++this.cacheTicks > 30) {
            this.cacheTicks = 0;
            if (this.groundCache.size() > 28000) {
                this.groundCache.clear();
            }
        }

        class_243 cam = camera.method_19326();
        this.updateGrains(cam, dt);
        this.updatePuffs(cam, dt);
        this.renderHaze(camera, cam);

        class_4587 ms = stage.l();
        if (!this.puffList.isEmpty()) {
            this.drawPuffs(ms, cam);
        }
        if (!this.grains.isEmpty()) {
            this.drawGrains(ms, cam, camera.method_19330());
        }
    }

    private void renderHaze(class_4184 camera, class_243 cam) {
        try {
            org.joml.Matrix4f viewRot = new org.joml.Matrix4f().rotation(camera.method_23767());
            org.joml.Matrix4f viewMatrix = new org.joml.Matrix4f(viewRot).invert();
            org.joml.Matrix4f projectionMatrix = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix();
            AtmosphereSurface.Parameters parameters = new AtmosphereSurface.Parameters();
            Color c = (Color) this.color.jT();
            parameters.mode = AtmosphereSurface.MODE_SANDSTORM;
            parameters.density = ((Float) this.haze.jT()).floatValue();
            parameters.height = 12.0f;
            parameters.maxDistance = ((Float) this.distance.jT()).floatValue();
            parameters.speed = ((Float) this.wind.jT()).floatValue() * this.presetWind();
            parameters.ground = ((Float) this.ground.jT()).floatValue();
            parameters.baseY = this.surfaceY((int) Math.floor(cam.field_1352), (int) Math.floor(cam.field_1350)) + 1;
            parameters.time = this.clock;
            parameters.red = c.getRed() / 255.0f;
            parameters.green = c.getGreen() / 255.0f;
            parameters.blue = c.getBlue() / 255.0f;
            AtmosphereSurface.getInstance().apply(camera, viewMatrix, projectionMatrix, parameters);
        } catch (Throwable ignored) {
        }
    }

    private void drawGrains(class_4587 ms, class_243 cam, float cameraYawDeg) {
        float r = ((Float) this.radius.jT()).floatValue();
        float fadeStart = r * 0.62f;
        float sizeMul = ((Float) this.size.jT()).floatValue();
        float alphaMul = ((Float) this.opacity.jT()).floatValue();
        float yawRad = (float) Math.toRadians(cameraYawDeg);
        int rgb = this.tint();
        for (int i = 0, count = this.grains.size(); i < count; i++) {
            Grain g = this.grains.get(i);
            double dx = g.x - cam.field_1352;
            double dy = g.y - cam.field_1351;
            double dz = g.z - cam.field_1350;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > r + 5.0) {
                continue;
            }
            float distanceFade = distance <= fadeStart ? 1.0f
                : 1.0f - Math.min(1.0f, Math.max(0.0f, (float) ((distance - fadeStart) / (r - fadeStart + 1.0f))));
            if (distanceFade <= 0.02f) {
                continue;
            }
            float nearFade = distance < 0.8 ? (float) (distance / 0.8) : 1.0f;
            double speed = Math.sqrt(g.vx * g.vx + g.vy * g.vy + g.vz * g.vz);
            if (speed < 0.001) {
                continue;
            }
            float length = Math.min(2.4f, Math.max(0.18f, (float) speed * 0.064f * sizeMul * g.size));
            float width = (0.014f + 0.026f * g.mass) * sizeMul;
            float alpha = Math.min(0.94f, alphaMul * g.brightness * distanceFade * nearFade * 0.74f);
            if (alpha <= 0.008f) {
                continue;
            }
            float tiltRad = 1.28f + (float) Math.sin(g.phase) * 0.22f;
            float half = width * 0.5f;
            float top = length * 0.5f;
            float bottom = -length * 0.5f;
            double[] p1 = bill(-half, top, yawRad, tiltRad);
            double[] p2 = bill(half, top, yawRad, tiltRad);
            double[] p3 = bill(half, bottom, yawRad, tiltRad);
            double[] p4 = bill(-half, bottom, yawRad, tiltRad);
            Render3D.drawGradientQuad(ms,
                new class_243(g.x + p1[0], g.y + p1[1], g.z + p1[2]),
                new class_243(g.x + p2[0], g.y + p2[1], g.z + p2[2]),
                new class_243(g.x + p3[0], g.y + p3[1], g.z + p3[2]),
                new class_243(g.x + p4[0], g.y + p4[1], g.z + p4[2]),
                argb(alpha * 0.06f, rgb), argb(alpha * 0.06f, rgb),
                argb(alpha, rgb), argb(alpha, rgb), true);
        }
    }

    private void drawPuffs(class_4587 ms, class_243 cam) {
        int rgb = this.tint();
        for (int i = 0, count = this.puffList.size(); i < count; i++) {
            Puff p = this.puffList.get(i);
            double dx = p.x - cam.field_1352;
            double dy = p.y - cam.field_1351;
            double dz = p.z - cam.field_1350;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > ((Float) this.radius.jT()).floatValue() + 12.0) {
                continue;
            }
            float life = Math.min(1.0f, p.life / p.maxLife);
            float fade = (float) Math.sin(life * Math.PI);
            float nearFade = distance < 2.5 ? (float) (distance / 2.5) : 1.0f;
            int alpha = (int) (Math.min(0.22f, p.alpha * fade * nearFade) * 255.0f);
            if (alpha < 2) {
                continue;
            }
            Render3D.renderBillboardTexture(new class_243(p.x, p.y, p.z), p.size * sizeMultiplier(), PUFF,
                alpha << 24 | (rgb & 0xFFFFFF), true, false);
        }
    }

    private float sizeMultiplier() {
        return ((Float) this.size.jT()).floatValue();
    }

    private static double[] bill(double lx, double ly, float yawRad, float tiltRad) {
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double x1 = lx * cy;
        double z1 = -lx * sy;
        double ct = Math.cos(-tiltRad);
        double st = Math.sin(-tiltRad);
        return new double[]{x1 * ct - ly * st, x1 * st + ly * ct, z1};
    }

    private static int argb(float alpha, int rgb) {
        int a = (int) (Math.min(1.0f, Math.max(0.0f, alpha)) * 255.0f);
        return a << 24 | rgb & 0xFFFFFF;
    }

    static enum StormPreset
    implements NamedOption {
        DESERT_WIND("modules.settings.sandstorm.preset.desert"),
        HABOOB("modules.settings.sandstorm.preset.haboob"),
        APOCALYPSE("modules.settings.sandstorm.preset.apocalypse");

        private final String name;

        StormPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private static final class Grain {
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
        float brightness = 1.0f;
        boolean dead;
    }

    private static final class Puff {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        float size;
        float alpha;
        float phase;
        float life;
        float maxLife;
    }
}
