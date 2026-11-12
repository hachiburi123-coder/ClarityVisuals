package ru.method.clarityvisuals.screen.clickgui.legion;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.class_1044;
import net.minecraft.class_1074;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

/**
 * Цветовая палитра (HSV + прозрачность + HEX) для настроек модулей в Legion-GUI.
 * Живёт в дизайн-координатах GUI, открывается рядом с панелью настроек,
 * свотч выбранного цвета в панели подсвечивается рамкой.
 */
public final class LegionColorPicker {
    private static final class_2960 HUE_TEXTURE = ClarityVisuals.id("client/hue/hue.png");
    private static final class_2960 EMPTY_TEXTURE = ClarityVisuals.id("client/hue/empty.png");
    private static final class_2960 CLOSE_TEXTURE = ClarityVisuals.id("client/colorpicker/xmark.png");

    private static final float W = 140.0f;
    private static final float H = 178.0f;
    private static final float PAD = 10.0f;
    private static final float CONTENT_W = 120.0f;
    private static final float TITLE_H = 18.0f;
    private static final float SV_SIZE = 100.0f;
    private static final float HUE_W = 12.0f;
    private static final float CLOSE_SIZE = 12.0f;
    private static final float PREVIEW_W = 20.0f;
    private static final float OP_H = 9.0f;
    private static final float HEX_H = 10.0f;
    private static final int HEX_MAX_LENGTH = 9;

    private ColorSetting setting;
    private boolean open;
    private boolean closing;
    private float alpha;
    private float x;
    private float y;
    private float designW;
    private float designH;
    private boolean draggingPicker;
    private float dragOffX;
    private float dragOffY;
    private boolean draggingSv;
    private boolean draggingHue;
    private boolean draggingOpacity;
    private float hue;
    private float saturation;
    private float value;
    private float opacity;
    private boolean hsvInitialized;
    private boolean hexFocused;
    private boolean hexSelected;
    private String hexBuffer = "";

    public boolean isActive() {
        return this.open;
    }

    public boolean isAlive() {
        return this.open;
    }

    public ColorSetting getSetting() {
        return this.setting;
    }

    public boolean isInside(double mx, double my) {
        return this.open && mx >= (double)this.x && mx <= (double)(this.x + W)
            && my >= (double)this.y && my <= (double)(this.y + H);
    }

    public void open(ColorSetting colorSetting, float panelX, float panelW, float anchorY,
                     float designW, float designH) {
        this.setting = colorSetting;
        this.designW = designW;
        this.designH = designH;
        this.closing = false;
        this.open = true;
        this.alpha = 0.0f;
        this.stopDragging();
        this.hexFocused = false;
        this.hexSelected = false;
        this.hexBuffer = "";
        this.hsvInitialized = false;
        float px = panelX - W - 8.0f;
        if (px < 6.0f) {
            px = panelX + panelW + 8.0f;
        }
        this.x = MathUtils.k(px, 6.0f, Math.max(6.0f, designW - W - 6.0f));
        this.y = MathUtils.k(anchorY - H * 0.5f, 6.0f, Math.max(6.0f, designH - H - 6.0f));
    }

    public void close() {
        if (!this.open || this.closing) {
            return;
        }
        this.closing = true;
        this.stopDragging();
        this.hexFocused = false;
        this.hexSelected = false;
    }

    public void closeNow() {
        this.open = false;
        this.closing = false;
        this.alpha = 0.0f;
        this.stopDragging();
        this.hexFocused = false;
        this.hexSelected = false;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!this.isActive()) {
            return false;
        }
        if (button != 0) {
            this.stopDragging();
            this.hexFocused = false;
            this.hexSelected = false;
            return this.isInside(mx, my);
        }
        if (!this.isInside(mx, my)) {
            this.stopDragging();
            return false;
        }
        float fmx = (float)mx;
        float fmy = (float)my;
        float closeX = this.getCloseX();
        float closeY = this.y + 3.0f;
        if (fmx >= closeX && fmx <= closeX + CLOSE_SIZE && fmy >= closeY && fmy <= closeY + CLOSE_SIZE) {
            this.close();
            return true;
        }
        if (fmy <= this.y + TITLE_H) {
            this.draggingPicker = true;
            this.dragOffX = fmx - this.x;
            this.dragOffY = fmy - this.y;
            this.hexFocused = false;
            this.hexSelected = false;
            return true;
        }
        float svX = this.x + PAD;
        float svY = this.y + TITLE_H;
        if (fmx >= svX && fmx <= svX + SV_SIZE && fmy >= svY && fmy <= svY + SV_SIZE) {
            this.draggingSv = true;
            this.hexFocused = false;
            this.hexSelected = false;
            this.updateSv(fmx, fmy);
            return true;
        }
        float hueX = svX + SV_SIZE + 8.0f;
        if (fmx >= hueX && fmx <= hueX + HUE_W && fmy >= svY && fmy <= svY + SV_SIZE) {
            this.draggingHue = true;
            this.hexFocused = false;
            this.hexSelected = false;
            this.updateHue(fmy);
            return true;
        }
        float opY = svY + SV_SIZE + 16.0f;
        float sliderX = svX + PREVIEW_W + 6.0f;
        float sliderW = this.opacitySliderWidth();
        if (fmx >= sliderX && fmx <= sliderX + sliderW && fmy >= opY && fmy <= opY + OP_H) {
            this.draggingOpacity = true;
            this.hexFocused = false;
            this.hexSelected = false;
            this.updateOpacity(fmx);
            return true;
        }
        float hexY = svY + SV_SIZE + 40.0f;
        if (fmx >= svX && fmx <= svX + CONTENT_W && fmy >= hexY && fmy <= hexY + HEX_H) {
            this.hexFocused = true;
            this.hexSelected = true;
            if (this.hexBuffer == null || this.hexBuffer.isEmpty()) {
                this.hexBuffer = this.formatHex((Color)this.setting.jT());
            }
            return true;
        }
        this.hexFocused = false;
        this.hexSelected = false;
        return true;
    }

    public void mouseReleased() {
        this.stopDragging();
    }

    public boolean keyPressed(int key) {
        if (!this.isActive()) {
            return false;
        }
        if (this.hexFocused) {
            switch (key) {
                case 256: {
                    this.hexFocused = false;
                    this.hexSelected = false;
                    this.syncHexBuffer();
                    return true;
                }
                case 257: {
                    this.applyHexBuffer();
                    this.hexFocused = false;
                    this.hexSelected = false;
                    this.syncHexBuffer();
                    return true;
                }
                case 259: {
                    if (this.hexSelected) {
                        this.hexBuffer = "";
                        this.hexSelected = false;
                        return true;
                    }
                    if (this.hexBuffer != null && !this.hexBuffer.isEmpty()) {
                        this.hexBuffer = this.hexBuffer.substring(0, this.hexBuffer.length() - 1);
                        this.hexBuffer = this.normalizeHexInput(this.hexBuffer);
                        this.applyHexBuffer();
                    }
                    return true;
                }
                case 261: {
                    this.hexBuffer = "";
                    this.hexSelected = false;
                    return true;
                }
                case 67: {
                    if (!class_437.method_25441()) {
                        return true;
                    }
                    String hex = this.formatHex((Color)this.setting.jT());
                    GLFW.glfwSetClipboardString((long)class_310.method_1551().method_22683().method_4490(), hex);
                    this.hexSelected = false;
                    return true;
                }
                case 86: {
                    if (!class_437.method_25441()) {
                        return true;
                    }
                    String clipboard = GLFW.glfwGetClipboardString((long)class_310.method_1551().method_22683().method_4490());
                    if (clipboard != null && !clipboard.isBlank()) {
                        this.hexBuffer = this.normalizeHexInput(clipboard);
                        this.hexSelected = false;
                        this.applyHexBuffer();
                    }
                    return true;
                }
                case 65: {
                    if (class_437.method_25441()) {
                        this.hexSelected = this.hexBuffer != null && !this.hexBuffer.isEmpty();
                    }
                    return true;
                }
            }
            return true;
        }
        if (key == 256) {
            this.close();
            return true;
        }
        return false;
    }

    public boolean charTyped(char c) {
        if (!this.isActive()) {
            return false;
        }
        if (this.hexFocused && (this.isHexChar(c) || c == '#')) {
            if (this.hexSelected) {
                this.hexBuffer = "";
            }
            String next = (this.hexBuffer == null ? "" : this.hexBuffer) + c;
            this.hexBuffer = this.normalizeHexInput(next);
            this.hexSelected = false;
            this.applyHexBuffer();
        }
        return true;
    }

    public void render(class_332 ctx, float mx, float my, int ca, int accent) {
        if (!this.open) {
            return;
        }
        float target = this.closing ? 0.0f : 1.0f;
        this.alpha += (target - this.alpha) * 0.24f;
        if (Math.abs(target - this.alpha) < 0.02f) {
            this.alpha = target;
        }
        if (this.closing && this.alpha <= 0.015f) {
            this.closeNow();
            return;
        }
        int A = (int)Math.min(255.0f, this.alpha * (float)Math.max(0, ca));
        if (A <= 3) {
            return;
        }
        if (this.draggingPicker) {
            this.x = mx - this.dragOffX;
            this.y = my - this.dragOffY;
            this.x = MathUtils.k(this.x, 6.0f, Math.max(6.0f, this.designW - W - 6.0f));
            this.y = MathUtils.k(this.y, 6.0f, Math.max(6.0f, this.designH - H - 6.0f));
        }
        if (this.draggingSv) {
            this.updateSv(mx, my);
        }
        if (this.draggingHue) {
            this.updateHue(my);
        }
        if (this.draggingOpacity) {
            this.updateOpacity(mx);
        }
        this.syncHsvFromSettingIfNeeded();
        this.syncHexBuffer();
        var matrix = ctx.method_51448();
        float cx = this.x + W / 2.0f;
        float cy = this.y + H / 2.0f;
        float sc = 0.94f + 0.06f * this.alpha;
        matrix.method_22903();
        matrix.method_46416(cx, cy, 0.0f);
        matrix.method_22905(sc, sc, 1.0f);
        matrix.method_46416(-cx, -cy, 0.0f);
        this.rr(matrix, this.x, this.y, W, H, 10.0f, new Color(11, 11, 13, Math.min(255, A + A / 3)));
        this.border(matrix, this.x, this.y, W, H, 10.0f, 0.8f, new Color((accent >> 16) & 0xFF, (accent >> 8) & 0xFF, accent & 0xFF, A * 55 / 100));
        String title = class_1074.method_4662(this.setting.getName(), new Object[0]);
        float closeX = this.getCloseX();
        float closeY = this.y + 3.0f;
        float titleMaxW = closeX - 4.0f - (this.x + PAD);
        String fitted = this.fit(title, 7.0f, titleMaxW);
        Render2D.drawFont(matrix, Fonts.LEGION_INTER.getFont(7.0f), fitted, this.x + PAD, this.y + 5.0f, ColorUtils.getTextColor(A));
        boolean closeHover = mx >= (double)closeX && mx <= (double)(closeX + CLOSE_SIZE)
            && my >= (double)closeY && my <= (double)(closeY + CLOSE_SIZE);
        this.rr(matrix, closeX, closeY, CLOSE_SIZE, CLOSE_SIZE, 4.0f,
            new Color(closeHover ? 58 : 34, closeHover ? 58 : 34, closeHover ? 64 : 38, A));
        this.drawTexture(ctx, CLOSE_TEXTURE, closeX + 1.5f, closeY + 1.5f, CLOSE_SIZE - 3.0f, CLOSE_SIZE - 3.0f, 2.0f, ColorUtils.getTextColor(A));
        float svX = this.x + PAD;
        float svY = this.y + TITLE_H;
        this.drawSvSquare(matrix, svX, svY);
        this.drawHueBar(ctx, svX + SV_SIZE + 8.0f, svY);
        float opLblY = svY + SV_SIZE + 6.0f;
        String opLabel = class_1074.method_4662("clarityvisuals.colorpicker.opacity", new Object[0]);
        Render2D.drawFont(matrix, Fonts.LEGION_INTER.getFont(6.5f), opLabel, svX, opLblY, new Color(228, 230, 238, A));
        String pct = Math.round(this.opacity * 100.0f) + "%";
        float sliderX = svX + PREVIEW_W + 6.0f;
        float sliderW = this.opacitySliderWidth();
        Render2D.drawFont(matrix, Fonts.LEGION_INTER.getFont(6.5f), pct, sliderX + sliderW - Fonts.LEGION_INTER.getWidth(pct, 6.5f), opLblY, new Color(228, 230, 238, A));
        float opY = svY + SV_SIZE + 16.0f;
        this.drawOpacityRow(ctx, svX, opY);
        float hexLblY = opY + OP_H + 6.0f;
        Render2D.drawFont(matrix, Fonts.LEGION_INTER.getFont(6.5f), "HEX", svX, hexLblY, new Color(228, 230, 238, A));
        float hexY = hexLblY + 9.0f;
        this.drawHexField(ctx, svX, hexY);
        matrix.method_22909();
    }

    private void drawSvSquare(class_4587 matrix, float x, float y) {
        Color base = Color.getHSBColor(this.hue, 1.0f, 1.0f);
        Render2D.drawRoundedHorizontalGradient(matrix, x, y, SV_SIZE, SV_SIZE, 6.0f, Color.WHITE, base);
        Render2D.drawRoundedVerticalGradient(matrix, x, y, SV_SIZE, SV_SIZE, 6.0f, new Color(0, 0, 0, 0), new Color(0, 0, 0, 255));
        float px = x + this.saturation * SV_SIZE;
        float py = y + (1.0f - this.value) * SV_SIZE;
        px = MathUtils.k(px, x + 4.0f, x + SV_SIZE - 4.0f);
        py = MathUtils.k(py, y + 4.0f, y + SV_SIZE - 4.0f);
        px = Math.round(px);
        py = Math.round(py);
        Render2D.drawRoundedRect(matrix, px - 4.0f - 1.0f, py - 4.0f - 1.0f, 8.0f + 2.0f, 8.0f + 2.0f, 5.0f, new Color(0, 0, 0, 150));
        Render2D.drawRoundedRect(matrix, px - 4.0f, py - 4.0f, 8.0f, 8.0f, 4.0f, new Color(255, 255, 255, 225));
        Color rgb = this.currentRgb();
        Render2D.drawRoundedRect(matrix, px - 3.1f, py - 3.1f, 6.2f, 6.2f, 3.1f, rgb);
    }

    private void drawHueBar(class_332 ctx, float x, float y) {
        this.drawTexture(ctx, HUE_TEXTURE, x, y, HUE_W, SV_SIZE, Math.min(6.0f, HUE_W * 0.5f), Color.WHITE);
        float cy = y + this.hue * SV_SIZE;
        cy = MathUtils.k(cy, y + 2.0f, y + SV_SIZE - 2.0f);
        Render2D.drawRoundedRect(ctx.method_51448(), x + 2.0f - 0.6f, cy - 0.9f, HUE_W - 4.0f + 1.2f, 1.6f + 1.8f, 1.7f, new Color(0, 0, 0, 140));
        Render2D.drawRoundedRect(ctx.method_51448(), x + 2.0f, cy - 0.8f, HUE_W - 4.0f, 1.6f, 0.8f, new Color(255, 255, 255, 235));
    }

    private void drawOpacityRow(class_332 ctx, float x, float y) {
        float sliderX = x + PREVIEW_W + 6.0f;
        float sliderW = this.opacitySliderWidth();
        Color rgb = this.currentRgb();
        Color alphaColor = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), this.opacity());
        this.drawTexture(ctx, EMPTY_TEXTURE, sliderX, y, sliderW, OP_H, 4.5f, Color.WHITE);
        Render2D.drawRoundedHorizontalGradient(ctx.method_51448(), sliderX, y, sliderW, OP_H, 4.5f,
            new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0),
            new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255));
        float px = MathUtils.k(sliderX + this.opacity * sliderW, sliderX + 2.0f, sliderX + sliderW - 2.0f);
        Render2D.drawRoundedRect(ctx.method_51448(), px - 0.9f - 0.6f, y - 0.6f, 1.8f + 1.2f, OP_H + 1.2f, 1.5f, new Color(0, 0, 0, 140));
        Render2D.drawRoundedRect(ctx.method_51448(), px - 0.9f, y, 1.8f, OP_H, 0.9f, new Color(255, 255, 255, 235));
        Render2D.drawRoundedRect(ctx.method_51448(), x, y, PREVIEW_W, OP_H, 4.5f, alphaColor);
    }

    private void drawHexField(class_332 ctx, float x, float y) {
        var matrix = ctx.method_51448();
        Render2D.drawRoundedRect(matrix, x, y, CONTENT_W, HEX_H, 4.0f, new Color(20, 20, 23, 190));
        Render2D.drawRoundedRect(matrix, x, y, CONTENT_W, HEX_H, 4.0f, new Color(70, 70, 78, 160));
        String text = this.hexBuffer == null || this.hexBuffer.isEmpty() ? "#" : this.hexBuffer;
        float size = 6.5f;
        float tx = x + 4.0f;
        float th = Fonts.REGULAR.getHeight(size);
        float ty = y + (HEX_H - th) / 2.0f;
        Render2D.startScissor(ctx, x + 1.0f, y + 1.0f, CONTENT_W - 2.0f, HEX_H - 2.0f);
        if (this.hexFocused && this.hexSelected && !text.isEmpty()) {
            float tw = Math.max(1.0f, Fonts.REGULAR.getWidth(text, size));
            Render2D.drawRoundedRect(matrix, tx, ty, tw, Math.max(1.0f, th), 0.0f, new Color(0, 120, 215, 150));
        }
        Render2D.drawFont(matrix, Fonts.REGULAR.getFont(size), text, tx, ty, ColorUtils.getTextColor(220));
        if (this.hexFocused) {
            Color caret = ColorUtils.pulse(ColorUtils.getTextColor(220), 15L);
            Render2D.drawFont(matrix, Fonts.REGULAR.getFont(size), "|", tx + Fonts.REGULAR.getWidth(text, size), ty, caret);
        }
        Render2D.stopScissor(ctx);
    }

    private float getCloseX() {
        return this.x + W - PAD - CLOSE_SIZE;
    }

    private float opacitySliderWidth() {
        return Math.max(1.0f, CONTENT_W - PREVIEW_W - 6.0f);
    }

    private int opacity() {
        return this.clampInt(Math.round(this.opacity * 255.0f), 0, 255);
    }

    private void updateSv(float mx, float my) {
        float x = this.x + PAD;
        float y = this.y + TITLE_H;
        this.saturation = MathUtils.k((mx - x) / SV_SIZE, 0.0f, 1.0f);
        this.value = 1.0f - MathUtils.k((my - y) / SV_SIZE, 0.0f, 1.0f);
        this.applyCurrentColor();
    }

    private void updateHue(float my) {
        float y = this.y + TITLE_H;
        this.hue = MathUtils.k((my - y) / SV_SIZE, 0.0f, 1.0f);
        this.applyCurrentColor();
    }

    private void updateOpacity(float mx) {
        float x = this.x + PAD + PREVIEW_W + 6.0f;
        this.opacity = MathUtils.k((mx - x) / this.opacitySliderWidth(), 0.0f, 1.0f);
        this.applyCurrentColor();
    }

    private void applyCurrentColor() {
        int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.value);
        this.setting.d(new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, this.opacity()));
    }

    private void syncHsvFromSettingIfNeeded() {
        if (this.hsvInitialized) {
            return;
        }
        Color color = (Color)this.setting.jT();
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.value = hsb[2];
        this.opacity = (float)color.getAlpha() / 255.0f;
        this.hsvInitialized = true;
    }

    private Color currentRgb() {
        int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.value);
        return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
    }

    private void stopDragging() {
        this.draggingPicker = false;
        this.draggingSv = false;
        this.draggingHue = false;
        this.draggingOpacity = false;
    }

    private void syncHexBuffer() {
        if (!this.hexFocused) {
            this.hexBuffer = this.formatHex((Color)this.setting.jT());
        }
    }

    private void applyHexBuffer() {
        Color color = this.parseHex(this.hexBuffer);
        if (color != null) {
            this.setting.d(color);
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            this.hue = hsb[0];
            this.saturation = hsb[1];
            this.value = hsb[2];
            this.opacity = (float)color.getAlpha() / 255.0f;
            this.hsvInitialized = true;
        }
    }

    private String normalizeHexInput(String input) {
        if (input == null) {
            return "";
        }
        String s = input.trim().toUpperCase(Locale.ROOT);
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() && sb.length() < HEX_MAX_LENGTH - 1; ++i) {
            char c = s.charAt(i);
            if (this.isHexChar(c)) {
                sb.append(c);
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        return "#" + sb;
    }

    private boolean isHexChar(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    private Color parseHex(String input) {
        if (input == null) {
            return null;
        }
        String s = input.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6 && s.length() != 8) {
            return null;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (this.isHexChar(s.charAt(i))) {
                continue;
            }
            return null;
        }
        try {
            int r = Integer.parseInt(s.substring(0, 2), 16);
            int g = Integer.parseInt(s.substring(2, 4), 16);
            int b = Integer.parseInt(s.substring(4, 6), 16);
            int a = s.length() == 8 ? Integer.parseInt(s.substring(6, 8), 16) : 255;
            return new Color(r, g, b, a);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatHex(Color color) {
        if (color == null) {
            return "#FFFFFF";
        }
        String rgb = String.format(Locale.ROOT, "%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        if (color.getAlpha() < 255) {
            return "#" + rgb + String.format(Locale.ROOT, "%02X", color.getAlpha());
        }
        return "#" + rgb;
    }

    private String fit(String text, float size, float maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0.0f) {
            return text == null ? "" : text;
        }
        if (Fonts.LEGION_INTER.getWidth(text, size) <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            String candidate = sb.toString() + text.charAt(i) + "...";
            if (Fonts.LEGION_INTER.getWidth(candidate, size) > maxWidth) {
                break;
            }
            sb.append(text.charAt(i));
        }
        sb.append("...");
        return sb.toString();
    }

    private int clampInt(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        return Math.min(v, max);
    }

    private void rr(class_4587 matrix, float x, float y, float w, float h, float radius, Color color) {
        Render2D.drawRoundedRect(matrix, x, y, w, h, radius, color);
    }

    private void border(class_4587 matrix, float x, float y, float w, float h, float radius, float thickness, Color color) {
        Render2D.drawBorder(matrix, x, y, w, h, radius, thickness, new ColorRGBA(color));
    }

    private void drawTexture(class_332 ctx, class_2960 id, float x, float y, float w, float h, float radius, Color color) {
        class_1044 texture = class_310.method_1551().method_1531().method_4619(id);
        if (texture != null) {
            texture.method_4527(true, false);
        }
        Render2D.drawTexture(ctx.method_51448(), x, y, w, h, radius, texture, color);
    }
}
