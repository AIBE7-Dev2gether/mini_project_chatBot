package com.example.archat.infrastructure.chat;

import com.example.archat.application.chat.ChatProvider;
import com.example.archat.domain.chat.Chat;
import com.example.archat.infrastructure.config.AiProperties;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.util.List;
import org.springframework.stereotype.Component;

@Component("genAIChatProvider")
public class GenAIChatProvider implements ChatProvider {

    private static final String SYSTEM_INSTRUCTION = "친절한 말투로, 100자 이내로, 가능한 한글로 답변.";

    private final String apiKey;

    public GenAIChatProvider(AiProperties properties) {
        this.apiKey = properties.geminiApiKey();
    }

    // 단일 챗
    @Override
    public String useAI(Chat chat) {
        try (Client client = createClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    chat.model(),
                    chat.message(),
                    createGenerateContentConfig(chat.model()));
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
        }
    }

    // 히스토리 포함
    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        List<Content> contents = new java.util.ArrayList<>();
        String currentRole = null;
        StringBuilder currentText = new StringBuilder();

        for (Chat c : chatHistory) {
            if (c.message() == null || c.message().trim().isEmpty()) {
                continue; // 이전에 저장된 빈 답변(오류)을 무시하여 '빈 답변 루프' 방지
            }
            String role = c.owner().equalsIgnoreCase("user") ? "user" : "model";
            String messageText = c.message() + "\n";

            if (currentRole == null) {
                currentRole = role;
                currentText.append(messageText);
            } else if (currentRole.equals(role)) {
                currentText.append(messageText); // 같은 역할이 연속되면 하나의 메시지로 병합 (API 오류 방지)
            } else {
                contents.add(Content.builder()
                        .role(currentRole)
                        .parts(Part.builder().text(currentText.toString().trim()).build())
                        .build());
                currentRole = role;
                currentText = new StringBuilder(messageText);
            }
        }
        if (currentRole != null) {
            contents.add(Content.builder()
                    .role(currentRole)
                    .parts(Part.builder().text(currentText.toString().trim()).build())
                    .build());
        }
        try (Client client = createClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    newChat.model(),
                    contents,
                    createGenerateContentConfig(newChat.model()));
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
        }
    }

    private Client createClient() {
        return Client.builder().apiKey(apiKey).build();
    }

    private com.google.genai.types.GenerateContentConfig createGenerateContentConfig(String modelName) {
        com.google.genai.types.GenerateContentConfig.Builder builder =
                com.google.genai.types.GenerateContentConfig.builder().maxOutputTokens(4096);

        if (modelName == null || !modelName.contains("gemma-4-26b")) {
            String timeContext = "참고로 현재 시스템 시각은 " + java.time.ZonedDateTime.now() + " 입니다.";
            builder.systemInstruction(
                    Content.builder()
                            .role("system")
                            .parts(Part.builder().text(SYSTEM_INSTRUCTION + "\n" + timeContext).build())
                            .build()
            );
        }

        if (modelName != null && modelName.contains("thinking")) {
            builder.thinkingConfig(
                    com.google.genai.types.ThinkingConfig.builder()
                            .includeThoughts(false)
                            .thinkingLevel(com.google.genai.types.ThinkingLevel.Known.MINIMAL)
                            .build()
            );
        }
        return builder.build();
    }
}
