package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5617;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.client.AllSkinsModule;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticFeatureRenderer;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticPreviewContext;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticsService;
import ru.method.clarityvisuals.util.PlayerNameSanitizer;
import ru.method.clarityvisuals.modules.impl.render.SelfNametagsModule;
import ru.method.clarityvisuals.util.StreamerTextSanitizer;

@Mixin(value={class_1007.class})
public abstract class PlayerEntityRendererMixin
extends class_922<class_742, class_10055, class_591> {
    protected PlayerEntityRendererMixin(class_5617.class_5618 class_56182, class_591 class_5912, float f) {
        super(class_56182, class_5912, f);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void clarityvisuals$registerCosmeticFeatures(class_5617.class_5618 class_56182, boolean bl, CallbackInfo callbackInfo) {
        this.method_4046(new CosmeticFeatureRenderer((class_3883<class_10055, class_591>)this));
    }

    @Inject(method={"updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"}, at={@At(value="RETURN")})
    private void clarityvisuals$applyAllSkins(class_742 class_7422, class_10055 class_100552, float f, CallbackInfo callbackInfo) {
        if (class_100552 == null || class_100552.field_53520 == null) {
            return;
        }
        class_2960 class_29602 = AllSkinsModule.it();
        if (class_29602 == null) {
            return;
        }
        class_8685 class_86852 = class_100552.field_53520;
        class_100552.field_53520 = new class_8685(class_29602, class_86852.comp_1911(), class_86852.comp_1627(), class_86852.comp_1628(), class_86852.comp_1629(), class_86852.comp_1630());
    }

    @Inject(method={"updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V"}, at={@At(value="RETURN")})
    private void clarityvisuals$sanitizeSelfRenderState(class_742 class_7422, class_10055 class_100552, float f, CallbackInfo callbackInfo) {
        boolean bl;
        if (class_100552 == null) {
            return;
        }
        if (CosmeticPreviewContext.aP()) {
            class_100552.field_53337 = null;
            class_100552.field_53525 = null;
            class_100552.field_53529 = null;
            return;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        class_310 class_3102 = class_310.method_1551();
        if (clarityVisuals == null || class_3102 == null || class_3102.field_1724 == null || class_7422 != class_3102.field_1724) {
            return;
        }
        SelfNametagsModule selfNametagsModule = clarityVisuals.getModuleManager().getModule(SelfNametagsModule.class);
        boolean bl2 = bl = selfNametagsModule != null && selfNametagsModule.isToggle();
        if (!bl) {
            return;
        }
        class_100552.field_53334 = false;
        if (class_100552.field_53337 != null) {
            class_100552.field_53337 = StreamerTextSanitizer.n(class_100552.field_53337);
        }
    }

    @Inject(method={"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$skipCosmeticPreviewPlayerLabel(CallbackInfo callbackInfo) {
        if (CosmeticPreviewContext.aP()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, require=0)
    private void clarityvisuals$beginSelfNametagRender(class_10055 class_100552, class_2561 class_25612, class_4587 class_45872, class_4597 class_45972, int n, CallbackInfo callbackInfo) {
        if (CosmeticPreviewContext.aP()) {
            return;
        }
        if (this.clarityvisuals$isSelfNametagState(class_100552)) {
            StreamerTextSanitizer.sz();
        }
    }

    @Inject(method={"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="RETURN")}, require=0)
    private void clarityvisuals$endSelfNametagRender(class_10055 class_100552, class_2561 class_25612, class_4587 class_45872, class_4597 class_45972, int n, CallbackInfo callbackInfo) {
        if (this.clarityvisuals$isSelfNametagState(class_100552)) {
            StreamerTextSanitizer.sA();
        }
    }

    @ModifyVariable(method={"renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0, require=0)
    private class_2561 clarityvisuals$sanitizePlayerLabel(class_2561 class_25612, class_10055 class_100552, class_2561 class_25613, class_4587 class_45872, class_4597 class_45972, int n) {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        class_310 class_3102 = class_310.method_1551();
        class_2561 class_25614 = class_25612;
        if (clarityVisuals != null && class_3102 != null && class_3102.field_1724 != null && class_100552 != null) {
            SelfNametagsModule selfNametagsModule = clarityVisuals.getModuleManager().getModule(SelfNametagsModule.class);
            String string = class_3102.field_1724.method_7334().getName();
            class_25614 = selfNametagsModule != null && selfNametagsModule.isToggle() && string != null && class_100552.field_53529 != null && class_100552.field_53529.equalsIgnoreCase(string) ? StreamerTextSanitizer.n(class_25612) : StreamerTextSanitizer.m(class_25612);
        } else {
            class_25614 = StreamerTextSanitizer.m(class_25612);
        }
        if (class_100552 != null && class_100552.field_53529 != null) {
            class_25614 = PlayerNameSanitizer.b(class_25614, null, class_100552.field_53529);
        }
        return class_25614;
    }

    private boolean clarityvisuals$isSelfNametagState(class_10055 class_100552) {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        class_310 class_3102 = class_310.method_1551();
        if (clarityVisuals == null || class_3102 == null || class_3102.field_1724 == null || class_100552 == null) {
            return false;
        }
        SelfNametagsModule selfNametagsModule = clarityVisuals.getModuleManager().getModule(SelfNametagsModule.class);
        String string = class_3102.field_1724.method_7334().getName();
        return selfNametagsModule != null && selfNametagsModule.isToggle() && string != null && class_100552.field_53529 != null && class_100552.field_53529.equalsIgnoreCase(string);
    }
}
