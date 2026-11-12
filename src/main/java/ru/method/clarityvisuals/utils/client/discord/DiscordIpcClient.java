package ru.method.clarityvisuals.utils.client.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import ru.method.clarityvisuals.ClarityVisuals;

/**
 * Чистый Java IPC-клиент Discord (без discord-rpc.dll).
 * Нужен ради кнопок: официальная discord-rpc.dll их не поддерживает,
 * а через IPC кнопки отправляются обычным JSON.
 */
public class DiscordIpcClient {
    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;
    private static final int OP_CLOSE = 2;
    private static final int OP_PING = 3;
    private static final int OP_PONG = 4;

    private final Object writeLock = new Object();
    private volatile RandomAccessFile pipe;
    private volatile boolean connected;
    private volatile String clientId;
    private Thread readThread;

    private final AtomicBoolean connectBusy = new AtomicBoolean(false);
    private final AtomicBoolean sendBusy = new AtomicBoolean(false);
    private volatile long sendStuckSince;

    /**
     * Неблокирующее подключение: вся работа с pipe идёт в фоновой нити,
     * чтобы зависший Discord не мог повесить вызывающий поток.
     */
    public void connectAsync(String applicationId) {
        if (this.isConnected() || !this.connectBusy.compareAndSet(false, true)) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                this.connect(applicationId);
            }
            catch (Throwable e) {
                ClarityVisuals.LOGGER.warn("[DiscordIPC] Async connect failed", e);
            }
            finally {
                this.connectBusy.set(false);
            }
        });
        t.setName("Discord-IPC-Connect");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Неблокирующая отправка активности. Если предыдущая отправка зависла
     * в write дольше 5 секунд — соединение считается мёртвым.
     */
    public void sendActivityAsync(String details, String state, long startTimestamp,
                                  String largeKey, String largeText,
                                  String smallKey, String smallText,
                                  String buttonLabel, String buttonUrl) {
        if (!this.isConnected()) {
            return;
        }
        if (!this.sendBusy.compareAndSet(false, true)) {
            if (System.currentTimeMillis() - this.sendStuckSince > 5000L) {
                this.markDead();
            }
            return;
        }
        this.sendStuckSince = System.currentTimeMillis();
        Thread t = new Thread(() -> {
            try {
                this.sendActivity(details, state, startTimestamp, largeKey, largeText,
                        smallKey, smallText, buttonLabel, buttonUrl);
            }
            finally {
                this.sendBusy.set(false);
            }
        });
        t.setName("Discord-IPC-Send");
        t.setDaemon(true);
        t.start();
    }

    public void clearActivityAsync() {
        if (!this.isConnected()) {
            return;
        }
        if (!this.sendBusy.compareAndSet(false, true)) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                this.clearActivity();
            }
            finally {
                this.sendBusy.set(false);
            }
        });
        t.setName("Discord-IPC-Send");
        t.setDaemon(true);
        t.start();
    }

    public boolean connect(String applicationId) {
        if (this.connected) {
            if (applicationId != null && applicationId.equals(this.clientId)) {
                return true;
            }
            this.close();
        }
        for (int i = 0; i < 10; i++) {
            String pipeName = "\\\\.\\pipe\\discord-ipc-" + i;
            try {
                RandomAccessFile raf = new RandomAccessFile(pipeName, "rw");
                this.pipe = raf;
                this.clientId = applicationId;
                JsonObject handshake = new JsonObject();
                handshake.addProperty("v", 1);
                handshake.addProperty("client_id", applicationId);
                this.send(OP_HANDSHAKE, handshake.toString().getBytes(StandardCharsets.UTF_8));
                // Ждём READY (с таймаутом чтения ниже, чтобы не висеть вечно)
                try {
                    Packet reply = this.readPacket(5000);
                    if (reply != null && reply.data != null && !reply.data.isEmpty()) {
                        try {
                            JsonObject json = JsonParser.parseString(reply.data).getAsJsonObject();
                            if (json.has("evt") && "ERROR".equalsIgnoreCase(json.get("evt").getAsString())) {
                                ClarityVisuals.LOGGER.warn("[DiscordIPC] Handshake error on {}: {}", pipeName, reply.data);
                                this.closePipe();
                                continue;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (IOException handshakeReadError) {
                    ClarityVisuals.LOGGER.warn("[DiscordIPC] No handshake reply on {}, пробую дальше", pipeName);
                    this.closePipe();
                    continue;
                }
                this.connected = true;
                this.startReadLoop();
                ClarityVisuals.LOGGER.info("[DiscordIPC] Connected via {}", pipeName);
                return true;
            } catch (IOException connectError) {
                this.closePipe();
            }
        }
        ClarityVisuals.LOGGER.warn("[DiscordIPC] Discord не найден (нет discord-ipc-0..9). Кнопки показываться не будут.");
        return false;
    }

    public boolean isConnected() {
        return this.connected && this.pipe != null;
    }

    public void sendActivity(String details, String state, long startTimestamp,
                             String largeKey, String largeText,
                             String smallKey, String smallText,
                             String buttonLabel, String buttonUrl) {
        if (!this.isConnected()) {
            return;
        }
        try {
            JsonObject activity = new JsonObject();
            if (details != null && !details.isEmpty()) {
                activity.addProperty("details", details);
            }
            if (state != null && !state.isEmpty()) {
                activity.addProperty("state", state);
            }
            JsonObject timestamps = new JsonObject();
            timestamps.addProperty("start", startTimestamp);
            activity.add("timestamps", timestamps);

            JsonObject assets = new JsonObject();
            if (largeKey != null && !largeKey.isEmpty()) {
                assets.addProperty("large_image", largeKey);
            }
            if (largeText != null && !largeText.isEmpty()) {
                assets.addProperty("large_text", largeText);
            }
            if (smallKey != null && !smallKey.isEmpty()) {
                assets.addProperty("small_image", smallKey);
            }
            if (smallText != null && !smallText.isEmpty()) {
                assets.addProperty("small_text", smallText);
            }
            if (assets.size() > 0) {
                activity.add("assets", assets);
            }

            if (buttonLabel != null && !buttonLabel.isEmpty() && buttonUrl != null && !buttonUrl.isEmpty()) {
                JsonArray buttons = new JsonArray();
                JsonObject btn = new JsonObject();
                btn.addProperty("label", buttonLabel.length() > 32 ? buttonLabel.substring(0, 32) : buttonLabel);
                btn.addProperty("url", buttonUrl);
                buttons.add(btn);
                activity.add("buttons", buttons);
            }

            JsonObject args = new JsonObject();
            args.addProperty("pid", (int) ProcessHandle.current().pid());
            args.add("activity", activity);

            JsonObject frame = new JsonObject();
            frame.addProperty("cmd", "SET_ACTIVITY");
            frame.add("args", args);
            frame.addProperty("nonce", UUID.randomUUID().toString());

            this.send(OP_FRAME, frame.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            ClarityVisuals.LOGGER.warn("[DiscordIPC] Failed to send activity", e);
            this.markDead();
        }
    }

    public void clearActivity() {
        if (!this.isConnected()) {
            return;
        }
        try {
            JsonObject args = new JsonObject();
            args.addProperty("pid", (int) ProcessHandle.current().pid());
            JsonObject frame = new JsonObject();
            frame.addProperty("cmd", "SET_ACTIVITY");
            frame.add("args", args);
            frame.addProperty("nonce", UUID.randomUUID().toString());
            this.send(OP_FRAME, frame.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    public synchronized void close() {
        this.connected = false;
        if (this.readThread != null) {
            this.readThread.interrupt();
            this.readThread = null;
        }
        this.closePipe();
    }

    private void markDead() {
        this.connected = false;
        this.closePipe();
    }

    private void closePipe() {
        RandomAccessFile raf = this.pipe;
        this.pipe = null;
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void send(int op, byte[] data) throws IOException {
        RandomAccessFile raf = this.pipe;
        if (raf == null) {
            throw new IOException("pipe closed");
        }
        synchronized (this.writeLock) {
            this.writeLeInt(raf, op);
            this.writeLeInt(raf, data.length);
            raf.write(data);
        }
    }

    private void writeLeInt(RandomAccessFile raf, int v) throws IOException {
        raf.write(v & 0xFF);
        raf.write((v >>> 8) & 0xFF);
        raf.write((v >>> 16) & 0xFF);
        raf.write((v >>> 24) & 0xFF);
    }

    private int readLeInt(RandomAccessFile raf) throws IOException {
        int b0 = raf.read();
        int b1 = raf.read();
        int b2 = raf.read();
        int b3 = raf.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException("pipe closed");
        }
        return (b0 & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 24);
    }

    private Packet readPacket() throws IOException {
        RandomAccessFile raf = this.pipe;
        if (raf == null) {
            throw new IOException("pipe closed");
        }
        int op = this.readLeInt(raf);
        int len = this.readLeInt(raf);
        if (len < 0 || len > 1024 * 1024) {
            throw new IOException("bad ipc frame len: " + len);
        }
        byte[] data = new byte[len];
        raf.readFully(data);
        return new Packet(op, new String(data, StandardCharsets.UTF_8));
    }

    private Packet readPacket(int timeoutMs) throws IOException {
        // Простое ожидание данных без busy-loop: опрашиваем available() нельзя,
        // поэтому читаем в отдельной нити с join(timeout).
        final Packet[] out = new Packet[1];
        final IOException[] err = new IOException[1];
        Thread t = new Thread(() -> {
            try {
                out[0] = this.readPacket();
            } catch (IOException e) {
                err[0] = e;
            }
        });
        t.setDaemon(true);
        t.start();
        try {
            t.join(timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        }
        if (t.isAlive()) {
            throw new IOException("handshake timeout");
        }
        if (out[0] != null) {
            return out[0];
        }
        throw err[0] != null ? err[0] : new IOException("no reply");
    }

    private void startReadLoop() {
        Thread t = new Thread(() -> {
            while (this.connected && this.pipe != null && !Thread.currentThread().isInterrupted()) {
                try {
                    Packet p = this.readPacket();
                    if (p == null) {
                        continue;
                    }
                    if (p.op == OP_PING) {
                        try {
                            this.send(OP_PONG, p.data != null ? p.data.getBytes(StandardCharsets.UTF_8) : new byte[0]);
                        } catch (IOException ignored) {
                        }
                    } else if (p.op == OP_CLOSE) {
                        ClarityVisuals.LOGGER.warn("[DiscordIPC] Discord closed connection");
                        this.markDead();
                        break;
                    }
                } catch (IOException e) {
                    if (this.connected) {
                        ClarityVisuals.LOGGER.warn("[DiscordIPC] Read error, reconnect next update");
                    }
                    this.markDead();
                    break;
                } catch (Exception e) {
                    ClarityVisuals.LOGGER.warn("[DiscordIPC] Read loop error", e);
                    this.markDead();
                    break;
                }
            }
        });
        t.setName("Discord-IPC-Read");
        t.setDaemon(true);
        this.readThread = t;
        t.start();
    }

    private record Packet(int op, String data) {
    }
}
