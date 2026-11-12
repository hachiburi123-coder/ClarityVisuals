package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_276;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.client.hud.HudEntityPreview;

@Mixin(value={class_761.class})
public abstract class WorldRendererFramebufferMixin {
    @Inject(method={"getEntityFramebuffer"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityRedirectPreviewEntityFramebuffer(CallbackInfoReturnable<class_276> callbackInfoReturnable) {
        class_276 class_2762 = HudEntityPreview.getFramebufferOverride();
        if (class_2762 != null) {
            callbackInfoReturnable.setReturnValue(class_2762);
        }
    }
}

