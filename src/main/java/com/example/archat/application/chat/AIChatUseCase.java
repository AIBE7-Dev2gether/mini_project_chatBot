package com.example.archat.application.chat;

import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.application.chat.ChatProvider;
import com.example.archat.infrastructure.chat.GenAIChatProvider;
import com.example.archat.infrastructure.chat.GroqConfig;
import com.example.archat.infrastructure.chat.NimConfig;
import com.example.archat.infrastructure.chat.OpenAICompatibleProvider;
import com.example.archat.infrastructure.chat.SupabaseChatRepository;

import java.time.ZonedDateTime;
import java.util.List;

public class AIChatUseCase implements ChatUseCase {

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
    private AIChatUseCase() {
        this.chatRepository = SupabaseChatRepository.getInstance();
        this.genAIChatProvider = GenAIChatProvider.getInstance();
        this.groqChatProvider = new OpenAICompatibleProvider(
                GroqConfig.ENDPOINT, GroqConfig.GROQ_API_KEY, GroqConfig.SYSTEM_INSTRUCTION, GroqConfig.MAX_TOKENS
        );
        this.nimChatProvider = new OpenAICompatibleProvider(
                NimConfig.ENDPOINT, NimConfig.NIM_API_KEY, NimConfig.SYSTEM_INSTRUCTION, NimConfig.MAX_TOKENS
        );
    }

    private static final AIChatUseCase instance = new AIChatUseCase();

    public static AIChatUseCase getInstance() {
        return instance;
    }

}