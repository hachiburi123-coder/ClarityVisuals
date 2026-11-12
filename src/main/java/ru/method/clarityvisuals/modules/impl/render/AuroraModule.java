package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Vector3f;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.AuroraSkyRenderer;
import ru.method.clarityvisuals.utils.render.Render3D;

/**
 * Cinematic aurora sky with layered curtains, stars, a faint galaxy band,
 * moon halo and rare shooting stars.
 */
public class AuroraModule
extends Module {
    private final EnumSetting<AuroraPreset> preset = new EnumSetting<AuroraPreset>(
        "modules.settings.aurora.preset", AuroraPreset.BOREALIS);
    private final SliderSetting intensity = new SliderSetting("modules.settings.aurora.intensity", 1.0f, 0.1f, 2.0f, 0.05f);
    private final SliderSetting height = new SliderSetting("modules.settings.aurora.height", 0.5f, 0.0f, 1.0f, 0.01f);
    private final SliderSetting stars = new SliderSetting("modules.settings.aurora.stars", 0.68f, 0.0f, 1.0f, 0.01f);
    private final SliderSetting purple = new SliderSetting("modules.settings.aurora.purple", 0.55f, 0.0f, 1.0f, 0.01f);
    private final SliderSetting speed = new SliderSetting("modules.settings.aurora.speed", 1.0f, 0.0f, 2.5f, 0.05f);
    private final ColorSetting colorA = new ColorSetting("modules.settings.aurora.color_a", new Color(56, 242, 158), this::customPalette);
    private final ColorSetting colorB = new ColorSetting("modules.settings.aurora.color_b", new Color(146, 64, 255), this::customPalette);
    private final ColorSetting nightColor = new ColorSetting("modules.settings.aurora.night_color", new Color(5, 8, 22), this::customPalette);
    private final BooleanSetting moon = new BooleanSetting("modules.settings.aurora.moon", true);
    private final BooleanSetting milkyWay = new BooleanSetting("modules.settings.aurora.milky_way", true);
    private final BooleanSetting shootingStars = new BooleanSetting("modules.settings.aurora.shooting_stars", true);

    private final List<Meteor> meteors = new ArrayList<>();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float meteorTimer = 1.5f;

    public AuroraModule() {
        super("Aurora", ModuleCategory.ql);
        this.getSettings().add(this.preset);
        this.getSettings().add(this.intensity);
        this.getSettings().add(this.height);
        this.getSettings().add(this.stars);
        this.getSettings().add(this.purple);
        this.getSettings().add(this.speed);
        this.getSettings().add(this.colorA);
        this.getSettings().add(this.colorB);
        this.getSettings().add(this.nightColor);
        this.getSettings().add(this.moon);
        this.getSettings().add(this.milkyWay);
        this.getSettings().add(this.shootingStars);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        AuroraSkyRenderer.resetTime();
        this.lastFrameNanos = 0L;
        this.meteorTimer = 1.5f;
        this.meteors.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        AuroraSkyRenderer.resetTime();
        this.lastFrameNanos = 0L;
        this.meteors.clear();
    }

    private boolean customPalette() {
        return this.preset.jT() == AuroraPreset.CUSTOM;
    }

    private int colorA() {
        return switch (this.preset.jT()) {
            case POLAR_PINK -> 0xFF6AF7C8;
            case DEEP_SKY -> 0xFF4FC9FF;
            case CUSTOM -> ((Color) this.colorA.jT()).getRGB();
            default -> 0xFF38F29E;
        };
    }

    private int colorB() {
        return switch (this.preset.jT()) {
            case POLAR_PINK -> 0xFFFF5FA8;
            case DEEP_SKY -> 0xFF6C62FF;
            case CUSTOM -> ((Color) this.colorB.jT()).getRGB();
            default -> 0xFF9240FF;
        };
    }

    private int nightColor() {
        return switch (this.preset.jT()) {
            case POLAR_PINK -> 0xFF090617;
            case DEEP_SKY -> 0xFF030A1A;
            case CUSTOM -> ((Color) this.nightColor.jT()).getRGB();
            default -> 0xFF050816;
        };
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (AuroraModule.fullNullCheck()) {
            return;
        }
        class_4184 camera = AuroraModule.aiG.field_1773.method_19418();
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
        boolean hasMeteors = ((Boolean) this.shootingStars.jT()).booleanValue();
        if (hasMeteors) {
            this.updateMeteors(camera, dt);
        } else {
            this.meteors.clear();
        }

        float elapsed = AuroraSkyRenderer.updateTime(((Float) this.speed.jT()).floatValue());
        AuroraSkyRenderer.render(elapsed, this.colorA(), this.colorB(), this.nightColor(),
            ((Float) this.intensity.jT()).floatValue(),
            ((Float) this.stars.jT()).floatValue(),
            ((Float) this.height.jT()).floatValue(),
            ((Float) this.purple.jT()).floatValue(),
            ((Boolean) this.moon.jT()).booleanValue(),
            ((Boolean) this.milkyWay.jT()).booleanValue());
        if (!this.meteors.isEmpty()) {
            this.drawMeteors(stage.l(), camera);
        }
    }

    private void updateMeteors(class_4184 camera, float dt) {
        this.meteors.removeIf(meteor -> {
            meteor.life += dt;
            meteor.x += meteor.vx * dt;
            meteor.y += meteor.vy * dt;
            meteor.z += meteor.vz * dt;
            return meteor.life >= meteor.maxLife;
        });
        this.meteorTimer -= dt;
        if (this.meteorTimer <= 0.0f) {
            this.spawnMeteor(camera);
            this.meteorTimer = 2.2f + this.random.nextFloat() * 4.6f;
        }
    }

    private void spawnMeteor(class_4184 camera) {
        class_243 cam = camera.method_19326();
        float yaw = (float) Math.toRadians(camera.method_19330());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);
        double ahead = 34.0 + this.random.nextDouble() * 46.0;
        double side = (this.random.nextDouble() * 2.0 - 1.0) * 30.0;

        Meteor meteor = new Meteor();
        meteor.x = cam.field_1352 + forwardX * ahead + rightX * side;
        meteor.z = cam.field_1350 + forwardZ * ahead + rightZ * side;
        meteor.y = cam.field_1351 + 30.0 + this.random.nextDouble() * 34.0;
        double angle = yaw + Math.PI * 0.5 + (this.random.nextDouble() - 0.5) * 1.3;
        double speed = 15.0 + this.random.nextDouble() * 12.0;
        meteor.vx = Math.cos(angle) * speed;
        meteor.vz = Math.sin(angle) * speed;
        meteor.vy = -2.0 - this.random.nextDouble() * 3.0;
        meteor.length = 3.4f + this.random.nextFloat() * 3.4f;
        meteor.width = 0.055f + this.random.nextFloat() * 0.055f;
        meteor.life = 0.0f;
        meteor.maxLife = 0.9f + this.random.nextFloat() * 0.8f;
        this.meteors.add(meteor);
    }

    private void drawMeteors(class_4587 matrices, class_4184 camera) {
        class_243 cam = camera.method_19326();
        for (Meteor meteor : this.meteors) {
            float life = Math.min(1.0f, Math.max(0.0f, meteor.life / meteor.maxLife));
            float fade = (float) Math.sin(life * Math.PI);
            if (fade <= 0.01f) {
                continue;
            }
            double speed = Math.sqrt(meteor.vx * meteor.vx + meteor.vy * meteor.vy + meteor.vz * meteor.vz);
            if (speed < 0.001) {
                continue;
            }
            Vector3f direction = new Vector3f((float) meteor.vx, (float) meteor.vy, (float) meteor.vz).normalize();
            Vector3f toCamera = new Vector3f((float) (cam.field_1352 - meteor.x),
                (float) (cam.field_1351 - meteor.y), (float) (cam.field_1350 - meteor.z)).normalize();
            Vector3f side = new Vector3f(direction).cross(toCamera);
            if (side.lengthSquared() < 0.00001f) {
                side.set(camera.method_23767().transform(new Vector3f(1.0f, 0.0f, 0.0f)));
            }
            side.normalize().mul(meteor.width * 0.5f);

            float tailX = (float) (meteor.x - direction.x * meteor.length);
            float tailY = (float) (meteor.y - direction.y * meteor.length);
            float tailZ = (float) (meteor.z - direction.z * meteor.length);
            class_243 p0 = new class_243(meteor.x + side.x, meteor.y + side.y, meteor.z + side.z);
            class_243 p1 = new class_243(meteor.x - side.x, meteor.y - side.y, meteor.z - side.z);
            class_243 p2 = new class_243(tailX - side.x, tailY - side.y, tailZ - side.z);
            class_243 p3 = new class_243(tailX + side.x, tailY + side.y, tailZ + side.z);
            int headColor = argb(fade, 255, 242, 250);
            int tailColor = argb(0.0f, 255, 191, 222);
            Render3D.drawGradientQuad(matrices, p0, p1, p2, p3, headColor, headColor, tailColor, tailColor, true);

            float coreWidth = meteor.width * 0.34f;
            Vector3f core = new Vector3f(side).normalize().mul(coreWidth);
            class_243 c0 = new class_243(meteor.x + core.x, meteor.y + core.y, meteor.z + core.z);
            class_243 c1 = new class_243(meteor.x - core.x, meteor.y - core.y, meteor.z - core.z);
            class_243 c2 = new class_243(meteor.x - direction.x * meteor.length * 0.44f - core.x,
                meteor.y - direction.y * meteor.length * 0.44f - core.y,
                meteor.z - direction.z * meteor.length * 0.44f - core.z);
            class_243 c3 = new class_243(meteor.x - direction.x * meteor.length * 0.44f + core.x,
                meteor.y - direction.y * meteor.length * 0.44f + core.y,
                meteor.z - direction.z * meteor.length * 0.44f + core.z);
            int coreColor = argb(fade * 0.8f, 255, 255, 255);
            Render3D.drawGradientQuad(matrices, c0, c1, c2, c3, coreColor, coreColor, argb(0.0f, 255, 240, 250), argb(0.0f, 255, 240, 250), true);
        }
    }

    private static int argb(float alpha, int red, int green, int blue) {
        int a = (int) (Math.min(1.0f, Math.max(0.0f, alpha)) * 255.0f);
        return a << 24 | red << 16 | green << 8 | blue;
    }

    private static final class Meteor {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        float life;
        float maxLife;
        float length;
        float width;
    }

    static enum AuroraPreset
    implements NamedOption {
        BOREALIS("modules.settings.aurora.preset.borealis"),
        POLAR_PINK("modules.settings.aurora.preset.polar_pink"),
        DEEP_SKY("modules.settings.aurora.preset.deep_sky"),
        CUSTOM("modules.settings.aurora.preset.custom");

        private final String name;

        AuroraPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
