package ru.method.clarityvisuals.ui;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.animation.InterpolationUtils;
import ru.method.clarityvisuals.render.LinkGuiBrandRenderer;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.api.setting.Setting;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.ui.ThemeTransition;
import ru.method.clarityvisuals.ui.UiInteractionState;
import ru.method.clarityvisuals.render.UiRenderUtils;
import ru.method.clarityvisuals.modules.impl.client.UI;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

public final class InterfaceSettingsPanel {
    private final UiInteractionState Zu;
    private final Animation Zv = new Animation(220L, 1.0, false, Easing.aiR);
    private float x;
    private float y;
    private float width;
    private boolean Zw;
    private SliderSetting Zx;

    public InterfaceSettingsPanel(UiInteractionState uiInteractionState) {
        this.Zu = uiInteractionState;
    }

    public void setBounds(float f, float f2, float f3) {
        this.x = f;
        this.y = f2;
        this.width = f3;
    }

    public float getHeight() {
        return 142.0f;
    }

    public void a(class_332 class_3322, int n, int n2) {
        this.c(class_3322.method_51448(), n, n2);
    }

    public void d(class_4587 class_45872, int n, int n2) {
        this.c(class_45872, n, n2);
    }

    private void c(class_4587 class_45872, int n, int n2) {
        float f;
        float f2;
        float f3 = this.getHeight();
        LinkGuiBrandRenderer.g(class_45872, this.x, this.y, this.width, f3, 12.0f);
        LinkGuiBrandRenderer.a(class_45872, "INTERFACE", "T", this.x + 14.0f, this.y + 12.0f);
        UI uI = ClarityVisuals.getInstance().getModuleManager().getModule(UI.class);
        float f4 = this.b(uI);
        Color color = ColorUtils.getTextColor(210);
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Interface Scale", this.x + 14.0f, this.y + 38.0f, color, 7.5f);
        String string = Math.round(f4 * 100.0f) + "%";
        UiRenderUtils.a(class_45872, Fonts.ROUNDBOLD, string, this.x + this.width - 14.0f - Fonts.ROUNDBOLD.getWidth(string, 7.5f), this.y + 38.0f, ThemeTransition.accent(), 7.5f);
        float f5 = this.x + 14.0f;
        float f6 = this.y + 54.0f;
        float f7 = this.width - 28.0f;
        UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f7, 3.0f, 1.5f, InterpolationUtils.aw(0.08f));
        float f8 = (f4 - 0.75f) / 0.75f;
        UiRenderUtils.drawRoundedRect(class_45872, f5, f6, f7 * f8, 3.0f, 1.5f, ThemeTransition.accent());
        UiRenderUtils.drawRoundedRect(class_45872, f5 + f7 * f8 - 4.0f, f6 - 3.0f, 8.0f, 8.0f, 4.0f, Color.WHITE);
        if (this.Zw && uI != null) {
            f2 = Math.max(0.0f, Math.min(1.0f, ((float)n - f5) / f7));
            f = 0.75f + f2 * 0.75f;
            this.a(uI, f);
            UI.previewInterfaceScale(f);
        }
        UiRenderUtils.a(class_45872, Fonts.MEDIUM, "Cursor Tracker", this.x + 14.0f, this.y + 78.0f, color, 7.5f);
        this.Zv.i(this.Zu.mH());
        LinkGuiBrandRenderer.i(class_45872, this.x + this.width - 48.0f, this.y + 74.0f, 34.0f, 16.0f, this.Zv.rk());
        float f11 = 8.0f;
        float f12 = this.y + 104.0f;
        float f13 = 22.0f;
        float f14 = (this.width - 28.0f - f11) / 2.0f;
        this.a(class_45872, this.x + 14.0f, f12, f14, f13, "Apply", n, n2, true);
        this.a(class_45872, this.x + 14.0f + f14 + f11, f12, f14, f13, "Default", n, n2, false);
    }

    private void a(class_4587 class_45872, float f, float f2, float f3, float f4, String string, int n, int n2, boolean bl) {
        boolean bl2 = MathUtils.c(f, f2, f3, f4, n, n2);
        LinkGuiBrandRenderer.h(class_45872, f, f2, f3, f4, 7.0f);
        LinkGuiBrandRenderer.h(class_45872, f + 1.0f, f2 + 1.0f, f3 - 2.0f, f4 - 2.0f, 6.0f, bl2 ? 1.0f : 0.18f);
        Color color = bl ? ThemeTransition.accent() : ColorUtils.getTextColor(bl2 ? 245 : 210);
        float f5 = 7.5f;
        float f6 = UiRenderUtils.a(Fonts.ROUNDBOLD, f5);
        UiRenderUtils.b(class_45872, Fonts.ROUNDBOLD, string, f + f3 / 2.0f, f2 + (f4 - f6) / 2.0f, color, f5);
    }

    public boolean mouseClicked(double d, double d2, int n) {
        if (n != 0) {
            return false;
        }
        float f = this.x + 14.0f;
        float f2 = this.y + 54.0f;
        float f3 = this.width - 28.0f;
        if (MathUtils.c(f, f2 - 5.0f, f3, 12.0f, (float)d, (float)d2)) {
            this.Zw = true;
            return true;
        }
        if (MathUtils.c(this.x + this.width - 48.0f, this.y + 74.0f, 34.0f, 16.0f, (float)d, (float)d2)) {
            this.Zu.y(!this.Zu.mH());
            return true;
        }
        float f6 = 8.0f;
        float f7 = this.y + 104.0f;
        float f8 = 22.0f;
        float f9 = (this.width - 28.0f - f6) / 2.0f;
        UI uI = ClarityVisuals.getInstance().getModuleManager().getModule(UI.class);
        if (MathUtils.c(this.x + 14.0f, f7, f9, f8, (float)d, (float)d2)) {
            if (uI != null) {
                uI.applyScaleSetting();
            }
            return true;
        }
        if (MathUtils.c(this.x + 14.0f + f9 + f6, f7, f9, f8, (float)d, (float)d2)) {
            if (uI != null) {
                uI.resetScaleSetting();
                UI.previewInterfaceScale(this.b(uI));
            }
            return true;
        }
        return false;
    }

    public void mouseReleased(double d, double d2, int n) {
        UI uI;
        if (this.Zw && (uI = ClarityVisuals.getInstance().getModuleManager().getModule(UI.class)) != null) {
            uI.applyScaleSetting();
        }
        this.Zw = false;
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

    private SliderSetting a(UI uI) {
        this.Zx = InterfaceSettingsPanel.a(uI, this.Zx, "settings.ui.scale", SliderSetting.class);
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
}

