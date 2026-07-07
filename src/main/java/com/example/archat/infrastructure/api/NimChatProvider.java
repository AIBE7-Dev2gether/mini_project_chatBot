package com.example.archat.infrastructure.api;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class NimChatProvider implements ChatProvider {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Override
    public String useAI(Chat chat) {
        return useAI(chat, List.of());
    }

    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        try {
            String body = buildBody(newChat, chatHistory);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(NimConfig.ENDPOINT))
                    .header("Authorization", "Bearer " + NimConfig.NIM_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                return "문제가 생겼어요 : NIM %d - %s".formatted(res.statusCode(), res.body());
            }

            JsonNode root = MAPPER.readTree(res.body());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
        }
    }

    private String buildBody(Chat newChat, List<Chat> history) throws Exception {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("model", newChat.model());
        payload.put("max_tokens", NimConfig.MAX_TOKENS);

        ArrayNode messages = payload.putArray("messages");
        messages.addObject().put("role", "system").put("content", NimConfig.SYSTEM_INSTRUCTION);

        for (Chat c : history) {
            messages.addObject()
                    .put("role", "USER".equals(c.owner()) ? "user" : "assistant")
                    .put("content", c.message());
        }

        return MAPPER.writeValueAsString(payload);
    }

    private NimChatProvider() {}

    private static final NimChatProvider instance = new NimChatProvider();

    public static NimChatProvider getInstance() {
        return instance;
    }
}