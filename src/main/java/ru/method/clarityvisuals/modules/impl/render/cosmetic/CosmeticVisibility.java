package ru.method.clarityvisuals.modules.impl.render.cosmetic;

import ru.method.clarityvisuals.api.setting.Setting;

import ru.method.clarityvisuals.api.setting.NamedOption;

public enum CosmeticVisibility
implements NamedOption  {
    dw("settings.cosmetics.visibility.everyone"),
    dx("settings.cosmetics.visibility.friends"),
    dy("settings.cosmetics.visibility.party"),
    dz("settings.cosmetics.visibility.self");
    private final String dA;

private CosmeticVisibility(String string2) {
        this.dA = string2;
    }

    @Override
    public String getName() {
        return this.dA;
    }

}

