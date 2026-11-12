package ru.method.clarityvisuals.modules.impl.client;

import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.utils.client.discord.DiscordRichPresenceUtil;

public class DiscordRpcModule
extends Module {
    public DiscordRpcModule() {
        super("DiscordRPC", ModuleCategory.qn);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DiscordRichPresenceUtil.discordrpc();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        DiscordRichPresenceUtil.shutdownDiscord();
    }
}

