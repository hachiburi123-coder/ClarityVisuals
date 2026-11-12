package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.NoRenderModule;

@Mixin(value={class_4603.class})
public abstract class InGameOverlayRendererMixin {
    @Inject(method={"renderFireOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private static void renderFireOverlay(class_4587 class_45872, class_4597 class_45972, CallbackInfo callbackInfo) {
        NoRenderModule noRenderModule = ClarityVisuals.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule != null && noRenderModule.isToggled() && ((Boolean)noRenderModule.Fb.jT()).booleanValue()) {
            callbackInfo.cancel();
        }
    }
}

