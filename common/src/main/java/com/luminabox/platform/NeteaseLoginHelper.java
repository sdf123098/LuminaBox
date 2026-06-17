package com.luminabox.platform;

import com.luminabox.LuminaBox;
import com.luminabox.config.ModConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class NeteaseLoginHelper {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private static String currentKey = null;
    private static boolean polling = false;

    public static class LoginResult {
        public final int code;
        public final String message;
        public final String cookie;

        public LoginResult(int code, String message, String cookie) {
            this.code = code;
            this.message = message;
            this.cookie = cookie;
        }
    }

    /**
     * Starts the login flow by fetching a unique key and returning the QR code URL.
     */
    public static CompletableFuture<String> startLogin() {
        currentKey = null;
        polling = false;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://music.163.com/api/login/qrcode/uniquekey"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (json.has("uniquekey")) {
                        currentKey = json.get("uniquekey").getAsString();
                        // Official QR code authorization page
                        return "https://music.163.com/login?codekey=" + currentKey;
                    }
                }
                throw new RuntimeException("Failed to get unique key from NetEase");
            });
    }

    /**
     * Checks the status of the current QR code login.
     */
    public static CompletableFuture<LoginResult> checkStatus() {
        if (currentKey == null) {
            return CompletableFuture.completedFuture(new LoginResult(800, "Not initialized", ""));
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://music.163.com/api/login/qrcode/client/check?key=" + currentKey))
            .POST(HttpRequest.BodyPublishers.noBody())
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    int code = json.get("code").getAsInt();
                    String message = json.get("message").getAsString();
                    String cookie = json.has("cookie") ? json.get("cookie").getAsString() : "";
                    
                    if (code == 803 && !cookie.isEmpty()) {
                        // Success, save cookie
                        ModConfig.getInstance().setPlatformCookie(cookie);
                    }
                    return new LoginResult(code, message, cookie);
                }
                return new LoginResult(500, "Server error: " + response.statusCode(), "");
            });
    }
}
