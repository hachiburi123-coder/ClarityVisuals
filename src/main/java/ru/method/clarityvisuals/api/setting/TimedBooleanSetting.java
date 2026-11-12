package ru.method.clarityvisuals.api.setting;

import ru.method.clarityvisuals.api.setting.BooleanSetting;

public class TimedBooleanSetting
extends BooleanSetting {
    private final float Kl;

    public TimedBooleanSetting(String string, Boolean bl, float f) {
        super(string, bl);
        this.Kl = f;
    }

    public float ke() {
        return this.Kl;
    }
}

