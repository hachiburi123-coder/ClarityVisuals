package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class AuraCube {
    public float[] origin = new float[3];
    public float[] size = new float[3];
    public float[] rotation = new float[]{0.0f, 0.0f, 0.0f};
    public float[] pivot = new float[]{0.0f, 0.0f, 0.0f};
    public float inflate = 0.0f;
    public boolean mirror = false;
    public final List<AuraQuad> quads = new ArrayList<AuraQuad>();

    public void render(class_4587 matrices, class_4588 consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        matrices.method_22903();
        if (this.rotation[0] != 0.0f || this.rotation[1] != 0.0f || this.rotation[2] != 0.0f) {
            matrices.method_46416(this.pivot[0] / 16.0f, this.pivot[1] / 16.0f, this.pivot[2] / 16.0f);
            if (this.rotation[2] != 0.0f) {
                matrices.method_22907(class_7833.field_40718.rotationDegrees(this.rotation[2]));
            }

            if (this.rotation[1] != 0.0f) {
                matrices.method_22907(class_7833.field_40716.rotationDegrees(this.rotation[1]));
            }

            if (this.rotation[0] != 0.0f) {
                matrices.method_22907(class_7833.field_40714.rotationDegrees(this.rotation[0]));
            }

            matrices.method_46416(-this.pivot[0] / 16.0f, -this.pivot[1] / 16.0f, -this.pivot[2] / 16.0f);
        }

        class_4587.class_4665 entry = matrices.method_23760();
        Matrix4f posMatrix = entry.method_23761();

        for (AuraQuad quad : this.quads) {
            for (AuraVertex vertex : quad.vertices) {
                consumer
                    .method_22918(posMatrix, vertex.pos.x, vertex.pos.y, vertex.pos.z)
                    .method_22915(red, green, blue, alpha)
                    .method_22913(vertex.u, vertex.v)
                    .method_22922(overlay)
                    .method_60803(light)
                    .method_60831(entry, quad.normal.x, quad.normal.y, quad.normal.z);
            }
        }

        matrices.method_22909();
    }

    public void collectBounds(Matrix4f parentMatrix, float[] bounds) {
        Matrix4f matrix = new Matrix4f(parentMatrix);
        if (this.rotation[0] != 0.0f || this.rotation[1] != 0.0f || this.rotation[2] != 0.0f) {
            matrix.translate(this.pivot[0] / 16.0f, this.pivot[1] / 16.0f, this.pivot[2] / 16.0f);
            if (this.rotation[2] != 0.0f) {
                matrix.rotateZ((float)Math.toRadians(this.rotation[2]));
            }

            if (this.rotation[1] != 0.0f) {
                matrix.rotateY((float)Math.toRadians(this.rotation[1]));
            }

            if (this.rotation[0] != 0.0f) {
                matrix.rotateX((float)Math.toRadians(this.rotation[0]));
            }

            matrix.translate(-this.pivot[0] / 16.0f, -this.pivot[1] / 16.0f, -this.pivot[2] / 16.0f);
        }

        Vector3f transformed = new Vector3f();

        for (AuraQuad quad : this.quads) {
            for (AuraVertex vertex : quad.vertices) {
                matrix.transformPosition(vertex.pos, transformed);
                bounds[0] = Math.min(bounds[0], transformed.x);
                bounds[1] = Math.min(bounds[1], transformed.y);
                bounds[2] = Math.min(bounds[2], transformed.z);
                bounds[3] = Math.max(bounds[3], transformed.x);
                bounds[4] = Math.max(bounds[4], transformed.y);
                bounds[5] = Math.max(bounds[5], transformed.z);
            }
        }
    }
}
