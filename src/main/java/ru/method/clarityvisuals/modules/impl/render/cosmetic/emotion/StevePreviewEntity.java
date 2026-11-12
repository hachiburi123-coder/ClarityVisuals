package ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion;

import com.mojang.authlib.GameProfile;
import net.minecraft.class_1068;
import net.minecraft.class_638;
import net.minecraft.class_745;
import net.minecraft.class_8685;

/**
 * Анонимный класс из исходника (EmotionWheelManager$1): OtherClientPlayerEntity
 * со строгим скином Steve — нужен для превью эмоции в секторе колеса.
 */
public class StevePreviewEntity
extends class_745 {
    public StevePreviewEntity(class_638 class_6382, GameProfile gameProfile) {
        super(class_6382, gameProfile);
    }

    @Override
    public class_8685 method_52814() {
        return class_1068.method_62620();
    }
}
