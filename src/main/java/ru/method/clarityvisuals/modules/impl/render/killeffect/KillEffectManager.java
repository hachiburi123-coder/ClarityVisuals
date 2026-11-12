package ru.method.clarityvisuals.modules.impl.render.killeffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_243;

/**
 * Tracks the kill effects that are currently playing. Effects are timestamped
 * when spawned and removed once their duration has elapsed.
 */
public final class KillEffectManager {
    private static final int MAX_ACTIVE = 24;
    private static final List<ActiveEffect> ACTIVE = new ArrayList<>();

    private KillEffectManager() {
    }

    public record ActiveEffect(KillEffectCatalog.Effect effect, class_243 position, long startMillis, float spawnYaw) {
        public float timeSeconds() {
            return (float) (System.currentTimeMillis() - this.startMillis) / 1000.0F;
        }

        public boolean isFinished() {
            return this.timeSeconds() >= this.effect.duration();
        }

        public long seed() {
            return Double.doubleToLongBits(this.position.field_1352)
                    ^ Long.rotateLeft(Double.doubleToLongBits(this.position.field_1350), 19)
                    ^ Long.rotateLeft(this.startMillis, 37);
        }
    }

    public static void spawn(KillEffectCatalog.Effect effect, class_243 position, float spawnYaw) {
        if (effect == null || position == null) {
            return;
        }
        if (ACTIVE.size() >= MAX_ACTIVE) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new ActiveEffect(effect, position, System.currentTimeMillis(), spawnYaw));
    }

    /**
     * Live effects, pruning the finished ones. The returned list is the mutable
     * backing list so callers should not add to it.
     */
    public static List<ActiveEffect> active() {
        Iterator<ActiveEffect> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isFinished()) {
                iterator.remove();
            }
        }
        return ACTIVE;
    }

    public static void despawn(ActiveEffect effect) {
        ACTIVE.remove(effect);
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
