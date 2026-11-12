package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1541;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;
import ru.method.clarityvisuals.utils.render.fonts.Instance;

/**
 * Port of the auravisual "TNT Timer" module: shows a panel with the fuse countdown for every
 * primed TNT near the player. Entity tracking is UUID-keyed (the aura version stored numeric
 * entity ids that no longer have a getter in 1.21.4) and keeps a direct TntEntity reference.
 */
public final class TntTimer
extends Module {
    private static final double DETECTION_RADIUS = 10.0;
    private static final double DETECTION_RADIUS_SQUARED = 100.0;
    private static final int PANEL_HEIGHT = 36;
    private static final int PANEL_GAP = 42;
    private static final float MIN_PANEL_WIDTH = 132.0f;
    private static final int TNT_ICON_RGB = 1052950;
    private static final int BORDER_RGB = 16777215;
    private final class_1799 fb = new class_1799(class_1802.field_8626);
    private final Map<UUID, TimerEntry> fm = new HashMap<UUID, TimerEntry>();
    private long fc;
    private long fd;

    public TntTimer() {
        super("TNT Timer", ModuleCategory.ql);
    }

    @EventHandler
    public void onTick(ClientTickEvent clientTickEvent) {
        if (TntTimer.aiG.field_1724 == null || TntTimer.aiG.field_1687 == null) {
            this.fm.clear();
            return;
        }
        for (TimerEntry timerEntry : this.fm.values()) {
            if (timerEntry.dt == null || timerEntry.dt.method_31481() || timerEntry.dt.method_6969() <= 0) {
                timerEntry.leaving = true;
                continue;
            }
            TntTimer.e(timerEntry, timerEntry.dt);
        }
        for (class_1541 class_15412 : TntTimer.aiG.field_1687.method_8390(class_1541.class, TntTimer.aiG.field_1724.method_5829().method_1014(DETECTION_RADIUS), class_15412 -> !class_15412.method_31481() && class_15412.method_6969() > 0 && class_15412.method_5707(TntTimer.aiG.field_1724.method_19538()) <= DETECTION_RADIUS_SQUARED)) {
            UUID uUID = class_15412.method_5667();
            TimerEntry timerEntry = this.fm.get(uUID);
            if (timerEntry == null) {
                timerEntry = new TimerEntry(uUID, class_15412, ++this.fc, TntTimer.fa(class_15412));
                this.fm.put(uUID, timerEntry);
            }
            TntTimer.e(timerEntry, class_15412);
        }
    }

    @EventHandler
    public void onHudRender(HudRenderEvent hudRenderEvent) {
        if (this.fm.isEmpty() || TntTimer.aiG.field_1724 == null || TntTimer.aiG.field_1687 == null || TntTimer.aiG.field_1690.field_1842) {
            return;
        }
        long l = System.nanoTime();
        float f = this.fd == 0L ? 0.016666668f : class_3532.method_15363((float)(l - this.fd) / 1.0E9f, 0.0f, 0.05f);
        this.fd = l;
        class_332 class_3322 = hudRenderEvent.j();
        float f2 = hudRenderEvent.k().method_60637(true);
        int n = class_3322.method_51443();
        int n2 = class_3322.method_51421();
        int n3 = Math.max(42, n / 7);
        ArrayList<TimerEntry> ordered = new ArrayList<TimerEntry>(this.fm.values());
        ordered.sort(Comparator.comparingLong((TimerEntry timerEntry) -> timerEntry.order).reversed());
        int n4 = 0;
        for (TimerEntry timerEntry : ordered) {
            float targetY;
            if (timerEntry.leaving) {
                targetY = Float.isNaN(timerEntry.fb2) ? (float)n3 : timerEntry.fb2;
            } else {
                targetY = (float)n3 + (float)n4 * (float)PANEL_GAP;
                n4++;
            }
            if (Float.isNaN(timerEntry.fb2)) {
                timerEntry.fb2 = targetY - 10.0f;
            }
            timerEntry.fb2 = TntTimer.e(timerEntry.fb2, targetY, 13.0f, f);
            timerEntry.fz = TntTimer.e(timerEntry.fz, timerEntry.leaving ? 0.0f : 1.0f, 11.0f, f);
            this.a(class_3322, n2, timerEntry, f2);
        }
        this.fm.values().removeIf(timerEntry -> timerEntry.leaving && timerEntry.fz < 0.025f);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.fm.clear();
        this.fd = 0L;
    }

    private void a(class_332 class_3322, int n, TimerEntry timerEntry, float f) {
        float f2 = Math.max(0.0f, (float)(timerEntry.fuseTicks - f) / 20.0f);
        String string = String.format(Locale.US, "%.1f сек", f2);
        Instance instance = Fonts.LEGION_INTER.getFont(8.0f);
        float f3 = Math.max(MIN_PANEL_WIDTH, instance.getWidth(timerEntry.name) + 50.0f);
        float panelX = ((float)n - f3) * 0.5f;
        float panelY = timerEntry.fb2;
        float f4 = 0.92f + 0.08f * timerEntry.fz;
        float f5 = panelX + f3 * 0.5f;
        float f6 = panelY + 18.0f;
        class_4587 class_45872 = class_3322.method_51448();
        class_45872.method_22903();
        try {
            class_45872.method_46416(f5, f6, 0.0f);
            class_45872.method_22905(f4, f4, 1.0f);
            class_45872.method_46416(-f5, -f6, 0.0f);
            int n2 = TntTimer.b(TNT_ICON_RGB, Math.round(166.0f * timerEntry.fz));
            int n3 = TntTimer.b(BORDER_RGB, Math.round(22.0f * timerEntry.fz));
            Render2D.drawRoundedRect(class_45872, panelX - 0.7f, panelY - 0.7f, f3 + 0.7f + 0.7f, 37.4f, 8.7f, TntTimer.c(n3));
            Render2D.drawRoundedRect(class_45872, panelX, panelY, f3, (float)PANEL_HEIGHT, 8.0f, TntTimer.c(n2));
            class_45872.method_22903();
            try {
                class_45872.method_46416(panelX + 9.0f, panelY + 8.0f, 0.0f);
                class_45872.method_22905(1.25f, 1.25f, 1.0f);
                class_3322.method_51427(this.fb, 0, 0);
            }
            finally {
                class_45872.method_22909();
            }
            float f7 = panelX + 38.0f;
            Render2D.drawFont(class_45872, instance, timerEntry.name, f7, panelY + 6.0f, TntTimer.c(TntTimer.b(BORDER_RGB, 255)));
            Render2D.drawFont(class_45872, instance, string, f7, panelY + 20.0f, TntTimer.c(TntTimer.b(TntTimer.d(f2), Math.round(235.0f * timerEntry.fz))));
        }
        finally {
            class_45872.method_22909();
        }
    }

    static void e(TimerEntry timerEntry, class_1541 class_15412) {
        timerEntry.dt = class_15412;
        timerEntry.fuseTicks = class_15412.method_6969();
        timerEntry.name = TntTimer.fa(class_15412);
        timerEntry.leaving = false;
    }

    static String fa(class_1541 class_15412) {
        class_2561 class_25612 = class_15412.method_5797();
        if (class_25612 != null) {
            String string = class_25612.method_54160();
            if (string != null && !string.isBlank()) {
                return string;
            }
        }
        return "Динамит";
    }

    static float e(float f, float f2, float f3, float f4) {
        float f5 = 1.0f - (float)Math.exp(-f3 * f4);
        return f + (f2 - f) * f5;
    }

    static int d(float f) {
        if (f <= 1.0f) {
            return 16737380;
        }
        return f <= 2.0f ? 16757854 : 15198702;
    }

    static int b(int n, int n2) {
        return Math.round(class_3532.method_15363((float)n2, 0.0f, 255.0f)) << 24 | n & 16777215;
    }

    static Color c(int n) {
        return new Color(n, true);
    }

    private static final class TimerEntry {
        final UUID id;
        class_1541 dt;
        final long order;
        String name;
        int fuseTicks;
        boolean leaving;
        float fb2 = Float.NaN;
        float fz;

        TimerEntry(UUID uUID, class_1541 class_15412, long l, String string) {
            this.id = uUID;
            this.dt = class_15412;
            this.order = l;
            this.name = string;
        }
    }
}
