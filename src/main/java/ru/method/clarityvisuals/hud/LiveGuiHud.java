package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.api.events.impl.EventMouse;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.modules.impl.render.FreeLookModule;
import ru.method.clarityvisuals.modules.impl.render.FullbrightModule;
import ru.method.clarityvisuals.modules.impl.render.SuperResolutionModule;
import ru.method.clarityvisuals.modules.impl.render.ZoomModule;
import ru.method.clarityvisuals.modules.impl.movement.SprintModule;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.util.Stopwatch;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class LiveGuiHud
extends HudElement {
    private final EnumSetting<LiveGuiStyle> style = new EnumSetting<LiveGuiStyle>("settings.livegui.style", LiveGuiStyle.PILL);
    private final BooleanSetting showFps = new BooleanSetting("settings.livegui.show_fps", true);
    private final BooleanSetting showActiveCount = new BooleanSetting("settings.livegui.show_active", true);
    private final SliderSetting chipSpacing = new SliderSetting("settings.livegui.spacing", 4.0f, 2.0f, 10.0f, 1.0f);

    private final AnimatedComponent animAlpha = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent animWidth = new AnimatedComponent(Easing.aiZ);

    private static final class QuickModule {
        final String label;
        final Class<? extends Module> moduleClass;
        float x, y, width, height;

        QuickModule(String label, Class<? extends Module> moduleClass) {
            this.label = label;
            this.moduleClass = moduleClass;
        }
    }

    private final List<QuickModule> quickModules = new ArrayList<>();

    public LiveGuiHud() {
        super("LiveGUI");
        this.getSettings().add(this.style);
        this.getSettings().add(this.showFps);
        this.getSettings().add(this.showActiveCount);
        this.getSettings().add(this.chipSpacing);

        quickModules.add(new QuickModule("Fullbright", FullbrightModule.class));
        quickModules.add(new QuickModule("Zoom", ZoomModule.class));
        quickModules.add(new QuickModule("Freelook", FreeLookModule.class));
        quickModules.add(new QuickModule("SuperRes", SuperResolutionModule.class));
        quickModules.add(new QuickModule("Sprint", SprintModule.class));
    }

    private boolean shouldRender() {
        return HudEditorScreen.mb() || (aiG.field_1724 != null && aiG.field_1687 != null);
    }

    @Override
    public void onRender2D(HudRenderEvent event) {
        if (LiveGuiHud.fullNullCheck() || this.bL()) {
            return;
        }

        boolean visible = shouldRender();
        float alpha = this.animAlpha.a(visible ? 1.0f : 0.0f, 220L);
        if (alpha <= 0.01f) {
            return;
        }

        float x = this.getX();
        float y = this.getY();
        int alphaInt = Math.max(0, Math.min(255, Math.round(255.0f * alpha)));

        float pad = 6.0f;
        float height = 22.0f;
        float spacing = ((Float) this.chipSpacing.jT()).floatValue();

        // Calculate dynamic width
        float brandWidth = Fonts.ROUNDBOLD.getWidth("Clarity Visuals", 7.5f) + 10.0f;
        float statsWidth = 0.0f;

        if (this.showActiveCount.jT()) {
            long activeCount = ClarityVisuals.getInstance().getModuleManager().getModules().stream().filter(Module::isToggled).count();
            statsWidth += Fonts.SEMIBOLD.getWidth(activeCount + " ON", 7.0f) + 8.0f;
        }
        if (this.showFps.jT()) {
            statsWidth += Fonts.SEMIBOLD.getWidth(Stopwatch.rT() + " FPS", 7.0f) + 8.0f;
        }

        float chipsTotalWidth = 0.0f;
        for (QuickModule qm : quickModules) {
            float w = Fonts.SEMIBOLD.getWidth(qm.label, 7.0f) + 14.0f;
            chipsTotalWidth += w + spacing;
        }

        float totalWidth = pad * 2.0f + brandWidth + statsWidth + chipsTotalWidth;
        float curWidth = this.animWidth.a(totalWidth, 180L);
        if (HudEditorScreen.mb()) {
            curWidth = totalWidth;
        }

        float scale = this.gn.rk() * alpha * this.bJ();
        float[] offsets = this.bK();
        float cx = x + curWidth / 2.0f + offsets[0];
        float cy = y + height / 2.0f + offsets[1];

        event.j().method_51448().method_22903();
        event.j().method_51448().method_46416(cx, cy, 0.0f);
        event.j().method_51448().method_22905(scale, scale, 1.0f);
        event.j().method_51448().method_46416(-cx, -cy, 0.0f);

        // Draw background
        if (HudThemeRenderer.active()) {
            HudThemeRenderer.b(event.j().method_51448(), x, y, curWidth, height, 6.0f, alpha);
        } else {
            Color bg = new Color(20, 24, 30, Math.round(210.0f * alpha));
            Render2D.drawLiquidRectHud(event.j().method_51448(), x, y, curWidth, height, new BorderRadius(6.0f), new ColorRGBA(bg), 2.5f, 22.0f, 11.0f, 175.0f, true, 0.0f, 0.19f);
        }

        // Render brand badge
        float curX = x + pad;
        Color accent = HudThemeRenderer.accent(alphaInt);
        Render2D.drawRoundedRect(event.j().method_51448(), curX, y + 3.0f, brandWidth - 4.0f, height - 6.0f, 4.0f, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Math.round(50.0f * alpha)));
        Render2D.drawFont(event.j().method_51448(), Fonts.ROUNDBOLD.getFont(7.5f), "Clarity", curX + 4.0f, y + 6.5f, HudThemeRenderer.d(alphaInt));
        curX += brandWidth + 2.0f;

        // Render stats if enabled
        if (this.showActiveCount.jT()) {
            long activeCount = ClarityVisuals.getInstance().getModuleManager().getModules().stream().filter(Module::isToggled).count();
            String actText = activeCount + " ON";
            float tw = Fonts.SEMIBOLD.getWidth(actText, 7.0f) + 6.0f;
            Render2D.drawRoundedRect(event.j().method_51448(), curX, y + 3.5f, tw, height - 7.0f, 4.0f, new Color(50, 200, 100, Math.round(40.0f * alpha)));
            Render2D.drawFont(event.j().method_51448(), Fonts.SEMIBOLD.getFont(7.0f), actText, curX + 3.0f, y + 7.0f, new Color(90, 235, 140, alphaInt));
            curX += tw + 4.0f;
        }

        if (this.showFps.jT()) {
            String fpsText = Stopwatch.rT() + " FPS";
            float tw = Fonts.SEMIBOLD.getWidth(fpsText, 7.0f) + 6.0f;
            Render2D.drawRoundedRect(event.j().method_51448(), curX, y + 3.5f, tw, height - 7.0f, 4.0f, new Color(255, 255, 255, Math.round(20.0f * alpha)));
            Render2D.drawFont(event.j().method_51448(), Fonts.SEMIBOLD.getFont(7.0f), fpsText, curX + 3.0f, y + 7.0f, HudThemeRenderer.e(alphaInt));
            curX += tw + 4.0f;
        }

        // Render interactive chips
        for (QuickModule qm : quickModules) {
            Module mod = ClarityVisuals.getInstance().getModuleManager().getModule(qm.moduleClass);
            boolean toggled = mod != null && mod.isToggled();

            float chipW = Fonts.SEMIBOLD.getWidth(qm.label, 7.0f) + 14.0f;
            float chipH = height - 6.0f;
            float chipY = y + 3.0f;

            qm.x = curX;
            qm.y = chipY;
            qm.width = chipW;
            qm.height = chipH;

            Color chipBg = toggled
                ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), Math.round(75.0f * alpha))
                : new Color(40, 45, 55, Math.round(80.0f * alpha));

            Render2D.drawRoundedRect(event.j().method_51448(), curX, chipY, chipW, chipH, 4.0f, chipBg);

            // Active dot
            Color dotColor = toggled ? new Color(70, 240, 130, alphaInt) : new Color(120, 125, 135, Math.round(150.0f * alpha));
            Render2D.drawCircle(event.j().method_51448(), curX + 6.0f, chipY + chipH / 2.0f, 2.0f, dotColor);

            // Label
            Color textCol = toggled ? Color.WHITE : new Color(180, 185, 195, alphaInt);
            Render2D.drawFont(event.j().method_51448(), Fonts.SEMIBOLD.getFont(7.0f), qm.label, curX + 11.0f, chipY + 4.0f, textCol);

            curX += chipW + spacing;
        }

        event.j().method_51448().method_22909();
        this.b(x, y, curWidth, height, 0.0f);
        super.onRender2D(event);
    }

    public boolean handleMouseClick(double mx, double my, int button) {
        if (!this.isToggled() || button != 0) {
            return false;
        }
        for (QuickModule qm : quickModules) {
            if (mx >= qm.x && mx <= qm.x + qm.width && my >= qm.y && my <= qm.y + qm.height) {
                Module mod = ClarityVisuals.getInstance().getModuleManager().getModule(qm.moduleClass);
                if (mod != null) {
                    mod.toggle();
                    return true;
                }
            }
        }
        return false;
    }

    public enum LiveGuiStyle implements NamedOption {
        PILL("Pill"),
        DASHBOARD("Dashboard"),
        COMPACT("Compact");

        private final String name;

        LiveGuiStyle(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
