package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;

public final class RainWetSurface
implements MinecraftClientAccess {
    private static final RainWetSurface INSTANCE = new RainWetSurface();
    private static final float EPSILON = 1.0E-4f;

    private final SceneTarget scene = new SceneTarget();
    private final SceneTarget depthScene = new SceneTarget();
    private boolean loggedActive;
    private boolean loggedNoDepth;
    private boolean loggedSceneFail;
    private RainShader shader;
    private int blitFramebuffer;
    private int drawFramebuffer;
    private int vertexArray;
    private int vertexBuffer;
    private boolean initialized;
    private boolean disabled;

    private RainWetSurface() {
    }

    public static RainWetSurface getInstance() {
        return INSTANCE;
    }

    public static final class Parameters {
        public float wetness = 1.0f;
        public float puddleCoverage = 0.82f;
        public float reflectionStrength = 1.25f;
        public float maxDistance = 56.0f;
        public float rippleStrength = 0.8f;
        public float rainAmount = 1.0f;
        public int reflectionSteps = 10;
        public boolean ripples = true;
    }

    private static final class SceneTarget {
        int framebuffer;
        int texture;
        int width;
        int height;
    }

    public void apply(class_4184 camera, Matrix4f viewMatrix, Matrix4f projectionMatrix, Parameters parameters) {
        if (this.disabled || camera == null || viewMatrix == null || projectionMatrix == null || parameters == null) {
            return;
        }
        if (RainWetSurface.aiG.field_1687 == null || RainWetSurface.aiG.field_1724 == null
                || parameters.wetness <= EPSILON || parameters.reflectionStrength <= EPSILON) {
            return;
        }
        int width = RainWetSurface.aiG.method_22683().method_4489();
        int height = RainWetSurface.aiG.method_22683().method_4506();
        if (width <= 1 || height <= 1) {
            return;
        }
        class_276 framebuffer = RainWetSurface.aiG.method_1522();
        if (framebuffer == null) {
            return;
        }
        int colorTexture = framebuffer.method_30277();
        if (colorTexture <= 0) {
            return;
        }
        RainGlState snapshot = RainGlState.capture();
        int prevDrawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int prevReadFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int prevDrawBuf = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);
        int prevReadBuf = GL11.glGetInteger(GL11.GL_READ_BUFFER);
        boolean attached = false;
        try {
            this.ensureInitialized();
            int mainFbo = framebuffer.field_1476 != 0 ? framebuffer.field_1476 : prevReadFbo;
            if (mainFbo <= 0) {
                mainFbo = prevDrawFbo;
            }
            if (this.disabled || !this.ensureScene(width, height) || !this.copyColorToScene(mainFbo, width, height)) {
                if (!this.loggedSceneFail) {
                    this.loggedSceneFail = true;
                    ClarityVisuals.LOGGER.warn("[Rain] wet-surface: scene copy failed");
                }
                return;
            }
            int depthTexture = this.blitDepthBuffer(mainFbo, width, height);
            if (depthTexture <= 0) {
                if (!this.loggedNoDepth) {
                    this.loggedNoDepth = true;
                    ClarityVisuals.LOGGER.warn("[Rain] wet-surface: no depth available");
                }
                return;
            }
            if (!this.loggedActive) {
                this.loggedActive = true;
                ClarityVisuals.LOGGER.info("[Rain] wet-surface active " + width + "x" + height);
            }
            Matrix4f inverseProjection = new Matrix4f(projectionMatrix).invert();
            Matrix4f inverseView = new Matrix4f(viewMatrix).invert();
            class_243 cameraPos = camera.method_19326();
            inverseView.m30((float) cameraPos.field_1352);
            inverseView.m31((float) cameraPos.field_1351);
            inverseView.m32((float) cameraPos.field_1350);
            attached = this.renderPass(colorTexture, depthTexture, width, height, viewMatrix, projectionMatrix,
                inverseProjection, inverseView, parameters);
        } catch (Throwable throwable) {
            this.disabled = true;
            ClarityVisuals.LOGGER.warn("[Rain] wet-surface renderer disabled: " + throwable.getMessage());
        } finally {
            if (attached && this.drawFramebuffer != 0) {
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.drawFramebuffer);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, 0, 0);
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL20.glUseProgram(0);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDrawFbo);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevReadFbo);
            GL11.glDrawBuffer(prevDrawBuf);
            GL11.glReadBuffer(prevReadBuf);
            snapshot.restore();
        }
    }

    private boolean renderPass(int colorTexture, int depthTexture, int width, int height,
            Matrix4f viewMatrix, Matrix4f projectionMatrix, Matrix4f inverseProjection, Matrix4f inverseView,
            Parameters parameters) {
        if (this.drawFramebuffer == 0) {
            this.drawFramebuffer = GL30.glGenFramebuffers();
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.drawFramebuffer);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture, 0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            return true;
        }
        GL11.glViewport(0, 0, width, height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(false);
        this.shader.bind();
        this.shader.setUniform1i("u_SceneTexture", 0);
        this.shader.setUniform1i("u_DepthTexture", 1);
        this.shader.setUniform2f("u_Resolution", (float) width, (float) height);
        this.shader.setUniformMatrix4f("u_ViewMatrix", viewMatrix);
        this.shader.setUniformMatrix4f("u_ProjectionMatrix", projectionMatrix);
        this.shader.setUniformMatrix4f("u_InverseProjectionMatrix", inverseProjection);
        this.shader.setUniformMatrix4f("u_InverseViewMatrix", inverseView);
        this.shader.setUniform1f("u_Time", (System.currentTimeMillis() % 1_000_000L) / 1000.0f);
        this.shader.setUniform1f("u_Wetness", clamp(parameters.wetness, 0.0f, 2.0f));
        this.shader.setUniform1f("u_PuddleCoverage", clamp(parameters.puddleCoverage, 0.0f, 1.0f));
        this.shader.setUniform1f("u_ReflectionStrength", clamp(parameters.reflectionStrength, 0.0f, 2.5f));
        this.shader.setUniform1f("u_MaxDistance", clamp(parameters.maxDistance, 4.0f, 128.0f));
        this.shader.setUniform1f("u_RippleStrength", clamp(parameters.rippleStrength, 0.0f, 2.0f));
        this.shader.setUniform1f("u_RainAmount", clamp(parameters.rainAmount, 0.0f, 2.0f));
        this.shader.setUniform1i("u_ReflectionSteps", Math.max(4, Math.min(16, parameters.reflectionSteps)));
        this.shader.setUniform1i("u_Ripples", parameters.ripples ? 1 : 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.scene.texture);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        GL30.glBindVertexArray(this.vertexArray);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(prevVao);
        return true;
    }

    private boolean copyColorToScene(int srcFbo, int width, int height) {
        if (srcFbo <= 0 || this.scene.framebuffer <= 0 || width <= 0 || height <= 0) {
            return false;
        }
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, srcFbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.scene.framebuffer);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        return true;
    }

    private int blitDepthBuffer(int srcFbo, int width, int height) {
        if (srcFbo <= 0 || !this.ensureDepthScene(width, height)) {
            return 0;
        }
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, srcFbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.depthScene.framebuffer);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        return this.depthScene.texture;
    }

    private boolean ensureDepthScene(int width, int height) {
        if (this.depthScene.texture != 0
                && (this.depthScene.width != width || this.depthScene.height != height || this.depthScene.framebuffer == 0)) {
            this.deleteDepthScene();
        }
        if (this.depthScene.texture == 0) {
            this.depthScene.texture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.depthScene.texture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT24, width, height, 0,
                GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (ByteBuffer) null);
            this.depthScene.framebuffer = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.depthScene.framebuffer);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D,
                this.depthScene.texture, 0);
            GL11.glDrawBuffer(GL11.GL_NONE);
            GL11.glReadBuffer(GL11.GL_NONE);
            if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
                this.deleteDepthScene();
                return false;
            }
        }
        this.depthScene.width = width;
        this.depthScene.height = height;
        return true;
    }

    private void deleteDepthScene() {
        if (this.depthScene.framebuffer != 0 && canModifyGlObjects()) {
            GL30.glDeleteFramebuffers(this.depthScene.framebuffer);
        }
        if (this.depthScene.texture != 0 && canModifyGlObjects()) {
            GL11.glDeleteTextures(this.depthScene.texture);
        }
        this.depthScene.framebuffer = 0;
        this.depthScene.texture = 0;
        this.depthScene.width = 0;
        this.depthScene.height = 0;
    }

    private boolean ensureScene(int width, int height) {
        if (this.scene.texture != 0 && (this.scene.width != width || this.scene.height != height || this.scene.framebuffer == 0)) {
            this.deleteScene();
        }
        if (this.scene.texture == 0) {
            this.scene.texture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.scene.texture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
            allocTexture2D(GL30.GL_RGBA8, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
            this.scene.framebuffer = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.scene.framebuffer);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.scene.texture, 0);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
            if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
                this.deleteScene();
                return false;
            }
        }
        this.scene.width = width;
        this.scene.height = height;
        return true;
    }

    private static void allocTexture2D(int internalFormat, int width, int height, int format, int type) {
        int unpackBuffer = GL11.glGetInteger(0x88EF);
        if (unpackBuffer != 0) {
            GL15.glBindBuffer(0x88EC, 0);
        }
        try {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        } finally {
            if (unpackBuffer != 0) {
                GL15.glBindBuffer(0x88EC, unpackBuffer);
            }
        }
    }

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.vertexArray = GL30.glGenVertexArrays();
        this.vertexBuffer = GL15.glGenBuffers();
        int prevVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        GL30.glBindVertexArray(this.vertexArray);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        float[] vertices = new float[]{
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f
        };
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 16, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 16, 8L);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(prevVao);
        this.shader = new RainShader("assets/clarityvisuals/shaders/rain/wet_surface.vsh",
            "assets/clarityvisuals/shaders/rain/wet_surface.fsh");
        this.initialized = true;
    }

    public void reset() {
        this.disabled = false;
    }

    public void close() {
        if (!canModifyGlObjects()) {
            this.clearObjectIds();
            return;
        }
        this.deleteScene();
        if (this.blitFramebuffer != 0) {
            GL30.glDeleteFramebuffers(this.blitFramebuffer);
        }
        if (this.drawFramebuffer != 0) {
            GL30.glDeleteFramebuffers(this.drawFramebuffer);
        }
        if (this.vertexArray != 0) {
            GL30.glDeleteVertexArrays(this.vertexArray);
        }
        if (this.vertexBuffer != 0) {
            GL15.glDeleteBuffers(this.vertexBuffer);
        }
        if (this.shader != null) {
            this.shader.delete();
        }
        this.clearObjectIds();
    }

    private void clearObjectIds() {
        this.scene.framebuffer = 0;
        this.scene.texture = 0;
        this.scene.width = 0;
        this.scene.height = 0;
        this.depthScene.framebuffer = 0;
        this.depthScene.texture = 0;
        this.depthScene.width = 0;
        this.depthScene.height = 0;
        this.blitFramebuffer = 0;
        this.drawFramebuffer = 0;
        this.vertexArray = 0;
        this.vertexBuffer = 0;
        this.shader = null;
        this.initialized = false;
        this.disabled = false;
    }

    private void deleteScene() {
        if (this.scene.framebuffer != 0 && canModifyGlObjects()) {
            GL30.glDeleteFramebuffers(this.scene.framebuffer);
        }
        if (this.scene.texture != 0 && canModifyGlObjects()) {
            GL11.glDeleteTextures(this.scene.texture);
        }
        this.deleteDepthScene();
        this.scene.framebuffer = 0;
        this.scene.texture = 0;
        this.scene.width = 0;
        this.scene.height = 0;
    }

    private static boolean canModifyGlObjects() {
        return RenderSystem.isOnRenderThread() && GLFW.glfwGetCurrentContext() != 0L;
    }

    private static float clamp(float value, float min, float max) {
        return !Float.isFinite(value) ? min : Math.max(min, Math.min(max, value));
    }
}
