package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.events.impl.EventMouse;
import ru.method.clarityvisuals.modules.impl.render.FreeLookModule;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion.EmotionWheelManager;
import ru.method.clarityvisuals.hud.HudManager;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.api.event.MouseScrollEvent;

@Mixin(value={class_312.class})
public abstract class MouseMixin {
    @Shadow
    private class_310 field_1779;

    @Inject(method={"onMouseButton"}, at={@At(value="HEAD")}, cancellable=true)
    public void onMouseButton(long l, int n, int n2, int n3, CallbackInfo callbackInfo) {
        HudManager hudManager;
        EventMouse eventMouse = new EventMouse(n, n2);
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals != null) {
            hudManager = clarityVisuals.getHudManager();
            if (hudManager != null && hudManager.a(eventMouse)) {
                callbackInfo.cancel();
                return;
            }
            clarityVisuals.getEventHandler().post((Object)eventMouse);
        }
        FreeLookModule freeLookModule = clarityVisuals != null ? clarityVisuals.getModuleManager().getModule(FreeLookModule.class) : null;
        if (freeLookModule != null) {
            freeLookModule.k(n, n2);
        }
        if (eventMouse.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"onMouseScroll"}, at={@At(value="HEAD")}, cancellable=true)
    public void onMouseScroll(long l, double d, double d2, CallbackInfo callbackInfo) {
        MouseScrollEvent mouseScrollEvent = new MouseScrollEvent(d, d2);
        ClarityVisuals.getInstance().getEventHandler().post((Object)mouseScrollEvent);
        if (mouseScrollEvent.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"updateMouse"}, at={@At(value="HEAD")})
    private void clarityvisuals$captureFreelookRotation(double d, CallbackInfo callbackInfo) {
        class_746 class_7462 = this.field_1779.field_1724;
        if (class_7462 == null) {
            return;
        }
        FreeLookModule freeLookModule = ClarityVisuals.getInstance().getModuleManager().getModule(FreeLookModule.class);
        if (freeLookModule != null) {
            freeLookModule.a(class_7462);
        }
        EmotionWheelManager.INSTANCE.captureLook(class_7462);
    }

    @Inject(method={"updateMouse"}, at={@At(value="RETURN")})
    private void clarityvisuals$restoreFreelookRotation(double d, CallbackInfo callbackInfo) {
        class_746 class_7462 = this.field_1779.field_1724;
        if (class_7462 == null) {
            return;
        }
        FreeLookModule freeLookModule = ClarityVisuals.getInstance().getModuleManager().getModule(FreeLookModule.class);
        if (freeLookModule != null) {
            freeLookModule.b(class_7462);
        }
        EmotionWheelManager.INSTANCE.restoreLook(class_7462);
    }
}
