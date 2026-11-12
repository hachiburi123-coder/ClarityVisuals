package ru.method.clarityvisuals.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import ru.method.clarityvisuals.client.hud.HudState;

public final class ClarityHudClient
implements ClientModInitializer {
    private static final HudState HUD_STATE = new HudState();

    public static HudState state() {
        return HUD_STATE;
    }

    public void onInitializeClient() {
        class_304 class_3042 = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.clarityvisuals.preview", class_3675.class_307.field_1668, 297, "category.clarityvisuals"));
        ClientTickEvents.END_CLIENT_TICK.register(class_3102 -> HUD_STATE.tick(class_3102, class_3042));
    }
}

