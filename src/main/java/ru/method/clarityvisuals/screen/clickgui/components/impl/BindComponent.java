package ru.method.clarityvisuals.screen.clickgui.components.impl;

import java.awt.Color;
import net.minecraft.class_1074;
import net.minecraft.class_332;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.api.setting.BindSetting;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.util.TextLayoutUtils;
import ru.method.clarityvisuals.screen.clickgui.components.Component;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.LinkGuiRenderContext;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

public class BindComponent
extends Component {
    private final BindSetting setting;
    private boolean binding;
    private final Animation hoverAnimation = new Animation(140L, 1.0, false, Easing.aiT);

    public BindComponent(BindSetting bindSetting) {
        super(bindSetting.getName());
        this.setting = bindSetting;
        this.visible = bindSetting::jS;
    }

    @Override
    public void render(class_332 class_3322, int n, int n2, float f) {
        float f2 = this.x + this.width - 44.0f;
        float f3 = this.y + 2.0f;
        float f4 = 40.0f;
        float f5 = this.height - 4.0f;
        boolean bl = MathUtils.c(f2, f3, f4, f5, n, n2);
        this.hoverAnimation.i(bl || this.binding);
        if (LinkGuiRenderContext.isActive()) {
            String string = class_1074.method_4662((String)this.setting.getName(), (Object[])new Object[0]);
            TextLayoutUtils.a(class_3322.method_51448(), string, this.x + 2.0f, this.y, this.width - 52.0f);
            String string2 = this.getBindText();
            string2 = this.fitText(string2, f4 - 8.0f);
            TextLayoutUtils.a(class_3322.method_51448(), string2, f2, this.y + 3.0f, f4, 18.0f, Math.max(this.hoverAnimation.rk(), this.binding ? 1.0f : 0.0f));
            return;
        }
        Color color = ColorUtils.getTextColor(255);
        String string = class_1074.method_4662((String)this.setting.getName(), (Object[])new Object[0]);
        float f6 = Fonts.REGULAR.getWidth(string, 7.5f);
        float f7 = Math.max(0.0f, this.width - 52.0f);
        boolean bl2 = MathUtils.c(this.x, this.y, this.width, this.height, n, n2);
        float f8 = this.getMarqueeOffset(f6, f7, bl2);
        float f9 = this.y + (this.height - Fonts.REGULAR.getHeight(7.5f)) / 2.0f;
        Render2D.startScissor(class_3322, this.x + 4.0f, this.y, f7, this.height);
        Render2D.drawFont(class_3322.method_51448(), Fonts.REGULAR.getFont(7.5f), string, this.x + 4.0f - f8, f9, color);
        if (f8 > 0.0f && f6 > f7) {
            Render2D.drawFont(class_3322.method_51448(), Fonts.REGULAR.getFont(7.5f), string, this.x + 4.0f - f8 + f6 + 12.0f, f9, color);
        }
        Render2D.stopScissor(class_3322);
        float f10 = 5.0f;
        Color color2 = bl || this.binding ? ColorUtils.getGlobalColor(235) : ColorUtils.alpha(ColorUtils.getGlassOutlineColor(), 150);
        Render2D.drawRoundedRect(class_3322.method_51448(), f2, f3, f4, f5, f10, color2);
        float f11 = 1.0f;
        float f12 = f2 + f11;
        float f13 = f3 + f11;
        float f14 = f4 - f11 * 2.0f;
        float f15 = f5 - f11 * 2.0f;
        float f16 = Math.max(0.0f, f10 - f11);
        Color color3 = ColorUtils.darken(ColorUtils.getGlassColor(255), 0.9f);
        Render2D.drawLiquidRectClickGuiChip(class_3322.method_51448(), f12, f13, f14, f15, new BorderRadius(f16), new ColorRGBA(color3), 2.5f, 18.0f, 11.0f, 220.0f, true, 0.0f, 0.19f);
        int n3 = (int)MathUtils.lerp(90.0f, 165.0f, this.hoverAnimation.rk());
        if (this.binding) {
            n3 = 185;
        }
        Render2D.drawRoundedRect(class_3322.method_51448(), f12, f13, f14, f15, f16, ColorUtils.getGlobalColor(n3));
        Color color4 = this.binding ? ColorUtils.pulse(ColorUtils.getTextColor(255), 18L) : (bl ? ColorUtils.getTextColor(255) : ColorUtils.mix(ColorUtils.getTextColor(255), ColorUtils.getGlassBase(), 0.25f));
        String string3 = this.getBindText();
        string3 = this.fitText(string3, f4 - 8.0f);
        float f17 = Fonts.MEDIUM.getWidth(string3, 7.0f);
        float f18 = Fonts.MEDIUM.getHeight(7.0f);
        float f19 = f2 + (f4 - f17) / 2.0f;
        float f20 = f3 + (f5 - f18) / 2.0f;
        Render2D.drawFont(class_3322.method_51448(), Fonts.MEDIUM.getFont(7.0f), string3, f19, f20, color4);
    }

    private String getBindText() {
        int n;
        int n2;
        if (this.binding) {
            return "...";
        }
        if (!((KeyBind)this.setting.jT()).jW() && (n2 = ((KeyBind)this.setting.jT()).c()) <= -95 && n2 >= -120 && (n = n2 + 100) >= 0) {
            ((KeyBind)this.setting.jT()).ah(n);
            ((KeyBind)this.setting.jT()).u(true);
        }
        if ((n2 = ((KeyBind)this.setting.jT()).c()) == -1) {
            return "None";
        }
        if (((KeyBind)this.setting.jT()).jW()) {
            return switch (n2) {
                case 1000 -> "Wheel+";
                case 1001 -> "Wheel-";
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                default -> "MB" + (n2 + 1);
            };
        }
        return ((KeyBind)this.setting.jT()).toString();
    }

    private String fitText(String string, float f) {
        if (Fonts.MEDIUM.getWidth(string, 7.0f) <= f) {
            return string;
        }
        String string2 = "...";
        float f2 = Fonts.MEDIUM.getWidth(string2, 7.0f);
        String string3 = string;
        while (string3.length() > 1 && Fonts.MEDIUM.getWidth(string3, 7.0f) + f2 > f) {
            string3 = string3.substring(0, string3.length() - 1);
        }
        return string3 + string2;
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        float f;
        float f2 = 40.0f;
        float f3 = this.x + this.width - 44.0f;
        float f4 = LinkGuiRenderContext.isActive() ? this.y + 3.0f : this.y + 2.0f;
        float f5 = f = LinkGuiRenderContext.isActive() ? 18.0f : this.height - 4.0f;
        if (MathUtils.c(f3, f4, f2, f, (float)d, (float)d2) && n == 0) {
            this.binding = !this.binding;
        } else if (this.binding && n != 0) {
            ((KeyBind)this.setting.jT()).ah(n);
            ((KeyBind)this.setting.jT()).u(true);
            this.binding = false;
        } else if (this.binding) {
            this.binding = false;
        }
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        int n;
        if (!this.binding || d4 == 0.0) {
            return false;
        }
        int n2 = n = d4 > 0.0 ? 1000 : 1001;
        ((KeyBind)this.setting.jT()).ah(n);
        ((KeyBind)this.setting.jT()).u(true);
        this.binding = false;
        return true;
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (this.binding) {
            if (n == 256 || n == 261) {
                ((KeyBind)this.setting.jT()).ah(-1);
                ((KeyBind)this.setting.jT()).u(false);
            } else {
                ((KeyBind)this.setting.jT()).ah(n);
                ((KeyBind)this.setting.jT()).u(false);
            }
            this.binding = false;
        }
    }

    @Override
    public void keyReleased(int n, int n2, int n3) {
    }

    @Override
    public void charTyped(char c, int n) {
    }

    @Override
    public void onClose() {
        this.binding = false;
    }

    @Override
    public boolean isBinding() {
        return this.binding;
    }
}

