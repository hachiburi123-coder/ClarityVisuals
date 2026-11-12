package ru.method.clarityvisuals.ui;

import ru.method.clarityvisuals.module.Module;

import ru.method.clarityvisuals.module.ModuleCategory;

public enum ModuleTab {
    Tz(ModuleCategory.qj, "P", "Combat"),
    TA(ModuleCategory.qm, "V", "Player"),
    TB(ModuleCategory.qk, "S", "Move"),
    TC(ModuleCategory.ql, "R", "Visuals"),
    TD(ModuleCategory.qn, "V", "Other");
    private final ModuleCategory TE;
    private final String TF;
    private final String TG;

private ModuleTab(ModuleCategory moduleCategory, String string2, String string3) {
        this.TE = moduleCategory;
        this.TF = string2;
        this.TG = string3;
    }

    public ModuleCategory mM() {
        return this.TE;
    }

    public String mN() {
        return this.TF;
    }

    public String label() {
        return this.TG;
    }

    public static String labelFor(ModuleCategory moduleCategory) {
        if (moduleCategory == null) {
            return "";
        }
        for (ModuleTab moduleTab : ModuleTab.values()) {
            if (moduleTab.TE == moduleCategory) {
                return moduleTab.TG;
            }
        }
        if (moduleCategory == ModuleCategory.qo) {
            return "HUD";
        }
        return moduleCategory.fK();
    }

}

