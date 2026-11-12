package ru.method.clarityvisuals.modules.impl.client;

import net.minecraft.class_2960;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;

public class AllSkinsModule
extends Module {
    public static final class_2960 yO = ClarityVisuals.id("textures/allskins/rudi.png");

    public AllSkinsModule() {
        super("AllSkins", ModuleCategory.qn);
    }

    public static class_2960 it() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.getModuleManager() == null) {
            return null;
        }
        AllSkinsModule allSkinsModule = clarityVisuals.getModuleManager().getModule(AllSkinsModule.class);
        return allSkinsModule != null && allSkinsModule.isToggle() ? yO : null;
    }
}

