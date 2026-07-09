package com.example.archat.presentation.chat;

import com.example.archat.domain.chat.ChatRoom;

public record ChatRoomResponseDTO(String id, String title) {
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public static ChatRoomResponseDTO of(ChatRoom room) {
        return new ChatRoomResponseDTO(room.id(), room.title());
    }
}
