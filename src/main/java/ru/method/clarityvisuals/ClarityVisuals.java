package ru.method.clarityvisuals;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.method.clarityvisuals.service.BuildControlService;
import ru.method.clarityvisuals.guard.ClarityGuardSecurity;
import ru.method.clarityvisuals.service.ClientTaskScheduler;
import ru.method.clarityvisuals.command.CommandManager;
import ru.method.clarityvisuals.manager.ConfigManager;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticResourceRegistry;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticWorldEffectRenderer;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticsService;
import ru.method.clarityvisuals.manager.CrashLogTelegramManager;
import ru.method.clarityvisuals.manager.FriendManager;
import ru.method.clarityvisuals.manager.GpsManager;
import ru.method.clarityvisuals.hud.HudManager;
import ru.method.clarityvisuals.manager.MacroManager;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.manager.NotificationManager;
import ru.method.clarityvisuals.server.ServerHandler;
import ru.method.clarityvisuals.manager.ServerManager;
import ru.method.clarityvisuals.manager.SoundManager;
import ru.method.clarityvisuals.manager.UsageStatsManager;
import ru.method.clarityvisuals.manager.UserSyncManager;
import ru.method.clarityvisuals.manager.WaypointManager;
import ru.method.clarityvisuals.managers.system.ModuleManager;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.RemotePets;
import ru.method.clarityvisuals.punchy.client.PunchyClient;
import ru.method.clarityvisuals.screen.clickgui.legion.LegionClickGui;
import ru.method.clarityvisuals.screen.clickgui.SettingsWindow;
import ru.method.clarityvisuals.utils.client.discord.DiscordManager;
import ru.method.clarityvisuals.utils.client.web.HwLiteApi;
import ru.method.clarityvisuals.utils.debug.ProfiledEventBus;
import ru.method.clarityvisuals.utils.render.BlockOverlayRenderHooks;
import ru.method.clarityvisuals.utils.render.HologramLateRenderer;

public class ClarityVisuals
implements ModInitializer,
MinecraftClientAccess {
    public static final String MOD_ID = "clarityvisuals";
    private static final boolean CRASH_ON_STARTUP_TEST_ENABLED;
    private static final String CRASH_ON_STARTUP_MARKER;
    private static ClarityVisuals instance;
    private IEventBus eventHandler;
    private SoundManager soundManager;
    private NotificationManager notifyManager;
    private FriendManager friendManager;
    private UserSyncManager userSyncManager;
    private CosmeticsService cosmeticsService;
    private ModuleManager moduleManager;
    private ServerHandler serverHandler;
    private CommandManager commandManager;
    private ServerManager serverManager;
    private MacroManager macroManager;
    private HudManager hudManager;
    private ConfigManager configManager;
    private CrashLogTelegramManager crashLogTelegramManager;
    private UsageStatsManager usageStatsManager;
    private GpsManager gpsManager;
    private LegionClickGui clickGui;
    private WaypointManager waypointManager;
    private DiscordManager discordManager;
    private RemotePets remotePets;
    private boolean panic = false;
    private boolean suppressToggleFeedback = false;
    private boolean configLoading = false;
    private long initTime;
    private final List<SettingsWindow> settingsWindows = new CopyOnWriteArrayList<SettingsWindow>();
    public static final Logger LOGGER;

    public File getGlobalsDir() {
        return new File(ClarityVisuals.aiG.field_1697, "ClarityVisuals");
    }

    public File getConfigsDir() {
        return new File(this.getGlobalsDir(), "configs");
    }

    public File getThemesDir() {
        return new File(this.getGlobalsDir(), "themes");
    }

    public void onInitialize() {
        ClarityGuardSecurity.verify();
        LOGGER.info("[ClarityVisuals] Starting initialization.");
        this.initTime = System.currentTimeMillis();
        instance = this;
        this.createDirs(this.getGlobalsDir(), this.getConfigsDir(), this.getThemesDir());
        BuildControlService.ex();
        this.eventHandler = new ProfiledEventBus();
        this.eventHandler.registerLambdaFactory(ClarityVisuals.class.getPackageName(), (method, clazz) -> (MethodHandles.Lookup)method.invoke(null, clazz, MethodHandles.lookup()));
        ClientTaskScheduler.register();
        this.soundManager = new SoundManager();
        this.notifyManager = new NotificationManager();
        this.friendManager = new FriendManager();
        this.cosmeticsService = new CosmeticsService();
        this.userSyncManager = new UserSyncManager();
        this.moduleManager = new ModuleManager();
        this.serverHandler = new ServerHandler();
        this.commandManager = new CommandManager();
        this.serverManager = new ServerManager();
        this.waypointManager = new WaypointManager();
        this.macroManager = new MacroManager();
        this.hudManager = new HudManager();
        this.crashLogTelegramManager = new CrashLogTelegramManager();
        this.usageStatsManager = new UsageStatsManager();
        this.configManager = new ConfigManager();
        this.gpsManager = new GpsManager();
        this.clickGui = new LegionClickGui();
        this.discordManager = new DiscordManager();
        try {
            BlockOverlayRenderHooks.register();
        }
        catch (LinkageError linkageError) {
            LOGGER.warn("[ClarityVisuals] BlockOverlay hooks are unavailable in this runtime; continuing without outline override support.", (Throwable)linkageError);
        }
        try {
            FabricLoader.getInstance().getModContainer("clarityvisuals-fabric").ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack((class_2960)class_2960.method_60655((String)"punchy", (String)"punchy"), (ModContainer)modContainer, (class_2561)class_2561.method_43470((String)"Punchy"), (ResourcePackActivationType)ResourcePackActivationType.ALWAYS_ENABLED));
            PunchyClient.init();
        }
        catch (Throwable throwable) {
            LOGGER.error("[ClarityVisuals] Punchy (Punch Mode) init failed; continuing without it.", throwable);
        }
        this.eventHandler.subscribe((Object)new HologramLateRenderer());
        try {
            this.remotePets = new RemotePets();
        }
        catch (Throwable throwable) {
            LOGGER.error("[ClarityVisuals] RemotePets unavailable; continuing without remote pets.", throwable);
        }
        this.configManager.initialize();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            CosmeticResourceRegistry.register();
            HwLiteApi.register();
        }
        this.usageStatsManager.eC();
        this.maybeCrashOnStartupForTest();
        this.crashLogTelegramManager.eh();
        try {
            ru.method.clarityvisuals.modules.impl.render.cosmetic.FiguraAvatars.ensureInstalled();
        }
        catch (Throwable throwable) {
            LOGGER.warn("[ClarityVisuals] Figura avatar installer failed to start.", throwable);
        }
        LOGGER.info("[ClarityVisuals] Successfully initialized for {} ms.", (Object)(System.currentTimeMillis() - this.initTime));
        LOGGER.info("[ClarityVisuals] BUILD MARKER 21 streamerTabDonate.");
    }

    private void maybeCrashOnStartupForTest() {
        if (!CRASH_ON_STARTUP_TEST_ENABLED) {
            return;
        }
        File file = new File(this.getGlobalsDir(), "startup-crash-test.done");
        if (file.exists()) {
            return;
        }
        try {
            Files.writeString(file.toPath(), (CharSequence)("created=" + System.currentTimeMillis()), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException iOException) {
        }
        throw new RuntimeException("[ClarityVisuals] Startup crash test is enabled (one-time).");
    }

    private void createDirs(File ... fileArray) {
        for (File file : fileArray) {
            file.mkdirs();
        }
    }

    public static class_2960 id(String string) {
        String string2 = string == null ? "" : string.trim().replace('\\', '/');
        class_2960 class_29602 = class_2960.method_12829((String)("clarityvisuals:" + string2));
        if (class_29602 != null) {
            return class_29602;
        }
        String string3 = ClarityVisuals.sanitizeIdentifierPath(string2);
        class_2960 class_29603 = class_2960.method_12829((String)("clarityvisuals:" + string3));
        if (class_29603 != null) {
            LOGGER.warn("Invalid Identifier path '{}' (sanitized to '{}')", (Object)string, (Object)string3);
            return class_29603;
        }
        LOGGER.error("Failed to create Identifier from path '{}'", (Object)string);
        return class_2960.method_60655((String)"clarityvisuals", (String)"invalid");
    }

    private static String sanitizeIdentifierPath(String string) {
        int n;
        char c;
        if (string == null) {
            return "invalid";
        }
        String string2 = string.trim().replace('\\', '/').toLowerCase(Locale.ROOT);
        if (string2.isEmpty()) {
            return "invalid";
        }
        StringBuilder stringBuilder = new StringBuilder(string2.length());
        boolean bl = false;
        for (int i = 0; i < string2.length(); ++i) {
            c = string2.charAt(i);
            int n2 = n = c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.' || c == '_' || c == '-' ? 1 : 0;
            if (n != 0) {
                stringBuilder.append(c);
                bl = false;
                continue;
            }
            if (bl) continue;
            stringBuilder.append('_');
            bl = true;
        }
        String string3 = stringBuilder.toString();
        for (c = '\u0000'; c < string3.length() && (string3.charAt(c) == '/' || string3.charAt(c) == '_'); ++c) {
        }
        for (n = string3.length(); n > c && string3.charAt(n - 1) == '_'; --n) {
        }
        return (string3 = string3.substring(c, n)).isEmpty() ? "invalid" : string3;
    }

    public List<SettingsWindow> getSettingsWindows() {
        return this.settingsWindows;
    }

    @Generated
    public IEventBus getEventHandler() {
        return this.eventHandler;
    }

    @Generated
    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    @Generated
    public NotificationManager getNotifyManager() {
        return this.notifyManager;
    }

    @Generated
    public FriendManager getFriendManager() {
        return this.friendManager;
    }

    @Generated
    public UserSyncManager getUserSyncManager() {
        return this.userSyncManager;
    }

    @Generated
    public CosmeticsService getCosmeticsService() {
        return this.cosmeticsService;
    }

    @Generated
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Generated
    public ServerHandler getServerHandler() {
        return this.serverHandler;
    }

    @Generated
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Generated
    public ServerManager getServerManager() {
        return this.serverManager;
    }

    @Generated
    public MacroManager getMacroManager() {
        return this.macroManager;
    }

    @Generated
    public HudManager getHudManager() {
        return this.hudManager;
    }

    @Generated
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Generated
    public CrashLogTelegramManager getCrashLogTelegramManager() {
        return this.crashLogTelegramManager;
    }

    @Generated
    public UsageStatsManager getUsageStatsManager() {
        return this.usageStatsManager;
    }

    @Generated
    public GpsManager getGpsManager() {
        return this.gpsManager;
    }

    @Generated
    public LegionClickGui getClickGui() {
        return this.clickGui;
    }

    @Generated
    public WaypointManager getWaypointManager() {
        return this.waypointManager;
    }

    @Generated
    public DiscordManager getDiscordManager() {
        return this.discordManager;
    }

    @Generated
    public RemotePets getRemotePets() {
        return this.remotePets;
    }

    @Generated
    public boolean isPanic() {
        return this.panic;
    }

    @Generated
    public boolean isSuppressToggleFeedback() {
        return this.suppressToggleFeedback;
    }

    @Generated
    public boolean isConfigLoading() {
        return this.configLoading;
    }

    @Generated
    public long getInitTime() {
        return this.initTime;
    }

    @Generated
    public static ClarityVisuals getInstance() {
        return instance;
    }

    @Generated
    public void setPanic(boolean bl) {
        this.panic = bl;
    }

    @Generated
    public void setSuppressToggleFeedback(boolean bl) {
        this.suppressToggleFeedback = bl;
    }

    @Generated
    public void setConfigLoading(boolean bl) {
        this.configLoading = bl;
    }

    static {
        CRASH_ON_STARTUP_MARKER = "startup-crash-test.done";
        CRASH_ON_STARTUP_TEST_ENABLED = Boolean.parseBoolean(System.getenv().getOrDefault("LV_STARTUP_CRASH_TEST", "false"));
        LOGGER = LogManager.getLogger((String)"ClarityVisuals");
    }
}

