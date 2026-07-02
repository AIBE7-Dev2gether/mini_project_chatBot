package com.example.archat.application.service;

import com.example.archat.application.port.ChatProvider;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.infrastructure.api.GenAIChatProvider;
import com.example.archat.infrastructure.repository.SupabaseChatRepository;

import java.time.ZonedDateTime;
import java.util.List;

public class GeminiChatService implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatProvider chatProvider;

    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
//        String aiResponse = useAI(chat);
        List<Chat> history = chatRepository.findAllByUserId(chat.userId());
        String aiResponse = chatProvider.useAI(chat, history);
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

    private GeminiChatService() {
        this.chatRepository = SupabaseChatRepository.getInstance();
        this.chatProvider = GenAIChatProvider.getInstance();
    }

    private static final GeminiChatService instance = new GeminiChatService();

    public static GeminiChatService getInstance() {
        return instance;
    }

}