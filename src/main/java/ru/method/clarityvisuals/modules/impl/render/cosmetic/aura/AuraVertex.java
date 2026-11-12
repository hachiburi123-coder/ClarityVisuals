package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import org.joml.Vector3f;

public final class AuraVertex {
    public final Vector3f pos;
    public final float u;
    public final float v;

    public AuraVertex(float x, float y, float z, float u, float v) {
        this.pos = new Vector3f(x, y, z);
        this.u = u;
        this.v = v;
    }
}
