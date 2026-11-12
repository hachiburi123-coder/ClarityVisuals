package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2902;
import net.minecraft.class_3532;
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
import ru.method.clarityvisuals.utils.render.RainWetSurface;
import ru.method.clarityvisuals.utils.render.Render3D;

public class RainModule
extends Module {
    private final EnumSetting<RainPreset> preset = new EnumSetting<RainPreset>("modules.settings.rain.preset", RainPreset.RAIN);
    private final SliderSetting density = new SliderSetting("modules.settings.rain.density", 1.0f, 0.1f, 3.0f, 0.05f);
    private final SliderSetting radius = new SliderSetting("modules.settings.rain.radius", 26.0f, 8.0f, 64.0f, 1.0f);
    private final SliderSetting altitude = new SliderSetting("modules.settings.rain.altitude", 16.0f, 6.0f, 40.0f, 1.0f);
    private final SliderSetting dropSize = new SliderSetting("modules.settings.rain.drop_size", 1.0f, 0.3f, 2.5f, 0.05f);
    private final SliderSetting fallSpeed = new SliderSetting("modules.settings.rain.fall_speed", 1.0f, 0.3f, 2.5f, 0.05f);
    private final SliderSetting wind = new SliderSetting("modules.settings.rain.wind", 1.0f, 0.0f, 3.0f, 0.05f);
    private final SliderSetting opacity = new SliderSetting("modules.settings.rain.opacity", 1.0f, 0.1f, 2.0f, 0.05f);
    private final BooleanSetting splashes = new BooleanSetting("modules.settings.rain.splashes", true);
    private final BooleanSetting droplets = new BooleanSetting("modules.settings.rain.droplets", true, this.splashes::jT);
    private final BooleanSetting groundMist = new BooleanSetting("modules.settings.rain.mist", true);
    private final BooleanSetting lightning = new BooleanSetting("modules.settings.rain.lightning", false);
    private final BooleanSetting skyCheck = new BooleanSetting("modules.settings.rain.sky_check", true);
    private final BooleanSetting onlyWhenRaining = new BooleanSetting("modules.settings.rain.only_raining", false);
    private final EnumSetting<RainColorMode> colorMode = new EnumSetting<RainColorMode>("modules.settings.rain.color_mode", RainColorMode.REALISTIC);
    private final ColorSetting customColor = new ColorSetting("modules.settings.rain.color", new Color(191, 216, 230, 255), () -> this.colorMode.jT() == RainColorMode.CUSTOM);
    private final BooleanSetting wetGround = new BooleanSetting("modules.settings.rain.wet_ground", true);
    private final SliderSetting wetness = new SliderSetting("modules.settings.rain.wetness", 1.25f, 0.1f, 2.0f, 0.05f, this.wetGround::jT);
    private final SliderSetting puddleCoverage = new SliderSetting("modules.settings.rain.puddles", 0.90f, 0.05f, 1.0f, 0.05f, this.wetGround::jT);
    private final SliderSetting reflectionStrength = new SliderSetting("modules.settings.rain.reflections", 1.55f, 0.1f, 2.0f, 0.05f, this.wetGround::jT);
    private final SliderSetting reflectionDistance = new SliderSetting("modules.settings.rain.reflection_distance", 56.0f, 12.0f, 96.0f, 1.0f, this.wetGround::jT);
    private final EnumSetting<ReflectionQuality> reflectionQuality = new EnumSetting<ReflectionQuality>("modules.settings.rain.reflection_quality", ReflectionQuality.BALANCED, this.wetGround::jT);
    private final BooleanSetting puddleRipples = new BooleanSetting("modules.settings.rain.ripples", true, this.wetGround::jT);
    private final SliderSetting rippleStrength = new SliderSetting("modules.settings.rain.ripple_strength", 0.8f, 0.0f, 1.6f, 0.05f, () -> ((Boolean) this.wetGround.jT()).booleanValue() && ((Boolean) this.puddleRipples.jT()).booleanValue());

    private static final double GRAVITY = 22.0;
    private static final double TERMINAL_VELOCITY = 34.0;
    private static final int RIPPLE_SEGMENTS = 14;
    private static final int MAX_DROPS = 4200;
    /**
     * Предвычисленные углы сегментов круга: Math.PI * 2.0 / RIPPLE_SEGMENTS * i —
     * те же значения, что в прежнем теле цикла, но без Math.cos/sin на сегмент.
     */
    private static final double[] RIPPLE_COS = new double[RIPPLE_SEGMENTS + 1];
    private static final double[] RIPPLE_SIN = new double[RIPPLE_SEGMENTS + 1];

    static {
        for (int i = 0; i <= RIPPLE_SEGMENTS; i++) {
            double angle = Math.PI * 2.0 / RIPPLE_SEGMENTS * i;
            RIPPLE_COS[i] = Math.cos(angle);
            RIPPLE_SIN[i] = Math.sin(angle);
        }
    }
    private static final int MAX_MIST = 90;
    private final List<Drop> drops = new ArrayList<>();
    private final List<Splash> splashList = new ArrayList<>();
    private final List<Droplet> dropletList = new ArrayList<>();
    private final List<Mist> mistList = new ArrayList<>();
    private final GroundHeightCache groundCache = new GroundHeightCache();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float clock;
    private float flash;
    private float nextFlashIn = 9.0f;
    private int cacheTicks;

    public RainModule() {
        super("Rain", ModuleCategory.ql);
        this.getSettings().add(this.preset);
        this.getSettings().add(this.density);
        this.getSettings().add(this.radius);
        this.getSettings().add(this.altitude);
        this.getSettings().add(this.dropSize);
        this.getSettings().add(this.fallSpeed);
        this.getSettings().add(this.wind);
        this.getSettings().add(this.opacity);
        this.getSettings().add(this.splashes);
        this.getSettings().add(this.droplets);
        this.getSettings().add(this.groundMist);
        this.getSettings().add(this.lightning);
        this.getSettings().add(this.skyCheck);
        this.getSettings().add(this.onlyWhenRaining);
        this.getSettings().add(this.colorMode);
        this.getSettings().add(this.customColor);
        this.getSettings().add(this.wetGround);
        this.getSettings().add(this.wetness);
        this.getSettings().add(this.puddleCoverage);
        this.getSettings().add(this.reflectionStrength);
        this.getSettings().add(this.reflectionDistance);
        this.getSettings().add(this.reflectionQuality);
        this.getSettings().add(this.puddleRipples);
        this.getSettings().add(this.rippleStrength);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastFrameNanos = 0L;
        RainWetSurface.getInstance().reset();
        this.clearAll();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        RainWetSurface.getInstance().reset();
        this.clearAll();
    }

    private void clearAll() {
        this.drops.clear();
        this.splashList.clear();
        this.dropletList.clear();
        this.mistList.clear();
        this.groundCache.clear();
        this.flash = 0.0f;
    }

    @EventHandler
    private void onTick(ClientTickEvent event) {
        if (RainModule.fullNullCheck()) {
            this.clearAll();
            return;
        }
    }

    private float presetDensity() {
        if (this.preset.jT() == RainPreset.DRIZZLE) {
            return 0.45f;
        }
        if (this.preset.jT() == RainPreset.DOWNPOUR) {
            return 1.9f;
        }
        if (this.preset.jT() == RainPreset.STORM) {
            return 2.8f;
        }
        return 1.0f;
    }

    private float presetWind() {
        if (this.preset.jT() == RainPreset.DRIZZLE) {
            return 0.5f;
        }
        if (this.preset.jT() == RainPreset.DOWNPOUR) {
            return 1.35f;
        }
        if (this.preset.jT() == RainPreset.STORM) {
            return 2.4f;
        }
        return 1.0f;
    }

    private float presetMass() {
        if (this.preset.jT() == RainPreset.DRIZZLE) {
            return 0.55f;
        }
        if (this.preset.jT() == RainPreset.DOWNPOUR) {
            return 1.25f;
        }
        if (this.preset.jT() == RainPreset.STORM) {
            return 1.5f;
        }
        return 1.0f;
    }

    private int targetDropCount() {
        float r = ((Float) this.radius.jT()).floatValue();
        float area = r * r * 0.0155f;
        int count = (int) (area * ((Float) this.density.jT()).floatValue() * this.presetDensity() * 26.0f);
        return Math.min(MAX_DROPS, Math.max(40, count));
    }

    private float[] resolveColor() {
        int color;
        if (this.colorMode.jT() == RainColorMode.CUSTOM) {
            color = ((Color) this.customColor.jT()).getRGB();
        } else if (this.colorMode.jT() == RainColorMode.CLIENT) {
            color = ColorUtils.getGlobalColor().getRGB();
        } else {
            color = 0xFFC8DCE8;
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
            top = RainModule.aiG.field_1687.method_8624(class_2902.class_2903.field_13197, x, z);
        } catch (Throwable ignored) {
            top = RainModule.aiG.field_1687.method_31607();
        }
        this.groundCache.put(key, top);
        return top;
    }

    private boolean isBlocking(double x, double y, double z) {
        if (RainModule.aiG.field_1687 == null) {
            return false;
        }
        class_2338 pos = new class_2338((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        try {
            if (RainModule.aiG.field_1687.method_8320(pos).method_26215()) {
                return false;
            }
            return RainModule.aiG.field_1687.method_8320(pos).method_51366();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean skyVisible(double x, double y, double z) {
        if (!((Boolean) this.skyCheck.jT()).booleanValue() || RainModule.aiG.field_1687 == null) {
            return true;
        }
        return this.surfaceY((int) Math.floor(x), (int) Math.floor(z)) <= (int) Math.floor(y) + 1;
    }

    private void spawnDrop(class_243 center, boolean fromTop) {
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
        Drop drop = new Drop();
        drop.x = x;
        drop.y = y;
        drop.z = z;
        drop.mass = this.random(0.6f, 1.4f) * this.presetMass();
        drop.vy = -this.random(6.0f, 11.0f) * drop.mass;
        drop.phase = this.random(0.0f, 62.8f);
        drop.brightness = this.random(0.55f, 1.0f);
        drop.ground = this.surfaceY((int) Math.floor(x), (int) Math.floor(z));
        this.drops.add(drop);
    }

    private void updateDrops(class_243 center, float dt) {
        int target = this.targetDropCount();
        float r = ((Float) this.radius.jT()).floatValue();
        float alt = ((Float) this.altitude.jT()).floatValue();
        this.drops.removeIf(d -> {
            double dx = d.x - center.field_1352;
            double dz = d.z - center.field_1350;
            float rr = r + 6.0f;
            return d.dead || dx * dx + dz * dz > rr * rr || d.y < center.field_1351 - alt - 12.0;
        });
        boolean fill = this.drops.size() < target / 2;
        int spawnBudget = fill ? Math.min(target - this.drops.size(), 900) : Math.min(target - this.drops.size(), 260);
        for (int i = 0; i < spawnBudget; i++) {
            this.spawnDrop(center, !fill);
        }
        float speedMul = ((Float) this.fallSpeed.jT()).floatValue();
        float windMul = ((Float) this.wind.jT()).floatValue() * this.presetWind();
        for (int i = 0, size = this.drops.size(); i < size; i++) {
            Drop d = this.drops.get(i);
            double gust = 0.65 + 0.35 * Math.sin(this.clock * 0.55 + d.phase * 0.15) + 0.18 * Math.sin(this.clock * 1.7 + d.phase);
            double windX = windMul * 3.1 * gust;
            double windZ = windMul * 1.7 * Math.sin(this.clock * 0.31 + d.phase * 0.05) * gust;
            double drag = 1.0 - Math.min(0.92, Math.abs(d.vy) / (TERMINAL_VELOCITY * d.mass));
            d.vy -= GRAVITY * d.mass * drag * dt;
            if (d.vy < -TERMINAL_VELOCITY * d.mass) {
                d.vy = -TERMINAL_VELOCITY * d.mass;
            }
            d.vx += (windX - d.vx) * Math.min(1.0, dt * 3.2);
            d.vz += (windZ - d.vz) * Math.min(1.0, dt * 3.2);
            double nx = d.x + d.vx * dt * speedMul;
            double ny = d.y + d.vy * dt * speedMul;
            double nz = d.z + d.vz * dt * speedMul;
            boolean hit = false;
            if (ny <= d.ground + 2.5) {
                if (this.isBlocking(nx, ny, nz)) {
                    hit = true;
                } else if (ny <= d.ground - 3.0) {
                    d.dead = true;
                }
            }
            if (hit) {
                if (((Boolean) this.splashes.jT()).booleanValue()) {
                    this.addSplash(nx, ny, nz, d);
                }
                d.dead = true;
                continue;
            }
            d.x = nx;
            d.y = ny;
            d.z = nz;
        }
    }

    private void addSplash(double x, double y, double z, Drop d) {
        if (this.splashList.size() > 700) {
            return;
        }
        double impactY = Math.floor(y) + 1.0005;
        Splash splash = new Splash();
        splash.x = x;
        splash.y = impactY;
        splash.z = z;
        splash.life = 0.0f;
        splash.maxLife = this.random(0.30f, 0.52f);
        splash.scale = (float) (0.32 + 0.22 * d.mass);
        this.splashList.add(splash);
        if (!((Boolean) this.droplets.jT()).booleanValue() || this.dropletList.size() > 1400) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double sp = this.random(1.1f, 2.6f) * d.mass;
            Droplet drop = new Droplet();
            drop.x = x;
            drop.y = impactY + 0.02;
            drop.z = z;
            drop.vx = Math.cos(a) * sp * 0.45 + d.vx * 0.08;
            drop.vz = Math.sin(a) * sp * 0.45 + d.vz * 0.08;
            drop.vy = this.random(1.9f, 4.1f);
            drop.maxLife = this.random(0.28f, 0.5f);
            this.dropletList.add(drop);
        }
    }

    private void updateDroplets(float dt) {
        for (int i = this.dropletList.size() - 1; i >= 0; i--) {
            Droplet dr = this.dropletList.get(i);
            dr.life += dt;
            if (dr.life >= dr.maxLife) {
                this.dropletList.remove(i);
                continue;
            }
            dr.vy -= GRAVITY * 0.55 * dt;
            dr.x += dr.vx * dt;
            dr.y += dr.vy * dt;
            dr.z += dr.vz * dt;
        }
    }

    private void updateSplashes(float dt) {
        for (int i = this.splashList.size() - 1; i >= 0; i--) {
            Splash s = this.splashList.get(i);
            s.life += dt;
            if (s.life >= s.maxLife) {
                this.splashList.remove(i);
            }
        }
    }

    private void updateMist(class_243 center, float dt) {
        if (!((Boolean) this.groundMist.jT()).booleanValue()) {
            this.mistList.clear();
            return;
        }
        float r = ((Float) this.radius.jT()).floatValue();
        int target = (int) Math.min(MAX_MIST, Math.max(6, 22.0f * ((Float) this.density.jT()).floatValue() * this.presetDensity()));
        this.mistList.removeIf(m -> {
            m.life += dt;
            double dx = m.x - center.field_1352;
            double dz = m.z - center.field_1350;
            m.x += m.vx * dt;
            m.z += m.vz * dt;
            return m.life > m.maxLife || dx * dx + dz * dz > r * r;
        });
        while (this.mistList.size() < target) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(this.random.nextDouble()) * r * 0.85;
            double x = center.field_1352 + Math.cos(angle) * dist;
            double z = center.field_1350 + Math.sin(angle) * dist;
            int ground = this.surfaceY((int) Math.floor(x), (int) Math.floor(z));
            Mist m = new Mist();
            m.x = x;
            m.z = z;
            m.y = ground + this.random(0.05f, 0.9f);
            m.size = this.random(2.2f, 5.6f);
            m.maxLife = this.random(2.4f, 5.5f);
            m.life = this.random(0.0f, 1.0f);
            m.vx = this.random(-0.35f, 0.35f) * ((Float) this.wind.jT()).floatValue();
            m.vz = this.random(-0.35f, 0.35f) * ((Float) this.wind.jT()).floatValue();
            this.mistList.add(m);
        }
    }

    private void updateLightning(float dt) {
        if (!((Boolean) this.lightning.jT()).booleanValue()) {
            this.flash = 0.0f;
            return;
        }
        this.flash = Math.max(0.0f, this.flash - dt * 3.4f);
        this.nextFlashIn -= dt;
        if (this.nextFlashIn <= 0.0f) {
            this.flash = this.random(0.55f, 1.0f);
            this.nextFlashIn = this.random(6.0f, 16.0f);
        }
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (RainModule.fullNullCheck()) {
            return;
        }
        if (((Boolean) this.onlyWhenRaining.jT()).booleanValue() && !RainModule.aiG.field_1687.method_8419()) {
            this.clearAll();
            return;
        }
        class_4184 camera = RainModule.aiG.field_1773.method_19418();
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
        float camPitch = camera.method_19329();
        this.updateDrops(cam, dt);
        this.updateSplashes(dt);
        this.updateDroplets(dt);
        this.updateMist(cam, dt);
        this.updateLightning(dt);
        if (((Boolean) this.wetGround.jT()).booleanValue()) {
            this.renderWetSurface(stage, camera, cam, camYaw, camPitch);
        }
        if (this.drops.isEmpty() && this.splashList.isEmpty() && this.dropletList.isEmpty() && this.mistList.isEmpty()) {
            return;
        }
        float[] rgb = this.resolveColor();
        float brightnessMul = Math.min(1.65f, Math.max(0.0f, 0.9f + this.flash * 0.9f));
        float alphaMul = ((Float) this.opacity.jT()).floatValue() * brightnessMul;
        class_4587 ms = stage.l();
        if (((Boolean) this.groundMist.jT()).booleanValue()) {
            this.drawMist(ms, cam, camYaw, rgb, alphaMul);
        }
        this.drawStreaks(ms, cam, camYaw, rgb, alphaMul, false);
        if (((Boolean) this.splashes.jT()).booleanValue()) {
            this.drawRipples(ms, cam, rgb, alphaMul);
        }
        this.drawStreaks(ms, cam, camYaw, rgb, alphaMul, true);
        if (((Boolean) this.splashes.jT()).booleanValue()) {
            this.drawCrowns(ms, cam, camYaw, rgb, alphaMul);
            if (((Boolean) this.droplets.jT()).booleanValue()) {
                this.drawDroplets(ms, cam, camYaw, rgb, alphaMul);
            }
        }
    }

    private void renderWetSurface(WorldRenderStages.MatrixStackStage stage, class_4184 camera, class_243 cam, float yaw, float pitch) {
        try {
            // Матрицы напрямую из камеры, как в референсе: поворот из кватерниона
            // камеры и живая проекция. Никаких пересборок по yaw/pitch и захваченных
            // матриц — там фрейм расходился с реальным при изменении питча.
            org.joml.Matrix4f viewRot = new org.joml.Matrix4f().rotation(camera.method_23767());
            org.joml.Matrix4f viewMatrix = new org.joml.Matrix4f(viewRot).invert();
            org.joml.Matrix4f projectionMatrix = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix();
            RainWetSurface.Parameters parameters = new RainWetSurface.Parameters();
            parameters.wetness = ((Float) this.wetness.jT()).floatValue();
            parameters.puddleCoverage = ((Float) this.puddleCoverage.jT()).floatValue();
            parameters.reflectionStrength = ((Float) this.reflectionStrength.jT()).floatValue();
            parameters.maxDistance = ((Float) this.reflectionDistance.jT()).floatValue();
            parameters.rippleStrength = ((Float) this.rippleStrength.jT()).floatValue();
            parameters.rainAmount = Math.min(1.45f, Math.max(0.55f, 0.55f + ((Float) this.density.jT()).floatValue() * this.presetDensity() * 0.28f));
            parameters.ripples = ((Boolean) this.puddleRipples.jT()).booleanValue();
            parameters.reflectionSteps = this.reflectionQuality.jT() == ReflectionQuality.HIGH ? 16
                : this.reflectionQuality.jT() == ReflectionQuality.PERFORMANCE ? 6 : 10;
            RainWetSurface.getInstance().apply(camera, viewMatrix, projectionMatrix, parameters);
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

    private static int cInt(float r, float g, float b, float a) {
        int ai = (int) (clamp01(a) * 255.0f + 0.5f);
        int ri = (int) (clamp01(r) * 255.0f + 0.5f);
        int gi = (int) (clamp01(g) * 255.0f + 0.5f);
        int bi = (int) (clamp01(b) * 255.0f + 0.5f);
        return ai << 24 | ri << 16 | gi << 8 | bi;
    }

    private void drawStreaks(class_4587 ms, class_243 cam, float camYawDeg, float[] rgb, float alphaMul, boolean highlight) {
        if (this.drops.isEmpty()) {
            return;
        }
        float sizeMul = ((Float) this.dropSize.jT()).floatValue();
        float r = ((Float) this.radius.jT()).floatValue();
        float fadeStart = r * 0.62f;
        double yawRad = (float) Math.toRadians(camYawDeg);
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        for (int i = 0, size = this.drops.size(); i < size; i++) {
            Drop d = this.drops.get(i);
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
            float nearFade = dist < 0.7 ? (float) (dist / 0.7) : 1.0f;
            double speed = Math.sqrt(d.vx * d.vx + d.vy * d.vy + d.vz * d.vz);
            if (speed < 0.0001) {
                continue;
            }
            float length = Math.min(2.6f, Math.max(0.16f, (float) (speed * 0.055 * sizeMul)));
            float width = (float) (0.014 + 0.019 * d.mass) * sizeMul * (highlight ? 0.45f : 1.0f);
            float alpha = Math.min(0.95f, alphaMul * d.brightness * distanceFade * nearFade * (highlight ? 0.30f : 0.42f));
            if (alpha <= 0.006f) {
                continue;
            }
            double rightX = Math.cos(yawRad);
            double rightZ = Math.sin(yawRad);
            double horizontal = d.vx * rightX + d.vz * rightZ;
            float tilt = (float) Math.toDegrees(Math.atan2(horizontal, Math.abs(d.vy) + 0.0001));
            tilt = Math.min(55.0f, Math.max(-55.0f, tilt));
            double tiltRad = (float) Math.toRadians(tilt);
            double ct = Math.cos(-tiltRad);
            double st = Math.sin(-tiltRad);
            float half = width * 0.5f;
            float top = length * 0.5f;
            float bottom = -length * 0.5f;
            if (!highlight) {
                int transparent = cInt(rgb, 0.0f);
                int alphaColor = cInt(rgb, alpha);
                Render3D.drawGradientQuadRel(ms, false,
                    (float) (d.x + billOffX(-half * 0.35f, top, cy, ct, st) - camX),
                    (float) (d.y + billOffY(-half * 0.35f, top, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(-half * 0.35f, sy) - camZ),
                    (float) (d.x + billOffX(half * 0.35f, top, cy, ct, st) - camX),
                    (float) (d.y + billOffY(half * 0.35f, top, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(half * 0.35f, sy) - camZ),
                    (float) (d.x + billOffX(half, bottom, cy, ct, st) - camX),
                    (float) (d.y + billOffY(half, bottom, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(half, sy) - camZ),
                    (float) (d.x + billOffX(-half, bottom, cy, ct, st) - camX),
                    (float) (d.y + billOffY(-half, bottom, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(-half, sy) - camZ),
                    transparent, transparent, alphaColor, alphaColor);
                float halo = half * 2.6f;
                float haloAlpha = alpha * 0.22f;
                int haloColor = cInt(rgb, haloAlpha);
                Render3D.drawGradientQuadRel(ms, false,
                    (float) (d.x + billOffX(-halo * 0.5f, top, cy, ct, st) - camX),
                    (float) (d.y + billOffY(-halo * 0.5f, top, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(-halo * 0.5f, sy) - camZ),
                    (float) (d.x + billOffX(halo * 0.5f, top, cy, ct, st) - camX),
                    (float) (d.y + billOffY(halo * 0.5f, top, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(halo * 0.5f, sy) - camZ),
                    (float) (d.x + billOffX(halo, bottom, cy, ct, st) - camX),
                    (float) (d.y + billOffY(halo, bottom, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(halo, sy) - camZ),
                    (float) (d.x + billOffX(-halo, bottom, cy, ct, st) - camX),
                    (float) (d.y + billOffY(-halo, bottom, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(-halo, sy) - camZ),
                    transparent, transparent, haloColor, haloColor);
            } else {
                float lensTop = bottom + length * 0.34f;
                float lensWidth = half * 1.25f;
                int transparent = cInt(1.0f, 1.0f, 1.0f, 0.0f);
                int alphaColor = cInt(1.0f, 1.0f, 1.0f, alpha * 0.85f);
                Render3D.drawGradientQuadRel(ms, false,
                    (float) (d.x + billOffX(-lensWidth, lensTop, cy, ct, st) - camX),
                    (float) (d.y + billOffY(-lensWidth, lensTop, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(-lensWidth, sy) - camZ),
                    (float) (d.x + billOffX(lensWidth, lensTop, cy, ct, st) - camX),
                    (float) (d.y + billOffY(lensWidth, lensTop, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(lensWidth, sy) - camZ),
                    (float) (d.x + billOffX(lensWidth, bottom, cy, ct, st) - camX),
                    (float) (d.y + billOffY(lensWidth, bottom, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(lensWidth, sy) - camZ),
                    (float) (d.x + billOffX(-lensWidth, bottom, cy, ct, st) - camX),
                    (float) (d.y + billOffY(-lensWidth, bottom, cy, ct, st) - camY),
                    (float) (d.z + billOffZ(-lensWidth, sy) - camZ),
                    transparent, transparent, alphaColor, alphaColor);
            }
        }
    }

    private void drawRipples(class_4587 ms, class_243 cam, float[] rgb, float alphaMul) {
        if (this.splashList.isEmpty()) {
            return;
        }
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        for (int i = 0, size = this.splashList.size(); i < size; i++) {
            Splash s = this.splashList.get(i);
            float progress = Math.min(1.0f, Math.max(0.0f, s.life / s.maxLife));
            float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
            float outer = s.scale * (0.18f + eased * 0.85f);
            float inner = outer * (0.55f + 0.35f * eased);
            float alpha = alphaMul * (1.0f - progress) * 0.38f;
            if (alpha <= 0.01f) {
                continue;
            }
            int in = cInt(rgb, alpha);
            int out = cInt(rgb, 0.0f);
            for (int seg = 0; seg < RIPPLE_SEGMENTS; seg++) {
                double cos0 = RIPPLE_COS[seg];
                double sin0 = RIPPLE_SIN[seg];
                double cos1 = RIPPLE_COS[seg + 1];
                double sin1 = RIPPLE_SIN[seg + 1];
                Render3D.drawGradientQuadRel(ms, false,
                    (float) (s.x + cos0 * inner - camX),
                    (float) (s.y - camY),
                    (float) (s.z + sin0 * inner - camZ),
                    (float) (s.x + cos1 * inner - camX),
                    (float) (s.y - camY),
                    (float) (s.z + sin1 * inner - camZ),
                    (float) (s.x + cos1 * outer - camX),
                    (float) (s.y - camY),
                    (float) (s.z + sin1 * outer - camZ),
                    (float) (s.x + cos0 * outer - camX),
                    (float) (s.y - camY),
                    (float) (s.z + sin0 * outer - camZ),
                    in, in, out, out);
            }
        }
    }

    private void drawCrowns(class_4587 ms, class_243 cam, float camYawDeg, float[] rgb, float alphaMul) {
        if (this.splashList.isEmpty()) {
            return;
        }
        double yawRad = (float) Math.toRadians(camYawDeg);
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double ct = Math.cos(-0.0);
        double st = Math.sin(-0.0);
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        for (int i = 0, size = this.splashList.size(); i < size; i++) {
            Splash s = this.splashList.get(i);
            float progress = Math.min(1.0f, Math.max(0.0f, s.life / s.maxLife));
            if (progress > 0.55f) {
                continue;
            }
            float local = progress / 0.55f;
            float height = s.scale * (0.55f * (float) Math.sin(local * Math.PI));
            float alpha = alphaMul * (1.0f - local) * 0.32f;
            if (alpha <= 0.01f || height <= 0.005f) {
                continue;
            }
            float base = s.scale * 0.16f;
            int transparent = cInt(rgb, 0.0f);
            int alphaColor = cInt(rgb, alpha);
            Render3D.drawGradientQuadRel(ms, false,
                (float) (s.x + billOffX(-base * 0.25f, height, cy, ct, st) - camX),
                (float) (s.y + billOffY(-base * 0.25f, height, cy, ct, st) - camY),
                (float) (s.z + billOffZ(-base * 0.25f, sy) - camZ),
                (float) (s.x + billOffX(base * 0.25f, height, cy, ct, st) - camX),
                (float) (s.y + billOffY(base * 0.25f, height, cy, ct, st) - camY),
                (float) (s.z + billOffZ(base * 0.25f, sy) - camZ),
                (float) (s.x + billOffX(base, 0.0f, cy, ct, st) - camX),
                (float) (s.y + billOffY(base, 0.0f, cy, ct, st) - camY),
                (float) (s.z + billOffZ(base, sy) - camZ),
                (float) (s.x + billOffX(-base, 0.0f, cy, ct, st) - camX),
                (float) (s.y + billOffY(-base, 0.0f, cy, ct, st) - camY),
                (float) (s.z + billOffZ(-base, sy) - camZ),
                transparent, transparent, alphaColor, alphaColor);
        }
    }

    private void drawDroplets(class_4587 ms, class_243 cam, float camYawDeg, float[] rgb, float alphaMul) {
        if (this.dropletList.isEmpty()) {
            return;
        }
        float sizeMul = ((Float) this.dropSize.jT()).floatValue();
        double yawRad = (float) Math.toRadians(camYawDeg);
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        for (int i = 0, size = this.dropletList.size(); i < size; i++) {
            Droplet d = this.dropletList.get(i);
            float progress = Math.min(1.0f, Math.max(0.0f, d.life / d.maxLife));
            float alpha = alphaMul * (1.0f - progress) * 0.5f;
            if (alpha <= 0.01f) {
                continue;
            }
            double speed = Math.sqrt(d.vx * d.vx + d.vy * d.vy + d.vz * d.vz);
            float length = Math.min(0.4f, Math.max(0.04f, (float) (speed * 0.03 * sizeMul)));
            float width = 0.016f * sizeMul;
            double horizontal = d.vx * Math.cos(yawRad) + d.vz * Math.sin(yawRad);
            float tilt = (float) Math.toDegrees(Math.atan2(horizontal, -d.vy + 0.0001));
            double tiltRad = (float) Math.toRadians(tilt);
            double ct = Math.cos(-tiltRad);
            double st = Math.sin(-tiltRad);
            int transparent = cInt(rgb, 0.0f);
            int alphaColor = cInt(rgb, alpha);
            Render3D.drawGradientQuadRel(ms, false,
                (float) (d.x + billOffX(-width * 0.4f, length * 0.5f, cy, ct, st) - camX),
                (float) (d.y + billOffY(-width * 0.4f, length * 0.5f, cy, ct, st) - camY),
                (float) (d.z + billOffZ(-width * 0.4f, sy) - camZ),
                (float) (d.x + billOffX(width * 0.4f, length * 0.5f, cy, ct, st) - camX),
                (float) (d.y + billOffY(width * 0.4f, length * 0.5f, cy, ct, st) - camY),
                (float) (d.z + billOffZ(width * 0.4f, sy) - camZ),
                (float) (d.x + billOffX(width, -length * 0.5f, cy, ct, st) - camX),
                (float) (d.y + billOffY(width, -length * 0.5f, cy, ct, st) - camY),
                (float) (d.z + billOffZ(width, sy) - camZ),
                (float) (d.x + billOffX(-width, -length * 0.5f, cy, ct, st) - camX),
                (float) (d.y + billOffY(-width, -length * 0.5f, cy, ct, st) - camY),
                (float) (d.z + billOffZ(-width, sy) - camZ),
                transparent, transparent, alphaColor, alphaColor);
        }
    }

    private void drawMist(class_4587 ms, class_243 cam, float camYawDeg, float[] rgb, float alphaMul) {
        if (this.mistList.isEmpty()) {
            return;
        }
        double yawRad = (float) Math.toRadians(camYawDeg);
        double cy = Math.cos(-yawRad);
        double sy = Math.sin(-yawRad);
        double ct = Math.cos(-0.0);
        double st = Math.sin(-0.0);
        double camX = cam.field_1352;
        double camY = cam.field_1351;
        double camZ = cam.field_1350;
        for (int i = 0, size = this.mistList.size(); i < size; i++) {
            Mist mist = this.mistList.get(i);
            float progress = Math.min(1.0f, Math.max(0.0f, mist.life / mist.maxLife));
            float fade = (float) Math.sin(progress * Math.PI);
            float alpha = alphaMul * fade * 0.05f;
            if (alpha <= 0.004f) {
                continue;
            }
            float half = mist.size * 0.5f;
            float top = mist.size * 0.42f;
            int transparent = cInt(rgb, 0.0f);
            int alphaColor = cInt(rgb, alpha);
            Render3D.drawGradientQuadRel(ms, false,
                (float) (mist.x + billOffX(-half, top, cy, ct, st) - camX),
                (float) (mist.y + billOffY(-half, top, cy, ct, st) - camY),
                (float) (mist.z + billOffZ(-half, sy) - camZ),
                (float) (mist.x + billOffX(half, top, cy, ct, st) - camX),
                (float) (mist.y + billOffY(half, top, cy, ct, st) - camY),
                (float) (mist.z + billOffZ(half, sy) - camZ),
                (float) (mist.x + billOffX(half, 0.0f, cy, ct, st) - camX),
                (float) (mist.y + billOffY(half, 0.0f, cy, ct, st) - camY),
                (float) (mist.z + billOffZ(half, sy) - camZ),
                (float) (mist.x + billOffX(-half, 0.0f, cy, ct, st) - camX),
                (float) (mist.y + billOffY(-half, 0.0f, cy, ct, st) - camY),
                (float) (mist.z + billOffZ(-half, sy) - camZ),
                transparent, transparent, alphaColor, alphaColor);
        }
    }

    private static float clamp01(float v) {
        return Math.min(1.0f, Math.max(0.0f, v));
    }

    static enum RainPreset
    implements NamedOption {
        DRIZZLE("modules.settings.rain.preset.drizzle"),
        RAIN("modules.settings.rain.preset.rain"),
        DOWNPOUR("modules.settings.rain.preset.downpour"),
        STORM("modules.settings.rain.preset.storm");
        private final String name;

        RainPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    static enum RainColorMode
    implements NamedOption {
        REALISTIC("modules.settings.rain.color_mode.realistic"),
        CLIENT("modules.settings.rain.color_mode.client"),
        CUSTOM("modules.settings.rain.color_mode.custom");
        private final String name;

        RainColorMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    static enum ReflectionQuality
    implements NamedOption {
        PERFORMANCE("modules.settings.rain.quality.performance"),
        BALANCED("modules.settings.rain.quality.balanced"),
        HIGH("modules.settings.rain.quality.high");
        private final String name;

        ReflectionQuality(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private static final class Drop {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        double mass = 1.0;
        double ground;
        float phase;
        float brightness = 1.0f;
        boolean dead;
    }

    private static final class Droplet {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        float life;
        float maxLife = 0.4f;
    }

    private static final class Splash {
        double x;
        double y;
        double z;
        float life;
        float maxLife = 0.4f;
        float scale = 0.4f;
    }

    private static final class Mist {
        double x;
        double y;
        double z;
        double vx;
        double vz;
        float size = 3.0f;
        float life;
        float maxLife = 4.0f;
    }
}
