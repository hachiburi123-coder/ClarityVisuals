package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_238;
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
import ru.method.clarityvisuals.modules.impl.render.BlockOverlayModule;

final class BlockOverlayShaderRenderer {
    private static final class_10156 PLASMA_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_plasma");
    private static final class_10156 NEBULA_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_nebula");
    private static final class_10156 STARS_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_stars");
    private static final class_10156 AURORA_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_aurora");
    private static final class_10156 WEB_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_web");
    private static final class_10156 NEON_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_neon");
    private static final class_10156 EMERALD_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_emerald");
    private static final class_10156 RAINBOW_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_rainbow");
    private static final class_10156 OCEAN_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_ocean");
    private static final class_10156 ICE_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_ice");
    private static final class_10156 CANDY_SHADER_KEY = BlockOverlayShaderRenderer.key("block_overlay_candy");

    private BlockOverlayShaderRenderer() {
    }

    static boolean canRender(BlockOverlayModule.OverlayShape overlayShape) {
        return overlayShape != null;
    }

    static void render(class_4587 class_45872, class_238 class_2383, int n, float f, BlockOverlayModule.OverlayShape overlayShape, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        class_5944 class_59442 = RenderSystem.setShader((class_10156)BlockOverlayShaderRenderer.shaderKey(overlayShape));
        float f2 = f * 4.0f;
        float f3 = Math.max(0.0f, Math.min(1.0f, (float)n / 255.0f));
        class_310 class_3102 = class_310.method_1551();
        int n2 = Math.max(1, class_3102.method_22683().method_4489());
        int n3 = Math.max(1, class_3102.method_22683().method_4506());
        BlockOverlayShaderRenderer.set(class_59442, "time", f2);
        BlockOverlayShaderRenderer.set(class_59442, "screenSize", n2, n3);
        BlockOverlayShaderRenderer.set(class_59442, "baseColor", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, f3);
        BlockOverlayShaderRenderer.set(class_59442, "alpha", f3);
        BlockOverlayShaderRenderer.set(class_59442, "zoomFactor", 1.0f);
        class_287 class_2872 = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
        BlockOverlayShaderRenderer.drawFaces(class_2872, class_45872.method_23760(), class_2383, n);
        class_286.method_43433((class_9801)class_2872.method_60800());
        RenderSystem.depthMask((boolean)true);
    }

    private static class_10156 key(String string) {
        return new class_10156(ResourceProvider.getShaderIdentifier(string), class_290.field_1575, class_10149.field_53930);
    }

    private static class_10156 shaderKey(BlockOverlayModule.OverlayShape overlayShape) {
        if (overlayShape == null) {
            return NEBULA_SHADER_KEY;
        }
        return switch (overlayShape) {
            case NEBULA -> NEBULA_SHADER_KEY;
            case STARS -> STARS_SHADER_KEY;
            case WEB -> WEB_SHADER_KEY;
            case PLASMA -> PLASMA_SHADER_KEY;
            case AURORA -> AURORA_SHADER_KEY;
            case NEON -> NEON_SHADER_KEY;
            case EMERALD -> EMERALD_SHADER_KEY;
            case RAINBOW -> RAINBOW_SHADER_KEY;
            case OCEAN -> OCEAN_SHADER_KEY;
            case ICE -> ICE_SHADER_KEY;
            case CANDY -> CANDY_SHADER_KEY;
        };
    }

    private static void set(class_5944 class_59442, String string, float f) {
        class_284 class_2842 = class_59442.method_34582(string);
        if (class_2842 != null) {
            class_2842.method_1251(f);
        }
    }

    private static void set(class_5944 class_59442, String string, float f, float f2) {
        class_284 class_2842 = class_59442.method_34582(string);
        if (class_2842 != null) {
            class_2842.method_1255(f, f2);
        }
    }

    private static void set(class_5944 class_59442, String string, float f, float f2, float f3, float f4) {
        class_284 class_2842 = class_59442.method_34582(string);
        if (class_2842 != null) {
            class_2842.method_35657(f, f2, f3, f4);
        }
    }

    private static void drawFaces(class_287 class_2872, class_4587.class_4665 class_46652, class_238 class_2383, int n) {
        float f = 1.0f;
        float f2 = 1.0f;
        float f3 = 1.0f;
        float f4 = (float)n / 255.0f;
        BlockOverlayShaderRenderer.face(class_2872, class_46652, class_2383.field_1323, class_2383.field_1322, class_2383.field_1321, class_2383.field_1320, class_2383.field_1322, class_2383.field_1321, class_2383.field_1320, class_2383.field_1325, class_2383.field_1321, class_2383.field_1323, class_2383.field_1325, class_2383.field_1321, f, f2, f3, f4);
        BlockOverlayShaderRenderer.face(class_2872, class_46652, class_2383.field_1320, class_2383.field_1322, class_2383.field_1324, class_2383.field_1323, class_2383.field_1322, class_2383.field_1324, class_2383.field_1323, class_2383.field_1325, class_2383.field_1324, class_2383.field_1320, class_2383.field_1325, class_2383.field_1324, f, f2, f3, f4);
        BlockOverlayShaderRenderer.face(class_2872, class_46652, class_2383.field_1323, class_2383.field_1322, class_2383.field_1324, class_2383.field_1323, class_2383.field_1322, class_2383.field_1321, class_2383.field_1323, class_2383.field_1325, class_2383.field_1321, class_2383.field_1323, class_2383.field_1325, class_2383.field_1324, f, f2, f3, f4);
        BlockOverlayShaderRenderer.face(class_2872, class_46652, class_2383.field_1320, class_2383.field_1322, class_2383.field_1321, class_2383.field_1320, class_2383.field_1322, class_2383.field_1324, class_2383.field_1320, class_2383.field_1325, class_2383.field_1324, class_2383.field_1320, class_2383.field_1325, class_2383.field_1321, f, f2, f3, f4);
        BlockOverlayShaderRenderer.face(class_2872, class_46652, class_2383.field_1323, class_2383.field_1325, class_2383.field_1321, class_2383.field_1320, class_2383.field_1325, class_2383.field_1321, class_2383.field_1320, class_2383.field_1325, class_2383.field_1324, class_2383.field_1323, class_2383.field_1325, class_2383.field_1324, f, f2, f3, f4);
        BlockOverlayShaderRenderer.face(class_2872, class_46652, class_2383.field_1323, class_2383.field_1322, class_2383.field_1324, class_2383.field_1320, class_2383.field_1322, class_2383.field_1324, class_2383.field_1320, class_2383.field_1322, class_2383.field_1321, class_2383.field_1323, class_2383.field_1322, class_2383.field_1321, f, f2, f3, f4);
    }

    private static void face(class_287 class_2872, class_4587.class_4665 class_46652, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12, float f, float f2, float f3, float f4) {
        class_2872.method_56824(class_46652, (float)d, (float)d2, (float)d3).method_22913(0.0f, 0.0f).method_22915(f, f2, f3, f4);
        class_2872.method_56824(class_46652, (float)d4, (float)d5, (float)d6).method_22913(1.0f, 0.0f).method_22915(f, f2, f3, f4);
        class_2872.method_56824(class_46652, (float)d7, (float)d8, (float)d9).method_22913(1.0f, 1.0f).method_22915(f, f2, f3, f4);
        class_2872.method_56824(class_46652, (float)d10, (float)d11, (float)d12).method_22913(0.0f, 1.0f).method_22915(f, f2, f3, f4);
    }
}
