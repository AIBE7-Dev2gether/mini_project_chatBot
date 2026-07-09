package com.example.archat.domain.chat;

import java.util.List;

public interface ChatRepository {
    void save(Chat chat);

    List<Chat> findAllByRoomId(String roomId);

    void deleteByRoomId(String roomId);
}
