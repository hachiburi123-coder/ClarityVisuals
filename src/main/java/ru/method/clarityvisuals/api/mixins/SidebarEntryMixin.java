package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.util.PlayerNameSanitizer;

@Mixin(targets={"net.minecraft.class_329$class_9016"})
public class SidebarEntryMixin {
    @Inject(method={"comp_2127", "name"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void clarityvisuals$sanitizeSidebarName(CallbackInfoReturnable<class_2561> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(PlayerNameSanitizer.r((class_2561)callbackInfoReturnable.getReturnValue()));
    }

    @Inject(method={"comp_2128", "score"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void clarityvisuals$sanitizeSidebarScore(CallbackInfoReturnable<class_2561> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(PlayerNameSanitizer.r((class_2561)callbackInfoReturnable.getReturnValue()));
    }
}
