package com.example.archat.application.port;

import com.example.archat.domain.model.Chat;

import java.util.List;

public interface ChatProvider {
    default String useAI(Chat chat) {
        return useAI(chat, List.of());
    }

    String useAI(Chat newChat, List<Chat> chatHistory);
}