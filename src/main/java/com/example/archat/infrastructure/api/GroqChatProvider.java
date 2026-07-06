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

public class GroqChatProvider implements ChatProvider {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    // 단일 챗
    @Override

    public String useAI(Chat chat) {
        return useAI(chat, List.of());
    }

    // 히스토리 포함
    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        try {
            String body = buildBody(newChat, chatHistory);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(GroqConfig.ENDPOINT))
                    .header("Authorization", "Bearer " + GroqConfig.GROQ_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) {
                return "문제가 생겼어요 : Groq %d - %s".formatted(res.statusCode(), res.body());
            }

            JsonNode root = MAPPER.readTree(res.body());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
        }
    }

    // 요청 JSON 조립
    private String buildBody(Chat newChat, List<Chat> history) throws Exception {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("model", newChat.model());
        payload.put("max_tokens", GroqConfig.MAX_TOKENS);

        ArrayNode messages = payload.putArray("messages");

        // system 프롬프트
        messages.addObject()
                .put("role", "system")
                .put("content", GroqConfig.SYSTEM_INSTRUCTION);

        // 히스토리 (newChat은 이미 history에 저장되어 포함됨)
        for (Chat c : history) {
            messages.addObject()
                    .put("role", "USER".equals(c.owner()) ? "user" : "assistant")
                    .put("content", c.message());
        }

        return MAPPER.writeValueAsString(payload);
    }

    private GroqChatProvider() {

    }

    private static final GroqChatProvider instance = new GroqChatProvider();

    public static GroqChatProvider getInstance() {
        return instance;
    }

}
