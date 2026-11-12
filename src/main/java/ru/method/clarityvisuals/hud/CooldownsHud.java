package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.animation.Animation;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1074;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.ui.CooldownEntry;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.animation.EasingMath;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.animation.TimedProgress;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Font;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;
import ru.method.clarityvisuals.client.hud.VisualHud;

public class CooldownsHud
extends HudElement {
    private final AnimatedComponent hj = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent hk = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent hl = new AnimatedComponent(Easing.aiZ);
    private boolean hm = true;
    private int hn;
    private final Map<String, TimedProgress> ho = new LinkedHashMap<String, TimedProgress>();
    private static final float hp = 12.0f;
    private static final float hq = 2.0f;
    private static final float hr = 3.0f;
    private static final float hs = 0.78489f;
    private List<CooldownEntry.CooldownCategory> ht = Collections.emptyList();
    private long cacheExpiresAtMs;

    public CooldownsHud() {
        super("CooldownsHud");
    }

    private boolean a(List<CooldownEntry.CooldownCategory> list) {
        if (HudEditorScreen.mb()) {
            return true;
        }
        return CooldownsHud.aiG.field_1724 != null && list != null && !list.isEmpty();
    }

    private void c(HudRenderEvent hudRenderEvent, class_1799 class_17992, float f, float f2) {
        if (class_17992 == null || class_17992.method_7960()) {
            return;
        }
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.enableDepthTest();
            float f3 = 0.75f;
            hudRenderEvent.j().method_51448().method_22903();
            hudRenderEvent.j().method_51448().method_46416(f, f2, 0.0f);
            hudRenderEvent.j().method_51448().method_22905(f3, f3, 1.0f);
            hudRenderEvent.j().method_51423((class_1309)CooldownsHud.aiG.field_1724, class_17992, 0, 0, 0);
            hudRenderEvent.j().method_51448().method_22909();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
        catch (Exception exception) {
        }
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        float f;
        float f2;
        float f3;
        Object object;
        List<CooldownEntry.CooldownCategory> list;
        if (CooldownsHud.fullNullCheck() || this.bL()) {
            return;
        }
        float f11 = aiG.method_61966().method_60637(false);
        boolean bl = HudEditorScreen.mb();
        long l = System.currentTimeMillis();
        if (l >= this.cacheExpiresAtMs) {
            this.ht = CooldownEntry.a(aiG, f11);
            this.cacheExpiresAtMs = l + 50L;
        }
        List<CooldownEntry.CooldownCategory> list2 = !(list = this.ht).isEmpty() ? list : (bl ? CooldownEntry.rG() : Collections.emptyList());
        boolean bl2 = this.a(list);
        float f12 = this.hl.a(bl2 ? 1.0f : 0.0f, 200L);
        if (f12 <= 0.0f) {
            this.hm = true;
            return;
        }
        float f13 = this.getX();
        float f14 = this.getY();
        float fixedTimerWidth = Fonts.UNBOUNDED.getWidth("00:00", 6.0f);
        float targetWidth = 75.0f;
        for (CooldownEntry.CooldownCategory cat : list2) {
            float nameW = Fonts.UNBOUNDED.getWidth(cat.rJ(), 6.0f);
            targetWidth = Math.max(nameW + fixedTimerWidth + 60.0f, targetWidth);
        }
        float targetHeight = 16.0f + (float)list2.size() * 12.0f + 4.0f;
        long l2 = this.hm ? 1L : 200L;
        float f7 = this.hj.a(targetWidth, l2);
        float f8 = this.hk.a(targetHeight, l2);
        this.hm = false;
        if (bl) {
            f7 = targetWidth;
            f8 = targetHeight;
        }
        int n = Math.max(0, Math.min(255, Math.round(255.0f * f12)));
        float f9 = this.b(hudRenderEvent.j().method_51448(), f13, f14);
        hudRenderEvent.j().method_51448().method_22903();
        float[] fArray = this.bK();
        float f10 = this.gn.rk() * f12 * this.bJ();
        float f20 = f13 + f7 / 2.0f + fArray[0];
        float f21 = f14 + f8 / 2.0f + fArray[1];
        hudRenderEvent.j().method_51448().method_46416(f20, f21, 0.0f);
        hudRenderEvent.j().method_51448().method_22905(f10, f10, 1.0f);
        hudRenderEvent.j().method_51448().method_46416(-f20, -f21, 0.0f);
        LegionHud.card(hudRenderEvent.j().method_51448(), f13, f14, f7, f8, 8.0f, n);
        Render2D.startMatrixScissor(hudRenderEvent.j(), f13, f14, f7, f8);
        float headerIconW = Fonts.LEGION_ICONS.getWidth("W", 8.0f);
        LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.LEGION_ICONS, "W", f13 + 6.0f, f14 + 6.0f, 8.0f, LegionHud.withAlpha(LegionHud.ACCENT, n));
        LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, "CoolDowns", f13 + 6.0f + headerIconW + 4.0f, f14 + 5.5f, 8.0f, LegionHud.withAlpha(LegionHud.SOFT, n));
        float moduleOffset = 23.5f;
        for (CooldownEntry.CooldownCategory cat : list2) {
            String timer = cat.rH();
            float timerW = Fonts.UNBOUNDED.getWidth(timer, 6.0f);
            this.d(hudRenderEvent, cat.stack(), f13 + 6.0f, f14 + moduleOffset - 4.0f, n);
            LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, cat.rJ(), f13 + 18.0f, f14 + moduleOffset - 3.5f, 7.0f, LegionHud.withAlpha(LegionHud.SOFT, n));
            LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, timer, f13 + f7 - timerW - 5.0f, f14 + moduleOffset - 2.5f, 6.0f, LegionHud.withAlpha(LegionHud.GRAY, n));
            moduleOffset += 12.0f;
        }
        Render2D.stopScissor(hudRenderEvent.j());
        hudRenderEvent.j().method_51448().method_22909();
        this.b(hudRenderEvent.j().method_51448());
        this.b(f13, f14, f7, f8, f9);
        super.onRender2D(hudRenderEvent);
    }

    private void d(HudRenderEvent hudRenderEvent, class_1799 stack, float f, float f2, int n) {
        if (stack == null || stack.method_7960()) {
            return;
        }
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.enableDepthTest();
            hudRenderEvent.j().method_51448().method_22903();
            hudRenderEvent.j().method_51448().method_46416(f, f2, 0.0f);
            hudRenderEvent.j().method_51448().method_22905(0.5f, 0.5f, 1.0f);
            hudRenderEvent.j().method_51423((class_1309)CooldownsHud.aiG.field_1724, stack, 0, 0, 0);
            hudRenderEvent.j().method_51448().method_22909();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
        catch (Exception exception) {
        }
    }

    private String a(CooldownEntry.CooldownCategory cooldownCategory) {
        return cooldownCategory.rJ();
    }

    private void a(class_332 class_3322, TimedProgress timedProgress, float f, float f2, Color color, float f3, Font font, float f4) {
        float f5;
        boolean bl = timedProgress.rg();
        float f6 = timedProgress.progress();
        if (!bl) {
            this.a(class_3322, Fonts.MEDIUM, 7.0f, timedProgress.rh(), f, f2, color);
            return;
        }
        float f7 = Math.max(0.0f, Math.min(1.0f, f6));
        int n = color.getAlpha();
        int n2 = Math.round((float)n * (1.0f - f7));
        int n3 = Math.round((float)n * f7);
        Color color2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), n2);
        Color color3 = new Color(color.getRed(), color.getGreen(), color.getBlue(), n3);
        float f8 = f2 - f3 * f7;
        float f9 = f2 + f3 * (1.0f - f7);
        String string = timedProgress.ri();
        String string2 = timedProgress.rh();
        int n4 = EasingMath.w(string, string2);
        int n5 = EasingMath.d(string, string2, n4);
        String string3 = EasingMath.a(string2, 0, n4);
        String string4 = EasingMath.a(string, n4, string.length() - n5);
        String string5 = EasingMath.a(string2, n4, string2.length() - n5);
        String string6 = EasingMath.a(string2, string2.length() - n5, string2.length());
        float f10 = font.getWidth(string3, f4);
        float f11 = font.getWidth(string5, f4);
        float f12 = font.getWidth(string4, f4);
        float f13 = f5 = f + f10;
        float f14 = f5 + (f11 - f12) / 2.0f;
        this.a(class_3322, Fonts.MEDIUM, 7.0f, string3, f, f2, color);
        this.a(class_3322, Fonts.MEDIUM, 7.0f, string6, f5 + f11, f2, color);
        this.a(class_3322, Fonts.MEDIUM, 7.0f, string4, f14, f8, color2);
        this.a(class_3322, Fonts.MEDIUM, 7.0f, string5, f13, f9, color3);
    }

    static final class CooldownItem {
        private final class_1799 stack;
        private final String hu;

        CooldownItem(class_1799 class_17992, String string) {
            this.stack = class_17992;
            this.hu = string;
        }

        @Override
        public final String toString() {
            return "CooldownItem[" + "stack=" + this.stack + ", " + "keyLabel=" + this.hu + "]";
        }

        @Override
        public final int hashCode() {
            return java.util.Objects.hash(this.stack, this.hu);
        }

        @Override
        public final boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof CooldownItem other)) return false;
            return java.util.Objects.deepEquals(this.stack, other.stack)
                && java.util.Objects.deepEquals(this.hu, other.hu);
        }

        public class_1799 stack() {
            return this.stack;
        }

        public String cl() {
            return this.hu;
        }
    }
}

