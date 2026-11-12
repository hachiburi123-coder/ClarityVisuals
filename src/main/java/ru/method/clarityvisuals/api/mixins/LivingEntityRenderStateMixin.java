package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_10042;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.method.clarityvisuals.api.accessor.ItemEntityRenderStateAccessor;

@Mixin(value={class_10042.class})
public class LivingEntityRenderStateMixin
implements ItemEntityRenderStateAccessor {
    @Unique
    private boolean clarityvisuals$isPlayer;
    @Unique
    private float clarityvisuals$hitColorStrength;

    @Override
    public boolean clarityvisuals$isPlayer() {
        return this.clarityvisuals$isPlayer;
    }

    @Override
    public void clarityvisuals$setPlayer(boolean bl) {
        this.clarityvisuals$isPlayer = bl;
    }

    @Override
    public float clarityvisuals$getHitColorStrength() {
        return this.clarityvisuals$hitColorStrength;
    }

    @Override
    public void clarityvisuals$setHitColorStrength(float f) {
        this.clarityvisuals$hitColorStrength = f;
    }
}

