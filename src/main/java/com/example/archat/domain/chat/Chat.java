package com.example.archat.domain.chat;

public record Chat(
        String message,
        String owner,
        String userId,
        String roomId,
        String model,
        String timestamp
) {
}
