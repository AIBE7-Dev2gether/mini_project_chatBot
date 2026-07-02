package com.example.archat.infrastructure.repository;

import com.example.archat.domain.model.Chat;
import com.example.archat.domain.repository.ChatRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// 1. ChatRepository로 업캐스팅
// 2. ChatRepository의 구현 책임을 가져간다
public class InMemoryChatRepository implements ChatRepository {
    private InMemoryChatRepository() {}

    public static final InMemoryChatRepository instance = new InMemoryChatRepository();

    public static InMemoryChatRepository getInstance() {
        return instance;
    }

    private final ConcurrentHashMap<String, List<Chat>> chatMap = new ConcurrentHashMap<>();

    @Override
    public void save(Chat chat) {
        chatMap.computeIfAbsent(
                chat.userId(),
                k -> new ArrayList<>()
        ).add(chat);
    }

    @Override
    public List<Chat> findAllByUserId(String userId) {
        return chatMap.getOrDefault(userId, Collections.emptyList());
    }
}
