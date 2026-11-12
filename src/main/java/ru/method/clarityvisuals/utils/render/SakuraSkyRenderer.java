package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_276;
import net.minecraft.class_284;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_5944;
import net.minecraft.class_9801;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.method.clarityvisuals.api.render.providers.ResourceProvider;

/**
 * Warm dawn sky for the Sakura module. The pass is emitted at NDC z = 1.0 with
 * LEQUAL testing, so only pixels where the depth buffer still contains clear
 * sky are replaced. Terrain, water, entities and buildings remain untouched.
 */
public final class SakuraSkyRenderer {
    private static final class_10156 KEY = new class_10156(ResourceProvider.getShaderIdentifier("sakura_sky"),
        class_290.field_1575, class_10149.field_53930);

    private SakuraSkyRenderer() {
    }

    public static void render(float elapsed, float intensity, boolean sun, boolean clouds) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1561() == null || mc.method_1561().field_4686 == null) {
            return;
        }
        class_276 framebuffer = mc.method_1522();
        if (framebuffer == null) {
            return;
        }
        float w = Math.max(1.0f, (float) framebuffer.field_1482);
        float h = Math.max(1.0f, (float) framebuffer.field_1481);
        class_4184 camera = mc.method_1561().field_4686;
        Quaternionf rot = camera.method_23767();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f).rotate(rot);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f).rotate(rot);
        Vector3f forward = new Vector3f(0.0f, 0.0f, -1.0f).rotate(rot);

        RenderSystem.disableScissor();
        RenderSystem.viewport(0, 0, (int) w, (int) h);
        framebuffer.method_1235(true);
        RenderSystem.depthFunc(515);
        RenderSystem.blendEquation(32774);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        class_5944 program = RenderSystem.setShader(KEY);
        set(program, "time", elapsed);
        set(program, "resolution", w, h);
        set(program, "camRight", right.x, right.y, right.z, 0.0f);
        set(program, "camUp", up.x, up.y, up.z, 0.0f);
        set(program, "camForward", forward.x, forward.y, forward.z, 0.0f);
        // Fixed world-space dawn sun. It stays in the sky instead of drifting
        // with the crosshair when the camera turns.
        set(program, "sunDirection", -0.55f, 0.20f, -0.81f, 0.0f);
        set(program, "intensity", Math.max(0.0f, Math.min(2.0f, intensity)));
        set(program, "sun", sun ? 1.0f : 0.0f);
        set(program, "clouds", clouds ? 1.0f : 0.0f);
        set(program, "topColor", 0.46f, 0.36f, 0.72f, 1.0f);
        set(program, "horizonColor", 1.0f, 0.65f, 0.66f, 1.0f);
        set(program, "sunColor", 1.0f, 0.84f, 0.64f, 1.0f);

        class_4587 matrices = new class_4587();
        class_287 buf = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
        vertex(buf, matrices.method_23760(), -1.0f, -1.0f);
        vertex(buf, matrices.method_23760(), 1.0f, -1.0f);
        vertex(buf, matrices.method_23760(), 1.0f, 1.0f);
        vertex(buf, matrices.method_23760(), -1.0f, 1.0f);
        class_286.method_43433((class_9801) buf.method_60800());
        RenderSystem.depthMask(true);
    }

    private static void vertex(class_287 buf, class_4587.class_4665 entry, float x, float y) {
        buf.method_56824(entry, x, y, 0.0f).method_22913(0.0f, 0.0f).method_22915(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void set(class_5944 program, String name, float value) {
        if (program == null) {
            return;
        }
        class_284 uniform = program.method_34582(name);
        if (uniform != null) {
            uniform.method_1251(value);
        }
    }

    private static void set(class_5944 program, String name, float x, float y) {
        if (program == null) {
            return;
        }
        class_284 uniform = program.method_34582(name);
        if (uniform != null) {
            uniform.method_1255(x, y);
        }
    }

    private static void set(class_5944 program, String name, float x, float y, float z, float w) {
        if (program == null) {
            return;
        }
        class_284 uniform = program.method_34582(name);
        if (uniform != null) {
            uniform.method_35657(x, y, z, w);
        }
    }
}
