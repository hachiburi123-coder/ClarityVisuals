package ru.method.clarityvisuals.api.mixins;

import java.awt.Color;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.CustomHitBoxModule;
import ru.method.clarityvisuals.modules.impl.render.AntiInvisModule;
import ru.method.clarityvisuals.managers.system.ModuleManager;
import ru.method.clarityvisuals.utils.render.ColorUtils;

@Mixin(value={class_898.class})
public class EntityRenderDispatcherMixin {
    @Unique
    private static Color cachedColor = null;
    @Unique
    private static long lastColorUpdate = 0L;
    @Unique
    private static final long COLOR_CACHE_TIME = 16L;

    @Unique
    private static boolean isCustomHitBoxActive() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return false;
        }
        ModuleManager moduleManager = clarityVisuals.getModuleManager();
        if (moduleManager == null) {
            return false;
        }
        CustomHitBoxModule customHitBoxModule = moduleManager.getModule(CustomHitBoxModule.class);
        return customHitBoxModule != null && customHitBoxModule.isToggled();
    }

    @Unique
    private static Color getHitboxColor() {
        long l = System.currentTimeMillis();
        if (cachedColor == null || l - lastColorUpdate > 16L) {
            cachedColor = ColorUtils.getGlobalColor();
            lastColorUpdate = l;
        }
        return cachedColor;
    }

    @Inject(method={"renderHitbox"}, at={@At(value="HEAD")}, cancellable=true)
    private static void cancelSelfHitbox(class_4587 class_45872, class_4588 class_45882, class_1297 class_12972, float f, float f2, float f3, float f4, CallbackInfo callbackInfo) {
        ClarityVisuals clarityVisuals;
        block12: {
            block11: {
                block10: {
                    block9: {
                        clarityVisuals = ClarityVisuals.getInstance();
                        if (clarityVisuals == null || class_12972 == null) break block9;
                        if (ClarityVisuals.aiG.field_1724 != null) break block10;
                    }
                    return;
                }
                if (ClarityVisuals.aiG.field_1724 == null) break block11;
                if (class_12972.method_5667().equals(ClarityVisuals.aiG.field_1724.method_5667())) break block12;
            }
            return;
        }
        ModuleManager moduleManager = clarityVisuals.getModuleManager();
        if (moduleManager == null) {
            return;
        }
        CustomHitBoxModule customHitBoxModule = moduleManager.getModule(CustomHitBoxModule.class);
        if (customHitBoxModule != null) {
            boolean bl = ClarityVisuals.aiG.field_1690.method_31044().method_31034();
            if (!customHitBoxModule.iy() || bl) {
                callbackInfo.cancel();
            }
        }
    }

    @ModifyVariable(method={"renderHitbox"}, at=@At(value="HEAD"), ordinal=1, argsOnly=true)
    private static float modifyRed(float f) {
        if (!EntityRenderDispatcherMixin.isCustomHitBoxActive()) {
            cachedColor = null;
            return f;
        }
        return (float)EntityRenderDispatcherMixin.getHitboxColor().getRed() / 255.0f;
    }

    @ModifyVariable(method={"renderHitbox"}, at=@At(value="HEAD"), ordinal=2, argsOnly=true)
    private static float modifyGreen(float f) {
        if (!EntityRenderDispatcherMixin.isCustomHitBoxActive()) {
            return f;
        }
        return (float)EntityRenderDispatcherMixin.getHitboxColor().getGreen() / 255.0f;
    }

    @ModifyVariable(method={"renderHitbox"}, at=@At(value="HEAD"), ordinal=3, argsOnly=true)
    private static float modifyBlue(float f) {
        if (!EntityRenderDispatcherMixin.isCustomHitBoxActive()) {
            return f;
        }
        return (float)EntityRenderDispatcherMixin.getHitboxColor().getBlue() / 255.0f;
    }

    /**
     * AntiInvis: как в старом клиенте — подмена чтения {@code EntityRenderState.invisible}
     * в {@code EntityRenderDispatcher.render}. Возвращаем {@code false} для всего, кроме
     * ArmorStand, поэтому невидимые игроки/мобы отбрасывают ванильную тень.
     * Таргет без ordinal: подменяются ОБА чтения (тень + неймтег-гейт), как в старом клиенте.
     */
    @ModifyExpressionValue(
        method={"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"},
        at={@At(value="FIELD", target="Lnet/minecraft/client/render/entity/state/EntityRenderState;invisible:Z")}
    )
    private boolean clarityvisuals$antiInvisShadowGate(boolean original, @Local(ordinal=0, argsOnly=true) class_1297 entity) {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return original;
        }
        ModuleManager moduleManager = clarityVisuals.getModuleManager();
        if (moduleManager == null) {
            return original;
        }
        AntiInvisModule antiInvisModule = moduleManager.getModule(AntiInvisModule.class);
        if (antiInvisModule == null || !antiInvisModule.isToggled()) {
            return original;
        }
        return entity instanceof class_1531;
    }
}

