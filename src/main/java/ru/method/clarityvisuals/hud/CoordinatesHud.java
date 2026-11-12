package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.animation.Animation;

import java.awt.Color;
import net.minecraft.class_1657;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.ui.AnimatedComponent;
import ru.method.clarityvisuals.animation.Easing;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.input.KeyConstants;
import ru.method.clarityvisuals.util.MathUtils;
import ru.method.clarityvisuals.render.NotificationRenderer;
import ru.method.clarityvisuals.util.PlayerSpeedCalculator;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;
import ru.method.clarityvisuals.utils.render.fonts.Font;
import ru.method.clarityvisuals.utils.render.fonts.Fonts;

public class CoordinatesHud
extends HudElement {
    private final AnimatedComponent hv = new AnimatedComponent(Easing.aiZ);
    private final AnimatedComponent hw = new AnimatedComponent(Easing.aiZ);
    private boolean hx = false;
    private float hy = 0.0f;
    private float hz = 0.0f;
    private float hA = 0.0f;
    private float hB = 0.0f;
    private String hC = null;
    private long hD = 0L;

    public CoordinatesHud() {
        super("CoordinatesHud");
    }

    private boolean cm() {
        return HudEditorScreen.mb() || CoordinatesHud.aiG.field_1724 != null && CoordinatesHud.aiG.field_1687 != null;
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        if (CoordinatesHud.fullNullCheck() || this.bL()) {
            return;
        }
        boolean bl = this.cm();
        float f = this.hv.a(bl ? 1.0f : 0.0f, 200L);
        if (f <= 0.0f) {
            this.hx = false;
            return;
        }
        float f2 = this.getX();
        float f3 = this.getY();
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        if (CoordinatesHud.aiG.field_1724 != null) {
            n = (int)Math.floor(CoordinatesHud.aiG.field_1724.method_23317());
            n2 = (int)Math.floor(CoordinatesHud.aiG.field_1724.method_23318());
            n3 = (int)Math.floor(CoordinatesHud.aiG.field_1724.method_23321());
        } else if (HudEditorScreen.mb()) {
            n = 1234;
            n2 = 64;
            n3 = -567;
        }
        String string2 = " " + n + "  " + n2 + "  " + n3;
        String string3 = n + " " + n2 + " " + n3;
        float f5 = 7.5f;
        float iconSize = 9.0f;
        float iconW = Fonts.ICONSNEW.getWidth("J", iconSize);
        float f8 = Fonts.UNBOUNDED.getWidth(string2, f5);
        float f4 = 5.0f;
        float gap = 4.0f;
        float f20 = f4 + iconW + gap + f8 + f4;
        float f21 = 17.0f;
        float f22 = this.hw.a(f20, 200L);
        boolean bl2 = HudEditorScreen.mb();
        if (bl2) {
            f22 = f20;
        }
        int n5 = Math.max(0, Math.min(255, Math.round(255.0f * f)));
        float f23 = this.b(hudRenderEvent.j().method_51448(), f2, f3);
        hudRenderEvent.j().method_51448().method_22903();
        float[] fArray = this.bK();
        float f24 = this.gn.rk() * f * this.bJ();
        float f25 = f2 + f22 / 2.0f + fArray[0];
        float f26 = f3 + f21 / 2.0f + fArray[1];
        hudRenderEvent.j().method_51448().method_46416(f25, f26, 0.0f);
        hudRenderEvent.j().method_51448().method_22905(f24, f24, 1.0f);
        hudRenderEvent.j().method_51448().method_46416(-f25, -f26, 0.0f);
        LegionHud.card(hudRenderEvent.j().method_51448(), f2, f3, f22, f21, 8.0f, n5);
        float iconH = Fonts.ICONSNEW.getHeight(iconSize);
        float f31 = f3 + f21 / 2.0f - Fonts.UNBOUNDED.getHeight(f5) / 2.0f;
        Color color2 = LegionHud.withAlpha(LegionHud.SOFT, n5);
        LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.ICONSNEW, "J", f2 + f4, f3 + f21 / 2.0f - iconH / 2.0f, iconSize, color2);
        LegionHud.text(hudRenderEvent.j().method_51448(), Fonts.UNBOUNDED, string2, f2 + f4 + iconW + gap, f31, f5, color2);
        float f35 = f25 + (f2 + f4 + iconW + gap - f25) * f24;
        float f36 = f26 + (f31 - f26) * f24;
        this.hy = this.c(f2, f35, f23);
        this.hz = this.d(f3, f36, f23);
        this.hA = (iconW + gap + f8) * f24 * f23;
        this.hB = Fonts.UNBOUNDED.getHeight(f5) * f24 * f23;
        this.hC = string3;
        this.hx = f24 > 0.001f && this.hA > 0.001f && this.hB > 0.001f;
        hudRenderEvent.j().method_51448().method_22909();
        this.b(hudRenderEvent.j().method_51448());
        this.b(f2, f3, f22, f21, f23);
        super.onRender2D(hudRenderEvent);
    }

    @Override
    protected boolean c(int n, int n2) {
        if (!this.hx) {
            return false;
        }
        if (!MathUtils.c(this.hy, this.hz, this.hA, this.hB, n, n2)) {
            return false;
        }
        if (this.hC == null || this.hC.isBlank()) {
            return true;
        }
        long l = System.currentTimeMillis();
        if (l - this.hD < 250L) {
            return true;
        }
        this.hD = l;
        CoordinatesHud.aiG.field_1774.method_1455(this.hC);
        ClarityVisuals.getInstance().getNotifyManager().a(new NotificationRenderer(KeyConstants.akO, "\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u044b", 1000L));
        return true;
    }
}

