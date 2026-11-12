package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import org.joml.Vector3f;

public final class AuraKeyframe {
    public final float time;
    public final Vector3f value;

    public AuraKeyframe(float time, float x, float y, float z) {
        this.time = time;
        this.value = new Vector3f(x, y, z);
    }
}
