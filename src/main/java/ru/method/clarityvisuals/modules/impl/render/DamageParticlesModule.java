package ru.method.clarityvisuals.modules.impl.render;

import ru.method.clarityvisuals.api.setting.Setting;

import java.awt.Color;
import meteordevelopment.orbit.EventHandler;
import ru.method.clarityvisuals.api.event.AttackEntityEvent;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.api.setting.ColorSetting;
import ru.method.clarityvisuals.modules.impl.render.DamageParticleController;
import ru.method.clarityvisuals.module.Module;
import ru.method.clarityvisuals.module.ModuleCategory;
import ru.method.clarityvisuals.modules.impl.render.ParticleEffectController;
import ru.method.clarityvisuals.api.event.WorldRenderStages;
import ru.method.clarityvisuals.utils.render.ColorUtils;

public class DamageParticlesModule
extends Module {
    private final ColorSetting Ac = new ColorSetting("settings.damageparticles.color", ColorUtils.getGlobalColor());
    private final DamageParticleController Ad = new DamageParticleController(this::iC);
    private final ParticleEffectController Ae = new ParticleEffectController(this::iC);

    public DamageParticlesModule() {
        super("DamageParticles", ModuleCategory.ql);
        this.getSettings().add(this.Ac);
        this.getSettings().addAll(this.Ad.getSettings());
        this.getSettings().addAll(this.Ae.getSettings());
    }

    private Color iC() {
        Color color = (Color)this.Ac.jT();
        return color == null ? ColorUtils.getGlobalColor() : color;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.Ad.clear();
        this.Ae.reset();
    }

    @Override
    public void onDisable() {
        this.Ad.clear();
        this.Ae.reset();
        super.onDisable();
    }

    @EventHandler
    private void onAttackEntity(AttackEntityEvent attackEntityEvent) {
        if (DamageParticlesModule.fullNullCheck() || attackEntityEvent.b() == null) {
            return;
        }
        this.Ad.f(attackEntityEvent.b());
        this.Ae.d(attackEntityEvent.b());
    }

    @EventHandler
    private void onTick(ClientTickEvent clientTickEvent) {
        if (DamageParticlesModule.fullNullCheck()) {
            return;
        }
        this.Ae.dI();
    }

    @EventHandler
    private void onRender3D(WorldRenderStages.MatrixStackStage matrixStackStage) {
        if (DamageParticlesModule.fullNullCheck()) {
            return;
        }
        this.Ad.jQ();
        this.Ae.d(matrixStackStage.l());
    }
}

