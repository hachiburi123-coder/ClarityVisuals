package ru.method.clarityvisuals.modules.impl.client;

import ru.method.clarityvisuals.api.setting.Setting;

import java.awt.Color;
import net.minecraft.class_3532;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ButtonSetting;
import ru.method.clarityvisuals.ui.ClientTheme;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.hud.HudManager;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.hud.ScoreboardHud;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.screen.clickgui.legion.LegionClickGui;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.RenderFrameTracker;

public class UI
extends Module {
    private static final float MIN_SCALE = 0.75f;
    private static final float MAX_SCALE = 1.5f;
    private static final float SCALE_SMOOTH_SPEED = 10.0f;
    private static float appliedInterfaceScale = 1.0f;
    private static float currentInterfaceScale = 1.0f;
    static float guiTransparency = 1.0f;
    private static long lastScaleUpdateNs = System.nanoTime();
    private static long lastScaleAnimatedFrameId = -1L;
    private final ColorSetting primaryColor = new ColorSetting("settings.ui.primary_color", ColorUtils.getPrimaryColor()){

        @Override
        public void d(Color color) {
            super.d(color);
            ColorUtils.setPrimaryColor((Color)this.jT());
        }
    };
    private final ColorSetting secondaryColor = new ColorSetting("settings.ui.secondary_color", ColorUtils.getSecondaryColor()){

        @Override
        public void d(Color color) {
            super.d(color);
            ColorUtils.setSecondaryColor((Color)this.jT());
        }
    };
    private final ColorSetting backgroundColor = new ColorSetting("settings.ui.background_color", ColorUtils.getBackgroundColor()){

        @Override
        public void d(Color color) {
            super.d(color);
            ColorUtils.setBackgroundColor((Color)this.jT());
        }
    };
    private final ColorSetting textColor = new ColorSetting("settings.ui.text_color", ColorUtils.getTextColor()){

        @Override
        public void d(Color color) {
            super.d(color);
            ColorUtils.setTextColor((Color)this.jT());
        }
    };
    private final BooleanSetting fade = new BooleanSetting("settings.ui.fade", true){

        @Override
        public void a(Boolean bl) {
            super.a(bl);
            ColorUtils.setThemeFadeEnabled((Boolean)this.jT());
        }
    };
    private final BooleanSetting blur = new BooleanSetting("settings.ui.blur", true){

        @Override
        public void a(Boolean bl) {
            super.a(bl);
            ColorUtils.setBlurEnabled((Boolean)this.jT());
        }
    };
    private final BooleanSetting customScoreboard = new BooleanSetting("settings.ui.scoreboard.custom", false){

        @Override
        public void a(Boolean bl) {
            super.a(bl);
            UI.syncCustomScoreboardHud((Boolean)this.jT());
        }
    };
    private final SliderSetting scoreboardRadius = new SliderSetting("settings.ui.scoreboard.radius", 9.0f, 0.0f, 18.0f, 0.5f, this.customScoreboard::jT);
    private final ColorSetting scoreboardBackground = new ColorSetting("settings.ui.scoreboard.background", new Color(18, 20, 26, 185), this.customScoreboard::jT);
    private final BooleanSetting customTab = new BooleanSetting("settings.ui.tab.custom", false);
    private final ColorSetting customTabColor = new ColorSetting("settings.ui.tab.color", new Color(15, 17, 22, 205), this.customTab::jT);
    private final SliderSetting transparency = new SliderSetting("settings.ui.transparency", 100.0f, 30.0f, 100.0f, 1.0f, () -> false){

        @Override
        public void a(Float f) {
            super.a(f);
            guiTransparency = class_3532.method_15363((float)(((Float)this.jT()).floatValue() / 100.0f), (float)0.3f, (float)1.0f);
        }
    };
    private final SliderSetting scale = new SliderSetting("settings.ui.scale", 1.0f, 0.5f, 1.5f, 0.01f){

        @Override
        public void a(Float f) {
            super.a(Float.valueOf(class_3532.method_15363((float)f.floatValue(), (float)0.5f, (float)1.5f)));
            ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
            if (clarityVisuals != null && clarityVisuals.isConfigLoading()) {
                UI.snapInterfaceScale(((Float)this.jT()).floatValue());
            }
        }

        @Override
        public void reset() {
            this.a((Float)this.jU());
        }
    };

    private final ButtonSetting hudEditor = new ButtonSetting("settings.ui.hud_editor", "settings.ui.hud_editor.open", () -> {
        if (!Module.fullNullCheck() && aiG != null) {
            aiG.method_1507(new HudEditorScreen());
        }
    });

    public UI() {
        super("UI", ModuleCategory.qn);
        this.setBind(new KeyBind(344, false));
        this.getSettings().add(this.primaryColor);
        this.getSettings().add(this.secondaryColor);
        this.getSettings().add(this.backgroundColor);
        this.getSettings().add(this.textColor);
        this.getSettings().add(this.fade);
        this.getSettings().add(this.blur);
        this.getSettings().add(this.customScoreboard);
        this.getSettings().add(this.scoreboardRadius);
        this.getSettings().add(this.scoreboardBackground);
        this.getSettings().add(this.customTab);
        this.getSettings().add(this.customTabColor);
        this.getSettings().add(this.transparency);
        this.getSettings().add(this.hudEditor);
        this.getSettings().add(this.scale);
        ColorUtils.setPrimaryColor((Color)this.primaryColor.jT());
        ColorUtils.setSecondaryColor((Color)this.secondaryColor.jT());
        ColorUtils.setBackgroundColor((Color)this.backgroundColor.jT());
        ColorUtils.setTextColor((Color)this.textColor.jT());
        ColorUtils.setThemeFadeEnabled((Boolean)this.fade.jT());
        ColorUtils.setBlurEnabled((Boolean)this.blur.jT());
        guiTransparency = class_3532.method_15363((float)(((Float)this.transparency.jT()).floatValue() / 100.0f), (float)0.3f, (float)1.0f);
        UI.snapInterfaceScale(((Float)this.scale.jT()).floatValue());
    }

    public void ensureDefaultBind() {
        this.setBind(new KeyBind(344, false));
    }

    public static HudThemeRenderer.ThemeMetrics getHudVariant() {
        return HudThemeRenderer.ThemeMetrics.gD;
    }

    public static HudThemeRenderer.ThemePalette getHudShape() {
        return HudThemeRenderer.ThemePalette.gu;
    }

    public void syncFromLinkTheme(ClientTheme clientTheme) {
        if (clientTheme == null) {
            return;
        }
        this.primaryColor.d(clientTheme.pe());
        this.secondaryColor.d(clientTheme.ph());
        this.backgroundColor.d(clientTheme.pi());
        this.textColor.d(clientTheme.pg());
    }

    private void openActiveGui(boolean bl) {
        LegionClickGui clickGui = ClarityVisuals.getInstance().getClickGui();
        if (bl) {
            if (!UI.isInPlayWorld()) {
                return;
            }
            clickGui.open();
            if (clickGui.isDisplayed()) {
                this.toggled = true;
            }
        } else {
            clickGui.beginClose();
            this.toggled = false;
        }
    }

    @Override
    public void toggle() {
        if (!UI.isInPlayWorld()) {
            return;
        }
        LegionClickGui clickGui = ClarityVisuals.getInstance().getClickGui();
        if (clickGui.isOpen()) {
            this.setToggled(false);
        } else if (clickGui.isDisplayed()) {
            clickGui.open();
            this.toggled = true;
        } else {
            this.setToggled(true);
        }
    }

    @Override
    public void setToggled(boolean bl) {
        this.openActiveGui(bl);
    }

    public void syncClosedState() {
        this.toggled = false;
    }

    public static boolean isInPlayWorld() {
        return UI.aiG.field_1724 != null && UI.aiG.field_1687 != null;
    }

    public static void previewInterfaceScale(float f) {
        UI.setAppliedScale(f, false);
    }

    public static float getInterfaceScale() {
        UI.updateScaleAnimation();
        return currentInterfaceScale;
    }

    public static float peekInterfaceScale() {
        return currentInterfaceScale;
    }

    public static float getGuiTransparency() {
        return guiTransparency;
    }

    public static boolean isCustomScoreboardEnabled() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return false;
        }
        UI uI = clarityVisuals.getModuleManager().getModule(UI.class);
        return uI != null && (Boolean)uI.customScoreboard.jT() != false;
    }

    public static boolean isCustomTabEnabled() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return false;
        }
        UI uI = clarityVisuals.getModuleManager().getModule(UI.class);
        return uI != null && (Boolean)uI.customTab.jT() != false;
    }

    public static Color getCustomTabColor() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return new Color(15, 17, 22, 205);
        }
        UI uI = clarityVisuals.getModuleManager().getModule(UI.class);
        return uI == null ? new Color(15, 17, 22, 205) : (Color)uI.customTabColor.jT();
    }

    public static float getScoreboardRadius() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return 9.0f;
        }
        UI uI = clarityVisuals.getModuleManager().getModule(UI.class);
        return uI == null ? 9.0f : ((Float)uI.scoreboardRadius.jT()).floatValue();
    }

    public static void setScoreboardRadius(float f) {
        UI uI = UI.module();
        if (uI != null) {
            uI.scoreboardRadius.a(Float.valueOf(class_3532.method_15363((float)f, (float)0.0f, (float)18.0f)));
        }
    }

    public static Color getScoreboardBackgroundColor() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return new Color(18, 20, 26, 185);
        }
        UI uI = clarityVisuals.getModuleManager().getModule(UI.class);
        return uI == null ? new Color(18, 20, 26, 185) : (Color)uI.scoreboardBackground.jT();
    }

    public static void setScoreboardBackgroundAlpha(int n) {
        UI uI = UI.module();
        if (uI == null) {
            return;
        }
        Color color = (Color)uI.scoreboardBackground.jT();
        uI.scoreboardBackground.d(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, n))));
    }

    private static UI module() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return null;
        }
        return clarityVisuals.getModuleManager().getModule(UI.class);
    }

    public void applyScaleSetting() {
        UI.setAppliedScale(((Float)this.scale.jT()).floatValue(), false);
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals != null && !clarityVisuals.isConfigLoading()) {
            clarityVisuals.getConfigManager().dU();
        }
    }

    public void resetScaleSetting() {
        this.scale.reset();
        UI.setAppliedScale(((Float)this.scale.jT()).floatValue(), false);
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals != null && !clarityVisuals.isConfigLoading()) {
            clarityVisuals.getConfigManager().dU();
        }
    }

    static void snapInterfaceScale(float f) {
        UI.setAppliedScale(f, true);
    }

    private static void setAppliedScale(float f, boolean bl) {
        float f2;
        appliedInterfaceScale = f2 = class_3532.method_15363((float)f, (float)0.5f, (float)1.5f);
        if (bl) {
            currentInterfaceScale = f2;
        }
        lastScaleUpdateNs = System.nanoTime();
    }

    private static void updateScaleAnimation() {
        long l = RenderFrameTracker.getFrameId();
        if (l == lastScaleAnimatedFrameId) {
            return;
        }
        lastScaleAnimatedFrameId = l;
        long l2 = System.nanoTime();
        double d = Math.max(0.0, (double)(l2 - lastScaleUpdateNs) / 1.0E9);
        lastScaleUpdateNs = l2;
        if (Math.abs(currentInterfaceScale - appliedInterfaceScale) <= 5.0E-4f) {
            currentInterfaceScale = appliedInterfaceScale;
            return;
        }
        float f = (float)(1.0 - Math.exp(-10.0 * d));
        currentInterfaceScale = class_3532.method_16439((float)class_3532.method_15363((float)f, (float)0.0f, (float)1.0f), (float)currentInterfaceScale, (float)appliedInterfaceScale);
    }

    static void syncCustomScoreboardHud(boolean bl) {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getHudManager() == null) {
            return;
        }
        if (aiG == null || aiG.method_22683() == null) {
            return;
        }
        HudManager hudManager = clarityVisuals.getHudManager();
        ScoreboardHud scoreboardHud = hudManager.a(ScoreboardHud.class);
        if (scoreboardHud == null) {
            return;
        }
        if (scoreboardHud.isToggled() != bl) {
            hudManager.a(scoreboardHud, bl, Math.max(0.0f, (float)aiG.method_22683().method_4486() - 150.0f), 40.0f);
        }
    }
}
