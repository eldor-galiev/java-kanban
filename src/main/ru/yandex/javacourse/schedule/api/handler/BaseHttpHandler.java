package main.ru.yandex.javacourse.schedule.api.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseHttpHandler {
    protected static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .create();

    public static Gson getGson() {
        return gson;
    }

    private static class DurationSerializer implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
        @Override
        public JsonElement serialize(Duration duration, Type type, JsonSerializationContext context) {
            return duration == null ? JsonNull.INSTANCE : new JsonPrimitive(duration.toString());
        }

        @Override
        public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return json.isJsonNull() ? null : Duration.parse(json.getAsString());
        }
    }

    private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, JsonSerializationContext context) {
            return dateTime == null ? JsonNull.INSTANCE : new JsonPrimitive(dateTime.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString(), formatter);
        }
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, Object data) throws IOException {
        String json = gson.toJson(data);
        sendText(exchange, json, 200);
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        sendText(exchange, "", 201);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"message\": \"" + message + "\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"message\": \"" + message + "\"}", 406);
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"message\": \"" + message + "\"}", 500);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected int parseIdFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] pathParts = path.split("/");

        for (String part : pathParts) {
            try {
                return Integer.parseInt(part);
            } catch (NumberFormatException ignored) {
                //
            }
        }

        return -1;
    }
}