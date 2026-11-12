package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;

public class SuperResolutionModule
extends Module {
    private final EnumSetting<SuperResolutionPreset> preset = new EnumSetting<SuperResolutionPreset>("settings.superresolution.preset", SuperResolutionPreset.QUALITY);
    private final SliderSetting customScale = new SliderSetting("settings.superresolution.scale", 0.70f, 0.25f, 1.0f, 0.05f, () -> this.preset.jT() == SuperResolutionPreset.CUSTOM);
    private final SliderSetting sharpness = new SliderSetting("settings.superresolution.sharpness", 0.65f, 0.0f, 1.0f, 0.05f);
    private final EnumSetting<UpscaleFilter> filter = new EnumSetting<UpscaleFilter>("settings.superresolution.filter", UpscaleFilter.CAS);

    public SuperResolutionModule() {
        super("SuperResolution", ModuleCategory.ql);
        this.getSettings().add(this.preset);
        this.getSettings().add(this.customScale);
        this.getSettings().add(this.sharpness);
        this.getSettings().add(this.filter);
    }

    public float getRenderScale() {
        if (!this.isToggled()) {
            return 1.0f;
        }
        return switch ((SuperResolutionPreset) this.preset.jT()) {
            case ULTRA_QUALITY -> 0.85f;
            case QUALITY -> 0.77f;
            case BALANCED -> 0.67f;
            case PERFORMANCE -> 0.50f;
            case ULTRA_PERFORMANCE -> 0.33f;
            case CUSTOM -> ((Float) this.customScale.jT()).floatValue();
        };
    }

    public float getSharpness() {
        return ((Float) this.sharpness.jT()).floatValue();
    }

    public int getFilterMode() {
        return this.filter.jT() == UpscaleFilter.CAS ? 0 : 1;
    }

    public enum SuperResolutionPreset implements NamedOption {
        ULTRA_QUALITY("settings.superresolution.preset.ultra_quality"),
        QUALITY("settings.superresolution.preset.quality"),
        BALANCED("settings.superresolution.preset.balanced"),
        PERFORMANCE("settings.superresolution.preset.performance"),
        ULTRA_PERFORMANCE("settings.superresolution.preset.ultra_performance"),
        CUSTOM("settings.superresolution.preset.custom");

        private final String name;

        SuperResolutionPreset(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    public enum UpscaleFilter implements NamedOption {
        CAS("CAS (AMD FSR)"),
        BILINEAR("Bilinear");

        private final String name;

        UpscaleFilter(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
