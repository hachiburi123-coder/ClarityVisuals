package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.Setting;

import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.SliderSetting;

public class AspectRatioModule
extends Module {
    public final SliderSetting yP = new SliderSetting("settings.aspectratio.ratio", 1.75f, 0.1f, 5.0f, 0.05f);

    public AspectRatioModule() {
        super("AspectRatio", ModuleCategory.ql);
    }
}

