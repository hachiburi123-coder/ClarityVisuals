package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_1657;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CapeSimulation;
import ru.method.clarityvisuals.api.accessor.CapeSimulationAccessor;

@Mixin(value={class_1657.class})
public class PlayerEntityCapeMixin
implements CapeSimulationAccessor {
    @Unique
    private CapeSimulation clarityvisuals$capeSimulation;

    @Override
    public CapeSimulation clarityvisuals$capeSimulation() {
        if (this.clarityvisuals$capeSimulation == null) {
            this.clarityvisuals$capeSimulation = new CapeSimulation();
        }
        return this.clarityvisuals$capeSimulation;
    }

    @Inject(method={"updateCapeAngles"}, at={@At(value="HEAD")})
    private void clarityvisuals$tickCapeSimulation(CallbackInfo callbackInfo) {
        PlayerEntityCapeMixin playerEntityCapeMixin = this;
        if ((Object)playerEntityCapeMixin instanceof class_742) {
            class_742 class_7422 = (class_742)(Object)playerEntityCapeMixin;
            this.clarityvisuals$capeSimulation().c((class_1657)class_7422);
        }
    }
}
