package ru.method.clarityvisuals.api.mixins;

import java.awt.Color;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1812;
import net.minecraft.class_1842;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_5481;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.render.CrosshairModule;
import ru.method.clarityvisuals.render.CustomHotbarRenderer;
import ru.method.clarityvisuals.modules.impl.render.CustomVitalsModule;
import ru.method.clarityvisuals.render.CustomVitalsRenderer;
import ru.method.clarityvisuals.hud.PotionHud;
import ru.method.clarityvisuals.hud.ScoreboardHud;
import ru.method.clarityvisuals.modules.impl.render.NoRenderModule;
import ru.method.clarityvisuals.util.StreamerTextSanitizer;
import ru.method.clarityvisuals.ui.UiThemeController;
import ru.method.clarityvisuals.modules.impl.client.UI;
import ru.method.clarityvisuals.screen.clickgui.ClickGui;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.HudRenderFrame;
import ru.method.clarityvisuals.utils.render.LiquidGlassReflectionCache;

@Mixin(value={class_329.class})
public abstract class InGameHudMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void clarityvisuals$beginHudFrame(class_332 class_3322, class_9779 class_97792, CallbackInfo callbackInfo) {
        UiThemeController.pb();
        HudRenderFrame.begin(class_3322, class_97792);
        class_310 class_3102 = class_310.method_1551();
        if (class_3102.field_1755 instanceof ClickGui) {
            LiquidGlassReflectionCache.captureClickGuiBackdrop(true);
        } else if (class_3102.field_1687 != null && ColorUtils.isLiquidGlassShaderActive()) {
            LiquidGlassReflectionCache.captureGameplayForHudFrame();
        }
    }

    @Inject(method={"renderStatusEffectOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$hideVanillaStatusEffectOverlay(class_332 class_3322, class_9779 class_97792, CallbackInfo callbackInfo) {
        PotionHud potionHud = ClarityVisuals.getInstance().getHudManager().a(PotionHud.class);
        if (potionHud != null && potionHud.isToggled() && !potionHud.bL()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderCrosshair"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$renderCustomCrosshair(class_332 class_3322, class_9779 class_97792, CallbackInfo callbackInfo) {
        CrosshairModule crosshairModule = ClarityVisuals.getInstance().getModuleManager().getModule(CrosshairModule.class);
        if (crosshairModule == null || !crosshairModule.isToggled()) {
            return;
        }
        if (crosshairModule.iw()) {
            callbackInfo.cancel();
        }
        crosshairModule.b(class_3322, class_97792);
    }

        @Inject(method={"render"}, at={@At(value="RETURN")})
    public void clarityvisuals$finishHudFrame(class_332 class_3322, class_9779 class_97792, CallbackInfo callbackInfo) {
        try {
            HudRenderFrame.State state = HudRenderFrame.get();
            if (state == null) {
                return;
            }
            if (!state.hudEventPosted()) {
                HudRenderFrame.dispatchHudRender(ClarityVisuals.getInstance().getEventHandler());
            }
            this.clarityvisuals$renderHudWindowOverlay(class_3322);
        }
        finally {
            HudRenderFrame.end();
        }
    }

    @Inject(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderScoreboardSidebar(class_332 class_3322, class_266 class_2662, CallbackInfo callbackInfo) {
        StreamerTextSanitizer.sv();
        HudRenderFrame.tryDispatchBeforeOverlays(ClarityVisuals.getInstance().getEventHandler());
        NoRenderModule noRenderModule = ClarityVisuals.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule != null && noRenderModule.isToggled() && ((Boolean)noRenderModule.Fc.jT()).booleanValue()) {
            callbackInfo.cancel();
            return;
        }
        ScoreboardHud scoreboardHud = ClarityVisuals.getInstance().getHudManager().a(ScoreboardHud.class);
        if (UI.isCustomScoreboardEnabled() && scoreboardHud != null && scoreboardHud.isToggled() && !scoreboardHud.bL()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"method_65023"}, at={@At(value="RETURN")}, cancellable=true)
    private static void clarityvisuals$sanitizeScoreboardText(String string, CallbackInfoReturnable<String> callbackInfoReturnable) {
        StreamerTextSanitizer.dn((String)callbackInfoReturnable.getReturnValue());
        callbackInfoReturnable.setReturnValue(StreamerTextSanitizer.dq((String)callbackInfoReturnable.getReturnValue()));
    }

    @ModifyArg(method={"method_55439"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud$SidebarEntry;<init>(Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;I)V"), index=0, require=0)
    private class_2561 clarityvisuals$sanitizeSidebarName(class_2561 class_25612) {
        StreamerTextSanitizer.j(class_25612);
        return StreamerTextSanitizer.scoreboardGroup(StreamerTextSanitizer.l(class_25612));
    }

    @ModifyArg(method={"method_55439"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/hud/InGameHud$SidebarEntry;<init>(Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;I)V"), index=1, require=0)
    private class_2561 clarityvisuals$sanitizeSidebarScore(class_2561 class_25612) {
        StreamerTextSanitizer.j(class_25612);
        return StreamerTextSanitizer.l(class_25612);
    }

    @ModifyArg(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"), index=1, require=0)
    private class_2561 clarityvisuals$sanitizeSidebarDrawText(class_2561 class_25612) {
        StreamerTextSanitizer.j(class_25612);
        return StreamerTextSanitizer.l(class_25612);
    }

    @ModifyArg(method={"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), index=1, require=0)
    private class_5481 clarityvisuals$sanitizeSidebarDrawOrderedText(class_5481 class_54812) {
        return StreamerTextSanitizer.b(class_54812);
    }

    @Inject(method={"renderHotbar"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void clarityvisuals$customHotbar(class_332 class_3322, class_9779 class_97792, CallbackInfo callbackInfo) {
        if (class_310.method_1551().field_1724 == null) {
            return;
        }
        boolean bl = InGameHudMixin.clarityvisuals$customVitalsActive();
        boolean bl2 = bl;
        if (bl2) {
            CustomHotbarRenderer.a(class_3322, class_97792);
        }
        if (bl) {
            this.clarityvisuals$drawVitals(class_3322);
        }
        if (bl2) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderExperienceBar"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void clarityvisuals$expBarUp(class_332 class_3322, int n, CallbackInfo callbackInfo) {
        if (InGameHudMixin.clarityvisuals$customVitalsActive()) {
            callbackInfo.cancel();
            return;
        }
        class_3322.method_51448().method_22903();
        class_3322.method_51448().method_46416(0.0f, -2.0f, 0.0f);
    }

    @Inject(method={"renderExperienceBar"}, at={@At(value="RETURN")}, require=0)
    private void clarityvisuals$expBarUpPop(class_332 class_3322, int n, CallbackInfo callbackInfo) {
        if (InGameHudMixin.clarityvisuals$customVitalsActive()) {
            return;
        }
        class_3322.method_51448().method_22909();
    }

    @Inject(method={"renderStatusBars"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void clarityvisuals$cancelVanillaStatus(class_332 class_3322, CallbackInfo callbackInfo) {
        if (InGameHudMixin.clarityvisuals$customVitalsActive()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"renderExperienceLevel"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void clarityvisuals$cancelVanillaExpLevel(class_332 class_3322, class_9779 class_97792, CallbackInfo callbackInfo) {
        if (InGameHudMixin.clarityvisuals$customVitalsActive()) {
            callbackInfo.cancel();
        }
    }

    @Unique
    private void clarityvisuals$drawVitals(class_332 class_3322) {
        class_1309 class_13092;
        class_310 class_3102 = class_310.method_1551();
        if (class_3102.field_1724 == null || class_3102.field_1687 == null) {
            return;
        }
        if (class_3102.field_1690.field_1842 || class_3102.field_1724.method_7325()) {
            return;
        }
        if (class_3102.field_1724.method_31549().field_7477) {
            return;
        }
        CustomVitalsModule customVitalsModule = InGameHudMixin.clarityvisuals$vitalsModule();
        if (customVitalsModule == null) {
            return;
        }
        class_746 class_7462 = class_3102.field_1724;
        CustomVitalsRenderer.Layout layout = new CustomVitalsRenderer.Layout();
        layout.fC = customVitalsModule.iz();
        layout.fD = customVitalsModule.iA();
        layout.fE = customVitalsModule.iB();
        float f = Math.max(1.0f, class_7462.method_6063());
        layout.fI = (int)Math.ceil(class_7462.method_6032());
        layout.fF = class_7462.method_6032() / f;
        layout.fJ = class_7462.method_7344().method_7586();
        layout.fG = (float)layout.fJ / 20.0f;
        layout.fH = class_7462.field_7510;
        layout.fK = class_7462.field_7520;
        layout.fL = class_7462.method_6096();
        int n = class_7462.method_5669();
        int n2 = class_7462.method_5748();
        layout.fM = n2 <= 0 ? 1.0f : (float)n / (float)n2;
        layout.fN = class_7462.method_5869() || n < n2;
        layout.fR = class_7462.method_6059(class_1294.field_5899);
        layout.fS = class_7462.method_6059(class_1294.field_5920);
        layout.fT = class_7462.method_6059(class_1294.field_5903);
        class_1297 class_12972 = class_7462.method_5854();
        if (class_12972 instanceof class_1309) {
            class_13092 = (class_1309)class_12972;
            layout.fO = true;
            layout.fP = class_13092.method_6032() / Math.max(1.0f, class_13092.method_6063());
        }
        class_746 player = class_3102.field_1724;
        if (player != null) {
            layout.fQ = Math.max(0.0f, Math.min(1.0f, player.method_3151()));
        }
        CustomVitalsRenderer.a(class_3322, layout);
    }

    @Unique
    private static CustomVitalsModule clarityvisuals$vitalsModule() {
        return ClarityVisuals.getInstance().getModuleManager().getModule(CustomVitalsModule.class);
    }

    @Unique
    private static boolean clarityvisuals$customVitalsActive() {
        CustomVitalsModule customVitalsModule = InGameHudMixin.clarityvisuals$vitalsModule();
        return customVitalsModule != null && customVitalsModule.isToggled();
    }

    @Unique
    private void clarityvisuals$renderHudWindowOverlay(class_332 class_3322) {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getHudManager() == null) {
            return;
        }
        clarityVisuals.getHudManager().a(class_3322);
    }

    @Unique
    private boolean isPotion(class_1799 class_17992, class_1291 class_12912) {
        if (!(class_17992.method_7909() instanceof class_1812)) {
            return false;
        }
        class_1844 class_18442 = (class_1844)class_17992.method_57824(class_9334.field_49651);
        if (class_18442 == null) {
            return false;
        }
        if (class_18442.comp_2378().isEmpty()) {
            return false;
        }
        for (class_1293 class_12932 : ((class_1842)((class_6880)class_18442.comp_2378().get()).comp_349()).method_8049()) {
            if (class_12932.method_5579().comp_349() != class_12912) continue;
            return true;
        }
        return false;
    }
}
