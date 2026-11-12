package ru.method.clarityvisuals.utils.client.discord;

import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.utils.client.discord.DiscordManager;

public final class DiscordRichPresenceUtil {
    private DiscordRichPresenceUtil() {
    }

    public static void discordrpc() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return;
        }
        DiscordManager discordManager = clarityVisuals.getDiscordManager();
        if (discordManager == null) {
            return;
        }
        discordManager.init();
        discordManager.forcePresenceReplace();
    }

    public static void shutdownDiscord() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return;
        }
        DiscordManager discordManager = clarityVisuals.getDiscordManager();
        if (discordManager == null) {
            return;
        }
        discordManager.stopRPC();
    }
}

