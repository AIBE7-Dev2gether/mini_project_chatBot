package com.example.archat.domain.chat;

public record ChatRoom(
        String id,
        String userId,
        String title,
        String createdAt
) {
}
