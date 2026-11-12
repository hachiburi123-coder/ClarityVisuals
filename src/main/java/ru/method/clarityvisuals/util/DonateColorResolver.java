package ru.method.clarityvisuals.util;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_310;
import net.minecraft.class_5251;
import net.minecraft.class_640;

/**
 * Resolves display colors for donate/privilege names.
 * Order: saved (disk) -> hardcoded -> live tab lookup (result is saved).
 * Names are normalized (small-caps folded to plain lowercase) for matching,
 * display text itself is never modified here.
 */
public final class DonateColorResolver
implements MinecraftClientAccess {
    private static final Map<String, int[]> PRIVILEGE_GRADIENTS = new ConcurrentHashMap<String, int[]>(Map.of(
        "stinger", new int[]{0xFF0000, 0xFF0E00, 0xFF1B00, 0xFF2900, 0xFF3600, 0xFF4400, 0xFF5100},
        "dragon", new int[]{0xE000FF, 0xD80CFF, 0xD019FF, 0xC925FF, 0xC132FF, 0xB93EFF},
        "kraken", new int[]{0x2DFD00, 0x24FD1E, 0x1BFD3D, 0x12FD5B, 0x09FD7A, 0x00FD98},
        "eternity", new int[]{0xFF0095, 0xFF00A2, 0xFF00AF, 0xFF00BC, 0xFF00CA, 0xFF00D7, 0xFF00E4, 0xFF00F1},
        "wither", new int[]{0xFB4F00},
        "mustang", new int[]{0xEEBE00}
    ));
    private static final Map<String, int[]> GRAD_CACHE = new ConcurrentHashMap<String, int[]>();
    private static final Map<String, class_2583> STY_CACHE = new ConcurrentHashMap<String, class_2583>();
    private static final Map<String, int[]> SAVED = new ConcurrentHashMap<String, int[]>();
    private static final int[] NO_GRADIENT = new int[0];
    private static volatile long liveCacheAt;
    private static volatile boolean savedLoaded;

    private DonateColorResolver() {
    }

    public static void setPrivilegeGradient(String privilege, int[] rgb) {
        if (privilege == null || privilege.isBlank() || rgb == null || rgb.length == 0) {
            return;
        }
        PRIVILEGE_GRADIENTS.put(normalize(privilege), rgb.clone());
    }

    public static Integer resolve(String donate) {
        int[] grad = resolveGradient(donate);
        return grad == null || grad.length == 0 ? null : Integer.valueOf(grad[0]);
    }

    public static int[] resolveGradient(String donate) {
        if (donate == null || donate.isBlank()) {
            return null;
        }
        String key = normalize(donate);
        if (key.isEmpty()) {
            return null;
        }
        ensureSavedLoaded();
        int[] saved = SAVED.get(key);
        if (saved != null) {
            return saved;
        }
        int[] hard = PRIVILEGE_GRADIENTS.get(key);
        if (hard != null) {
            return hard;
        }
        long now = System.currentTimeMillis();
        if (now - liveCacheAt > 15000L) {
            liveCacheAt = now;
            GRAD_CACHE.clear();
            STY_CACHE.clear();
        }
        int[] cached = GRAD_CACHE.get(key);
        if (cached != null) {
            return cached.length == 0 ? null : cached;
        }
        int[] live = liveGradient(donate.trim(), key);
        GRAD_CACHE.put(key, live == null ? NO_GRADIENT : live);
        if (live != null) {
            saveColor(key, live);
        }
        return live;
    }

    public static class_2583 resolveStyleSample(String donate) {
        if (donate == null || donate.isBlank()) {
            return null;
        }
        String key = normalize(donate);
        if (key.isEmpty()) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (now - liveCacheAt > 15000L) {
            liveCacheAt = now;
            GRAD_CACHE.clear();
            STY_CACHE.clear();
        }
        if (STY_CACHE.containsKey(key)) {
            return STY_CACHE.get(key);
        }
        class_2583 sample = liveStyleSample(donate.trim(), key);
        if (sample != null) {
            STY_CACHE.put(key, sample);
        }
        return sample;
    }

    private static int[] liveGradient(String donate, String normDonate) {
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || mc.field_1724 == null || mc.field_1687 == null || mc.method_1562() == null) {
                return null;
            }
            class_269 scoreboard = mc.field_1687.method_8428();
            if (scoreboard == null) {
                return null;
            }
            for (Object o : (Collection<?>) mc.method_1562().method_45732()) {
                if (!(o instanceof class_640)) {
                    continue;
                }
                class_640 entry = (class_640) o;
                if (entry.method_2966() == null || entry.method_2966().getName() == null) {
                    continue;
                }
                class_268 team;
                try {
                    team = scoreboard.method_1164(entry.method_2966().getName());
                }
                catch (Throwable t) {
                    continue;
                }
                if (team == null) {
                    continue;
                }
                class_2561 prefix;
                try {
                    prefix = team.method_1144();
                }
                catch (Throwable t2) {
                    continue;
                }
                if (prefix == null || prefix.getString() == null || prefix.getString().isEmpty()) {
                    continue;
                }
                String normPlain = normalizeText(prefix.getString());
                int at = normPlain.indexOf(normDonate);
                if (at < 0) {
                    continue;
                }
                int[] grad = sliceGradient(prefix, at, normDonate.length());
                if (grad != null && grad.length > 0) {
                    return grad;
                }
            }
            return null;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static class_2583 liveStyleSample(String donate, String normDonate) {
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || mc.field_1724 == null || mc.field_1687 == null || mc.method_1562() == null) {
                return null;
            }
            class_269 scoreboard = mc.field_1687.method_8428();
            if (scoreboard == null) {
                return null;
            }
            for (Object o : (Collection<?>) mc.method_1562().method_45732()) {
                if (!(o instanceof class_640)) {
                    continue;
                }
                class_640 entry = (class_640) o;
                if (entry.method_2966() == null || entry.method_2966().getName() == null) {
                    continue;
                }
                class_268 team;
                try {
                    team = scoreboard.method_1164(entry.method_2966().getName());
                }
                catch (Throwable t) {
                    continue;
                }
                if (team == null) {
                    continue;
                }
                class_2561 prefix;
                try {
                    prefix = team.method_1144();
                }
                catch (Throwable t2) {
                    continue;
                }
                if (prefix == null || prefix.getString() == null || prefix.getString().isEmpty()) {
                    continue;
                }
                String normPlain = normalizeText(prefix.getString());
                int at = normPlain.indexOf(normDonate);
                if (at < 0) {
                    continue;
                }
                final class_2583[] found = new class_2583[1];
                final int[] index = new int[]{0};
                try {
                    prefix.method_27658((style, str) -> {
                        if (found[0] != null || str == null || str.isEmpty()) {
                            return Optional.empty();
                        }
                        int pos = index[0];
                        index[0] += str.length();
                        if (pos <= at && at < pos + str.length() && style != null) {
                            found[0] = style;
                        }
                        return Optional.empty();
                    }, class_2583.field_24360);
                }
                catch (Throwable t3) {
                    continue;
                }
                if (found[0] != null) {
                    return found[0];
                }
            }
            return null;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static int[] sliceGradient(class_2561 text, int start, int length) {
        try {
            final java.util.ArrayList<Integer> colors = new java.util.ArrayList<Integer>();
            final int[] index = new int[]{0};
            text.method_27658((style, str) -> {
                if (str == null || str.isEmpty()) {
                    return Optional.empty();
                }
                class_5251 tc = style == null ? null : style.method_10973();
                int rgb = tc == null ? 0xFFFFFF : tc.method_27716() & 0xFFFFFF;
                for (int i = 0; i < str.length(); ++i) {
                    int pos = index[0]++;
                    if (pos >= start && pos < start + length) {
                        colors.add(rgb);
                    }
                }
                return Optional.empty();
            }, class_2583.field_24360);
            if (colors.size() != length) {
                return null;
            }
            int[] out = new int[colors.size()];
            for (int i = 0; i < out.length; ++i) {
                out[i] = colors.get(i);
            }
            return out;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static void ensureSavedLoaded() {
        if (savedLoaded) {
            return;
        }
        savedLoaded = true;
        try {
            Path path = savePath();
            if (path == null || !Files.exists(path)) {
                return;
            }
            com.google.gson.JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    JsonArray arr = entry.getValue().getAsJsonArray();
                    if (arr.size() == 0) {
                        continue;
                    }
                    int[] grad = new int[arr.size()];
                    for (int i = 0; i < arr.size(); ++i) {
                        grad[i] = arr.get(i).getAsInt() & 0xFFFFFF;
                    }
                    SAVED.put(normalize(entry.getKey()), grad);
                }
                catch (Throwable ignored) {
                }
            }
        }
        catch (Throwable t) {
            ClarityVisuals.LOGGER.warn("[DonateColors] Failed to load saved colors", t);
        }
    }

    private static void saveColor(String key, int[] grad) {
        int[] copy = grad.clone();
        SAVED.put(key, copy);
        Thread t = new Thread(() -> {
            try {
                Path path = savePath();
                if (path == null) {
                    return;
                }
                Files.createDirectories(path.getParent());
                JsonObject root = new JsonObject();
                for (Map.Entry<String, int[]> entry : SAVED.entrySet()) {
                    JsonArray arr = new JsonArray();
                    for (int rgb : entry.getValue()) {
                        arr.add(rgb & 0xFFFFFF);
                    }
                    root.add(entry.getKey(), arr);
                }
                Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            }
            catch (Throwable e) {
                ClarityVisuals.LOGGER.warn("[DonateColors] Failed to save colors", e);
            }
        });
        t.setName("DonateColors-Save");
        t.setDaemon(true);
        t.start();
    }

    private static Path savePath() {
        try {
            return FabricLoader.getInstance().getConfigDir().resolve("ClarityVisuals_donate_colors_v2.json");
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static String toSmallCaps(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            sb.append(smallCapChar(Character.toLowerCase(s.charAt(i))));
        }
        return sb.toString();
    }

    private static char smallCapChar(char c) {
        switch (c) {
            case 'a': return '\u1d00';
            case 'b': return '\u0299';
            case 'c': return '\u1d04';
            case 'd': return '\u1d30';
            case 'e': return '\u1d07';
            case 'f': return '\ua730';
            case 'g': return '\u0262';
            case 'h': return '\u029c';
            case 'i': return '\u026a';
            case 'j': return '\u1d0a';
            case 'k': return '\u1d0b';
            case 'l': return '\u029f';
            case 'm': return '\u1d0d';
            case 'n': return '\u0274';
            case 'o': return '\u1d0f';
            case 'p': return '\u1d18';
            case 'r': return '\u0280';
            case 't': return '\u1d1b';
            case 'u': return '\u1d1c';
            case 'v': return '\u1d20';
            case 'w': return '\u1d21';
            case 'y': return '\u028f';
            case 'z': return '\u1d22';
            default: return c;
        }
    }

    static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return normalizeText(s).trim();
    }

    static String normalizeText(String s) {
        if (s == null) {
            return "";
        }
        String lower = s.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); ++i) {
            sb.append(foldChar(lower.charAt(i)));
        }
        return sb.toString();
    }

    private static char foldChar(char c) {
        switch (c) {
            case 'ᴀ': return 'a';
            case 'ʙ': return 'b';
            case 'ᴄ': return 'c';
            case 'ᴅ': return 'd';
            case 'ᴇ': return 'e';
            case 'ꜰ': return 'f';
            case 'ɢ': return 'g';
            case 'ʜ': return 'h';
            case 'ɪ': return 'i';
            case 'ᴊ': return 'j';
            case 'ᴋ': return 'k';
            case 'ʟ': return 'l';
            case 'ᴍ': return 'm';
            case 'ɴ': return 'n';
            case 'ᴏ': return 'o';
            case 'ᴘ': return 'p';
            case 'ʀ': return 'r';
            case 'ᴛ': return 't';
            case 'ᴜ': return 'u';
            case 'ᴠ': return 'v';
            case 'ᴡ': return 'w';
            case 'ʏ': return 'y';
            case 'ᴢ': return 'z';
            default: return c;
        }
    }
}
