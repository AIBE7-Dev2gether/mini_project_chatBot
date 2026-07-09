package com.example.archat.application.chat;

import com.example.archat.domain.chat.Chat;

import java.util.List;

public interface ChatUseCase {
    // application.service -> impl

    // presentation 용
    // -> 내부 로직을 보여줄 필요가 X
    // ai 관련된 내용이 없어도 됨

    // sessionId -> userId
    // 전체 데이터를 불러오기
    List<Chat> findAllByUserId(String userId);

    void save(Chat chat);
}
