package com.example.archat.domain.chat;

import java.util.List;

public interface ChatRoomRepository {
    void save(ChatRoom chatRoom);
    List<ChatRoom> findAllByUserId(String userId);
    boolean existsByIdAndUserId(String id, String userId);
    void deleteByIdAndUserId(String id, String userId);
    void updateTitle(String id, String userId, String title);
}
