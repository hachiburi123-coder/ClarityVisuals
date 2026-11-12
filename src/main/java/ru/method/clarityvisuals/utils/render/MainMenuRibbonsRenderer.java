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
import net.minecraft.class_4587;
import net.minecraft.class_5944;
import net.minecraft.class_9801;
import ru.method.clarityvisuals.api.render.providers.ResourceProvider;

public final class MainMenuRibbonsRenderer {
    private static final class_10156 SHADER_KEY = new class_10156(ResourceProvider.getShaderIdentifier("mainmenu_ribbons"), class_290.field_1575, class_10149.field_53930);

    private MainMenuRibbonsRenderer() {
    }

    public static void render(class_4587 matrices, float x, float y, float w, float h) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        class_5944 shader = RenderSystem.setShader(SHADER_KEY);
        class_310 client = class_310.method_1551();
        int fbW = 1920;
        int fbH = 1080;
        if (client != null && client.method_22683() != null) {
            fbW = Math.max(1, client.method_22683().method_4489());
            fbH = Math.max(1, client.method_22683().method_4506());
        }
        float time = (float) (System.currentTimeMillis() / 1000.0);
        set(shader, "time", time);
        set(shader, "screenSize", (float) fbW, (float) fbH);
        class_287 builder = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
        class_4587.class_4665 pose = matrices.method_23760();
        builder.method_56824(pose, x, y + h, 0.0f).method_22913(0.0f, 1.0f).method_22915(1.0f, 1.0f, 1.0f, 1.0f);
        builder.method_56824(pose, x + w, y + h, 0.0f).method_22913(1.0f, 1.0f).method_22915(1.0f, 1.0f, 1.0f, 1.0f);
        builder.method_56824(pose, x + w, y, 0.0f).method_22913(1.0f, 0.0f).method_22915(1.0f, 1.0f, 1.0f, 1.0f);
        builder.method_56824(pose, x, y, 0.0f).method_22913(0.0f, 0.0f).method_22915(1.0f, 1.0f, 1.0f, 1.0f);
        class_286.method_43433(builder.method_60800());
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void set(class_5944 shader, String name, float v) {
        class_284 uniform = shader.method_34582(name);
        if (uniform != null) {
            uniform.method_1251(v);
        }
    }

    private static void set(class_5944 shader, String name, float x, float y) {
        class_284 uniform = shader.method_34582(name);
        if (uniform != null) {
            uniform.method_1255(x, y);
        }
    }
}
