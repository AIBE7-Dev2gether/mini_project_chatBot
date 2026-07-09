package com.example.archat.infrastructure.chat;

import com.example.archat.application.chat.ChatProvider;
import com.example.archat.domain.chat.Chat;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.util.List;

public class GenAIChatProvider implements ChatProvider {

    // 단일 챗
    @Override
    public String useAI(Chat chat) {
        try (Client client = GenAIConfig.getClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    chat.model(),
                    chat.message(),
                    GenAIConfig.getGenerateContentConfig(chat.model()));
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
            String textWithTime = c.message() + " [발송 시간: " + c.timestamp() + "]\n";

            if (currentRole == null) {
                currentRole = role;
                currentText.append(textWithTime);
            } else if (currentRole.equals(role)) {
                currentText.append(textWithTime); // 같은 역할이 연속되면 하나의 메시지로 병합 (API 오류 방지)
            } else {
                contents.add(Content.builder()
                        .role(currentRole)
                        .parts(Part.builder().text(currentText.toString().trim()).build())
                        .build());
                currentRole = role;
                currentText = new StringBuilder(textWithTime);
            }
        }
        if (currentRole != null) {
            contents.add(Content.builder()
                    .role(currentRole)
                    .parts(Part.builder().text(currentText.toString().trim()).build())
                    .build());
        }
        try (Client client = GenAIConfig.getClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    newChat.model(),
                    contents,
                    GenAIConfig.getGenerateContentConfig(newChat.model()));
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
        }
    }

    private GenAIChatProvider() {

    }

    private static final GenAIChatProvider instance = new GenAIChatProvider();

    public static GenAIChatProvider getInstance() {
        return instance;
    }

}