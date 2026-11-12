package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_2561;
import net.minecraft.class_267;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.util.StreamerTextSanitizer;

@Mixin(value={class_267.class})
public class ScoreboardScoreMixin {
    @Inject(method={"getDisplayText"}, at={@At(value="RETURN")}, cancellable=true)
    private void clarityvisuals$sanitizeDisplayText(CallbackInfoReturnable<class_2561> callbackInfoReturnable) {
        StreamerTextSanitizer.j((class_2561)callbackInfoReturnable.getReturnValue());
        callbackInfoReturnable.setReturnValue(StreamerTextSanitizer.l((class_2561)callbackInfoReturnable.getReturnValue()));
    }
}

