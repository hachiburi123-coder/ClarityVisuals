package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_10039;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.method.clarityvisuals.api.accessor.LivingEntityRenderStateAccessor;

@Mixin(value={class_10039.class})
public class ItemEntityRenderStateMixin
implements LivingEntityRenderStateAccessor {
    @Unique
    private boolean clarityvisuals$onGround;
    @Unique
    private float clarityvisuals$horizontalSpeed;
    @Unique
    private float clarityvisuals$verticalSpeed;
    @Unique
    private float clarityvisuals$xRotation;
    @Unique
    private float clarityvisuals$yRotation;
    @Unique
    private boolean clarityvisuals$additionalOffset;

    @Override
    public boolean clarityvisuals$isOnGround() {
        return this.clarityvisuals$onGround;
    }

    @Override
    public void clarityvisuals$setOnGround(boolean bl) {
        this.clarityvisuals$onGround = bl;
    }

    @Override
    public float clarityvisuals$getHorizontalSpeed() {
        return this.clarityvisuals$horizontalSpeed;
    }

    @Override
    public void clarityvisuals$setHorizontalSpeed(float f) {
        this.clarityvisuals$horizontalSpeed = f;
    }

    @Override
    public float clarityvisuals$getVerticalSpeed() {
        return this.clarityvisuals$verticalSpeed;
    }

    @Override
    public void clarityvisuals$setVerticalSpeed(float f) {
        this.clarityvisuals$verticalSpeed = f;
    }

    @Override
    public float clarityvisuals$getXRotation() {
        return this.clarityvisuals$xRotation;
    }

    @Override
    public void clarityvisuals$setXRotation(float f) {
        this.clarityvisuals$xRotation = f;
    }

    @Override
    public float clarityvisuals$getYRotation() {
        return this.clarityvisuals$yRotation;
    }

    @Override
    public void clarityvisuals$setYRotation(float f) {
        this.clarityvisuals$yRotation = f;
    }

    @Override
    public boolean clarityvisuals$hasAdditionalOffset() {
        return this.clarityvisuals$additionalOffset;
    }

    @Override
    public void clarityvisuals$setAdditionalOffset(boolean bl) {
        this.clarityvisuals$additionalOffset = bl;
    }
}

