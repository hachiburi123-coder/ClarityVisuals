package ru.method.clarityvisuals.hud;

import java.awt.Color;
import java.time.LocalTime;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_2761;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.api.event.PacketEvent;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

public class WatermarkHud
extends HudElement {
    private static final class_2960 CLARITY_ICON = ClarityVisuals.id("textures/gui/clarity_icon.png");
    private double lastBpsX;
    private double lastBpsY;
    private double lastBpsZ;
    private boolean lastBpsSet;
    private long lastBpsTime = System.currentTimeMillis();
    private float rawBps;
    private float animFps;
    private float animPing;
    private float animTps = 20.0f;
    private float animBps;
    private long lastTpsTime;
    private float lastTps = 20.0f;

    public WatermarkHud() {
        super("Watermark");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive receive) {
        if (receive.i() instanceof class_2761) {
            long now = System.currentTimeMillis();
            if (this.lastTpsTime > 0L) {
                float dt = (float)(now - this.lastTpsTime) / 1000.0f;
                if (dt > 0.05f) {
                    this.lastTps = Math.max(0.0f, Math.min(20.0f, 20.0f / dt));
                }
            }
            this.lastTpsTime = now;
        }
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        if (WatermarkHud.fullNullCheck() || this.bL()) {
            return;
        }
        float appear = this.gn.rk();
        if (appear <= 0.01f) {
            return;
        }
        int alpha = Math.max(0, Math.min(255, Math.round(255.0f * appear)));
        class_640 entry;
        int ping = 0;
        if (aiG.method_1562() != null && aiG.field_1724 != null && (entry = aiG.method_1562().method_2871(aiG.field_1724.method_5667())) != null) {
            ping = entry.method_2959();
        }
        if (aiG.field_1724 != null) {
            double cx = aiG.field_1724.method_23317();
            double cy = aiG.field_1724.method_23318();
            double cz = aiG.field_1724.method_23321();
            long nowTime = System.currentTimeMillis();
            if (this.lastBpsSet && nowTime - this.lastBpsTime >= 500L) {
                double dx = cx - this.lastBpsX;
                double dy = cy - this.lastBpsY;
                double dz = cz - this.lastBpsZ;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double secs = (double)(nowTime - this.lastBpsTime) / 1000.0;
                if (secs > 0.0) {
                    this.rawBps = (float)(dist / secs);
                }
                this.lastBpsX = cx;
                this.lastBpsY = cy;
                this.lastBpsZ = cz;
                this.lastBpsTime = nowTime;
            }
            if (!this.lastBpsSet) {
                this.lastBpsX = cx;
                this.lastBpsY = cy;
                this.lastBpsZ = cz;
                this.lastBpsSet = true;
            }
        }
        LocalTime now = LocalTime.now();
        String timeText = String.format("%02d:%02d:%02d", now.getHour(), now.getMinute(), now.getSecond());
        this.animFps += ((float)aiG.method_47599() - this.animFps) * 0.12f;
        this.animPing += ((float)ping - this.animPing) * 0.12f;
        this.animTps += (this.lastTps - this.animTps) * 0.12f;
        this.animBps += (this.rawBps - this.animBps) * 0.12f;
        float iconSize = 12.0f;
        float iconW = 12.0f;
        float textSize = 7.0f;
        float yCenter = 4.0f;
        float gap = 2.0f;
        String fpsText = Math.round(this.animFps) + " FPS";
        String pingText = Math.round(this.animPing) + " ms";
        String bpsText = String.format("%.1f BPS", Float.valueOf(this.animBps));
        String tpsText = String.format("%.1f TPS", Float.valueOf(this.animTps));
        float fpsW = Fonts.LEGION_ICONS.getWidth("q", textSize) + 2.0f + Fonts.UNBOUNDED.getWidth(fpsText, textSize);
        float pingW = Fonts.LEGION_ICONS.getWidth("P", textSize) + 2.0f + Fonts.UNBOUNDED.getWidth(pingText, textSize);
        float bpsW = Fonts.LEGION_ICONS.getWidth("e", textSize) + 2.0f + Fonts.UNBOUNDED.getWidth(bpsText, textSize);
        float tpsW = Fonts.LEGION_ICONS.getWidth("g", textSize) + 2.0f + Fonts.UNBOUNDED.getWidth(tpsText, textSize);
        float timeW = Fonts.LEGION_ICONS.getWidth("b", textSize) + 2.0f + Fonts.UNBOUNDED.getWidth(timeText, textSize);
        float contentW = iconW + 4.0f + gap;
        contentW += fpsW + pingW + bpsW + tpsW + timeW + 16.0f + gap * 4.0f;
        float sqW = contentW + 6.0f;
        float f = this.getX();
        float f2 = this.getY();
        class_4587 matrices = hudRenderEvent.j().method_51448();
        float f3 = this.b(matrices, f, f2);
        matrices.method_22903();
        float f4 = appear * this.bJ();
        matrices.method_46416(f + sqW / 2.0f, f2 + 8.0f, 0.0f);
        matrices.method_22905(f4, f4, 1.0f);
        matrices.method_46416(-(f + sqW / 2.0f), -(f2 + 8.0f), 0.0f);
        LegionHud.card(matrices, f, f2, sqW, 16.0f, 6.0f, alpha);
        Color textColor = LegionHud.withAlpha(LegionHud.ACCENT, alpha);
        Color textWhite = LegionHud.withAlpha(LegionHud.SOFT, alpha);
        Render2D.drawTexture(matrices, f + 4.0f, f2 + (16.0f - iconSize) / 2.0f, iconSize, iconSize, 0.0f, CLARITY_ICON, textColor);
        float curX = f + 2.0f + iconW + 4.0f + gap;
        LegionHud.text(matrices, Fonts.LEGION_ICONS, "q", curX, f2 + yCenter + 0.5f, textSize, textColor);
        LegionHud.text(matrices, Fonts.UNBOUNDED, fpsText, curX + Fonts.LEGION_ICONS.getWidth("q", textSize) + 2.0f, f2 + yCenter, textSize, textWhite);
        curX += fpsW + 4.0f + gap;
        LegionHud.text(matrices, Fonts.LEGION_ICONS, "P", curX - 1.0f, f2 + yCenter, textSize + 1.0f, textColor);
        LegionHud.text(matrices, Fonts.UNBOUNDED, pingText, curX + Fonts.LEGION_ICONS.getWidth("P", textSize) + 2.0f, f2 + yCenter, textSize, textWhite);
        curX += pingW + 4.0f + gap;
        LegionHud.text(matrices, Fonts.LEGION_ICONS, "e", curX, f2 + yCenter + 0.5f, textSize, textColor);
        LegionHud.text(matrices, Fonts.UNBOUNDED, bpsText, curX + Fonts.LEGION_ICONS.getWidth("e", textSize) + 2.0f, f2 + yCenter, textSize, textWhite);
        curX += bpsW + 4.0f + gap;
        LegionHud.text(matrices, Fonts.LEGION_ICONS, "g", curX, f2 + yCenter + 0.5f, textSize, textColor);
        LegionHud.text(matrices, Fonts.UNBOUNDED, tpsText, curX + Fonts.LEGION_ICONS.getWidth("g", textSize) + 2.0f, f2 + yCenter, textSize, textWhite);
        curX += tpsW + 4.0f + gap;
        LegionHud.text(matrices, Fonts.LEGION_ICONS, "b", curX, f2 + yCenter + 0.5f, textSize, textColor);
        LegionHud.text(matrices, Fonts.UNBOUNDED, timeText, curX + Fonts.LEGION_ICONS.getWidth("b", textSize) + 2.0f, f2 + yCenter, textSize, textWhite);
        matrices.method_22909();
        this.b(matrices);
        this.b(f, f2, sqW, 16.0f, f3);
        super.onRender2D(hudRenderEvent);
    }
}
