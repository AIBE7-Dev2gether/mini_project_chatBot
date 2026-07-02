package com.example.archat.application.service;

import com.example.archat.infrastructure.api.GenAIConfig;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.infrastructure.repository.InMemoryChatRepository;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.time.ZonedDateTime;
import java.util.List;

public class GeminiChatService {

    private final ChatRepository chatRepository;

    // 1. 생성자를 private으로 변경
    private GeminiChatService() {
        // ChatService - InMemoryChatRepository
        // 생성자에서 엮여있긴 함. -> chatRepository을 호출하는 메서드는 InMemoryChatRepository 알아야함?
        // No. 만약 SupabaseChatRepository로 바꾼다면 생성자에서 진행하는 의존성 주입을 바꿔주기만 하면 OK
        this.chatRepository = InMemoryChatRepository.getInstance();
    }

    // 2. instance를 static에 등록
    private static final GeminiChatService instance = new GeminiChatService();

    // 3. getInstance() 메서드 만들기 (나중에 상위에서 쓸)
    public static GeminiChatService getInstance() {
        return instance;
    }

    public void sendMessage(Chat chat) {
        // 유저 데이터를 저장
        chatRepository.save(chat);
        // AI 데이터를 생성
        // ...
        String aiResponse = useAI(chat);
        Chat aiChat = new Chat(
                aiResponse,
                "AI",
                chat.userId(),
                chat.model(),
                ZonedDateTime.now().toString()
        );
        chatRepository.save(aiChat);
    }

    private String useAI(Chat chat) {
        List<Chat> history = chatRepository.findAllByUserId(chat.userId());
        List<Content> contents = history.stream()
                .map((c) -> Content.builder()
                        .role(chat.owner().equals("USER") ? "user" : "model")
                        .parts(Part.builder().text(c.message()).build())
                        .build())
                .toList();

        try (Client client = GenAIConfig.getClient()) {
            GenerateContentResponse response = client.models.generateContent(
                    chat.model(),
                    contents,
//                    chat.message(),
                    GenAIConfig.getGenerateContentConfig());
            return response.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "문제가 생겼어요 : %s".formatted(e.getMessage());
            // 원래 사용자에게 노출 X
        }
//        return
//                "%s라고 하셨네요.".formatted(chat.message());
    }

    public List<Chat> readHistory(String userId) {
        return chatRepository.findAllByUserId(userId);
    }

}
