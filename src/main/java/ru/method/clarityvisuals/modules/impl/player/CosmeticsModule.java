package ru.method.clarityvisuals.modules.impl.player;

import ru.method.clarityvisuals.api.setting.Setting;

import net.minecraft.class_437;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.setting.ButtonSetting;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticVisibility;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticsScreen;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticsService;
import ru.method.clarityvisuals.api.setting.EnumSetting;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.api.setting.SettingPersistence;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticEffects;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.FiguraAvatars;
import ru.method.clarityvisuals.screen.clickgui.ClickGui;

public class CosmeticsModule
extends Module {
    private final ButtonSetting xz = new ButtonSetting("settings.cosmetics.open", "settings.cosmetics.open", this::hY);
    private final EnumSetting<CosmeticVisibility> xA = new EnumSetting<CosmeticVisibility>("settings.cosmetics.visibility", CosmeticVisibility.dw){

        @Override
        public void a(CosmeticVisibility cosmeticVisibility) {
            super.a(cosmeticVisibility);
            CosmeticsModule.this.ia();
        }
    };
    private final CosmeticEffects xB = new CosmeticEffects();

    public CosmeticsModule() {
        super("Cosmetics", ModuleCategory.qm);
        this.getSettings().add(this.xz);
        this.getSettings().add(this.xA);
        this.ia();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.ia();
        // Возвращаем аватар фигуры, надетый через гардероб (если модуль сам его снимал).
        String string = FiguraAvatars.loadAppliedKey();
        if (string == null || string.isEmpty()) {
            return;
        }
        for (FiguraAvatars.AvatarEntry avatarEntry : FiguraAvatars.listAvatars()) {
            if (!avatarEntry.name().equals(string)) continue;
            FiguraAvatars.applyAvatar(avatarEntry.dir());
            break;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.ia();
        // Снимаем модель фигуры: пока косметика выключена, аватар не должен рендериться.
        FiguraAvatars.clearAvatar();
    }

    public CosmeticEffects hX() {
        return this.xB;
    }

    private void hY() {
        class_437 class_4372;
        if (aiG == null) {
            return;
        }
        if (!this.isToggle()) {
            this.setToggled(true);
        }
        class_437 class_4373 = class_4372 = CosmeticsModule.aiG.field_1755 instanceof ClickGui ? null : CosmeticsModule.aiG.field_1755;
        if (!(CosmeticsModule.aiG.field_1755 instanceof CosmeticsScreen)) {
            aiG.method_1507((class_437)new CosmeticsScreen(this, class_4372));
        }
    }

    public void hZ() {
    }

    void ia() {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        if (clarityVisuals == null) {
            return;
        }
        CosmeticsService cosmeticsService = clarityVisuals.getCosmeticsService();
        if (cosmeticsService == null) {
            return;
        }
        cosmeticsService.setEnabled(this.isToggle());
        cosmeticsService.a((CosmeticVisibility)this.xA.jT());
    }
}
