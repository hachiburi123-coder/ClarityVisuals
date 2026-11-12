package ru.method.clarityvisuals.modules.impl.render;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_276;
import net.minecraft.class_310;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.render.SkyFogRenderer;

public class ShaderFogModule
extends Module {
    private final EnumSetting<SkyFogMode> mode = new EnumSetting<SkyFogMode>("modules.settings.shaderfog.mode", SkyFogMode.NIGHT_SKY);
    private final ColorSetting fogColor = new ColorSetting("modules.settings.shaderfog.color", new Color(185, 56, 195, 255));
    private final SliderSetting intensity = new SliderSetting("modules.settings.shaderfog.intensity", 0.5f, 0.0f, 1.5f, 0.01f);
    private final SliderSetting speed = new SliderSetting("modules.settings.shaderfog.speed", 1.0f, 0.0f, 5.0f, 0.1f);

    public ShaderFogModule() {
        super("ShaderFog", ModuleCategory.ql);
        this.getSettings().add(this.mode);
        this.getSettings().add(this.fogColor);
        this.getSettings().add(this.intensity);
        this.getSettings().add(this.speed);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SkyFogRenderer.resetTime();
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (ShaderFogModule.fullNullCheck()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1522() == null) {
            return;
        }
        class_276 mainBuffer = mc.method_1522();
        float w = Math.max(1.0f, (float)mainBuffer.field_1482);
        float h = Math.max(1.0f, (float)mainBuffer.field_1481);
        float time = SkyFogRenderer.updateTime(((Float)this.speed.jT()).floatValue());
        Color color = (Color)this.fogColor.jT();
        SkyFogRenderer.render(time, w, h, color.getRGB(), ((Float)this.intensity.jT()).floatValue(), ((SkyFogMode)this.mode.jT()).ordinal());
    }

    public int ae(int originalColor) {
        Color color = (Color)this.fogColor.jT();
        float f = Math.max(0.0f, Math.min(1.0f, ((Float)this.intensity.jT()).floatValue()));
        int n2 = originalColor >> 16 & 0xFF;
        int n3 = originalColor >> 8 & 0xFF;
        int n4 = originalColor & 0xFF;
        int n5 = Math.round((float)n2 * (1.0f - f) + (float)color.getRed() * f);
        int n6 = Math.round((float)n3 * (1.0f - f) + (float)color.getGreen() * f);
        int n7 = Math.round((float)n4 * (1.0f - f) + (float)color.getBlue() * f);
        return n5 << 16 | n6 << 8 | n7;
    }

    public static enum SkyFogMode
    implements NamedOption {
        NIGHT_SKY("Night Sky"),
        DAY_SKY("Day Sky"),
        MORNING_SKY("Morning Sky"),
        SPACE("Space");

        private final String name;

        private SkyFogMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
