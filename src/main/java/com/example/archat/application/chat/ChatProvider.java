package com.example.archat.application.chat;

import com.example.archat.domain.chat.Chat;

import java.util.List;

public interface ChatProvider {
    default String useAI(Chat chat) {
        return useAI(chat, List.of());
    }

    String useAI(Chat newChat, List<Chat> chatHistory);
}