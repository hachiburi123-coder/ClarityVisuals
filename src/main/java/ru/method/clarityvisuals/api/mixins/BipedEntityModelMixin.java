package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_572;
import net.minecraft.class_630;
import net.minecraft.class_10034;
import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion.EmotionWheelManager;

@Mixin(value={class_572.class})
public abstract class BipedEntityModelMixin {
    @Shadow
    public class_630 field_3398;
    @Shadow
    public class_630 field_3394;
    @Shadow
    public class_630 field_3391;
    @Shadow
    public class_630 field_3401;
    @Shadow
    public class_630 field_27433;
    @Shadow
    public class_630 field_3392;
    @Shadow
    public class_630 field_3397;

    @Inject(method={"method_17087(Lnet/minecraft/class_10034;)V"}, at={@At(value="RETURN")}, require=0)
    private void clarityvisuals$emotionsAfterSetAngles(class_10034 class_100342, CallbackInfo callbackInfo) {
        if (class_100342 instanceof class_10055) {
            class_10055 class_100552 = (class_10055)class_100342;
            EmotionWheelManager.INSTANCE.applyToModel(class_100552, this.field_3398, this.field_3394, this.field_3391, this.field_3401, this.field_27433, this.field_3392, this.field_3397);
        }
    }
}
