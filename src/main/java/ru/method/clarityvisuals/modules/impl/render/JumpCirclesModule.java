package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.event.PlayerTickEvent;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.JumpCircleShaderRenderer;
import ru.method.clarityvisuals.utils.render.JumpHillMesh;
import ru.method.clarityvisuals.utils.render.Render3D;

public class JumpCirclesModule
extends Module {
    private static final double EXPAND_END = 0.42;
    private static final double HOLD_END = 0.55;
    private static final int MAX_BURSTS = 4;
    private static final double CULL_DIST_SQ = 3600.0;
    private static final double LOOP_SEC = 2.0;

    private final SliderSetting size = new SliderSetting("settings.jumpcircles.size", 1.0f, 0.5f, 4.0f, 0.05f);
    private final SliderSetting lifetime = new SliderSetting("settings.jumpcircles.lifetime", 0.9f, 0.4f, 5.0f, 0.05f);
    private final EnumSetting<CircleMode> mode = new EnumSetting<CircleMode>("settings.jumpcircles.mode", CircleMode.RING);

    private final List<Burst> bursts = new ArrayList<Burst>();
    private final Random random = new Random();
    private boolean wasOnGround = true;

    public JumpCirclesModule() {
        super("JumpCircles", ModuleCategory.ql);
    }

    @EventHandler
    private void onPlayerTick(PlayerTickEvent playerTickEvent) {
        if (JumpCirclesModule.fullNullCheck()) {
            return;
        }
        boolean onGround = JumpCirclesModule.aiG.field_1724.method_24828();
        boolean jumpHeld = JumpCirclesModule.aiG.field_1690.field_1903.method_1434();
        if (this.wasOnGround && !onGround && jumpHeld) {
            class_243 origin = new class_243(
                JumpCirclesModule.aiG.field_1724.method_23317(),
                JumpCirclesModule.aiG.field_1724.method_24515().method_10264() + 0.02,
                JumpCirclesModule.aiG.field_1724.method_23321());
            this.spawnBurst(origin);
        }
        if (!this.bursts.isEmpty()) {
            long now = System.currentTimeMillis();
            double fallSpeed = JumpCirclesModule.aiG.field_1724.method_18798().field_1351;
            boolean landed = !this.wasOnGround && onGround;
            for (Burst burst : this.bursts) {
                double u = burst.progress(now);
                if (landed && u < HOLD_END) {
                    // Приземлились — плавно ускоряем возврат, без телепорта прогресса.
                    burst.extraMs += 90L;
                } else if (!onGround && fallSpeed < -0.18 && u >= 0.3 && u < HOLD_END) {
                    // Падаем — плавно ускоряем переход к фазе возврата.
                    burst.extraMs += 40L;
                }
            }
        }
        this.wasOnGround = onGround;
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (JumpCirclesModule.fullNullCheck()) {
            return;
        }
        int modeIndex = ((CircleMode) this.mode.jT()).ordinal();
        JumpCircleShaderRenderer.preferMode(modeIndex);
        JumpCircleShaderRenderer.tickBake(modeIndex);
        if (this.bursts.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        float sizeMul = Math.max(0.2f, floatOf(this.size.jT()));
        Color theme = ColorUtils.getGlobalColor();
        Color themeFlat = new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), 255);
        boolean hill = modeIndex == CircleMode.HILL.ordinal();
        Color tintBase = hill ? Color.WHITE : ColorUtils.fade(Color.WHITE, themeFlat, 0.25f);
        class_243 camera = JumpCirclesModule.aiG.field_1773.method_19418().method_19326();
        int frames = JumpCircleShaderRenderer.frameCount();

        Iterator<Burst> it = this.bursts.iterator();
        while (it.hasNext()) {
            Burst burst = it.next();
            double life = (double) burst.lifeMs;
            double age = (double) (burst.age(now) + burst.extraMs);
            double u = age / life;
            if (u >= 1.0) {
                it.remove();
                continue;
            }
            if (burst.origin.method_1025(camera) > CULL_DIST_SQ) {
                continue;
            }
            double scatter = scatterOf(u);
            double fadeIn = clamp01(age / 100.0);
            double fadeOut = u > 0.6 ? 1.0 - (u - 0.6) / 0.4 : 1.0;
            double masterAlpha = fadeIn * fadeOut;
            if (masterAlpha <= 0.012) {
                continue;
            }
            if (hill && burst.hill != null) {
                JumpHillMesh.render(matrixStackStage.l(), burst.hill, scatter, masterAlpha);
                continue;
            }
            int frame;
            if (hill) {
                frame = (int) Math.min((long) (frames - 1), Math.max(0L, Math.round(u * (double) (frames - 1))));
            } else {
                frame = pingPongFrame(age, burst.seedOffset, frames);
            }
            class_2960 texture = JumpCircleShaderRenderer.frameId(modeIndex, frame);
            if (texture == null) {
                continue;
            }
            float quadSize = (float) (2.4 * (double) sizeMul * (0.45 + 0.55 * scatter));
            class_243 pos = new class_243(burst.origin.field_1352, burst.origin.field_1351 + 0.06 * (double) sizeMul, burst.origin.field_1350);
            int alpha = (int) Math.round(255.0 * masterAlpha);
            int argb = alpha << 24 | tintBase.getRed() << 16 | tintBase.getGreen() << 8 | tintBase.getBlue();
            boolean additive = !hill;
            Render3D.renderOrientedTexture(pos, quadSize, texture, argb, class_2350.field_11036, true, additive);
        }
    }

    private void spawnBurst(class_243 origin) {
        while (this.bursts.size() >= MAX_BURSTS) {
            this.bursts.remove(0);
        }
        long lifeMs = Math.max(300L, Math.min(5000L, Math.round(floatOf(this.lifetime.jT()) * 1000.0f)));
        float sizeMul = Math.max(0.2f, floatOf(this.size.jT()));
        JumpHillMesh.Mesh hill = null;
        if (((CircleMode) this.mode.jT()) == CircleMode.HILL) {
            try {
                hill = JumpHillMesh.build(origin, sizeMul, (double) System.currentTimeMillis() / 1000.0);
            } catch (Throwable ignored) {
                hill = null;
            }
        }
        this.bursts.add(new Burst(origin, System.currentTimeMillis(), lifeMs, this.random.nextInt(JumpCircleShaderRenderer.frameCount()), hill));
        if (hill != null) {
            ClarityVisuals.LOGGER.info("[JumpCircle] hill spawned: {}", JumpHillMesh.debugStats(hill));
        }
    }

    private static float floatOf(Float f) {
        return f == null ? 1.0f : f.floatValue();
    }

    private static int pingPongFrame(double ageMs, int offset, int frames) {
        if (frames <= 1) {
            return 0;
        }
        double cycle = ageMs / 1000.0 / LOOP_SEC + (double) offset / (double) frames;
        double tri = 1.0 - Math.abs(1.0 - (cycle % 2.0));
        int frame = (int) (tri * (double) frames);
        if (frame < 0) {
            return 0;
        }
        if (frame >= frames) {
            return frames - 1;
        }
        return frame;
    }

    private static double scatterOf(double u) {
        if (u <= 0.0) {
            return 0.0;
        }
        if (u < EXPAND_END) {
            return easeOutCubic(u / EXPAND_END);
        }
        if (u < HOLD_END) {
            return 1.0;
        }
        if (u >= 1.0) {
            return 0.0;
        }
        return 1.0 - easeInOutCubic((u - HOLD_END) / (1.0 - HOLD_END));
    }

    private static double easeOutCubic(double t) {
        double x = 1.0 - clamp01(t);
        return 1.0 - x * x * x;
    }

    private static double easeInOutCubic(double t) {
        double x = clamp01(t);
        if (x < 0.5) {
            return 4.0 * x * x * x;
        }
        double y = 1.0 - x;
        return 1.0 - 4.0 * y * y * y;
    }

    private static double clamp01(double v) {
        if (v < 0.0) {
            return 0.0;
        }
        if (v > 1.0) {
            return 1.0;
        }
        return v;
    }

    @Override
    public void onDisable() {
        this.bursts.clear();
        super.onDisable();
    }

    public static enum CircleMode
    implements NamedOption {
        RING("settings.jumpcircles.mode.ring"),
        ETHER("settings.jumpcircles.mode.ether"),
        GOLD("settings.jumpcircles.mode.gold"),
        HILL("settings.jumpcircles.mode.hill");

        private final String key;

        private CircleMode(String key) {
            this.key = key;
        }

        @Override
        public String getName() {
            return this.key;
        }
    }

    static final class Burst {
        final class_243 origin;
        long startMs;
        final long lifeMs;
        final int seedOffset;
        final JumpHillMesh.Mesh hill;
        long extraMs;

        Burst(class_243 origin, long startMs, long lifeMs, int seedOffset, JumpHillMesh.Mesh hill) {
            this.origin = origin;
            this.startMs = startMs;
            this.lifeMs = lifeMs;
            this.seedOffset = seedOffset;
            this.hill = hill;
        }

        long age(long now) {
            return now - this.startMs;
        }

        double progress(long now) {
            return ((double) this.age(now) + (double) this.extraMs) / (double) this.lifeMs;
        }
    }
}
