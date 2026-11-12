package ru.method.clarityvisuals.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.modules.impl.render.SuperResolutionModule;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class SuperResolutionRenderer
implements MinecraftClientAccess {
    private static ShaderProgram shader;
    private static class_6367 upscaleFbo;

    private SuperResolutionRenderer() {
    }

    public static void render() {
        if (!isActive()) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        try {
            SuperResolutionModule module = ClarityVisuals.getInstance().getModuleManager().getModule(SuperResolutionModule.class);
            if (module == null || !module.isToggled()) {
                return;
            }

            class_310 client = class_310.method_1551();
            class_276 mainFbo = client.method_1522();
            if (mainFbo == null) {
                return;
            }

            ensureFramebuffers();
            if (upscaleFbo == null || shader == null) {
                return;
            }

            float sharpness = module.getSharpness();
            int filterMode = module.getFilterMode();

            // Render CAS pass into upscaleFbo
            upscaleFbo.method_1235(false);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();

            shader.bind();
            bindTexture(33984, mainFbo.method_30277(), 9729);
            shader.set1i("InputTexture", 0);
            shader.set2f("TexelSize", 1.0f / (float) mainFbo.field_1482, 1.0f / (float) mainFbo.field_1481);
            shader.set1f("Sharpness", sharpness);
            shader.set1i("FilterMode", filterMode);

            drawFullscreenQuad();

            shader.unbind();
            RenderSystem.activeTexture(33984);
            RenderSystem.bindTexture(0);

            // Blit result back to mainFbo
            blitColor((class_276) upscaleFbo, mainFbo);

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
        } catch (Throwable throwable) {
            ClarityVisuals.LOGGER.warn("[ClarityVisuals] SuperResolution pass failed.", throwable);
            release();
        } finally {
            if (aiG != null && aiG.method_1522() != null) {
                aiG.method_1522().method_1235(false);
            }
            RenderSystem.activeTexture(33984);
        }
    }

    public static void release() {
        if (shader != null) {
            shader.delete();
            shader = null;
        }
        deleteFramebuffer((class_276) upscaleFbo);
        upscaleFbo = null;
    }

    public static boolean isActive() {
        if (aiG == null || aiG.field_1724 == null || aiG.field_1687 == null) {
            return false;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.isPanic() || clarityVisuals.getModuleManager() == null) {
            return false;
        }
        SuperResolutionModule module = clarityVisuals.getModuleManager().getModule(SuperResolutionModule.class);
        return module != null && module.isToggled();
    }

    private static void ensureFramebuffers() {
        int width = Math.max(1, aiG.method_22683().method_4489());
        int height = Math.max(1, aiG.method_22683().method_4506());
        if (upscaleFbo == null || upscaleFbo.field_1482 != width || upscaleFbo.field_1481 != height) {
            deleteFramebuffer((class_276) upscaleFbo);
            upscaleFbo = new class_6367(width, height, false);
            upscaleFbo.method_58226(9729);
        }
        if (shader == null) {
            shader = new ShaderProgram("effects/super_resolution");
        }
    }

    private static void drawFullscreenQuad() {
        class_287 builder = RenderSystem.renderThreadTesselator().method_60827(class_293.class_5596.field_27382, class_290.field_1592);
        builder.method_22912(-1.0f, -1.0f, 0.0f);
        builder.method_22912(1.0f, -1.0f, 0.0f);
        builder.method_22912(1.0f, 1.0f, 0.0f);
        builder.method_22912(-1.0f, 1.0f, 0.0f);
        class_286.method_43437((class_9801) builder.method_60800());
    }

    private static void bindTexture(int textureUnit, int textureId, int filter) {
        RenderSystem.activeTexture(textureUnit);
        RenderSystem.bindTexture(textureId);
        GL11.glTexParameteri(3553, 10241, filter);
        GL11.glTexParameteri(3553, 10240, filter);
    }

    private static void blitColor(class_276 src, class_276 dst) {
        if (src == null || dst == null) {
            return;
        }
        int readFbo = GL11.glGetInteger(36010);
        int drawFbo = GL11.glGetInteger(36006);
        GL30.glBindFramebuffer(36008, src.field_1476);
        GL30.glBindFramebuffer(36009, dst.field_1476);
        GL30.glBlitFramebuffer(0, 0, src.field_1482, src.field_1481, 0, 0, dst.field_1482, dst.field_1481, 16384, 9728);
        GL30.glBindFramebuffer(36008, readFbo);
        GL30.glBindFramebuffer(36009, drawFbo);
    }

    private static void deleteFramebuffer(class_276 fbo) {
        if (fbo != null) {
            fbo.method_1238();
        }
    }

    static int compile(String src, int type) {
        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, src);
        GL20.glCompileShader(shaderId);
        if (GL20.glGetShaderi(shaderId, 35713) == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId);
            GL20.glDeleteShader(shaderId);
            throw new IllegalStateException("Failed to compile SuperResolution shader: " + log);
        }
        return shaderId;
    }

    static String readShader(String path) {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        try (InputStream inputStream = SuperResolutionRenderer.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Shader file is missing: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read shader: " + resourcePath, exception);
        }
    }

    static final class ShaderProgram {
        private final int id;
        private final Map<String, Integer> uniformCache = new HashMap<>();

        ShaderProgram(String name) {
            String vsh = SuperResolutionRenderer.readShader("assets/clarityvisuals/shaders/impl/" + name + ".vsh");
            String fsh = SuperResolutionRenderer.readShader("assets/clarityvisuals/shaders/impl/" + name + ".fsh");
            int vshId = SuperResolutionRenderer.compile(vsh, 35633);
            int fshId = SuperResolutionRenderer.compile(fsh, 35632);
            int progId = GL20.glCreateProgram();
            GL20.glAttachShader(progId, vshId);
            GL20.glAttachShader(progId, fshId);
            GL20.glLinkProgram(progId);
            if (GL20.glGetProgrami(progId, 35714) == 0) {
                String log = GL20.glGetProgramInfoLog(progId);
                GL20.glDeleteProgram(progId);
                GL20.glDeleteShader(vshId);
                GL20.glDeleteShader(fshId);
                throw new IllegalStateException("Failed to link SuperResolution shader: " + name + " -> " + log);
            }
            GL20.glDetachShader(progId, vshId);
            GL20.glDetachShader(progId, fshId);
            GL20.glDeleteShader(vshId);
            GL20.glDeleteShader(fshId);
            this.id = progId;
        }

        void bind() {
            GL20.glUseProgram(this.id);
        }

        void unbind() {
            GL20.glUseProgram(0);
        }

        void set1i(String name, int val) {
            int loc = uniform(name);
            if (loc >= 0) GL20.glUniform1i(loc, val);
        }

        void set1f(String name, float val) {
            int loc = uniform(name);
            if (loc >= 0) GL20.glUniform1f(loc, val);
        }

        void set2f(String name, float x, float y) {
            int loc = uniform(name);
            if (loc >= 0) GL20.glUniform2f(loc, x, y);
        }

        void delete() {
            GL20.glDeleteProgram(this.id);
        }

        private int uniform(String name) {
            return this.uniformCache.computeIfAbsent(name, s -> GL20.glGetUniformLocation(this.id, s));
        }
    }
}
