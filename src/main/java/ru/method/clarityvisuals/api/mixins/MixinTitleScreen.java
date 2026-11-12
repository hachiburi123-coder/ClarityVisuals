package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.screen.mainmenu.CustomMainMenuScreen;

@Mixin(value={class_442.class})
public abstract class MixinTitleScreen
implements MinecraftClientAccess {
    @Inject(method={"init"}, at={@At(value="RETURN")})
    private void clarityvisuals$redirectToCustom(CallbackInfo callbackInfo) {
        if (aiG == null) {
            return;
        }
        if (MixinTitleScreen.aiG.field_1755 instanceof CustomMainMenuScreen) {
            return;
        }
        aiG.execute(() -> {
            class_437 class_4372 = MixinTitleScreen.aiG.field_1755;
            if (class_4372 instanceof class_442 && !(class_4372 instanceof CustomMainMenuScreen)) {
                aiG.method_1507((class_437)new CustomMainMenuScreen());
            }
        });
    }
}

