package com.example.archat.application.service;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;
import com.example.archat.domain.service.ChatService;
import com.example.archat.infrastructure.api.GenAIChatProvider;
import com.example.archat.infrastructure.api.GroqChatProvider;
import com.example.archat.infrastructure.api.NimChatProvider;
import com.example.archat.infrastructure.repository.SupabaseChatRepository;

import java.time.ZonedDateTime;
import java.util.List;

public class AIChatService implements ChatService {

    private final ChatRepository chatRepository;
//    private final ChatProvider chatProvider;
    private final GroqChatProvider groqChatProvider;
    private final GenAIChatProvider genAIChatProvider;
    private final NimChatProvider nimChatProvider;


    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
//        String aiResponse = useAI(chat);
        List<Chat> history = chatRepository.findAllByUserId(chat.userId());

        String aiResponse = null;
        if (chat.model().contains("gemini") || chat.model().contains("gemma")) {
            aiResponse = genAIChatProvider.useAI(chat, history);
        } else if (chat.model().contains("nvidia") || chat.model().contains("nemotron")) {
            aiResponse = nimChatProvider.useAI(chat, history);
        }else {
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
//        this.chatProvider = GenAIChatProvider.getInstance();
        this.genAIChatProvider = GenAIChatProvider.getInstance();
        this.groqChatProvider = GroqChatProvider.getInstance();

        this.nimChatProvider = NimChatProvider.getInstance();
    }

    private static final AIChatService instance = new AIChatService();

    public static AIChatService getInstance() {
        return instance;
    }

}