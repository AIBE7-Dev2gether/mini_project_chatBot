package com.example.archat.domain.chat;

import java.util.List;

public interface ChatRepository {
    // Create, Read
    // You Ain't Gonna Need it

    void save(Chat chat);

    List<Chat> findAllByUserId(String userId);
}
