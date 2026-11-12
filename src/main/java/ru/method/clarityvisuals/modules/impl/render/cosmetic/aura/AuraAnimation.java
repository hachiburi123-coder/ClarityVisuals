package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import java.util.HashMap;
import java.util.Map;
import org.joml.Vector3f;

public final class AuraAnimation {
    public final String name;
    public final float length;
    public final boolean loop;
    public final Map<String, AuraBoneTimeline> bones = new HashMap<String, AuraBoneTimeline>();

    public AuraAnimation(String name, float length, boolean loop) {
        this.name = name;
        this.length = length > 0.0f ? length : 1.0f;
        this.loop = loop;
    }

    public void apply(String boneName, float timeSeconds, Vector3f outRot, Vector3f outPos) {
        AuraBoneTimeline timeline = this.bones.get(boneName);
        if (timeline != null) {
            float t = this.loop ? timeSeconds % this.length : Math.min(timeSeconds, this.length);
            timeline.evaluateRotation(t, outRot);
            timeline.evaluatePosition(t, outPos);
        }
    }
}
