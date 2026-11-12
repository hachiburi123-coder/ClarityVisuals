package ru.method.clarityvisuals.utils.client.discord.callbacks;

import com.sun.jna.Callback;
import ru.method.clarityvisuals.utils.client.discord.utils.DiscordUser;

public interface ReadyCallback
extends Callback {
    public void apply(DiscordUser var1);
}

