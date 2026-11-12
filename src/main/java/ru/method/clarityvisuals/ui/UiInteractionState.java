package ru.method.clarityvisuals.ui;

import ru.method.clarityvisuals.module.Module;

import lombok.Generated;
import ru.method.clarityvisuals.ui.ClientTheme;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.ui.ModuleTab;
import ru.method.clarityvisuals.ui.UiThemeController;

public final class UiInteractionState {
    private ModuleCategory Tm = ModuleCategory.qj;
    private boolean Tn = false;

    public ClientTheme mF() {
        return UiThemeController.oX();
    }

    public void a(ClientTheme clientTheme) {
        UiThemeController.c(clientTheme);
    }

    public void a(ModuleTab moduleTab) {
        this.Tm = moduleTab.mM();
    }

    @Generated
    public ModuleCategory mG() {
        return this.Tm;
    }

    @Generated
    public boolean mH() {
        return this.Tn;
    }

    @Generated
    public void a(ModuleCategory moduleCategory) {
        this.Tm = moduleCategory;
    }

    @Generated
    public void y(boolean bl) {
        this.Tn = bl;
    }
}

