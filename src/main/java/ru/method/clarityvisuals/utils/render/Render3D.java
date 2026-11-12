package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.minecraft.class_10142;
import net.minecraft.class_10156;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import ru.method.clarityvisuals.api.mixins.accessors.IWorldRenderer;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;

public final class Render3D
implements MinecraftClientAccess {
    public static boolean rendering = false;
    public static final List<VertexCollection> QUADS = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> DEPTH_QUADS = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> LIGHTNING_QUADS = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> DEBUG_LINES = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> DEPTH_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> SHINE_QUADS = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> DEPTH_SHINE_QUADS = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> NEON_QUADS = new ArrayList<VertexCollection>();
    /**
     * Неон, который сколько-нибудь верно соотносится с миром: флашится с depth-тестом,
     * поэтому не светится сквозь стены. Отдельная очередь, чтобы не поменять поведение
     * остального неона (он по-прежнему рендерится сквозь стены).
     */
    public static final List<VertexCollection> NEON_QUADS_DEPTH = new ArrayList<VertexCollection>();
    private static final float[] NEON_WIDTH_SCALE = new float[]{5.4f, 3.5f, 2.25f, 1.18f, 0.58f};
    private static final float[] NEON_ALPHA_SCALE = new float[]{0.055f, 0.11f, 0.26f, 0.78f, 0.95f};
    /**
     * Рёбра неона срезаются, когда сегмент проходит ближе этого расстояния от прямой
     * view камеры: в саду в такой клин риббон разворачивается в заливку на весь экран.
     */
    private static final float NEON_LENS_SHELL = 0.25f;
    private static final float[] NEON_WHITE_MIX = new float[]{0.0f, 0.12f, 0.34f, 0.58f, 0.88f};
    public static final List<VertexCollection> SHINE_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static final List<VertexCollection> DEPTH_SHINE_DEBUG_LINES = new ArrayList<VertexCollection>();
    public static final List<TexturedQuad> TEXTURED_QUADS = new ArrayList<TexturedQuad>();
    public static final List<TexturedQuad> TEXTURED_QUADS_ADDITIVE = new ArrayList<TexturedQuad>();
    public static final List<TexturedQuad> TEXTURED_QUADS_DEPTH = new ArrayList<TexturedQuad>();
    public static final List<TexturedQuad> TEXTURED_QUADS_DEPTH_ADDITIVE = new ArrayList<TexturedQuad>();
    public static final List<CustomTexturedQuad> CUSTOM_UV_QUADS = new ArrayList<CustomTexturedQuad>();
    public static final List<CustomTexturedQuad> DEPTH_CUSTOM_UV_QUADS = new ArrayList<CustomTexturedQuad>();
    public static final List<VariableLine> VARIABLE_LINES = new ArrayList<VariableLine>();
    public static final List<VariableLine> DEPTH_VARIABLE_LINES = new ArrayList<VariableLine>();
    private static final Map<class_2960, List<TexturedQuad>> textureBatches = new LinkedHashMap<class_2960, List<TexturedQuad>>();
    private static final int SHAPE_CACHE_CAPACITY = 256;
    private static final Map<class_265, ShapeCache> SHAPE_CACHE = new LinkedHashMap<class_265, ShapeCache>(64, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<class_265, ShapeCache> entry) {
            return this.size() > 256;
        }
    };
    private static Matrix4f lastProjectionMatrix = new Matrix4f();
    private static Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    /**
     * Кэш билборд-поворота камеры: строится один раз за кадр (при смене yaw/pitch),
     * квады частиц собирают финальную матрицу как translate * rotation без
     * MatrixStack-операций на каждую частицу.
     */
    private static final Matrix4f BILLBOARD_ROT = new Matrix4f();
    private static float billboardYaw = Float.NaN;
    private static float billboardPitch = Float.NaN;
    private static final float DEG_TO_RAD = (float)(Math.PI / 180.0);
    /**
     * Повороты квада для каждой из шести граней мира (renderOrientedTexture):
     * эквивалент applyFaceRotation, вычислен один раз вместо стека на каждую частицу.
     */
    private static final Matrix4f[] FACE_ROTATIONS = Render3D.buildFaceRotations();
    /** Пулы квадов: рендер частиц создаёт сотни-тысячи квадов за кадр. */
    private static final ArrayDeque<TexturedQuad> TEXTURED_QUAD_POOL = new ArrayDeque<>();
    private static final ArrayDeque<CustomTexturedQuad> CUSTOM_QUAD_POOL = new ArrayDeque<>();
    private static final int MAX_TEXTURE_BATCHES = 256;
    private static float[] widthScratch = new float[8];

    public static void setLastProjectionMatrix(Matrix4f matrix4f) {
        if (matrix4f == null) {
            return;
        }
        lastProjectionMatrix.set((Matrix4fc)matrix4f);
    }

    public static Matrix4f getLastProjectionMatrix() {
        return new Matrix4f((Matrix4fc)lastProjectionMatrix);
    }

    public static void setLastWorldSpaceMatrix(class_4587.class_4665 class_46652) {
        if (class_46652 == null) {
            return;
        }
        lastWorldSpaceMatrix.set((Matrix4fc)class_46652.method_23761());
    }

    public static Matrix4f getLastWorldSpaceMatrix() {
        return new Matrix4f((Matrix4fc)lastWorldSpaceMatrix);
    }

    public static void renderCube(class_4587 class_45872, class_243 class_2432, double d, boolean bl, Color color, boolean bl2, Color color2) {
        if (bl2) {
            Render3D.renderBoxOutline(class_45872, new class_238(class_2432.method_1031(-0.5 * d, -0.5 * d, -0.5 * d), class_2432.method_1031(0.5 * d, 0.5 * d, 0.5 * d)), color2);
        }
        if (bl) {
            Render3D.renderBox(class_45872, new class_238(class_2432.method_1031(-0.5 * d, -0.5 * d, -0.5 * d), class_2432.method_1031(0.5 * d, 0.5 * d, 0.5 * d)), color);
        }
    }

    public static void renderCube(class_4587 class_45872, class_238 class_2383, boolean bl, Color color, boolean bl2, Color color2) {
        if (bl2) {
            Render3D.renderBoxOutline(class_45872, class_2383, color2);
        }
        if (bl) {
            Render3D.renderBox(class_45872, class_2383, color);
        }
    }

    public static void drawLine(class_4587 class_45872, class_243 class_2432, class_243 class_2433, Color color) {
        Render3D.drawLine(class_45872, class_2432, class_2433, color, false);
    }

    public static void drawLine(class_4587 class_45872, class_243 class_2432, class_243 class_2433, Color color, boolean bl) {
        if (!rendering) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2434 = Render3D.aiG.field_1773.method_19418().method_19326();
        float f = (float)(class_2432.field_1352 - class_2434.field_1352);
        float f2 = (float)(class_2432.field_1351 - class_2434.field_1351);
        float f3 = (float)(class_2432.field_1350 - class_2434.field_1350);
        float f4 = (float)(class_2433.field_1352 - class_2434.field_1352);
        float f5 = (float)(class_2433.field_1351 - class_2434.field_1351);
        float f6 = (float)(class_2433.field_1350 - class_2434.field_1350);
        (bl ? DEPTH_DEBUG_LINES : DEBUG_LINES).add(new VertexCollection(new Vertex(matrix4f, f, f2, f3, color.getRGB()), new Vertex(matrix4f, f4, f5, f6, color.getRGB())));
    }

    public static void drawLine(class_4587 class_45872, class_243 class_2432, class_243 class_2433, Color color, Color color2, float f, boolean bl) {
        if (color == null || color2 == null) {
            return;
        }
        Render3D.drawLine(class_45872, class_2432, class_2433, color.getRGB(), color2.getRGB(), f, bl);
    }

    public static void drawLine(class_4587 class_45872, class_243 class_2432, class_243 class_2433, Color color, float f, boolean bl) {
        if (color == null) {
            return;
        }
        int n = color.getRGB();
        Render3D.drawLine(class_45872, class_2432, class_2433, n, n, f, bl);
    }

    public static void drawLine(class_4587 class_45872, class_243 class_2432, class_243 class_2433, int n, float f, boolean bl) {
        Render3D.drawLine(class_45872, class_2432, class_2433, n, n, f, bl);
    }

    public static void drawLine(class_4587 class_45872, class_243 class_2432, class_243 class_2433, int n, int n2, float f, boolean bl) {
        if (!rendering) {
            return;
        }
        if (class_45872 == null || class_2432 == null || class_2433 == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2434 = Render3D.aiG.field_1773.method_19418().method_19326();
        float f2 = (float)(class_2432.field_1352 - class_2434.field_1352);
        float f3 = (float)(class_2432.field_1351 - class_2434.field_1351);
        float f4 = (float)(class_2432.field_1350 - class_2434.field_1350);
        float f5 = (float)(class_2433.field_1352 - class_2434.field_1352);
        float f6 = (float)(class_2433.field_1351 - class_2434.field_1351);
        float f7 = (float)(class_2433.field_1350 - class_2434.field_1350);
        VariableLine variableLine = new VariableLine(new Vertex(matrix4f, f2, f3, f4, n), new Vertex(matrix4f, f5, f6, f7, n2), Math.max(1.0f, f));
        (bl ? DEPTH_VARIABLE_LINES : VARIABLE_LINES).add(variableLine);
    }

    public static void renderBox(class_4587 class_45872, class_238 class_2383, Color color) {
        Render3D.renderGradientBox(class_45872, class_2383, color, color);
    }

    public static void renderBoxOutline(class_4587 class_45872, class_238 class_2383, Color color) {
        Render3D.renderGradientBoxOutline(class_45872, class_2383, color, color);
    }

    public static void renderTriangle(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, Color color) {
        Render3D.renderTriangle(class_45872, class_2432, class_2433, class_2434, color, false);
    }

    public static void renderTriangle(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, Color color, boolean bl) {
        if (!rendering) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2435 = Render3D.aiG.field_1773.method_19418().method_19326();
        float f = (float)(class_2432.field_1352 - class_2435.field_1352);
        float f2 = (float)(class_2432.field_1351 - class_2435.field_1351);
        float f3 = (float)(class_2432.field_1350 - class_2435.field_1350);
        float f4 = (float)(class_2433.field_1352 - class_2435.field_1352);
        float f5 = (float)(class_2433.field_1351 - class_2435.field_1351);
        float f6 = (float)(class_2433.field_1350 - class_2435.field_1350);
        float f7 = (float)(class_2434.field_1352 - class_2435.field_1352);
        float f8 = (float)(class_2434.field_1351 - class_2435.field_1351);
        float f9 = (float)(class_2434.field_1350 - class_2435.field_1350);
        (bl ? DEPTH_QUADS : QUADS).add(new VertexCollection(new Vertex(matrix4f, f, f2, f3, color.getRGB()), new Vertex(matrix4f, f4, f5, f6, color.getRGB()), new Vertex(matrix4f, f7, f8, f9, color.getRGB()), new Vertex(matrix4f, f, f2, f3, color.getRGB())));
    }

    public static void renderGradientBox(class_4587 class_45872, class_238 class_2383, Color color, Color color2) {
        if (!rendering) {
            return;
        }
        if (!Render3D.isVisible(class_2383)) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_2383 = Render3D.cameraTransform(class_2383, matrix4f);
        QUADS.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB())));
        QUADS.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB())));
    }

    public static void renderCircle(class_4587 class_45872, class_243 class_2432, double d, Color color) {
        if (!rendering) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2433 = Render3D.aiG.field_1773.method_19418().method_19326();
        int n = 64;
        for (int i = 0; i < n; ++i) {
            double d2 = Math.PI * 2 * (double)i / (double)n;
            double d3 = Math.PI * 2 * (double)(i + 1) / (double)n;
            double d4 = class_2432.field_1352 + d * Math.cos(d2) - class_2433.field_1352;
            double d5 = class_2432.field_1350 + d * Math.sin(d2) - class_2433.field_1350;
            double d6 = class_2432.field_1352 + d * Math.cos(d3) - class_2433.field_1352;
            double d7 = class_2432.field_1350 + d * Math.sin(d3) - class_2433.field_1350;
            double d8 = class_2432.field_1351 - class_2433.field_1351;
            DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)d4, (float)d8, (float)d5, color.getRGB()), new Vertex(matrix4f, (float)d6, (float)d8, (float)d7, color.getRGB())));
        }
    }

    public static void renderGradientBoxOutline(class_4587 class_45872, class_238 class_2383, Color color, Color color2) {
        if (!rendering) {
            return;
        }
        if (!Render3D.isVisible(class_2383)) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_2383 = Render3D.cameraTransform(class_2383, matrix4f);
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1321, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1321, color.getRGB())));
        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1320, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1322, (float)class_2383.field_1324, color2.getRGB()), new Vertex(matrix4f, (float)class_2383.field_1323, (float)class_2383.field_1325, (float)class_2383.field_1324, color.getRGB())));
    }

    public static void drawQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, Color color, boolean bl) {
        if (!rendering) {
            return;
        }
        if (class_45872 == null || class_2432 == null || class_2433 == null || class_2434 == null || class_2435 == null || color == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2436 = Render3D.aiG.field_1773.method_19418().method_19326();
        Vertex vertex = new Vertex(matrix4f, (float)(class_2432.field_1352 - class_2436.field_1352), (float)(class_2432.field_1351 - class_2436.field_1351), (float)(class_2432.field_1350 - class_2436.field_1350), color.getRGB());
        Vertex vertex2 = new Vertex(matrix4f, (float)(class_2433.field_1352 - class_2436.field_1352), (float)(class_2433.field_1351 - class_2436.field_1351), (float)(class_2433.field_1350 - class_2436.field_1350), color.getRGB());
        Vertex vertex3 = new Vertex(matrix4f, (float)(class_2434.field_1352 - class_2436.field_1352), (float)(class_2434.field_1351 - class_2436.field_1351), (float)(class_2434.field_1350 - class_2436.field_1350), color.getRGB());
        Vertex vertex4 = new Vertex(matrix4f, (float)(class_2435.field_1352 - class_2436.field_1352), (float)(class_2435.field_1351 - class_2436.field_1351), (float)(class_2435.field_1350 - class_2436.field_1350), color.getRGB());
        (bl ? DEPTH_QUADS : QUADS).add(new VertexCollection(vertex, vertex2, vertex3, vertex4));
    }

    public static void drawQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, int n, boolean bl) {
        Render3D.drawQuad(class_45872, class_2432, class_2433, class_2434, class_2435, Render3D.fromArgb(n), bl);
    }

    public static void drawLightningQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, int n) {
        if (!rendering) {
            return;
        }
        if (class_45872 == null || class_2432 == null || class_2433 == null || class_2434 == null || class_2435 == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2436 = Render3D.aiG.field_1773.method_19418().method_19326();
        Vertex vertex = new Vertex(matrix4f, (float)(class_2432.field_1352 - class_2436.field_1352), (float)(class_2432.field_1351 - class_2436.field_1351), (float)(class_2432.field_1350 - class_2436.field_1350), n);
        Vertex vertex2 = new Vertex(matrix4f, (float)(class_2433.field_1352 - class_2436.field_1352), (float)(class_2433.field_1351 - class_2436.field_1351), (float)(class_2433.field_1350 - class_2436.field_1350), n);
        Vertex vertex3 = new Vertex(matrix4f, (float)(class_2434.field_1352 - class_2436.field_1352), (float)(class_2434.field_1351 - class_2436.field_1351), (float)(class_2434.field_1350 - class_2436.field_1350), n);
        Vertex vertex4 = new Vertex(matrix4f, (float)(class_2435.field_1352 - class_2436.field_1352), (float)(class_2435.field_1351 - class_2436.field_1351), (float)(class_2435.field_1350 - class_2436.field_1350), n);
        LIGHTNING_QUADS.add(new VertexCollection(vertex, vertex2, vertex3, vertex4));
    }

    public static void drawLightning() {
        if (LIGHTNING_QUADS.isEmpty()) {
            return;
        }
        Render3D.drawDepth(LIGHTNING_QUADS, List.of(), true);
    }

    public static void drawGradientQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, Color color, Color color2, Color color3, Color color4, boolean bl) {
        if (!rendering) {
            return;
        }
        if (class_45872 == null || class_2432 == null || class_2433 == null || class_2434 == null || class_2435 == null || color == null || color2 == null || color3 == null || color4 == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2436 = Render3D.aiG.field_1773.method_19418().method_19326();
        Vertex vertex = new Vertex(matrix4f, (float)(class_2432.field_1352 - class_2436.field_1352), (float)(class_2432.field_1351 - class_2436.field_1351), (float)(class_2432.field_1350 - class_2436.field_1350), color.getRGB());
        Vertex vertex2 = new Vertex(matrix4f, (float)(class_2433.field_1352 - class_2436.field_1352), (float)(class_2433.field_1351 - class_2436.field_1351), (float)(class_2433.field_1350 - class_2436.field_1350), color2.getRGB());
        Vertex vertex3 = new Vertex(matrix4f, (float)(class_2434.field_1352 - class_2436.field_1352), (float)(class_2434.field_1351 - class_2436.field_1351), (float)(class_2434.field_1350 - class_2436.field_1350), color3.getRGB());
        Vertex vertex4 = new Vertex(matrix4f, (float)(class_2435.field_1352 - class_2436.field_1352), (float)(class_2435.field_1351 - class_2436.field_1351), (float)(class_2435.field_1350 - class_2436.field_1350), color4.getRGB());
        (bl ? DEPTH_QUADS : QUADS).add(new VertexCollection(vertex, vertex2, vertex3, vertex4));
    }

    public static void drawGradientQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, int n, int n2, int n3, int n4, boolean bl) {
        Render3D.drawGradientQuad(class_45872, class_2432, class_2433, class_2434, class_2435, Render3D.fromArgb(n), Render3D.fromArgb(n2), Render3D.fromArgb(n3), Render3D.fromArgb(n4), bl);
    }

    /**
     * Камера-относительный градиентный квад: 12 float вместо четырёх Vector3d и
     * ARGB-цвета вместо java.awt.Color — путь модулей частиц без аллокаций.
     */
    public static void drawGradientQuadRel(class_4587 class_45872, boolean bl,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            int c0, int c1, int c2, int c3) {
        if (!rendering || class_45872 == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        List<VertexCollection> list = bl ? DEPTH_QUADS : QUADS;
        list.add(new VertexCollection(
            new Vertex(matrix4f, x0, y0, z0, c0),
            new Vertex(matrix4f, x1, y1, z1, c1),
            new Vertex(matrix4f, x2, y2, z2, c2),
            new Vertex(matrix4f, x3, y3, z3, c3)));
    }

    public static void drawBox(class_4587 class_45872, class_238 class_2383, Color color, float f, boolean bl, boolean bl2, boolean bl3) {
        if (!rendering) {
            return;
        }
        if (class_45872 == null || class_2383 == null || color == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2432 = Render3D.aiG.field_1773.method_19418().method_19326();
        class_238 class_2384 = class_2383.method_1014(0.001);
        float f2 = (float)(class_2384.field_1323 - class_2432.field_1352);
        float f3 = (float)(class_2384.field_1322 - class_2432.field_1351);
        float f4 = (float)(class_2384.field_1321 - class_2432.field_1350);
        float f5 = (float)(class_2384.field_1320 - class_2432.field_1352);
        float f6 = (float)(class_2384.field_1325 - class_2432.field_1351);
        float f7 = (float)(class_2384.field_1324 - class_2432.field_1350);
        int n = color.getRGB();
        if (bl2) {
            int n2 = Render3D.multiplyAlpha(n, 0.1f);
            List<VertexCollection> quadList = bl3 ? DEPTH_QUADS : QUADS;
            Render3D.quad(quadList, matrix4f, f2, f3, f4, f5, f3, f4, f5, f3, f7, f2, f3, f7, n2);
            Render3D.quad(quadList, matrix4f, f2, f3, f4, f2, f6, f4, f5, f6, f4, f5, f3, f4, n2);
            Render3D.quad(quadList, matrix4f, f5, f3, f4, f5, f6, f4, f5, f6, f7, f5, f3, f7, n2);
            Render3D.quad(quadList, matrix4f, f2, f3, f7, f5, f3, f7, f5, f6, f7, f2, f6, f7, n2);
            Render3D.quad(quadList, matrix4f, f2, f3, f4, f2, f3, f7, f2, f6, f7, f2, f6, f4, n2);
            Render3D.quad(quadList, matrix4f, f2, f6, f4, f2, f6, f7, f5, f6, f7, f5, f6, f4, n2);
        }
        if (bl) {
            float f8 = Math.max(1.0f, f);
            List<VariableLine> lineList = bl3 ? DEPTH_VARIABLE_LINES : VARIABLE_LINES;
            Render3D.edge(lineList, matrix4f, f2, f3, f4, f5, f3, f4, n, f8);
            Render3D.edge(lineList, matrix4f, f5, f3, f4, f5, f3, f7, n, f8);
            Render3D.edge(lineList, matrix4f, f5, f3, f7, f2, f3, f7, n, f8);
            Render3D.edge(lineList, matrix4f, f2, f3, f7, f2, f3, f4, n, f8);
            Render3D.edge(lineList, matrix4f, f2, f3, f7, f2, f6, f7, n, f8);
            Render3D.edge(lineList, matrix4f, f2, f3, f4, f2, f6, f4, n, f8);
            Render3D.edge(lineList, matrix4f, f5, f3, f7, f5, f6, f7, n, f8);
            Render3D.edge(lineList, matrix4f, f5, f3, f4, f5, f6, f4, n, f8);
            Render3D.edge(lineList, matrix4f, f2, f6, f4, f5, f6, f4, n, f8);
            Render3D.edge(lineList, matrix4f, f5, f6, f4, f5, f6, f7, n, f8);
            Render3D.edge(lineList, matrix4f, f5, f6, f7, f2, f6, f7, n, f8);
            Render3D.edge(lineList, matrix4f, f2, f6, f7, f2, f6, f4, n, f8);
        }
    }

    private static void quad(List<VertexCollection> list, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, int n) {
        list.add(new VertexCollection(new Vertex(matrix4f, f, f2, f3, n), new Vertex(matrix4f, f4, f5, f6, n), new Vertex(matrix4f, f7, f8, f9, n), new Vertex(matrix4f, f10, f11, f12, n)));
    }

    private static void edge(List<VariableLine> list, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int n, float f7) {
        list.add(new VariableLine(new Vertex(matrix4f, f, f2, f3, n), new Vertex(matrix4f, f4, f5, f6, n), f7));
    }

    public static void drawBox(class_4587 class_45872, class_238 class_2383, int n, float f, boolean bl, boolean bl2, boolean bl3) {
        Render3D.drawBox(class_45872, class_2383, Render3D.fromArgb(n), f, bl, bl2, bl3);
    }

    /**
     * Draws a true geometry-based neon box. Unlike {@code glLineWidth}, the ribbon
     * width is expressed in screen pixels, so the thickness setting always works.
     */
    public static void drawNeonBox(class_4587 stack, class_238 box, int rgb, float pixelWidth, float alpha) {
        Render3D.drawNeonBox(stack, box, rgb, pixelWidth, alpha, false);
    }

    /**
     * Draws a true geometry-based neon box. Unlike {@code glLineWidth}, the ribbon
     * width is expressed in screen pixels, so the thickness setting always works.
     * With {@code occluded} the ribbons are depth-tested against the world, so the
     * highlight does not shine through walls.
     */
    public static void drawNeonBox(class_4587 stack, class_238 box, int rgb, float pixelWidth, float alpha, boolean occluded) {
        if (!rendering || stack == null || box == null) {
            return;
        }
        float strength = Math.max(0.0f, Math.min(1.0f, alpha));
        if (strength <= 0.002f) {
            return;
        }
        Matrix4f matrix = stack.method_23760().method_23761();
        class_243 camera = Render3D.aiG.field_1773.method_19418().method_19326();
        double minX = box.field_1323;
        double minY = box.field_1322;
        double minZ = box.field_1321;
        double maxX = box.field_1320;
        double maxY = box.field_1325;
        double maxZ = box.field_1324;
        Render3D.neonEdge(matrix, camera, minX, minY, minZ, maxX, minY, minZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, maxX, minY, minZ, maxX, minY, maxZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, maxX, minY, maxZ, minX, minY, maxZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, minX, minY, maxZ, minX, minY, minZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, minX, maxY, minZ, maxX, maxY, minZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, maxX, maxY, minZ, maxX, maxY, maxZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, maxX, maxY, maxZ, minX, maxY, maxZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, minX, maxY, maxZ, minX, maxY, minZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, minX, minY, minZ, minX, maxY, minZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, maxX, minY, minZ, maxX, maxY, minZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, maxX, minY, maxZ, maxX, maxY, maxZ, rgb, pixelWidth, strength, occluded);
        Render3D.neonEdge(matrix, camera, minX, minY, maxZ, minX, maxY, maxZ, rgb, pixelWidth, strength, occluded);
    }

    private static void neonEdge(Matrix4f matrix, class_243 camera, double ax, double ay, double az,
            double bx, double by, double bz, int rgb, float pixelWidth, float alpha, boolean occluded) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-5) {
            return;
        }
        dx /= length;
        dy /= length;
        dz /= length;
        // Геометрическая защита от залива: риббон, проходящий в NEON_LENS_SHELL от
        // объектива, разворачивается в аддитивный клин на весь экран (альфой это не
        // лечится). Смотрим расстояние от камеры до ближайшей точки отрезка.
        double toCamX = camera.field_1352 - ax;
        double toCamY = camera.field_1351 - ay;
        double toCamZ = camera.field_1350 - az;
        double along = toCamX * dx + toCamY * dy + toCamZ * dz;
        double projection = Math.max(0.0, Math.min(length, along));
        double closestX = ax + dx * projection - camera.field_1352;
        double closestY = ay + dy * projection - camera.field_1351;
        double closestZ = az + dz * projection - camera.field_1350;
        if (Math.sqrt(closestX * closestX + closestY * closestY + closestZ * closestZ) < (double)NEON_LENS_SHELL) {
            return;
        }
        float depthA = Render3D.neonDepth(matrix, ax, ay, az, camera);
        float depthB = Render3D.neonDepth(matrix, bx, by, bz, camera);
        int framebufferHeight = Math.max(1, Render3D.aiG.method_22683().method_4506());
        float projectionScale = Math.max(0.01f, lastProjectionMatrix.m11());
        float worldPerPixel = 2.0f / (projectionScale * (float)framebufferHeight);
        double viewX = (ax + bx) * 0.5 - camera.field_1352;
        double viewY = (ay + by) * 0.5 - camera.field_1351;
        double viewZ = (az + bz) * 0.5 - camera.field_1350;
        double sideX = dy * viewZ - dz * viewY;
        double sideY = dz * viewX - dx * viewZ;
        double sideZ = dx * viewY - dy * viewX;
        double sideLength = Math.sqrt(sideX * sideX + sideY * sideY + sideZ * sideZ);
        if (sideLength < 1.0E-5) {
            sideX = -dz;
            sideY = 0.0;
            sideZ = dx;
            sideLength = Math.sqrt(sideX * sideX + sideZ * sideZ);
            if (sideLength < 1.0E-5) {
                sideX = 1.0;
                sideY = 0.0;
                sideZ = 0.0;
                sideLength = 1.0;
            }
        }
        sideX /= sideLength;
        sideY /= sideLength;
        sideZ /= sideLength;

        float width = Math.max(0.5f, pixelWidth);
        float coreHalfA = Math.max(0.15f, depthA) * worldPerPixel * width * 0.5f;
        float coreHalfB = Math.max(0.15f, depthB) * worldPerPixel * width * 0.5f;
        float extension = (coreHalfA + coreHalfB) * 0.5f;
        double startX = ax - dx * extension;
        double startY = ay - dy * extension;
        double startZ = az - dz * extension;
        double endX = bx + dx * extension;
        double endY = by + dy * extension;
        double endZ = bz + dz * extension;

        float[] widthScale = NEON_WIDTH_SCALE;
        float[] alphaScale = NEON_ALPHA_SCALE;
        float[] whiteMix = NEON_WHITE_MIX;
        for (int i = 0; i < widthScale.length; ++i) {
            float layerWidth = width * widthScale[i];
            float halfA = Math.max(0.15f, depthA) * worldPerPixel * layerWidth * 0.5f;
            float halfB = Math.max(0.15f, depthB) * worldPerPixel * layerWidth * 0.5f;
            int layerAlpha = Math.round(255.0f * alpha * alphaScale[i]);
            if (layerAlpha <= 0) {
                continue;
            }
            int color = layerAlpha << 24 | Render3D.mixRgb(rgb, 0xFFFFFF, whiteMix[i]);
            Render3D.neonQuad(matrix, camera, startX, startY, startZ, endX, endY, endZ,
                    sideX, sideY, sideZ, halfA, halfB, color, occluded);
        }
    }

    private static void neonQuad(Matrix4f matrix, class_243 camera,
            double ax, double ay, double az, double bx, double by, double bz,
            double sideX, double sideY, double sideZ, float halfA, float halfB, int color, boolean occluded) {
        List<VertexCollection> quads = occluded ? NEON_QUADS_DEPTH : NEON_QUADS;
        quads.add(new VertexCollection(
                new Vertex(matrix, (float)(ax - sideX * halfA - camera.field_1352),
                        (float)(ay - sideY * halfA - camera.field_1351),
                        (float)(az - sideZ * halfA - camera.field_1350), color),
                new Vertex(matrix, (float)(ax + sideX * halfA - camera.field_1352),
                        (float)(ay + sideY * halfA - camera.field_1351),
                        (float)(az + sideZ * halfA - camera.field_1350), color),
                new Vertex(matrix, (float)(bx + sideX * halfB - camera.field_1352),
                        (float)(by + sideY * halfB - camera.field_1351),
                        (float)(bz + sideZ * halfB - camera.field_1350), color),
                new Vertex(matrix, (float)(bx - sideX * halfB - camera.field_1352),
                        (float)(by - sideY * halfB - camera.field_1351),
                        (float)(bz - sideZ * halfB - camera.field_1350), color)));
    }

    private static float neonDepth(Matrix4f matrix, double x, double y, double z, class_243 camera) {
        Vector4f vector = new Vector4f((float)(x - camera.field_1352), (float)(y - camera.field_1351),
                (float)(z - camera.field_1350), 1.0f);
        vector.mul((Matrix4fc)matrix);
        return Math.max(0.1f, Math.abs(vector.z()));
    }

    private static int mixRgb(int from, int to, float amount) {
        float t = Math.max(0.0f, Math.min(1.0f, amount));
        int fromRed = from >> 16 & 0xFF;
        int fromGreen = from >> 8 & 0xFF;
        int fromBlue = from & 0xFF;
        int toRed = to >> 16 & 0xFF;
        int toGreen = to >> 8 & 0xFF;
        int toBlue = to & 0xFF;
        return (int)((float)fromRed + (float)(toRed - fromRed) * t) << 16
                | (int)((float)fromGreen + (float)(toGreen - fromGreen) * t) << 8
                | (int)((float)fromBlue + (float)(toBlue - fromBlue) * t);
    }

    /**
     * Soft additive glow ribbon between two world points, used as a lightweight
     * bloom pass around wide accent strokes so they melt into the scene instead
     * of reading as hard strips. The ribbon keeps a constant world width.
     */
    public static void drawNeonLine(class_4587 stack, class_243 first, class_243 second, int rgb, float worldWidth, float alpha) {
        if (!Render3D.rendering || stack == null || first == null || second == null) {
            return;
        }
        float strength = Math.max(0.0f, Math.min(1.0f, alpha));
        if (strength <= 0.003f || worldWidth <= 0.0f) {
            return;
        }
        Matrix4f matrix = stack.method_23760().method_23761();
        class_243 camera = Render3D.aiG.field_1773.method_19418().method_19326();
        float depthA = Render3D.neonDepth(matrix, first.field_1352, first.field_1351, first.field_1350, camera);
        float depthB = Render3D.neonDepth(matrix, second.field_1352, second.field_1351, second.field_1350, camera);
        int framebufferHeight = Math.max(1, Render3D.aiG.method_22683().method_4506());
        float projectionScale = Math.max(0.01f, Render3D.lastProjectionMatrix.m11());
        float worldPerPixel = 2.0f / (projectionScale * (float)framebufferHeight);
        float averageDepth = Math.max(0.1f, (depthA + depthB) * 0.5f);
        float pixelWidth = Math.max(0.6f, worldWidth / (averageDepth * worldPerPixel));
        Render3D.neonEdge(matrix, camera, first.field_1352, first.field_1351, first.field_1350,
                second.field_1352, second.field_1351, second.field_1350, rgb, pixelWidth, strength, false);
    }

    /**
     * Additive quad flushed after the texture pass, so glows paint neatly over
     * particles and props. Used for beams, flashes and soft shockwave fills.
     */
    public static void drawGlowQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, int n, boolean bl) {
        if (!Render3D.rendering || class_45872 == null || class_2432 == null || class_2433 == null || class_2434 == null || class_2435 == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2436 = Render3D.aiG.field_1773.method_19418().method_19326();
        Vertex vertex = new Vertex(matrix4f, (float)(class_2432.field_1352 - class_2436.field_1352), (float)(class_2432.field_1351 - class_2436.field_1351), (float)(class_2432.field_1350 - class_2436.field_1350), n);
        Vertex vertex2 = new Vertex(matrix4f, (float)(class_2433.field_1352 - class_2436.field_1352), (float)(class_2433.field_1351 - class_2436.field_1351), (float)(class_2433.field_1350 - class_2436.field_1350), n);
        Vertex vertex3 = new Vertex(matrix4f, (float)(class_2434.field_1352 - class_2436.field_1352), (float)(class_2434.field_1351 - class_2436.field_1351), (float)(class_2434.field_1350 - class_2436.field_1350), n);
        Vertex vertex4 = new Vertex(matrix4f, (float)(class_2435.field_1352 - class_2436.field_1352), (float)(class_2435.field_1351 - class_2436.field_1351), (float)(class_2435.field_1350 - class_2436.field_1350), n);
        (bl ? DEPTH_SHINE_QUADS : SHINE_QUADS).add(new VertexCollection(vertex, vertex2, vertex3, vertex4));
    }

    /**
     * Additive gradient quad with per-corner colors, used for beams that fade
     * with height and for glows with soft edges instead of hard rims.
     */
    public static void drawGlowGradientQuad(class_4587 class_45872, class_243 class_2432, class_243 class_2433, class_243 class_2434, class_243 class_2435, int n, int n2, int n3, int n4, boolean bl) {
        if (!Render3D.rendering || class_45872 == null || class_2432 == null || class_2433 == null || class_2434 == null || class_2435 == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2436 = Render3D.aiG.field_1773.method_19418().method_19326();
        Vertex vertex = new Vertex(matrix4f, (float)(class_2432.field_1352 - class_2436.field_1352), (float)(class_2432.field_1351 - class_2436.field_1351), (float)(class_2432.field_1350 - class_2436.field_1350), n);
        Vertex vertex2 = new Vertex(matrix4f, (float)(class_2433.field_1352 - class_2436.field_1352), (float)(class_2433.field_1351 - class_2436.field_1351), (float)(class_2433.field_1350 - class_2436.field_1350), n2);
        Vertex vertex3 = new Vertex(matrix4f, (float)(class_2434.field_1352 - class_2436.field_1352), (float)(class_2434.field_1351 - class_2436.field_1351), (float)(class_2434.field_1350 - class_2436.field_1350), n3);
        Vertex vertex4 = new Vertex(matrix4f, (float)(class_2435.field_1352 - class_2436.field_1352), (float)(class_2435.field_1351 - class_2436.field_1351), (float)(class_2435.field_1350 - class_2436.field_1350), n4);
        (bl ? DEPTH_SHINE_QUADS : SHINE_QUADS).add(new VertexCollection(vertex, vertex2, vertex3, vertex4));
    }

    public static void drawShape(class_4587 class_45872, class_2338 class_23382, class_265 class_2652, Color color, float f, boolean bl, boolean bl2) {
        if (!rendering) {
            return;
        }
        if (class_45872 == null || class_23382 == null || class_2652 == null || class_2652.method_1110() || color == null) {
            return;
        }
        ShapeCache shapeCache = SHAPE_CACHE.computeIfAbsent(class_2652, Render3D::buildShapeCache);
        double d = class_23382.method_10263();
        double d2 = class_23382.method_10264();
        double d3 = class_23382.method_10260();
        if (bl) {
            for (class_238 class_24322 : shapeCache.boxes()) {
                Render3D.drawBox(class_45872, class_24322.method_989(d, d2, d3), color, f, false, true, bl2);
            }
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 class_2432 = Render3D.aiG.field_1773.method_19418().method_19326();
        int n = color.getRGB();
        float f2 = Math.max(1.0f, f);
        List<VariableLine> list = bl2 ? DEPTH_VARIABLE_LINES : VARIABLE_LINES;
        for (ShapeLine shapeLine : shapeCache.lines()) {
            Render3D.edge(list, matrix4f, (float)(shapeLine.start().field_1352 + d - class_2432.field_1352), (float)(shapeLine.start().field_1351 + d2 - class_2432.field_1351), (float)(shapeLine.start().field_1350 + d3 - class_2432.field_1350), (float)(shapeLine.end().field_1352 + d - class_2432.field_1352), (float)(shapeLine.end().field_1351 + d2 - class_2432.field_1351), (float)(shapeLine.end().field_1350 + d3 - class_2432.field_1350), n, f2);
        }
    }

    public static void drawShape(class_4587 class_45872, class_2338 class_23382, class_265 class_2652, int n, float f, boolean bl, boolean bl2) {
        Render3D.drawShape(class_45872, class_23382, class_2652, Render3D.fromArgb(n), f, bl, bl2);
    }

    private static ShapeCache buildShapeCache(class_265 class_2652) {
        List list = class_2652.method_1090();
        ArrayList<ShapeLine> arrayList = new ArrayList<ShapeLine>();
        class_2652.method_1104((d, d2, d3, d4, d5, d6) -> arrayList.add(new ShapeLine(new class_243(d, d2, d3), new class_243(d4, d5, d6))));
        return new ShapeCache(list, arrayList);
    }

    private static int multiplyAlpha(int n, float f) {
        int n2 = Math.max(0, Math.min(255, Math.round((float)(n >>> 24 & 0xFF) * f)));
        return n2 << 24 | n & 0xFFFFFF;
    }

    private static Color fromArgb(int n) {
        return new Color(n, true);
    }

    public static void prepare() {
        Render3D.releaseQuads(TEXTURED_QUADS);
        Render3D.releaseQuads(TEXTURED_QUADS_ADDITIVE);
        Render3D.releaseQuads(TEXTURED_QUADS_DEPTH);
        Render3D.releaseQuads(TEXTURED_QUADS_DEPTH_ADDITIVE);
        Render3D.releaseCustomQuads(CUSTOM_UV_QUADS);
        Render3D.releaseCustomQuads(DEPTH_CUSTOM_UV_QUADS);
        QUADS.clear();
        DEPTH_QUADS.clear();
        LIGHTNING_QUADS.clear();
        DEBUG_LINES.clear();
        DEPTH_DEBUG_LINES.clear();
        SHINE_QUADS.clear();
        DEPTH_SHINE_QUADS.clear();
        NEON_QUADS.clear();
        SHINE_DEBUG_LINES.clear();
        DEPTH_SHINE_DEBUG_LINES.clear();
        VARIABLE_LINES.clear();
        DEPTH_VARIABLE_LINES.clear();
        rendering = true;
    }

    private static void releaseQuads(List<TexturedQuad> list) {
        for (int i = 0; i < list.size(); i++) {
            TEXTURED_QUAD_POOL.offer(list.get(i));
        }
        list.clear();
    }

    private static void releaseCustomQuads(List<CustomTexturedQuad> list) {
        for (int i = 0; i < list.size(); i++) {
            CUSTOM_QUAD_POOL.offer(list.get(i));
        }
        list.clear();
    }

    private static TexturedQuad acquireTexturedQuad() {
        TexturedQuad quad = TEXTURED_QUAD_POOL.poll();
        return quad != null ? quad : new TexturedQuad();
    }

    private static CustomTexturedQuad acquireCustomQuad() {
        CustomTexturedQuad quad = CUSTOM_QUAD_POOL.poll();
        return quad != null ? quad : new CustomTexturedQuad();
    }

    /**
     * Строит матрицу билборд-поворота камеры один раз за кадр: R = Ry(-yaw) * Rx(pitch).
     * Квад частицы затем собирает финальную матрицу как translate * R без
     * MatrixStack-операций на каждую частицу.
     */
    private static void updateBillboardRotation(float yaw, float pitch) {
        if (Float.compare(yaw, billboardYaw) != 0 || Float.compare(pitch, billboardPitch) != 0) {
            billboardYaw = yaw;
            billboardPitch = pitch;
            BILLBOARD_ROT.rotationY(-yaw * DEG_TO_RAD).rotateX(pitch * DEG_TO_RAD);
        }
    }

    private static void enqueueTexturedQuad(TexturedQuad quad, boolean depth, boolean additive) {
        if (depth) {
            (additive ? TEXTURED_QUADS_DEPTH_ADDITIVE : TEXTURED_QUADS_DEPTH).add(quad);
        } else {
            (additive ? TEXTURED_QUADS_ADDITIVE : TEXTURED_QUADS).add(quad);
        }
    }

    public static void draw(List<VertexCollection> list, List<VertexCollection> list2, boolean bl) {
        class_287 class_2872;
        if (list.isEmpty() && list2.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        if (bl) {
            RenderSystem.blendFunc((int)770, (int)32772);
        } else {
            RenderSystem.blendFuncSeparate((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA, (GlStateManager.class_4535)GlStateManager.class_4535.ONE, (GlStateManager.class_4534)GlStateManager.class_4534.ZERO);
        }
        if (!list.isEmpty()) {
            class_2872 = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1576);
            for (VertexCollection vertexCollection : list) {
                vertexCollection.vertex(class_2872);
            }
            RenderSystem.setShader((class_10156)class_10142.field_53876);
            RenderSystem.disableCull();
            class_286.method_43433((class_9801)class_2872.method_60800());
            RenderSystem.enableCull();
        }
        if (!list2.isEmpty()) {
            class_2872 = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_29344, class_290.field_1576);
            for (VertexCollection vertexCollection : list2) {
                vertexCollection.vertex(class_2872);
            }
            RenderSystem.setShader((class_10156)class_10142.field_53876);
            GL11.glHint((int)3154, (int)4354);
            GL11.glEnable((int)2848);
            class_286.method_43433((class_9801)class_2872.method_60800());
            GL11.glDisable((int)2848);
        }
        RenderSystem.disableBlend();
    }

    public static void drawVariableWidthLines(boolean bl) {
        List<VariableLine> list;
        List<VariableLine> list2 = list = bl ? DEPTH_VARIABLE_LINES : VARIABLE_LINES;
        if (list.isEmpty()) {
            return;
        }
        if (bl) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)false);
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA, (GlStateManager.class_4535)GlStateManager.class_4535.ONE, (GlStateManager.class_4534)GlStateManager.class_4534.ZERO);
        Render3D.drawVariableLines(list);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        if (bl) {
            RenderSystem.depthMask((boolean)true);
        } else {
            RenderSystem.enableDepthTest();
        }
        list.clear();
    }

    private static void drawVariableLines(List<VariableLine> list) {
        if (list.isEmpty()) {
            return;
        }
        int n = 0;
        for (VariableLine variableLine : list) {
            float f = variableLine.width();
            boolean bl = false;
            for (int i = 0; i < n; ++i) {
                if (Float.compare(widthScratch[i], f) != 0) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            if (n == widthScratch.length) {
                widthScratch = Arrays.copyOf(widthScratch, n * 2);
            }
            Render3D.widthScratch[n++] = f;
        }
        RenderSystem.setShader((class_10156)class_10142.field_53876);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        for (int i = 0; i < n; ++i) {
            float f = widthScratch[i];
            RenderSystem.lineWidth((float)f);
            class_287 class_2872 = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_29344, class_290.field_1576);
            for (VariableLine variableLine : list) {
                if (Float.compare(variableLine.width(), f) != 0) continue;
                Vertex vertex = variableLine.start();
                Vertex vertex2 = variableLine.end();
                class_2872.method_22918(vertex.matrix, vertex.x, vertex.y, vertex.z).method_39415(vertex.color);
                class_2872.method_22918(vertex2.matrix, vertex2.x, vertex2.y, vertex2.z).method_39415(vertex2.color);
            }
            class_286.method_43433((class_9801)class_2872.method_60800());
        }
        RenderSystem.lineWidth((float)1.0f);
        GL11.glDisable((int)2848);
    }

    public static void drawDepth(List<VertexCollection> list, List<VertexCollection> list2, boolean bl) {
        if (list.isEmpty() && list2.isEmpty()) {
            return;
        }
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        try {
            Render3D.draw(list, list2, bl);
        }
        finally {
            RenderSystem.depthMask((boolean)true);
        }
    }

    /**
     * Flushes the neon pass (camera-facing ribbons, additive glow, no depth test)
     * so outlines also glow through walls like a proper neon wireframe.
     */
    public static void drawNeon() {
        if (NEON_QUADS.isEmpty()) {
            return;
        }
        Render3D.drawNeonPass(NEON_QUADS, false);
        NEON_QUADS.clear();
    }

    /**
     * Same neon pass, but depth-tested: ribbons hide behind solid geometry,
     * so box highlights never shine through walls.
     */
    public static void drawNeonDepth() {
        if (NEON_QUADS_DEPTH.isEmpty()) {
            return;
        }
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        try {
            Render3D.drawNeonPass(NEON_QUADS_DEPTH, true);
        }
        finally {
            RenderSystem.depthMask((boolean)true);
            NEON_QUADS_DEPTH.clear();
        }
    }

    private static void drawNeonPass(List<VertexCollection> quads, boolean depth) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE);
        if (!depth) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask((boolean)false);
        RenderSystem.disableCull();
        RenderSystem.setShader((class_10156)class_10142.field_53876);
        class_287 buffer = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1576);
        for (VertexCollection collection : quads) {
            collection.vertex(buffer);
        }
        class_286.method_43433((class_9801)buffer.method_60800());
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
    }

    public static void renderBillboardTexture(class_243 class_2432, float f, class_2960 class_29602, int n, boolean bl) {
        Render3D.renderBillboardTexture(class_2432, f, class_29602, n, bl, false);
    }

    public static void renderBillboardTexture(class_243 class_2432, float f, class_2960 class_29602, int n, boolean bl, boolean bl2) {
        Render3D.renderBillboardTexture(class_2432.field_1352, class_2432.field_1351, class_2432.field_1350, f, class_29602, n, bl, bl2);
    }

    /**
     * Билборд без Vector3d: координаты передаются напрямую, пути модулей частиц
     * не создают вектор на каждую частицу за кадр.
     */
    public static void renderBillboardTexture(double px, double py, double pz, float f, class_2960 class_29602, int n, boolean bl, boolean bl2) {
        if (!rendering) {
            return;
        }
        class_4184 class_41842 = Render3D.aiG.field_1773.method_19418();
        class_243 class_2433 = class_41842.method_19326();
        Render3D.updateBillboardRotation(class_41842.method_19330(), class_41842.method_19329());
        TexturedQuad texturedQuad = Render3D.acquireTexturedQuad();
        texturedQuad.matrix.translation(
            (float)(px - class_2433.field_1352),
            (float)(py - class_2433.field_1351),
            (float)(pz - class_2433.field_1350)).mul(BILLBOARD_ROT);
        texturedQuad.set(class_29602, -f / 2.0f, -f / 2.0f, f, f, n);
        Render3D.enqueueTexturedQuad(texturedQuad, bl, bl2);
    }

    public static void renderOrientedTexture(class_243 class_2432, float f, class_2960 class_29602, int n, class_2350 class_23502, boolean bl) {
        Render3D.renderOrientedTexture(class_2432, f, class_29602, n, class_23502, bl, false);
    }

    public static void renderOrientedTexture(class_243 class_2432, float f, class_2960 class_29602, int n, class_2350 class_23502, boolean bl, boolean bl2) {
        if (!rendering) {
            return;
        }
        class_243 class_2433 = Render3D.aiG.field_1773.method_19418().method_19326();
        TexturedQuad texturedQuad = Render3D.acquireTexturedQuad();
        texturedQuad.matrix.translation(
            (float)(class_2432.field_1352 - class_2433.field_1352),
            (float)(class_2432.field_1351 - class_2433.field_1351),
            (float)(class_2432.field_1350 - class_2433.field_1350)).mul(FACE_ROTATIONS[class_23502.ordinal()]);
        texturedQuad.set(class_29602, -f / 2.0f, -f / 2.0f, f, f, n);
        Render3D.enqueueTexturedQuad(texturedQuad, bl, bl2);
    }

    public static void drawTexture(class_4587.class_4665 class_46652, class_2960 class_29602, float f, float f2, float f3, float f4, int n, boolean bl) {
        Render3D.drawTexture(class_46652, class_29602, f, f2, f3, f4, n, bl, false);
    }

    public static void drawTexture(class_4587.class_4665 class_46652, class_2960 class_29602, float f, float f2, float f3, float f4, int n, boolean bl, boolean bl2) {
        if (!rendering || class_46652 == null || class_29602 == null) {
            return;
        }
        TexturedQuad texturedQuad = Render3D.acquireTexturedQuad();
        texturedQuad.matrix.set((Matrix4fc)class_46652.method_23761());
        texturedQuad.set(class_29602, f, f2, f3, f4, n);
        Render3D.enqueueTexturedQuad(texturedQuad, bl, bl2);
    }

    private static Matrix4f[] buildFaceRotations() {
        Matrix4f[] arr = new Matrix4f[6];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new Matrix4f();
        }
        arr[class_2350.field_11036.ordinal()].rotationX(-DEG_TO_RAD * 90.0f);
        arr[class_2350.field_11033.ordinal()].rotationX(DEG_TO_RAD * 90.0f);
        arr[class_2350.field_11043.ordinal()].rotationY(DEG_TO_RAD * 180.0f);
        arr[class_2350.field_11035.ordinal()].identity();
        arr[class_2350.field_11034.ordinal()].rotationY(-DEG_TO_RAD * 90.0f);
        arr[class_2350.field_11039.ordinal()].rotationY(DEG_TO_RAD * 90.0f);
        return arr;
    }

    public static void drawTextures() {
        Render3D.drawTexturedQuads(TEXTURED_QUADS, false, false, false);
        Render3D.drawTexturedQuads(TEXTURED_QUADS_ADDITIVE, false, true, false);
        Render3D.drawTexturedQuads(TEXTURED_QUADS_DEPTH, true, false, false);
        Render3D.drawTexturedQuads(TEXTURED_QUADS_DEPTH_ADDITIVE, true, true, false);
        Render3D.drawCustomUvQuads(CUSTOM_UV_QUADS, false);
        Render3D.drawCustomUvQuads(DEPTH_CUSTOM_UV_QUADS, true);
    }

    public static void drawDepthTexturesPreFog() {
    }

    private static void drawTexturedQuads(List<TexturedQuad> list, boolean bl, boolean bl2, boolean bl3) {
        if (list.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        if (bl2) {
            RenderSystem.blendFunc((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE);
        } else {
            RenderSystem.blendFunc((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA);
        }
        if (bl) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)bl3);
        } else {
            RenderSystem.disableDepthTest();
        }
        textureBatches.clear();
        for (TexturedQuad quad : list) {
            textureBatches.computeIfAbsent(quad.texture(), ignored -> new ArrayList<>()).add(quad);
        }
        for (Map.Entry<class_2960, List<TexturedQuad>> entry : textureBatches.entrySet()) {
            List<TexturedQuad> batch = entry.getValue();
            if (batch.isEmpty()) continue;
            RenderSystem.setShaderTexture(0, entry.getKey());
            RenderSystem.setShader((class_10156)class_10142.field_53880);
            class_287 buffer = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
            for (TexturedQuad texturedQuad : batch) {
                Render3D.emitTexturedQuad(buffer, texturedQuad);
            }
            class_286.method_43433((class_9801)buffer.method_60800());
        }
        if (bl) {
            RenderSystem.depthMask((boolean)true);
        } else {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        Render3D.releaseQuads(list);
    }

    private static void emitTexturedQuad(class_287 class_2872, TexturedQuad texturedQuad) {
        Matrix4f matrix4f = texturedQuad.matrix();
        float f = texturedQuad.x();
        float f2 = texturedQuad.y();
        float f3 = texturedQuad.width();
        float f4 = texturedQuad.height();
        int n = texturedQuad.red();
        int n2 = texturedQuad.green();
        int n3 = texturedQuad.blue();
        int n4 = texturedQuad.alpha();
        class_2872.method_22918(matrix4f, f, f2 + f4, 0.0f).method_22913(0.0f, 1.0f).method_1336(n, n2, n3, n4);
        class_2872.method_22918(matrix4f, f + f3, f2 + f4, 0.0f).method_22913(1.0f, 1.0f).method_1336(n, n2, n3, n4);
        class_2872.method_22918(matrix4f, f + f3, f2, 0.0f).method_22913(1.0f, 0.0f).method_1336(n, n2, n3, n4);
        class_2872.method_22918(matrix4f, f, f2, 0.0f).method_22913(0.0f, 0.0f).method_1336(n, n2, n3, n4);
    }

    public static void drawTexturedQuadCustom(class_4587 class_45872, class_2960 texture,
            class_243 p0, float u0, float v0, int c0,
            class_243 p1, float u1, float v1, int c1,
            class_243 p2, float u2, float v2, int c2,
            class_243 p3, float u3, float v3, int c3,
            boolean depth) {
        if (!rendering || class_45872 == null || texture == null) {
            return;
        }
        Matrix4f matrix4f = class_45872.method_23760().method_23761();
        class_243 cam = Render3D.aiG.field_1773.method_19418().method_19326();
        CustomTexturedQuad quad = Render3D.acquireCustomQuad();
        quad.matrix.set((Matrix4fc)matrix4f);
        quad.set(texture,
            (float) (p0.field_1352 - cam.field_1352), (float) (p0.field_1351 - cam.field_1351), (float) (p0.field_1350 - cam.field_1350), u0, v0, c0,
            (float) (p1.field_1352 - cam.field_1352), (float) (p1.field_1351 - cam.field_1351), (float) (p1.field_1350 - cam.field_1350), u1, v1, c1,
            (float) (p2.field_1352 - cam.field_1352), (float) (p2.field_1351 - cam.field_1351), (float) (p2.field_1350 - cam.field_1350), u2, v2, c2,
            (float) (p3.field_1352 - cam.field_1352), (float) (p3.field_1351 - cam.field_1351), (float) (p3.field_1350 - cam.field_1350), u3, v3, c3);
        (depth ? DEPTH_CUSTOM_UV_QUADS : CUSTOM_UV_QUADS).add(quad);
    }

    /**
     * Камера-относительный вариант drawTexturedQuadCustom: координаты уже посчитаны
     * как (мир - камера), поэтому пути модулей не создают Vector3d на каждый угол.
     */
    public static void drawTexturedQuadCustomRel(class_4587 class_45872, class_2960 texture,
            float x0, float y0, float z0, float u0, float v0, int c0,
            float x1, float y1, float z1, float u1, float v1, int c1,
            float x2, float y2, float z2, float u2, float v2, int c2,
            float x3, float y3, float z3, float u3, float v3, int c3,
            boolean depth) {
        if (!rendering || class_45872 == null || texture == null) {
            return;
        }
        CustomTexturedQuad quad = Render3D.acquireCustomQuad();
        quad.matrix.set((Matrix4fc)class_45872.method_23760().method_23761());
        quad.set(texture, x0, y0, z0, u0, v0, c0, x1, y1, z1, u1, v1, c1, x2, y2, z2, u2, v2, c2, x3, y3, z3, u3, v3, c3);
        (depth ? DEPTH_CUSTOM_UV_QUADS : CUSTOM_UV_QUADS).add(quad);
    }

    private static final Map<class_2960, List<CustomTexturedQuad>> customTextureBatches = new LinkedHashMap<class_2960, List<CustomTexturedQuad>>();

    private static void drawCustomUvQuads(List<CustomTexturedQuad> list, boolean depth) {
        if (list.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.class_4535)GlStateManager.class_4535.SRC_ALPHA, (GlStateManager.class_4534)GlStateManager.class_4534.ONE_MINUS_SRC_ALPHA);
        if (depth) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean) false);
        } else {
            RenderSystem.disableDepthTest();
        }
        customTextureBatches.clear();
        for (CustomTexturedQuad quad : list) {
            customTextureBatches.computeIfAbsent(quad.texture(), ignored -> new ArrayList<>()).add(quad);
        }
        for (Map.Entry<class_2960, List<CustomTexturedQuad>> entry : customTextureBatches.entrySet()) {
            List<CustomTexturedQuad> batch = entry.getValue();
            if (batch.isEmpty()) continue;
            RenderSystem.setShaderTexture(0, entry.getKey());
            RenderSystem.setShader((class_10156)class_10142.field_53880);
            class_287 buffer = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
            for (CustomTexturedQuad quad : batch) {
                Render3D.emitCustomUvQuad(buffer, quad);
            }
            class_286.method_43433((class_9801)buffer.method_60800());
        }
        if (depth) {
            RenderSystem.depthMask((boolean) true);
        } else {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        Render3D.releaseCustomQuads(list);
    }

    private static void emitCustomUvQuad(class_287 buffer, CustomTexturedQuad quad) {
        Render3D.emitCustomVertex(buffer, quad.matrix(), quad.x0(), quad.y0(), quad.z0(), quad.u0(), quad.v0(), quad.c0());
        Render3D.emitCustomVertex(buffer, quad.matrix(), quad.x1(), quad.y1(), quad.z1(), quad.u1(), quad.v1(), quad.c1());
        Render3D.emitCustomVertex(buffer, quad.matrix(), quad.x2(), quad.y2(), quad.z2(), quad.u2(), quad.v2(), quad.c2());
        Render3D.emitCustomVertex(buffer, quad.matrix(), quad.x3(), quad.y3(), quad.z3(), quad.u3(), quad.v3(), quad.c3());
    }

    private static void emitCustomVertex(class_287 buffer, Matrix4f matrix, float x, float y, float z, float u, float v, int color) {
        buffer.method_22918(matrix, x, y, z).method_22913(u, v).method_1336(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, color >>> 24 & 0xFF);
    }

    private static boolean isVisible(class_238 class_2383) {
        return ((IWorldRenderer)Render3D.aiG.field_1769).getFrustum().method_23093(class_2383);
    }

    private static class_243 cameraTransform(class_243 class_2432, Matrix4f matrix4f) {
        class_243 class_2433 = Render3D.aiG.field_1773.method_19418().method_19326();
        Vector4f vector4f = new Vector4f((float)(class_2432.field_1352 - class_2433.field_1352), (float)(class_2432.field_1351 - class_2433.field_1351), (float)(class_2432.field_1350 - class_2433.field_1350), 1.0f);
        vector4f.mul((Matrix4fc)matrix4f);
        return new class_243((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
    }

    private static class_238 cameraTransform(class_238 class_2383, Matrix4f matrix4f) {
        class_243 class_2432 = Render3D.cameraTransform(new class_243(class_2383.field_1323, class_2383.field_1322, class_2383.field_1321), matrix4f);
        class_243 class_2433 = Render3D.cameraTransform(new class_243(class_2383.field_1320, class_2383.field_1325, class_2383.field_1324), matrix4f);
        return new class_238(class_2432, class_2433);
    }

    @Generated
    private Render3D() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static final class VertexCollection {
        private final Vertex[] vertices;

        VertexCollection(Vertex... vertices) {
            this.vertices = vertices;
        }

        public void vertex(class_287 class_2872) {
            for (Vertex vertex : this.vertices) {
                class_2872.method_22918(vertex.matrix, vertex.x, vertex.y, vertex.z).method_39415(vertex.color);
            }
        }
    }

    static final class Vertex {
        final Matrix4f matrix;
        final float x;
        final float y;
        final float z;
        final int color;

        Vertex(Matrix4f matrix4f, float f, float f2, float f3, int n) {
            this.matrix = matrix4f;
            this.x = f;
            this.y = f2;
            this.z = f3;
            this.color = n;
        }

        @Override
        public final String toString() {
            return "Vertex[" + "matrix=" + this.matrix + ", " + "x=" + this.x + ", " + "y=" + this.y + ", " + "z=" + this.z + ", " + "color=" + this.color + "]";
        }

        @Override
        public final int hashCode() {
            return java.util.Objects.hash(this.matrix, this.x, this.y, this.z, this.color);
        }

        @Override
        public final boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Vertex other)) return false;
            return java.util.Objects.deepEquals(this.matrix, other.matrix)
                && java.util.Objects.deepEquals(this.x, other.x)
                && java.util.Objects.deepEquals(this.y, other.y)
                && java.util.Objects.deepEquals(this.z, other.z)
                && java.util.Objects.deepEquals(this.color, other.color);
        }

        public Matrix4f matrix() {
            return this.matrix;
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public float z() {
            return this.z;
        }

        public int color() {
            return this.color;
        }
    }

    record VariableLine(Vertex start, Vertex end, float width) {
    }

    record ShapeCache(List<class_238> boxes, List<ShapeLine> lines) {
    }

    record ShapeLine(class_243 start, class_243 end) {
    }

    /**
     * Текстурированный квад с собственной матрицей: set() копирует матрицу, чтобы квад
     * не держал ссылку на live-стек вызывателя (между add и flush стек меняется).
     * Экземпляры переиспользуются из TEXTURED_QUAD_POOL.
     */
    static final class TexturedQuad {
        final Matrix4f matrix = new Matrix4f();
        class_2960 texture;
        float x;
        float y;
        float width;
        float height;
        int red;
        int green;
        int blue;
        int alpha;

        TexturedQuad() {
        }

        void set(class_2960 texture, float x, float y, float width, float height, int argb) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.red = argb >>> 16 & 0xFF;
            this.green = argb >>> 8 & 0xFF;
            this.blue = argb & 0xFF;
            this.alpha = argb >>> 24 & 0xFF;
        }

        public Matrix4f matrix() {
            return this.matrix;
        }

        public class_2960 texture() {
            return this.texture;
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public float width() {
            return this.width;
        }

        public float height() {
            return this.height;
        }

        public int red() {
            return this.red;
        }

        public int green() {
            return this.green;
        }

        public int blue() {
            return this.blue;
        }

        public int alpha() {
            return this.alpha;
        }
    }

    static final class CustomTexturedQuad {
        final Matrix4f matrix = new Matrix4f();
        class_2960 texture;
        float x0;
        float y0;
        float z0;
        float u0;
        float v0;
        int c0;
        float x1;
        float y1;
        float z1;
        float u1;
        float v1;
        int c1;
        float x2;
        float y2;
        float z2;
        float u2;
        float v2;
        int c2;
        float x3;
        float y3;
        float z3;
        float u3;
        float v3;
        int c3;

        CustomTexturedQuad() {
        }

        void set(class_2960 texture,
                 float x0, float y0, float z0, float u0, float v0, int c0,
                 float x1, float y1, float z1, float u1, float v1, int c1,
                 float x2, float y2, float z2, float u2, float v2, int c2,
                 float x3, float y3, float z3, float u3, float v3, int c3) {
            this.texture = texture;
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.u0 = u0;
            this.v0 = v0;
            this.c0 = c0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.u1 = u1;
            this.v1 = v1;
            this.c1 = c1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.u2 = u2;
            this.v2 = v2;
            this.c2 = c2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;
            this.u3 = u3;
            this.v3 = v3;
            this.c3 = c3;
        }

        public Matrix4f matrix() {
            return this.matrix;
        }

        public class_2960 texture() {
            return this.texture;
        }

        public float x0() {
            return this.x0;
        }

        public float y0() {
            return this.y0;
        }

        public float z0() {
            return this.z0;
        }

        public float u0() {
            return this.u0;
        }

        public float v0() {
            return this.v0;
        }

        public int c0() {
            return this.c0;
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float z1() {
            return this.z1;
        }

        public float u1() {
            return this.u1;
        }

        public float v1() {
            return this.v1;
        }

        public int c1() {
            return this.c1;
        }

        public float x2() {
            return this.x2;
        }

        public float y2() {
            return this.y2;
        }

        public float z2() {
            return this.z2;
        }

        public float u2() {
            return this.u2;
        }

        public float v2() {
            return this.v2;
        }

        public int c2() {
            return this.c2;
        }

        public float x3() {
            return this.x3;
        }

        public float y3() {
            return this.y3;
        }

        public float z3() {
            return this.z3;
        }

        public float u3() {
            return this.u3;
        }

        public float v3() {
            return this.v3;
        }

        public int c3() {
            return this.c3;
        }
    }
}
