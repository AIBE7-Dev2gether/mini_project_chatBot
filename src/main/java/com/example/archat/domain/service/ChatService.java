package com.example.archat.domain.service;

import com.example.archat.domain.model.Chat;

import java.util.List;

public interface ChatService {
    // application.service -> impl

    // presentation 용
    // -> 내부 로직을 보여줄 필요가 X
    // ai 관련된 내용이 없어도 됨

    // sessionId -> userId
    // 전체 데이터를 불러오기
    List<Chat> findArrByUserId(String userId);

    void save(Chat chat);
}
