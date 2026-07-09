package com.example.archat.application.service;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.application.port.ChatProvider;
import com.example.archat.infrastructure.api.GenAIChatProvider;
import com.example.archat.infrastructure.api.GroqConfig;
import com.example.archat.infrastructure.api.NimConfig;
import com.example.archat.infrastructure.api.OpenAICompatibleProvider;
import com.example.archat.infrastructure.repository.SupabaseChatRepository;

import java.time.ZonedDateTime;
import java.util.List;

public class AIChatService implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatProvider groqChatProvider;
    private final ChatProvider genAIChatProvider;
    private final ChatProvider nimChatProvider;


    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
        List<Chat> history = chatRepository.findAllByUserId(chat.userId());

        String aiResponse = null;
        if (chat.model().contains("gemini") || chat.model().contains("gemma")) {
            aiResponse = genAIChatProvider.useAI(chat, history);
        }  else if (chat.model().contains("nvidia") || chat.model().contains("nemotron") || chat.model().startsWith("meta/")) {
            aiResponse = nimChatProvider.useAI(chat, history);
        } else {
            aiResponse = groqChatProvider.useAI(chat, history);
        }

        Chat aiChat = new Chat(
                aiResponse,
                "AI",
                chat.userId(),
                chat.model(),
                ZonedDateTime.now().toString()
        );
        chatRepository.save(aiChat);
    }

    @Override
    public List<Chat> findAllByUserId(String userId) {
        return chatRepository.findAllByUserId(userId);
    }

    // 싱글톤 등록
    private AIChatService() {
        this.chatRepository = SupabaseChatRepository.getInstance();
        this.genAIChatProvider = GenAIChatProvider.getInstance();
        this.groqChatProvider = new OpenAICompatibleProvider(
                GroqConfig.ENDPOINT, GroqConfig.GROQ_API_KEY, GroqConfig.SYSTEM_INSTRUCTION, GroqConfig.MAX_TOKENS
        );
        this.nimChatProvider = new OpenAICompatibleProvider(
                NimConfig.ENDPOINT, NimConfig.NIM_API_KEY, NimConfig.SYSTEM_INSTRUCTION, NimConfig.MAX_TOKENS
        );
    }

    private static final AIChatService instance = new AIChatService();

    public static AIChatService getInstance() {
        return instance;
    }

}