package ru.method.clarityvisuals.input;

import net.minecraft.class_310;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.manager.ClickRippleManager;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.ui.ModuleCard;
import ru.method.clarityvisuals.ui.ModuleSettingsPanel;
import ru.method.clarityvisuals.modules.impl.client.UI;
import ru.method.clarityvisuals.screen.clickgui.legion.LegionClickGui;

public final class ClickGuiKeyHandler {
    public static final int qw = 344;
    private static final int qx = 54;
    private static final long qy = 120000000L;
    private static long qz;

    private ClickGuiKeyHandler() {
    }

    public static boolean b(int n, int n2, int n3) {
        if (n3 != 1 || n < 0) {
            return false;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null || clarityVisuals.isPanic() || clarityVisuals.getModuleManager() == null) {
            return false;
        }
        UI uI = clarityVisuals.getModuleManager().getModule(UI.class);
        if (uI == null || !ClickGuiKeyHandler.a(uI, n, n2) || !ClickGuiKeyHandler.fN()) {
            return false;
        }
        if (ClickGuiKeyHandler.a(clarityVisuals)) {
            return false;
        }
        long l = System.nanoTime();
        if (l - qz < 120000000L) {
            return true;
        }
        qz = l;
        uI.toggle();
        return true;
    }

    public static boolean a(UI uI, int n) {
        return ClickGuiKeyHandler.a(uI, n, -1);
    }

    public static boolean a(UI uI, int n, int n2) {
        KeyBind keyBind = uI.getBind();
        return keyBind != null && keyBind.af(n);
    }

    private static boolean a(ClarityVisuals clarityVisuals) {
        if (ModuleCard.ov()) {
            return true;
        }
        for (ModuleSettingsPanel moduleSettingsPanel : ClickRippleManager.nW()) {
            if (!moduleSettingsPanel.hasActiveBinding()) continue;
            return true;
        }
        LegionClickGui clickGui = clarityVisuals.getClickGui();
        return clickGui != null && clickGui.isDisplayed() && clickGui.hasActiveBinding();
    }

    public static boolean fN() {
        if (!UI.isInPlayWorld()) {
            return false;
        }
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return false;
        }
        LegionClickGui clickGui = clarityVisuals.getClickGui();
        if (clickGui == null) {
            return false;
        }
        class_310 class_3102 = class_310.method_1551();
        return class_3102.field_1755 == null || class_3102.field_1755 == clickGui;
    }
}

