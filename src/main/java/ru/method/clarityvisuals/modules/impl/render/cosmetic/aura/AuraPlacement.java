package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Port of the auravisual placement engine (CosmeticsManager placement/applyPlacement)
 * limited to the "hats" and "wings" categories. The translate/scale/rotation order is
 * kept 1:1 with the original so that models land at identical world positions.
 */
public final class AuraPlacement {
    private static final Map<String, WingAdjustment> FIXED_WING_ADJUSTMENTS = new HashMap<String, WingAdjustment>();

    static {
        FIXED_WING_ADJUSTMENTS.put("wings_1_cddf708b", new WingAdjustment(0.05249998f, 0.0f));
        FIXED_WING_ADJUSTMENTS.put("wings_3_81b06a4b", new WingAdjustment(0.101499975f, -0.0485f));
        FIXED_WING_ADJUSTMENTS.put("wings_7_ee401e1a", new WingAdjustment(-0.055999994f, 0.11074999f));
        FIXED_WING_ADJUSTMENTS.put("wings_8_ee401e1a", new WingAdjustment(0.17849997f, 0.094499975f));
        FIXED_WING_ADJUSTMENTS.put("wings_9_ee401e1a", new WingAdjustment(-0.24849999f, 0.0f));
        FIXED_WING_ADJUSTMENTS.put("wings_12_f684c6fe", new WingAdjustment(0.0f, 0.10100001f));
        FIXED_WING_ADJUSTMENTS.put("wings_14_19ec3eb9", new WingAdjustment(-0.1435f, 0.0f));
        FIXED_WING_ADJUSTMENTS.put("wings_22_3dfa663d", new WingAdjustment(0.0f, -0.064750016f));
        FIXED_WING_ADJUSTMENTS.put("wings_23_28b34740", new WingAdjustment(0.048999995f, -0.0485f));
    }

    private static final Map<String, ModelAdjustment> FIXED_MODEL_ADJUSTMENTS = new HashMap<String, ModelAdjustment>();

    static {
        FIXED_MODEL_ADJUSTMENTS.put("hats_2_cf6887c0", new ModelAdjustment(0.0f, 0.0f, -0.16f, 0.0f, 90.0f, -90.0f));
        FIXED_MODEL_ADJUSTMENTS.put("hats_13_e2b017aa", new ModelAdjustment(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 180.0f));
    }

    private static final ModelAdjustment NO_MODEL_ADJUSTMENT = new ModelAdjustment(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private static final Set<String> TOP_HATS = Set.of(
        "hats_1_f83bf9c2",
        "hats_5_469c9b9e",
        "hats_8_6a481e43",
        "hats_9_5f7f08cb",
        "hats_15_5a319037",
        "hats_16_f37d302a",
        "hats_17_8f15edc6",
        "hats_20_f2a80fc7",
        "hats_25_2b9734ca"
    );
    private static final Set<String> LOWER_PLAYER_WINGS = Set.of("wings_8_ee401e1a");
    private static final Set<String> HIGHER_PLAYER_WINGS = Set.of("wings_3_81b06a4b");
    private static final String EVEN_HIGHER_INFERNAL_WINGS = "wings_4_ac93f2ee";
    private static final Set<String> SLIGHTLY_HIGHER_PLAYER_WINGS = Set.of("wings_13_04875af2", "wings_23_28b34740");
    private static final String RAISED_PLAYER_WINGS = "wings_9_ee401e1a";
    private static final Set<String> OPPOSITE_VERTICAL_WINGS = Set.of(
        "wings_3_81b06a4b", "wings_4_ac93f2ee", "wings_8_ee401e1a", "wings_9_ee401e1a",
        "wings_13_04875af2", "wings_22_3dfa663d", "wings_23_28b34740"
    );

    public final float x;
    public final float y;
    public final float z;
    public final float scale;
    public final float autoYaw;

    AuraPlacement(float x, float y, float z, float scale, float autoYaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
        this.autoYaw = autoYaw;
    }

    private static final class WingAdjustment {
        final float height;
        final float distance;

        WingAdjustment(float height, float distance) {
            this.height = height;
            this.distance = distance;
        }
    }

    private static final class ModelAdjustment {
        final float x;
        final float y;
        final float z;
        final float rotationX;
        final float rotationY;
        final float rotationZ;

        ModelAdjustment(float x, float y, float z, float rotationX, float rotationY, float rotationZ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotationX = rotationX;
            this.rotationY = rotationY;
            this.rotationZ = rotationZ;
        }
    }

    public static AuraPlacement a(AuraPayload payload) {
        return b(payload);
    }

    /**
     * Port of CosmeticsManager.parsePlacement for the "hats" and "wings" categories.
     */
    private static AuraPlacement b(AuraPayload payload) {
        String slug = payload.slug;
        String category = payload.category;
        if (payload.model == null) {
            return new AuraPlacement(0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        }

        float[] orientationSign = payload.orientationSign;
        float autoYaw = 0.0f;
        float[] bounds = AuraPlacement.orientedBounds(payload.model, payload.orientationRotation, orientationSign, 0.0f);
        float width = bounds[3] - bounds[0];
        float height = bounds[4] - bounds[1];
        float depth = bounds[5] - bounds[2];
        if ("wings".equals(category) && depth > width * 1.35f) {
            autoYaw = 90.0f;
            bounds = AuraPlacement.orientedBounds(payload.model, payload.orientationRotation, orientationSign, autoYaw);
            width = bounds[3] - bounds[0];
            height = bounds[4] - bounds[1];
            depth = bounds[5] - bounds[2];
        }

        if ("hats".equals(category)) {
            float scale = Math.min(1.0f, Math.min(AuraPlacement.safeRatio(1.25f, width), Math.min(AuraPlacement.safeRatio(1.15f, height), AuraPlacement.safeRatio(1.1f, depth))));
            float largest = Math.max(width, Math.max(height, depth));
            if (largest < 0.35f) {
                scale = Math.min(1.65f, AuraPlacement.safeRatio(0.52f, largest));
            } else if (largest < 0.55f) {
                scale = Math.min(1.3f, AuraPlacement.safeRatio(0.62f, largest));
            }

            float targetX = 0.0f;
            float targetY = TOP_HATS.contains(slug) ? 0.27f : -0.03f;
            float targetZ = 0.0f;
            return new AuraPlacement(
                targetX - AuraPlacement.center(bounds[0], bounds[3]) * scale,
                targetY - bounds[1] * scale,
                targetZ - AuraPlacement.center(bounds[2], bounds[5]) * scale,
                scale,
                autoYaw
            );
        }

        float scale = Math.min(1.0f, Math.min(AuraPlacement.safeRatio(2.8f, width), AuraPlacement.safeRatio(1.75f, height)));
        float targetY = -0.32f;
        float targetZ = 0.14f;
        if (LOWER_PLAYER_WINGS.contains(slug)) {
            targetY -= 0.06f;
        }

        if (HIGHER_PLAYER_WINGS.contains(slug)) {
            targetY += 0.16f;
        }

        if (EVEN_HIGHER_INFERNAL_WINGS.equals(slug)) {
            targetY += 0.21f;
        }

        if (SLIGHTLY_HIGHER_PLAYER_WINGS.contains(slug)) {
            targetY += 0.08f;
        }

        if (RAISED_PLAYER_WINGS.equals(slug)) {
            targetY += 0.11f;
            targetZ += 0.14f;
        }

        return new AuraPlacement(
            -AuraPlacement.center(bounds[0], bounds[3]) * scale,
            targetY - AuraPlacement.center(bounds[1], bounds[4]) * scale,
            targetZ - bounds[2] * scale,
            scale,
            autoYaw
        );
    }

    /**
     * Port of CosmeticsManager.applyPlacement.
     */
    public static void a(class_4587 matrices, AuraPayload payload) {
        String category = payload.category;
        String slug = payload.slug;
        ModelAdjustment modelAdjustment = FIXED_MODEL_ADJUSTMENTS.getOrDefault(slug, NO_MODEL_ADJUSTMENT);
        float customHeight = "wings".equals(category) ? AuraPlacement.wingHeight(slug) : 0.0f;
        float customDistance = "wings".equals(category) ? AuraPlacement.wingDistance(slug) : 0.0f;
        matrices.method_46416(
            payload.placement.x + modelAdjustment.x,
            payload.placement.y + customHeight + modelAdjustment.y,
            payload.placement.z + customDistance + modelAdjustment.z
        );
        matrices.method_22905(payload.placement.scale, payload.placement.scale, payload.placement.scale);
        float adjustmentRotationX = modelAdjustment.rotationX;
        float adjustmentRotationY = modelAdjustment.rotationY;
        float adjustmentRotationZ = modelAdjustment.rotationZ;
        AuraPlacement.applyCenteredRotation(matrices, payload, payload.placement.autoYaw, adjustmentRotationX, adjustmentRotationY, adjustmentRotationZ);
        if ("wings".equals(category)) {
            float[] bounds = AuraPlacement.orientedBounds(payload.model, payload.orientationRotation, payload.orientationSign, payload.placement.autoYaw);
            float pivotX = AuraPlacement.center(bounds[0], bounds[3]);
            float pivotY = AuraPlacement.center(bounds[1], bounds[4]);
            float pivotZ = AuraPlacement.center(bounds[2], bounds[5]);
            matrices.method_46416(pivotX, pivotY, pivotZ);
            if (OPPOSITE_VERTICAL_WINGS.contains(slug)) {
                matrices.method_22905(-1.0f, 1.0f, 1.0f);
            } else {
                matrices.method_22905(1.0f, -1.0f, 1.0f);
            }

            matrices.method_46416(-pivotX, -pivotY, -pivotZ);
        }

        AuraPlacement.applyRotation(matrices, payload.orientationRotation[0], payload.orientationRotation[1], payload.orientationRotation[2]);
        if (payload.placement.autoYaw != 0.0f) {
            matrices.method_22907(class_7833.field_40716.rotationDegrees(payload.placement.autoYaw));
        }

        matrices.method_22905(payload.orientationSign[0], payload.orientationSign[1], payload.orientationSign[2]);
    }

    private static void applyCenteredRotation(class_4587 matrices, AuraPayload payload, float autoYaw, float rotationX, float rotationY, float rotationZ) {
        if (rotationX != 0.0f || rotationY != 0.0f || rotationZ != 0.0f) {
            float[] bounds = AuraPlacement.orientedBounds(payload.model, payload.orientationRotation, payload.orientationSign, autoYaw);
            float pivotX = AuraPlacement.center(bounds[0], bounds[3]);
            float pivotY = AuraPlacement.center(bounds[1], bounds[4]);
            float pivotZ = AuraPlacement.center(bounds[2], bounds[5]);
            matrices.method_46416(pivotX, pivotY, pivotZ);
            AuraPlacement.applyRotation(matrices, rotationX, rotationY, rotationZ);
            matrices.method_46416(-pivotX, -pivotY, -pivotZ);
        }
    }

    public static float[] orientedBounds(AuraModel model, float[] rotation, float[] sign, float autoYaw) {
        Matrix4f transform = new Matrix4f();
        if (rotation[2] != 0.0f) {
            transform.rotateZ((float)Math.toRadians(rotation[2]));
        }

        if (rotation[1] != 0.0f) {
            transform.rotateY((float)Math.toRadians(rotation[1]));
        }

        if (rotation[0] != 0.0f) {
            transform.rotateX((float)Math.toRadians(rotation[0]));
        }

        if (autoYaw != 0.0f) {
            transform.rotateY((float)Math.toRadians(autoYaw));
        }

        transform.scale(sign[0], sign[1], sign[2]);
        float[] result = new float[]{
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY
        };
        Vector3f point = new Vector3f();
        float[] xs = new float[]{model.minX, model.maxX};
        float[] ys = new float[]{model.minY, model.maxY};
        float[] zs = new float[]{model.minZ, model.maxZ};
        for (float x : xs) {
            for (float y : ys) {
                for (float z : zs) {
                    transform.transformPosition(x, y, z, point);
                    result[0] = Math.min(result[0], point.x);
                    result[3] = Math.max(result[3], point.x);
                    result[1] = Math.min(result[1], point.y);
                    result[4] = Math.max(result[4], point.y);
                    result[2] = Math.min(result[2], point.z);
                    result[5] = Math.max(result[5], point.z);
                }
            }
        }

        return result;
    }

    private static float wingHeight(String slug) {
        WingAdjustment adjustment = FIXED_WING_ADJUSTMENTS.get(slug);
        return adjustment == null ? 0.0f : adjustment.height;
    }

    private static float wingDistance(String slug) {
        WingAdjustment adjustment = FIXED_WING_ADJUSTMENTS.get(slug);
        return adjustment == null ? 0.0f : adjustment.distance;
    }

    static float safeRatio(float target, float value) {
        return value > 1.0e-4f ? target / value : 1.0f;
    }

    static float center(float min, float max) {
        return (min + max) * 0.5f;
    }

    private static void applyRotation(class_4587 matrices, float x, float y, float z) {
        if (z != 0.0f) {
            matrices.method_22907(class_7833.field_40718.rotationDegrees(z));
        }

        if (y != 0.0f) {
            matrices.method_22907(class_7833.field_40716.rotationDegrees(y));
        }

        if (x != 0.0f) {
            matrices.method_22907(class_7833.field_40714.rotationDegrees(x));
        }
    }
}
