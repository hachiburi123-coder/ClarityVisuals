package ru.method.clarityvisuals.utils.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;

public final class JumpHillMesh
implements MinecraftClientAccess {
    private static final int CELLS = 52;
    private static final int POINTS = CELLS + 1;
    private static final double WINDOW = 15.0;
    private static final double STEP = WINDOW * 2.0 / (double) CELLS;
    private static final double WORLD_PER_UNIT = 0.075;
    private static final double HEIGHT_BOOST = 1.3;
    private static final double SUN_X = 0.55;
    private static final double SUN_Y = 0.75;
    private static final double SUN_Z = 0.35;
    private static final Map<String, class_2960> BLOCK_TEXTURES = new HashMap<String, class_2960>();
    private static final Map<String, BufferedImage> PENDING_UPLOADS = new HashMap<String, BufferedImage>();
    private static final Queue<String> PENDING_QUEUE = new ArrayDeque<String>();
    private static boolean idLogged;

    private JumpHillMesh() {
    }

    public static final class Mesh {
        final class_243 origin;
        final float sizeMul;
        final float[] baseH = new float[POINTS * POINTS];
        final float[] gradX = new float[POINTS * POINTS];
        final float[] gradZ = new float[POINTS * POINTS];
        final float[] groundY = new float[POINTS * POINTS];
        final String[] blockIds = new String[POINTS * POINTS];

        Mesh(class_243 origin, float sizeMul) {
            this.origin = origin;
            this.sizeMul = sizeMul;
        }
    }

    public static Mesh build(class_243 origin, float sizeMul, double timeSeed) {
        Mesh mesh = new Mesh(origin, sizeMul);
        double worldScale = WORLD_PER_UNIT * (double) sizeMul;
        int baseY = (int) Math.floor(origin.field_1351) + 1;
        for (int j = 0; j < POINTS; ++j) {
            for (int i = 0; i < POINTS; ++i) {
                double lx = -WINDOW + (double) i * STEP;
                double lz = -WINDOW + (double) j * STEP;
                double wx = origin.field_1352 + lx * worldScale;
                double wz = origin.field_1350 + lz * worldScale;
                double h = heightAt(lx, lz, timeSeed);
                int k = j * POINTS + i;
                mesh.baseH[k] = (float) h;
                int ground = findGround(wx, baseY, wz);
                mesh.groundY[k] = (float) ground;
                mesh.blockIds[k] = blockIdAt(wx, ground - 1, wz);
            }
        }
        double eps = 0.4;
        for (int j = 0; j < POINTS; ++j) {
            for (int i = 0; i < POINTS; ++i) {
                double lx = -WINDOW + (double) i * STEP;
                double lz = -WINDOW + (double) j * STEP;
                int k = j * POINTS + i;
                mesh.gradX[k] = (float) ((heightAt(lx + eps, lz, timeSeed) - heightAt(lx - eps, lz, timeSeed)) / (2.0 * eps));
                mesh.gradZ[k] = (float) ((heightAt(lx, lz + eps, timeSeed) - heightAt(lx, lz - eps, timeSeed)) / (2.0 * eps));
            }
        }
        return mesh;
    }

    public static void render(class_4587 matrices, Mesh mesh, double grow, double masterAlpha) {
        flushPendingUploads();
        if (mesh == null || grow <= 0.01 || masterAlpha <= 0.01) {
            return;
        }
        float g = (float) grow;
        int alpha = (int) Math.round(255.0 * Math.min(1.0, masterAlpha));
        double worldScale = WORLD_PER_UNIT * (double) mesh.sizeMul;
        double sunLen = Math.sqrt(SUN_X * SUN_X + SUN_Y * SUN_Y + SUN_Z * SUN_Z);
        double sunX = SUN_X / sunLen;
        double sunY = SUN_Y / sunLen;
        double sunZ = SUN_Z / sunLen;
        for (int j = 0; j < CELLS; ++j) {
            for (int i = 0; i < CELLS; ++i) {
                int k00 = j * POINTS + i;
                int k10 = k00 + 1;
                int k01 = k00 + POINTS;
                int k11 = k01 + 1;
                double peak = Math.max(Math.max((double) mesh.baseH[k00], (double) mesh.baseH[k10]), Math.max((double) mesh.baseH[k01], (double) mesh.baseH[k11])) * (double) g * worldScale * HEIGHT_BOOST;
                if (peak < 0.025) {
                    continue;
                }
                double x0 = mesh.origin.field_1352 + (-WINDOW + (double) i * STEP) * worldScale;
                double x1 = mesh.origin.field_1352 + (-WINDOW + (double) (i + 1) * STEP) * worldScale;
                double z0 = mesh.origin.field_1350 + (-WINDOW + (double) j * STEP) * worldScale;
                double z1 = mesh.origin.field_1350 + (-WINDOW + (double) (j + 1) * STEP) * worldScale;
                class_243 p00 = new class_243(x0, (double) mesh.groundY[k00] + 0.02 + (double) mesh.baseH[k00] * (double) g * worldScale * HEIGHT_BOOST, z0);
                class_243 p10 = new class_243(x1, (double) mesh.groundY[k10] + 0.02 + (double) mesh.baseH[k10] * (double) g * worldScale * HEIGHT_BOOST, z0);
                class_243 p01 = new class_243(x0, (double) mesh.groundY[k01] + 0.02 + (double) mesh.baseH[k01] * (double) g * worldScale * HEIGHT_BOOST, z1);
                class_243 p11 = new class_243(x1, (double) mesh.groundY[k11] + 0.02 + (double) mesh.baseH[k11] * (double) g * worldScale * HEIGHT_BOOST, z1);
                class_2960 texture = textureFor(mesh.blockIds[k00]);
                if (texture == null) {
                    int fallback = (alpha << 24) | 0x808080;
                    Render3D.drawGradientQuad(matrices,
                        p00, p10, p11, p01,
                        new java.awt.Color(fallback, true), new java.awt.Color(fallback, true),
                        new java.awt.Color(fallback, true), new java.awt.Color(fallback, true), true);
                    continue;
                }
                int c00 = shadeWhite(mesh, k00, edgeAlpha(i, j), g, sunX, sunY, sunZ, alpha);
                int c10 = shadeWhite(mesh, k10, edgeAlpha(i + 1, j), g, sunX, sunY, sunZ, alpha);
                int c01 = shadeWhite(mesh, k01, edgeAlpha(i, j + 1), g, sunX, sunY, sunZ, alpha);
                int c11 = shadeWhite(mesh, k11, edgeAlpha(i + 1, j + 1), g, sunX, sunY, sunZ, alpha);
                Render3D.drawTexturedQuadCustom(matrices, texture,
                    p00, frac(x0), frac(z0), c00,
                    p10, frac(x1), frac(z0), c10,
                    p11, frac(x1), frac(z1), c11,
                    p01, frac(x0), frac(z1), c01,
                    true);
            }
        }
    }

    private static float frac(double v) {
        double f = v - Math.floor(v);
        if (f < 0.0f) {
            return 0.0f;
        }
        if (f > 1.0f) {
            return 1.0f;
        }
        return (float) f;
    }

    private static double edgeAlpha(int i, int j) {
        double lx = Math.abs(-WINDOW + (double) i * STEP);
        double lz = Math.abs(-WINDOW + (double) j * STEP);
        double m = Math.max(lx, lz);
        double t = (14.5 - m) / 3.0;
        if (t < 0.0) {
            return 0.0;
        }
        if (t > 1.0) {
            return 1.0;
        }
        return t * t * (3.0 - 2.0 * t);
    }

    private static int shadeWhite(Mesh mesh, int k, double edge, float grow, double sunX, double sunY, double sunZ, int alpha) {
        double nx = -(double) mesh.gradX[k] * (double) grow;
        double nz = -(double) mesh.gradZ[k] * (double) grow;
        double invLen = 1.0 / Math.sqrt(nx * nx + 1.0 + nz * nz);
        double diff = Math.max(0.0, (nx * sunX + sunY + nz * sunZ) * invLen);
        double light = 0.38 + 0.52 * diff;
        int v = (int) Math.min(255.0, light * 255.0);
        int a = (int) Math.round((double) alpha * edge);
        return a << 24 | v << 16 | v << 8 | v;
    }

    private static class_2960 textureFor(String id) {
        if (id == null) {
            return null;
        }
        return BLOCK_TEXTURES.get(id);
    }

    public static String debugStats(Mesh mesh) {
        if (mesh == null) {
            return "null";
        }
        int textured = 0;
        int total = POINTS * POINTS;
        Map<String, Integer> ids = new HashMap<String, Integer>();
        for (int k = 0; k < total; ++k) {
            String id = mesh.blockIds[k];
            if (id != null) {
                ids.put(id, Integer.valueOf(ids.getOrDefault(id, 0) + 1));
                if (BLOCK_TEXTURES.get(id) != null) {
                    ++textured;
                }
            }
        }
        return "textured=" + textured + "/" + total + " ids=" + ids.toString();
    }

    private static void flushPendingUploads() {
        if (PENDING_QUEUE.isEmpty()) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_1531() == null) {
            return;
        }
        int budget = 4;
        while (budget-- > 0 && !PENDING_QUEUE.isEmpty()) {
            String id = PENDING_QUEUE.poll();
            if (BLOCK_TEXTURES.containsKey(id)) {
                continue;
            }
            BufferedImage image = PENDING_UPLOADS.remove(id);
            if (image == null) {
                continue;
            }
            try {
                class_1011 nativeImage = new class_1011(image.getWidth(), image.getHeight(), true);
                for (int y = 0; y < image.getHeight(); ++y) {
                    for (int x = 0; x < image.getWidth(); ++x) {
                        nativeImage.method_61941(x, y, image.getRGB(x, y));
                    }
                }
                String safe = id.replace(':', '/');
                class_2960 texId = class_2960.method_60655((String) "clarityvisuals", (String) ("dynamic/jumpblock/" + safe));
                class_1043 texture = new class_1043(nativeImage);
                texture.method_4527(true, false);
                mc.method_1531().method_4616(texId, (net.minecraft.class_1044) texture);
                BLOCK_TEXTURES.put(id, texId);
            } catch (Throwable ignored) {
            }
        }
    }

    private static int findGround(double wx, int startY, double wz) {
        if (JumpHillMesh.aiG == null || JumpHillMesh.aiG.field_1687 == null) {
            return startY;
        }
        int bx = (int) Math.floor(wx);
        int bz = (int) Math.floor(wz);
        for (int y = startY; y > startY - 10; --y) {
            class_2680 state = JumpHillMesh.aiG.field_1687.method_8320(class_2338.method_49637((double) bx, (double) y, (double) bz));
            if (state == null || state.method_26215()) {
                continue;
            }
            return y + 1;
        }
        return startY - 9;
    }

    private static String blockIdAt(double wx, int y, double wz) {
        try {
            if (JumpHillMesh.aiG == null || JumpHillMesh.aiG.field_1687 == null) {
                return null;
            }
            int bx = (int) Math.floor(wx);
            int bz = (int) Math.floor(wz);
            class_2680 state = JumpHillMesh.aiG.field_1687.method_8320(class_2338.method_49637((double) bx, (double) y, (double) bz));
            if (state == null) {
                return null;
            }
            String text = state.method_26204().toString();
            if (!idLogged) {
                idLogged = true;
                ClarityVisuals.LOGGER.info("[JumpCircle] block toString sample: {}", text);
            }
            int open = text.indexOf('{');
            int close = text.lastIndexOf('}');
            String inner;
            if (open >= 0 && close > open + 1) {
                inner = text.substring(open + 1, close);
            } else {
                inner = text;
            }
            int bracket = inner.indexOf('[');
            if (bracket >= 0) {
                inner = inner.substring(0, bracket);
            }
            inner = inner.trim();
            if (inner.isEmpty() || inner.indexOf(':') < 0 && inner.indexOf('/') >= 0) {
                return null;
            }
            if (!BLOCK_TEXTURES.containsKey(inner) && !PENDING_UPLOADS.containsKey(inner)) {
                BufferedImage image = resolveBlockImage(inner);
                if (image != null) {
                    PENDING_UPLOADS.put(inner, image);
                    PENDING_QUEUE.add(inner);
                } else {
                    BLOCK_TEXTURES.put(inner, null);
                }
            }
            return inner;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static BufferedImage resolveBlockImage(String id) {
        try {
            String namespace = "minecraft";
            String path = id;
            int colon = id.indexOf(':');
            if (colon >= 0) {
                namespace = id.substring(0, colon);
                path = id.substring(colon + 1);
            }
            JsonObject textures = new JsonObject();
            String modelPath = "block/" + path;
            for (int depth = 0; depth < 4; ++depth) {
                JsonObject model = readJson("assets/" + namespace + "/models/" + modelPath + ".json");
                if (model == null) {
                    break;
                }
                if (model.has("textures") && model.get("textures").isJsonObject()) {
                    JsonObject own = model.getAsJsonObject("textures");
                    for (String key : own.keySet()) {
                        if (!textures.has(key) && own.get(key).isJsonPrimitive()) {
                            textures.add(key, own.get(key));
                        }
                    }
                }
                if (!model.has("parent")) {
                    break;
                }
                String parent = model.get("parent").getAsString();
                int pc = parent.indexOf(':');
                if (pc >= 0) {
                    namespace = parent.substring(0, pc);
                    parent = parent.substring(pc + 1);
                }
                if (parent.equals(modelPath)) {
                    break;
                }
                modelPath = parent;
            }
            String[] preference = new String[]{"all", "top", "side", "end", "particle", "cross", "plant", "texture"};
            for (String key : preference) {
                if (!textures.has(key)) {
                    continue;
                }
                String tex = textures.get(key).getAsString();
                if (tex.indexOf('#') >= 0) {
                    continue;
                }
                String texNs = "minecraft";
                String texPath = tex;
                int tc = tex.indexOf(':');
                if (tc >= 0) {
                    texNs = tex.substring(0, tc);
                    texPath = tex.substring(tc + 1);
                }
                BufferedImage image = readPng("assets/" + texNs + "/textures/" + texPath + ".png");
                if (image != null) {
                    return image;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static JsonObject readJson(String path) {
        try (InputStream in = JumpHillMesh.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            byte[] bytes = in.readAllBytes();
            String text = new String(bytes, StandardCharsets.UTF_8);
            return JsonParser.parseString(text).getAsJsonObject();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static BufferedImage readPng(String path) {
        try (InputStream in = JumpHillMesh.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return ImageIO.read(in);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static double heightAt(double lx, double lz, double t) {
        double sx = lx;
        double sz = 16.0 + lz;
        double c = Math.max(0.0, Math.sqrt(sx * sx + (sz - 16.0) * (sz - 16.0)));
        double cc = revSmoothstep(20.0, 5.0, c);
        cc *= cc;
        double[] n1 = noiseWithGrad(sx * 0.07, sz * 0.07, t * 0.5);
        double nn = n1[0] * Math.sqrt(n1[1] * n1[1] + n1[2] * n1[2] + n1[3] * n1[3]);
        double[] n2 = noiseWithGrad(sx * 0.173, sz * 0.173, t * 0.639);
        nn += 0.25 * n2[0] * Math.sqrt(n2[1] * n2[1] + n2[2] * n2[2] + n2[3] * n2[3]);
        nn = smoothstep(-0.5, 0.5, nn);
        return 6.0 * nn * cc;
    }

    private static double[] noiseWithGrad(double x, double y, double z) {
        double[] p = new double[]{Math.floor(x), Math.floor(y), Math.floor(z)};
        double[] w = new double[]{x - p[0], y - p[1], z - p[2]};
        double[] u = new double[3];
        for (int a = 0; a < 3; ++a) {
            double t = w[a];
            u[a] = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
        }
        double[][] g = new double[8][];
        g[0] = hash3(p[0], p[1], p[2]);
        g[1] = hash3(p[0] + 1.0, p[1], p[2]);
        g[2] = hash3(p[0], p[1] + 1.0, p[2]);
        g[3] = hash3(p[0] + 1.0, p[1] + 1.0, p[2]);
        g[4] = hash3(p[0], p[1], p[2] + 1.0);
        g[5] = hash3(p[0] + 1.0, p[1], p[2] + 1.0);
        g[6] = hash3(p[0], p[1] + 1.0, p[2] + 1.0);
        g[7] = hash3(p[0] + 1.0, p[1] + 1.0, p[2] + 1.0);
        double[] va = new double[8];
        va[0] = g[0][0] * w[0] + g[0][1] * w[1] + g[0][2] * w[2];
        va[1] = g[1][0] * (w[0] - 1.0) + g[1][1] * w[1] + g[1][2] * w[2];
        va[2] = g[2][0] * w[0] + g[2][1] * (w[1] - 1.0) + g[2][2] * w[2];
        va[3] = g[3][0] * (w[0] - 1.0) + g[3][1] * (w[1] - 1.0) + g[3][2] * w[2];
        va[4] = g[4][0] * w[0] + g[4][1] * w[1] + g[4][2] * (w[2] - 1.0);
        va[5] = g[5][0] * (w[0] - 1.0) + g[5][1] * w[1] + g[5][2] * (w[2] - 1.0);
        va[6] = g[6][0] * w[0] + g[6][1] * (w[1] - 1.0) + g[6][2] * (w[2] - 1.0);
        va[7] = g[7][0] * (w[0] - 1.0) + g[7][1] * (w[1] - 1.0) + g[7][2] * (w[2] - 1.0);
        double v = va[0] + u[0] * (va[1] - va[0]) + u[1] * (va[2] - va[0]) + u[2] * (va[4] - va[0])
            + u[0] * u[1] * (va[0] - va[1] - va[2] + va[3])
            + u[1] * u[2] * (va[0] - va[2] - va[4] + va[6])
            + u[2] * u[0] * (va[0] - va[1] - va[4] + va[5])
            + (-va[0] + va[1] + va[2] - va[3] + va[4] - va[5] - va[6] + va[7]) * u[0] * u[1] * u[2];
        double gx = g[0][0] + u[0] * (g[1][0] - g[0][0]) + u[1] * (g[2][0] - g[0][0]) + u[2] * (g[4][0] - g[0][0])
            + u[0] * u[1] * (g[0][0] - g[1][0] - g[2][0] + g[3][0])
            + u[1] * u[2] * (g[0][0] - g[2][0] - g[4][0] + g[6][0])
            + u[2] * u[0] * (g[0][0] - g[1][0] - g[4][0] + g[5][0])
            + (-g[0][0] + g[1][0] + g[2][0] - g[3][0] + g[4][0] - g[5][0] - g[6][0] + g[7][0]) * u[0] * u[1] * u[2];
        double gy = g[0][1] + u[0] * (g[1][1] - g[0][1]) + u[1] * (g[2][1] - g[0][1]) + u[2] * (g[4][1] - g[0][1])
            + u[0] * u[1] * (g[0][1] - g[1][1] - g[2][1] + g[3][1])
            + u[1] * u[2] * (g[0][1] - g[2][1] - g[4][1] + g[6][1])
            + u[2] * u[0] * (g[0][1] - g[1][1] - g[4][1] + g[5][1])
            + (-g[0][1] + g[1][1] + g[2][1] - g[3][1] + g[4][1] - g[5][1] - g[6][1] + g[7][1]) * u[0] * u[1] * u[2];
        double gz = g[0][2] + u[0] * (g[1][2] - g[0][2]) + u[1] * (g[2][2] - g[0][2]) + u[2] * (g[4][2] - g[0][2])
            + u[0] * u[1] * (g[0][2] - g[1][2] - g[2][2] + g[3][2])
            + u[1] * u[2] * (g[0][2] - g[2][2] - g[4][2] + g[6][2])
            + u[2] * u[0] * (g[0][2] - g[1][2] - g[4][2] + g[5][2])
            + (-g[0][2] + g[1][2] + g[2][2] - g[3][2] + g[4][2] - g[5][2] - g[6][2] + g[7][2]) * u[0] * u[1] * u[2];
        return new double[]{v, gx, gy, gz};
    }

    private static double[] hash3(double x, double y, double z) {
        double hx = x * 127.1 + y * 311.7 + z * 74.7;
        double hy = x * 269.5 + y * 183.3 + z * 246.1;
        double hz = x * 113.5 + y * 271.9 + z * 124.6;
        return new double[]{
            -1.0 + 2.0 * fract(Math.sin(hx) * 43758.5453123),
            -1.0 + 2.0 * fract(Math.sin(hy) * 43758.5453123),
            -1.0 + 2.0 * fract(Math.sin(hz) * 43758.5453123)};
    }

    private static double fract(double v) {
        return v - Math.floor(v);
    }

    private static double smoothstep(double e0, double e1, double x) {
        double t = (x - e0) / (e1 - e0);
        if (t < 0.0) {
            t = 0.0;
        } else if (t > 1.0) {
            t = 1.0;
        }
        return t * t * (3.0 - 2.0 * t);
    }

    private static double revSmoothstep(double e0, double e1, double x) {
        double t = (e0 - x) / (e0 - e1);
        if (t < 0.0) {
            t = 0.0;
        } else if (t > 1.0) {
            t = 1.0;
        }
        return t * t * (3.0 - 2.0 * t);
    }
}
