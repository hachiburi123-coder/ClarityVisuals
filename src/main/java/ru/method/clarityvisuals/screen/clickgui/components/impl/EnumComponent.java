package ru.method.clarityvisuals.screen.clickgui.components.impl;

import ru.method.clarityvisuals.api.setting.Setting;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1074;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.render.LinkGuiBrandRenderer;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.util.TextLayoutUtils;
import ru.method.clarityvisuals.animation.TimedProgress;
import ru.method.clarityvisuals.screen.clickgui.components.Component;
import ru.method.clarityvisuals.ui.ThemeTransition;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.LinkGuiRenderContext;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;
import ru.method.clarityvisuals.utils.render.fonts.Instance;

public class EnumComponent
extends Component {
    private static final float LOGICAL_WIDTH = 1920.0f;
    private static final float LOGICAL_HEIGHT = 1080.0f;
    private static final float POPUP_MARGIN = 8.0f;
    private static final int DROPDOWN_TRIGGER_COUNT = 9;
    private static final int POPUP_MAX_ROWS = 9;
    private static final float POPUP_ROW_HEIGHT = 14.0f;
    private static final float POPUP_FONT_SIZE = 7.4f;
    private final EnumSetting<?> setting;
    private final Animation openAnimation = new Animation(180L, 1.0, false, Easing.aiT);
    private final Animation popupAnimation = new Animation(160L, 1.0, false, Easing.aiT);
    private final Map<Enum<?>, Animation> pickAnimations = new HashMap();
    private boolean open;
    private final Animation hoverAnimation = new Animation(160L, 1.0, false, Easing.aiT);
    private final TimedProgress valueAnim;
    private boolean editing;
    private boolean closing;
    private float chipX;
    private float chipY;
    private float chipWidth;
    private float chipHeight;
    private float popupX;
    private float popupY;
    private float popupWidth;
    private float popupHeight;
    private float contentX;
    private float contentY;
    private float viewportHeight;
    private float columnWidth;
    private float columnGap;
    private int columns = 1;
    private int rowsPerColumn = 1;
    private float scroll;
    private float scrollMax;

    public EnumComponent(EnumSetting<?> enumSetting) {
        super(enumSetting.getName());
        this.setting = enumSetting;
        this.valueAnim = new TimedProgress(220L, Easing.aiZ, this.resolveDisplay((Enum)enumSetting.jT(), Color.WHITE).text());
        for (Enum enum_ : (Enum[])((Enum)enumSetting.jT()).getClass().getEnumConstants()) {
            Animation animation = new Animation(180L, 1.0, false, Easing.aiT);
            animation.ae(enum_ == enumSetting.jT());
            this.pickAnimations.put(enum_, animation);
        }
        this.openAnimation.ae(false);
        this.addHeight = () -> Float.valueOf(this.openAnimation.rk() > 0.0f ? (float)((Enum[])((Enum)enumSetting.jT()).getClass().getEnumConstants()).length * 14.0f * this.openAnimation.rk() : 0.0f);
        this.visible = enumSetting::jS;
    }

    @Override
    public void render(class_332 class_3322, int n, int n2, float f) {
        this.openAnimation.i(this.open);
        if (LinkGuiRenderContext.isActive()) {
            this.renderLink(class_3322, n, n2);
            return;
        }
        Color color = ColorUtils.getTextColor(255);
        String string = class_1074.method_4662((String)this.setting.getName(), (Object[])new Object[0]) + ": ";
        DisplayText displayText = this.resolveDisplay((Enum)this.setting.jT(), color);
        float f2 = Fonts.REGULAR.getWidth(string, 7.5f);
        float f3 = Fonts.REGULAR.getWidth(displayText.text(), 7.5f);
        float f4 = Math.max(0.0f, this.width - 10.0f - f3);
        boolean bl = MathUtils.c(this.x, this.y, this.width, this.height, n, n2);
        float f5 = this.getMarqueeOffset(f2, f4, bl);
        float f6 = this.x + 4.0f;
        Render2D.startScissor(class_3322, f6, this.y, f4, this.height);
        Render2D.drawFont(class_3322.method_51448(), Fonts.REGULAR.getFont(7.5f), string, f6 - f5, this.y + 3.0f, color);
        if (f5 > 0.0f && f2 > f4) {
            Render2D.drawFont(class_3322.method_51448(), Fonts.REGULAR.getFont(7.5f), string, f6 - f5 + f2 + 12.0f, this.y + 3.0f, color);
        }
        Render2D.stopScissor(class_3322);
        float f7 = f6 + Math.min(f2, f4) + 1.5f;
        Render2D.drawFont(class_3322.method_51448(), Fonts.REGULAR.getFont(7.5f), displayText.text(), f7, this.y + 3.0f, displayText.color());
        if (this.openAnimation.rk() > 0.0f) {
            float f8 = (float)((Enum[])((Enum)this.setting.jT()).getClass().getEnumConstants()).length * 14.0f * this.openAnimation.rk();
            Render2D.startScissor(class_3322, this.x, this.y + this.height, this.width, f8);
            float f9 = this.height;
            for (Enum enum_ : (Enum[])((Enum)this.setting.jT()).getClass().getEnumConstants()) {
                Animation animation = this.pickAnimations.get(enum_);
                animation.i(enum_ == this.setting.jT());
                Color color2 = ColorUtils.getTextColor((int)(255.0f * animation.rk()));
                Render2D.drawFont(class_3322.method_51448(), Fonts.ICONSNEW.getFont(10.0f), "K", this.x + this.width - 14.0f, this.y + f9 + 2.0f, color2);
                DisplayText displayText2 = this.resolveDisplay(enum_, color);
                Render2D.drawFont(class_3322.method_51448(), Fonts.REGULAR.getFont(7.5f), displayText2.text(), this.x + 6.0f, this.y + f9 + 2.0f, displayText2.color());
                f9 += 14.0f;
            }
            Render2D.stopScissor(class_3322);
        }
    }

    private void renderLink(class_332 class_3322, int n, int n2) {
        Color color = ColorUtils.getTextColor(255);
        DisplayText displayText = this.resolveDisplay((Enum)this.setting.jT(), color);
        this.valueAnim.cF(displayText.text());
        this.computeChipBounds();
        float f = 7.6f;
        boolean bl = MathUtils.c(this.chipX, this.chipY, this.chipWidth, this.chipHeight, n, n2);
        this.hoverAnimation.i(bl && !this.editing);
        TextLayoutUtils.a(class_3322.method_51448(), class_1074.method_4662((String)this.setting.getName(), (Object[])new Object[0]), this.x + 2.0f, this.y, Math.max(10.0f, this.width - this.chipWidth - 8.0f));
        LinkGuiBrandRenderer.h(class_3322.method_51448(), this.chipX, this.chipY, this.chipWidth, this.chipHeight, 5.0f, Math.max(this.hoverAnimation.rk(), this.editing ? 0.5f : 0.0f));
        Render2D.startScissor(class_3322, this.chipX + 1.0f, this.chipY, this.chipWidth - 2.0f, this.chipHeight);
        this.drawChipValue(class_3322, this.chipX + this.chipWidth / 2.0f, this.chipY + this.chipHeight / 2.0f, f, displayText.color());
        Render2D.stopScissor(class_3322);
    }

    private void computeChipBounds() {
        float f = 7.6f;
        String string = this.resolveDisplay((Enum)this.setting.jT(), Color.WHITE).text();
        float f2 = Math.max(Fonts.MEDIUM.getWidth(this.valueAnim.rh(), f), Fonts.MEDIUM.getWidth(string, f));
        this.chipWidth = MathUtils.k(f2 + 14.0f, 38.0f, this.width * 0.62f);
        this.chipX = this.x + this.width - this.chipWidth - 2.0f;
        this.chipY = this.y + 3.0f;
        this.chipHeight = 18.0f;
    }

    private void drawChipValue(class_332 class_3322, float f, float f2, float f3, Color color) {
        float f4 = Fonts.MEDIUM.getHeight(f3);
        float f5 = f2 - f4 / 2.0f;
        if (!this.valueAnim.rg()) {
            String string = this.valueAnim.rh();
            float f6 = Fonts.MEDIUM.getWidth(string, f3);
            Render2D.drawFont(class_3322.method_51448(), Fonts.MEDIUM.getFont(f3), string, f - f6 / 2.0f, f5, color);
            return;
        }
        float f7 = Math.max(0.0f, Math.min(1.0f, this.valueAnim.progress()));
        float f8 = f4 * 1.1f;
        String string = this.valueAnim.ri();
        String string2 = this.valueAnim.rh();
        float f9 = Fonts.MEDIUM.getWidth(string, f3);
        float f10 = Fonts.MEDIUM.getWidth(string2, f3);
        Color color2 = EnumComponent.withAlpha(color, Math.round((float)color.getAlpha() * (1.0f - f7)));
        Color color3 = EnumComponent.withAlpha(color, Math.round((float)color.getAlpha() * f7));
        Render2D.drawFont(class_3322.method_51448(), Fonts.MEDIUM.getFont(f3), string, f - f9 / 2.0f, f5 - f8 * f7, color2);
        Render2D.drawFont(class_3322.method_51448(), Fonts.MEDIUM.getFont(f3), string2, f - f10 / 2.0f, f5 + f8 * (1.0f - f7), color3);
    }

    private static Color withAlpha(Color color, int n) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, n)));
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (LinkGuiRenderContext.isActive()) {
            this.mouseClickedLink(d, d2, n);
            return;
        }
        if (MathUtils.c(this.x, this.y, this.width, this.height, (float)d, (float)d2)) {
            if (n == 0) {
                this.setting.jY();
            } else if (n == 1) {
                boolean bl = this.open = !this.open;
            }
        }
        if (this.open && n == 0) {
            float f = this.height;
            if (MathUtils.c(this.x, this.y + this.height, this.width, ((Float)this.addHeight.get()).floatValue(), (float)d, (float)d2)) {
                for (Enum enum_ : (Enum[])((Enum)this.setting.jT()).getClass().getEnumConstants()) {
                    if (MathUtils.c(this.x, this.y + f, this.width, 14.0f, (float)d, (float)d2)) {
                        this.setting.bS(((NamedOption)((Object)enum_)).getName());
                        break;
                    }
                    f += 14.0f;
                }
            }
        }
    }

    private void mouseClickedLink(double d, double d2, int n) {
        this.computeChipBounds();
        boolean bl = MathUtils.c(this.x, this.y, this.width, this.height, (float)d, (float)d2);
        boolean bl2 = MathUtils.c(this.chipX, this.chipY, this.chipWidth, this.chipHeight, (float)d, (float)d2);
        if (this.editing) {
            if (n == 0 && bl && this.pickPopupValue((float)d, (float)d2)) {
                this.requestClose();
                return;
            }
            this.requestClose();
            return;
        }
        if (this.closing) {
            return;
        }
        if (n != 0 || !bl) {
            return;
        }
        if (bl2 || this.dropdownCandidate()) {
            this.editing = true;
            this.closing = false;
            this.popupAnimation.ae(true);
            this.selectCurrentRow(true);
            this.updatePopupGeometry();
            return;
        }
        this.setting.jY();
    }

    private boolean dropdownCandidate() {
        Enum<?>[] EArray = (Enum<?>[])((Enum<?>)this.setting.jT()).getDeclaringClass().getEnumConstants();
        return EArray != null && EArray.length >= DROPDOWN_TRIGGER_COUNT;
    }

    private void requestClose() {
        if (!this.closing) {
            this.editing = false;
            this.closing = true;
            this.popupAnimation.ae(false);
        }
    }

    private boolean pickPopupValue(float f, float f2) {
        Enum<?>[] EArray = (Enum<?>[])((Enum<?>)this.setting.jT()).getDeclaringClass().getEnumConstants();
        if (EArray == null || EArray.length == 0) {
            return false;
        }
        if (f < this.contentX || f2 < this.contentY || f2 > this.contentY + this.viewportHeight) {
            return false;
        }
        int n = (int)((f - this.contentX) / (this.columnWidth + this.columnGap));
        if (n < 0 || n >= this.columns) {
            return false;
        }
        int n2 = (int)((f2 - this.contentY + this.scroll) / POPUP_ROW_HEIGHT);
        if (n2 < 0 || n2 >= this.rowsPerColumn) {
            return false;
        }
        int n3 = n * this.rowsPerColumn + n2;
        if (n3 >= EArray.length) {
            return false;
        }
        Enum<?> enum_ = EArray[n3];
        this.setting.bS(enum_ instanceof NamedOption ? ((NamedOption)((Object)enum_)).getName() : String.valueOf(enum_));
        return true;
    }

    private int indexOfCurrent(Enum<?>[] EArray) {
        Enum<?> enum_ = (Enum<?>)this.setting.jT();
        for (int i = 0; i < EArray.length; ++i) {
            if (EArray[i] != enum_) continue;
            return i;
        }
        return 0;
    }

    private void selectCurrentRow(boolean openOnly) {
        Enum<?>[] EArray = (Enum<?>[])((Enum<?>)this.setting.jT()).getDeclaringClass().getEnumConstants();
        if (EArray == null || EArray.length == 0) {
            return;
        }
        this.updatePopupGeometry();
        int n = this.indexOfCurrent(EArray);
        int n2 = n / this.rowsPerColumn;
        int n3 = n % this.rowsPerColumn;
        float f = (float)n3 * POPUP_ROW_HEIGHT;
        this.scroll = Math.max(0.0f, Math.min(this.scrollMax, Math.max(0.0f, f - (this.viewportHeight - POPUP_ROW_HEIGHT) / 2.0f)));
    }

    private void updatePopupGeometry() {
        Enum<?>[] EArray = (Enum<?>[])((Enum<?>)this.setting.jT()).getDeclaringClass().getEnumConstants();
        if (EArray == null || EArray.length == 0) {
            return;
        }
        this.computeChipBounds();
        int n = EArray.length;
        this.columns = n > 26 ? 3 : (n > 12 ? 2 : 1);
        Instance instance = Fonts.MEDIUM.getFont(7.4f);
        float f = 26.0f;
        for (Enum<?> enum_ : EArray) {
            String string = this.display(enum_);
            f = Math.max(f, instance.getWidth(string));
        }
        this.columnWidth = Math.max(58.0f, Math.min(158.0f, f + 14.0f));
        this.columnGap = this.columns > 1 ? 7.0f : 0.0f;
        this.popupWidth = (float)this.columns * this.columnWidth + (float)(this.columns - 1) * this.columnGap;
        this.rowsPerColumn = (n + this.columns - 1) / this.columns;
        float f2 = (float)this.rowsPerColumn * POPUP_ROW_HEIGHT;
        int n2 = Math.min(this.rowsPerColumn, 9);
        this.viewportHeight = (float)n2 * POPUP_ROW_HEIGHT;
        this.scrollMax = Math.max(0.0f, f2 - this.viewportHeight);
        this.scroll = Math.max(0.0f, Math.min(this.scrollMax, this.scroll));
        this.popupHeight = this.viewportHeight + 12.0f;
        float f3 = this.chipX + this.chipWidth - this.popupWidth;
        if (f3 < POPUP_MARGIN) {
            f3 = POPUP_MARGIN;
        }
        if (f3 + this.popupWidth > LOGICAL_WIDTH - POPUP_MARGIN) {
            f3 = LOGICAL_WIDTH - POPUP_MARGIN - this.popupWidth;
        }
        this.popupX = f3;
        this.popupY = this.chipY + this.chipHeight + 6.0f;
        if (this.popupY + this.popupHeight > LOGICAL_HEIGHT - POPUP_MARGIN) {
            float f4 = this.chipY - 6.0f - this.popupHeight;
            this.popupY = f4 >= POPUP_MARGIN ? f4 : Math.max(POPUP_MARGIN, LOGICAL_HEIGHT - POPUP_MARGIN - this.popupHeight);
        }
        this.contentX = this.popupX + 5.0f;
        this.contentY = this.popupY + 6.0f;
    }

    private String display(Enum<?> enum_) {
        return enum_ instanceof NamedOption ? class_1074.method_4662(((NamedOption)((Object)enum_)).getName(), (Object[])new Object[0]) : String.valueOf(enum_);
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (!LinkGuiRenderContext.isActive() || !this.editing || this.closing) {
            return false;
        }
        this.updatePopupGeometry();
        if (!MathUtils.c(this.popupX, this.popupY, this.popupWidth, this.popupHeight, (float)d, (float)d2)) {
            return false;
        }
        if (this.scrollMax > 0.0f) {
            this.scroll = Math.max(0.0f, Math.min(this.scrollMax, this.scroll - (float)(d4 * POPUP_ROW_HEIGHT * 2.0)));
        }
        return true;
    }

    @Override
    public void renderOverlay(class_332 class_3322, int n, int n2, float f) {
        if (!LinkGuiRenderContext.isActive()) {
            return;
        }
        if (!this.editing) {
            if (!this.closing || this.popupAnimation.rk() <= 0.01f) {
                return;
            }
        } else {
            this.popupAnimation.ae(true);
        }
        this.popupAnimation.i(!this.closing);
        float f2 = this.popupAnimation.rk();
        if (this.closing && f2 <= 0.01f) {
            this.closing = false;
            return;
        }
        this.updatePopupGeometry();
        float f3 = LinkGuiRenderContext.getFadeMultiplier();
        int n3 = Math.max(0, Math.min(255, Math.round((float)255 * f2 * f3)));
        if (n3 <= 2) {
            return;
        }
        Color accentColor = ThemeTransition.accent();
        float f4 = this.popupX + this.popupWidth / 2.0f;
        float f5 = this.popupY;
        class_4587 class_45872 = class_3322.method_51448();
        class_45872.method_22903();
        class_45872.method_46416(f4, f5, 0.0f);
        float f6 = 1.0f - (1.0f - f2) * 0.5f;
        class_45872.method_22905(f6, f6, 1.0f);
        class_45872.method_46416(-f4, -f5, 0.0f);
        Color color = LinkGuiBrandRenderer.mA();
        Render2D.drawRoundedRect(class_45872, this.popupX, this.popupY, this.popupWidth, this.popupHeight, 10.0f, new Color(color.getRed(), color.getGreen(), color.getBlue(), n3));
        Render2D.drawBorder(class_45872, this.popupX, this.popupY, this.popupWidth, this.popupHeight, 10.0f, 0.8f, new ColorRGBA(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), Math.round((float)n3 * 0.55f))));
        Instance instance = Fonts.MEDIUM.getFont(7.4f);
        Instance instance2 = Fonts.ICONSNEW.getFont(8.0f);
        Enum<?>[] EArray = (Enum<?>[])((Enum<?>)this.setting.jT()).getDeclaringClass().getEnumConstants();
        int n4 = EArray.length;
        Object value = this.setting.jT();
        float f8 = this.contentY;
        Render2D.startScissor(class_3322, this.contentX - 2.0f, this.popupY + 2.0f, this.popupWidth - 4.0f, this.viewportHeight + 10.0f);
        for (int i = 0; i < this.columns; ++i) {
            for (int j = 0; j < this.rowsPerColumn; ++j) {
                float f9;
                int n5 = i * this.rowsPerColumn + j;
                if (n5 >= n4 || (f9 = f8 + (float)j * POPUP_ROW_HEIGHT - this.scroll) + POPUP_ROW_HEIGHT <= f8 - 2.0f || f9 >= f8 + this.viewportHeight + 2.0f) continue;
                Enum<?> enum_ = EArray[n5];
                boolean bl2 = enum_ == value;
                boolean bl3 = MathUtils.c(this.contentX + (float)i * (this.columnWidth + this.columnGap), f9, this.columnWidth, POPUP_ROW_HEIGHT, n, n2);
                if (bl2 || bl3) {
                    int n6 = Math.max(0, Math.min(255, Math.round((float)(bl2 ? 60 : 34) * f2 * f3)));
                    Render2D.drawRoundedRect(class_45872, this.contentX + (float)i * (this.columnWidth + this.columnGap) + 1.0f, f9, this.columnWidth - 3.0f, POPUP_ROW_HEIGHT - 1.0f, 5.0f, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), n6));
                }
                String string = this.display(enum_);
                Color color2 = bl2 ? new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), Math.min(255, Math.round((float)255 * f2 * f3) + 20)) : ColorUtils.getTextColor(Math.max(0, Math.min(255, Math.round((float)212 * f2 * f3))));
                Render2D.drawFont(class_45872, instance, string, this.contentX + (float)i * (this.columnWidth + this.columnGap) + 6.0f, f9 + POPUP_ROW_HEIGHT / 2.0f - instance.getHeight() / 2.0f, color2);
                if (!bl2) continue;
                Render2D.drawFont(class_45872, instance2, "K", this.contentX + (float)i * (this.columnWidth + this.columnGap) + this.columnWidth - 14.0f, f9 + POPUP_ROW_HEIGHT / 2.0f - instance2.getHeight() / 2.0f, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), Math.min(255, Math.round((float)240 * f2 * f3))));
            }
        }
        Render2D.stopScissor(class_3322);
        if (this.scrollMax > 0.0f) {
            float f10 = this.popupY + 6.0f;
            float f11 = this.popupHeight - 12.0f;
            float f12 = Math.max(10.0f, f11 * (this.viewportHeight / (this.scrollMax + (this.viewportHeight))));
            float f13 = this.scroll / this.scrollMax * (f11 - f12);
            int n7 = Math.max(0, Math.min(255, Math.round((float)66 * f2 * f3)));
            Render2D.drawRoundedRect(class_45872, this.popupX + this.popupWidth - 4.5f, f10, 2.0f, f11, 1.0f, new Color(255, 255, 255, n7 / 4));
            int n8 = Math.max(0, Math.min(255, Math.round((float)150 * f2 * f3)));
            Render2D.drawRoundedRect(class_45872, this.popupX + this.popupWidth - 4.5f, f10 + f13, 2.0f, f12, 1.0f, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), n8));
        }
        class_45872.method_22909();
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
    }

    @Override
    public void keyReleased(int n, int n2, int n3) {
    }

    @Override
    public void charTyped(char c, int n) {
    }

    @Override
    public boolean hasActiveOverlay() {
        return (this.editing || this.closing) && LinkGuiRenderContext.isActive();
    }

    @Override
    public boolean isExtendedHovered(double d, double d2) {
        return LinkGuiRenderContext.isActive() && this.editing;
    }

    @Override
    public void onClose() {
        this.editing = false;
        this.closing = false;
    }

    private DisplayText resolveDisplay(Enum<?> enum_, Color color) {
        return new DisplayText(class_1074.method_4662((String)((NamedOption)((Object)enum_)).getName(), (Object[])new Object[0]), color);
    }

    record DisplayText(String text, Color color) {
    }
}
