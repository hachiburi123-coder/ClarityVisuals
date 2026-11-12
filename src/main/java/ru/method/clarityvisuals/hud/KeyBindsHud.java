package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.animation.Animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1074;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.managers.system.ModuleManager;
import ru.method.clarityvisuals.modules.impl.client.UI;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Font;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

public class KeyBindsHud
extends HudElement {
    private final AnimatedComponent jI = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent jJ = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent jK = new AnimatedComponent(Easing.aiZ);
    private List<KeyBindEntry> jL;
    private long cacheExpiresAtMs;
    private boolean jt;

    public KeyBindsHud() {
        super("KeyBindsHud");
    }

    private List<KeyBindEntry> h(boolean bl) {
        ArrayList<KeyBindEntry> arrayList = new ArrayList<KeyBindEntry>();
        ModuleManager moduleManager = ClarityVisuals.getInstance().getModuleManager();
        if (moduleManager != null) {
            for (Module module : moduleManager.getModules()) {
                KeyBind keyBind = module.getBind();
                if (module instanceof UI || module.isServerBlocked() || keyBind == null || keyBind.isEmpty() || !module.isToggled()) continue;
                arrayList.add(new KeyBindEntry(module.getDisplayName(), keyBind.toString(), true));
            }
        }
        if (bl && arrayList.isEmpty()) {
            arrayList.add(new KeyBindEntry("Sprint", "R", true));
            arrayList.add(new KeyBindEntry("Fullbright", "F", true));
            arrayList.add(new KeyBindEntry("DiscordRPC", "G", true));
        }
        return arrayList;
    }

    private boolean a(List<KeyBindEntry> list) {
        if (HudEditorScreen.mb()) {
            return true;
        }
        return !list.isEmpty();
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        float f;
        float f2;
        float f3;
        List<KeyBindEntry> list;
        boolean bl;
        float f4;
        if (KeyBindsHud.fullNullCheck() || this.bL()) {
            return;
        }
        boolean bl2 = HudEditorScreen.mb();
        long l = System.currentTimeMillis();
        if (this.jL == null || bl2 != this.jt || l >= this.cacheExpiresAtMs) {
            this.jL = this.h(bl2);
            this.jt = bl2;
            this.cacheExpiresAtMs = l + 200L;
        }
        if ((f4 = this.jK.a((bl = this.a(list = this.jL)) ? 1.0f : 0.0f, 200L)) <= 0.0f) {
            return;
        }
        float f5 = this.getX();
        float f6 = this.getY();
        float targetWidth = 75.0f;
        for (KeyBindEntry row : list) {
            float nameW = Fonts.UNBOUNDED.getWidth(row.name(), 6.0f);
            float bindW = Fonts.UNBOUNDED.getWidth(row.cH(), 6.0f);
            targetWidth = Math.max(nameW + bindW + 50.0f, targetWidth);
        }
        float targetHeight = 16.0f + (float)list.size() * 12.0f + 4.0f;
        float f17 = this.jI.a(targetWidth, 200L);
        float f18 = this.jJ.a(targetHeight, 200L);
        if (bl2) {
            f17 = targetWidth;
            f18 = targetHeight;
        }
        float f19 = this.bI();
        float f20 = aiG.method_22683().method_4486();
        float f21 = aiG.method_22683().method_4502();
        f5 = Math.max(0.0f, Math.min(f5, Math.max(0.0f, f20 - f17 * f19)));
        f6 = Math.max(0.0f, Math.min(f6, Math.max(0.0f, f21 - f18 * f19)));
        int a = Math.max(0, Math.min(255, Math.round(255.0f * f4)));
        this.b(hudRenderEvent.j().method_51448(), f5, f6);
        hudRenderEvent.j().method_51448().method_22903();
        float[] fArray = this.bK();
        float f22 = this.gn.rk() * f4 * this.bJ();
        float f23 = f5 + f17 / 2.0f + fArray[0];
        float f24 = f6 + f18 / 2.0f + fArray[1];
        hudRenderEvent.j().method_51448().method_46416(f23, f24, 0.0f);
        hudRenderEvent.j().method_51448().method_22905(f22, f22, 1.0f);
        hudRenderEvent.j().method_51448().method_46416(-f23, -f24, 0.0f);
        LegionHud.card(hudRenderEvent.j().method_51448(), f5, f6, f17, f18, 8.0f, a);
        Render2D.startMatrixScissor(hudRenderEvent.j(), f5, f6, f17, f18);
        float headerIconW = Fonts.LEGION_ICONS.getWidth("Q", 8.0f);
        LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.LEGION_ICONS, "Q", f5 + 6.0f, f6 + 6.0f, 8.0f, LegionHud.withAlpha(LegionHud.ACCENT, a));
        LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, "Hotkeys", f5 + 6.0f + headerIconW + 4.0f, f6 + 5.5f, 8.0f, LegionHud.withAlpha(LegionHud.SOFT, a));
        float moduleOffset = 18.0f;
        for (KeyBindEntry row : list) {
            String bind = row.cH();
            LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, row.name(), f5 + 6.0f, f6 + moduleOffset + 2.0f, 7.0f, LegionHud.withAlpha(LegionHud.SOFT, a));
            float bindW = Fonts.UNBOUNDED.getWidth(bind, 7.0f);
            LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, bind, f5 + f17 - bindW - 6.0f, f6 + moduleOffset + 2.5f, 6.5f, LegionHud.withAlpha(LegionHud.GRAY, a));
            moduleOffset += 12.0f;
        }
        Render2D.stopScissor(hudRenderEvent.j());
        hudRenderEvent.j().method_51448().method_22909();
        this.b(hudRenderEvent.j().method_51448());
        this.b(f5, f6, f17, f18, f19);
        super.onRender2D(hudRenderEvent);
    }

    static final class KeyBindEntry {
        private final String jM;
        private final String key;
        private final boolean toggled;

        KeyBindEntry(String string, String string2, boolean bl) {
            this.jM = string;
            this.key = string2;
            this.toggled = bl;
        }

        @Override
        public final String toString() {
            return "KeyBindEntry[" + "name=" + this.jM + ", " + "key=" + this.key + ", " + "toggled=" + this.toggled + "]";
        }

        @Override
        public final int hashCode() {
            return java.util.Objects.hash(this.jM, this.key, this.toggled);
        }

        @Override
        public final boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof KeyBindEntry other)) return false;
            return java.util.Objects.deepEquals(this.jM, other.jM)
                && java.util.Objects.deepEquals(this.key, other.key)
                && java.util.Objects.deepEquals(this.toggled, other.toggled);
        }

        public String name() {
            return this.jM;
        }

        public String cH() {
            return this.key;
        }

        public boolean cI() {
            return this.toggled;
        }
    }
}
