package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_243;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.ShaderFogModule;

@Mixin(value={class_638.class})
public abstract class ClientWorldMixin {
    @Inject(method={"getSkyColor(Lnet/minecraft/util/math/Vec3d;F)I"}, at={@At(value="RETURN")}, cancellable=true)
    private void clarityvisuals$skyColorHook(class_243 class_2432, float f, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        ShaderFogModule shaderFogModule = ClarityVisuals.getInstance().getModuleManager().getModule(ShaderFogModule.class);
        if (shaderFogModule != null && shaderFogModule.isToggled()) {
            callbackInfoReturnable.setReturnValue(shaderFogModule.ae((Integer)callbackInfoReturnable.getReturnValue()));
        }
    }
}

