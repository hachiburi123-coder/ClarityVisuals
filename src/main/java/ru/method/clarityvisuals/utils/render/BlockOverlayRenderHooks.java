package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9974;
import org.lwjgl.opengl.GL11;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.BlockOverlayModule;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.utils.render.BlockOverlayShaderRenderer;
import ru.method.clarityvisuals.utils.render.ColorUtils;

public final class BlockOverlayRenderHooks {
    private static boolean registered = false;
    private static final long ANIM_EPOCH_NANOS = System.nanoTime();

    private BlockOverlayRenderHooks() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        WorldRenderEvents.BLOCK_OUTLINE.register(BlockOverlayRenderHooks::onBlockOutline);
    }

    private static boolean onBlockOutline(WorldRenderContext worldRenderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext) {
        class_238 class_23832;
        List<class_238> list;
        BlockOverlayModule blockOverlayModule;
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.isPanic()) {
            return true;
        }
        BlockOverlayModule blockOverlayModule2 = blockOverlayModule = clarityVisuals.getModuleManager() == null ? null : clarityVisuals.getModuleManager().getModule(BlockOverlayModule.class);
        if (blockOverlayModule == null || !blockOverlayModule.isToggled() || Module.fullNullCheck()) {
            return true;
        }
        if (worldRenderContext.world() == null) {
            return true;
        }
        class_4597 class_45972 = worldRenderContext.consumers();
        if (class_45972 == null) {
            return true;
        }
        class_4587 class_45872 = worldRenderContext.matrixStack();
        if (class_45872 == null) {
            return true;
        }
        class_2338 class_23382 = blockOutlineContext.blockPos();
        class_2680 class_26802 = blockOutlineContext.blockState();
        if (class_23382 == null || class_26802 == null) {
            return true;
        }
        boolean fill = (Boolean) blockOverlayModule.yQ.jT();
        boolean outline = (Boolean) blockOverlayModule.showOutline.jT();
        if (!fill && !outline) {
            return true;
        }
        class_3726 class_37262 = blockOutlineContext.entity() != null ? class_3726.method_16195((class_1297) blockOutlineContext.entity()) : class_3726.method_16194();
        class_265 class_2652 = class_26802.method_26172((class_1922) worldRenderContext.world(), class_23382, class_37262);
        if (class_2652 == null) {
            class_2652 = class_259.method_1077();
        }
        if ((list = class_2652.method_1090()).isEmpty()) {
            return true;
        }
        if (list.size() > 1) {
            class_238 class_2384 = class_2652.method_1107();
            list = List.of(class_2384);
        }
        double d = blockOutlineContext.cameraX();
        double d2 = blockOutlineContext.cameraY();
        double d3 = blockOutlineContext.cameraZ();
        float f = 0.0f;
        boolean bl = GL11.glIsEnabled((int) 3042);
        boolean bl2 = GL11.glIsEnabled((int) 2884);
        boolean bl3 = GL11.glIsEnabled((int) 2929);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((int) 770, (int) 771);
        boolean useShader = fill && blockOverlayModule.fillType.jT() == BlockOverlayModule.FillType.SHADER;
        int n = BlockOverlayRenderHooks.clampAlpha(((Float) blockOverlayModule.yT.jT()).floatValue());
        int n2 = outline ? BlockOverlayRenderHooks.clampAlpha(((Float) blockOverlayModule.yU.jT()).floatValue()) : 0;
        float shaderAlpha = Math.max(0.0f, Math.min(1.0f, ((Float) blockOverlayModule.shaderAlpha.jT()).floatValue()));
        float f2 = (float) ((double) (System.nanoTime() - ANIM_EPOCH_NANOS) / 4.0E9) * ((Float) blockOverlayModule.yV.jT()).floatValue();
        float animMul = BlockOverlayRenderHooks.animationMultiplier(blockOverlayModule, f2, class_23382);
        BlockOverlayModule.OverlayShape overlayShape = (BlockOverlayModule.OverlayShape) blockOverlayModule.yS.jT();
        Color baseColor = BlockOverlayRenderHooks.resolveBaseColor(blockOverlayModule, overlayShape, f2);
        if (useShader && BlockOverlayShaderRenderer.canRender(overlayShape)) {
            int n3 = Math.max(0, Math.round(255.0f * shaderAlpha * animMul));
            if (n3 > 0) {
                for (class_238 outlineBox : list) {
                    class_238 class_2385 = outlineBox.method_996(class_23382).method_1014(0.003).method_989(-d, -d2, -d3);
                    BlockOverlayShaderRenderer.render(class_45872, class_2385, n3, f2, overlayShape, baseColor);
                }
            }
        } else if (fill && n > 0) {
            int flatAlpha = Math.max(0, Math.round((float) n * animMul));
            if (flatAlpha > 0) {
                for (class_238 class_2386 : list) {
                    class_238 fillBox = class_2386.method_996(class_23382).method_1014((double) f).method_989(-d, -d2, -d3);
                    BlockOverlayRenderHooks.drawFilledBox(class_45872, class_45972, fillBox, new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), flatAlpha));
                }
            }
        }
        if (n2 > 0) {
            int outlineAlpha = Math.max(0, Math.round((float) n2 * animMul));
            if (outlineAlpha > 0) {
                float thickness = Math.max(0.5f, Math.min(5.0f, ((Float) blockOverlayModule.lineWidth.jT()).floatValue()));
                for (class_238 class_2386 : list) {
                    class_238 outlineBox = class_2386.method_996(class_23382).method_1014((double) f).method_989(-d, -d2, -d3);
                    BlockOverlayRenderHooks.renderOutline(class_45872, class_45972, outlineBox, outlineAlpha, thickness, baseColor);
                }
            }
        }
        if (bl2) {
            RenderSystem.enableCull();
        }
        if (!bl) {
            RenderSystem.disableBlend();
        }
        if (bl3) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        return false;
    }

    private static Color resolveBaseColor(BlockOverlayModule module, BlockOverlayModule.OverlayShape shape, float time) {
        boolean clientColor = (Boolean) module.clientColor.jT();
        Color custom = (Color) module.customColor.jT();
        Color base = !clientColor && custom != null ? custom : ColorUtils.getPrimaryColor();
        if (base == null) {
            base = new Color(151, 71, 255, 255);
        }
        if (shape == BlockOverlayModule.OverlayShape.RAINBOW) {
            float hue = (time * 0.07f) % 1.0f;
            if (hue < 0.0f) {
                hue += 1.0f;
            }
            return new Color(Color.HSBtoRGB(hue, 0.75f, 1.0f));
        }
        return base;
    }

    private static float animationMultiplier(BlockOverlayModule module, float time, class_2338 pos) {
        BlockOverlayModule.OutlineAnimation anim = (BlockOverlayModule.OutlineAnimation) module.animation.jT();
        if (anim == null || anim == BlockOverlayModule.OutlineAnimation.NONE) {
            return 1.0f;
        }
        if (anim == BlockOverlayModule.OutlineAnimation.PULSE) {
            return 0.72f + 0.28f * (float) Math.sin(time * 3.2f);
        }
        double wavePhase = (double) time * 3.2 - (double) (pos.method_10263() + pos.method_10264() + pos.method_10260()) * 0.35;
        return 0.72f + 0.28f * (float) Math.sin(wavePhase);
    }

    private static void renderOutline(class_4587 class_45872, class_4597 class_45972, class_238 class_2383, int n, float thickness, Color base) {
        Color color = ColorUtils.mix(base, Color.WHITE, 0.35f);
        float expand = 0.0012f * thickness;
        for (int i = 0; i < 2; ++i) {
            float f2 = 1.0f - (float) i * 0.45f;
            int n2 = Math.max(8, Math.round((float) n * (0.24f * f2)));
            class_238 class_2384 = class_2383.method_1014(expand * (double) (i + 1));
            class_9974.method_62295((class_4587) class_45872, (class_4588) class_45972.getBuffer(class_1921.method_23594()), (class_238) class_2384, (float) ((float) color.getRed() / 255.0f), (float) ((float) color.getGreen() / 255.0f), (float) ((float) color.getBlue() / 255.0f), (float) ((float) n2 / 255.0f));
        }
        Color color3 = new Color(base.getRed(), base.getGreen(), base.getBlue(), n);
        class_9974.method_62295((class_4587) class_45872, (class_4588) class_45972.getBuffer(class_1921.method_23594()), (class_238) class_2383, (float) ((float) color3.getRed() / 255.0f), (float) ((float) color3.getGreen() / 255.0f), (float) ((float) color3.getBlue() / 255.0f), (float) ((float) color3.getAlpha() / 255.0f));
    }

    private static void drawFilledBox(class_4587 class_45872, class_4597 class_45972, class_238 class_2383, Color color) {
        class_9974.method_62300((class_4587) class_45872, (class_4588) class_45972.getBuffer(class_1921.method_49047()), (double) class_2383.field_1323, (double) class_2383.field_1322, (double) class_2383.field_1321, (double) class_2383.field_1320, (double) class_2383.field_1325, (double) class_2383.field_1324, (float) ((float) color.getRed() / 255.0f), (float) ((float) color.getGreen() / 255.0f), (float) ((float) color.getBlue() / 255.0f), (float) ((float) color.getAlpha() / 255.0f));
    }

    private static int clampAlpha(float f) {
        if (f <= 0.0f) {
            return 0;
        }
        if (f >= 255.0f) {
            return 255;
        }
        return Math.round(f);
    }
}
