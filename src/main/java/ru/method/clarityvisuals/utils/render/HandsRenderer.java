package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_276;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import net.minecraft.class_9801;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.HandsModule;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;

public final class HandsRenderer
implements MinecraftClientAccess {
    private static final Map<String, ShaderProgram> SHADERS = new HashMap<String, ShaderProgram>();
    private static final int RESOLUTION_SCALE = 2;
    private static class_6367 handFramebuffer;

    private HandsRenderer() {
    }

        public static void render(float f) {
        if (!HandsRenderer.isActive()) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        try {
            class_310 class_3102 = class_310.method_1551();
            class_276 class_2762 = class_3102.method_1522();
            HandsRenderer.ensureFramebuffer();
            if (class_2762 == null || handFramebuffer == null) {
                return;
            }
            HandsRenderer.blitColorAndDepth(class_2762, (class_276)handFramebuffer);
            class_2762.method_1235(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask((boolean)false);
            RenderSystem.disableCull();
            ShaderProgram shaderProgram = HandsRenderer.shaderFor(HandsRenderer.resolveMode());
            shaderProgram.bind();
            HandsRenderer.bindTexture(33984, handFramebuffer.method_30277(), 9729);
            HandsRenderer.bindTexture(33985, handFramebuffer.method_30278(), 9728);
            HandsRenderer.setupUniforms(shaderProgram, f);
            HandsRenderer.drawFullscreenQuad();
            shaderProgram.unbind();
            RenderSystem.activeTexture((int)33985);
            RenderSystem.bindTexture((int)0);
            RenderSystem.activeTexture((int)33984);
            RenderSystem.bindTexture((int)class_2762.method_30277());
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
        catch (Throwable throwable) {
            ClarityVisuals.LOGGER.warn("[ClarityVisuals] Hands shader pass failed.", throwable);
            HandsRenderer.release();
        }
        finally {
            if (aiG != null && aiG.method_1522() != null) {
                aiG.method_1522().method_1235(false);
            }
            RenderSystem.activeTexture((int)33984);
        }
    }

    public static void release() {
        for (ShaderProgram shaderProgram : SHADERS.values()) {
            shaderProgram.delete();
        }
        SHADERS.clear();
        HandsRenderer.deleteFramebuffer((class_276)handFramebuffer);
        handFramebuffer = null;
    }

    public static boolean isActive() {
        if (aiG == null || HandsRenderer.aiG.field_1724 == null || HandsRenderer.aiG.field_1687 == null) {
            return false;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.isPanic() || clarityVisuals.getModuleManager() == null) {
            return false;
        }
        HandsModule handsModule = clarityVisuals.getModuleManager().getModule(HandsModule.class);
        return handsModule != null && handsModule.isToggled();
    }

    private static HandsModule resolveModule() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        return clarityVisuals == null || clarityVisuals.getModuleManager() == null ? null : clarityVisuals.getModuleManager().getModule(HandsModule.class);
    }

    private static HandsModule.HandMode resolveMode() {
        HandsModule handsModule = HandsRenderer.resolveModule();
        return handsModule == null ? HandsModule.HandMode.AQUA : (HandsModule.HandMode)handsModule.Bi.jT();
    }

    private static String shaderName(HandsModule.HandMode handMode) {
        return handMode == null ? "sirius_aqua" : handMode.getShaderName();
    }

    private static ShaderProgram shaderFor(HandsModule.HandMode handMode) {
        String string2 = HandsRenderer.shaderName(handMode);
        return SHADERS.computeIfAbsent(string2, string -> new ShaderProgram("hands/" + string));
    }

    private static void setupUniforms(ShaderProgram shaderProgram, float f) {
        int n = Math.max(1, HandsRenderer.handFramebuffer.field_1482);
        int n2 = Math.max(1, HandsRenderer.handFramebuffer.field_1481);
        HandsModule handsModule = HandsRenderer.resolveModule();
        HandsModule.HandMode handMode = handsModule == null ? HandsModule.HandMode.AQUA : (HandsModule.HandMode)handsModule.Bi.jT();
        float speed = handsModule == null ? 1.0f : handsModule.handSpeed();
        float alpha = handsModule == null ? 1.0f : handsModule.handAlpha();
        float timeScale = handMode == null ? 1.0f : handMode.getTimeScale();
        float f7 = (float)(System.nanoTime() % 1000000000000L) / 1.0E9f * speed * timeScale;
        shaderProgram.set1i("ColorTexture", 0);
        shaderProgram.set1i("DepthTexture", 1);
        shaderProgram.set1f("time", f7);
        shaderProgram.set2f("resolution", n, n2);
        shaderProgram.set2f("iResolution", n, n2);
        shaderProgram.set2f("handMotion", 0.5f, 0.5f);
        shaderProgram.set2f("speed", speed, speed);
        shaderProgram.set1f("shift", f7 * 0.35f);
        shaderProgram.set2f("iMouse", 0.0f, 0.0f);
        shaderProgram.set2f("mouse", 0.5f, 0.5f);
        shaderProgram.set1f("effectAlpha", Math.max(0.0f, Math.min(1.0f, alpha)));
        shaderProgram.set1f("nearPlane", 0.05f);
        shaderProgram.set1f("farPlane", f);
    }

    private static void ensureFramebuffer() {
        int n = Math.max(1, aiG.method_22683().method_4489() / 2);
        int n2 = Math.max(1, aiG.method_22683().method_4506() / 2);
        if (n <= 0 || n2 <= 0) {
            return;
        }
        if (handFramebuffer == null || HandsRenderer.handFramebuffer.field_1482 != n || HandsRenderer.handFramebuffer.field_1481 != n2) {
            HandsRenderer.deleteFramebuffer((class_276)handFramebuffer);
            handFramebuffer = new class_6367(n, n2, true);
            handFramebuffer.method_58226(9729);
        }
    }

    private static void drawFullscreenQuad() {
        class_287 class_2872 = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1592);
        class_2872.method_22912(-1.0f, -1.0f, 0.0f);
        class_2872.method_22912(1.0f, -1.0f, 0.0f);
        class_2872.method_22912(1.0f, 1.0f, 0.0f);
        class_2872.method_22912(-1.0f, 1.0f, 0.0f);
        class_286.method_43437((class_9801)class_2872.method_60800());
    }

    private static void bindTexture(int n, int n2, int n3) {
        RenderSystem.activeTexture((int)n);
        RenderSystem.bindTexture((int)n2);
        GL11.glTexParameteri((int)3553, (int)10241, (int)n3);
        GL11.glTexParameteri((int)3553, (int)10240, (int)n3);
    }

    private static void blitColorAndDepth(class_276 class_2762, class_276 class_2763) {
        if (class_2762 == null || class_2763 == null) {
            return;
        }
        int n = GL11.glGetInteger((int)36010);
        int n2 = GL11.glGetInteger((int)36006);
        GL30.glBindFramebuffer((int)36008, (int)class_2762.field_1476);
        GL30.glBindFramebuffer((int)36009, (int)class_2763.field_1476);
        GL30.glBlitFramebuffer((int)0, (int)0, (int)class_2762.field_1482, (int)class_2762.field_1481, (int)0, (int)0, (int)class_2763.field_1482, (int)class_2763.field_1481, (int)16640, (int)9728);
        GL30.glBindFramebuffer((int)36008, (int)n);
        GL30.glBindFramebuffer((int)36009, (int)n2);
    }

    private static void deleteFramebuffer(class_276 class_2762) {
        if (class_2762 != null) {
            class_2762.method_1238();
        }
    }

    static int compile(String string, int n) {
        int n2 = GL20.glCreateShader((int)n);
        GL20.glShaderSource((int)n2, (CharSequence)string);
        GL20.glCompileShader((int)n2);
        if (GL20.glGetShaderi((int)n2, (int)35713) == 0) {
            String string2 = GL20.glGetShaderInfoLog((int)n2);
            GL20.glDeleteShader((int)n2);
            throw new IllegalStateException("Failed to compile shader: " + string2);
        }
        return n2;
    }

    static String readShader(String string) {
        String resourcePath = string.startsWith("/") ? string : "/" + string;
        try (InputStream inputStream = HandsRenderer.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Shader file is missing: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Exception exception) {
            throw new IllegalStateException("Unable to read shader: " + resourcePath, exception);
        }
    }

    static final class ShaderProgram {
        private final int id;
        private final Map<String, Integer> uniformCache = new HashMap<String, Integer>();

        ShaderProgram(String string) {
            String string2 = HandsRenderer.readShader("assets/clarityvisuals/shaders/impl/" + string + ".vsh");
            String string3 = HandsRenderer.readShader("assets/clarityvisuals/shaders/impl/" + string + ".fsh");
            int n = HandsRenderer.compile(string2, 35633);
            int n2 = HandsRenderer.compile(string3, 35632);
            int n3 = GL20.glCreateProgram();
            GL20.glAttachShader((int)n3, (int)n);
            GL20.glAttachShader((int)n3, (int)n2);
            GL20.glLinkProgram((int)n3);
            if (GL20.glGetProgrami((int)n3, (int)35714) == 0) {
                String string4 = GL20.glGetProgramInfoLog((int)n3);
                GL20.glDeleteProgram((int)n3);
                GL20.glDeleteShader((int)n);
                GL20.glDeleteShader((int)n2);
                throw new IllegalStateException("Failed to link shader: " + string + " -> " + string4);
            }
            GL20.glDetachShader((int)n3, (int)n);
            GL20.glDetachShader((int)n3, (int)n2);
            GL20.glDeleteShader((int)n);
            GL20.glDeleteShader((int)n2);
            this.id = n3;
        }

        void bind() {
            GL20.glUseProgram((int)this.id);
        }

        void unbind() {
            GL20.glUseProgram((int)0);
        }

        void set1i(String string, int n) {
            int n2 = this.uniform(string);
            if (n2 >= 0) {
                GL20.glUniform1i((int)n2, (int)n);
            }
        }

        void set1f(String string, float f) {
            int n = this.uniform(string);
            if (n >= 0) {
                GL20.glUniform1f((int)n, (float)f);
            }
        }

        void set2f(String string, float f, float f2) {
            int n = this.uniform(string);
            if (n >= 0) {
                GL20.glUniform2f((int)n, (float)f, (float)f2);
            }
        }

        void set3f(String string, float f, float f2, float f3) {
            int n = this.uniform(string);
            if (n >= 0) {
                GL20.glUniform3f((int)n, (float)f, (float)f2, (float)f3);
            }
        }

        void delete() {
            GL20.glDeleteProgram((int)this.id);
        }

        private int uniform(String string2) {
            return this.uniformCache.computeIfAbsent(string2, string -> GL20.glGetUniformLocation((int)this.id, (CharSequence)string));
        }
    }
}
