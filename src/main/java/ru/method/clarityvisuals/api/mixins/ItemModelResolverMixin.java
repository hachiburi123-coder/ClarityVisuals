package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_10442;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticSwords;

/**
 * Hooks every ItemModelManager resolution entry point and lets the cosmetic sword
 * swap the render stack for a CUSTOM_MODEL_DATA-carrying copy. Re-running these
 * hooks is safe: getRenderStack() replaces CUSTOM_MODEL_DATA on a fresh copy each time.
 */
@Mixin(value={class_10442.class})
public class ItemModelResolverMixin {
    @ModifyVariable(method={
        "updateForLivingEntity(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/entity/LivingEntity;)V",
        "updateForNonLivingEntity(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/entity/Entity;)V",
        "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
        "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"
    }, at=@At(value="HEAD"), argsOnly=true, ordinal=0, require=0)
    private class_1799 clarityvisuals$customSword(class_1799 class_17992) {
        return CosmeticSwords.INSTANCE.getRenderStack(class_17992);
    }
}
