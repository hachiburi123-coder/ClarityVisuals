package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_324;
import net.minecraft.class_3481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.modules.impl.render.SakuraModule;

/**
 * Re-tints every vanilla leaf block while Sakura is active. The original
 * brightness is preserved, so forests keep their depth and leaf texture detail
 * instead of turning into a flat pink silhouette.
 */
@Mixin(value={class_324.class})
public abstract class SakuraLeavesMixin {
    @Inject(method={"method_1697"}, at={@At(value="RETURN")}, cancellable=true)
    private void clarityvisuals$pinkLeaves(class_2680 state, class_1920 world, class_2338 pos, int tintIndex,
            CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (SakuraModule.shouldTintLeaves() && state.method_26164(class_3481.field_15503)) {
            int original = (Integer) callbackInfoReturnable.getReturnValue();
            callbackInfoReturnable.setReturnValue(SakuraModule.tintLeafColor(original));
        }
    }

    @Inject(method={"method_1691"}, at={@At(value="RETURN")}, cancellable=true)
    private void clarityvisuals$pinkLeafParticles(class_2680 state, net.minecraft.class_1937 world, class_2338 pos,
            CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (SakuraModule.shouldTintLeaves() && state.method_26164(class_3481.field_15503)) {
            int original = (Integer) callbackInfoReturnable.getReturnValue();
            callbackInfoReturnable.setReturnValue(SakuraModule.tintLeafColor(original));
        }
    }
}
