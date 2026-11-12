package ru.method.clarityvisuals.utils.client.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.class_310;
import ru.method.clarityvisuals.modules.impl.render.cosmetic.emotion.EmotionWheelManager;

/**
 * Порт канала эмоций из auravisual CosmeticsNetworkClient (JDK java.net.http.WebSocket
 * без новых зависимостей). Синхронизация эмоций между игроками: hello/ping/emotion.
 * Косметика-сообщения (cosmetics_sync/user_cosmetics) игнорируются — в ClarityVisuals их нет.
 */
public final class EmotionNetwork {
    public static final EmotionNetwork INSTANCE = new EmotionNetwork();
    private static final String[] SERVER_URLS = new String[]{"ws://152.89.199.138:7660", "wss://auravisuals.space/ws/cosmetics"};
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static final class_310 aiG = class_310.method_1551();
    private final AtomicReference<WebSocket> socketRef = new AtomicReference<WebSocket>();
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private int currentUrlIndex;
    private long lastReconnectMs;
    private long lastPingMs;
    private volatile String pendingEmotion;
    private volatile String lastWorldScope = "";

    private EmotionNetwork() {
    }

    public void tick() {
        long l = System.currentTimeMillis();
        WebSocket webSocket = this.socketRef.get();
        if (webSocket == null && !this.connecting.get()) {
            if (l - this.lastReconnectMs > 5000L) {
                this.lastReconnectMs = l;
                this.connect();
            }
        } else if (webSocket != null) {
            if (l - this.lastPingMs > 20000L) {
                this.lastPingMs = l;
                this.sendPing();
            }
            String string = this.getWorldScope();
            if (!string.equals(this.lastWorldScope)) {
                this.sendHello();
            }
        }
    }

    public synchronized void connect() {
        if (this.connecting.get()) {
            return;
        }
        if (this.socketRef.get() != null) {
            return;
        }
        this.connecting.set(true);
        try {
            URI uri = URI.create(SERVER_URLS[this.currentUrlIndex]);
            HTTP.newWebSocketBuilder().buildAsync(uri, new WebSocket.Listener(){
                private String buffer = "";

                @Override
                public void onOpen(WebSocket webSocket) {
                    EmotionNetwork.this.connecting.set(false);
                    EmotionNetwork.this.socketRef.set(webSocket);
                    webSocket.request(1L);
                    EmotionNetwork.this.sendHello();
                    String string = EmotionNetwork.this.pendingEmotion;
                    if (string != null) {
                        EmotionNetwork.this.pendingEmotion = null;
                        EmotionNetwork.this.sendEmotion(string);
                    }
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence charSequence, boolean bl) {
                    webSocket.request(1L);
                    this.buffer = this.buffer + charSequence.toString();
                    if (!bl) {
                        return CompletableFuture.completedFuture(null);
                    }
                    String string = this.buffer;
                    this.buffer = "";
                    aiG.execute(() -> EmotionNetwork.this.handleMessage(string));
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int n, String string) {
                    EmotionNetwork.this.socketRef.compareAndSet(webSocket, null);
                    EmotionNetwork.this.connecting.set(false);
                    EmotionNetwork.this.currentUrlIndex = (EmotionNetwork.this.currentUrlIndex + 1) % EmotionNetwork.SERVER_URLS.length;
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public void onError(WebSocket webSocket, Throwable throwable) {
                    EmotionNetwork.this.socketRef.compareAndSet(webSocket, null);
                    EmotionNetwork.this.connecting.set(false);
                    EmotionNetwork.this.currentUrlIndex = (EmotionNetwork.this.currentUrlIndex + 1) % EmotionNetwork.SERVER_URLS.length;
                }
            }).whenComplete((webSocket, throwable) -> {
                this.connecting.set(false);
                if (throwable != null) {
                    this.socketRef.set(null);
                    this.currentUrlIndex = (this.currentUrlIndex + 1) % SERVER_URLS.length;
                }
            });
        }
        catch (Throwable throwable) {
            this.connecting.set(false);
            this.currentUrlIndex = (this.currentUrlIndex + 1) % SERVER_URLS.length;
        }
    }

    public void sendHello() {
        WebSocket webSocket = this.socketRef.get();
        if (webSocket == null) {
            this.connect();
        } else {
            String string = this.getPlayerName();
            if (!string.isEmpty()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "hello");
                jsonObject.addProperty("name", string);
                String string2 = this.getWorldScope();
                this.lastWorldScope = string2;
                jsonObject.addProperty("scope", string2);
                jsonObject.add("equipped", new JsonObject());
                try {
                    webSocket.sendText(jsonObject.toString(), true);
                }
                catch (Exception exception) {
                    // сокет умер — переподключение на тике
                }
            }
        }
    }

    void sendPing() {
        WebSocket webSocket = this.socketRef.get();
        if (webSocket == null) {
            return;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "ping");
        try {
            webSocket.sendText(jsonObject.toString(), true);
        }
        catch (Exception exception) {
            // сокет умер — переподключение на тике
        }
    }

    public void sendEmotion(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        WebSocket webSocket = this.socketRef.get();
        if (webSocket == null) {
            this.pendingEmotion = string;
            this.connect();
        } else {
            String string2 = this.getPlayerName();
            if (!string2.isEmpty()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "emotion");
                jsonObject.addProperty("name", string2);
                jsonObject.addProperty("emotion", string);
                try {
                    webSocket.sendText(jsonObject.toString(), true);
                }
                catch (Exception exception) {
                    // сокет умер — переподключение на тике
                }
            }
        }
    }

    void handleMessage(String string) {
        try {
            JsonElement jsonElement = JsonParser.parseString(string);
            if (!jsonElement.isJsonObject()) {
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String string2 = jsonObject.has("type") ? jsonObject.get("type").getAsString() : "";
            if ("emotion".equals(string2)) {
                String string3 = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
                String string4 = jsonObject.has("emotion") ? jsonObject.get("emotion").getAsString() : "";
                if (!string3.isEmpty() && !string4.isEmpty()) {
                    EmotionWheelManager.INSTANCE.playRemoteEmotion(string3, string4);
                }
            }
            // cosmetics_sync / user_cosmetics — косметика этого клиента не существует, пропускаем.
        }
        catch (Exception exception) {
            // невалидный пакет — игнорируем
        }
    }

    public String getPlayerName() {
        if (aiG.field_1724 != null) {
            if (aiG.field_1724.method_7334() != null && aiG.field_1724.method_7334().getName() != null && !aiG.field_1724.method_7334().getName().isBlank()) {
                return aiG.field_1724.method_7334().getName().trim();
            }
            if (aiG.field_1724.method_5476() != null && !aiG.field_1724.method_5476().getString().isBlank()) {
                return aiG.field_1724.method_5476().getString().trim();
            }
        }
        return "User";
    }

    String getWorldScope() {
        if (aiG.field_1687 != null && aiG.method_1558() != null) {
            String string = aiG.method_1558().field_3761;
            if (string != null && !string.isBlank()) {
                String string2 = aiG.field_1687.method_27983().method_29177().toString().toLowerCase(Locale.ROOT);
                return string.trim().toLowerCase(Locale.ROOT) + "|" + string2;
            }
            return "";
        }
        return "";
    }
}
