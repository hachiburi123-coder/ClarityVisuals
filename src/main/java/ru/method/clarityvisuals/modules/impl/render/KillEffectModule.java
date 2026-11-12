package ru.method.clarityvisuals.modules.impl.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2663;
import net.minecraft.class_8143;
import ru.method.clarityvisuals.api.event.AttackEntityEvent;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.api.event.PacketEvent;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectCatalog;
import ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectManager;
import ru.method.clarityvisuals.modules.impl.render.killeffect.KillEffectRenderer;

/**
 * Plays the selected kill effect at the position where a player's victim died.
 * Kill detection is unchanged: the module remembers who the local player hit,
 * follows that entity until it dies and then hands the last known position to
 * {@link KillEffectManager}.
 */
public class KillEffectModule
extends Module {
    private static final long TRACK_LIFETIME = 4500L;

    private final EnumSetting<KillEffectCatalog.Effect> mode =
            new EnumSetting<>("settings.killeffect.mode", KillEffectCatalog.Effect.TETRIS_SMASH);
    private final Map<Integer, Shard> tracked = new HashMap<>();

    public KillEffectModule() {
        super("KillEffect", ModuleCategory.ql);
        this.getSettings().add(this.mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.tracked.clear();
        KillEffectManager.clear();
    }

    @Override
    public void onDisable() {
        this.tracked.clear();
        KillEffectManager.clear();
        super.onDisable();
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        if (KillEffectModule.fullNullCheck()) {
            return;
        }
        if (event.a() != KillEffectModule.aiG.field_1724) {
            return;
        }
        class_1297 target = event.b();
        if (!(target instanceof class_1309 living) || living == KillEffectModule.aiG.field_1724) {
            return;
        }
        this.tracked.put(living.method_5628(), new Shard(System.currentTimeMillis(), living.method_19538()));
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (KillEffectModule.fullNullCheck()) {
            return;
        }
        class_2596<?> packet = event.i();
        if (packet instanceof class_8143 damage) {
            this.onDamagePacket(damage);
        } else if (packet instanceof class_2663 status && status.method_11470() == 3) {
            this.onDeathStatus(status);
        }
    }

    private void onDamagePacket(class_8143 packet) {
        if (KillEffectModule.aiG.field_1687 == null || KillEffectModule.aiG.field_1724 == null) {
            return;
        }
        int victimId = packet.comp_1267();
        if (victimId == KillEffectModule.aiG.field_1724.method_5628()) {
            return;
        }
        int selfId = KillEffectModule.aiG.field_1724.method_5628();
        boolean attackerIsSelf = packet.comp_1269() == selfId || packet.comp_1270() == selfId;
        class_1297 owner = KillEffectModule.aiG.field_1687.method_8469(packet.comp_1270());
        if (!attackerIsSelf && !this.belongsToSelf(owner)) {
            return;
        }
        class_1297 entity = KillEffectModule.aiG.field_1687.method_8469(victimId);
        if (!(entity instanceof class_1309 living) || living == KillEffectModule.aiG.field_1724) {
            return;
        }
        this.tracked.put(victimId, new Shard(System.currentTimeMillis(), living.method_19538()));
    }

    private void onDeathStatus(class_2663 packet) {
        if (KillEffectModule.aiG.field_1687 == null) {
            return;
        }
        class_1297 entity = packet.method_11469((class_1937) KillEffectModule.aiG.field_1687);
        if (!(entity instanceof class_1309 living)) {
            return;
        }
        Shard shard = this.tracked.remove(living.method_5628());
        if (shard == null) {
            return;
        }
        this.trigger(shard.position != null ? shard.position : living.method_19538());
    }

    @EventHandler
    private void onTick(ClientTickEvent event) {
        if (KillEffectModule.fullNullCheck()) {
            this.tracked.clear();
            KillEffectManager.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Shard>> iterator = this.tracked.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Shard> entry = iterator.next();
            Shard shard = entry.getValue();
            if (now - shard.time > TRACK_LIFETIME) {
                iterator.remove();
                continue;
            }
            class_1297 entity = KillEffectModule.aiG.field_1687.method_8469(entry.getKey());
            if (!(entity instanceof class_1309 living)) {
                iterator.remove();
                continue;
            }
            if (living.method_5805() && living.method_6032() > 0.0f) {
                shard.position = living.method_19538();
                continue;
            }
            this.trigger(shard.position != null ? shard.position : living.method_19538());
            iterator.remove();
        }
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage stage) {
        if (KillEffectModule.fullNullCheck()) {
            return;
        }
        KillEffectRenderer.render(stage.l(), KillEffectManager.active());
    }

    private void trigger(class_243 position) {
        if (position == null) {
            return;
        }
        float yaw = KillEffectModule.aiG.field_1724 != null ? KillEffectModule.aiG.field_1724.method_36454() : 0.0f;
        KillEffectManager.spawn((KillEffectCatalog.Effect) this.mode.jT(), position, yaw);
    }

    private boolean belongsToSelf(class_1297 entity) {
        if (entity instanceof class_1657 player) {
            return player.method_5667().equals(KillEffectModule.aiG.field_1724.method_5667());
        }
        return entity == KillEffectModule.aiG.field_1724;
    }

    private static final class Shard {
        final long time;
        class_243 position;

        Shard(long time, class_243 position) {
            this.time = time;
            this.position = position;
        }
    }
}
