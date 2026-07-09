package com.example.archat.application.chat;

import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRoom;

import java.util.List;

public interface ChatUseCase {
    // application.service -> impl
    ChatRoom createRoom(String userId, String title);
    List<ChatRoom> findAllRooms(String userId);
    void deleteRoom(String roomId);
    void renameRoom(String roomId, String title);

    // presentation 용
    // -> 내부 로직을 보여줄 필요가 X
    // ai 관련된 내용이 없어도 됨

    // sessionId -> userId
    // 전체 데이터를 불러오기
    List<Chat> findAllByRoomId(String roomId);

    void save(Chat chat);
}
