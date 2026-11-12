package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.animation.Animation;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1058;
import net.minecraft.class_1074;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1921;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.animation.EasingMath;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.util.TextFormattingStripper;
import ru.method.clarityvisuals.animation.TimedProgress;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Font;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;
import ru.method.clarityvisuals.client.hud.VisualHud;

public class PotionHud
extends HudElement {
    private static final float jV = 0.82365f;
    private final AnimatedComponent jW = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent jX = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent jY = new AnimatedComponent(Easing.aiZ);
    private boolean hm = true;
    private int hn;
    private final Map<String, TimedProgress> jZ = new LinkedHashMap<String, TimedProgress>();
    private final Map<String, String> ka = new HashMap<String, String>();
    private static final float kb = 12.0f;
    private static final float kc = 2.0f;
    private static final String[] kd = new String[]{"", "I", "II", "III", "IV", "V"};

    public PotionHud() {
        super("PotionHud");
    }

    private static final class Row {
        final class_1293 effect;
        final String name;
        final String level;
        final String timer;
        final float nameW;
        final float levelW;
        final float timerW;
        final float rowW;

        Row(class_1293 effect, String name, String level, String timer, float nameW, float levelW, float timerW, float rowW) {
            this.effect = effect;
            this.name = name;
            this.level = level;
            this.timer = timer;
            this.nameW = nameW;
            this.levelW = levelW;
            this.timerW = timerW;
            this.rowW = rowW;
        }
    }

    private static String a(class_1293 class_12932) {
        if (class_12932.method_48559()) {
            return "\u221e";
        }
        int n = class_12932.method_5584();
        int n2 = n / 1200;
        int n3 = n % 1200 / 20;
        return n3 < 10 ? n2 + ":0" + n3 : n2 + ":" + n3;
    }

    private static String j(int n) {
        if (n <= 0) {
            return "";
        }
        return n < kd.length ? kd[n] : String.valueOf(n);
    }

    private Color c(Color color) {
        return new Color(Math.round((float)color.getRed() * 0.7f), Math.round((float)color.getGreen() * 0.7f), Math.round((float)color.getBlue() * 0.7f), color.getAlpha());
    }

    private boolean cm() {
        if (HudEditorScreen.mb()) {
            return true;
        }
        return PotionHud.aiG.field_1724 != null && !PotionHud.aiG.field_1724.method_6026().isEmpty();
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        float f;
        float f2;
        float f3;
        float f4;
        Object object;
        Object object22;
        Collection<class_1293> collection;
        if (PotionHud.fullNullCheck() || this.bL()) {
            return;
        }
        boolean bl = this.cm();
        float f12 = this.jY.a(bl ? 1.0f : 0.0f, 200L);
        if (f12 <= 0.0f) {
            this.hm = true;
            return;
        }
        boolean bl2 = HudEditorScreen.mb();
        if (bl2) {
            if (PotionHud.aiG.field_1724 != null && !PotionHud.aiG.field_1724.method_6026().isEmpty()) {
                collection = PotionHud.aiG.field_1724.method_6026();
            } else {
                collection = new ArrayList<class_1293>();
                collection.add(new class_1293(class_1294.field_5904, 1200, 1));
                collection.add(new class_1293(class_1294.field_5910, 600, 0));
                collection.add(new class_1293(class_1294.field_5918, 3600, 0));
            }
        } else {
            collection = PotionHud.aiG.field_1724.method_6026();
        }
        float f13 = this.getX();
        float f14 = this.getY();
        ArrayList<Row> rows = new ArrayList<Row>();
        float maxW = 0.0f;
        for (class_1293 effect : collection) {
            class_1291 type = (class_1291)effect.method_5579().comp_349();
            String effectName = this.ka.computeIfAbsent(type.method_5560().getString(), TextFormattingStripper::da);
            int amplifier = effect.method_5578();
            String level = amplifier > 0 ? " " + (amplifier + 1) : "";
            String timer = PotionHud.a(effect);
            float nameW = Fonts.UNBOUNDED.getWidth(effectName, 6.5f);
            float levelW = level.isEmpty() ? 0.0f : Fonts.UNBOUNDED.getWidth(level, 6.5f);
            float timerW = Fonts.UNBOUNDED.getWidth(timer, 6.5f);
            float rowW = 19.0f + nameW + levelW + 10.0f + timerW + 5.0f;
            maxW = Math.max(maxW, rowW);
            rows.add(new Row(effect, effectName, level, timer, nameW, levelW, timerW, rowW));
        }
        float totalH = rows.isEmpty() ? 0.0f : (float)rows.size() * 19.0f + (float)(rows.size() - 1) * 4.0f;
        long l = this.hm ? 1L : 200L;
        float f24 = this.jW.a(maxW, l);
        float f25 = this.jX.a(totalH, l);
        this.hm = false;
        if (bl2) {
            f24 = maxW;
            f25 = totalH;
        }
        int n = Math.max(0, Math.min(255, Math.round(255.0f * f12)));
        float f26 = this.b(hudRenderEvent.j().method_51448(), f13, f14);
        hudRenderEvent.j().method_51448().method_22903();
        float[] fArray = this.bK();
        float f27 = this.gn.rk() * f12 * this.bJ();
        float f28 = f13 + f24 / 2.0f + fArray[0];
        float f29 = f14 + f25 / 2.0f + fArray[1];
        hudRenderEvent.j().method_51448().method_46416(f28, f29, 0.0f);
        hudRenderEvent.j().method_51448().method_22905(f27, f27, 1.0f);
        hudRenderEvent.j().method_51448().method_46416(-f28, -f29, 0.0f);
        class_4587 matrices = hudRenderEvent.j().method_51448();
        float rowY = f14;
        for (Row row : rows) {
            LegionHud.card(matrices, f13, rowY, row.rowW, 19.0f, 7.0f, n);
            float rowCenter = rowY + 9.5f;
            matrices.method_22903();
            matrices.method_46416(f13 + 5.0f, rowCenter - 4.5f, 0.0f);
            matrices.method_22905(0.5f, 0.5f, 1.0f);
            this.a(hudRenderEvent.j(), row.effect, 0.0f, 0.0f, 18.0f, n);
            matrices.method_22909();
            float nameX = f13 + 5.0f + 9.0f + 5.0f;
            float textY = rowCenter - 3.25f;
            LegionHud.text(matrices, Fonts.UNBOUNDED, row.name, nameX, textY, 6.5f, LegionHud.withAlpha(LegionHud.SOFT, n));
            if (!row.level.isEmpty()) {
                LegionHud.text(matrices, Fonts.UNBOUNDED, row.level, nameX + row.nameW + 3.0f, textY, 6.5f, LegionHud.withAlpha(LegionHud.GRAY, n));
            }
            float timerX = f13 + row.rowW - 5.0f - row.timerW;
            LegionHud.text(matrices, Fonts.UNBOUNDED, row.timer, timerX, textY, 6.5f, LegionHud.withAlpha(LegionHud.SOFT, n));
            rowY += 23.0f;
        }
        hudRenderEvent.j().method_51448().method_22909();
        this.b(hudRenderEvent.j().method_51448());
        this.b(f13, f14, f24, f25, f26);
        super.onRender2D(hudRenderEvent);
    }

    private String b(class_1293 class_12932) {
        class_1291 class_12912 = (class_1291)class_12932.method_5579().comp_349();
        String string = class_12912 != null ? class_12912.method_5567() : "unknown";
        return string + ":" + class_12932.method_5578();
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

    private void a(class_332 class_3322, class_1293 class_12932, float f, float f2, float f3, int n) {
        if (class_12932 == null || aiG == null) {
            return;
        }
        try {
            class_1058 class_10582 = aiG.method_18505().method_18663(class_12932.method_5579());
            if (class_10582 == null) {
                return;
            }
            int n2 = Math.max(0, Math.min(255, n));
            int n3 = n2 << 24 | 0xFFFFFF;
            int n4 = Math.max(1, Math.round(f3));
            RenderSystem.disableDepthTest();
            class_3322.method_52710(class_1921::method_62277, class_10582, Math.round(f), Math.round(f2), n4, n4, n3);
            RenderSystem.disableDepthTest();
        }
        catch (Exception exception) {
        }
    }
}
