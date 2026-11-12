package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_276;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.client.hud.HudEntityPreview;

@Mixin(value={class_310.class})
public abstract class MinecraftClientFramebufferMixin {
    @Inject(method={"getFramebuffer"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityRedirectPreviewFramebuffer(CallbackInfoReturnable<class_276> callbackInfoReturnable) {
        class_276 class_2762 = HudEntityPreview.getFramebufferOverride();
        if (class_2762 != null) {
            callbackInfoReturnable.setReturnValue(class_2762);
        }
    }
}

