package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticModel;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.CosmeticPreviewContext;

/**
 * Self-contained render entry point for migrated auravisual ("auraApi") cosmetics.
 * Mirrors the original auravisual world rendering pipeline: translucent entity buffer,
 * the auravisual placement engine, then the parsed bedrock geometry with (optional)
 * bone animation. The anchor/camera transforms are applied by the callers.
 */
public final class AuraRenderer {
    private static final long eo = System.nanoTime();
    private static long en = 0L;

    private AuraRenderer() {
    }

    public static void a(CosmeticModel cosmeticModel, class_4587 class_45872, class_4597 class_45972, int n, int n2, float f) {
        if (cosmeticModel == null || cosmeticModel.texture() == null) {
            return;
        }
        AuraPayload auraPayload = cosmeticModel.bt();
        if (auraPayload == null || auraPayload.model == null) {
            return;
        }
        class_4588 class_45882 = class_45972.getBuffer(class_1921.method_23578(cosmeticModel.texture()));
        int n3 = n2 >> 24 & 0xFF;
        int n4 = n2 >> 16 & 0xFF;
        int n5 = n2 >> 8 & 0xFF;
        int n6 = n2 & 0xFF;
        class_45872.method_22903();
        try {
            AuraPlacement.a(class_45872, auraPayload);
            auraPayload.model.render(class_45872, class_45882, n, class_4608.field_21444,
                (float)n4 / 255.0f, (float)n5 / 255.0f, (float)n6 / 255.0f, (float)n3 / 255.0f,
                auraPayload.animation, f);
        }
        finally {
            class_45872.method_22909();
        }
    }

    /**
     * Animation clock used when the render path (e.g. GUI card previews) has no entity render state.
     */
    public static float b() {
        CosmeticPreviewContext.PreviewState previewState = CosmeticPreviewContext.aO();
        if (previewState != null) {
            return previewState.aS();
        }
        long l = System.nanoTime();
        if (en == 0L) {
            en = l - eo;
        }
        return (float)(l - eo) / 1.0E9f * 0.5f;
    }
}
