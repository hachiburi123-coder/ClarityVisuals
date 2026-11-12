package ru.method.clarityvisuals.network;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;

public class TelegramClient {
    public TelegramClient(HttpClient httpClient) {
    }

    public void close() {
    }

    public TelegramResponse a(String string, String string2, Path path, String string3, Duration duration) {
        return TelegramResponse.df("disabled");
    }

    public static final class TelegramResponse {
        public TelegramResponse(boolean bl, int n, String string, String string2) {
        }

        public static TelegramResponse df(String string) {
            return new TelegramResponse(false, -1, "", string == null ? "disabled" : string);
        }

        public boolean rY() {
            return false;
        }

        public int rZ() {
            return 200;
        }

        public String sa() {
            return "";
        }

        public String sb() {
            return "";
        }
    }
}
