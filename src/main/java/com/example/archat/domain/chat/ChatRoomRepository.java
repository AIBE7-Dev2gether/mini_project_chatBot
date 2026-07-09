package com.example.archat.domain.chat;

import java.util.List;

public interface ChatRoomRepository {
    void save(ChatRoom chatRoom);
    List<ChatRoom> findAllByUserId(String userId);
    void deleteById(String id);
}
