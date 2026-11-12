package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;

public class SaturationModule
extends Module {
    private final SliderSetting strength = new SliderSetting("modules.settings.saturation.strength", 1.0f, 0.0f, 2.0f, 0.01f);

    public SaturationModule() {
        super("Saturation", ModuleCategory.ql);
        this.getSettings().add(this.strength);
    }

    public float getStrength() {
        return ((Float)this.strength.jT()).floatValue();
    }
}
