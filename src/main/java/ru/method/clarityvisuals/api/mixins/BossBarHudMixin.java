package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_332;
import net.minecraft.class_337;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.NoRenderModule;

@Mixin(value={class_337.class})
public abstract class BossBarHudMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    public void preRender(class_332 class_3322, CallbackInfo callbackInfo) {
        NoRenderModule noRenderModule = ClarityVisuals.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule != null && noRenderModule.isToggled() && ((Boolean)noRenderModule.Fd.jT()).booleanValue()) {
            callbackInfo.cancel();
        }
    }
}
