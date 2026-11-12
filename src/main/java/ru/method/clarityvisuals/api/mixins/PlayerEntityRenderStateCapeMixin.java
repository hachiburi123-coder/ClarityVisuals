package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CapeSimulation;
import ru.method.clarityvisuals.api.accessor.CapeSimulationStateAccessor;

@Mixin(value={class_10055.class})
public class PlayerEntityRenderStateCapeMixin
implements CapeSimulationStateAccessor {
    @Unique
    private CapeSimulation clarityvisuals$capeSimulation;
    @Unique
    private float clarityvisuals$capeTickDelta;
    @Unique
    private boolean clarityvisuals$capeCrouching;

    @Override
    public CapeSimulation clarityvisuals$capeSimulation() {
        return this.clarityvisuals$capeSimulation;
    }

    @Override
    public void clarityvisuals$setCapeSimulation(CapeSimulation capeSimulation) {
        this.clarityvisuals$capeSimulation = capeSimulation;
    }

    @Override
    public float clarityvisuals$capeTickDelta() {
        return this.clarityvisuals$capeTickDelta;
    }

    @Override
    public void clarityvisuals$setCapeTickDelta(float f) {
        this.clarityvisuals$capeTickDelta = f;
    }

    @Override
    public boolean clarityvisuals$capeCrouching() {
        return this.clarityvisuals$capeCrouching;
    }

    @Override
    public void clarityvisuals$setCapeCrouching(boolean bl) {
        this.clarityvisuals$capeCrouching = bl;
    }
}

