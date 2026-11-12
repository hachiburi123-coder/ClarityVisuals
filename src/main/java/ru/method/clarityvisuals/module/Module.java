package ru.method.clarityvisuals.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.Generated;
import net.minecraft.class_1074;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.input.KeyBind;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.Setting;
import ru.method.clarityvisuals.manager.SoundManager;

public abstract class Module
implements MinecraftClientAccess {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    protected boolean toggled;
    private boolean serverBlocked;
    private KeyBind bind = new KeyBind(-1, false);
    private final List<Setting<?>> settings = new ArrayList();
    private static final Set<String> NEW_MODULES = Set.of("AutoReply", "CustomVitals", "Drochka", "KillStreak", "Sounds",
        "Emotions");

    public Module(String string, ModuleCategory moduleCategory) {
        this.name = string;
        this.category = moduleCategory;
        this.description = "descriptions." + moduleCategory.name().toLowerCase() + "." + string.toLowerCase();
    }

    public String getDisplayName() {
        String string;
        String string2 = "modules." + this.name.toLowerCase(Locale.ROOT);
        return string2.equals(string = class_1074.method_4662((String)string2, (Object[])new Object[0])) ? this.name : string;
    }

    public void onEnable() {
        this.toggled = true;
        ClarityVisuals.getInstance().getEventHandler().subscribe((Object)this);
        boolean bl = ClarityVisuals.getInstance().isSuppressToggleFeedback();
        SoundManager soundManager = ClarityVisuals.getInstance().getSoundManager();
        if (!bl && soundManager != null) {
            soundManager.fF();
        }
    }

    public void onDisable() {
        this.toggled = false;
        ClarityVisuals.getInstance().getEventHandler().unsubscribe((Object)this);
        boolean bl = ClarityVisuals.getInstance().isSuppressToggleFeedback();
        SoundManager soundManager = ClarityVisuals.getInstance().getSoundManager();
        if (!bl && soundManager != null) {
            soundManager.fG();
        }
    }

    public void setToggled(boolean bl) {
        // Идемпотентность: применение того же состояния не должно повторно
        // подписывать модуль в шину событий (иначе каждый повторный вызов —
        // например, загрузка конфига — навсегда удваивает обработку событий).
        if (bl == this.toggled) {
            return;
        }
        // Функция в блок-листе сервера (LiteAPI): включать нельзя.
        if (bl && this.serverBlocked) {
            return;
        }
        if (bl) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public void toggle() {
        this.setToggled(!this.toggled);
    }

    public boolean isToggle() {
        return this.toggled;
    }

    public boolean isServerBlocked() {
        return this.serverBlocked;
    }

    public void setServerBlocked(boolean bl) {
        this.serverBlocked = bl;
    }

    public boolean isUnsafe() {
        return false;
    }

    public boolean isActionModule() {
        return false;
    }

    public void activate() {
        this.toggle();
    }

    public boolean hidesToggle() {
        return this.isActionModule();
    }

    public boolean isNew() {
        return NEW_MODULES.contains(this.name);
    }

    public String getUnsafeDescriptionKey() {
        return "descriptions.module.unsafe";
    }

    public static boolean fullNullCheck() {
        return Module.aiG.field_1724 == null || Module.aiG.field_1687 == null;
    }

    protected void playCriticalSound() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return;
        }
        SoundManager soundManager = clarityVisuals.getSoundManager();
        if (soundManager != null) {
            soundManager.a(this);
        }
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public ModuleCategory getCategory() {
        return this.category;
    }

    @Generated
    public boolean isToggled() {
        return this.toggled;
    }

    @Generated
    public KeyBind getBind() {
        return this.bind;
    }

    @Generated
    public List<Setting<?>> getSettings() {
        return this.settings;
    }

    @Generated
    public void setBind(KeyBind keyBind) {
        this.bind = keyBind;
    }
}

