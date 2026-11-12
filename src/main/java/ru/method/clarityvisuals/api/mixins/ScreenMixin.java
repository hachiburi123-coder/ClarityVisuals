package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.NoRenderModule;
import ru.method.clarityvisuals.render.ServerEntryRenderer;
import ru.method.clarityvisuals.screen.clickgui.ClickGui;
import ru.method.clarityvisuals.utils.debug.RenderProfiler;
import ru.method.clarityvisuals.utils.render.LiquidGlassReflectionCache;

@Mixin(value={class_437.class})
public abstract class ScreenMixin {
    @Shadow
    protected class_310 field_22787;

    @Inject(method={"setInitialFocus"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$guardNullClient(CallbackInfo callbackInfo) {
        if (this.field_22787 == null) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void clarityvisuals$captureLiquidGlassBackdrop(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        if ((Object)this instanceof ClickGui) {
            LiquidGlassReflectionCache.captureClickGuiBackdrop(true);
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$connectFlowSkin(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        class_437 class_4372 = (class_437)(Object)this;
        if (!ServerEntryRenderer.b(class_4372)) {
            return;
        }
        callbackInfo.cancel();
        ServerEntryRenderer.a(class_3322, n, n2, ServerEntryRenderer.c(class_4372), ServerEntryRenderer.qa(), ServerEntryRenderer.qb(), false);
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void clarityvisuals$renderProfilerOverlay(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        RenderProfiler.renderOverlay(class_3322);
    }

    @Inject(method={"renderInGameBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$hideInventoryBackground(class_332 class_3322, CallbackInfo callbackInfo) {
        if (!((Object)this instanceof class_465)) {
            return;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return;
        }
        NoRenderModule noRenderModule = clarityVisuals.getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule != null && noRenderModule.isToggled() && ((Boolean)noRenderModule.Fg.jT()).booleanValue()) {
            callbackInfo.cancel();
        }
    }
}
