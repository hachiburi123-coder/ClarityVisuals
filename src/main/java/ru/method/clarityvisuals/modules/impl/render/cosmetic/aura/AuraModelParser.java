package ru.method.clarityvisuals.modules.impl.render.cosmetic.aura;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.HashMap;
import java.util.Map;

public final class AuraModelParser {
    private AuraModelParser() {
    }

    /**
     * Parses an embedded bedrock geometry object ("minecraft:geometry" wrapper or a geometry root).
     */
    public static AuraModel a(com.google.gson.JsonObject root) {
        try {
            if (root == null) {
                return null;
            }

            com.google.gson.JsonObject geometry = root;
            if (root.has("minecraft:geometry") && root.get("minecraft:geometry").isJsonArray()) {
                JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
                if (geometries.size() <= 0) {
                    return null;
                }

                geometry = geometries.get(0).getAsJsonObject();
            } else if (!root.has("bones")) {
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    if (entry.getKey().startsWith("geometry.") && entry.getValue().isJsonObject()) {
                        geometry = entry.getValue().getAsJsonObject();
                        break;
                    }
                }
            }

            return AuraModelParser.parseGeometryObject(geometry);
        } catch (Exception exception) {
            ru.method.clarityvisuals.ClarityVisuals.LOGGER.warn("[Cosmetics] Failed to parse aura geometry", exception);
            return null;
        }
    }

    private static AuraModel parseGeometryObject(com.google.gson.JsonObject geometry) {
        float textureWidth = 64.0f;
        float textureHeight = 64.0f;
        if (geometry.has("description")) {
            com.google.gson.JsonObject description = geometry.getAsJsonObject("description");
            if (description.has("texture_width")) {
                textureWidth = description.get("texture_width").getAsFloat();
            }

            if (description.has("texture_height")) {
                textureHeight = description.get("texture_height").getAsFloat();
            }
        } else {
            if (geometry.has("texturewidth")) {
                textureWidth = geometry.get("texturewidth").getAsFloat();
            }

            if (geometry.has("textureheight")) {
                textureHeight = geometry.get("textureheight").getAsFloat();
            }
        }

        AuraModel model = new AuraModel(textureWidth, textureHeight);
        if (!geometry.has("bones")) {
            return model;
        }

        Map<String, AuraBone> boneMap = new HashMap<String, AuraBone>();
        float lowestY = Float.MAX_VALUE;
        float highestY = -Float.MAX_VALUE;
        for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
            if (!boneElement.isJsonObject()) {
                continue;
            }

            com.google.gson.JsonObject boneObject = boneElement.getAsJsonObject();
            AuraBone bone = new AuraBone();
            bone.name = boneObject.has("name") ? boneObject.get("name").getAsString() : "";
            bone.parent = boneObject.has("parent") ? boneObject.get("parent").getAsString() : null;
            if (boneObject.has("pivot")) {
                JsonArray pivot = boneObject.getAsJsonArray("pivot");
                bone.pivot[0] = pivot.get(0).getAsFloat();
                bone.pivot[1] = pivot.get(1).getAsFloat();
                bone.pivot[2] = pivot.get(2).getAsFloat();
            }

            if (boneObject.has("rotation")) {
                JsonArray rotation = boneObject.getAsJsonArray("rotation");
                bone.rotation[0] = rotation.get(0).getAsFloat();
                bone.rotation[1] = rotation.get(1).getAsFloat();
                bone.rotation[2] = rotation.get(2).getAsFloat();
            }

            if (boneObject.has("cubes")) {
                for (JsonElement cubeElement : boneObject.getAsJsonArray("cubes")) {
                    if (cubeElement.isJsonObject()) {
                        com.google.gson.JsonObject cubeObject = cubeElement.getAsJsonObject();
                        AuraCube cube = AuraModelParser.parseCube(cubeObject, textureWidth, textureHeight);
                        if (cube != null) {
                            bone.cubes.add(cube);
                            if (cube.origin[1] < lowestY) {
                                lowestY = cube.origin[1];
                            }

                            if (cube.origin[1] + cube.size[1] > highestY) {
                                highestY = cube.origin[1] + cube.size[1];
                            }
                        }
                    }
                }
            }

            model.bones.add(bone);
            boneMap.put(bone.name, bone);
        }

        if (lowestY != Float.MAX_VALUE) {
            model.minY = lowestY;
            model.maxY = highestY;
        }

        for (AuraBone bone : model.bones) {
            if (bone.parent != null && boneMap.containsKey(bone.parent)) {
                boneMap.get(bone.parent).children.add(bone);
            }
        }

        model.recalculateBounds();
        return model;
    }

    private static AuraCube parseCube(com.google.gson.JsonObject cubeObject, float textureWidth, float textureHeight) {
        AuraCube cube = new AuraCube();
        if (cubeObject.has("origin")) {
            JsonArray origin = cubeObject.getAsJsonArray("origin");
            cube.origin[0] = origin.get(0).getAsFloat();
            cube.origin[1] = origin.get(1).getAsFloat();
            cube.origin[2] = origin.get(2).getAsFloat();
        }

        if (cubeObject.has("size")) {
            JsonArray size = cubeObject.getAsJsonArray("size");
            cube.size[0] = size.get(0).getAsFloat();
            cube.size[1] = size.get(1).getAsFloat();
            cube.size[2] = size.get(2).getAsFloat();
        }

        if (cubeObject.has("inflate")) {
            cube.inflate = cubeObject.get("inflate").getAsFloat();
        }

        if (cubeObject.has("mirror")) {
            cube.mirror = cubeObject.get("mirror").getAsBoolean();
        }

        if (cubeObject.has("pivot")) {
            JsonArray pivot = cubeObject.getAsJsonArray("pivot");
            cube.pivot[0] = pivot.get(0).getAsFloat();
            cube.pivot[1] = pivot.get(1).getAsFloat();
            cube.pivot[2] = pivot.get(2).getAsFloat();
        }

        if (cubeObject.has("rotation")) {
            JsonArray rotation = cubeObject.getAsJsonArray("rotation");
            cube.rotation[0] = rotation.get(0).getAsFloat();
            cube.rotation[1] = rotation.get(1).getAsFloat();
            cube.rotation[2] = rotation.get(2).getAsFloat();
        }

        float x0 = (cube.origin[0] - cube.inflate) / 16.0f;
        float y0 = (cube.origin[1] - cube.inflate) / 16.0f;
        float z0 = (cube.origin[2] - cube.inflate) / 16.0f;
        float x1 = (cube.origin[0] + cube.size[0] + cube.inflate) / 16.0f;
        float y1 = (cube.origin[1] + cube.size[1] + cube.inflate) / 16.0f;
        float z1 = (cube.origin[2] + cube.size[2] + cube.inflate) / 16.0f;
        if (cubeObject.has("uv") && cubeObject.get("uv").isJsonObject()) {
            com.google.gson.JsonObject uvObject = cubeObject.getAsJsonObject("uv");
            AuraModelParser.addPerFace(cube, uvObject, "north", x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0.0f, 0.0f, -1.0f, textureWidth, textureHeight);
            AuraModelParser.addPerFace(cube, uvObject, "south", x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0.0f, 0.0f, 1.0f, textureWidth, textureHeight);
            AuraModelParser.addPerFace(cube, uvObject, "east", x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, -1.0f, 0.0f, 0.0f, textureWidth, textureHeight);
            AuraModelParser.addPerFace(cube, uvObject, "west", x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, 1.0f, 0.0f, 0.0f, textureWidth, textureHeight);
            AuraModelParser.addPerFace(cube, uvObject, "up", x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0.0f, 1.0f, 0.0f, textureWidth, textureHeight);
            AuraModelParser.addPerFace(cube, uvObject, "down", x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0.0f, -1.0f, 0.0f, textureWidth, textureHeight);
        } else {
            float u = 0.0f;
            float v = 0.0f;
            if (cubeObject.has("uv") && cubeObject.get("uv").isJsonArray()) {
                JsonArray uvArray = cubeObject.getAsJsonArray("uv");
                u = uvArray.get(0).getAsFloat();
                v = uvArray.get(1).getAsFloat();
            }

            float dx = cube.size[0];
            float dy = cube.size[1];
            float dz = cube.size[2];
            AuraModelParser.addBoxQuad(cube, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1,
                (u + dz) / textureWidth, v / textureHeight, (u + dz + dx) / textureWidth, (v + dz) / textureHeight, 0.0f, 1.0f, 0.0f);
            AuraModelParser.addBoxQuad(cube, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0,
                (u + dz + dx) / textureWidth, v / textureHeight, (u + dz + dx + dx) / textureWidth, (v + dz) / textureHeight, 0.0f, -1.0f, 0.0f);
            AuraModelParser.addBoxQuad(cube, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0,
                (u + dz) / textureWidth, (v + dz) / textureHeight, (u + dz + dx) / textureWidth, (v + dz + dy) / textureHeight, 0.0f, 0.0f, -1.0f);
            AuraModelParser.addBoxQuad(cube, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1,
                (u + dz + dx + dz) / textureWidth, (v + dz) / textureHeight, (u + dz + dx + dz + dx) / textureWidth, (v + dz + dy) / textureHeight, 0.0f, 0.0f, 1.0f);
            AuraModelParser.addBoxQuad(cube, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0,
                u / textureWidth, (v + dz) / textureHeight, (u + dz) / textureWidth, (v + dz + dy) / textureHeight, -1.0f, 0.0f, 0.0f);
            AuraModelParser.addBoxQuad(cube, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1,
                (u + dz + dx) / textureWidth, (v + dz) / textureHeight, (u + dz + dx + dz) / textureWidth, (v + dz + dy) / textureHeight, 1.0f, 0.0f, 0.0f);
        }

        return cube;
    }

    private static void addPerFace(
        AuraCube cube,
        com.google.gson.JsonObject uvObject,
        String face,
        float x0, float y0, float z0,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float x3, float y3, float z3,
        float normalX, float normalY, float normalZ,
        float textureWidth, float textureHeight
    ) {
        if (uvObject.has(face)) {
            com.google.gson.JsonObject faceObject = uvObject.getAsJsonObject(face);
            if (faceObject.has("uv") && faceObject.has("uv_size")) {
                JsonArray uv = faceObject.getAsJsonArray("uv");
                JsonArray size = faceObject.getAsJsonArray("uv_size");
                float u0 = uv.get(0).getAsFloat() / textureWidth;
                float v0 = uv.get(1).getAsFloat() / textureHeight;
                float u1 = (uv.get(0).getAsFloat() + size.get(0).getAsFloat()) / textureWidth;
                float v1 = (uv.get(1).getAsFloat() + size.get(1).getAsFloat()) / textureHeight;
                AuraModelParser.addQuad(cube, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, u0, v0, u1, v1, normalX, normalY, normalZ);
            }
        }
    }

    private static void addQuad(
        AuraCube cube,
        float x0, float y0, float z0,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float x3, float y3, float z3,
        float u0, float v0, float u1, float v1,
        float normalX, float normalY, float normalZ
    ) {
        AuraVertex vertexA = new AuraVertex(x0, y0, z0, u0, v1);
        AuraVertex vertexB = new AuraVertex(x1, y1, z1, u1, v1);
        AuraVertex vertexC = new AuraVertex(x2, y2, z2, u1, v0);
        AuraVertex vertexD = new AuraVertex(x3, y3, z3, u0, v0);
        cube.quads.add(new AuraQuad(vertexA, vertexB, vertexC, vertexD, new org.joml.Vector3f(normalX, normalY, normalZ)));
    }

    private static void addBoxQuad(
        AuraCube cube,
        float x0, float y0, float z0,
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float x3, float y3, float z3,
        float u0, float v0, float u1, float v1,
        float normalX, float normalY, float normalZ
    ) {
        if (cube.mirror) {
            float swap = u0;
            u0 = u1;
            u1 = swap;
        }

        AuraModelParser.addQuad(cube, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, u0, v0, u1, v1, normalX, normalY, normalZ);
    }
}
