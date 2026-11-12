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
 * Ground-hugging volumetric mist with drifting banks and soft floating motes.
 */
public class LowFogModule
extends Module {
    private static final class_2960 MOTE = ClarityVisuals.id("client/particles/firefly.png");
    private static final class_2960 MIST = ClarityVisuals.id("client/particles/mist_puff.png");

    private final EnumSetting<FogPreset> preset = new EnumSetting<FogPreset>("modules.settings.lowfog.preset", FogPreset.MISTY_VALLEY);
    private final SliderSetting density = new SliderSetting("modules.settings.lowfog.density", 1.15f, 0.05f, 3.0f, 0.05f);
    private final SliderSetting height = new SliderSetting("modules.settings.lowfog.height", 9.0f, 1.0f, 36.0f, 0.5f);
    private final SliderSetting distance = new SliderSetting("modules.settings.lowfog.distance", 72.0f, 16.0f, 140.0f, 2.0f);
    private final SliderSetting speed = new SliderSetting("modules.settings.lowfog.speed", 1.0f, 0.0f, 3.0f, 0.05f);
    private final ColorSetting color = new ColorSetting("modules.settings.lowfog.color", new Color(190, 210, 226));
    private final BooleanSetting motes = new BooleanSetting("modules.settings.lowfog.motes", true);
    private final SliderSetting moteAmount = new SliderSetting("modules.settings.lowfog.mote_amount", 0.65f, 0.1f, 2.0f, 0.05f, this.motes::jT);

    private final List<Mote> moteList = new ArrayList<>();
    private final List<Bank> bankList = new ArrayList<>();
    private final Random random = new Random();
    private long lastFrameNanos;
    private float clock;
    private int groundCacheTicks;
    private int cachedGroundY = 64;

    public LowFogModule() {
        super("LowFog", ModuleCategory.ql);
        this.getSettings().add(this.preset);
        this.getSettings().add(this.density);
        this.getSettings().add(this.height);
        this.getSettings().add(this.distance);
        this.getSettings().add(this.speed);
        this.getSettings().add(this.color);
        this.getSettings().add(this.motes);
        this.getSettings().add(this.moteAmount);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastFrameNanos = 0L;
        this.moteList.clear();
        this.bankList.clear();
        AtmosphereSurface.getInstance().reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.moteList.clear();
        this.bankList.clear();
        AtmosphereSurface.getInstance().reset();
    }

    private float presetDensity() {
        return switch (this.preset.jT()) {
            case MORNING_HAZE -> 0.72f;
            case SILENT_SWAMP -> 1.35f;
            case NIGHT_VEIL -> 1.12f;
            default -> 1.0f;
        };
    }

    private float random(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private float presetHeight() {
        return switch (this.preset.jT()) {
            case MORNING_HAZE -> 1.25f;
            case SILENT_SWAMP -> 0.85f;
            case NIGHT_VEIL -> 1.05f;
            default -> 1.0f;
        };
    }

    private int groundY(class_243 center) {
        if (++this.groundCacheTicks > 20) {
            this.groundCacheTicks = 0;
            try {
                int top = LowFogModule.aiG.field_1687.method_8624(class_2902.class_2903.field_13197,
                    (int) Math.floor(center.field_1352), (int) Math.floor(center.field_1350));
                this.cachedGroundY = Math.min(top + 1, (int) Math.floor(center.field_1351));
            } catch (Throwable ignored) {
                this.cachedGroundY = (int) Math.floor(center.field_1351) - 1;
            }
        }
        return this.cachedGroundY;
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (LowFogModule.fullNullCheck()) {
            this.moteList.clear();
            this.bankList.clear();
            return;
        }
        class_4184 camera = LowFogModule.aiG.field_1773.method_19418();
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
        int baseY = this.groundY(cam);
        this.updateBanks(cam, baseY, dt);
        this.renderMist(camera, baseY);
        class_4587 ms = stage.l();
        this.drawBanks(ms, cam);
        if (((Boolean) this.motes.jT()).booleanValue()) {
            this.updateMotes(cam, baseY, dt);
            this.drawMotes(ms, cam);
        } else {
            this.moteList.clear();
        }
    }

    private void updateBanks(class_243 center, int baseY, float dt) {
        float density = ((Float) this.density.jT()).floatValue() * this.presetDensity();
        float radius = Math.min(42.0f, ((Float) this.distance.jT()).floatValue() * 0.56f);
        int target = (int) Math.min(44, Math.max(10, 26.0f * density));
        this.bankList.removeIf(bank -> {
            bank.life += dt;
            double dx = bank.x - center.field_1352;
            double dz = bank.z - center.field_1350;
            return bank.life >= bank.maxLife || dx * dx + dz * dz > radius * radius * 1.35;
        });
        int budget = Math.min(Math.max(0, target - this.bankList.size()), 3);
        for (int i = 0; i < budget; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(this.random.nextDouble()) * radius;
            Bank bank = new Bank();
            bank.x = center.field_1352 + Math.cos(angle) * dist;
            bank.z = center.field_1350 + Math.sin(angle) * dist;
            bank.y = baseY + this.random(0.15f, Math.max(0.8f, ((Float) this.height.jT()).floatValue() * 0.42f));
            bank.vx = this.random(0.16f, 0.55f) * ((Float) this.speed.jT()).floatValue();
            bank.vz = this.random(-0.22f, 0.22f);
            bank.size = this.random(2.8f, 7.8f);
            bank.alpha = this.random(0.055f, 0.135f);
            bank.phase = this.random(0.0f, 62.8f);
            bank.life = 0.0f;
            bank.maxLife = this.random(9.0f, 19.0f);
            this.bankList.add(bank);
        }
        for (int i = 0, count = this.bankList.size(); i < count; i++) {
            Bank bank = this.bankList.get(i);
            float wobble = (float) Math.sin(this.clock * 0.30 + bank.phase);
            bank.x += (bank.vx + wobble * 0.08f) * dt;
            bank.z += (bank.vz + wobble * 0.04f) * dt;
            bank.y += Math.sin(this.clock * 0.22 + bank.phase) * 0.018f * dt;
        }
    }

    private void drawBanks(class_4587 matrices, class_243 cam) {
        Color color = (Color) this.color.jT();
        int rgb = color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
        float density = ((Float) this.density.jT()).floatValue() * this.presetDensity();
        for (int i = 0, count = this.bankList.size(); i < count; i++) {
            Bank bank = this.bankList.get(i);
            double dx = bank.x - cam.field_1352;
            double dy = bank.y - cam.field_1351;
            double dz = bank.z - cam.field_1350;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > ((Float) this.distance.jT()).floatValue() + 12.0) {
                continue;
            }
            float life = bank.life / bank.maxLife;
            float fade = (float) Math.sin(Math.min(1.0f, life) * Math.PI);
            float nearFade = distance < 2.5 ? (float) (distance / 2.5) : 1.0f;
            int alpha = (int) (Math.min(0.19f, bank.alpha * fade * nearFade * (0.62f + 0.38f * density)) * 255.0f);
            if (alpha < 2) {
                continue;
            }
            Render3D.renderBillboardTexture(new class_243(bank.x, bank.y, bank.z), bank.size, MIST,
                alpha << 24 | rgb, true, false);
        }
    }

    private void renderMist(class_4184 camera, int baseY) {
        try {
            org.joml.Matrix4f viewRot = new org.joml.Matrix4f().rotation(camera.method_23767());
            org.joml.Matrix4f viewMatrix = new org.joml.Matrix4f(viewRot).invert();
            org.joml.Matrix4f projectionMatrix = com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix();
            AtmosphereSurface.Parameters parameters = new AtmosphereSurface.Parameters();
            Color c = (Color) this.color.jT();
            parameters.mode = AtmosphereSurface.MODE_FOG;
            parameters.density = ((Float) this.density.jT()).floatValue() * this.presetDensity();
            parameters.height = ((Float) this.height.jT()).floatValue() * this.presetHeight();
            parameters.maxDistance = ((Float) this.distance.jT()).floatValue();
            parameters.speed = ((Float) this.speed.jT()).floatValue();
            parameters.baseY = baseY;
            parameters.time = this.clock;
            parameters.red = c.getRed() / 255.0f;
            parameters.green = c.getGreen() / 255.0f;
            parameters.blue = c.getBlue() / 255.0f;
            AtmosphereSurface.getInstance().apply(camera, viewMatrix, projectionMatrix, parameters);
        } catch (Throwable ignored) {
        }
    }

    private void updateMotes(class_243 center, int baseY, float dt) {
        float radius = Math.min(34.0f, ((Float) this.distance.jT()).floatValue() * 0.48f);
        int target = (int) Math.min(180, Math.max(24,
            55.0f * ((Float) this.moteAmount.jT()).floatValue() * this.presetDensity()));
        this.moteList.removeIf(m -> {
            m.life += dt;
            double dx = m.x - center.field_1352;
            double dz = m.z - center.field_1350;
            return m.life > m.maxLife || dx * dx + dz * dz > radius * radius;
        });
        int budget = Math.min(target - this.moteList.size(), 24);
        for (int i = 0; i < budget; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(this.random.nextDouble()) * radius;
            Mote mote = new Mote();
            mote.x = center.field_1352 + Math.cos(angle) * dist;
            mote.z = center.field_1350 + Math.sin(angle) * dist;
            mote.y = baseY + 0.25 + this.random.nextDouble() * Math.max(1.0, ((Float) this.height.jT()).floatValue() * 0.55);
            mote.vx = (this.random.nextDouble() - 0.5) * 0.12;
            mote.vz = (this.random.nextDouble() - 0.5) * 0.12;
            mote.vy = (this.random.nextDouble() - 0.35) * 0.05;
            mote.phase = this.random.nextFloat() * 62.8f;
            mote.size = 0.12f + this.random.nextFloat() * 0.22f;
            mote.life = 0.0f;
            mote.maxLife = 4.0f + this.random.nextFloat() * 7.0f;
            this.moteList.add(mote);
        }
        for (int i = 0, size = this.moteList.size(); i < size; i++) {
            Mote mote = this.moteList.get(i);
            mote.x += mote.vx * dt;
            mote.z += mote.vz * dt;
            mote.y += (mote.vy + Math.sin(this.clock * 0.8 + mote.phase) * 0.035) * dt;
        }
    }

    private void drawMotes(class_4587 matrices, class_243 cam) {
        Color color = (Color) this.color.jT();
        for (int i = 0, size = this.moteList.size(); i < size; i++) {
            Mote mote = this.moteList.get(i);
            float life = Math.min(1.0f, mote.life / mote.maxLife);
            float fade = (float) Math.sin(life * Math.PI);
            float twinkle = 0.60f + 0.40f * (float) Math.sin(this.clock * 2.2 + mote.phase);
            int alpha = (int) (fade * twinkle * 105.0f);
            if (alpha < 4) {
                continue;
            }
            int rgb = (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            Render3D.renderBillboardTexture(new class_243(mote.x, mote.y, mote.z), mote.size, MOTE,
                (alpha << 24) | rgb, true, true);
        }
    }

    static enum FogPreset
    implements NamedOption {
        MISTY_VALLEY("modules.settings.lowfog.preset.valley"),
        MORNING_HAZE("modules.settings.lowfog.preset.morning"),
        SILENT_SWAMP("modules.settings.lowfog.preset.swamp"),
        NIGHT_VEIL("modules.settings.lowfog.preset.night");

        private final String name;

        FogPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private static final class Mote {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        float size;
        float phase;
        float life;
        float maxLife;
    }

    private static final class Bank {
        double x;
        double y;
        double z;
        double vx;
        double vz;
        float size;
        float alpha;
        float phase;
        float life;
        float maxLife;
    }
}
