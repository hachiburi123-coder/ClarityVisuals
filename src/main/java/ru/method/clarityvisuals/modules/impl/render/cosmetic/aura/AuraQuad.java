package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import org.joml.Vector3f;

public final class AuraQuad {
    public final AuraVertex[] vertices = new AuraVertex[4];
    public Vector3f normal = new Vector3f(0.0f, 1.0f, 0.0f);

    public AuraQuad(AuraVertex vertex1, AuraVertex vertex2, AuraVertex vertex3, AuraVertex vertex4, Vector3f normalizedNormal) {
        this.vertices[0] = vertex1;
        this.vertices[1] = vertex2;
        this.vertices[2] = vertex3;
        this.vertices[3] = vertex4;
        this.normal = normalizedNormal;
    }
}
