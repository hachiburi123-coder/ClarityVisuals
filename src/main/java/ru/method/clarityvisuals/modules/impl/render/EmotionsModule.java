package ru.method.clarityvisuals.modules.impl.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.api.event.HudRenderEvent;
import ru.method.clarityvisuals.api.setting.BindSetting;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion.EmotionWheelManager;

/**
 * Порт модуля "Emotions" из auravisual: колесо эмоций на удержании бинда
 * (по умолчанию V), позирование моделей и синхронизация эмоций между игроками.
 */
public class EmotionsModule
extends Module {
    private final BindSetting wheelBind = new BindSetting("settings.emotions.wheel_bind", new KeyBind(86, false));

    public EmotionsModule() {
        super("Emotions", ModuleCategory.ql);
        this.getSettings().add(this.wheelBind);
    }

    @Override
    public void onDisable() {
        EmotionWheelManager.INSTANCE.closeWheel(true);
        EmotionWheelManager.INSTANCE.clearActive();
        super.onDisable();
    }

    @EventHandler
    private void onTick(ClientTickEvent clientTickEvent) {
        if (this.isToggled() && !EmotionsModule.fullNullCheck()) {
            EmotionWheelManager.INSTANCE.tick(this);
        }
    }

    @EventHandler
    private void onHudRender(HudRenderEvent hudRenderEvent) {
        if (!this.isToggled()) {
            return;
        }
        EmotionWheelManager.INSTANCE.render(hudRenderEvent.j());
    }

    public boolean isWheelKeyHeld() {
        if (aiG.field_1755 != null) {
            return false;
        }
        KeyBind keyBind = (KeyBind)this.wheelBind.jT();
        if (keyBind == null || keyBind.isEmpty()) {
            return false;
        }
        long l = aiG.method_22683().method_4490();
        if (keyBind.jW()) {
            return GLFW.glfwGetMouseButton((long)l, (int)keyBind.c()) == 1;
        }
        return GLFW.glfwGetKey((long)l, (int)keyBind.c()) == 1;
    }
}
