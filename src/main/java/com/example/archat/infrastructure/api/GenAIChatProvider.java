package com.example.archat.infrastructure.api;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
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
                    GenAIConfig.getGenerateContentConfig());
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
        }
    }

    // 히스토리 포함
    @Override
    public String useAI(Chat newChat, List<Chat> chatHistory) {
        List<Content> contents = chatHistory.stream()
                .map((c) -> {
                    String textWithTime = c.message() + " [발송 시간: " + c.timestamp() + "]";

                    return Content.builder()
                            .role(newChat.owner().equals("USER") ? "user" : "model")
                            .parts(Part.builder().text(textWithTime).build())
                            .build();
                })
                .toList();
        try (Client client = GenAIConfig.getClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    newChat.model(),
                    contents,
                    GenAIConfig.getGenerateContentConfig());
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