package ru.method.clarityvisuals.ui;

import java.awt.Color;
import net.minecraft.class_1074;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;
import ru.method.clarityvisuals.animation.Animation;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.api.setting.StringSetting;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

/**
 * Текстовое поле для HudSettingsPanel. Порт StringComponent из clickgui:
 * фокус по клику, ввод, backspace, Ctrl+A/C/V, Enter/Esc — коммит.
 */
public class HudStringSettingComponent extends HudSettingComponent {
    private static final float LABEL_FONT_SIZE = 7.5f;
    private static final float FIELD_HEIGHT = 14.0f;
    private static final float FIELD_RADIUS = 2.5f;
    private static final float PREFERRED_HEIGHT = 31.5f;
    private final StringSetting setting;
    private boolean focused;
    private boolean selected;
    private final Animation animation = new Animation(180L, 1.0, false, Easing.aiR);
    private float scrollOffset;
    private String buffer = "";

    public HudStringSettingComponent(String string, StringSetting stringSetting) {
        super(string);
        this.setting = stringSetting;
        this.visible = stringSetting::jS;
    }

    @Override
    public void render(class_332 class_3322, int n, int n2, float f) {
        this.height = PREFERRED_HEIGHT;
        float labelX = this.x + 5.0f;
        float labelY = this.y + 2.0f;
        float fieldX = this.x + 5.0f;
        float fieldY = this.y + 2.0f + LABEL_FONT_SIZE + 4.0f;
        float fieldW = Math.max(1.0f, this.width - 10.0f);
        float textW = Math.max(1.0f, fieldW - 8.0f);
        this.animation.i(this.focused);
        if (!this.focused) {
            this.buffer = this.setting.kg() == null ? "" : this.setting.kg();
        } else {
            this.setting.bI(this.buffer);
            this.buffer = this.setting.kg() == null ? "" : this.setting.kg();
        }
        String label = class_1074.method_4662((String) this.setting.getName(), (Object[]) new Object[0]);
        String hint = this.setting.kh();
        String hintText = hint == null || hint.isEmpty() ? label
            : class_1074.method_4662((String) hint, (Object[]) new Object[0]);
        String value = this.buffer == null ? "" : this.buffer;
        boolean hasText = !value.isEmpty();
        boolean showHint = !hasText && !this.focused;
        String shown = showHint ? hintText : value;
        float valueW = hasText ? Fonts.ROUNDBOLD.getWidth(value, LABEL_FONT_SIZE) : 0.0f;
        if (hasText && valueW > textW) {
            if (valueW - this.scrollOffset > textW) {
                this.scrollOffset = valueW - textW;
            }
            if (valueW - this.scrollOffset < 0.0f) {
                this.scrollOffset = valueW;
            }
        } else {
            this.scrollOffset = 0.0f;
        }
        Color labelColor = new Color(230, 230, 230, 235);
        Render2D.drawFont(class_3322.method_51448(), Fonts.ROUNDBOLD.getFont(LABEL_FONT_SIZE), label,
            labelX, labelY, labelColor);
        Color bg = new Color(23, 23, 23, 100);
        Color outline = ColorUtils.alpha(ColorUtils.getGlassOutlineColor(), this.focused ? 200 : 130);
        Render2D.drawRoundedRect(class_3322.method_51448(), fieldX, fieldY, fieldW, FIELD_HEIGHT,
            FIELD_RADIUS, bg);
        Render2D.drawRoundedRect(class_3322.method_51448(), fieldX, fieldY, fieldW, FIELD_HEIGHT,
            FIELD_RADIUS, outline);
        float textX = fieldX + 4.0f;
        float textY = fieldY + 3.1f;
        Render2D.startScissor(class_3322, fieldX + 1.0f, fieldY, fieldW - 2.0f, FIELD_HEIGHT);
        if (this.selected && hasText) {
            Render2D.drawRoundedRect(class_3322.method_51448(), textX, textY, Math.max(1.0f, valueW),
                Math.max(1.0f, Fonts.ROUNDBOLD.getHeight(LABEL_FONT_SIZE)), 0.0f,
                new Color(0, 0, 255, 150));
        }
        if (showHint) {
            Render2D.drawFont(class_3322.method_51448(), Fonts.ROUNDBOLD.getFont(LABEL_FONT_SIZE), hintText,
                textX, textY, ColorUtils.getTextColor((int) (200.0f * this.animation.rm())));
        } else if (hasText) {
            Render2D.drawFontRaw(class_3322.method_51448(), Fonts.ROUNDBOLD.getFont(LABEL_FONT_SIZE), value,
                textX - this.scrollOffset, textY, ColorUtils.getTextColor(255));
        }
        if (this.focused) {
            Color caret = ColorUtils.getTextColor(255);
            Color pulse = ColorUtils.pulse(caret, 15L);
            Render2D.drawFont(class_3322.method_51448(), Fonts.ROUNDBOLD.getFont(LABEL_FONT_SIZE), "|",
                textX - this.scrollOffset + valueW, textY - 0.25f,
                ColorUtils.alpha(pulse, (int) ((float) pulse.getAlpha() * this.animation.rk())));
        }
        Render2D.stopScissor(class_3322);
    }

    @Override
    public void mouseClicked(double d, double d2, int n) {
        if (n != 0) {
            return;
        }
        float fieldX = this.x + 5.0f;
        float fieldY = this.y + 2.0f + LABEL_FONT_SIZE + 4.0f;
        float fieldW = Math.max(1.0f, this.width - 10.0f);
        boolean inside = MathUtils.c(fieldX, fieldY, fieldW, FIELD_HEIGHT, (float) d, (float) d2);
        if (this.focused && !inside) {
            this.commitBuffer();
        }
        this.focused = inside;
        this.selected = false;
    }

    @Override
    public void mouseReleased(double d, double d2, int n) {
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        switch (n) {
            case 259: {
                if (this.selected) {
                    this.buffer = "";
                    this.selected = false;
                    if (this.focused) {
                        this.commitBuffer();
                    }
                }
                if (!this.focused || this.buffer == null || this.buffer.isEmpty()) {
                    break;
                }
                this.buffer = this.buffer.substring(0, this.buffer.length() - 1);
                this.commitBuffer();
                break;
            }
            case 256:
            case 257: {
                if (!this.focused) {
                    break;
                }
                this.commitBuffer();
                this.focused = false;
                this.selected = false;
                break;
            }
            case 261: {
                if (!this.focused) {
                    break;
                }
                this.buffer = "";
                this.selected = false;
                this.commitBuffer();
                break;
            }
            case 67: {
                if (!class_437.method_25441() || !this.focused || !this.selected || this.buffer == null
                    || this.buffer.isEmpty()) {
                    break;
                }
                GLFW.glfwSetClipboardString((long) aiG.method_22683().method_4490(), (CharSequence) this.buffer);
                this.selected = false;
                break;
            }
            case 86: {
                if (!class_437.method_25441() || !this.focused
                    || GLFW.glfwGetClipboardString((long) aiG.method_22683().method_4490()) == null) {
                    break;
                }
                this.selected = false;
                this.buffer = (this.buffer == null ? "" : this.buffer)
                    + GLFW.glfwGetClipboardString((long) aiG.method_22683().method_4490());
                this.commitBuffer();
                break;
            }
            case 65: {
                if (!class_437.method_25441() || !this.focused || this.buffer == null || this.buffer.isEmpty()) {
                    break;
                }
                this.selected = true;
            }
        }
    }

    @Override
    public void keyReleased(int n, int n2, int n3) {
    }

    @Override
    public void charTyped(char c, int n) {
        if (!this.focused) {
            return;
        }
        if (this.selected) {
            this.buffer = "";
        }
        if (this.buffer != null && this.buffer.length() >= this.setting.kf()) {
            return;
        }
        this.buffer = (this.buffer == null ? "" : this.buffer) + c;
        this.selected = false;
        this.commitBuffer();
    }

    @Override
    public void onClose() {
        this.commitBuffer();
        this.focused = false;
        this.selected = false;
        this.scrollOffset = 0.0f;
        this.buffer = "";
    }

    private void commitBuffer() {
        this.setting.bI(this.buffer == null ? "" : this.buffer);
        this.buffer = this.setting.kg() == null ? "" : this.setting.kg();
    }
}
