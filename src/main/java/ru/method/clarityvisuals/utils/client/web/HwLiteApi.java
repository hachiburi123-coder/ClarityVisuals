package ru.method.clarityvisuals.utils.client.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_634;
import net.minecraft.class_8710;
import net.minecraft.class_9139;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;
import ru.method.clarityvisuals.api.event.ClientTickEvent;
import ru.method.clarityvisuals.managers.system.ModuleManager;
import ru.method.clarityvisuals.module.Module;

/**
 * In-Game API серверов HolyWorld (LiteAPI).
 * Транспорт — plugin messaging канал {@code liteapi:feature-control}, содержимое — UTF-8 JSON.
 * Сразу после входа на сервер отправляем {@code checkFeatures}: список активных функций мода,
 * сервер отвечает блок-листом. Заблокированные функции полностью скрываются из GUI
 * и не могут быть включены до следующей проверки. На серверах без LiteAPI канал не
 * зарегистрирован ({@code canSend} == false) — мод молча ничего не делает.
 *
 * Документация: https://wiki.holyworld.me/lite/api/protocol.md
 */
public final class HwLiteApi
implements MinecraftClientAccess {
    private static final String CLIENT_ID = "clarityvisuals";
    private static final class_2960 CHANNEL = class_2960.method_60655("liteapi", "feature-control");
    private static final long RATE_LIMIT_MS = 10000L;
    private static final int JOIN_DELAY_TICKS = 40;
    private static final int RETRY_DELAY_TICKS = 20;
    private static final long REQUEST_TIMEOUT_MS = 5L;
    // имя модуля -> имена функций в словаре LiteAPI (server-side blocklist)
    private static final Map<String, List<String>> FEATURES = Map.ofEntries(
        Map.entry("Sprint", List.of("sprint")),
        Map.entry("Fullbright", List.of("fullbright")),
        Map.entry("Zoom", List.of("zoom")),
        Map.entry("Freelook", List.of("freelook")),
        Map.entry("TargetEsp", List.of("esp", "targetesp")),
        Map.entry("AntiInvis", List.of("antiinvis")),
        Map.entry("ShiftTap", List.of("shifttap")));

    public static final HwLiteApi INSTANCE = new HwLiteApi();

    private final Map<UUID, CompletableFuture<List<String>>> pending = new ConcurrentHashMap<UUID, CompletableFuture<List<String>>>();
    private class_634 lastHandler = null;
    private int joinCountdown = 0;
    private boolean joinRetried = false;
    private long lastRequestMs = 0L;

    private HwLiteApi() {
        ClarityVisuals.getInstance().getEventHandler().subscribe(this);
    }

    /**
     * Регистрация payload-типа и приёмника канала. Вызывается один раз при старте клиента.
     */
    public static void register() {
        PayloadTypeRegistry.playC2S().register(Payload.ID, Payload.CODEC);
        PayloadTypeRegistry.playS2C().register(Payload.ID, Payload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(Payload.ID, (payload, context) -> {
            context.client().execute(() -> HwLiteApi.handle(payload.a()));
        });
    }

    @EventHandler
    public void onTick(ClientTickEvent clientTickEvent) {
        if (Module.fullNullCheck()) {
            return;
        }
        class_634 class_6342 = HwLiteApi.aiG.method_1562();
        if (class_6342 == null) {
            if (this.lastHandler != null) {
                this.pending.clear();
                this.joinCountdown = 0;
                this.apply(List.of(), true);
            }
            this.lastHandler = null;
            return;
        }
        if (class_6342 != this.lastHandler) {
            this.lastHandler = class_6342;
            this.joinCountdown = JOIN_DELAY_TICKS;
            this.joinRetried = false;
        }
        if (this.joinCountdown > 0 && --this.joinCountdown == 0) {
            this.sendCheck();
        }
    }

    private void sendCheck() {
        if (!ClientPlayNetworking.canSend(Payload.ID)) {
            if (!this.joinRetried) {
                // При входе канал мог ещё не успеть зарегистрироваться в handshake —
                // через секунду пробуем ещё раз, прежде чем считать сервер "без LiteAPI".
                this.joinRetried = true;
                this.joinCountdown = RETRY_DELAY_TICKS;
                return;
            }
            // Сервер действительно не поддерживает LiteAPI (ванила/обычный сервер).
            ClarityVisuals.LOGGER.info("[ClarityVisuals] [LiteAPI] channel 'liteapi:feature-control' not supported by this server, skipping checkFeatures");
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastRequestMs < RATE_LIMIT_MS) {
            return;
        }
        this.lastRequestMs = l;
        UUID uUID = UUID.randomUUID();
        ArrayList<String> arrayList = new ArrayList<String>();
        for (List<String> list : FEATURES.values()) {
            for (String string : list) {
                if (arrayList.contains(string)) continue;
                arrayList.add(string);
            }
        }
        JsonArray jsonArray = new JsonArray();
        for (String string : arrayList) {
            jsonArray.add(string);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("client", CLIENT_ID);
        jsonObject.add("features", jsonArray);
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("id", uUID.toString());
        jsonObject2.addProperty("method", "checkFeatures");
        jsonObject2.add("payload", jsonObject);
        CompletableFuture<List<String>> completableFuture = new CompletableFuture<List<String>>();
        this.pending.put(uUID, completableFuture);
        // Доказательство интеграции в логе: факт отправки запроса.
        ClarityVisuals.LOGGER.info("[ClarityVisuals] [LiteAPI] -> checkFeatures: client='{}', features=[{}]", CLIENT_ID, String.join(", ", arrayList));
        try {
            ClientPlayNetworking.send(new Payload(jsonObject2.toString().getBytes(StandardCharsets.UTF_8)));
        }
        catch (Throwable throwable) {
            this.pending.remove(uUID);
            return;
        }
        completableFuture.orTimeout(REQUEST_TIMEOUT_MS, TimeUnit.SECONDS).whenComplete((list, throwable) -> {
            this.pending.remove(uUID);
            if (throwable != null || list == null) {
                // таймаут: сервер не LiteAPI либо не ответил
                ClarityVisuals.LOGGER.warn("[ClarityVisuals] [LiteAPI] no response within {}s (id={})", REQUEST_TIMEOUT_MS, uUID);
                return;
            }
            HwLiteApi.INSTANCE.apply(list, false);
        });
    }

    private static void handle(byte[] byteArray) {
        JsonObject jsonObject;
        String string = new String(byteArray, StandardCharsets.UTF_8);
        try {
            jsonObject = JsonParser.parseString((String)string).getAsJsonObject();
        }
        catch (Exception exception) {
            return;
        }
        // Доказательство интеграции в логе: сырой ответ сервера (обрезаем до 512 символов).
        String string2 = string.length() > 512 ? string.substring(0, 512) + "…" : string;
        ClarityVisuals.LOGGER.info("[ClarityVisuals] [LiteAPI] <- {}", string2);
        if (jsonObject.has("event") && !jsonObject.has("id")) {
            return; // push-события LiteAPI пока не используются
        }
        if (!jsonObject.has("id")) {
            return;
        }
        UUID uUID;
        try {
            uUID = UUID.fromString(jsonObject.get("id").getAsString());
        }
        catch (Exception exception) {
            return;
        }
        CompletableFuture<List<String>> completableFuture = HwLiteApi.INSTANCE.pending.remove(uUID);
        if (completableFuture == null) {
            return;
        }
        if (!jsonObject.has("ok") || jsonObject.get("ok").isJsonNull()) {
            completableFuture.complete(null);
            return;
        }
        if (!jsonObject.get("ok").getAsBoolean()) {
            completableFuture.complete(List.of()); // ошибка сервера — блокировки не применяем
            return;
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        if (jsonObject.has("payload") && !jsonObject.get("payload").isJsonNull()) {
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("payload");
            if (jsonObject2.has("blocklist") && !jsonObject2.get("blocklist").isJsonNull()) {
                for (JsonElement jsonElement : jsonObject2.getAsJsonArray("blocklist")) {
                    arrayList.add(jsonElement.getAsString().toLowerCase(Locale.ROOT));
                }
            }
        }
        completableFuture.complete(arrayList);
    }

    /**
     * Применение блок-листа: блокируем/разблокируем модули, глушим включённые.
     */
    private void apply(List<String> blocklist, boolean bl) {
        ClarityVisuals clarityVisuals = ClarityVisuals.getInstance();
        ModuleManager moduleManager = clarityVisuals == null ? null : clarityVisuals.getModuleManager();
        if (moduleManager == null) {
            return;
        }
        List<String> list = blocklist == null ? List.of() : blocklist;
        for (Module module : moduleManager.getModules()) {
            List<String> list2 = FEATURES.get(module.getName());
            if (list2 == null) {
                continue;
            }
            boolean bl2 = false;
            for (String string : list2) {
                if (!list.contains(string.toLowerCase(Locale.ROOT))) continue;
                bl2 = true;
                break;
            }
            if (bl2 == module.isServerBlocked()) {
                continue;
            }
            module.setServerBlocked(bl2);
            if (bl2 && module.isToggled()) {
                clarityVisuals.setSuppressToggleFeedback(true);
                module.setToggled(false);
                clarityVisuals.setSuppressToggleFeedback(false);
            }
        }
        // В чат ничего не выводим: единственное доказательство интеграции — строки в логе.
    }

    /**
     * Payload канала liteapi:feature-control — сырой UTF-8 JSON в byte[].
     */
    public record Payload(byte[] a)
    implements class_8710 {
        public static final class_8710.class_9154<Payload> ID = new class_8710.class_9154<Payload>(HwLiteApi.CHANNEL);
        // LiteAPI использует сырые UTF-8 JSON байты БЕЗ length-префикса (как в официальном
        // примере HolyWorld): пишем/читаем всё содержимое буфера целиком через netty ByteBuf API.
        public static final class_9139<class_2540, Payload> CODEC = class_9139.method_56438(
            (payload, buf) -> buf.writeBytes(payload.a()),
            buf -> {
                byte[] bs = new byte[buf.readableBytes()];
                buf.readBytes(bs);
                return new Payload(bs);
            });

        @Override
        public class_8710.class_9154<? extends class_8710> method_56479() {
            return ID;
        }
    }
}
