package com.example.archat.infrastructure.chat;

import com.example.archat.application.chat.ChatProvider;
import com.example.archat.domain.chat.Chat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class OpenAICompatibleProvider implements ChatProvider {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final String endpoint;
    private final String apiKey;
    private final String systemInstruction;
    private final int maxTokens;

    public OpenAICompatibleProvider(String endpoint, String apiKey, String systemInstruction, int maxTokens) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.systemInstruction = systemInstruction;
        this.maxTokens = maxTokens;
    }

    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        try {
            String body = buildBody(newChat, chatHistory);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                return "문제가 생겼어요 : API " + res.statusCode() + " - " + res.body();
            }

            JsonNode root = MAPPER.readTree(res.body());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : " + e.getMessage();
        }
    }

    private String buildBody(Chat newChat, List<Chat> history) throws Exception {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("model", newChat.model());
        payload.put("max_tokens", maxTokens);

        ArrayNode messages = payload.putArray("messages");

        String timeContext = "참고로 현재 시스템 시각은 " + java.time.ZonedDateTime.now().toString() + " 입니다.";
        messages.addObject()
                .put("role", "system")
                .put("content", systemInstruction + "\n" + timeContext);

        for (Chat c : history) {
            messages.addObject()
                    .put("role", "USER".equals(c.owner()) ? "user" : "assistant")
                    .put("content", c.message());
        }

        return MAPPER.writeValueAsString(payload);
    }
}
