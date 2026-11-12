package ru.method.clarityvisuals.managers.system;

import ru.method.clarityvisuals.api.setting.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import meteordevelopment.orbit.EventHandler;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.events.impl.EventMouse;
import ru.method.clarityvisuals.modules.impl.client.AllSkinsModule;
import ru.method.clarityvisuals.modules.impl.render.AspectRatioModule;
import ru.method.clarityvisuals.modules.impl.client.AuctionHelperModule;
import ru.method.clarityvisuals.modules.impl.player.AutoAcceptModule;
import ru.method.clarityvisuals.modules.impl.player.AutoNearModule;
import ru.method.clarityvisuals.modules.impl.player.AutoReplyModule;
import ru.method.clarityvisuals.modules.impl.player.AutoSpecModule;
import ru.method.clarityvisuals.modules.impl.render.BlockOverlayModule;
import ru.method.clarityvisuals.modules.impl.player.ChatHelperModule;
import ru.method.clarityvisuals.modules.impl.render.ChinaHatModule;
import ru.method.clarityvisuals.input.ClickGuiKeyHandler;
import ru.method.clarityvisuals.modules.impl.player.CommandDelayModule;
import ru.method.clarityvisuals.modules.impl.player.CoordInviteModule;
import ru.method.clarityvisuals.modules.impl.player.CosmeticsModule;
import ru.method.clarityvisuals.modules.impl.render.CrosshairModule;
import ru.method.clarityvisuals.modules.impl.render.CustomHitBoxModule;
import ru.method.clarityvisuals.modules.impl.render.CustomVitalsModule;
import ru.method.clarityvisuals.modules.impl.render.DamageParticlesModule;
import ru.method.clarityvisuals.modules.impl.player.DeathCoordsModule;
import ru.method.clarityvisuals.modules.impl.client.DiscordRpcModule;
import ru.method.clarityvisuals.modules.impl.render.DrochkaModule;
import ru.method.clarityvisuals.modules.impl.render.FogBlurModule;
import ru.method.clarityvisuals.modules.impl.render.FortniteDamageModule;
import ru.method.clarityvisuals.modules.impl.render.FreeLookModule;
import ru.method.clarityvisuals.modules.impl.combat.FriendHelperModule;
import ru.method.clarityvisuals.modules.impl.render.FullbrightModule;
import ru.method.clarityvisuals.modules.impl.render.HandsModule;
import ru.method.clarityvisuals.modules.impl.render.HitColorModule;
import ru.method.clarityvisuals.modules.impl.render.HitRangeModule;
import ru.method.clarityvisuals.modules.impl.player.HitSoundModule;
import ru.method.clarityvisuals.modules.impl.player.HWHelperModule;
import ru.method.clarityvisuals.modules.impl.render.ItemPhysicsModule;
import ru.method.clarityvisuals.modules.impl.render.ItemRadiusModule;
import ru.method.clarityvisuals.modules.impl.player.ItemScrollerModule;
import ru.method.clarityvisuals.modules.impl.render.JumpCirclesModule;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.api.event.KeyInputEvent;
import ru.method.clarityvisuals.modules.impl.render.KillEffectModule;
import ru.method.clarityvisuals.modules.impl.render.KillStreakModule;
import ru.method.clarityvisuals.modules.impl.player.LockSlotModule;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.modules.impl.render.MotionBlurModule;
import ru.method.clarityvisuals.api.event.MouseScrollEvent;
import ru.method.clarityvisuals.modules.impl.render.NoRenderModule;
import ru.method.clarityvisuals.modules.impl.render.ParticlesModule;
import ru.method.clarityvisuals.modules.impl.render.AuroraModule;
import ru.method.clarityvisuals.modules.impl.render.LowFogModule;
import ru.method.clarityvisuals.modules.impl.render.RainModule;
import ru.method.clarityvisuals.modules.impl.render.SakuraModule;
import ru.method.clarityvisuals.modules.impl.render.SandstormModule;
import ru.method.clarityvisuals.modules.impl.render.WinterModule;
import ru.method.clarityvisuals.modules.impl.client.PartyModule;
import ru.method.clarityvisuals.modules.impl.player.PotionTrackerModule;
import ru.method.clarityvisuals.modules.impl.player.PvpSaveModule;
import ru.method.clarityvisuals.modules.impl.render.SelfNametagsModule;
import ru.method.clarityvisuals.api.setting.SettingPersistence;
import ru.method.clarityvisuals.modules.impl.combat.ShiftTapModule;
import ru.method.clarityvisuals.modules.impl.render.ShaderFogModule;
import ru.method.clarityvisuals.modules.impl.render.SaturationModule;
import ru.method.clarityvisuals.modules.impl.player.SoundsModule;
import ru.method.clarityvisuals.modules.impl.movement.SprintModule;
import ru.method.clarityvisuals.modules.impl.client.StreamerModeModule;
import ru.method.clarityvisuals.modules.impl.render.SwingAnimationModule;
import ru.method.clarityvisuals.modules.impl.player.TapeMouseModule;
import ru.method.clarityvisuals.modules.impl.render.AntiInvisModule;
import ru.method.clarityvisuals.modules.impl.render.TargetEspModule;
import ru.method.clarityvisuals.modules.impl.player.TotemCounterModule;
import ru.method.clarityvisuals.modules.impl.player.TotemTrackerModule;
import ru.method.clarityvisuals.modules.impl.render.TntTimer;
import ru.method.clarityvisuals.modules.impl.render.TrailsModule;
import ru.method.clarityvisuals.modules.impl.render.TrajectoriesModule;
import ru.method.clarityvisuals.modules.impl.render.ViewModelModule;
import ru.method.clarityvisuals.modules.impl.player.WaypointsModule;
import ru.method.clarityvisuals.modules.impl.render.WorldModule;
import ru.method.clarityvisuals.modules.impl.render.ZoomModule;
import ru.method.clarityvisuals.modules.impl.client.UI;

public class ModuleManager
implements MinecraftClientAccess {
    private final List<Module> modules = new ArrayList<Module>();
    private final Map<Class<? extends Module>, Module> moduleLookup = new HashMap<Class<? extends Module>, Module>();
    private final Map<ModuleCategory, List<Module>> modulesByCategory = new LinkedHashMap<ModuleCategory, List<Module>>();
    private final List<ModuleCategory> categories = List.of(ModuleCategory.values());

    public ModuleManager() {
        ClarityVisuals.getInstance().getEventHandler().subscribe((Object)this);
        for (ModuleCategory moduleCategory : ModuleCategory.values()) {
            this.modulesByCategory.put(moduleCategory, new ArrayList());
        }
        this.addModules(new SprintModule(), new TrajectoriesModule(), new ChinaHatModule(), new UI(), new DiscordRpcModule(), new NoRenderModule(), new ItemScrollerModule(), new TargetEspModule(), new AntiInvisModule(), new AllSkinsModule(), new HitRangeModule(), new SelfNametagsModule(), new ShiftTapModule(), new AspectRatioModule(), new CustomHitBoxModule(), new BlockOverlayModule(), new HandsModule(), new HWHelperModule(), new DamageParticlesModule(), new ParticlesModule(), new RainModule(), new WinterModule(), new AuroraModule(), new SakuraModule(), new LowFogModule(), new SandstormModule(), new FogBlurModule(), new MotionBlurModule(), new TrailsModule(), new WorldModule(), new SwingAnimationModule(), new FullbrightModule(), new ZoomModule(), new ru.method.clarityvisuals.modules.impl.render.EmotionsModule(), new FreeLookModule(), new JumpCirclesModule(), new ItemPhysicsModule(), new FriendHelperModule(), new AutoNearModule(), new AuctionHelperModule(), new AutoSpecModule(), new CoordInviteModule(), new DeathCoordsModule(), new ChatHelperModule(), new CommandDelayModule(), new LockSlotModule(), new PvpSaveModule(), new TapeMouseModule(), new WaypointsModule(), new PartyModule(), new AutoAcceptModule(), new PotionTrackerModule(), new HitSoundModule(), new SoundsModule(), new KillEffectModule(), new KillStreakModule(), new ViewModelModule(), new StreamerModeModule(), new TotemTrackerModule(), new TotemCounterModule(), new ItemRadiusModule(), new DrochkaModule(), new ShaderFogModule(), new SaturationModule(), new HitColorModule(), new CrosshairModule(), new FortniteDamageModule(), new CustomVitalsModule(), new AutoReplyModule(), new CosmeticsModule(), new ru.method.clarityvisuals.modules.impl.render.SuperResolutionModule(), new TntTimer());
        for (Module module : this.modules) {
            SettingPersistence.a(module, module.getSettings(), module.getName());
        }
    }

    private void addModules(Module ... moduleArray) {
        for (Module module : moduleArray) {
            this.modules.add(module);
            this.moduleLookup.put(module.getClass(), module);
            this.modulesByCategory.computeIfAbsent(module.getCategory(), moduleCategory -> new ArrayList()).add(module);
        }
    }

    @EventHandler
    public void onKey(KeyInputEvent keyInputEvent) {
        if (ClarityVisuals.getInstance().isPanic()) {
            return;
        }
        if (keyInputEvent.getAction() == 1) {
            Object object;
            int n = keyInputEvent.c();
            if (n < 0) {
                return;
            }
            UI uI = this.getModule(UI.class);
            if (uI != null && (object = uI.getBind()) != null && (ClickGuiKeyHandler.a(uI, n) || ((KeyBind)object).af(n))) {
                return;
            }
            if (Module.fullNullCheck() || ModuleManager.aiG.field_1755 != null) {
                return;
            }
            for (Module module : this.modules) {
                KeyBind keyBind;
                if (module instanceof UI || (keyBind = module.getBind()).isEmpty() || keyBind.jW() || keyBind.c() != n) continue;
                module.toggle();
            }
        }
    }

    @EventHandler
    public void onMouse(EventMouse eventMouse) {
        if (Module.fullNullCheck() || ModuleManager.aiG.field_1755 != null || ClarityVisuals.getInstance().isPanic()) {
            return;
        }
        if (eventMouse.getAction() == 1) {
            int n = eventMouse.getButton();
            if (n < 0) {
                return;
            }
            for (Module module : this.modules) {
                KeyBind keyBind = module.getBind();
                if (keyBind.isEmpty() || !keyBind.jW() || keyBind.c() != n) continue;
                module.toggle();
            }
        }
    }

    @EventHandler
    public void onScroll(MouseScrollEvent mouseScrollEvent) {
        if (Module.fullNullCheck() || ModuleManager.aiG.field_1755 != null || ClarityVisuals.getInstance().isPanic()) {
            return;
        }
        double d = mouseScrollEvent.h();
        if (d == 0.0) {
            return;
        }
        for (Module module : this.modules) {
            if (module instanceof UI || !module.getBind().c(d)) continue;
            module.toggle();
        }
    }

    public List<Module> getModules(ModuleCategory moduleCategory) {
        return this.modulesByCategory.getOrDefault((Object)moduleCategory, List.of());
    }

    public List<ModuleCategory> getCategories() {
        return this.categories;
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return (T)this.moduleLookup.get(clazz);
    }

    @Generated
    public List<Module> getModules() {
        return this.modules;
    }

    @Generated
    public Map<Class<? extends Module>, Module> getModuleLookup() {
        return this.moduleLookup;
    }

    @Generated
    public Map<ModuleCategory, List<Module>> getModulesByCategory() {
        return this.modulesByCategory;
    }
}

