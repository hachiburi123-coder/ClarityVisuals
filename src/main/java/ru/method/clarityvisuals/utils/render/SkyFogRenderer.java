package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
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

public final class SkyFogRenderer {
    private static final class_10156 KEY = new class_10156(ResourceProvider.getShaderIdentifier("sky_fog"), class_290.field_1575, class_10149.field_53930);
    private static long startNano = -1L;
    private static float accumulated;

    private SkyFogRenderer() {
    }

    public static float updateTime(float speed) {
        long now = System.nanoTime();
        if (startNano < 0L) {
            startNano = now;
            return accumulated;
        }
        float delta = Math.min((float)(now - startNano) / 1.0E9f, 0.1f);
        startNano = now;
        accumulated = (accumulated + delta * Math.max(0.0f, speed)) % 10000.0f;
        return accumulated;
    }

    public static void resetTime() {
        startNano = -1L;
        accumulated = 0.0f;
    }

    public static void render(float time, float w, float h, int fogColor, float intensity, int mode) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1561() == null || mc.method_1561().field_4686 == null) {
            return;
        }
        class_4184 camera = mc.method_1561().field_4686;
        Quaternionf rot = camera.method_23767();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f).rotate(rot);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f).rotate(rot);
        Vector3f fwd = new Vector3f(0.0f, 0.0f, -1.0f).rotate(rot);
        RenderSystem.disableScissor();
        RenderSystem.viewport(0, 0, (int)w, (int)h);
        mc.method_1522().method_1235(true);
        RenderSystem.depthFunc(515);
        RenderSystem.blendEquation(32774);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        class_5944 program = RenderSystem.setShader(KEY);
        SkyFogRenderer.set(program, "time", time);
        SkyFogRenderer.set(program, "resolution", w, h);
        SkyFogRenderer.set(program, "fogColor", (float)(fogColor >> 16 & 0xFF) / 255.0f, (float)(fogColor >> 8 & 0xFF) / 255.0f, (float)(fogColor & 0xFF) / 255.0f, 1.0f);
        SkyFogRenderer.set(program, "intensity", intensity);
        SkyFogRenderer.set(program, "fogMode", (float)mode);
        SkyFogRenderer.set(program, "camRight", right.x, right.y, right.z, 0.0f);
        SkyFogRenderer.set(program, "camUp", up.x, up.y, up.z, 0.0f);
        SkyFogRenderer.set(program, "camForward", fwd.x, fwd.y, fwd.z, 0.0f);
        org.joml.Matrix4f proj = RenderSystem.getProjectionMatrix();
        float fovScale = 1.0f;
        if (proj != null && Math.abs(proj.m11()) > 1.0E-5f) {
            fovScale = 1.0f / proj.m11();
        }
        SkyFogRenderer.set(program, "fovScale", fovScale);
        class_4587 matrices = new class_4587();
        class_287 buf = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
        SkyFogRenderer.vertex(buf, matrices.method_23760(), -1.0f, -1.0f);
        SkyFogRenderer.vertex(buf, matrices.method_23760(), 1.0f, -1.0f);
        SkyFogRenderer.vertex(buf, matrices.method_23760(), 1.0f, 1.0f);
        SkyFogRenderer.vertex(buf, matrices.method_23760(), -1.0f, 1.0f);
        class_286.method_43433((class_9801)buf.method_60800());
        RenderSystem.depthMask(true);
    }

    private static void vertex(class_287 buf, class_4587.class_4665 entry, float x, float y) {
        buf.method_56824(entry, x, y, 0.0f).method_22913(0.0f, 0.0f).method_22915(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void set(class_5944 program, String name, float f) {
        if (program == null) {
            return;
        }
        class_284 uniform = program.method_34582(name);
        if (uniform != null) {
            uniform.method_1251(f);
        }
    }

    private static void set(class_5944 program, String name, float f, float f2) {
        if (program == null) {
            return;
        }
        class_284 uniform = program.method_34582(name);
        if (uniform != null) {
            uniform.method_1255(f, f2);
        }
    }

    private static void set(class_5944 program, String name, float f, float f2, float f3, float f4) {
        if (program == null) {
            return;
        }
        class_284 uniform = program.method_34582(name);
        if (uniform != null) {
            uniform.method_35657(f, f2, f3, f4);
        }
    }
}
