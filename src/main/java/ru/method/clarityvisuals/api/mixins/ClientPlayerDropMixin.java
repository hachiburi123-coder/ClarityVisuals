package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.player.LockSlotModule;

@Mixin(value={class_746.class})
public abstract class ClientPlayerDropMixin {
    @Inject(method={"dropSelectedItem(Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$lockSlotDrop(boolean bl, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        LockSlotModule lockSlotModule = ClarityVisuals.getInstance().getModuleManager().getModule(LockSlotModule.class);
        if (lockSlotModule == null || !lockSlotModule.isToggled()) {
            return;
        }
        class_746 class_7462 = (class_746)(Object)this;
        if (class_7462.method_31548() != null && lockSlotModule.y(class_7462.method_31548().field_7545)) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}

