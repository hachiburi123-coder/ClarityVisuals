package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.NamedOption;
import ru.method.clarityvisuals.api.setting.SliderSetting;
import ru.method.clarityvisuals.utils.render.HandsRenderer;

public class HandsModule
extends Module {
    public final EnumSetting<HandMode> Bi = new EnumSetting<HandMode>("modules.settings.hands.mode", HandMode.AQUA);
    public final SliderSetting Bn = new SliderSetting("modules.settings.hands.gradient_speed", 1.0f, 0.1f, 5.0f, 0.1f);
    public final SliderSetting Bo = new SliderSetting("settings.hands.alpha", 100.0f, 0.0f, 100.0f, 1.0f);

    public HandsModule() {
        super("Hands", ModuleCategory.ql);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        HandsRenderer.release();
    }

    public float handSpeed() {
        return ((Float) this.Bn.jT()).floatValue();
    }

    public float handAlpha() {
        return ((Float) this.Bo.jT()).floatValue() / 100.0f;
    }

    public static enum HandMode
    implements NamedOption  {
        AQUA("settings.hands.mode.aqua", "sirius_aqua", 0.6f),
        FLOW("settings.hands.mode.flow", "sirius_flow", 0.06f),
        SMOKE("settings.hands.mode.smoke", "sirius_smoke", 3.0f),
        HOLY_FUCK("settings.hands.mode.holy_fuck", "sirius_holyfuck", 0.6f),
        GANG("settings.hands.mode.gang", "sirius_gang", 0.6f),
        GAMER("settings.hands.mode.gamer", "sirius_gamer", 1.8f),
        GALAXY("settings.hands.mode.galaxy", "sirius_galaxy", 0.06f),
        TECHNO("settings.hands.mode.techno", "sirius_techno", 0.6f),
        GOLDEN("settings.hands.mode.golden", "sirius_golden", 0.6f),
        GUI_SHADER("settings.hands.mode.gui_shader", "sirius_guishader", 1.2f),
        HIDEF("settings.hands.mode.hidef", "sirius_hidef", 3.0f),
        HOMIE("settings.hands.mode.homie", "sirius_homie", 0.06f),
        SHELDON("settings.hands.mode.sheldon", "sirius_sheldon", 0.06f),
        SMOKY("settings.hands.mode.smoky", "sirius_smoky", 0.06f),
        YIPPIE_OWNS("settings.hands.mode.yippie_owns", "sirius_yippieowns", 6.0f),
        PURPLE("settings.hands.mode.purple", "sirius_purple", 3.0f);

        private final String Bu;
        private final String shaderName;
        private final float timeScale;

        private HandMode(String string2, String shaderName, float timeScale) {
            this.Bu = string2;
            this.shaderName = shaderName;
            this.timeScale = timeScale;
        }

        @Override
        public String getName() {
            return this.Bu;
        }

        public String getShaderName() {
            return this.shaderName;
        }

        public float getTimeScale() {
            return this.timeScale;
        }
    }
}
