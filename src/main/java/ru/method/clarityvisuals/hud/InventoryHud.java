package ru.method.clarityvisuals.hud;

import ru.method.clarityvisuals.api.setting.Setting;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import ru.method.clarityvisuals.api.setting.BooleanSetting;
import ru.method.clarityvisuals.screen.HudEditorScreen;
import ru.method.clarityvisuals.modules.impl.hud.HudElement;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.hud.HudThemeRenderer;
import ru.method.clarityvisuals.utils.render.BorderRadius;
import ru.method.clarityvisuals.utils.render.ColorRGBA;
import ru.method.clarityvisuals.utils.render.ColorUtils;
import ru.method.clarityvisuals.utils.render.Render2D;

public class InventoryHud
extends HudElement {
    private static final float jz = 18.0f;
    private static final float jA = 16.0f;
    private static final float jB = 0.9f;
    private static final float jC = 2.0f;
    private static final float jD = 7.0f;
    private static final float jE = 7.0f;
    private static final float jF = 1.0f;
    private static final int jG = 9;
    private final BooleanSetting jH = new BooleanSetting("settings.inventoryhud.hotbar", false);

    public InventoryHud() {
        super("InventoryHud");
    }

    @Override
    public void onRender2D(HudRenderEvent hudRenderEvent) {
        if (InventoryHud.fullNullCheck() || this.bL()) {
            return;
        }
        float f3 = this.getX();
        float f4 = this.getY();
        float f5 = this.b(hudRenderEvent.j().method_51448(), f3, f4);
        hudRenderEvent.j().method_51448().method_22903();
        float f6 = this.gn.rk() * this.bJ();
        float padding = 6.0f;
        float slotGap = 1.0f;
        float slotsWidth = 108.0f + 8.0f * slotGap;
        float slotsHeight = 36.0f + 2.0f * slotGap;
        float contentWidth = slotsWidth + padding * 2.0f;
        float contentHeight = slotsHeight + padding * 2.0f;
        float f = contentWidth;
        float f2 = contentHeight + 4.0f;
        float f7 = f3 + f / 2.0f;
        float f8 = f4 + f2 / 2.0f;
        hudRenderEvent.j().method_51448().method_46416(f7, f8, 0.0f);
        hudRenderEvent.j().method_51448().method_22905(f6, f6, 1.0f);
        hudRenderEvent.j().method_51448().method_46416(-f7, -f8, 0.0f);
        int a = Math.max(0, Math.min(255, Math.round(255.0f * f6)));
        Render2D.drawBorder(hudRenderEvent.j().method_51448(), f3 + 2.0f, f4 + 2.0f, contentWidth - 4.0f, contentHeight - 4.0f, 5.0f, 0.35f, new ColorRGBA(new Color(90, 90, 90, a)));
        float slotsStartX = f3 + padding;
        float slotsStartY = f4 + padding;
        ArrayList<String> counts = new ArrayList<String>();
        ArrayList<float[]> countPos = new ArrayList<float[]>();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int n2 = 9 + i * 9 + j;
                float slotX = slotsStartX + (float)j * (12.0f + slotGap);
                float slotY = slotsStartY + (float)i * (12.0f + slotGap);
                Render2D.drawRoundedRect(hudRenderEvent.j().method_51448(), slotX, slotY, 12.0f, 12.0f, 2.0f, new Color(28, 28, 28, a));
                class_1799 stack = InventoryHud.aiG.field_1724.method_31548().method_5438(n2);
                if (stack.method_7960()) {
                    continue;
                }
                float itemX = slotX + (12.0f - 8.0f) / 2.0f;
                float itemY = slotY + (12.0f - 8.0f) / 2.0f;
                hudRenderEvent.j().method_51448().method_22903();
                hudRenderEvent.j().method_51448().method_46416(itemX, itemY, 0.0f);
                hudRenderEvent.j().method_51448().method_22905(0.5f, 0.5f, 1.0f);
                this.b(hudRenderEvent, stack, 0.0f, 0.0f);
                hudRenderEvent.j().method_51448().method_22909();
                int count = stack.method_7947();
                if (count <= 1) {
                    continue;
                }
                counts.add(String.valueOf(count));
                countPos.add(new float[]{slotX, slotY});
            }
        }
        for (int i = 0; i < counts.size(); ++i) {
            String countText = counts.get(i);
            float[] pos = countPos.get(i);
            int textWidth = InventoryHud.aiG.field_1772.method_1727(countText);
            int textX = (int)(pos[0] + 12.0f - (float)textWidth);
            int textY = (int)(pos[1] + 12.0f - 9.0f + 1.0f);
            hudRenderEvent.j().method_51433(InventoryHud.aiG.field_1772, countText, textX, textY, a << 24 | 0xFFFFFF, true);
        }
        hudRenderEvent.j().method_51448().method_22909();
        this.b(hudRenderEvent.j().method_51448());
        this.b(f3, f4, f, f2, f5);
        super.onRender2D(hudRenderEvent);
    }

    private void b(HudRenderEvent hudRenderEvent, class_1799 class_17992, float f, float f2) {
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            RenderSystem.enableDepthTest();
            int n = Math.round(f);
            int n2 = Math.round(f2);
            hudRenderEvent.j().method_51448().method_22903();
            hudRenderEvent.j().method_51448().method_46416((float)n, (float)n2, 0.0f);
            hudRenderEvent.j().method_51448().method_22905(0.5f, 0.5f, 1.0f);
            hudRenderEvent.j().method_51423((class_1309)InventoryHud.aiG.field_1724, class_17992, 0, 0, 0);
            hudRenderEvent.j().method_51448().method_22909();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
        catch (Exception exception) {
        }
    }

    private float i(float f) {
        return Math.round(f);
    }
}

