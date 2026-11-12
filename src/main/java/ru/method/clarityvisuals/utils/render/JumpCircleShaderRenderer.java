package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;

public final class JumpCircleShaderRenderer
implements MinecraftClientAccess {
    private static final String BAKE_VSH = "assets/clarityvisuals/shaders/jump/jump_bake.vsh";
    private static final String RING_FSH = "assets/clarityvisuals/shaders/jump/jump_ring.fsh";
    private static final String ETHER_FSH = "assets/clarityvisuals/shaders/jump/jump_ether.fsh";
    private static final String GOLD_FSH = "assets/clarityvisuals/shaders/jump/jump_gold.fsh";
    private static final int MODES = 4;
    private static final int HILL_MODE = 3;
    private static final int FRAMES = 24;
    private static final int SIZE = 288;
    private static final float FRAME_STEP = 0.09f;

    private static RainShader ringShader;
    private static RainShader etherShader;
    private static RainShader goldShader;
    private static int vao;
    private static int vbo;
    private static int bakeFbo;
    private static int bakeTexture;
    private static boolean ready;
    private static boolean failed;
    private static final class_2960[][] frameIds = new class_2960[MODES][FRAMES];
    private static final boolean[][] frameDone = new boolean[MODES][FRAMES];
    private static final Queue<int[]> bakeQueue = new ArrayDeque<int[]>();
    private static boolean queueBuilt;

    private JumpCircleShaderRenderer() {
    }

    public static int frameCount() {
        return FRAMES;
    }

    public static class_2960 frameId(int mode, int idx) {
        if (mode < 0 || mode >= MODES || idx < 0 || idx >= FRAMES) {
            return null;
        }
        return frameDone[mode][idx] ? frameIds[mode][idx] : null;
    }

    public static void tickBake(int preferredMode) {
        if (failed || !RenderSystem.isOnRenderThread() || JumpCircleShaderRenderer.aiG == null) {
            return;
        }
        try {
            ensureInit();
        } catch (Throwable throwable) {
            failed = true;
            ClarityVisuals.LOGGER.warn("[JumpCircle] bake init failed: {}", String.valueOf(throwable.getMessage()));
            return;
        }
        if (!queueBuilt) {
            queueBuilt = true;
            for (int m = 0; m < MODES; ++m) {
                int mode = (preferredMode + m) % MODES;
                if (mode == HILL_MODE) {
                    continue;
                }
                for (int f = 0; f < FRAMES; ++f) {
                    if (!frameDone[mode][f]) {
                        bakeQueue.add(new int[]{mode, f});
                    }
                }
            }
        }
        int budget = 1;
        while (budget-- > 0 && !bakeQueue.isEmpty()) {
            int[] task = bakeQueue.poll();
            try {
                bakeFrame(task[0], task[1]);
            } catch (Throwable throwable) {
                failed = true;
                ClarityVisuals.LOGGER.warn("[JumpCircle] bake failed: {}", String.valueOf(throwable.getMessage()));
                return;
            }
        }
    }

    public static void preferMode(int preferredMode) {
        if (preferredMode < 0 || preferredMode >= MODES) {
            return;
        }
        ArrayDeque<int[]> mine = new ArrayDeque<int[]>();
        ArrayDeque<int[]> rest = new ArrayDeque<int[]>();
        for (int[] task : bakeQueue) {
            if (task[0] == preferredMode) {
                mine.add(task);
            } else {
                rest.add(task);
            }
        }
        bakeQueue.clear();
        bakeQueue.addAll(mine);
        bakeQueue.addAll(rest);
    }

    private static void ensureInit() {
        if (ready) {
            return;
        }
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            java.nio.FloatBuffer data = stack.mallocFloat(15);
            data.put(-1.0f).put(-1.0f).put(0.0f).put(0.0f).put(0.0f);
            data.put(3.0f).put(-1.0f).put(0.0f).put(2.0f).put(0.0f);
            data.put(-1.0f).put(3.0f).put(0.0f).put(0.0f).put(2.0f);
            data.flip();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        }
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 20, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 20, 12L);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(prevVao);

        bakeTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, bakeTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, SIZE, SIZE, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        bakeFbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, bakeFbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, bakeTexture, 0);
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            throw new IllegalStateException("bake framebuffer incomplete");
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        ringShader = new RainShader(BAKE_VSH, RING_FSH);
        etherShader = new RainShader(BAKE_VSH, ETHER_FSH);
        goldShader = new RainShader(BAKE_VSH, GOLD_FSH);
        ready = true;
    }

    private static void bakeFrame(int mode, int frame) {
        RainShader shader = ringShader;
        if (mode == 1) {
            shader = etherShader;
        } else if (mode == 2) {
            shader = goldShader;
        }
        float time = FRAME_STEP * (float) frame;

        RainGlState snapshot = RainGlState.capture();
        int prevVao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int prevBlendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int prevBlendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        int prevDrawBuf = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);
        int prevReadBuf = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        int prevDrawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int prevReadFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int[] prevViewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);
        ByteBuffer pixels = null;
        try {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, bakeFbo);
            GL11.glViewport(0, 0, SIZE, SIZE);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glDepthMask(false);
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            shader.bind();
            shader.setUniform1f("u_Time", time);
            shader.setUniform1f("u_Alpha", 1.0f);
            shader.setUniform1f("u_Seed", 0.0f);
            shader.setUniform3f("u_Tint", 1.0f, 1.0f, 1.0f);
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL30.glBindVertexArray(0);
            shader.unbind();

            pixels = MemoryUtil.memAlloc(SIZE * SIZE * 4);
            GL11.glReadPixels(0, 0, SIZE, SIZE, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            registerFrame(mode, frame, pixels);
        } finally {
            if (pixels != null) {
                MemoryUtil.memFree(pixels);
            }
            GL30.glBindVertexArray(prevVao);
            GL11.glBlendFunc(prevBlendSrc, prevBlendDst);
            GL11.glDrawBuffer(prevDrawBuf);
            GL11.glReadBuffer(prevReadBuf);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);
            GL11.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
            snapshot.restore();
        }
    }

    private static void registerFrame(int mode, int frame, ByteBuffer pixels) {
        class_310 mc = class_310.method_1551();
        if (mc == null) {
            return;
        }
        class_1011 image = new class_1011(SIZE, SIZE, true);
        for (int y = 0; y < SIZE; ++y) {
            for (int x = 0; x < SIZE; ++x) {
                int srcY = SIZE - 1 - y;
                int i = (srcY * SIZE + x) * 4;
                int r = pixels.get(i) & 0xFF;
                int g = pixels.get(i + 1) & 0xFF;
                int b = pixels.get(i + 2) & 0xFF;
                int a = pixels.get(i + 3) & 0xFF;
                image.method_61941(x, y, a << 24 | r << 16 | g << 8 | b);
            }
        }
        class_2960 id = class_2960.method_60655((String) "clarityvisuals", (String) ("dynamic/jumpcircle/" + mode + "_" + frame));
        class_1043 texture = new class_1043(image);
        texture.method_4527(true, false);
        mc.method_1531().method_4616(id, (class_1044) texture);
        frameIds[mode][frame] = id;
        frameDone[mode][frame] = true;
    }
}
