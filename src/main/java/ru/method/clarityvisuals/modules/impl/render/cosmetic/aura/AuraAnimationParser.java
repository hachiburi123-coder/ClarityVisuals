package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class AuraAnimationParser {
    private AuraAnimationParser() {
    }

    public static AuraAnimation a(JsonObject root) {
        try {
            if (root == null || !root.has("animations")) {
                return null;
            }

            JsonObject anims = root.getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> entry : anims.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject animationObject = entry.getValue().getAsJsonObject();
                    float length = animationObject.has("animation_length")
                        ? animationObject.get("animation_length").getAsFloat()
                        : 1.5f;
                    boolean loop = !animationObject.has("loop") || animationObject.get("loop").getAsBoolean();
                    AuraAnimation animation = new AuraAnimation(entry.getKey(), length, loop);
                    if (animationObject.has("bones")) {
                        JsonObject bonesObject = animationObject.getAsJsonObject("bones");
                        for (Map.Entry<String, JsonElement> boneEntry : bonesObject.entrySet()) {
                            if (boneEntry.getValue().isJsonObject()) {
                                String boneName = boneEntry.getKey();
                                JsonObject boneData = boneEntry.getValue().getAsJsonObject();
                                AuraBoneTimeline timeline = new AuraBoneTimeline();
                                if (boneData.has("rotation")) {
                                    AuraAnimationParser.parseKeyframes(boneData.get("rotation"), timeline.rotationKeyframes);
                                }

                                if (boneData.has("position")) {
                                    AuraAnimationParser.parseKeyframes(boneData.get("position"), timeline.positionKeyframes);
                                }

                                animation.bones.put(boneName, timeline);
                            }
                        }
                    }

                    return animation;
                }
            }

            return null;
        } catch (Exception exception) {
            ru.method.clarityvisuals.ClarityVisuals.LOGGER.warn("[Cosmetics] Failed to parse aura animation", exception);
            return null;
        }
    }

    private static void parseKeyframes(JsonElement element, List<AuraKeyframe> outList) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                float time;
                try {
                    time = Float.parseFloat(entry.getKey());
                } catch (NumberFormatException ignored) {
                    continue;
                }

                float[] values = AuraAnimationParser.parseVec3(entry.getValue());
                if (values != null) {
                    outList.add(new AuraKeyframe(time, values[0], values[1], values[2]));
                }
            }

            outList.sort(Comparator.comparingDouble(keyframe -> keyframe.time));
        } else if (element.isJsonArray()) {
            float[] values = AuraAnimationParser.parseVec3(element);
            if (values != null) {
                outList.add(new AuraKeyframe(0.0f, values[0], values[1], values[2]));
            }
        }
    }

    private static float[] parseVec3(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() >= 3) {
                return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("post") && object.get("post").isJsonArray()) {
                JsonArray array = object.getAsJsonArray("post");
                if (array.size() >= 3) {
                    return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
                }
            }
        }

        return null;
    }
}
