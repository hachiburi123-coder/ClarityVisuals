package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.Setting;

import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.utils.render.MotionBlurRenderer;

public class MotionBlurModule
extends Module {
    private final SliderSetting EZ = new SliderSetting("settings.motionblur.strength", 50.0f, 5.0f, 90.0f, 5.0f);

    public MotionBlurModule() {
        super("MotionBlur", ModuleCategory.ql);
    }

    @Override
    public void onDisable() {
        MotionBlurRenderer.release();
        super.onDisable();
    }

    public void iH() {
        MotionBlurRenderer.render(((Float)this.EZ.jT()).floatValue() / 100.0f);
    }
}

