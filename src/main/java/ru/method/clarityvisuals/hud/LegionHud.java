package ru.method.clarityvisuals.hud;

import java.awt.Color;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Font;

public final class LegionHud {
    public static final Color ACCENT = new Color(255, 255, 255);
    public static final Color SOFT = new Color(255, 255, 255);
    public static final Color GRAY = new Color(165, 165, 165);
    public static final Color BG = new Color(24, 24, 24);
    public static final int BG_TINT = -15198184;

    private LegionHud() {
    }

    public static int clampAlpha(int n) {
        return Math.max(0, Math.min(255, n));
    }

    public static int replAlpha(int color, int alpha) {
        return (color & 0xFFFFFF) | (LegionHud.clampAlpha(alpha) << 24);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), LegionHud.clampAlpha(alpha));
    }

    public static void card(class_4587 matrices, float x, float y, float w, float h, float cr, int alpha) {
        if (alpha <= 0 || w <= 0.0f || h <= 0.0f) {
            return;
        }
        Render2D.drawBlurredRect(matrices, x, y, w, h, cr, 14.0f, new Color(0, 0, 0, LegionHud.clampAlpha(Math.round((float)alpha * 0.14f))));
        Render2D.drawRoundedRect(matrices, x, y, w, h, cr, new Color(24, 24, 24, LegionHud.clampAlpha(Math.round((float)alpha * 0.46f))));
    }

    public static void text(class_4587 matrices, Font font, String string, float x, float y, float size, Color color) {
        if (string == null || string.isEmpty() || color.getAlpha() <= 0) {
            return;
        }
        try {
            Render2D.drawFont(matrices, font.getFont(size), string, x, y, color);
        } catch (Exception exception) {
        }
    }
}
