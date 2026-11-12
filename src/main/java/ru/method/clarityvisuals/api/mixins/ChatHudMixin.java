package ru.method.clarityvisuals.api.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_338;
import net.minecraft.class_3532;
import net.minecraft.class_5481;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.player.ChatHelperModule;
import ru.method.clarityvisuals.command.ChatOutput;
import ru.method.clarityvisuals.util.PlayerNameSanitizer;
import ru.method.clarityvisuals.modules.impl.player.PvpSaveModule;
import ru.method.clarityvisuals.util.StreamerTextSanitizer;

@Mixin(value={class_338.class})
public abstract class ChatHudMixin {
    @Shadow
    private List<class_303> field_2061;
    @Unique
    private String clarityvisuals$lastAntiSpamMessage = "";
    @Unique
    private int clarityvisuals$lastAntiSpamCount = 1;
    @Unique
    private static final float clarityvisuals$CHAT_APPEAR_TICKS = 9.0f;

    @Shadow
    protected abstract void method_44813();

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void clarityvisuals$beginChatHudRenderScope(class_332 class_3322, int n, int n2, int n3, boolean bl, CallbackInfo callbackInfo) {
        StreamerTextSanitizer.sw();
    }

    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void clarityvisuals$endChatHudRenderScope(class_332 class_3322, int n, int n2, int n3, boolean bl, CallbackInfo callbackInfo) {
        StreamerTextSanitizer.sx();
    }

    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), index=1, require=0)
    private class_5481 clarityvisuals$moveChatBadgeBeforeDonate(class_5481 class_54812) {
        return PlayerNameSanitizer.d(class_54812);
    }

    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), index=4, require=0)
    private int clarityvisuals$animateNewChatLineColor(int n, @Local(argsOnly=true, ordinal=0) int n2, @Local class_303.class_7590 class_75902) {
        if (class_75902 == null) {
            return n;
        }
        float f = this.clarityvisuals$chatLineAge(n2, class_75902);
        if (f < 0.0f || f >= 9.0f) {
            return n;
        }
        float f2 = class_3532.method_15363((float)(f / 9.0f), (float)0.0f, (float)1.0f);
        f2 = 1.0f - (1.0f - f2) * (1.0f - f2);
        int n3 = n >>> 24 & 0xFF;
        if (n3 == 0) {
            n3 = 255;
        }
        int n4 = class_3532.method_15340((int)Math.round((float)n3 * class_3532.method_16439((float)f2, (float)0.0f, (float)1.0f)), (int)0, (int)255);
        return n & 0xFFFFFF | n4 << 24;
    }

    @ModifyArg(method={"render"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"), index=2, require=0)
    private int clarityvisuals$slideNewChatLineX(int n, @Local(argsOnly=true, ordinal=0) int n2, @Local class_303.class_7590 class_75902) {
        if (class_75902 == null) {
            return n;
        }
        float f = this.clarityvisuals$chatLineAge(n2, class_75902);
        if (f < 0.0f || f >= 9.0f) {
            return n;
        }
        float f2 = class_3532.method_15363((float)(f / 9.0f), (float)0.0f, (float)1.0f);
        float f3 = 1.0f - (1.0f - f2) * (1.0f - f2) * (1.0f - f2);
        return n - Math.round((1.0f - f3) * 24.0f);
    }

    @Unique
    private float clarityvisuals$chatLineAge(int n, class_303.class_7590 class_75902) {
        float f = class_310.method_1551().method_61966().method_60637(false);
        return (float)(n - class_75902.comp_895()) + f;
    }

    @ModifyVariable(method={"addMessage"}, at=@At(value="HEAD"), argsOnly=true, require=0)
    private class_2561 clarityvisuals$badgeChatAny(class_2561 class_25612) {
        if (ChatOutput.h(class_25612)) {
            this.clarityvisuals$resetAntiSpamCounter();
            this.captureEventCoords(class_25612);
            return ChatHelperModule.c(class_25612);
        }
        class_2561 class_25613 = StreamerTextSanitizer.o(class_25612);
        class_25613 = StreamerTextSanitizer.donatePrefix(class_25613, false, true);
        StreamerTextSanitizer.c(class_25612, class_25613);
        if (StreamerTextSanitizer.p(class_25613)) {
            class_25613 = PlayerNameSanitizer.u(class_25613);
            class_25613 = PlayerNameSanitizer.v(class_25613);
        }
        class_25613 = this.clarityvisuals$applyAntiSpamCounter(class_25613);
        this.captureEventCoords(class_25613);
        return ChatHelperModule.c(class_25613);
    }

    /**
     * Фильтр спама от мода Figura: скриптовые отчеты об ошибках приходят в чат с
     * префиксом "[lua]" (например,Network API/ERR_NOT_ALLOWED при загрузке аватаров).
     * Отрезаем такие сообщения на входе в ChatHud, статус не зависит от настроек.
     */
    @Unique
    private static boolean clarityvisuals$isFiguraChatSpam(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.trim();
        if (!string2.startsWith("[lua]")) {
            return false;
        }
        return string2.contains("[ERROR]") || string2.contains("ERR_");
    }

    @Inject(method={"method_1812(Lnet/minecraft/class_2561;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$filterFiguraLua1(class_2561 class_25612, CallbackInfo callbackInfo) {
        if (clarityvisuals$isFiguraChatSpam(class_25612.getString())) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$filterFiguraLua2(class_2561 class_25612, class_7469 class_74692, class_7591 class_75912, CallbackInfo callbackInfo) {
        if (clarityvisuals$isFiguraChatSpam(class_25612.getString())) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"clear"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$keepChatHistory(boolean bl, CallbackInfo callbackInfo) {
        if (bl && ChatHelperModule.gC()) {
            callbackInfo.cancel();
        }
    }

    @Unique
    private class_2561 clarityvisuals$applyAntiSpamCounter(class_2561 class_25612) {
        if (!ChatHelperModule.gD() || class_25612 == null) {
            this.clarityvisuals$resetAntiSpamCounter();
            return class_25612;
        }
        String string = class_25612.getString();
        if (!string.equals(this.clarityvisuals$lastAntiSpamMessage)) {
            this.clarityvisuals$lastAntiSpamMessage = string;
            this.clarityvisuals$lastAntiSpamCount = 1;
            return class_25612;
        }
        ++this.clarityvisuals$lastAntiSpamCount;
        if (this.field_2061 != null && !this.field_2061.isEmpty()) {
            this.field_2061.removeFirst();
            this.method_44813();
        }
        return class_2561.method_43473().method_10852((class_2561)class_25612.method_27661()).method_10852((class_2561)class_2561.method_43470((String)(" (x" + this.clarityvisuals$lastAntiSpamCount + ")")).method_27692(class_124.field_1063));
    }

    @Unique
    private void clarityvisuals$resetAntiSpamCounter() {
        this.clarityvisuals$lastAntiSpamMessage = "";
        this.clarityvisuals$lastAntiSpamCount = 1;
    }

    private void captureEventCoords(class_2561 class_25612) {
        PvpSaveModule pvpSaveModule;
        if (class_25612 == null) {
            return;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.isPanic()) {
            return;
        }
        if (clarityVisuals.getWaypointManager() != null) {
            clarityVisuals.getWaypointManager().P(class_25612.getString());
        }
        if (clarityVisuals.getModuleManager() != null && (pvpSaveModule = clarityVisuals.getModuleManager().getModule(PvpSaveModule.class)) != null && pvpSaveModule.isToggled()) {
            pvpSaveModule.bD(class_25612.getString());
        }
    }
}

