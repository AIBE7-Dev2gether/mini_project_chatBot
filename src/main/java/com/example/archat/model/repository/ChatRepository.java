package com.example.archat.model.repository;

import com.example.archat.model.Chat;

import java.util.List;

public interface ChatRepository {
    // Create, Read
    // You Ain't Gonna Need it

    void save(Chat chat);

    List<Chat> findAllByUserId(String userId);
}
