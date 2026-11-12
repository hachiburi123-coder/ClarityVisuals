package ru.method.clarityvisuals.api.setting;

import java.util.function.Supplier;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.api.setting.Setting;

public class BindSetting
extends Setting<KeyBind> {
    public BindSetting(String string, KeyBind keyBind) {
        super(string, keyBind);
    }

    public BindSetting(String string, KeyBind keyBind, Supplier<Boolean> supplier) {
        super(string, keyBind, supplier);
    }
}

