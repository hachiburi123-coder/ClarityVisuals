package ru.method.clarityvisuals.utils.render;

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

/**
 * Fullscreen depth-aware atmosphere pass.
 *
 * <p>Mode 0 is a low, world-space mist that hugs the ground. Mode 1 is a desert
 * sandstorm: distance haze, fast dust sheets and procedural wind ripples on
 * upward-facing blocks. Mode 2 is the alien atmosphere: teal ground haze with
 * slowly breathing bioluminescent veins running across upward faces.</p>
 */
public final class AtmosphereSurface
implements MinecraftClientAccess {
    private static final AtmosphereSurface INSTANCE = new AtmosphereSurface();
    private static final float EPSILON = 1.0E-4f;

    public static final int MODE_FOG = 0;
    public static final int MODE_SANDSTORM = 1;
    public static final int MODE_ALIEN = 2;

    private final SceneTarget scene = new SceneTarget();
    private final SceneTarget depthScene = new SceneTarget();
    private boolean loggedActive;
    private boolean loggedNoDepth;
    private boolean loggedSceneFail;
    private RainShader shader;
    private int drawFramebuffer;
    private int vertexArray;
    private int vertexBuffer;
    private boolean initialized;
    private boolean disabled;

    private AtmosphereSurface() {
    }

    public static AtmosphereSurface getInstance() {
        return INSTANCE;
    }

    public static final class Parameters {
        public int mode = MODE_FOG;
        public float density = 1.0f;
        public float height = 8.0f;
        public float speed = 1.0f;
        public float maxDistance = 64.0f;
        public float ground = 1.0f;
        public float baseY = 64.0f;
        public float time;
        public float red = 0.72f;
        public float green = 0.80f;
        public float blue = 0.88f;
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
        if (AtmosphereSurface.aiG.field_1687 == null || AtmosphereSurface.aiG.field_1724 == null
                || parameters.density <= EPSILON) {
            return;
        }
        int width = AtmosphereSurface.aiG.method_22683().method_4489();
        int height = AtmosphereSurface.aiG.method_22683().method_4506();
        if (width <= 1 || height <= 1) {
            return;
        }
        class_276 framebuffer = AtmosphereSurface.aiG.method_1522();
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
                    ClarityVisuals.LOGGER.warn("[Atmosphere] scene copy failed");
                }
                return;
            }
            int depthTexture = this.blitDepthBuffer(mainFbo, width, height);
            if (depthTexture <= 0) {
                if (!this.loggedNoDepth) {
                    this.loggedNoDepth = true;
                    ClarityVisuals.LOGGER.warn("[Atmosphere] no depth available");
                }
                return;
            }
            if (!this.loggedActive) {
                this.loggedActive = true;
                ClarityVisuals.LOGGER.info("[Atmosphere] active " + width + "x" + height);
            }
            Matrix4f inverseProjection = new Matrix4f(projectionMatrix).invert();
            Matrix4f inverseView = new Matrix4f(viewMatrix).invert();
            net.minecraft.class_243 cameraPos = camera.method_19326();
            inverseView.m30((float) cameraPos.field_1352);
            inverseView.m31((float) cameraPos.field_1351);
            inverseView.m32((float) cameraPos.field_1350);
            attached = this.renderPass(colorTexture, depthTexture, width, height, viewMatrix, projectionMatrix,
                inverseProjection, inverseView, parameters);
        } catch (Throwable throwable) {
            this.disabled = true;
            ClarityVisuals.LOGGER.warn("[Atmosphere] renderer disabled: " + throwable.getMessage());
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
        this.shader.setUniform1f("u_Time", parameters.time);
        this.shader.setUniform1f("u_Mode", (float) parameters.mode);
        this.shader.setUniform1f("u_Density", clamp(parameters.density, 0.0f, 3.0f));
        this.shader.setUniform1f("u_Height", clamp(parameters.height, 0.5f, 48.0f));
        this.shader.setUniform1f("u_Speed", clamp(parameters.speed, 0.0f, 4.0f));
        this.shader.setUniform1f("u_MaxDistance", clamp(parameters.maxDistance, 4.0f, 160.0f));
        this.shader.setUniform1f("u_Ground", clamp(parameters.ground, 0.0f, 2.0f));
        this.shader.setUniform1f("u_BaseY", parameters.baseY);
        this.shader.setUniform3f("u_Color", clamp(parameters.red, 0.0f, 1.0f),
            clamp(parameters.green, 0.0f, 1.0f), clamp(parameters.blue, 0.0f, 1.0f));
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
                GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (java.nio.ByteBuffer) null);
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
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, (java.nio.ByteBuffer) null);
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
        this.shader = new RainShader("assets/clarityvisuals/shaders/atmosphere/atmosphere_surface.vsh",
            "assets/clarityvisuals/shaders/atmosphere/atmosphere_surface.fsh");
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
        return com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread() && GLFW.glfwGetCurrentContext() != 0L;
    }

    private static float clamp(float value, float min, float max) {
        return !Float.isFinite(value) ? min : Math.max(min, Math.min(max, value));
    }
}
