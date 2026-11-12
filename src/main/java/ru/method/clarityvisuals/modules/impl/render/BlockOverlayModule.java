package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;

public class BlockOverlayModule
extends Module {
    public final BooleanSetting yQ = new BooleanSetting("settings.blockoverlay.fill", true);
    public final EnumSetting<FillType> fillType = new EnumSetting<FillType>("settings.blockoverlay.fill_type", FillType.SHADER, () -> (Boolean) this.yQ.jT());
    public final EnumSetting<OverlayShape> yS = new EnumSetting<OverlayShape>("settings.blockoverlay.fill_mode", OverlayShape.NEBULA, () -> (Boolean) this.yQ.jT() && this.fillType.jT() == FillType.SHADER);
    public final SliderSetting yT = new SliderSetting("settings.blockoverlay.fill_alpha", 70.0f, 0.0f, 255.0f, 1.0f, () -> (Boolean) this.yQ.jT() && this.fillType.jT() == FillType.NORMAL);
    public final SliderSetting shaderAlpha = new SliderSetting("settings.blockoverlay.shader_alpha", 1.0f, 0.1f, 1.0f, 0.05f, () -> (Boolean) this.yQ.jT() && this.fillType.jT() == FillType.SHADER);
    public final SliderSetting yV = new SliderSetting("modules.settings.blockoverlay.shader_speed", 1.0f, 0.1f, 3.0f, 0.1f, () -> (Boolean) this.yQ.jT() != false && this.fillType.jT() == FillType.SHADER);
    public final EnumSetting<OutlineAnimation> animation = new EnumSetting<OutlineAnimation>("settings.blockoverlay.animation", OutlineAnimation.NONE);
    public final BooleanSetting showOutline = new BooleanSetting("settings.blockoverlay.outline", true);
    public final SliderSetting lineWidth = new SliderSetting("settings.blockoverlay.line_width", 2.0f, 0.5f, 5.0f, 0.1f, () -> (Boolean) this.showOutline.jT());
    public final SliderSetting yU = new SliderSetting("settings.blockoverlay.outline_alpha", 255.0f, 0.0f, 255.0f, 1.0f, () -> (Boolean) this.showOutline.jT());
    public final BooleanSetting clientColor = new BooleanSetting("settings.blockoverlay.client_color", true);
    public final ColorSetting customColor = new ColorSetting("settings.blockoverlay.custom_color", new Color(255, 255, 255, 255), () -> !(Boolean) this.clientColor.jT());

    public BlockOverlayModule() {
        super("BlockOverlay", ModuleCategory.ql);
    }

    public static enum FillType
    implements NamedOption {
        NORMAL("settings.blockoverlay.fill_type.normal"),
        SHADER("settings.blockoverlay.fill_type.shader");
        private final String name;

        private FillType(String string2) {
            this.name = string2;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public static enum OverlayShape
    implements NamedOption  {
        NEBULA("settings.blockoverlay.fill_mode.nebula"),
        STARS("settings.blockoverlay.fill_mode.stars"),
        WEB("settings.blockoverlay.fill_mode.web"),
        PLASMA("settings.blockoverlay.fill_mode.plasma"),
        AURORA("settings.blockoverlay.fill_mode.aurora"),
        NEON("settings.blockoverlay.fill_mode.neon"),
        EMERALD("settings.blockoverlay.fill_mode.emerald"),
        RAINBOW("settings.blockoverlay.fill_mode.rainbow"),
        OCEAN("settings.blockoverlay.fill_mode.ocean"),
        ICE("settings.blockoverlay.fill_mode.ice"),
        CANDY("settings.blockoverlay.fill_mode.candy");
        private final String zf;

        private OverlayShape(String string2) {
            this.zf = string2;
        }

        @Override
        public String getName() {
            return this.zf;
        }
    }

    public static enum OutlineAnimation
    implements NamedOption {
        NONE("settings.blockoverlay.animation.none"),
        PULSE("settings.blockoverlay.animation.pulse"),
        WAVE("settings.blockoverlay.animation.wave");
        private final String name;

        private OutlineAnimation(String string2) {
            this.name = string2;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
