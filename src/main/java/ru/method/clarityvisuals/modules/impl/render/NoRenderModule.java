package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.TimedBooleanSetting;

public class NoRenderModule
extends Module {
    public BooleanSetting Fa = new TimedBooleanSetting("settings.norender.hurtcam", (Boolean)true, 2.0f);
    public BooleanSetting Fb = new TimedBooleanSetting("settings.norender.fire", (Boolean)true, 2.0f);
    public BooleanSetting wH = new TimedBooleanSetting("settings.norender.totem", (Boolean)true, 2.0f);
    public BooleanSetting Fc = new TimedBooleanSetting("settings.norender.scoreboard", (Boolean)false, 2.0f);
    public BooleanSetting Fd = new TimedBooleanSetting("settings.norender.bossbar", (Boolean)false, 2.0f);
    public BooleanSetting Fe = new TimedBooleanSetting("settings.norender.particles", (Boolean)true, 2.0f);
    public BooleanSetting Ff = new TimedBooleanSetting("settings.norender.armor", (Boolean)false, 2.0f);
    public BooleanSetting Fg = new TimedBooleanSetting("settings.norender.inventory_background", (Boolean)false, 2.0f);

    public NoRenderModule() {
        super("NoRender", ModuleCategory.ql);
        this.getSettings().add(this.Fa);
        this.getSettings().add(this.Fb);
        this.getSettings().add(this.wH);
        this.getSettings().add(this.Fc);
        this.getSettings().add(this.Fd);
        this.getSettings().add(this.Fe);
        this.getSettings().add(this.Ff);
        this.getSettings().add(this.Fg);
    }
}

