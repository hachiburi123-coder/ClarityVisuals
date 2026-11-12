package ru.method.clarityvisuals.utils.client.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Публичное API HolyWorld: список серверов и активных ивентов.
 * Та же апишка, что использует HW Events в Clarity.
 */
public final class HwEventsApi {
    private static final String BASE_URL = "https://api.holyworld.me/v1";

    public record HwEvent(String name, String rarity, String type, String server, long voteEndsAt, boolean voting) {
        public String displayName() {
            return this.name != null && !this.name.isBlank() ? this.name : typeName(this.type);
        }

        public static String typeName(String type) {
            if (type == null) {
                return "";
            }
            return switch (type) {
                case "CUBE" -> "Игральный куб";
                case "MINE" -> "Смертельная шахта";
                case "PARCELS" -> "Посылка";
                case "SHIP" -> "Таинственный корабль";
                case "CARGO" -> "Таинственный груз";
                case "FLOWER" -> "Цветочная поляна";
                case "TYPO" -> "Опытный Тыпо";
                case "CONTAINER" -> "Контейнер";
                case "JAYCOB" -> "Тайна Джейкоба";
                case "SNOWQUARRY" -> "Снежный карьер";
                case "GOLD" -> "Золотая лихорадка";
                case "HUNT" -> "Трофейная охота";
                case "VOTE" -> "Голосование";
                default -> type;
            };
        }

        public String displayRarity() {
            if (this.rarity != null && !this.rarity.isEmpty()) {
                String r = this.rarity.toLowerCase();
                if (r.contains("default") || r.contains("normal") || r.contains("ordinary") || r.contains("common") || r.contains("basic") || r.contains("typo")) {
                    return "Обычная";
                } else if (r.contains("rare")) {
                    return "Редкая";
                } else if (r.contains("epic")) {
                    return "Эпическая";
                } else if (r.contains("legend")) {
                    return "Легендарная";
                } else if (r.contains("roskosh")) {
                    return "Роскошная";
                } else if (r.contains("zazhitochn") || r.contains("bogat") || r.contains("rich")) {
                    return "Зажиточная";
                } else if (r.contains("peaceful")) {
                    return "Мирная";
                } else if (r.contains("deadly")) {
                    return "Смертельная";
                } else if (r.contains("explosive")) {
                    return "Взрывоопасная";
                } else if (r.contains("special")) {
                    return "Особая";
                } else if (r.contains("обычн") || r.contains("обыкн")) {
                    return "Обычная";
                } else if (r.contains("редк")) {
                    return "Редкая";
                } else if (r.contains("эпическ")) {
                    return "Эпическая";
                } else if (r.contains("легенд")) {
                    return "Легендарная";
                } else if (r.contains("роскошн")) {
                    return "Роскошная";
                } else if (r.contains("зажит")) {
                    return "Зажиточная";
                } else if (r.contains("особ")) {
                    return "Особая";
                } else {
                    return r.matches("[a-z0-9_\\-]+") ? "Обычная" : this.rarity;
                }
            } else {
                return "Обычная";
            }
        }
    }

    public static List<HwEvent> fetchEvents(String serverId) {
        String url = BASE_URL + "/events";
        if (serverId != null && !serverId.isEmpty()) {
            url = url + "?server=" + URLEncoder.encode(serverId, StandardCharsets.UTF_8);
        }
        String json = get(url);
        List<HwEvent> events = new ArrayList<>();
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (Exception e) {
            return events;
        }
        if (root.isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray()) {
                HwEvent event = parseEvent(element, serverId);
                if (event != null) {
                    events.add(event);
                }
            }
        } else if (root.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
                String server = entry.getKey();
                if (entry.getValue().isJsonArray()) {
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        HwEvent event = parseEvent(element, server);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                }
            }
        }
        return events;
    }

    public static Map<String, String> fetchServers() {
        Map<String, String> servers = new LinkedHashMap<>();
        String json = get(BASE_URL + "/servers");
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (Exception e) {
            return servers;
        }
        if (root.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
                servers.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return servers;
    }

    private static HwEvent parseEvent(JsonElement element, String server) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        JsonObject obj = element.getAsJsonObject();
        String type = getString(obj, "id");
        String name = "";
        String rarity = "";
        long voteEndsAt = -1L;
        boolean voting = false;
        if (obj.has("metadata") && obj.get("metadata").isJsonObject()) {
            JsonObject meta = obj.getAsJsonObject("metadata");
            name = getString(meta, "displayName");
            rarity = getString(meta, "rare");
            for (Map.Entry<String, JsonElement> entry : meta.entrySet()) {
                String key = entry.getKey().toLowerCase();
                JsonElement value = entry.getValue();
                if (value.isJsonPrimitive()) {
                    if (value.getAsJsonPrimitive().isNumber()) {
                        long parsed = parseTimeValue(key, value.getAsLong());
                        if (parsed > 0L && voteEndsAt < 0L) {
                            voteEndsAt = parsed;
                        }
                    } else {
                        Long parsed = parseTimeString(value.getAsString());
                        if (parsed != null && voteEndsAt < 0L) {
                            voteEndsAt = parsed;
                        }
                    }
                }
                if (key.contains("vote") && !key.contains("end") && !key.contains("until")) {
                    voting = true;
                }
            }
        }
        if (type.equalsIgnoreCase("VOTE") || type.toLowerCase().contains("vote")) {
            voting = true;
        }
        String[] knownTimeKeys = new String[]{
            "startsat", "startat", "starttime", "votestart", "voteuntil",
            "votetimestamp", "voteends", "until", "timer", "countdown",
            "cooldownend", "startstamp", "timestamp", "votestarts"
        };
        for (String key : knownTimeKeys) {
            long parsed = parseTimeValue(key, obj);
            if (parsed > 0L && voteEndsAt < 0L) {
                voteEndsAt = parsed;
            }
        }
        return new HwEvent(name, rarity, type, server, voteEndsAt, voting);
    }

    private static long parseTimeValue(String key, JsonObject obj) {
        if (!obj.has(key)) {
            return -1L;
        }
        JsonElement value = obj.get(key);
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            return parseTimeValue(key, value.getAsLong());
        } else if (value.isJsonPrimitive()) {
            Long parsed = parseTimeString(value.getAsString());
            return parsed == null ? -1L : parsed;
        } else {
            return -1L;
        }
    }

    private static long parseTimeValue(String key, long num) {
        long now = System.currentTimeMillis();
        if (num > 1000000000000L) {
            return num;
        } else if (num > 1000000000L) {
            return num * 1000L;
        } else {
            return num > 0L ? now + num * 1000L : -1L;
        }
    }

    private static Long parseTimeString(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            long num = Long.parseLong(str.trim());
            long now = System.currentTimeMillis();
            if (num > 1000000000000L) {
                return num;
            } else if (num > 1000000000L) {
                return num * 1000L;
            } else {
                return num > 0L ? now + num * 1000L : null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private static String get(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "ClarityVisuals/2.0");
            connection.setRequestProperty("Accept", "application/json");
            int code = connection.getResponseCode();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    code >= 400 ? connection.getErrorStream() : connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            return "[]";
        }
    }

    private HwEventsApi() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
