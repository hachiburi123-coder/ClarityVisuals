package ru.method.clarityvisuals.utils.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

final class RainGlState {
    private final int[] viewport = new int[4];
    private final boolean blend;
    private final boolean depthTest;
    private final boolean cullFace;
    private final boolean scissorTest;
    private final boolean depthMask;
    private final int program;
    private final int activeTexture;
    private final boolean srgb;
    private final int tex0;
    private final int tex1;

    private RainGlState() {
        int[] vp = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
        System.arraycopy(vp, 0, this.viewport, 0, 4);
        this.blend = GL11.glIsEnabled(GL11.GL_BLEND);
        this.depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        this.cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        this.scissorTest = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        this.depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        this.program = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        this.srgb = GL11.glIsEnabled(GL30.GL_FRAMEBUFFER_SRGB);
        this.activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        // Unit 0 and 1 bindings are read and later restored: the atmosphere pass
        // binds its own scene/depth textures there. All state that either pass
        // touches must come back exactly as it was, otherwise the vanilla hand
        // pass that follows inherits broken texture/sRGB state.
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        this.tex0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        this.tex1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL13.glActiveTexture(this.activeTexture);
    }

    static RainGlState capture() {
        return new RainGlState();
    }

    void restore() {
        GL11.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
        setEnabled(GL11.GL_BLEND, this.blend);
        setEnabled(GL11.GL_DEPTH_TEST, this.depthTest);
        setEnabled(GL11.GL_CULL_FACE, this.cullFace);
        setEnabled(GL11.GL_SCISSOR_TEST, this.scissorTest);
        GL11.glDepthMask(this.depthMask);
        if (this.srgb) {
            GL11.glEnable(GL30.GL_FRAMEBUFFER_SRGB);
        } else {
            GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);
        }
        GL20.glUseProgram(this.program);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.tex0);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.tex1);
        GL13.glActiveTexture(this.activeTexture);
    }

    int getActiveTexture() {
        return this.activeTexture;
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }
}
