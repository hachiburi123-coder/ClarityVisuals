package ru.method.clarityvisuals.ui;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.ui.ClientTheme;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.animation.InterpolationUtils;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.render.LinkGuiBrandRenderer;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.hud.ScoreboardHud;
import ru.method.clarityvisuals.api.setting.Setting;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.ui.ThemeTransition;
import ru.method.clarityvisuals.ui.UiInteractionState;
import ru.method.clarityvisuals.render.UiRenderUtils;
import ru.method.clarityvisuals.modules.impl.client.UI;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

public final class ThemeSettingsPanel {
    private static final long aae = 480L;
    private static final float aaf = 34.0f;
    private static final float aag = 10.0f;
    private static final float aah = 30.0f;
    private static final float aai = 5.0f;
    private final UiInteractionState aaj;
    private final AnimatedComponent aak = new AnimatedComponent(Easing.aiR);
    private final Animation aal = new Animation(220L, 1.0, false, Easing.aiR);
    private final Animation aam = new Animation(220L, 1.0, false, Easing.aiR);
    private final Animation[] aar = ThemeSettingsPanel.v(180L);
    private final Animation[] aas = ThemeSettingsPanel.v(220L);
    private final Animation aat = new Animation(180L, 1.0, false, Easing.aiZ);
    private final Animation aay = new Animation(180L, 1.0, false, Easing.aiZ);
    private final Animation aaz = new Animation(180L, 1.0, false, Easing.aiZ);
    private float x;
    private float y;
    private float width;
    private boolean aaA;
    private boolean Zw;
    private boolean aaB;
    private boolean aaC;
    private Float aaD;
    private static final float aaE = 52.0f;
    private boolean aaF;
    private boolean aaG;
    private SliderSetting Zx;
    private SliderSetting aaH;
    private BooleanSetting aaL;

    public ThemeSettingsPanel(UiInteractionState uiInteractionState) {
        this.aaj = uiInteractionState;
    }

    private static Animation[] v(long l) {
        Animation[] animationArray = new Animation[ClientTheme.values().length];
        for (int i = 0; i < animationArray.length; ++i) {
            animationArray[i] = new Animation(l, 1.0, false, Easing.aiZ);
        }
        return animationArray;
    }

    public void setBounds(float f, float f2, float f3) {
        this.x = f;
        this.y = f2;
        this.width = f3;
    }

    public float getHeight() {
        return 482.0f + 52.0f * this.aam.rk();
    }

    private float pv() {
        int n = (ClientTheme.values().length + 1) / 2;
        return this.y + 34.0f + (float)n * 35.0f + 8.0f;
    }

    public void a(class_332 class_3322, int n, int n2) {
        this.c(class_3322.method_51448(), n, n2);
    }

    public void d(class_4587 class_45872, int n, int n2) {
        this.c(class_45872, n, n2);
    }

    private void c(class_4587 class_45872, int n, int n2) {
        float f = this.getHeight();
        LinkGuiBrandRenderer.g(class_45872, this.x, this.y, this.width, f, 12.0f);
        LinkGuiBrandRenderer.a(class_45872, "THEME", "U", this.x + 14.0f, this.y + 12.0f);
        ClientTheme[] clientThemeArray = ClientTheme.values();
        if (!this.aaA) {
            for (int i = 0; i < clientThemeArray.length; ++i) {
                if (this.aaj.mF() == clientThemeArray[i]) {
                    this.aas[i].ro();
                    continue;
                }
                this.aas[i].ae(false);
            }
            this.aaA = true;
        }
        float f2 = this.width - 20.0f;
        float f3 = (f2 - 5.0f) / 2.0f;
        for (int i = 0; i < clientThemeArray.length; ++i) {
            float f4;
            ClientTheme clientTheme = clientThemeArray[i];
            boolean bl = this.aaj.mF() == clientTheme;
            float f5 = this.x + 10.0f + (float)(i % 2) * (f3 + 5.0f);
            float f6 = this.y + 34.0f + (float)(i / 2) * 35.0f;
            boolean bl2 = MathUtils.c(f5, f6, f3, 30.0f, n, n2);
            this.aar[i].i(bl2 && !bl);
            this.aas[i].i(bl);
            float f7 = this.aar[i].rk();
            float f8 = this.aas[i].rk();
            UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f3, 30.0f, 8.0f, clientTheme.pf());
            UiRenderUtils.c(class_45872, f5 + 1.0f, f6 + 1.0f, f3 - 2.0f, 28.0f, 7.0f, InterpolationUtils.e(clientTheme.pe(), 0.3f), InterpolationUtils.e(clientTheme.pe(), 0.0f));
            UiRenderUtils.drawRoundedRect(class_45872, f5 + 8.0f, f6 + 15.0f - 4.0f, 8.0f, 8.0f, 4.0f, clientTheme.pe());
            if (f7 > 0.01f) {
                UiRenderUtils.drawRoundedRect(class_45872, f5 + 1.0f, f6 + 1.0f, f3 - 2.0f, 28.0f, 7.0f, new Color(255, 255, 255, Math.round(16.0f * f7)));
            }
            Color color = (f4 = (0.299f * (float)clientTheme.pf().getRed() + 0.587f * (float)clientTheme.pf().getGreen() + 0.114f * (float)clientTheme.pf().getBlue()) / 255.0f) > 0.6f ? new Color(26, 28, 34) : new Color(238, 240, 245);
            float f9 = 7.2f;
            UiRenderUtils.a(class_45872, Fonts.MEDIUM, clientTheme.getName(), f5 + 21.0f, f6 + (30.0f - Fonts.MEDIUM.getHeight(f9)) / 2.0f, new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(195.0f + 60.0f * f8)), f9);
            UiRenderUtils.c(class_45872, f5, f6, f3, 30.0f, 8.0f, 1.2f, InterpolationUtils.e(clientTheme.pe(), 0.16f + 0.74f * f8));
            if (!(f8 > 0.03f)) continue;
            Color color2 = clientTheme.pe();
            UiRenderUtils.a(class_45872, Fonts.ICONSNEW, "H", f5 + f3 - 15.0f, f6 + 15.0f - 4.0f, new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), Math.round(255.0f * f8)), 7.0f);
        }
        this.f(class_45872, n, n2);
    }

    private void f(class_4587 class_45872, int n, int n2) {
        float f;
        float f2;
        float f3 = this.pv();
        LinkGuiBrandRenderer.g(class_45872, this.x + 12.0f, f3 - 6.0f, this.width - 24.0f);
        LinkGuiBrandRenderer.a(class_45872, "INTERFACE", "T", this.x + 14.0f, f3);
        UI uI = ClarityVisuals.getInstance().getModuleManager().getModule(UI.class);
        float f4 = this.aaD != null ? this.aaD.floatValue() : this.b(uI);
        Color color = ThemeTransition.d(210);
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Interface Scale", this.x + 14.0f, f3 + 25.0f, color, 8.4f);
        String string = Math.round(f4 * 100.0f) + "%";
        UiRenderUtils.a(class_45872, Fonts.ROUNDBOLD, string, this.x + this.width - 14.0f - Fonts.ROUNDBOLD.getWidth(string, 8.4f), f3 + 25.0f, ThemeTransition.accent(), 8.4f);
        float f5 = this.x + 14.0f;
        float f6 = f3 + 42.0f;
        float f7 = this.width - 28.0f;
        UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f7, 3.0f, 1.5f, ThemeTransition.d(20));
        float f8 = MathUtils.k((f4 - 0.75f) / 0.75f, 0.0f, 1.0f);
        UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f7 * f8, 3.0f, 1.5f, ThemeTransition.accent());
        LinkGuiBrandRenderer.f(class_45872, f5 + f7 * f8, f6 + 1.5f, 8.0f);
        if (this.Zw && uI != null) {
            f2 = Math.max(0.0f, Math.min(1.0f, ((float)n - f5) / f7));
            this.aaD = Float.valueOf(0.75f + f2 * 0.75f);
        }
        f2 = 8.0f;
        float f9 = f3 + 56.0f;
        float f10 = 20.0f;
        float f11 = (this.width - 28.0f - f2) / 2.0f;
        this.a(class_45872, this.x + 14.0f, f9, f11, f10, "Apply", n, n2, true, this.aay);
        this.a(class_45872, this.x + 14.0f + f11 + f2, f9, f11, f10, "Default", n, n2, false, this.aaz);
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Cursor Tracker", this.x + 14.0f, f3 + 86.0f, color, 8.4f);
        this.aal.i(this.aaj.mH());
        LinkGuiBrandRenderer.i(class_45872, this.x + this.width - 48.0f, f3 + 83.0f, 34.0f, 16.0f, this.aal.rk());
        float f12 = this.i(uI);
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "GUI Transparency", this.x + 14.0f, f3 + 112.0f, color, 8.4f);
        String string2 = Math.round(f12) + "%";
        UiRenderUtils.a(class_45872, Fonts.ROUNDBOLD, string2, this.x + this.width - 14.0f - Fonts.ROUNDBOLD.getWidth(string2, 8.4f), f3 + 112.0f, ThemeTransition.accent(), 8.4f);
        float f13 = this.x + 14.0f;
        float f14 = f3 + 129.0f;
        float f15 = this.width - 28.0f;
        UiRenderUtils.drawRoundedRect(class_45872, f13, f14, f15, 3.0f, 1.5f, ThemeTransition.d(20));
        float f16 = MathUtils.k((f12 - 30.0f) / 70.0f, 0.0f, 1.0f);
        UiRenderUtils.drawRoundedRect(class_45872, f13, f14, f15 * f16, 3.0f, 1.5f, ThemeTransition.accent());
        LinkGuiBrandRenderer.f(class_45872, f13 + f15 * f16, f14 + 1.5f, 8.0f);
        if (this.aaB && uI != null) {
            f = MathUtils.k(((float)n - f13) / f15, 0.0f, 1.0f);
            this.b(uI, 30.0f + f * 70.0f);
        }
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Open Keybind", this.x + 14.0f, f3 + 152.0f, color, 8.4f);
        f = this.x + 14.0f;
        float f17 = f3 + 167.0f;
        float f18 = this.width - 28.0f;
        float f19 = 22.0f;
        boolean bl = MathUtils.c(f, f17, f18, f19, n, n2);
        this.aat.i(bl);
        LinkGuiBrandRenderer.h(class_45872, f, f17, f18, f19, 7.0f);
        float f20 = Math.max(0.6f * this.aat.rk(), this.aaC ? 1.0f : 0.0f);
        if (f20 > 0.01f) {
            LinkGuiBrandRenderer.h(class_45872, f + 1.0f, f17 + 1.0f, f18 - 2.0f, f19 - 2.0f, 6.0f, f20);
        }
        String string3 = this.aaC ? "..." : ThemeSettingsPanel.e(uI);
        float f21 = Fonts.MEDIUM.getWidth(string3, 8.4f);
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, string3, f + (f18 - f21) / 2.0f, f17 + 6.0f, ThemeTransition.d(235), 8.4f);
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Custom Scoreboard", this.x + 14.0f, f3 + 199.0f, color, 8.4f);
        this.aam.i(this.g(uI));
        float f24 = this.aam.rk();
        LinkGuiBrandRenderer.i(class_45872, this.x + this.width - 48.0f, f3 + 196.0f, 34.0f, 16.0f, f24);
        if (f24 > 0.03f) {
            this.b(class_45872, f3, f24, n, n2);
        }
    }

    private void b(class_4587 class_45872, float f, float f2, int n, int n2) {
        float f3;
        ScoreboardHud scoreboardHud = ThemeSettingsPanel.pw();
        if (scoreboardHud == null) {
            return;
        }
        int n3 = Math.round(210.0f * f2);
        Color color = ThemeTransition.d(n3);
        float f4 = scoreboardHud.cO();
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Opacity", this.x + 14.0f, f + 225.0f, color, 8.4f);
        String string = Math.round(f4 / 255.0f * 100.0f) + "%";
        UiRenderUtils.a(class_45872, Fonts.ROUNDBOLD, string, this.x + this.width - 14.0f - Fonts.ROUNDBOLD.getWidth(string, 8.4f), f + 225.0f, ThemeTransition.accent(), 8.4f);
        float f5 = this.x + 14.0f;
        float f6 = f + 242.0f;
        float f7 = this.width - 28.0f;
        UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f7, 3.0f, 1.5f, ThemeTransition.d(Math.round(20.0f * f2)));
        float f8 = MathUtils.k(f4 / 255.0f, 0.0f, 1.0f);
        UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f7 * f8, 3.0f, 1.5f, ColorUtils.alpha(ThemeTransition.accent(), Math.round(255.0f * f2)));
        LinkGuiBrandRenderer.f(class_45872, f5 + f7 * f8, f6 + 1.5f, 8.0f);
        if (this.aaF) {
            f3 = MathUtils.k(((float)n - f5) / f7, 0.0f, 1.0f);
            scoreboardHud.j(f3 * 255.0f);
        }
        f3 = scoreboardHud.cP();
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Corner Radius", this.x + 14.0f, f + 259.0f, color, 8.4f);
        String string2 = String.valueOf(Math.round(f3));
        UiRenderUtils.a(class_45872, Fonts.ROUNDBOLD, string2, this.x + this.width - 14.0f - Fonts.ROUNDBOLD.getWidth(string2, 8.4f), f + 259.0f, ThemeTransition.accent(), 8.4f);
        float f9 = this.x + 14.0f;
        float f10 = f + 276.0f;
        float f11 = this.width - 28.0f;
        UiRenderUtils.drawRoundedRect(class_45872, f9, f10, f11, 3.0f, 1.5f, ThemeTransition.d(Math.round(20.0f * f2)));
        float f12 = MathUtils.k(f3 / 18.0f, 0.0f, 1.0f);
        UiRenderUtils.drawRoundedRect(class_45872, f9, f10, f11 * f12, 3.0f, 1.5f, ColorUtils.alpha(ThemeTransition.accent(), Math.round(255.0f * f2)));
        LinkGuiBrandRenderer.f(class_45872, f9 + f11 * f12, f10 + 1.5f, 8.0f);
        if (this.aaG) {
            float f13 = MathUtils.k(((float)n - f9) / f11, 0.0f, 1.0f);
            scoreboardHud.k(f13 * 18.0f);
        }
    }

    private static ScoreboardHud pw() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getHudManager() == null) {
            return null;
        }
        return clarityVisuals.getHudManager().a(ScoreboardHud.class);
    }

    public boolean px() {
        return this.aaC;
    }

    public boolean r(int n, int n2) {
        if (!this.aaC) {
            return false;
        }
        UI uI = ClarityVisuals.getInstance().getModuleManager().getModule(UI.class);
        if (uI != null) {
            if (n == 256 || n == 261) {
                uI.setBind(new KeyBind(344, false));
            } else {
                uI.setBind(new KeyBind(n, false));
            }
            ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
            if (clarityVisuals != null && !clarityVisuals.isConfigLoading()) {
                clarityVisuals.getConfigManager().dU();
            }
        }
        this.aaC = false;
        return true;
    }

    private static String e(UI uI) {
        if (uI == null || uI.getBind() == null || uI.getBind().isEmpty()) {
            return "Right Shift";
        }
        return uI.getBind().toString().replace('_', ' ').replace('-', ' ').trim();
    }

    private void a(class_4587 class_45872, float f, float f2, float f3, float f4, String string, int n, int n2, boolean bl, Animation animation) {
        boolean bl2 = MathUtils.c(f, f2, f3, f4, n, n2);
        animation.i(bl2);
        float f5 = animation.rk();
        LinkGuiBrandRenderer.h(class_45872, f, f2, f3, f4, 7.0f);
        LinkGuiBrandRenderer.h(class_45872, f + 1.0f, f2 + 1.0f, f3 - 2.0f, f4 - 2.0f, 6.0f, 0.18f + 0.82f * f5);
        Color color = bl ? ThemeTransition.accent() : ThemeTransition.d(Math.round(210.0f + 35.0f * f5));
        float f6 = 7.5f;
        float f7 = UiRenderUtils.a(Fonts.ROUNDBOLD, f6);
        UiRenderUtils.b(class_45872, Fonts.ROUNDBOLD, string, f + f3 / 2.0f, f2 + (f4 - f7) / 2.0f, color, f6);
    }

    public boolean mouseClicked(double d, double d2, int n) {
        ScoreboardHud scoreboardHud;
        float f;
        float f2;
        float f3;
        if (n != 0) {
            return false;
        }
        float f4 = this.width - 20.0f;
        float f5 = (f4 - 5.0f) / 2.0f;
        ClientTheme[] clientThemeArray = ClientTheme.values();
        for (int i = 0; i < clientThemeArray.length; ++i) {
            f3 = this.x + 10.0f + (float)(i % 2) * (f5 + 5.0f);
            f2 = this.y + 34.0f + (float)(i / 2) * 35.0f;
            if (!MathUtils.c(f3, f2, f5, 30.0f, (float)d, (float)d2)) continue;
            this.aaj.a(clientThemeArray[i]);
            return true;
        }
        f3 = this.x + 14.0f;
        float f6 = this.pv();
        f2 = f6 + 42.0f;
        if (MathUtils.c(f3, f2 - 5.0f, f = this.width - 28.0f, 12.0f, (float)d, (float)d2)) {
            this.Zw = true;
            return true;
        }
        float f7 = 8.0f;
        float f8 = f6 + 56.0f;
        float f9 = 20.0f;
        float f10 = (this.width - 28.0f - f7) / 2.0f;
        UI uI = ClarityVisuals.getInstance().getModuleManager().getModule(UI.class);
        if (MathUtils.c(this.x + 14.0f, f8, f10, f9, (float)d, (float)d2)) {
            if (uI != null) {
                if (this.aaD != null) {
                    this.a(uI, this.aaD.floatValue());
                }
                uI.applyScaleSetting();
            }
            this.aaD = null;
            return true;
        }
        if (MathUtils.c(this.x + 14.0f + f10 + f7, f8, f10, f9, (float)d, (float)d2)) {
            if (uI != null) {
                uI.resetScaleSetting();
                UI.previewInterfaceScale(this.b(uI));
            }
            this.aaD = null;
            return true;
        }
        if (MathUtils.c(this.x + this.width - 48.0f, f6 + 83.0f, 34.0f, 16.0f, (float)d, (float)d2)) {
            this.aaj.y(!this.aaj.mH());
            return true;
        }
        float f11 = this.x + 14.0f;
        float f12 = f6 + 129.0f;
        float f13 = this.width - 28.0f;
        if (MathUtils.c(f11, f12 - 5.0f, f13, 12.0f, (float)d, (float)d2)) {
            this.aaB = true;
            if (uI != null) {
                float f14 = MathUtils.k(((float)d - f11) / f13, 0.0f, 1.0f);
                this.b(uI, 30.0f + f14 * 70.0f);
            }
            return true;
        }
        if (MathUtils.c(this.x + 14.0f, f6 + 167.0f, this.width - 28.0f, 22.0f, (float)d, (float)d2)) {
            this.aaC = !this.aaC;
            return true;
        }
        if (MathUtils.c(this.x + this.width - 48.0f, f6 + 196.0f, 34.0f, 16.0f, (float)d, (float)d2)) {
            if (uI != null) {
                this.a(uI, !this.g(uI));
            }
            return true;
        }
        if (this.g(uI) && (scoreboardHud = ThemeSettingsPanel.pw()) != null) {
            if (MathUtils.c(this.x + 14.0f, f6 + 242.0f - 5.0f, this.width - 28.0f, 12.0f, (float)d, (float)d2)) {
                this.aaF = true;
                scoreboardHud.j(MathUtils.k(((float)d - (this.x + 14.0f)) / (this.width - 28.0f), 0.0f, 1.0f) * 255.0f);
                return true;
            }
            if (MathUtils.c(this.x + 14.0f, f6 + 276.0f - 5.0f, this.width - 28.0f, 12.0f, (float)d, (float)d2)) {
                this.aaG = true;
                scoreboardHud.k(MathUtils.k(((float)d - (this.x + 14.0f)) / (this.width - 28.0f), 0.0f, 1.0f) * 18.0f);
                return true;
            }
        }
        return false;
    }

    private static void py() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals != null && !clarityVisuals.isConfigLoading()) {
            clarityVisuals.getConfigManager().dU();
        }
    }

    private static <T extends Setting<?>> T a(UI uI, T t, String string, Class<T> clazz) {
        if (t != null) {
            return t;
        }
        if (uI == null) {
            return null;
        }
        for (Setting<?> setting : uI.getSettings()) {
            if (!clazz.isInstance(setting) || !string.equals(setting.getName())) continue;
            return (T)setting;
        }
        return null;
    }

    private BooleanSetting f(UI uI) {
        this.aaL = ThemeSettingsPanel.a(uI, this.aaL, "settings.ui.scoreboard.custom", BooleanSetting.class);
        return this.aaL;
    }

    private boolean g(UI uI) {
        BooleanSetting booleanSetting = this.f(uI);
        return booleanSetting != null ? (Boolean)booleanSetting.jT() : false;
    }

    private void a(UI uI, boolean bl) {
        ClarityVisuals clarityVisuals;
        BooleanSetting booleanSetting = this.f(uI);
        if (booleanSetting != null) {
            booleanSetting.a(bl);
        }
        if ((clarityVisuals = ClarityVisuals.getInstance()) != null && !clarityVisuals.isConfigLoading()) {
            clarityVisuals.getConfigManager().dU();
        }
    }

    public void mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            this.Zw = false;
            this.aaB = false;
            if (this.aaF || this.aaG) {
                this.aaF = false;
                this.aaG = false;
                ThemeSettingsPanel.py();
            }
        }
    }

    private SliderSetting a(UI uI) {
        this.Zx = ThemeSettingsPanel.a(uI, this.Zx, "settings.ui.scale", SliderSetting.class);
        return this.Zx;
    }

    private float b(UI uI) {
        SliderSetting sliderSetting = this.a(uI);
        return sliderSetting != null ? ((Float)sliderSetting.jT()).floatValue() : 1.0f;
    }

    private void a(UI uI, float f) {
        SliderSetting sliderSetting = this.a(uI);
        if (sliderSetting != null) {
            sliderSetting.a(Float.valueOf(f));
        }
    }

    private SliderSetting h(UI uI) {
        this.aaH = ThemeSettingsPanel.a(uI, this.aaH, "settings.ui.transparency", SliderSetting.class);
        return this.aaH;
    }

    private float i(UI uI) {
        SliderSetting sliderSetting = this.h(uI);
        return sliderSetting != null ? ((Float)sliderSetting.jT()).floatValue() : 100.0f;
    }

    private void b(UI uI, float f) {
        SliderSetting sliderSetting = this.h(uI);
        if (sliderSetting != null) {
            sliderSetting.a(Float.valueOf(f));
        }
    }
}

