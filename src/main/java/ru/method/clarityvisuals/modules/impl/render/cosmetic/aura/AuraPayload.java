package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

/**
 * Payload embedded in a cosmetic wrapper (<b>auraApi</b>): one migrated auravisual cosmetic.
 * Everything that is needed to place and render the model exactly like the original
 * auravisual pipeline: geometry, (optional) animation, orientation meta and pre-computed placement.
 */
public final class AuraPayload {
    public final String category;
    public final String slug;
    public final AuraModel model;
    public final AuraAnimation animation;
    public final float[] orientationRotation = new float[]{0.0f, 0.0f, 0.0f};
    public final float[] orientationSign = new float[]{1.0f, 1.0f, 1.0f};
    public final AuraPlacement placement;

    public AuraPayload(String category, String slug, AuraModel model, AuraAnimation animation, float[] rotation, float[] sign) {
        this.category = category;
        this.slug = slug;
        this.model = model;
        this.animation = animation;
        if (rotation != null && rotation.length >= 3) {
            for (int index = 0; index < 3; index++) {
                float value = rotation[index];
                if (Float.isFinite(value)) {
                    this.orientationRotation[index] = value;
                }
            }
        }

        if (sign != null && sign.length >= 3) {
            for (int index = 0; index < 3; index++) {
                float value = sign[index];
                if (Float.isFinite(value) && value != 0.0f) {
                    this.orientationSign[index] = Math.signum(value);
                }
            }
        }

        this.placement = AuraPlacement.a(this);
    }
}
