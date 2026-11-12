package ru.method.clarityvisuals.punchy.client.modelparts;

import ru.method.clarityvisuals.animation.Animation;

import net.minecraft.class_310;
import ru.method.clarityvisuals.punchy.client.animation.ItemAnimationManager;

final class AnimationStateMachine {
    AnimationStateMachine() {
    }

    void tick(class_310 class_3102) {
        ItemAnimationManager.tick(class_3102);
    }
}

