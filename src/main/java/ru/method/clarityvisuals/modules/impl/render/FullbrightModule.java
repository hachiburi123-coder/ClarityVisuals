package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;

public class FullbrightModule
extends Module {
    private final EnumSetting<FullbrightMode> mode = new EnumSetting<FullbrightMode>("settings.fullbright.mode", FullbrightMode.GAMMA);
    private final SliderSetting brightness = new SliderSetting("settings.fullbright.brightness", 1.0f, 0.1f, 1.0f, 0.05f);

    public FullbrightModule() {
        super("Fullbright", ModuleCategory.ql);
        this.getSettings().add(this.mode);
        this.getSettings().add(this.brightness);
    }

    public FullbrightMode getMode() {
        return (FullbrightMode) this.mode.jT();
    }

    public float getBrightness() {
        return ((Float) this.brightness.jT()).floatValue();
    }

    public enum FullbrightMode implements NamedOption {
        GAMMA("settings.fullbright.mode.gamma"),
        NIGHT_VISION("settings.fullbright.mode.night_vision");

        private final String name;

        FullbrightMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
