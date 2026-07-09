package com.example.archat.presentation.chat;

import com.example.archat.domain.chat.Chat;

public record ChatResponseDTO(String owner, String model, String message, String timestamp) {
    public String getOwner() {
        return owner;
    }

    public String getModel() {
        return model;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static ChatResponseDTO of(Chat chat) {
        return new ChatResponseDTO(
                chat.owner(),
                chat.model(),
                chat.message(),
                chat.timestamp()
        );
    }
}
