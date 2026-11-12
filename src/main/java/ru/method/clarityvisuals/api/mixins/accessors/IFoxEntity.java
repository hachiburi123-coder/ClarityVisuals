package ru.method.clarityvisuals.api.mixins.accessors;

import net.minecraft.class_4019;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_4019.class})
public interface IFoxEntity {
    @Invoker(value="setSleeping")
    public void clarityvisuals$setSleeping(boolean var1);
}

