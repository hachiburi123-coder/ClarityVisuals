package ru.method.clarityvisuals.punchy.platform;

import ru.method.clarityvisuals.punchy.platform.FabricClientPlatform;
import ru.method.clarityvisuals.punchy.platform.FabricPlatformHelper;
import ru.method.clarityvisuals.punchy.platform.services.IClientPlatform;
import ru.method.clarityvisuals.punchy.platform.services.IPlatformHelper;

public final class Services {
    public static final IPlatformHelper PLATFORM = new FabricPlatformHelper();
    public static final IClientPlatform CLIENT = new FabricClientPlatform();

    private Services() {
    }
}

