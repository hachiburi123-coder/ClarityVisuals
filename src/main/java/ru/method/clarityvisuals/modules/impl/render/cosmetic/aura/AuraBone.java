package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class AuraBone {
    public String name;
    public String parent;
    public float[] pivot = new float[]{0.0f, 0.0f, 0.0f};
    public float[] rotation = new float[]{0.0f, 0.0f, 0.0f};
    public final List<AuraCube> cubes = new ArrayList<AuraCube>();
    public final List<AuraBone> children = new ArrayList<AuraBone>();

    private void applyTransform(class_4587 matrices, Vector3f animRot, Vector3f animPos) {
        float pivotX = this.pivot[0] / 16.0f;
        float pivotY = this.pivot[1] / 16.0f;
        float pivotZ = this.pivot[2] / 16.0f;
        matrices.method_46416(pivotX, pivotY, pivotZ);
        if (animPos != null && (animPos.x != 0.0f || animPos.y != 0.0f || animPos.z != 0.0f)) {
            matrices.method_46416(animPos.x / 16.0f, animPos.y / 16.0f, animPos.z / 16.0f);
        }

        float rotZ = this.rotation[2] + (animRot != null ? animRot.z : 0.0f);
        float rotY = this.rotation[1] + (animRot != null ? animRot.y : 0.0f);
        float rotX = this.rotation[0] + (animRot != null ? animRot.x : 0.0f);
        if (rotZ != 0.0f) {
            matrices.method_22907(class_7833.field_40718.rotationDegrees(rotZ));
        }

        if (rotY != 0.0f) {
            matrices.method_22907(class_7833.field_40716.rotationDegrees(rotY));
        }

        if (rotX != 0.0f) {
            matrices.method_22907(class_7833.field_40714.rotationDegrees(rotX));
        }

        matrices.method_46416(-pivotX, -pivotY, -pivotZ);
    }

    public void render(class_4587 matrices, class_4588 consumer, int light, int overlay, float red, float green, float blue, float alpha, AuraAnimation animation, float animTime) {
        matrices.method_22903();
        Vector3f animRot = null;
        Vector3f animPos = null;
        if (animation != null && this.name != null) {
            animRot = new Vector3f();
            animPos = new Vector3f();
            animation.apply(this.name, animTime, animRot, animPos);
        }

        this.applyTransform(matrices, animRot, animPos);
        for (AuraCube cube : this.cubes) {
            cube.render(matrices, consumer, light, overlay, red, green, blue, alpha);
        }

        for (AuraBone child : this.children) {
            child.render(matrices, consumer, light, overlay, red, green, blue, alpha, animation, animTime);
        }

        matrices.method_22909();
    }

    public void collectBounds(Matrix4f parentMatrix, float[] bounds) {
        Matrix4f matrix = new Matrix4f(parentMatrix);
        float pivotX = this.pivot[0] / 16.0f;
        float pivotY = this.pivot[1] / 16.0f;
        float pivotZ = this.pivot[2] / 16.0f;
        matrix.translate(pivotX, pivotY, pivotZ);
        if (this.rotation[2] != 0.0f) {
            matrix.rotateZ((float)Math.toRadians(this.rotation[2]));
        }

        if (this.rotation[1] != 0.0f) {
            matrix.rotateY((float)Math.toRadians(this.rotation[1]));
        }

        if (this.rotation[0] != 0.0f) {
            matrix.rotateX((float)Math.toRadians(this.rotation[0]));
        }

        matrix.translate(-pivotX, -pivotY, -pivotZ);

        for (AuraCube cube : this.cubes) {
            cube.collectBounds(matrix, bounds);
        }

        for (AuraBone child : this.children) {
            child.collectBounds(matrix, bounds);
        }
    }
}
