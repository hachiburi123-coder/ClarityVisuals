package ru.method.clarityvisuals.api.mixins.accessors;

import net.minecraft.class_4267;
import net.minecraft.class_642;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_4267.class_4270.class})
public interface IServerEntry {
    @Accessor(value="server")
    public class_642 clarityvisuals$getServer();
}

