package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.joml.Matrix4f;

public final class AuraModel {
    public final float textureWidth;
    public final float textureHeight;
    public float minY = 0.0f;
    public float maxY = 24.0f;
    public float minX = 0.0f;
    public float maxX = 0.0f;
    public float minZ = 0.0f;
    public float maxZ = 0.0f;
    public final List<AuraBone> bones = new ArrayList<AuraBone>();

    public AuraModel(float textureWidth, float textureHeight) {
        this.textureWidth = textureWidth > 0.0f ? textureWidth : 64.0f;
        this.textureHeight = textureHeight > 0.0f ? textureHeight : 64.0f;
    }

    public void render(class_4587 matrices, class_4588 consumer, int light, int overlay, float red, float green, float blue, float alpha, AuraAnimation animation, float animTime) {
        for (AuraBone bone : this.bones) {
            if (bone.parent == null || bone.parent.isEmpty()) {
                bone.render(matrices, consumer, light, overlay, red, green, blue, alpha, animation, animTime);
            }
        }
    }

    public void collectBounds(Matrix4f identity, float[] bounds) {
        for (AuraBone bone : this.bones) {
            if (bone.parent == null || bone.parent.isEmpty()) {
                bone.collectBounds(identity, bounds);
            }
        }
    }

    public void recalculateBounds() {
        float[] bounds = new float[]{
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY
        };
        this.collectBounds(new Matrix4f(), bounds);

        if (Float.isFinite(bounds[0])) {
            this.minX = bounds[0];
            this.minY = bounds[1];
            this.minZ = bounds[2];
            this.maxX = bounds[3];
            this.maxY = bounds[4];
            this.maxZ = bounds[5];
        }
    }
}
