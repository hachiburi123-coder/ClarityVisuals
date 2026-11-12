package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2902;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.GroundHeightCache;
import ru.method.clarityvisuals.utils.render.Render3D;
import ru.method.clarityvisuals.utils.render.SnowGroundSurface;

public class WinterModule
extends Module {
    private final EnumSetting<SnowPreset> preset = new EnumSetting<SnowPreset>("modules.settings.winter.preset", SnowPreset.SNOWFALL);
    private final BooleanSetting snowfall = new BooleanSetting("modules.settings.winter.snowfall", true);
    private final SliderSetting density = new SliderSetting("modules.settings.winter.density", 1.0f, 0.1f, 3.0f, 0.05f, this.snowfall::jT);
    private final SliderSetting radius = new SliderSetting("modules.settings.winter.radius", 28.0f, 8.0f, 64.0f, 1.0f, this.snowfall::jT);
    private final SliderSetting altitude = new SliderSetting("modules.settings.winter.altitude", 18.0f, 6.0f, 40.0f, 1.0f, this.snowfall::jT);
    private final SliderSetting flakeSize = new SliderSetting("modules.settings.winter.flake_size", 1.0f, 0.3f, 2.5f, 0.05f, this.snowfall::jT);
    private final SliderSetting fallSpeed = new SliderSetting("modules.settings.winter.fall_speed", 1.0f, 0.3f, 2.5f, 0.05f, this.snowfall::jT);
    private final SliderSetting wind = new SliderSetting("modules.settings.winter.wind", 1.0f, 0.0f, 3.0f, 0.05f);
    private final SliderSetting sway = new SliderSetting("modules.settings.winter.sway", 1.0f, 0.0f, 2.0f, 0.05f, this.snowfall::jT);
    private final SliderSetting opacity = new SliderSetting("modules.settings.winter.opacity", 1.0f, 0.1f, 2.0f, 0.05f, this.snowfall::jT);
    private final BooleanSetting skyCheck = new BooleanSetting("modules.settings.winter.sky_check", true);
    private final BooleanSetting onlyWhenSnowing = new BooleanSetting("modules.settings.winter.only_snowing", false);
    private final EnumSetting<SnowColorMode> colorMode = new EnumSetting<SnowColorMode>("modules.settings.winter.color_mode", SnowColorMode.REALISTIC);
    private final ColorSetting customColor = new ColorSetting("modules.settings.winter.color", new Color(244, 248, 255, 255), () -> this.colorMode.jT() == SnowColorMode.CUSTOM);
    private final BooleanSetting groundDrift = new BooleanSetting("modules.settings.winter.ground_drift", true);
    private final SliderSetting driftAmount = new SliderSetting("modules.settings.winter.drift_amount", 1.0f, 0.1f, 2.0f, 0.05f, this.groundDrift::jT);
    private final SliderSetting driftSpeed = new SliderSetting("modules.settings.winter.drift_speed", 1.0f, 0.2f, 2.5f, 0.05f, this.groundDrift::jT);
    private final SliderSetting driftHeight = new SliderSetting("modules.settings.winter.drift_height", 0.35f, 0.05f, 1.2f, 0.05f, this.groundDrift::jT);
    private final SliderSetting driftOpacity = new SliderSetting("modules.settings.winter.drift_opacity", 1.0f, 0.1f, 2.0f, 0.05f, this.groundDrift::jT);
    private final BooleanSetting snowGround = new BooleanSetting("modules.settings.winter.snow_ground", true);
    private final SliderSetting coverage = new SliderSetting("modules.settings.winter.coverage", 0.90f, 0.05f, 1.0f, 0.05f, this.snowGround::jT);
    private final SliderSetting brightness = new SliderSetting("modules.settings.winter.brightness", 1.15f, 0.5f, 1.8f, 0.05f, this.snowGround::jT);
    private final BooleanSetting sparkles = new BooleanSetting("modules.settings.winter.sparkles", true, this.snowGround::jT);
    private final SliderSetting sparkleStrength = new SliderSetting("modules.settings.winter.sparkle_strength", 0.8f, 0.0f, 1.6f, 0.05f, () -> ((Boolean) this.snowGround.jT()).booleanValue() && ((Boolean) this.sparkles.jT()).booleanValue());
    private final SliderSetting snowDistance = new SliderSetting("modules.settings.winter.snow_distance", 56.0f, 12.0f, 96.0f, 1.0f, this.snowGround::jT);

    private static final double GRAVITY = 3.4;
    private static final double TERMINAL_VELOCITY = 3.2;
    private static final int MAX_FLAKES = 3500;
    private static final int MAX_DRIFT = 420;
    private final List<Flake> flakes = new ArrayList<>();
    private final List<Drift> driftList = new ArrayList<>();
    private final GroundHeightCache groundCache = new GroundHeightCache();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float clock;
    private int cacheTicks;

    public WinterModule() {
        super("Winter", ModuleCategory.ql);
        this.getSettings().add(this.preset);
        this.getSettings().add(this.snowfall);
        this.getSettings().add(this.density);
        this.getSettings().add(this.radius);
        this.getSettings().add(this.altitude);
        this.getSettings().add(this.flakeSize);
        this.getSettings().add(this.fallSpeed);
        this.getSettings().add(this.wind);
        this.getSettings().add(this.sway);
        this.getSettings().add(this.opacity);
        this.getSettings().add(this.skyCheck);
        this.getSettings().add(this.onlyWhenSnowing);
        this.getSettings().add(this.colorMode);
        this.getSettings().add(this.customColor);
        this.getSettings().add(this.groundDrift);
        this.getSettings().add(this.driftAmount);
        this.getSettings().add(this.driftSpeed);
        this.getSettings().add(this.driftHeight);
        this.getSettings().add(this.driftOpacity);
        this.getSettings().add(this.snowGround);
        this.getSettings().add(this.coverage);
        this.getSettings().add(this.brightness);
        this.getSettings().add(this.sparkles);
        this.getSettings().add(this.sparkleStrength);
        this.getSettings().add(this.snowDistance);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastFrameNanos = 0L;
        SnowGroundSurface.getInstance().reset();
        this.clearAll();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SnowGroundSurface.getInstance().reset();
        this.clearAll();
    }

    private void clearAll() {
        this.flakes.clear();
        this.driftList.clear();
        this.groundCache.clear();
    }

    @EventHandler
    private void onTick(ClientTickEvent event) {
        if (WinterModule.fullNullCheck()) {
            this.clearAll();
            return;
        }
    }

    private float presetDensity() {
        if (this.preset.jT() == SnowPreset.LIGHT_SNOW) {
            return 0.5f;
        }
        if (this.preset.jT() == SnowPreset.BLIZZARD) {
            return 2.2f;
        }
        return 1.0f;
    }

    private float presetWind() {
        if (this.preset.jT() == SnowPreset.LIGHT_SNOW) {
            return 0.6f;
        }
        if (this.preset.jT() == SnowPreset.BLIZZARD) {
            return 2.0f;
        }
        return 1.0f;
    }

    private float presetMass() {
        if (this.preset.jT() == SnowPreset.LIGHT_SNOW) {
            return 0.8f;
        }
        if (this.preset.jT() == SnowPreset.BLIZZARD) {
            return 1.25f;
        }
        return 1.0f;
    }

    private int targetFlakeCount() {
        float r = ((Float) this.radius.jT()).floatValue();
        float area = r * r * 0.0155f;
        int count = (int) (area * ((Float) this.density.jT()).floatValue() * this.presetDensity() * 22.0f);
        return Math.min(MAX_FLAKES, Math.max(40, count));
    }

    private int targetDriftCount() {
        int count = (int) (90.0f * ((Float) this.density.jT()).floatValue() * this.presetDensity() * ((Float) this.driftAmount.jT()).floatValue());
        return Math.min(MAX_DRIFT, Math.max(8, count));
    }

    private float[] resolveColor() {
        int color;
        if (this.colorMode.jT() == SnowColorMode.CUSTOM) {
            color = ((Color) this.customColor.jT()).getRGB();
        } else if (this.colorMode.jT() == SnowColorMode.CLIENT) {
            color = ColorUtils.getGlobalColor().getRGB();
        } else {
            color = 0xFFF6FAFF;
        }
        return new float[]{((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f};
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
            top = WinterModule.aiG.field_1687.method_8624(class_2902.class_2903.field_13197, x, z);
        } catch (Throwable ignored) {
            top = WinterModule.aiG.field_1687.method_31607();
        }
        this.groundCache.put(key, top);
        return top;
    }

    private boolean isBlocking(double x, double y, double z) {
        if (WinterModule.aiG.field_1687 == null) {
            return false;
        }
        class_2338 pos = new class_2338((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        try {
            if (WinterModule.aiG.field_1687.method_8320(pos).method_26215()) {
                return false;
            }
            return WinterModule.aiG.field_1687.method_8320(pos).method_51366();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean skyVisible(double x, double y, double z) {
        if (!((Boolean) this.skyCheck.jT()).booleanValue() || WinterModule.aiG.field_1687 == null) {
            return true;
        }
        return this.surfaceY((int) Math.floor(x), (int) Math.floor(z)) <= (int) Math.floor(y) + 1;
    }

    private void spawnFlake(class_243 center, boolean fromTop) {
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * r;
        double x = center.field_1352 + Math.cos(angle) * dist;
        double z = center.field_1350 + Math.sin(angle) * dist;
        double y = fromTop ? center.field_1351 + alt * this.random(0.75f, 1.0f) : center.field_1351 + this.random(-4.0f, alt);
        if (!this.skyVisible(x, y, z)) {
            return;
        }
        Flake flake = new Flake();
        flake.x = x;
        flake.y = y;
        flake.z = z;
        flake.mass = this.random(0.6f, 1.4f) * this.presetMass();
        flake.size = this.random(0.6f, 1.4f);
        flake.vy = -this.random(0.8f, 1.8f) * flake.mass;
        flake.phase = this.random(0.0f, 62.8f);
        flake.spin = this.random(0.0f, 6.28f);
        flake.spinSpeed = this.random(-2.5f, 2.5f);
        flake.swayAmp = this.random(0.6f, 1.6f);
        flake.swayFreq = this.random(0.8f, 2.2f);
        flake.brightness = this.random(0.7f, 1.0f);
        flake.ground = this.surfaceY((int) Math.floor(x), (int) Math.floor(z));
        this.flakes.add(flake);
    }

    private void updateFlakes(class_243 center, float dt) {
        if (!((Boolean) this.snowfall.jT()).booleanValue()) {
            this.flakes.clear();
            return;
        }
        int target = this.targetFlakeCount();
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        this.flakes.removeIf(f -> {
            double dx = f.x - center.field_1352;
            double dz = f.z - center.field_1350;
            float rr = r + 6.0f;
            return f.dead || dx * dx + dz * dz > rr * rr || f.y < center.field_1351 - alt - 12.0;
        });
        boolean fill = this.flakes.size() < target / 2;
        int spawnBudget = fill ? Math.min(target - this.flakes.size(), 700) : Math.min(target - this.flakes.size(), 220);
        for (int i = 0; i < spawnBudget; i++) {
            this.spawnFlake(center, !fill);
        }
        float speedMul = ((Float) this.fallSpeed.jT()).floatValue();
        float windMul = ((Float) this.wind.jT()).floatValue() * this.presetWind();
        float swayMul = ((Float) this.sway.jT()).floatValue();
        for (int i = 0, size = this.flakes.size(); i < size; i++) {
            Flake f = this.flakes.get(i);
            double gust = 0.65 + 0.35 * Math.sin(this.clock * 0.55 + f.phase * 0.15) + 0.18 * Math.sin(this.clock * 1.7 + f.phase);
            double windX = windMul * 3.1 * gust;
            double windZ = windMul * 1.7 * Math.sin(this.clock * 0.31 + f.phase * 0.05) * gust;
            double swayX = Math.sin(this.clock * f.swayFreq + f.phase) * f.swayAmp * swayMul;
            double swayZ = Math.cos(this.clock * f.swayFreq * 0.9 + f.phase * 1.3) * f.swayAmp * swayMul * 0.8;
            double drag = 1.0 - Math.min(0.92, Math.abs(f.vy) / (TERMINAL_VELOCITY * f.mass));
            f.vy -= GRAVITY * f.mass * drag * dt;
            if (f.vy < -TERMINAL_VELOCITY * f.mass) {
                f.vy = -TERMINAL_VELOCITY * f.mass;
            }
            f.vx += ((windX + swayX) - f.vx) * Math.min(1.0, dt * 1.8);
            f.vz += ((windZ + swayZ) - f.vz) * Math.min(1.0, dt * 1.8);
            f.spin += f.spinSpeed * dt;
            double nx = f.x + f.vx * dt * speedMul;
            double ny = f.y + f.vy * dt * speedMul;
            double nz = f.z + f.vz * dt * speedMul;
            if (ny <= f.ground + 0.06) {
                f.dead = true;
                continue;
            }
            f.x = nx;
            f.y = ny;
            f.z = nz;
        }
    }

    private void spawnDrift(class_243 center) {
        float r = ((Float) this.radius.jT()).floatValue();
        float maxH = ((Float) this.driftHeight.jT()).floatValue();
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * r * 0.9;
        double x = center.field_1352 + Math.cos(angle) * dist;
        double z = center.field_1350 + Math.sin(angle) * dist;
        int ground = this.surfaceY((int) Math.floor(x), (int) Math.floor(z));
        Drift drift = new Drift();
        drift.x = x;
        drift.z = z;
        drift.y = ground + this.random(0.03f, Math.max(0.06f, maxH));
        drift.ground = ground;
        drift.life = 0.0f;
        drift.maxLife = this.random(1.2f, 3.0f);
        drift.size = this.random(0.5f, 1.3f);
        drift.seed = this.random(0.0f, 62.8f);
        this.driftList.add(drift);
    }

    private void updateDrift(class_243 center, float dt) {
        if (!((Boolean) this.groundDrift.jT()).booleanValue()) {
            this.driftList.clear();
            return;
        }
        float r = ((Float) this.radius.jT()).floatValue();
        float speedMul = ((Float) this.driftSpeed.jT()).floatValue();
        float maxH = ((Float) this.driftHeight.jT()).floatValue();
        this.driftList.removeIf(d -> {
            d.life += dt;
            double dx = d.x - center.field_1352;
            double dz = d.z - center.field_1350;
            return d.life > d.maxLife || dx * dx + dz * dz > r * r;
        });
        int target = this.targetDriftCount();
        int spawnBudget = Math.min(target - this.driftList.size(), 40);
        for (int i = 0; i < spawnBudget; i++) {
            this.spawnDrift(center);
        }
        double dirX = 0.9928;
        double dirZ = 0.1191;
        for (int i = 0, size = this.driftList.size(); i < size; i++) {
            Drift d = this.driftList.get(i);
            double base = (3.0 + 2.0 * Math.sin(d.seed + this.clock * 0.7)) * speedMul;
            double turbulence = Math.sin(d.z * 0.8 + this.clock * 2.0 + d.seed) * 0.8 * speedMul;
            d.x += (dirX * base + -dirZ * turbulence) * dt;
            d.z += (dirZ * base + dirX * turbulence) * dt;
            double hover = 0.03 + (0.5 + 0.5 * Math.sin(this.clock * 2.0 + d.seed * 3.0)) * maxH * 0.6;
            d.y += ((d.ground + hover) - d.y) * Math.min(1.0, dt * 3.0);
        }
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (WinterModule.fullNullCheck()) {
            return;
        }
        if (((Boolean) this.onlyWhenSnowing.jT()).booleanValue() && !WinterModule.aiG.field_1687.method_8419()) {
            this.clearAll();
            return;
        }
        class_4184 camera = WinterModule.aiG.field_1773.method_19418();
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
        float camYaw = camera.method_19330();
        this.updateFlakes(cam, dt);
        this.updateDrift(cam, dt);
        if (((Boolean) this.snowGround.jT()).booleanValue()) {
            this.renderSnowGround(camera);
        }
        if (this.flakes.isEmpty() && this.driftList.isEmpty()) {
            return;
        }
        float[] rgb = this.resolveColor();
        class_4587 ms = stage.l();
        if (!this.driftList.isEmpty()) {
            this.drawDrift(ms, cam, camYaw, rgb);
        }
        if (!this.flakes.isEmpty()) {
            this.drawFlakes(ms, cam, camYaw, rgb);
        }
    }

    private void renderSnowGround(class_4184 camera) {
        try {
            org.joml.Matrix4f viewRot = new org.joml.Matrix4f().rotation(camera.method_23767());
            org.joml.Matrix4f viewMatrix = new org.joml.Matrix4f(viewRot).invert();
            org.joml.Matrix4f projectionMatrix = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix();
            SnowGroundSurface.Parameters parameters = new SnowGroundSurface.Parameters();
            parameters.coverage = ((Float) this.coverage.jT()).floatValue();
            parameters.brightness = ((Float) this.brightness.jT()).floatValue();
            parameters.sparkle = ((Float) this.sparkleStrength.jT()).floatValue();
            parameters.snowAmount = Math.min(1.6f, Math.max(0.5f, 0.6f + ((Float) this.density.jT()).floatValue() * this.presetDensity() * 0.25f));
            parameters.maxDistance = ((Float) this.snowDistance.jT()).floatValue();
            parameters.sparkles = ((Boolean) this.sparkles.jT()).booleanValue();
            SnowGroundSurface.getInstance().apply(camera, viewMatrix, projectionMatrix, parameters);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Смещение угла квада без аллокации массива: чистая double-математика прежнего
     * bill(), результат сразу приводится к float. cy/sy/ct/st считаются один раз
     * на частицу и передаются параметрами.
     */
    private static float billOffX(double lx, double ly, double cy, double ct, double st) {
        return (float) (lx * cy * ct - ly * st);
    }

    private static float billOffY(double lx, double ly, double cy, double ct, double st) {
        return (float) (lx * cy * st + ly * ct);
    }

    private static float billOffZ(double lx, double sy) {
        return (float) (-lx * sy);
    }

    /** ARGB-цвет в один int — прежний путь создавал java.awt.Color на каждый угол. */
    private static int cInt(float[] rgb, float a) {
        int ai = (int) (clamp01(a) * 255.0f + 0.5f);
        int ri = (int) (clamp01(rgb[0]) * 255.0f + 0.5f);
        int gi = (int) (clamp01(rgb[1]) * 255.0f + 0.5f);
        int bi = (int) (clamp01(rgb[2]) * 255.0f + 0.5f);
        return ai << 24 | ri << 16 | gi << 8 | bi;
    }

    private void drawFlakes(class_4587 ms, class_243 cam, float camYawDeg, float[] rgb) {
        float sizeMul = ((Float) this.flakeSize.jT()).floatValue();
        float alphaMul = ((Float) this.opacity.jT()).floatValue();
        float r = ((Float) this.radius.jT()).floatValue();
        float fadeStart = r * 0.62f;
        double yawRad = (float) Math.toRadians(camYawDeg);
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        for (int i = 0, size = this.flakes.size(); i < size; i++) {
            Flake f = this.flakes.get(i);
            double dx = f.x - camX;
            double dy = f.y - camY;
            double dz = f.z - camZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > r + 4.0) {
                continue;
            }
            float distanceFade = dist <= fadeStart ? 1.0f
                : 1.0f - Math.min(1.0f, Math.max(0.0f, (float) ((dist - fadeStart) / (r - fadeStart + 1.0f))));
            if (distanceFade <= 0.02f) {
                continue;
            }
            float nearFade = dist < 0.7 ? (float) (dist / 0.7) : 1.0f;
            float twinkle = 0.78f + 0.22f * (float) Math.sin(this.clock * 2.5 + f.phase);
            float sizeWorld = (float) ((0.035 + 0.030 * f.mass * f.size) * sizeMul);
            float alpha = Math.min(0.95f, alphaMul * f.brightness * distanceFade * nearFade * 0.60f * twinkle);
            if (alpha <= 0.006f) {
                continue;
            }
            double tiltRad = (float) f.spin;
            double ct = Math.cos(-tiltRad);
            double st = Math.sin(-tiltRad);
            float half = sizeWorld * 0.5f;
            int coreColor = cInt(rgb, alpha);
            Render3D.drawGradientQuadRel(ms, false,
                (float) (f.x + billOffX(-half, half, cy, ct, st) - camX),
                (float) (f.y + billOffY(-half, half, cy, ct, st) - camY),
                (float) (f.z + billOffZ(-half, sy) - camZ),
                (float) (f.x + billOffX(half, half, cy, ct, st) - camX),
                (float) (f.y + billOffY(half, half, cy, ct, st) - camY),
                (float) (f.z + billOffZ(half, sy) - camZ),
                (float) (f.x + billOffX(half, -half, cy, ct, st) - camX),
                (float) (f.y + billOffY(half, -half, cy, ct, st) - camY),
                (float) (f.z + billOffZ(half, sy) - camZ),
                (float) (f.x + billOffX(-half, -half, cy, ct, st) - camX),
                (float) (f.y + billOffY(-half, -half, cy, ct, st) - camY),
                (float) (f.z + billOffZ(-half, sy) - camZ),
                coreColor, coreColor, coreColor, coreColor);
            float halo = half * 2.4f;
            float haloAlpha = alpha * 0.25f;
            int transparent = cInt(rgb, 0.0f);
            int haloColor = cInt(rgb, haloAlpha);
            Render3D.drawGradientQuadRel(ms, false,
                (float) (f.x + billOffX(-halo, halo, cy, ct, st) - camX),
                (float) (f.y + billOffY(-halo, halo, cy, ct, st) - camY),
                (float) (f.z + billOffZ(-halo, sy) - camZ),
                (float) (f.x + billOffX(halo, halo, cy, ct, st) - camX),
                (float) (f.y + billOffY(halo, halo, cy, ct, st) - camY),
                (float) (f.z + billOffZ(halo, sy) - camZ),
                (float) (f.x + billOffX(halo, -halo, cy, ct, st) - camX),
                (float) (f.y + billOffY(halo, -halo, cy, ct, st) - camY),
                (float) (f.z + billOffZ(halo, sy) - camZ),
                (float) (f.x + billOffX(-halo, -halo, cy, ct, st) - camX),
                (float) (f.y + billOffY(-halo, -halo, cy, ct, st) - camY),
                (float) (f.z + billOffZ(-halo, sy) - camZ),
                transparent, transparent, haloColor, haloColor);
            if (sizeWorld > 0.045f) {
                float armLen = sizeWorld * 0.85f;
                float armW = sizeWorld * 0.11f;
                float armAlpha = alpha * 0.5f;
                int armColor = cInt(rgb, armAlpha);
                for (int arm = 0; arm < 2; arm++) {
                    double armTilt = (float) tiltRad + (arm == 0 ? 1.047f : -1.047f);
                    double act = Math.cos(-armTilt);
                    double ast = Math.sin(-armTilt);
                    Render3D.drawGradientQuadRel(ms, false,
                        (float) (f.x + billOffX(-armW, armLen, cy, act, ast) - camX),
                        (float) (f.y + billOffY(-armW, armLen, cy, act, ast) - camY),
                        (float) (f.z + billOffZ(-armW, sy) - camZ),
                        (float) (f.x + billOffX(armW, armLen, cy, act, ast) - camX),
                        (float) (f.y + billOffY(armW, armLen, cy, act, ast) - camY),
                        (float) (f.z + billOffZ(armW, sy) - camZ),
                        (float) (f.x + billOffX(armW, -armLen, cy, act, ast) - camX),
                        (float) (f.y + billOffY(armW, -armLen, cy, act, ast) - camY),
                        (float) (f.z + billOffZ(armW, sy) - camZ),
                        (float) (f.x + billOffX(-armW, -armLen, cy, act, ast) - camX),
                        (float) (f.y + billOffY(-armW, -armLen, cy, act, ast) - camY),
                        (float) (f.z + billOffZ(-armW, sy) - camZ),
                        transparent, transparent, armColor, armColor);
                }
            }
        }
    }

    private void drawDrift(class_4587 ms, class_243 cam, float camYawDeg, float[] rgb) {
        float r = ((Float) this.radius.jT()).floatValue();
        float fadeStart = r * 0.62f;
        double yawRad = Math.toRadians(camYawDeg);
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        float driftSpeedMul = ((Float) this.driftSpeed.jT()).floatValue();
        float opacityMul = ((Float) this.driftOpacity.jT()).floatValue();
        for (int i = 0, size = this.driftList.size(); i < size; i++) {
            Drift d = this.driftList.get(i);
            double dx = d.x - camX;
            double dy = d.y - camY;
            double dz = d.z - camZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > r + 4.0) {
                continue;
            }
            float distanceFade = dist <= fadeStart ? 1.0f
                : 1.0f - Math.min(1.0f, Math.max(0.0f, (float) ((dist - fadeStart) / (r - fadeStart + 1.0f))));
            if (distanceFade <= 0.02f) {
                continue;
            }
            float lifeFade = (float) Math.sin(Math.min(1.0f, Math.max(0.0f, d.life / d.maxLife)) * Math.PI);
            double h = d.y - d.ground;
            float heightFade = (float) Math.exp(-Math.max(0.0, h) * 7.0);
            double gateNoise = Math.sin(d.x * 0.22 - this.clock * 1.1 * driftSpeedMul + Math.sin(d.z * 0.65 + d.seed) * 1.4);
            float gate = 0.2f + 0.8f * smoothstep(-0.3f, 0.7f, (float) gateNoise);
            float alpha = Math.min(0.5f, opacityMul * 0.20f * heightFade * gate * distanceFade * lifeFade);
            if (alpha <= 0.006f) {
                continue;
            }
            float length = (0.5f + 0.5f * d.size) * (0.6f + driftSpeedMul * 0.5f);
            float width = 0.025f * d.size + 0.012f;
            double tiltRad = 1.35f;
            double ct = Math.cos(-tiltRad);
            double st = Math.sin(-tiltRad);
            float halfW = width * 0.5f;
            float top = length * 0.5f;
            float bottom = -length * 0.5f;
            int transparent = cInt(rgb, 0.0f);
            int alphaColor = cInt(rgb, alpha);
            Render3D.drawGradientQuadRel(ms, false,
                (float) (d.x + billOffX(-halfW, top, cy, ct, st) - camX),
                (float) (d.y + billOffY(-halfW, top, cy, ct, st) - camY),
                (float) (d.z + billOffZ(-halfW, sy) - camZ),
                (float) (d.x + billOffX(halfW, top, cy, ct, st) - camX),
                (float) (d.y + billOffY(halfW, top, cy, ct, st) - camY),
                (float) (d.z + billOffZ(halfW, sy) - camZ),
                (float) (d.x + billOffX(halfW, bottom, cy, ct, st) - camX),
                (float) (d.y + billOffY(halfW, bottom, cy, ct, st) - camY),
                (float) (d.z + billOffZ(halfW, sy) - camZ),
                (float) (d.x + billOffX(-halfW, bottom, cy, ct, st) - camX),
                (float) (d.y + billOffY(-halfW, bottom, cy, ct, st) - camY),
                (float) (d.z + billOffZ(-halfW, sy) - camZ),
                transparent, transparent, alphaColor, alphaColor);
        }
    }

    private static float clamp01(float v) {
        return Math.min(1.0f, Math.max(0.0f, v));
    }

    private static float smoothstep(float edge0, float edge1, float v) {
        float t = Math.min(1.0f, Math.max(0.0f, (v - edge0) / (edge1 - edge0)));
        return t * t * (3.0f - 2.0f * t);
    }

    static enum SnowPreset
    implements NamedOption {
        LIGHT_SNOW("modules.settings.winter.preset.light"),
        SNOWFALL("modules.settings.winter.preset.snowfall"),
        BLIZZARD("modules.settings.winter.preset.blizzard");
        private final String name;

        SnowPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    static enum SnowColorMode
    implements NamedOption {
        REALISTIC("modules.settings.winter.color_mode.realistic"),
        CLIENT("modules.settings.winter.color_mode.client"),
        CUSTOM("modules.settings.winter.color_mode.custom");
        private final String name;

        SnowColorMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private static final class Flake {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        double mass = 1.0;
        float size = 1.0f;
        double ground;
        float phase;
        double spin;
        double spinSpeed;
        double swayAmp = 1.0;
        double swayFreq = 1.4;
        float brightness = 1.0f;
        boolean dead;
    }

    private static final class Drift {
        double x;
        double y;
        double z;
        double ground;
        float life;
        float maxLife = 2.0f;
        float size = 1.0f;
        float seed;
    }
}
