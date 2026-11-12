package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.animation.Animation;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_308;
import net.minecraft.class_332;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import net.minecraft.class_742;
import net.minecraft.class_746;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.modules.impl.client.AllSkinsModule;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.ui.ClientTheme;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.input.KeyConstants;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.render.NotificationRenderer;
import ru.method.clarityvisuals.server.ServerDetector;
import ru.method.clarityvisuals.util.StreamerTextSanitizer;
import ru.method.clarityvisuals.ui.UiThemeController;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;
import ru.method.clarityvisuals.client.hud.VisualHud;

public class TargetHud
extends HudElement {
    private final AnimatedComponent kz = new AnimatedComponent(Easing.aiQ);
    private final AnimatedComponent kA = new AnimatedComponent(Easing.aiQ);
    private final AnimatedComponent kB = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent kC = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent kD = new AnimatedComponent(Easing.aiQ);
    private float kE = -1.0f;
    private float kF = 0.0f;
    private class_1657 kG = null;
    private class_1657 kH = null;
    private long kI = 0L;
    private final long kJ = 1000L;
    private final double kK = 3.5;
    private static final double kL = 0.75;
    private boolean kM = false;
    private float kN = 0.0f;
    private float kO = 0.0f;
    private float kP = 0.0f;
    private float kQ = 0.0f;
    private String kR = null;
    private long hD = 0L;
    private float kS = 0.0f;
    private float kT = 0.0f;
    private float kU = 0.0f;
    private float kV = 0.0f;
    private boolean kW = false;
    private String kX = null;
    private float kY = -1.0f;
    private String kZ = null;
    private long la = Long.MIN_VALUE;
    private String lb = null;
    private final List<class_1799> lc = new ArrayList<class_1799>();
    private long ld = 0L;
    private class_1657 le = null;
    private static final long lf = 100L;
    private class_1657 lg;
    private long lh;

    public TargetHud() {
        super("TargetHud");
    }

    private boolean d(class_1657 class_16572) {
        return class_16572 == null || class_16572.method_5767() || class_16572.method_6059(class_1294.field_5905);
    }

    private boolean e(class_1657 class_16572) {
        if (class_16572 == null) {
            return false;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getFriendManager() == null) {
            return false;
        }
        GameProfile gameProfile = class_16572.method_7334();
        if (gameProfile == null || gameProfile.getName() == null) {
            return false;
        }
        return clarityVisuals.getFriendManager().av(gameProfile.getName());
    }

    private static String m(float f) {
        if (f < 1000.0f) {
            return String.format("%.1f", Float.valueOf(f));
        }
        if (f < 1000000.0f) {
            return String.format("%.1fK", Float.valueOf(f / 1000.0f));
        }
        return String.format("%.1fM", Float.valueOf(f / 1000000.0f));
    }

    private class_1657 cW() {
        class_3966 class_39662;
        class_1297 class_12972;
        if (TargetHud.fullNullCheck()) {
            return null;
        }
        class_239 class_2392 = TargetHud.aiG.field_1765;
        if (class_2392 != null && class_2392.method_17783() == class_239.class_240.field_1331 && (class_12972 = (class_39662 = (class_3966)class_2392).method_17782()) instanceof class_1657 && class_12972 != TargetHud.aiG.field_1724) {
            class_1657 class_16572 = (class_1657)class_12972;
            if (this.d(class_16572)) {
                return null;
            }
            double d = TargetHud.aiG.field_1724.method_5739(class_12972);
            if (d <= 3.5) {
                return class_16572;
            }
        }
        return this.cX();
    }

    private class_1657 cX() {
        long l = System.currentTimeMillis();
        if (l - this.lh < 120L) {
            return this.lg;
        }
        this.lh = l;
        this.lg = this.cY();
        return this.lg;
    }

    private class_1657 cY() {
        if (TargetHud.aiG.field_1687 == null || TargetHud.aiG.field_1724 == null) {
            return null;
        }
        class_243 class_2432 = TargetHud.aiG.field_1724.method_33571();
        class_243 class_2433 = TargetHud.aiG.field_1724.method_5828(1.0f);
        class_243 class_2434 = class_2432.method_1019(class_2433.method_1021(3.5));
        class_1657 class_16572 = null;
        double d = Double.MAX_VALUE;
        for (class_1657 class_16573 : TargetHud.aiG.field_1687.method_18456()) {
            double d2;
            Optional optional;
            if (class_16573 == TargetHud.aiG.field_1724 || this.d(class_16573) || this.e(class_16573) || (double)TargetHud.aiG.field_1724.method_5739((class_1297)class_16573) > 3.5 || (optional = class_16573.method_5829().method_1014(0.75).method_992(class_2432, class_2434)).isEmpty() || !((d2 = ((class_243)optional.get()).method_1025(class_2432)) < d)) continue;
            d = d2;
            class_16572 = class_16573;
        }
        return class_16572 != null && this.a(class_2432, class_16572) ? class_16572 : null;
    }

    private boolean a(class_243 class_2432, class_1657 class_16572) {
        class_243 class_2433 = class_16572.method_5829().method_1005();
        class_3965 class_39652 = TargetHud.aiG.field_1687.method_17742(new class_3959(class_2432, class_2433, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)TargetHud.aiG.field_1724));
        return class_39652 == null || class_39652.method_17783() != class_239.class_240.field_1332 || class_39652.method_17784().method_1025(class_2432) >= class_2433.method_1025(class_2432) - 0.001;
    }

    private boolean cZ() {
        return aiG != null && TargetHud.aiG.field_1755 instanceof class_408;
    }

    private boolean da() {
        if (HudEditorScreen.mb() || this.cZ()) {
            class_1657 class_16572 = this.cW();
            if (class_16572 != null && this.e(class_16572)) {
                this.kH = null;
                this.kG = null;
                return false;
            }
            if (class_16572 != null && !this.d(class_16572)) {
                this.kH = class_16572;
                return true;
            }
            class_746 class_7462 = TargetHud.aiG.field_1724;
            if (class_7462 == null || this.d((class_1657)class_7462)) {
                this.kH = null;
                return false;
            }
            this.kH = class_7462;
            return true;
        }
        class_1657 class_16573 = this.cW();
        long l = System.currentTimeMillis();
        if (class_16573 != null) {
            if (this.e(class_16573)) {
                this.kH = null;
                this.kG = null;
                return false;
            }
            if (this.d(class_16573)) {
                this.kH = null;
                return false;
            }
            this.kG = class_16573;
            this.kH = class_16573;
            this.kI = l;
            return true;
        }
        if (this.kG != null) {
            if (l - this.kI < 1000L && !this.kG.method_31481() && this.kG.method_5805() && (double)TargetHud.aiG.field_1724.method_5739((class_1297)this.kG) <= 3.5 && !this.d(this.kG) && !this.e(this.kG)) {
                this.kH = this.kG;
                return true;
            }
            this.kG = null;
            return false;
        }
        return false;
    }

    private Color db() {
        ClientTheme clientTheme = UiThemeController.oX();
        return clientTheme != null ? clientTheme.pf() : ColorUtils.getBackgroundColor();
    }

    private Color k(int n) {
        if (!HudThemeRenderer.active()) {
            return ColorUtils.safeColor(18, 18, 21, n);
        }
        Color color = this.db();
        return ColorUtils.safeColor(color.getRed(), color.getGreen(), color.getBlue(), n);
    }

    private Color l(int n) {
        if (!HudThemeRenderer.active()) {
            return ColorUtils.safeColor(255, 255, 255, n);
        }
        ClientTheme clientTheme = UiThemeController.oX();
        Color color = clientTheme != null ? clientTheme.pg() : ColorUtils.getTextColor();
        return ColorUtils.safeColor(color.getRed(), color.getGreen(), color.getBlue(), n);
    }

    private Color m(int n) {
        return this.f(n, 0.16f);
    }

    private Color f(int n, float f) {
        Color color = HudThemeRenderer.active() ? this.db() : ColorUtils.safeColor(18, 18, 21, 255);
        boolean bl = (0.299f * (float)color.getRed() + 0.587f * (float)color.getGreen() + 0.114f * (float)color.getBlue()) / 255.0f > 0.6f;
        Color color2 = ColorUtils.mix(color, bl ? Color.BLACK : Color.WHITE, f);
        return ColorUtils.safeColor(color2.getRed(), color2.getGreen(), color2.getBlue(), n);
    }

    private void a(class_4587 class_45872, class_332 class_3322, class_1799 class_17992, float f, float f2, float f3, float f4, float f5, int n, boolean bl) {
        Color color = this.f(Math.round(235.0f * f5), bl ? 0.42f : 0.16f);
        Render2D.drawRoundedRect(class_45872, f, f2, f3, f3, 3.5f, color);
        Color color2 = HudThemeRenderer.active() ? ColorUtils.alpha(ColorUtils.getPrimaryColor(), Math.round(170.0f * f5)) : ColorUtils.safeColor(255, 255, 255, Math.round(55.0f * f5));
        Render2D.drawBorder(class_45872, f, f2, f3, f3, 3.5f, 1.0f, 2.0f, color2);
        float f6 = 16.0f * f4;
        float f7 = f + (f3 - f6) / 2.0f;
        float f8 = f2 + (f3 - f6) / 2.0f;
        class_45872.method_22903();
        class_45872.method_46416(f7, f8, 200.0f);
        class_45872.method_22905(f4, f4, 1.0f);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        class_3322.method_51427(class_17992, 0, 0);
        class_45872.method_22909();
        if (class_17992.method_7963()) {
            float f9 = 1.0f - (float)class_17992.method_7919() / (float)class_17992.method_7936();
            int n2 = Math.max(0, Math.min(100, Math.round(f9 * 100.0f)));
            boolean bl2 = HudThemeRenderer.isLightTheme();
            Color color3 = n2 < 25 ? (bl2 ? new Color(198, 40, 40) : new Color(255, 108, 108)) : (n2 < 50 ? (bl2 ? new Color(176, 116, 0) : new Color(255, 211, 82)) : (bl2 ? new Color(34, 138, 60) : new Color(108, 221, 122)));
            String string = n2 + "%";
            float f10 = 4.6f;
            float f11 = Fonts.MEDIUM.getWidth(string, f10);
            Render2D.drawFont(class_45872, Fonts.MEDIUM.getFont(f10), string, f + f3 / 2.0f - f11 / 2.0f, f2 + f3 + 1.5f, ColorUtils.safeColor(color3.getRed(), color3.getGreen(), color3.getBlue(), n));
        }
    }

    private Color a(class_1657 class_16572, int n) {
        int n2 = Math.max(0, Math.min(255, n));
        if (class_16572 instanceof class_1309) {
            class_1657 class_16573 = class_16572;
            if (class_16573.field_6235 > 0) {
                float f = Math.max(0.0f, Math.min(10.0f, (float)class_16573.field_6235));
                float f2 = f / 10.0f;
                float f3 = Math.max(0.0f, Math.min(1.0f, this.kC.a(f2, 300L)));
                int n3 = 255;
                float f4 = 255.0f - 105.0f * f3;
                float f5 = 255.0f - 105.0f * f3;
                int n4 = Math.max(0, Math.min(255, Math.round(f4)));
                int n5 = Math.max(0, Math.min(255, Math.round(f5)));
                return ColorUtils.safeColor(n3, n4, n5, n2);
            }
        }
        this.kC.a(0.0f, 300L);
        return ColorUtils.safeColor(255, 255, 255, n2);
    }

    private class_2960 f(class_1657 class_16572) {
        class_640 class_6402;
        if (class_16572 == null) {
            return null;
        }
        class_2960 class_29602 = AllSkinsModule.it();
        if (class_29602 != null) {
            return class_29602;
        }
        if (aiG.method_1562() != null && (class_6402 = aiG.method_1562().method_2871(class_16572.method_5667())) != null && class_6402.method_52810() != null) {
            return class_6402.method_52810().comp_1626();
        }
        if (class_16572 instanceof class_742) {
            class_742 clientPlayer = (class_742)class_16572;
            return clientPlayer.method_52814().comp_1626();
        }
        return null;
    }

    private void a(HudRenderEvent hudRenderEvent, class_1657 class_16572, float f, float f2, float f3, float f4, Color color) {
        class_2960 class_29602 = this.f(class_16572);
        if (class_29602 == null) {
            Render2D.drawRoundedRect(hudRenderEvent.j().method_51448(), f, f2, f3, f3, f4, ColorUtils.alpha(ColorUtils.getPrimaryColor(), color.getAlpha()));
            return;
        }
        class_4587 class_45872 = hudRenderEvent.j().method_51448();
        Render2D.drawTexture(class_45872, f, f2, f3, f3, f4, 0.125f, 0.125f, 0.125f, 0.125f, class_29602, color);
        Render2D.drawTexture(class_45872, f, f2, f3, f3, f4, 0.625f, 0.125f, 0.125f, 0.125f, class_29602, color);
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        if (TargetHud.fullNullCheck() || this.bL()) {
            return;
        }
        boolean bl = this.da();
        float f6 = this.kB.a(bl ? 1.0f : 0.0f, 200L);
        if (f6 <= 0.0f) {
            this.kH = null;
            this.kM = false;
            this.kW = false;
            return;
        }
        if (this.kH == null || this.d(this.kH) || this.e(this.kH)) {
            this.kH = null;
            this.kM = false;
            this.kW = false;
            return;
        }
        float f7 = this.getX();
        float f8 = this.getY();
        class_1657 class_16572 = this.kH;
        float hp = class_16572.method_6032();
        float maxHp = Math.max(class_16572.method_6063(), 1.0f);
        float absorb = Math.max(0.0f, class_16572.method_6067());
        String name = StreamerTextSanitizer.dr(class_16572.method_7334().getName());
        float nameW = Fonts.UNBOUNDED.getWidth(name, 7.5f);
        String hpStr = String.valueOf((int)Math.ceil(hp + absorb));
        float hpW = Fonts.UNBOUNDED.getWidth(hpStr, 7.0f);
        float iconW = Fonts.LEGION_PULSE.getWidth("A", 7.0f);
        float hpEst = Fonts.UNBOUNDED.getWidth("100", 7.0f) + iconW + 2.0f;
        float contentW = Math.max(69.0f, nameW * 0.75f + 8.0f + hpEst);
        float w = 30.0f + contentW + 15.0f;
        float h = 32.0f;
        float f24 = Math.max(0.0f, Math.min(1.0f, f6));
        int a = Math.max(0, Math.min(255, Math.round(255.0f * f24)));
        class_4587 class_45873 = hudRenderEvent.j().method_51448();
        float f25 = this.b(class_45873, f7, f8);
        class_45873.method_22903();
        float cx = f7 + w / 2.0f;
        float cy = f8 + h / 2.0f;
        class_45873.method_46416(cx, cy, 0.0f);
        class_45873.method_22905(0.95f * f24, 0.95f * f24, 1.0f);
        class_45873.method_46416(-cx, -cy, 0.0f);
        LegionHud.card(class_45873, f7, f8, w, h, 10.0f, a);
        Color faceTint = this.a(class_16572, a);
        this.a(hudRenderEvent, class_16572, f7 + 6.0f, f8 + h / 2.0f - 11.0f, 22.0f, 4.0f, faceTint);
        float contentX = f7 + 34.0f;
        float nameY = f8 + 7.5f;
        LegionHud.text(class_45873, Fonts.UNBOUNDED, name, contentX, nameY - 1.0f, 7.5f, LegionHud.withAlpha(LegionHud.SOFT, a));
        float rightEdge = f7 + w - 7.5f;
        LegionHud.text(class_45873, Fonts.UNBOUNDED, hpStr, rightEdge - iconW - hpW, nameY - 1.2f, 7.0f, LegionHud.withAlpha(LegionHud.ACCENT, a));
        LegionHud.text(class_45873, Fonts.LEGION_PULSE, "A", rightEdge - iconW, nameY - 0.5f, 7.0f, LegionHud.withAlpha(LegionHud.ACCENT, a));
        float healthRatio = Math.max(0.0f, Math.min(1.0f, hp / maxHp));
        float absRatio = Math.max(0.0f, Math.min(1.0f, absorb / maxHp));
        float barH = 4.0f;
        float barY = f8 + h - 10.5f;
        float barX = contentX - 1.0f;
        float barW = rightEdge - barX;
        Color track = new Color(56, 56, 56, a);
        Render2D.drawRoundedRect(class_45873, barX, barY, barW + 1.0f, barH, 3.0f, track);
        if (barW * healthRatio > 0.5f) {
            Color fillDark = new Color(115, 115, 115, a);
            Color fillLight = new Color(204, 204, 204, a);
            Render2D.drawRoundedHorizontalGradient(class_45873, barX, barY, Math.max(0.5f, barW * healthRatio), barH, 3.0f, fillDark, fillLight);
        }
        if (absRatio > 0.0f && barW * absRatio > 0.5f) {
            Color goldDark = new Color(115, 90, 23, a);
            Color goldLight = new Color(204, 159, 41, a);
            Render2D.drawRoundedHorizontalGradient(class_45873, barX, barY, Math.max(0.5f, barW * absRatio), barH, 3.0f, goldDark, goldLight);
        }
        this.kR = name;
        this.kN = contentX;
        this.kO = nameY - 1.0f;
        this.kP = Math.max(nameW, 1.0f);
        this.kQ = Fonts.UNBOUNDED.getHeight(7.5f);
        this.kM = f24 > 0.001f;
        this.kW = false;
        class_45873.method_22909();
        this.b(class_45873);
        this.b(f7, f8, w, h, f25);
        super.onRender2D(hudRenderEvent);
    }

    @Override
    protected boolean c(int n, int n2) {
        boolean bl;
        if (this.kR == null || this.kR.isBlank()) {
            return false;
        }
        boolean bl2 = bl = this.kM && MathUtils.c(this.kN, this.kO, this.kP, this.kQ, n, n2);
        if (!bl && this.kW) {
            bl = MathUtils.c(this.kS, this.kT, this.kU, this.kV, n, n2);
        }
        if (!bl) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - this.hD < 250L) {
            return true;
        }
        this.hD = l;
        TargetHud.aiG.field_1774.method_1455(this.kR);
        ClarityVisuals.getInstance().getNotifyManager().a(new NotificationRenderer(KeyConstants.akO, "\u041d\u0438\u043a \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d", 1000L));
        return true;
    }
}
