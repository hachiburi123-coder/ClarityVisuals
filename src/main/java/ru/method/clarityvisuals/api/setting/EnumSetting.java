package ru.method.clarityvisuals.api.setting;

import java.util.function.Supplier;
import ru.method.clarityvisuals.api.setting.EnumCycler;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.Setting;

public class EnumSetting<Value extends Enum<Value>>
extends Setting<Value> {
    public EnumSetting(String string, Value Value) {
        super(string, Value);
    }

    public EnumSetting(String string, Value Value, Supplier<Boolean> supplier) {
        super(string, Value, supplier);
    }

    public void jY() {
        this.a(EnumCycler.b(this.Ke));
    }

    public String jZ() {
        return ((NamedOption)((Object)((Enum)this.Ke))).getName();
    }

    public void bS(String string) {
        Value[] values = this.Ke.getDeclaringClass().getEnumConstants();
        for (Value value : values) {
            String optionName = value instanceof NamedOption ? ((NamedOption)value).getName() : value.name();
            // Конфиги и старый GUI хранят локализованный ключ (getName),
            // а Legion-селектор передаёт имя константы (name).
            if (!optionName.equalsIgnoreCase(string) && !value.name().equalsIgnoreCase(string)) continue;
            this.a(value);
            break;
        }
    }
}
